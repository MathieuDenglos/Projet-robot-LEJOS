public class Labyrinthe {
    public static void main(String[] args) {
        Orientation test = Orientation.NORD;
        test.droite();
        System.out.println(test.droite().droite());
    }
}