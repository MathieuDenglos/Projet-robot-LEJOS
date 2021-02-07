import java.util.ArrayList;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.SensorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Motor;
import lejos.nxt.Button;

public class Robot {

    // Donnés nécessaire au robot
    private boolean tresor;
    private Orientation orientation = Orientation.NORD;
    private final ColorSensor capteur_couleur = new ColorSensor(SensorPort.S3);
    private NXTRegulatedMotor moteur_gauche = Motor.B;
    private NXTRegulatedMotor moteur_droite = Motor.A;
    private final float coefficient_rotation = (float) ((2.0 * 54.0) / (56.0)); // 2*excentricité des roues/D roue
    private final int distance_roue_capteur = 69; // distance entre le centre de rotation et le capteur
    private Color couleur_scannee;
    private int red_avg = 140; // Valeur de la consigne du suiveur de ligne

    /**
     * Sert juste à faire les tests, sera enlevé à la version finale
     */
    public void test() {
        calibration();
        Button.waitForAnyPress();
        avancer_au_noeud(Orientation.NORD);
        scan();
        Button.waitForAnyPress();
    }

    /**
     * Programme de calibration du robot (calibration du détecteur de couleur pour
     * minimiser les risques d'erreurs d'analyse)
     */
    public void calibration() {
        // Programme d'initialisation, execute différents tests pour calibrer le robot
        // objectif de diminuer les risques d'erreurs

        // Récupère la valeur de la couleur blanche. On récupere la valeur de la couleur
        // rouge car c'est celle utilisée dans la suite du programme
        int color = Color.WHITE;
        int white = capteur_couleur.getColor().getRed();

        System.out.println("Blanc =" + white);

        // avance jusqu'a detecter du noir
        rotation_gauche(30);
        rotation_droite(30);
        color = Color.WHITE;
        while (color != Color.BLACK) {
            color = capteur_couleur.getColor().getColor();
        }

        // s'arrète sur le noir
        moteur_gauche.stop();
        moteur_droite.stop();

        // Récupère la valeur de la couleur noir
        int black = capteur_couleur.getColor().getRed();
        System.out.println("Noir =" + black);

        // Fait la moyenne du blanc et du noir pour definir la consigne du suiveur de
        // ligne
        red_avg = (white + black) / 2;
        System.out.println(red_avg);
        Button.waitForAnyPress();
    }

    /**
     * Permet au robot d'avancer jusqu'au noeud se situant au bout du couloir
     * 
     * @param direction si direction envoyé = direction couloir -> explore jusqu'au
     *                  prochain noeud ; si direction = l'inverse de la direction du
     *                  couloir -> demi tour
     */
    public void avancer_au_noeud(Orientation direction) {
        this.tourner_vers(direction); // S'oriente dans la bonne direction
        this.trouver_ligne(true); // Positionne correctement le robot par rapport a la ligne
        this.avancer(); // lance le suiveur de ligne
    }

    public void tourner_gauche() {
        this.tourner(-90);
        orientation = orientation.gauche();
    }

    public void tourner_droite() {
        this.tourner(90);
        orientation = orientation.droite();
    }

    public void tourner_vers(Orientation direction) {

        // Calcule l'angle entre sa position initiale et la position d'arrivée
        // tourne de l'angle et actualise la direction du robot
        int angle = orientation.difference(direction) * -90;
        tourner(angle);
        orientation = direction;
    }

    public void tourner(int angle) {

        // définit la rotation que chaque moteur doit réaliser ainsi que son écart
        int consigne_gauche = (int) (angle * coefficient_rotation);
        int consigne_droite = -((int) (angle * coefficient_rotation));
        float P = -3;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
        int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

        // définit l'accélération des moteurs
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);

        // tourne jusqu'à avoir fais une rotation du robot de : angle
        while (ecart_gauche != 0 && ecart_droite != 0) {
            rotation_gauche(limite_vitesse(P * ecart_gauche, 720f));
            rotation_droite(limite_vitesse(P * ecart_droite, 720f));
            ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
            ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
        }

