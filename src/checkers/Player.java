package checkers;

/**
 * Represents the two players in a checkers game.
 */
public enum Player {
    WHITE, BLACK;
    
    /**
     * Returns the opponent of this player.
     * @return the opposing player
     */
    public Player opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
