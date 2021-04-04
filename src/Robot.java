import java.util.ArrayList;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.SensorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Motor;
import lejos.nxt.Button;

// Version Sans Boucle

public class Robot {

    // Donnes necessaire au robot
    private boolean tresor;
    private Orientation orientation = Orientation.NORD;
    private final ColorSensor capteur_couleur = new ColorSensor(SensorPort.S3);
    private NXTRegulatedMotor moteur_gauche = Motor.B;
    private NXTRegulatedMotor moteur_droite = Motor.A;
    private final float diametre_roue = 56f;
    private final float coefficient_rotation = (float) ((2.0 * 54.0) / (diametre_roue)); // 2*excentricite des roues/D
                                                                                         // roue
    private final int distance_roue_capteur = 69; // distance entre le centre de rotation et le capteur
    private Color couleur_scannee;
    private int red_avg = 140; // Valeur de la consigne du suiveur de ligne

    /**
     * Sert juste a faire les tests, sera enleve a la version finale
     */
    public void test() {
        calibration();
        Button.waitForAnyPress();
        avancer_au_noeud(Orientation.NORD);
        scan();
        Button.waitForAnyPress();
    }

    /**
     * Programme de calibration du robot (calibration du detecteur de couleur pour
     * minimiser les risques d'erreurs d'analyse)
     */
    public void calibration() {
        // Programme d'initialisation, execute differents tests pour calibrer le robot
        // objectif de diminuer les risques d'erreurs

        // Recupere la valeur de la couleur blanche. On recupere la valeur de la couleur
        // rouge car c'est celle utilisee dans la suite du programme
        int color = Color.WHITE;
        int white = capteur_couleur.getColor().getRed();

        System.out.println("Blanc =" + white);

        // avance jusqu'a detecter du noir
        rotation_gauche(30);
        rotation_droite(30);
        while (color != Color.BLACK) {
            color = capteur_couleur.getColor().getColor();
        }

        // s'arrete sur le noir
        moteur_gauche.stop();
        moteur_droite.stop();

        // Recupere la valeur de la couleur noir
        int black = capteur_couleur.getColor().getRed();
        System.out.println("Noir =" + black);

        // Fait la moyenne du blanc et du noir pour definir la consigne du suiveur de
        // ligne
        red_avg = ((white + black) / 2) - 17;
        System.out.println(red_avg);
        Button.waitForAnyPress();
    }

    /**
     * Permet au robot d'avancer jusqu'au noeud se situant au bout du couloir
     * 
     * @param direction si direction envoye = direction couloir -> explore jusqu'au
     *                  prochain noeud ; si direction = l'inverse de la direction du
     *                  couloir -> demi tour
     */
    public void avancer_au_noeud(Orientation direction) {
        this.tourner_vers(direction); // S'oriente dans la bonne direction
        this.trouver_ligne(); // Se place sur la ligne
        this.avancer(); // lance le suiveur de ligne
    }

    /**
     * Permet au robot de tourner jusqu'a ce que le capteur se trouve sur la ligne
     * noire
     *
     */
    public void trouver_ligne() {
        int mesure = capteur_couleur.getColor().getRed();

        // Tourner vers la droite
        rotation_gauche(100);
        rotation_droite(-100);

        // Attends que le capteur detecte une couleur sombre
        while (mesure > red_avg) {
            mesure = capteur_couleur.getColor().getRed();
        }
        // Arrete la rotation
        moteur_droite.stop();
        moteur_gauche.stop();
    }

    /**
     * Permet au robot de s'orienter vers une direction
     *
     */
    private void tourner_vers(Orientation direction) {

        // Calcule l'angle entre sa position initiale et la position d'arrivee
        // tourne de l'angle et actualise la direction du robot
        int angle = orientation.difference(direction) * -90;

        // ajoute un decalage afin de se trouver a gauche de la ligne
        angle -= 11;

        tourner(angle);

        // stockage de la nouvelle orientation du robot
        orientation = direction;
    }

    /**
     * Ce programme controle les moteurs afin d'effectuer une rotation sur lui même
     * d'un angle precis
     *
     */
    private void tourner(int angle) {

        // Parametre : Coefficient de proportionalite entre l'ecart et la vitesse des
        // moteurs
        float P = -2f;

        // initialisation
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);

        // definit la rotation que chaque moteur doit realiser ainsi que son ecart
        int consigne_gauche = (int) (angle * coefficient_rotation);
        int consigne_droite = -((int) (angle * coefficient_rotation));
        int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
        int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

        // tourne jusqu'a avoir fais une rotation du robot de : angle
        while (ecart_gauche != 0 && ecart_droite != 0) {
            rotation_gauche(limite_vitesse(P * ecart_gauche, 720f));
            rotation_droite(limite_vitesse(P * ecart_droite, 720f));
            ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
            ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
        }

