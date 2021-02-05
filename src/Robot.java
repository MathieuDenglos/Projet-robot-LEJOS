import java.util.ArrayList;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.SensorPort;
import lejos.util.Delay;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Motor;
import lejos.nxt.Button;

public class Robot {

    // Donnés nécessaire au robot
    private boolean tresor;
    private Orientation orientation = Orientation.NORD;
    private final ColorSensor capteur_couleur = new ColorSensor(SensorPort.S1);
    private NXTRegulatedMotor moteur_gauche = Motor.A;
    private NXTRegulatedMotor moteur_droite = Motor.B;
    private final float coefficient_rotation = (float) ((2.0 * 48.0) / (56.0)); // D/2d
    private final int distance_roue_capteur = 64;
    private Color couleur_scannee;
    private int red_avg = 140;

    public void test() {
        calibration();
        avancer();
        Button.waitForAnyPress();
    }

    public void calibration() {
        // Programme d'initialisation, execute différents tests pour calibrer le robot
        // objectif de diminuer les risques d'erreurs

        int color = Color.WHITE;
        /*
         * capteur_couleur.calibrateHigh(); rotation_gauche(30); rotation_droite(30);
         * while (color!=Color.BLACK) { color=capteur_couleur.getColor().getColor(); }
         * moteur_gauche.stop(); moteur_droite.stop(); capteur_couleur.calibrateLow();
         * System.out.println("Placer le robot sur du blanc"); Button.waitForAnyPress();
         */
        int white = capteur_couleur.getColor().getRed();

        System.out.println(white);
        System.out.println(capteur_couleur.getColor().getRed() + " " + capteur_couleur.getColor().getGreen() + " "
                + capteur_couleur.getColor().getBlue());
        rotation_gauche(30);
        rotation_droite(30);
        color = Color.WHITE;
        while (color != Color.BLACK) {
            color = capteur_couleur.getColor().getColor();
        }
        moteur_gauche.stop();
        moteur_droite.stop();

        int black = capteur_couleur.getColor().getRed();
        System.out.println(black);
        System.out.println(capteur_couleur.getColor().getRed() + " " + capteur_couleur.getColor().getGreen() + " "
                + capteur_couleur.getColor().getBlue());

        red_avg = (white + black) / 2;
        System.out.println(red_avg);
        Button.waitForAnyPress();
    }

    public void avancer_au_noeud(Orientation direction) {
        // si direction == couloir.orientation -> explore jusqu'au prochain noeud
        // si direction == couloir.orientation.droite().droite() -> demi tour
        this.tourner_vers(direction);
        this.trouver_ligne(true);
        this.avancer();
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
        int angle = orientation.difference(direction) * -90;
        tourner(angle);
        orientation = direction;
    }

    private void tourner(int angle) {

        // définit la rotation que chaque moteur doit réaliser ainsi que son écart
        int consigne_gauche = (int) (angle * coefficient_rotation);
        int consigne_droite = -((int) (angle * coefficient_rotation));
        int P = -3;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
        int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

        // définit l'accélération des moteurs
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);

        // tourne jusqu'à avoir fais une rotation du robot de : angle
        while (ecart_gauche != 0 && ecart_droite != 0) {
            rotation_gauche(P * ecart_gauche);
            rotation_droite(P * ecart_droite);
            ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
            ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
        }

