public class Labyrinthe {
    public static void main(String[] args) {
        // crée un robot, le calibre,
        Robot robot = new Robot();
        robot.calibration();
        Couloir couloir = new Couloir(Orientation.NORD);
        couloir.visite_couloir(robot);
        if (robot.get_tresor_trouve()) {
            robot.tourner(360);
            robot.tourner(-360);
        }
    }
}