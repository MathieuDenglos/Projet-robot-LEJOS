public class Labyrinthe {
    public static void main(String[] args) {
        // cree un robot, le calibre,
        Robot robot = new Robot();
        robot.calibration();
        Couloir couloir = new Couloir(Orientation.NORD);
        couloir.visite_couloir(robot);
        if (robot.get_tresor_trouve()) {
            System.out.println("fini avec le tresor");
        }
    }
}