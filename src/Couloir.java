public class Couloir {

    private Orientation orientation;
    private Noeud noeud;

    public Couloir(Orientation orientation) {
        this.orientation = orientation;
    }

    public boolean verifier_orientation(Robot robot) {
        return this.orientation == robot.get_orientation();
    }

    /**
     * Fonction recursive permettant au robot de visiter le labyrinthe et y sort des
     * que le tresor a ete trouve ou le labyrinthe entierement visite
     * 
     * @param robot Le robot envoye dans le couloir
     */
    public void visite_couloir(Robot robot) {
        // oriente le robot et le fait avancer jusqu'au prochain noeud
        robot.avancer_au_noeud(this.orientation);

        // recupere le noeud avec sa couleur et ses potentiels chemins
        noeud = robot.scan();

        // Si le noeud est un carrefour, analyse le carrefour pour de potentiel chemins
        // a emprunter
        if (noeud.get_couleur() == TypeNoeud.embranchement) {
            // explore tous les embranchements et sous-embranchements du noeud
            for (Couloir couloir : noeud.get_couloirs()) {
                System.out.println("parcours d'un chemin, " + couloir.orientation.toString());
                couloir.visite_couloir(robot);

                // Si le tresor a ete trouve plus loins dans le parcours, retourne sans verifier
                // les autres chemins
                if (robot.get_tresor_trouve()) {
                    robot.avancer_au_noeud(this.orientation.droite().droite());
                    return;
                }
            }
            // Si aucun des chemins ne vont vers le tresor retourne au noeud precedent
            robot.avancer_au_noeud(this.orientation.droite().droite());
        }
        // Si le noeud contient le tresor, indique au robot que le tresor a ete trouve
        // et retourne
        else if (noeud.get_couleur() == TypeNoeud.tresor) {
            System.out.println("tresor recupere, retour au depart");
            robot.set_tresor_trouve(true);
            robot.avancer_au_noeud(this.orientation.droite().droite());
            return;
        }
        // Si le noeud est un cul de sac ou qu'aucune des branches ne mene au tresor,
        // fait demi tour
        else if (noeud.get_couleur() == TypeNoeud.cul_de_sac) {
            System.out.println("cul_de_sac : demi tour");
            robot.avancer_au_noeud(this.orientation.droite().droite());
            return;
        }
    }
}