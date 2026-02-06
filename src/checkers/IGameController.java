package checkers;

/**
 * Interface for game controller that handles user input and coordinates
 * between model and view.
 */
public interface IGameController {
    /**
     * Handles a click on a board position.
     * @param row the row that was clicked
     * @param col the column that was clicked
     */
    void handleSquareClick(int row, int col);
    
    /**
     * Resets the game to initial state.
     */
    void resetGame();
    
    /**
     * Updates the game status display (title, score, etc).
     */
    void updateStatus();
}
