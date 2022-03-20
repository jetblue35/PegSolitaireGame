public class GUIDriver
{
    public static void main(String[] args) {
        PegSolitaire game = new PegSolitaire();
        PegSolitaire cloneGame = (PegSolitaire) game.clone();
    }
}