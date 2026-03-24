package checkers;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the checkers game model with Observer pattern support.
 * This class maintains the game state and notifies observers of changes.
 */
public class CheckersModel implements IBoardModel {
    private static final int BOARD_SIZE = 8;
    private static CheckersModel instance;
    
    private IGamePiece[][] board;
    private Player currentPlayer;
    private final List<IGameObserver> observers;
    private boolean gameOver;
    private Player winner;
    
    private CheckersModel() {
        this.observers = new ArrayList<>();
        resetGame();
    }

    public static CheckersModel getInstance() {
        if (instance == null) {
            instance = new CheckersModel();
        }
        return instance;
    }
    
    @Override
    public void addObserver(IGameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(IGameObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (IGameObserver observer : observers) {
            observer.onBoardChanged();
        }
    }
    
    private void notifyPlayerChanged() {
        for (IGameObserver observer : observers) {
            observer.onPlayerChanged(currentPlayer);
        }
    }
    
    private void notifyGameOver(Player winner, String message) {
        for (IGameObserver observer : observers) {
            observer.onGameOver(winner, message);
        }
    }
    
    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }
    
    @Override
    public void resetGame() {
        board = new IGamePiece[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = Player.WHITE;
        gameOver = false;
        winner = null;
        
        // Initialize pieces on playable squares
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (BoardUtils.isPlayableSquare(row, col)) {
                    if (row < 3) {
                        board[row][col] = new Piece(Player.BLACK);
                    } else if (row > 4) {
                        board[row][col] = new Piece(Player.WHITE);
                    }
                }
            }
        }
        
