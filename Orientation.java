public enum Orientation {

    NORD(0), OUEST(90), SUD(180), EST(270);

    private int deg;

    Orientation(int deg) {
        this.deg = deg;
    }

    // Pour les deux fonctions qui suivent s'intéresser à la méthode ordinal
    public Orientation droite() {
        int degD = (deg + 270) % 360;
        switch (degD) {
            case 90:
                return OUEST;
            case 180:
                return SUD;
            case 270:
                return EST;
            default: // for the case 0
                return NORD;
        }
    }

    public Orientation gauche() {
        int degD = (deg + 90) % 360;
        switch (degD) {
            case 90:
                return OUEST;
            case 180:
                return SUD;
            case 270:
                return EST;
            default: // for the case 0
                return NORD;
        }
    }

    int difference(Orientation c) {
        // Inutile dans l"état, ne retourne pas le chemin le plus optimisé
        return Math.abs(this.deg - c.deg) % 160;
    }

}