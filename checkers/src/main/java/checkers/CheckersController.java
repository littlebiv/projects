package checkers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

/**
 * Controller for the checkers game.
 * Handles user input and coordinates between model and view.
 */
public class CheckersController extends MouseAdapter implements IGameController {
    private final IBoardModel model;
    private final IGameView view;
    private final JFrame frame;
    private Position selectedPosition;
    
    public CheckersController(IBoardModel model, IGameView view, JFrame frame) {
        this.model = model;
        this.view = view;
        this.frame = frame;
        this.selectedPosition = null;
        
        // Register mouse listener if view is a component
        if (view instanceof JPanel) {
            ((JPanel) view).addMouseListener(this);
        }
        
        updateStatus();
    }
    
    @Override
    public void handleSquareClick(int row, int col) {
        if (model.isGameOver()) {
            return;
        }
        
        Position clickedPos = new Position(row, col);
        IGamePiece clickedPiece = model.getPieceAt(clickedPos);
        Player currentPlayer = model.getCurrentPlayer();
        
        // If clicking on own piece, select it
        if (clickedPiece != null && clickedPiece.getPlayer() == currentPlayer) {
            selectPiece(clickedPos);
        }
        // If a piece is already selected, try to move to clicked position
        else if (selectedPosition != null) {
            attemptMove(clickedPos);
        }
    }
    
    private void selectPiece(Position position) {
        selectedPosition = position;
        List<Move> validMoves = model.getValidMovesForPiece(position);
        
        if (validMoves.isEmpty()) {
            // No valid moves for this piece
            selectedPosition = null;
            view.clearHighlights();
        } else {
            view.highlightMoves(position, validMoves);
        }
    }
    
    private void attemptMove(Position destination) {
        List<Move> validMoves = model.getValidMovesForPiece(selectedPosition);
        Move selectedMove = null;
        
        // Find the move that matches the destination
        for (Move move : validMoves) {
            if (move.getEnd().equals(destination)) {
                selectedMove = move;
                break;
            }
        }
        
        if (selectedMove != null) {
            // Valid move found, execute it
            model.makeMove(selectedMove);
            clearSelection();
            updateStatus();
        } else {
            // Invalid destination, clear selection
            clearSelection();
        }
    }
    
    private void clearSelection() {
        selectedPosition = null;
        view.clearHighlights();
    }
    
    @Override
    public void resetGame() {
        model.resetGame();
        clearSelection();
        updateStatus();
    }
    
    @Override
    public void updateStatus() {
        int whiteCount = model.getPieceCount(Player.WHITE);
        int blackCount = model.getPieceCount(Player.BLACK);
        Player currentPlayer = model.getCurrentPlayer();
        
        String title;
        if (model.isGameOver()) {
            Player winner = model.getWinner();
            String winnerName = winner == Player.WHITE ? "White" : "Black";
            title = "GAME OVER - " + winnerName + " Wins!";
        } else {
            String currentPlayerName = currentPlayer == Player.WHITE ? "White" : "Black";
            title = String.format("Checkers | White: %d | Black: %d | Turn: %s",
                                whiteCount, blackCount, currentPlayerName);
        }
        
        frame.setTitle(title);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / view.getTileSize();
        int row = e.getY() / view.getTileSize();
        
        // Validate bounds
        if (BoardUtils.isValidPosition(row, col, model.getBoardSize())) {
            handleSquareClick(row, col);
        }
    }
}
