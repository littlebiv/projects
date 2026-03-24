package checkers;

/**
 * Interface representing a game piece with common operations.
 */
public interface IGamePiece {
    /**
     * Gets the player who owns this piece.
     * @return the player
     */
    Player getPlayer();
    
    /**
     * Checks if this piece is a king.
     * @return true if the piece is a king
     */
    boolean isKing();
    
    /**
     * Promotes this piece to a king.
     */
    void promote();
}
