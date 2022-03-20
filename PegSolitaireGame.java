public interface PegSolitaireGame
{
    /**
     * This function does not take any parameters, it plays the game by the computer for one move.
     */
    void playAuto();

    /**
     * This function does not take any parameters, it plays the game until it is over.
     * This one calls playAuto for all the moves. it also prints the board between the moves after some pause.
     */
    void playAutoAll();

    /**
     * This function does not take any parameters, it prints the game on the screen.
     */
    String toString();

    
    /**
     * This function does not take any parameters.
     * @return true if the game is ended.
     */
    boolean endGame();

    /**
     * This function does not take any parameters. It returns the number of remaining pegs in the board.
     * @return an int score value for the current board.
     */
    int boardScore();

    /**
     * This function does not take any parameters, it initializes the board.
     */
    void initialize();
}