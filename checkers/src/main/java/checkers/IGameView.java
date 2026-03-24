package checkers;

import java.util.List;

/**
 * Interface for the game view component.
 */
public interface IGameView {
    /**
     * Updates the view to reflect current game state.
     */
    void updateView();
    
    /**
     * Highlights a selected piece and its valid moves.
     * @param position the position of the selected piece
     * @param validMoves the list of valid moves for that piece
     */
    void highlightMoves(Position position, List<Move> validMoves);
    
    /**
     * Clears all highlights.
     */
    void clearHighlights();
    
    /**
     * Displays a game over message.
     * @param message the message to display
     */
    void showGameOver(String message);
    
    /**
     * Gets the tile size for coordinate calculations.
     * @return the size of each tile in pixels
     */
    int getTileSize();
}
