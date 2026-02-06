package checkers;

/**
 * Observer interface for the Observer pattern.
 * Classes that want to be notified of game state changes should implement this.
 */
public interface IGameObserver {
    /**
     * Called when the game board state has changed (piece moved, captured, etc).
     */
    void onBoardChanged();
    
    /**
     * Called when the current player has changed.
     * @param newPlayer the player whose turn it now is
     */
    void onPlayerChanged(Player newPlayer);
    
    /**
     * Called when the game has ended.
     * @param winner the winning player
     * @param message the game over message
     */
    void onGameOver(Player winner, String message);
    
    /**
     * Called when a piece has been selected and valid moves are available.
     * @param position the position of the selected piece
     */
    void onPieceSelected(Position position);
    
    /**
     * Called when the selection should be cleared.
     */
    void onSelectionCleared();
}
