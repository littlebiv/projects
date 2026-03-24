package checkers;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Swing-based view implementation for the checkers game.
 * Implements GameObserver to receive updates from the model.
 */
public class CheckersView extends JPanel implements IGameView, IGameObserver {
    private static final int TILE_SIZE = 80;
    private static final int PIECE_PADDING = 12;
    
    private static final Color LIGHT_SQUARE = new Color(235, 206, 168);
    private static final Color DARK_SQUARE = new Color(133, 94, 66);
    private static final Color SELECTED_HIGHLIGHT = new Color(100, 255, 100, 100);
    private static final Color VALID_MOVE_COLOR = new Color(0, 255, 0, 150);
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 200);
    private static final Color GAME_OVER_TEXT_COLOR = new Color(255, 215, 0);
    
    private final IBoardModel model;
    private Position selectedSquare;
    private List<Move> validMovesForSelected;
    private String gameOverMessage;
    
    public CheckersView(IBoardModel model) {
        this.model = model;
        this.selectedSquare = null;
        this.validMovesForSelected = new ArrayList<>();
        this.gameOverMessage = null;
        
        int boardSize = model.getBoardSize() * TILE_SIZE;
        setPreferredSize(new Dimension(boardSize, boardSize));
        
        // Register as observer
        model.addObserver(this);
    }
    
    @Override
    public void updateView() {
        repaint();
    }
    
    @Override
    public void highlightMoves(Position position, List<Move> validMoves) {
        this.selectedSquare = position;
        this.validMovesForSelected = new ArrayList<>(validMoves);
        repaint();
    }
    
    @Override
    public void clearHighlights() {
        this.selectedSquare = null;
        this.validMovesForSelected = new ArrayList<>();
        repaint();
    }
    
    @Override
    public void showGameOver(String message) {
        this.gameOverMessage = message;
        repaint();
    }
    
    @Override
    public int getTileSize() {
        return TILE_SIZE;
    }
    
    // IGameObserver implementation
    // These methods will be called by the model when the game state changes
    
    @Override
    public void onBoardChanged() {
        updateView();
    }
    
    @Override
    public void onPlayerChanged(Player newPlayer) {
        // View updates automatically through onBoardChanged
        updateView();
    }
    
    @Override
    public void onGameOver(Player winner, String message) {
        showGameOver(message);
    }
    
    @Override
    public void onPieceSelected(Position position) {
        List<Move> moves = model.getValidMovesForPiece(position);
        highlightMoves(position, moves);
    }
    
    @Override
    public void onSelectionCleared() {
        clearHighlights();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2);
        drawPieces(g2);
        drawHighlights(g2);
        
        if (gameOverMessage != null) {
            drawGameOverOverlay(g2);
        }
    }
    
    private void drawBoard(Graphics2D g2) {
        int boardSize = model.getBoardSize();
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Draw checkerboard pattern
                if ((row + col) % 2 == 0) {
                    g2.setColor(LIGHT_SQUARE);
                } else {
                    g2.setColor(DARK_SQUARE);
                }
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                
                // Highlight selected square
                if (selectedSquare != null && selectedSquare.equals(row, col)) {
                    g2.setColor(SELECTED_HIGHLIGHT);
                    g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }
    
    private void drawPieces(Graphics2D g2) {
        int boardSize = model.getBoardSize();
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                IGamePiece piece = model.getPieceAt(row, col);
                if (piece != null) {
                    drawPiece(g2, piece, row, col);
                }
            }
        }
    }
    
    private void drawPiece(Graphics2D g2, IGamePiece piece, int row, int col) {
        // Draw piece circle
        if (piece.getPlayer() == Player.WHITE) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(Color.BLACK);
        }
        
        int x = col * TILE_SIZE + PIECE_PADDING;
        int y = row * TILE_SIZE + PIECE_PADDING;
        int size = TILE_SIZE - 2 * PIECE_PADDING;
        
        g2.fillOval(x, y, size, size);
        
        // Draw border
        g2.setColor(Color.GRAY);
        g2.drawOval(x, y, size, size);
        
        // Draw king indicator
        if (piece.isKing()) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 30));
            int textX = col * TILE_SIZE + TILE_SIZE / 2 - 10;
            int textY = row * TILE_SIZE + TILE_SIZE / 2 + 10;
            g2.drawString("K", textX, textY);
        }
    }
    
    private void drawHighlights(Graphics2D g2) {
        g2.setColor(VALID_MOVE_COLOR);
        
        for (Move move : validMovesForSelected) {
            Position end = move.getEnd();
            int cx = end.getCol() * TILE_SIZE + TILE_SIZE / 2;
            int cy = end.getRow() * TILE_SIZE + TILE_SIZE / 2;
            g2.fillOval(cx - 10, cy - 10, 20, 20);
        }
    }
    
    private void drawGameOverOverlay(Graphics2D g2) {
        // Draw semi-transparent overlay
        g2.setColor(OVERLAY_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw game over text
        g2.setColor(GAME_OVER_TEXT_COLOR);
        g2.setFont(new Font("Arial", Font.BOLD, 25));
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverMessage);
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + fm.getAscent()) / 2;
        
        g2.drawString(gameOverMessage, x, y);
    }
}