        // arrête les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();
    }

    public boolean trouver_ligne(boolean stop) {
        tourner(30);

        // définit la rotation que chaque moteur doit réaliser ainsi que son écart
        int consigne_gauche = (int) (-60 * coefficient_rotation);
        int consigne_droite = -((int) (-60 * coefficient_rotation));
        int P = -1;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart_gauche = moteur_gauche.getTachoCount() - consigne_gauche;
        int ecart_droite = moteur_droite.getTachoCount() - consigne_droite;

        // définit l'accélération des moteurs
        moteur_gauche.setAcceleration(200);
        moteur_droite.setAcceleration(200);

        // tourne jusqu'à détecter uneligne noire ou après avoir tourné de 60°
        boolean ligne = false;
        while ((ecart_gauche != 0 && ecart_droite != 0) && (!ligne || !stop)) {

            // change la vitesse de rotation, actualise l'écart et vérifie la couleur
            rotation_gauche(limite_vitesse(P * ecart_gauche, 30));
            rotation_droite(limite_vitesse(P * ecart_droite, 30));
            ecart_gauche = (moteur_gauche.getTachoCount() - consigne_gauche);
            ecart_droite = (moteur_droite.getTachoCount() - consigne_droite);
            ligne = capteur_couleur.getColor().getColor() == Color.BLACK;
            System.out.println(ecart_gauche + " " + ecart_droite);
        }

        // arrête les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();

        // tourne le robot le robot de 30° si aucune ligne noire n'a été trouvée
        // dans l'objectif de le recentrer
        if (!(ligne && stop)) {
            tourner(30);
        }

        // retourne si le robot est sur la ligne ou non
        return ligne;
    }

    public void afficher(int a) {
        // TODO
    }

    public void avancer() {

        // initialise les accélérations et fait avancer le robot
        int acceleration = 1000, acceleration2 = 600;
        float ecart, speed = 0, P = -0.5f;
        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);

        // initialise le capteur de couleur;
        int couleur = Color.BLACK, couleur1 = Color.BLACK, couleur2 = Color.BLACK, couleur3 = Color.BLACK;
        Color mesure = new Color(0, 0, 0, 0, 0);

        // continue d'avancer tant que le robot ne détecte pas de noeud

        while (couleur != TypeNoeud.cul_de_sac && couleur != TypeNoeud.debut && couleur != TypeNoeud.embranchement
                && couleur != TypeNoeud.tresor) {
            mesure = capteur_couleur.getColor();
            couleur3 = couleur2;
            couleur2 = couleur1;
            couleur1 = mesure.getColor();
            couleur = Couleur_Moyenne(couleur1, couleur2, couleur3);
            afficher(couleur);
            System.out.println("R" + mesure.getRed() + "G" + mesure.getGreen() + "B" + mesure.getBlue());
            ecart = P * (mesure.getRed() - red_avg);
            rotation_gauche(speed - ecart);
            rotation_droite(speed + ecart);
        }

        // arrête les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();

        // stocke la couleur actuellement visible (celle du noeud)
        couleur_scannee = mesure;

        // avance de quelques centimètres pour avoir le centre de rotation du robot sur
        // le noeud

        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();

        moteur_gauche.setAcceleration(acceleration2);
        moteur_droite.setAcceleration(acceleration2);

        int consigne = (int) (distance_roue_capteur * (360 / (56.0 * 3.1415))); // 56.0 : diamètre des roues
        ecart = moteur_gauche.getTachoCount() - consigne;
        P = -3f;
        while (ecart != 0) {
            ecart = moteur_gauche.getTachoCount() - consigne;
            rotation_gauche(P * ecart);
            rotation_droite(P * ecart);
        }
    }

    public int Couleur_Moyenne(int a, int b, int c) {
        if (a == b && b == c) {
            return a;
        }
        return Color.BLACK;
    }

    public Noeud scan() {// renvoi un node
        boolean a = false, b = false, c = false;

        // vérifie s'il y a un chemin en face de lui
        if (capteur_couleur.getColor().getColor() == TypeNoeud.ligne) {
            a = true;
        } else {
            a = trouver_ligne(false);
        }

        // vérifie s'il y a un chemin à gauche
        tourner_gauche();
        if (capteur_couleur.getColor().getColor() == TypeNoeud.ligne) {
            b = true;
        } else {
            b = trouver_ligne(false);
        }

        // vérifie s'il y a un chemin à droite
        tourner_vers(this.orientation.droite().droite());
        if (capteur_couleur.getColor().getColor() == TypeNoeud.ligne) {
            c = true;
        } else {
            c = trouver_ligne(false);
        }

        // ajoute les chemins s'ils existent dans l'ordre : droite, gauche, en face
        // cet ordre pour optimisation et limiter les rotations
        ArrayList<Couloir> couloirs = new ArrayList<Couloir>();
        if (c) {
            couloirs.add(new Couloir(orientation));
        }
        if (b) {
            couloirs.add(new Couloir(orientation.gauche().gauche()));
        }
        if (a) {
            couloirs.add(new Couloir(orientation.gauche()));
        }
        return new Noeud(couleur_scannee, couloirs);
    }

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

    private void rotation_droite(float vitesse) {

        if (vitesse >= 0) {
            moteur_droite.forward();
            moteur_droite.setSpeed(vitesse);
        } else {
            moteur_droite.backward();
            moteur_droite.setSpeed(-vitesse);
        }
    }

    public void avancer_gauche(float vitesse) {
        // Fonction qui sera utilisé dans avancer
        if (vitesse >= 0) {
            moteur_gauche.forward();
            moteur_gauche.setSpeed(vitesse);
        } else {
            moteur_droite.setSpeed(0);
        }
    }

    public void avancer_droite(float vitesse) {
        // Fonction qui sera utilisé dans avancer
        if (vitesse >= 0) {
            moteur_droite.forward();
            moteur_droite.setSpeed(vitesse);
        } else {
            moteur_droite.setSpeed(0);
        }
    }

    private int limite_vitesse(int vitesse, int limite) {
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
