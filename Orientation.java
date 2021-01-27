public enum Orientation {
    NORD, EST, SUD, OUEST;

    private static final Orientation[] valeurs = values();

    public final Orientation droite() {
        return valeurs[(this.ordinal() + 1) % valeurs.length];
    }

    public final Orientation gauche() {
        return valeurs[(this.ordinal() - 1 + valeurs.length) % valeurs.length];
    }

    public final int difference(Orientation comparaison) {
        int temp = this.ordinal() - comparaison.ordinal();
        if (temp == -3)
            return 1;
        if (temp == 3)
            return -1;
        else
            return temp;
    }
}
