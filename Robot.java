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

    public void tourner_gauche(int angle) {
        int consigne = (int) (angle * coefficient_rotation), P = 2; // consigne =D/(180*d)
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart1 = moteur_gauche.getTachoCount() - consigne;
        int ecart2 = moteur_droite.getTachoCount() - consigne;
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);
        moteur_gauche.backward();
        moteur_droite.forward();
        while (ecart1 != 0 && ecart2 != 0) {
            moteur_gauche.setSpeed(P * ecart1);
            moteur_droite.setSpeed(P * ecart2);
            ecart1 = (moteur_gauche.getTachoCount() + consigne);
            ecart2 = (moteur_droite.getTachoCount() - consigne);
            System.out.println(consigne);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
        // delay.msDelay(3000);
        orientation = orientation.gauche();
    }

    public void tourner_droite(int angle) {
        int consigne = (int) (angle * coefficient_rotation), P = 2;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart1 = moteur_gauche.getTachoCount() - consigne;
        int ecart2 = moteur_droite.getTachoCount() - consigne;
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);
        moteur_gauche.forward();
        moteur_droite.backward();
        while (ecart1 != 0 && ecart2 != 0) {
            moteur_gauche.setSpeed(P * ecart1);
            moteur_droite.setSpeed(P * ecart2);
            ecart1 = (moteur_gauche.getTachoCount() - consigne);
            ecart2 = (moteur_droite.getTachoCount() + consigne);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
        orientation = orientation.droite();
    }

    public void trouver_ligne() {
        tourner_droite(30);
        // Tourner vers la gauche jusqua voir la ligne ou a 30 degree gauche si il n y'a
        // pas de ligne, sinon retour angle 0
    }

    public void avancer() {
        // trouver_ligne
        int redAvg = 140, ecart, speed = 180, P = 5, acceleration = 1800;
        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);
        moteur_gauche.forward();
        moteur_droite.forward();
        while (capteur_couleur.getColor().getColor() != Color.BLUE/* ajouter les autres couleurs */) {
            ecart = P * capteur_couleur.getColor().getRed() - redAvg;
            moteur_gauche.setSpeed(speed + ecart);
            moteur_droite.setSpeed(speed - ecart);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
        // regarder la couleur
        // avancer pour se placer pour le scan
        // return Color.BLUE;
    }
    /*
     * Methodes Restantes
     */
}
