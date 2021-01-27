public class Couloir {

    private int distance = 0;
    private Orientation orientation;
    private Noeud noeud;

    public Couloir(Orientation orientation) {
        this.orientation = orientation;
    }

    public boolean verifier_orientation(Robot robot) {
        return this.orientation == robot.get_orientation();
    }

    public void visite_couloir(Robot robot) {
        // oriente le robot et le fait avancer jusqu'au prochain noeud
        robot.avancer_au_noeud(this.orientation);

        // récupère le noeud avec sa couleur et ses potentiels chemins
        noeud = robot.scan();

        // Si le noeud est un carrefour, analyse le carrefour pour de potentiel chemins
        // à emprunter
        if (noeud.get_couleur() == TypeNoeud.embranchement) {
            // explore tous les embranchements et sous-embranchements du noeud
            for (Couloir couloir : noeud.get_couloirs()) {
                couloir.visite_couloir(robot);
                System.out.println("parcours d'un chemin, " + couloir.orientation.toString());
                if (robot.get_tresor_trouve()) {
                    robot.avancer_au_noeud(this.orientation.droite().droite());
                    return;
                }
            }
            robot.avancer_au_noeud(this.orientation.droite().droite());
        }
        // Si le noeud contient le trésor, indique au robot que le trésor a été trouvé
        // et retourne
        else if (noeud.get_couleur() == TypeNoeud.tresor) {
            System.out.println("tresor récupéré, retour au départ");
            robot.set_tresor_trouve(true);
            robot.avancer_au_noeud(this.orientation.droite().droite());
            return;
        }
        // Si le noeud est un cul de sac ou qu'aucune des branches ne mène au trésor,
        // fait demi tour
        else if (noeud.get_couleur() == TypeNoeud.cul_de_sac) {
            System.out.println("cul_de_sac : demi tour");
            robot.avancer_au_noeud(this.orientation.droite().droite());
            return;
        }
    }
}