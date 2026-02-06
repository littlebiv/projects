package checkers;

/**
 * Static utility class for board validation and common operations.
 */
public final class BoardUtils {
    
    // Private constructor to prevent instantiation
    private BoardUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if the given position is within valid board bounds.
     * @param row the row index
     * @param col the column index
     * @param boardSize the size of the board
     * @return true if position is valid
     */
    public static boolean isValidPosition(int row, int col, int boardSize) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }
    
    /**
     * Checks if the given position is within valid board bounds.
     * @param position the position to check
     * @param boardSize the size of the board
     * @return true if position is valid
     */
    public static boolean isValidPosition(Position position, int boardSize) {
        return isValidPosition(position.getRow(), position.getCol(), boardSize);
    }
    
    /**
     * Checks if a square should be playable (dark square on checkerboard).
     * @param row the row index
     * @param col the column index
     * @return true if the square is playable
     */
    public static boolean isPlayableSquare(int row, int col) {
        return (row + col) % 2 != 0;
    }
    
    /**
     * Gets the forward direction for a player (row delta).
     * @param player the player
     * @return -1 for WHITE (moves up), +1 for BLACK (moves down)
     */
    public static int getForwardDirection(Player player) {
        return player == Player.WHITE ? -1 : 1;
    }
    
    /**
     * Checks if a piece should be promoted based on its position.
     * @param piece the piece to check
     * @param row the current row of the piece
     * @param boardSize the size of the board
     * @return true if the piece should be promoted
     */
    public static boolean shouldPromote(IGamePiece piece, int row, int boardSize) {
        if (piece.isKing()) {
            return false;
        }
        return (piece.getPlayer() == Player.WHITE && row == 0) ||
               (piece.getPlayer() == Player.BLACK && row == boardSize - 1);
    }
}
