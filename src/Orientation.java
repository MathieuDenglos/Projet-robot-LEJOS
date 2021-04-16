public enum Orientation {

    // Les differentes valeurs
    NORD, EST, SUD, OUEST;

    // tableau qui contient les valeurs de l'enum {NORD, EST, SUD, OUEST}
    private static final Orientation[] valeurs = values();

    public final Orientation droite() {
        return valeurs[(this.ordinal() + 1) % valeurs.length];
    }

    public final Orientation gauche() {
        return valeurs[(this.ordinal() - 1 + valeurs.length) % valeurs.length];
    }

    /**
     * compare deux directions
     * 
     * @param comparaison L'orientation visee
     * @return Le nombre de quarts de tours le plus faible vers la direction
     */
    public final int difference(Orientation comparaison) {
        int temp = this.ordinal() - comparaison.ordinal();
        if (temp == -3)
            return 1;
        if (temp == 3)
            return -1;
        else
            return temp;
    }
};