        // arrete les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();
    }

    public void afficher(int a) {
        // Affiche la couleur sur l'ecran du robot
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

    private void avancer() {

        // parametres de vitesse et d'acceleration
        int acceleration = 1000;
        float speed = 150, P = -0.5f;

        // initialisation
        float ecart, distance_parcourue = 0;
        moteur_gauche.resetTachoCount();
        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);

        // initialise le capteur de couleur;
        int couleur = TypeNoeud.ligne, couleur1 = TypeNoeud.ligne, couleur2 = TypeNoeud.ligne,
                couleur3 = TypeNoeud.ligne;
        Color mesure = new Color(0, 0, 0, 0, 0);

        // continue d'avancer tant que le robot ne detecte pas de noeud
        while (couleur == TypeNoeud.ligne || couleur == TypeNoeud.sol) {

            // a chaque iteration, fait une mesure et compare la moyenne des 3 mesures
            // precedentes pour limiter les risques de fausses mesures
            mesure = capteur_couleur.getColor();
            couleur3 = couleur2;
            couleur2 = couleur1;
            couleur1 = mesure.getColor();
            couleur = Couleur_Moyenne(couleur1, couleur2, couleur3);
            afficher(couleur);

            // Permet de reguler legerement la direction de rotation (pour toujours se
            // situer entre la ligne noir et le sol blanc)
            ecart = P * (mesure.getRed() - red_avg);
            rotation_gauche(speed - ecart);
            rotation_droite(speed + ecart);
        }

        // mesure de la distance parcourue
        distance_parcourue = moteur_gauche.getTachoCount();

        // Parametre : Coefficient de proportionalite entre la distance restante et la
        // vitesse des moteurs
        P = -3f;

        // ralenti jusqu'a avoir le centre de rotation du robot sur le noeud
        int consigne = (int) (distance_roue_capteur * (360 / (diametre_roue * 3.1415))); // 56.0 : diametre des roues
        ecart = moteur_gauche.getTachoCount() - consigne - distance_parcourue;

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
     * @param a Premiere couleur mesuree
     * @param b Deuxieme couleur mesuree
     * @param c Troisieme couleur mesuree
     * @return La couleur des trois mesures si elles sont identiques sinon noir
     */
    private int Couleur_Moyenne(int a, int b, int c) {
        if (a == b && b == c) {
            return a;
        }
        return TypeNoeud.ligne;
    }

    /**
     * Renvoie un noeud avec tout ses couloirs ainsi que sa couleur.
     * 
     */
    public Noeud scan() {

        ArrayList<Couloir> couloirs = new ArrayList<Couloir>();
        if (couleur_scannee.getColor() == TypeNoeud.embranchement) {
            // parametres de vitesse et d'acceleration
            int P = -3;
            int vmax = 100;

            // cree la consigne pour faire un tour complet
            int consigne_gauche = (int) (360 * coefficient_rotation);
            int consigne_droite = -((int) (360 * coefficient_rotation));

            // initialisation
            boolean a = false, b = false, c = false;
            moteur_gauche.resetTachoCount();
            moteur_droite.resetTachoCount();
            int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
            int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

            // divise la rotation en 8 sections de detections (sections de 45°)
            int intervalle = consigne_gauche / 8;

            // commence a tourner sur lui-même selon la consigne
            rotation_gauche(limite_vitesse(P * ecart_gauche, vmax));
            rotation_droite(limite_vitesse(P * ecart_droite, vmax));

            // detection du couloirs d'en face (0°-45°)
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!a) {
                    a = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // detection du couloir de gauche (45°-135°)
            intervalle = 3 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!b) {
                    b = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // on saute la detection de la ligne arriere (ligne d'arrivee)
            intervalle = 5 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {

            }

            // Detection de la ligne de droite
            intervalle = 7 * consigne_gauche / 8;
            while (moteur_gauche.getTachoCount() < intervalle) {
                if (!c) {
                    c = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // Sur la derniere section ralenti jusqu'a la fin de son tour
            while (ecart_gauche != 0 && ecart_droite != 0) {
                rotation_gauche(limite_vitesse(P * ecart_gauche, vmax));
                rotation_droite(limite_vitesse(P * ecart_droite, vmax));
                ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
                ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
                if (!a) {
                    a = capteur_couleur.getColor().getColor() == TypeNoeud.ligne;
                }
            }

            // arrete le moteur
            moteur_gauche.stop();
            moteur_droite.stop();

            // rajoute les couloirs dans le tableau
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
     * Convertit une vitesse negative en rotation inversee pour la roue gauche (Aide
     * a la programmation)
     * 
     * @param vitesse la vitesse souhaitee (negative si rotation inversee)
     */
    private void rotation_gauche(float vitesse) {
        // controle la vitesse et le sens de rotation de la roue gauche
        // La methode setSpeed ne permet pas de controller le sens de rotation du moteur
        if (vitesse >= 0) {
            moteur_gauche.forward();
            moteur_gauche.setSpeed(vitesse);
        } else {
            moteur_gauche.backward();
            moteur_gauche.setSpeed(-vitesse);
        }
    }

    // Similaire a rotation_gauche
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
     * Permet de limiter la vitesse des roues (pour eviter le glissement)
     * 
     * @param vitesse La vitesse de consigne du robot
     * @param limite  La vitesse maximale autorisee
     * @return La vitesse corrigee
     */
    private float limite_vitesse(float vitesse, float limite) {
        // Permet d'empecher le programme de rentrer des vitesses trop elevees
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