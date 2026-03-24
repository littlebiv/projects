package checkers;

import java.util.List;

/**
 * Interface representing a board game model with core operations.
 */
public interface IBoardModel extends IGameSubject {
    /**
     * Gets the board size (number of rows/columns).
     * @return the board size
     */
    int getBoardSize();
    
    /**
     * Gets the piece at the specified position.
     * @param row the row index
     * @param col the column index
     * @return the piece at that position, or null if empty
     */
    IGamePiece getPieceAt(int row, int col);
    
    /**
     * Gets the piece at the specified position.
     * @param position the position
     * @return the piece at that position, or null if empty
     */
    IGamePiece getPieceAt(Position position);
    
    /**
     * Gets the current player whose turn it is.
     * @return the current player
     */
    Player getCurrentPlayer();
    
    /**
     * Gets all valid moves for the specified player.
     * @param player the player
     * @return list of valid moves
     */
    List<Move> getValidMoves(Player player);
    
    /**
     * Gets all valid moves for a specific piece at a position.
     * @param position the position of the piece
     * @return list of valid moves for that piece
     */
    List<Move> getValidMovesForPiece(Position position);
    
    /**
     * Attempts to make a move.
     * @param move the move to make
     * @return true if the move was successful
     */
    boolean makeMove(Move move);
    
    /**
     * Gets the count of remaining pieces for a player.
     * @param player the player
     * @return the number of pieces
     */
    int getPieceCount(Player player);
    
    /**
     * Checks if the game has ended.
     * @return true if the game is over
     */
    boolean isGameOver();
    
    /**
     * Gets the winner if the game is over.
     * @return the winning player, or null if game is not over
     */
    Player getWinner();
    
    /**
     * Resets the game to initial state.
     */
    void resetGame();
}
