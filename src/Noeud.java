import java.util.ArrayList;

import lejos.nxt.ColorSensor.Color;

public class Noeud {
    private ArrayList<Couloir> couloirs = new ArrayList<>();
    private Color couleur;

    public Noeud(Color couleur, ArrayList<Couloir> couloirs) {
        // Vérifie que la couleur envoyée correspond bien à une couleur de noeud
        if (couleur.getColor() != TypeNoeud.tresor && couleur.getColor() != TypeNoeud.embranchement
                && couleur.getColor() != TypeNoeud.cul_de_sac) {
            System.out.println("ERREUR RENTREE DANS LE CONSTRUCTEUR DE NOEUD");
        } else {
            this.couleur = couleur;
        }
        this.couloirs = couloirs;
    }

    public int get_couleur() {
        return couleur.getColor();
    }

    public ArrayList<Couloir> get_couloirs() {
        return couloirs;
    }

    public void afficherNoeud() {
        // afficher la couleur du noeud et le nombre de couloirs qui en sortent
        System.out.println("La couleur du noeud est : " + couleur);
        if (couleur.getColor() == TypeNoeud.embranchement)
            System.out.println("Le nombre de chemin est :" + couloirs.size());
    }
}