        notifyObservers();
    }
    
    @Override
    public IGamePiece getPieceAt(int row, int col) {
        if (!BoardUtils.isValidPosition(row, col, BOARD_SIZE)) {
            return null;
        }
        return board[row][col];
    }
    
    @Override
    public IGamePiece getPieceAt(Position position) {
        return getPieceAt(position.getRow(), position.getCol());
    }
    
    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    @Override
    public List<Move> getValidMoves(Player player) {
        List<Move> regularMoves = new ArrayList<>();
        List<Move> captureMoves = new ArrayList<>();
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                IGamePiece piece = board[row][col];
                if (piece != null && piece.getPlayer() == player) {
                    Position pos = new Position(row, col);
                    findMovesForPiece(pos, piece, regularMoves, captureMoves);
                }
            }
        }
        
        // Captures are mandatory in checkers
        return captureMoves.isEmpty() ? regularMoves : captureMoves;
    }
    
    @Override
    public List<Move> getValidMovesForPiece(Position position) {
        IGamePiece piece = getPieceAt(position);
        if (piece == null) {
            return new ArrayList<>();
        }
        
        List<Move> allPlayerMoves = getValidMoves(piece.getPlayer());
        List<Move> pieceMoves = new ArrayList<>();
        
        for (Move move : allPlayerMoves) {
            if (move.getStart().equals(position)) {
                pieceMoves.add(move);
            }
        }
        
        return pieceMoves;
    }
    
    private void findMovesForPiece(Position pos, IGamePiece piece, List<Move> regularMoves, List<Move> captureMoves) {
        // All four diagonal directions
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            if (piece.isKing()) {
                findKingMoves(pos, piece, dir, regularMoves, captureMoves);
            } else {
                findRegularPieceMoves(pos, piece, dir, regularMoves, captureMoves);
            }
        }
    }
    
    private void findKingMoves(Position pos, IGamePiece piece, int[] dir, List<Move> regularMoves, List<Move> captureMoves) {
        int row = pos.getRow();
        int col = pos.getCol();
        int dRow = dir[0];
        int dCol = dir[1];
        
        boolean foundEnemy = false;
        Position enemyPos = null;
        
        // Kings can move multiple squares
        for (int dist = 1; dist < BOARD_SIZE; dist++) {
            int targetRow = row + (dRow * dist);
            int targetCol = col + (dCol * dist);
            
            if (!BoardUtils.isValidPosition(targetRow, targetCol, BOARD_SIZE)) {
                break;
            }
            
            IGamePiece targetPiece = board[targetRow][targetCol];
            Position targetPos = new Position(targetRow, targetCol);
            
            if (targetPiece == null) {
                // Empty square
                if (!foundEnemy) {
                    regularMoves.add(new Move(pos, targetPos, false, null));
                } else {
                    captureMoves.add(new Move(pos, targetPos, true, enemyPos));
                }
            } else {
                // Occupied square
                if (targetPiece.getPlayer() == piece.getPlayer()) {
                    // Own piece blocks further movement
                    break;
                } else {
                    // Enemy piece
                    if (foundEnemy) {
                        // Can't capture two pieces in one direction
                        break;
                    }
                    foundEnemy = true;
                    enemyPos = targetPos;
                }
            }
        }
    }
    
    private void findRegularPieceMoves(Position pos, IGamePiece piece, int[] dir, List<Move> regularMoves, List<Move> captureMoves) {
        int row = pos.getRow();
        int col = pos.getCol();
        int dRow = dir[0];
        int dCol = dir[1];
        
        int forwardDir = BoardUtils.getForwardDirection(piece.getPlayer());
        
        // Regular move (one square forward diagonally)
        if (dRow == forwardDir) {
            int targetRow = row + dRow;
            int targetCol = col + dCol;
            
            if (BoardUtils.isValidPosition(targetRow, targetCol, BOARD_SIZE) && 
                board[targetRow][targetCol] == null) {
                regularMoves.add(new Move(pos, new Position(targetRow, targetCol), false, null));
            }
        }
        
        // Capture move (jump over enemy piece)
        int jumpRow = row + (dRow * 2);
        int jumpCol = col + (dCol * 2);
        int midRow = row + dRow;
        int midCol = col + dCol;
        
        if (BoardUtils.isValidPosition(jumpRow, jumpCol, BOARD_SIZE) && 
            board[jumpRow][jumpCol] == null) {
            
            IGamePiece midPiece = board[midRow][midCol];
            if (midPiece != null && midPiece.getPlayer() != piece.getPlayer()) {
                captureMoves.add(new Move(pos, new Position(jumpRow, jumpCol), true, new Position(midRow, midCol)));
            }
        }
    }
    
    @Override
    public boolean makeMove(Move move) {
        Position start = move.getStart();
        Position end = move.getEnd();
        
        IGamePiece piece = getPieceAt(start);
        if (piece == null || piece.getPlayer() != currentPlayer) {
            return false;
        }
        
        // Validate move is in valid moves list
        List<Move> validMoves = getValidMoves(currentPlayer);
        if (!validMoves.contains(move) && !isMoveInList(move, validMoves)) {
            return false;
        }
        
        // Execute move
        board[end.getRow()][end.getCol()] = piece;
        board[start.getRow()][start.getCol()] = null;
        
        // Handle capture
        if (move.isCapture()) {
            Position captured = move.getCapturedPiece();
            board[captured.getRow()][captured.getCol()] = null;
        }
        
        // Check for promotion
        if (BoardUtils.shouldPromote(piece, end.getRow(), BOARD_SIZE)) {
            piece.promote();
        }
        
        // Switch player
        currentPlayer = currentPlayer.opponent();
        
        // Notify observers
        notifyObservers();
        notifyPlayerChanged();
        
        // Check for game over
        checkGameOver();
        
        return true;
    }
    
    private boolean isMoveInList(Move move, List<Move> moves) {
        for (Move m : moves) {
            if (m.getStart().equals(move.getStart()) && 
                m.getEnd().equals(move.getEnd())) {
                return true;
            }
        }
        return false;
    }
    
    private void checkGameOver() {
        int whiteCount = getPieceCount(Player.WHITE);
        int blackCount = getPieceCount(Player.BLACK);
        
        // Check if one player has no pieces left
        if (whiteCount == 0) {
            endGame(Player.BLACK, "Black wins! White has no pieces left.");
            return;
        }
        if (blackCount == 0) {
            endGame(Player.WHITE, "White wins! Black has no pieces left.");
            return;
        }
        
        // Check if current player has no valid moves
        if (getValidMoves(currentPlayer).isEmpty()) {
            Player gameWinner = currentPlayer.opponent();
            String colorName = gameWinner == Player.WHITE ? "White" : "Black";
            endGame(gameWinner, colorName + " wins! Opponent has no valid moves.");
        }
    }
    
    private void endGame(Player winner, String message) {
        this.gameOver = true;
        this.winner = winner;
        notifyGameOver(winner, message);
    }
    
    @Override
    public int getPieceCount(Player player) {
        int count = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                IGamePiece piece = board[row][col];
                if (piece != null && piece.getPlayer() == player) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Override
    public boolean isGameOver() {
        return gameOver;
    }
    
    @Override
    public Player getWinner() {
        return winner;
    }
}
