package checkers;

/**
 * Represents a move in the checkers game.
 */
public class Move {
    private final Position start;
    private final Position end;
    private final boolean isCapture;
    private final Position capturedPiece;
    
    public Move(Position start, Position end, boolean isCapture, Position capturedPiece) {
        this.start = start;
        this.end = end;
        this.isCapture = isCapture;
        this.capturedPiece = capturedPiece;
    }
    
    public Position getStart() {
        return start;
    }
    
    public Position getEnd() {
        return end;
    }
    
    public boolean isCapture() {
        return isCapture;
    }
    
    public Position getCapturedPiece() {
        return capturedPiece;
    }
    
    @Override
    public String toString() {
        return "Move{" + start + " -> " + end + (isCapture ? " (capture at " + capturedPiece + ")" : "") + "}";
    }
}
