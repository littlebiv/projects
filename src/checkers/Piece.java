package checkers;

/**
 * Represents a checkers piece.
 */
public class Piece implements IGamePiece {
    private final Player player;
    private boolean isKing;
    
    public Piece(Player player) {
        this.player = player;
        this.isKing = false;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public boolean isKing() {
        return isKing;
    }
    
    @Override
    public void promote() {
        this.isKing = true;
    }
}
