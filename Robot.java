package files;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.SensorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Motor;

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

    public void calibration() {
        // Programme d'initialisation, execute différents tests pour calibrer le robot
        // objectif de diminuer les risques d'erreurs
        red_avg = capteur_couleur.getColor().getRed();
    }

    public void tourner_gauche() {
        this.tourner(-90);
        orientation = orientation.gauche();
    }

    public void tourner_droite() {
        this.tourner(90);
        orientation = orientation.droite();
    }

    private void tourner(int angle) {

        // définit la rotation que chaque moteur doit réaliser ainsi que son écart
        int consigne_gauche = (int) (angle * coefficient_rotation);
        int consigne_droite = -((int) (angle * coefficient_rotation));
        int P = -2;
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
        while ((ecart_gauche != 0 && ecart_droite != 0) && (ligne || stop)) {

            // change la vitesse de rotation, actualise l'écart et vérifie la couleur
            rotation_gauche(P * ecart_gauche);
            rotation_droite(P * ecart_droite);
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
        if (!(ligne && stop))
            tourner(30);

        // retourne si le robot est sur la ligne ou non
        return ligne;
    }

    public void avancer() {

        // initialise les accélérations et fait avancer le robot
        int redAvg = 140, ecart, speed = 180, P = 5, acceleration = 1800;
        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);
        moteur_gauche.forward();
        moteur_droite.forward();

        // continue d'avancer tant que le robot ne détecte pas de noeud
        int couleur = capteur_couleur.getColor().getColor();
        while (couleur != TypeNoeud.cul_de_sac && couleur != TypeNoeud.debut && couleur != TypeNoeud.embranchement
                && couleur != TypeNoeud.tresor) {
            ecart = P * capteur_couleur.getColor().getRed() - redAvg;
            moteur_gauche.setSpeed(speed + ecart);
            moteur_droite.setSpeed(speed - ecart);
        }
        // arrête les moteurs
        moteur_gauche.stop();
        moteur_droite.stop();

        // stocke la couleur actuellement visible (celle du noeud)
        couleur_scannee = capteur_couleur.getColor();
    }

    public Noeud scan() {// renvoi un node

    }

    private void rotation_gauche(int vitesse) {
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

    private void rotation_droite(int vitesse) {

        if (vitesse >= 0) {
            moteur_droite.forward();
            moteur_droite.setSpeed(vitesse);
        } else {
            moteur_droite.backward();
            moteur_droite.setSpeed(-vitesse);
        }
    }

    public Orientation get_orientation() {
        return this.orientation;
    }
}