        // arrête les moteurs
        moteur_gauche.setSpeed(0);
        moteur_droite.setSpeed(0);
    }

    public boolean trouver_ligne(boolean stop) {

        tourner(-30);

        // définit la rotation que chaque moteur doit réaliser ainsi que son écart
        int consigne_gauche = (int) (60 * coefficient_rotation);
        int consigne_droite = -((int) (60 * coefficient_rotation));
        int P = -1;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
        int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

        // définit l'accélération des moteurs
        moteur_gauche.setAcceleration(200);
        moteur_droite.setAcceleration(200);

        // tourne jusqu'à détecter une ligne noire ou après avoir tourné de 60°
        boolean ligne = false;
        while ((ecart_gauche != 0 && ecart_droite != 0) && (!ligne || !stop)) {

            // change la vitesse de rotation, actualise l'écart et vérifie la couleur
            rotation_gauche(limite_vitesse(P * ecart_gauche, 30));
            rotation_droite(limite_vitesse(P * ecart_droite, 30));
            ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
            ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
            if (!ligne) {
                ligne = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
            }
        }

        // arrête les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();

        // tourne le robot le robot de 30° si aucune ligne noire n'a été trouvée
        // dans l'objectif de le recentrer
        if (!(ligne && stop)) {
            tourner(-30);
        }

        // retourne si le robot est sur la ligne ou non
        return ligne;
    }

    public void afficher(int a) {
        // Affiche la couleur sur l'écran du robot
        switch (a) {
            case Color.BLACK:
                System.out.println("Noir");
                break;

            case Color.WHITE:
                System.out.println("Blanc");
                break;

            case Color.BLUE:
                System.out.println("Bleu");
                break;

            case Color.GREEN:
                System.out.println("Vert");
                break;

            case Color.YELLOW:
                System.out.println("Jaune");
                break;

            case Color.RED:
                System.out.println("Rouge");
                break;
        }
    }

    public void avancer() {

        // initialise les accélérations et fait avancer le robot
        int acceleration = 1000;
        float ecart, speed = 150, P = -0.5f;
        float distance_parcourue = 0;
        moteur_gauche.resetTachoCount();

        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);

        // initialise le capteur de couleur;
        int couleur = TypeNoeud.ligne, couleur1 = TypeNoeud.ligne, couleur2 = TypeNoeud.ligne,
                couleur3 = TypeNoeud.ligne;
        Color mesure = new Color(0, 0, 0, 0, 0);

        // continue d'avancer tant que le robot ne détecte pas de noeud
        while (couleur == TypeNoeud.ligne || couleur == TypeNoeud.sol) {
            // à chaque itération, fait une mesure et compare la moyenne des 3 mesures
            // précédentes pour limiter les risques de fausses mesures
            mesure = capteur_couleur.getColor();
            couleur3 = couleur2;
            couleur2 = couleur1;
            couleur1 = mesure.getColor();
            couleur = Couleur_Moyenne(couleur1, couleur2, couleur3);
            afficher(couleur);
            System.out.println("R=" + mesure.getRed() + " G=" + mesure.getGreen() + " B=" + mesure.getBlue());

            // Permet de réguler légèrement la direction de rotation (pour toujours se
            // situer entre la ligne noir et le sol blanc)
            ecart = P * (mesure.getRed() - red_avg);
            rotation_gauche(speed - ecart);
            rotation_droite(speed + ecart);
        }
        distance_parcourue = moteur_gauche.getTachoCount();

        // avance jusqu'à avoir le centre de rotation du robot sur le noeud
        int consigne = (int) (distance_roue_capteur * (360 / (56.0 * 3.1415))); // 56.0 : diamètre des roues
        ecart = moteur_gauche.getTachoCount() - consigne - distance_parcourue;
        P = -3f;
        while (ecart != 0) {
            ecart = moteur_gauche.getTachoCount() - consigne - distance_parcourue;
            rotation_gauche(limite_vitesse(P * ecart, speed));
            rotation_droite(limite_vitesse(P * ecart, speed));
        }
        moteur_gauche.stop();
        moteur_droite.stop();

        // stocke la couleur du noeud
        couleur_scannee = mesure;
    }

    /**
     * Prend 3 couleurs et ressort la couleur moyenne ; Permet de gommer les fausses
     * lectures du capteur de couleur
     * 
     * @param a Première couleur mesurée
     * @param b Deuxième couleur mesurée
     * @param c Troisième couleur mesurée
     * @return La couleur des trois mesures si elles sont identiques sinon noir
     */
    public int Couleur_Moyenne(int a, int b, int c) {
        if (a == b && b == c) {
            return a;
        }
        return TypeNoeud.ligne;
    }

    public Noeud scan() {

        ArrayList<Couloir> couloirs = new ArrayList<Couloir>();
        if (couleur_scannee.getColor() == TypeNoeud.embranchement) {
            boolean a = false, b = false, c = false;

            // crée la consigne pour faire un tour complet
            int consigne_gauche = (int) (360 * coefficient_rotation);
            int consigne_droite = -((int) (360 * coefficient_rotation));
            int P = -3;
            int vmax = 100;
            moteur_gauche.resetTachoCount();
            moteur_droite.resetTachoCount();
            int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
            int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

            // divise la rotation en 8 sections de détections (sections de 45°)
            int intervalle = consigne_gauche / 8;

            // commence à tourner sur lui-même selon la consigne
            rotation_gauche(limite_vitesse(P * ecart_gauche, vmax));
            rotation_droite(limite_vitesse(P * ecart_droite, vmax));

            // détection du couloirs d'en face (0°-45°)
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!a) {
                    a = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // détection du couloir de gauche (45°-135°)
            intervalle = 3 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!b) {
                    b = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // on saut la détection de la ligne arrière (ligne d'arrivée)
            intervalle = 5 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {

            }

            // Détection de la ligne de droite
            intervalle = 7 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!c) {
                    c = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // Sur la dernière section ralenti jusqu'à la fin de son tour
            while (ecart_gauche != 0 && ecart_droite != 0) {
                rotation_gauche(limite_vitesse(P * ecart_gauche, vmax));
                rotation_droite(limite_vitesse(P * ecart_droite, vmax));
                ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
                ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
                if (!a) {
                    a = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // arrète le moteur
            moteur_gauche.stop();
            moteur_droite.stop();

            // rajoute les couloirs dans le tableau dans un ordre optimisé
            if (a) {
                couloirs.add(new Couloir(orientation));
            }
            if (b) {
                couloirs.add(new Couloir(orientation.droite()));
            }
            if (c) {
                couloirs.add(new Couloir(orientation.gauche()));
            }
        }

        // affiche la couleur du noeud et le nombre de couloirs qui en sortent
        System.out.println("La couleur du noeud est : ");
        afficher(couleur_scannee.getColor());
        System.out.println("Le nombre de chemin est :" + couloirs.size());
        return new Noeud(couleur_scannee, couloirs);
    }

    /**
     * Convertit une vitesse négative en rotation inversée pour la roue gauche (Aide
     * à la programmation)
     * 
     * @param vitesse la vitesse souhaitée (négative si rotation inversée)
     */
    private void rotation_gauche(float vitesse) {
        // controle la vitesse et le sens de rotation de la roue gauche
        // La méthode setSpeed ne permet pas de controller le sens de rotation du moteur
        if (vitesse >= 0) {
            moteur_gauche.forward();
            moteur_gauche.setSpeed(vitesse);
        } else {
            moteur_gauche.backward();
            moteur_gauche.setSpeed(-vitesse);
        }
    }

    // Similaire à rotation_gauche
    private void rotation_droite(float vitesse) {

        if (vitesse >= 0) {
            moteur_droite.forward();
            moteur_droite.setSpeed(vitesse);
        } else {
            moteur_droite.backward();
            moteur_droite.setSpeed(-vitesse);
        }
    }

    /**
     * Permet de limiter la vitesse des roues (pour éviter le glissement)
     * 
     * @param vitesse La vitesse de consigne du robot
     * @param limite  La vitesse maximale autorisée
     * @return La vitesse corrigée
     */
    private float limite_vitesse(float vitesse, float limite) {
        // Permet d'empecher le programme de rentrer des vitesses trop elevées
        if (vitesse < limite && vitesse > -limite) {
            return vitesse;
        }
        if (vitesse > 0) {
            return limite;
        }
        return -limite;
    }

    public Orientation get_orientation() {
        return this.orientation;
    }

    public boolean get_tresor_trouve() {
        return this.tresor;
    }

    public void set_tresor_trouve(boolean tresor_trouve) {
        this.tresor = tresor_trouve;
    }
}
