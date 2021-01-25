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

    private void rotation_gauche(int vitesse) {
        // La méthode setSpeed ne permet pas de controller le sens de rotation du moteur
        // via des valeurs négatives, cette méthode permet de contourner cette
        // contrainte
        if (vitesse >= 0) {
            moteur_gauche.forward();
            moteur_gauche.setSpeed(vitesse);
        } else {
            moteur_gauche.backward();
            moteur_gauche.setSpeed(-vitesse);
        }
    }

    private void rotation_droite(int vitesse) {
        // La méthode setSpeed ne permet pas de controller le sens de rotation du moteur
        // via des valeurs négatives, cette méthode permet de contourner cette
        // contrainte
        if (vitesse >= 0) {
            moteur_droite.forward();
            moteur_droite.setSpeed(vitesse);
        } else {
            moteur_droite.backward();
            moteur_droite.setSpeed(-vitesse);
        }
    }

    private void tourner(int angle) {
        int consigneGauche = (int) (angle * coefficient_rotation);
        int consigneDroite = -((int) (angle * coefficient_rotation));
        int P = -2;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();

        int ecart1 = moteur_gauche.getTachoCount() - consigneGauche;
        int ecart2 = moteur_droite.getTachoCount() - consigneDroite;
        moteur_gauche.setAcceleration(600);
        moteur_droite.setAcceleration(600);

        while (ecart1 != 0 && ecart2 != 0) {
            rotation_gauche(P * ecart1);
            rotation_droite(P * ecart2);
            ecart1 = (moteur_gauche.getTachoCount() - consigneGauche);
            ecart2 = (moteur_droite.getTachoCount() - consigneDroite);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
    }

    public boolean trouver_ligne(boolean stop) {
        tourner(30);
        int consigneGauche = (int) (-60 * coefficient_rotation);
        int consigneDroite = -((int) (-60 * coefficient_rotation));
        int P = -1;
        moteur_gauche.resetTachoCount();
        moteur_droite.resetTachoCount();
        int ecart1 = moteur_gauche.getTachoCount() - consigneGauche;
        int ecart2 = moteur_droite.getTachoCount() - consigneDroite;
        moteur_gauche.setAcceleration(200);
        moteur_droite.setAcceleration(200);
        boolean ligne = false;
        while ((ecart1 != 0 && ecart2 != 0) && (ligne == false || stop == false)) {
            rotation_gauche(P * ecart1);
            rotation_droite(P * ecart2);
            ecart1 = (moteur_gauche.getTachoCount() - consigneGauche);
            ecart2 = (moteur_droite.getTachoCount() - consigneDroite);
            if (capteur_couleur.getColor().getColor() == Color.BLACK) {
                ligne = true;
            }
            System.out.println(ecart1 + " " + ecart2);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
        if (ligne == true) {
            if (stop == false) {
                tourner(30);
            }
            return true;
        } else {
            tourner(30);
            return false;
        }
    }

    public void avancer() {
        // trouver_ligne
        int redAvg = 140, ecart, speed = 180, P = 5, acceleration = 1800;
        moteur_gauche.setAcceleration(acceleration);
        moteur_droite.setAcceleration(acceleration);
        moteur_gauche.forward();
        moteur_droite.forward();
        while (capteur_couleur.getColor().getColor() != Color.BLACK/* ajouter les autres couleurs */) {
            ecart = P * capteur_couleur.getColor().getRed() - redAvg;
            moteur_gauche.setSpeed(speed + ecart);
            moteur_droite.setSpeed(speed - ecart);
        }
        moteur_gauche.stop();
        moteur_droite.stop();
        // regarder la couleur
        // avancer pour se placer pour le scan
        couleur_scannee = capteur_couleur.getColor();
    }

    public Noeud scan() {// renvoi un node

    }
}
