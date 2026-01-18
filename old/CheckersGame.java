import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// --- ENUMS & DATA STRUCTURES ---

enum Player {
    WHITE, BLACK;
    public Player opponent() { return this == WHITE ? BLACK : WHITE; }
}

class Piece {
    private final Player player;
    private boolean isKing;

    public Piece(Player player) {
        this.player = player;
        this.isKing = false;
    }

    public Player getPlayer() { return player; }
    public boolean isKing() { return isKing; }
    public void makeKing() { this.isKing = true; }
}

class Point { // Custom Point class to avoid java.awt.Point confusion in logic
    int row, col;
    public Point(int row, int col) { this.row = row; this.col = col; }
    public boolean equals(int r, int c) { return this.row == r && this.col == c; }
}

class Move {
    Point start, end;
    boolean isCapture;
    Point capturedPiece; // Location of the piece being eaten

    public Move(Point start, Point end, boolean isCapture, Point capturedPiece) {
        this.start = start;
        this.end = end;
        this.isCapture = isCapture;
        this.capturedPiece = capturedPiece;
    }
}

// --- MODEL (The Logic) ---

class CheckersModel {
    public static final int SIZE = 8;
    private Piece[][] board;
    private Player currentPlayer;
    
    public CheckersModel() {
        resetGame();
    }

    public void resetGame() {
        board = new Piece[SIZE][SIZE];
        currentPlayer = Player.WHITE;
        
        // Setup pieces
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 != 0) {
                    if (row < 3) board[row][col] = new Piece(Player.BLACK);
                    else if (row > 4) board[row][col] = new Piece(Player.WHITE);
                }
            }
        }
    }

    public Piece getPieceAt(int row, int col) {
        if (!isValidBounds(row, col)) return null;
        return board[row][col];
    }

    public Player getCurrentPlayer() { return currentPlayer; }

    public boolean makeMove(Move move) {
        Piece piece = board[move.start.row][move.start.col];
        if (piece == null) return false;

        // Execute move
        board[move.end.row][move.end.col] = piece;
        board[move.start.row][move.start.col] = null;

        // Handle capture
        if (move.isCapture) {
            board[move.capturedPiece.row][move.capturedPiece.col] = null;
        }

        // King Promotion
        if (!piece.isKing() && ((piece.getPlayer() == Player.WHITE && move.end.row == 0) ||
            (piece.getPlayer() == Player.BLACK && move.end.row == SIZE - 1))) {
            piece.makeKing();
        }

        currentPlayer = currentPlayer.opponent();
        return true;
    }

    public List<Move> getValidMoves(Player player) {
        List<Move> moves = new ArrayList<>();
        List<Move> captures = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = board[r][c];
                if (p != null && p.getPlayer() == player) {
                    checkMovesForPiece(r, c, p, moves, captures);
                }
            }
        }

        if (!captures.isEmpty()) return captures;
        return moves;
    }

    // --- לוגיקה חדשה: תמיכה במלכה מעופפת ---
    private void checkMovesForPiece(int r, int c, Piece p, List<Move> moves, List<Move> captures) {
        // All 4 diagonals: Up-Left, Up-Right, Down-Left, Down-Right
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];

            if (p.isKing()) {
                // --- KING LOGIC (Flying) ---
                boolean foundEnemy = false;
                Point enemyPos = null;

                // Scan the whole diagonal
                for (int dist = 1; dist < SIZE; dist++) {
                    int targetR = r + (dRow * dist);
                    int targetC = c + (dCol * dist);

                    if (!isValidBounds(targetR, targetC)) break; // End of board

                    Piece targetP = board[targetR][targetC];

                    if (targetP == null) {
                        // Empty square
                        if (!foundEnemy) {
                            // Simple move (no capture yet)
                            moves.add(new Move(new Point(r, c), new Point(targetR, targetC), false, null));
                        } else {
                            // Landing spot AFTER capture
                            captures.add(new Move(new Point(r, c), new Point(targetR, targetC), true, enemyPos));
                        }
                    } else {
                        // Found a piece
                        if (targetP.getPlayer() == p.getPlayer()) {
                            break; // Blocked by own piece
                        } else {
                            // Found enemy
                            if (foundEnemy) break; // Cannot capture two enemies in a row without landing
                            foundEnemy = true;
                            enemyPos = new Point(targetR, targetC);
                        }
                    }
                }
            } else {
                // --- REGULAR MAN LOGIC ---
                // 1. Simple Move (1 step) - Only forward
                int forwardDir = (p.getPlayer() == Player.WHITE) ? -1 : 1;
                if (dRow == forwardDir) {
                    int targetR = r + dRow;
                    int targetC = c + dCol;
                    if (isValidBounds(targetR, targetC) && board[targetR][targetC] == null) {
                        moves.add(new Move(new Point(r, c), new Point(targetR, targetC), false, null));
                    }
                }

                // 2. Capture (2 steps) - Can capture backwards too
                int jumpR = r + (dRow * 2);
                int jumpC = c + (dCol * 2);
                int midR = r + dRow;
                int midC = c + dCol;

                if (isValidBounds(jumpR, jumpC) && board[jumpR][jumpC] == null) {
                    Piece midP = board[midR][midC];
                    if (midP != null && midP.getPlayer() != p.getPlayer()) {
                        captures.add(new Move(new Point(r, c), new Point(jumpR, jumpC), true, new Point(midR, midC)));
                    }
                }
            }
        }
    }

    private boolean isValidBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }
}

// --- VIEW (The GUI) ---

class CheckersView extends JPanel {
    private CheckersModel model;
    private final int TILE_SIZE = 80;
    private Point selectedSquare = null;
    private List<Move> validMovesForSelected = new ArrayList<>();

    public CheckersView(CheckersModel model) {
        this.model = model;
        setPreferredSize(new Dimension(CheckersModel.SIZE * TILE_SIZE, CheckersModel.SIZE * TILE_SIZE));
    }

    public void setSelectedSquare(Point p, List<Move> moves) {
        this.selectedSquare = p;
        this.validMovesForSelected = moves;
        repaint();
    }

    public int getTileSize() { return TILE_SIZE; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int row = 0; row < CheckersModel.SIZE; row++) {
            for (int col = 0; col < CheckersModel.SIZE; col++) {
                // Draw Board
                if ((row + col) % 2 == 0) g2.setColor(new Color(235, 206, 168)); // Light Wood
                else g2.setColor(new Color(133, 94, 66)); // Dark Wood
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Highlight Selection
                if (selectedSquare != null && selectedSquare.equals(row, col)) {
                    g2.setColor(new Color(100, 255, 100, 100)); // Green highlight
                    g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Draw Pieces
                Piece piece = model.getPieceAt(row, col);
                if (piece != null) {
                    int padding = 12;
                    if (piece.getPlayer() == Player.WHITE) g2.setColor(Color.WHITE);
                    else g2.setColor(Color.BLACK);
                    
                    g2.fillOval(col * TILE_SIZE + padding, row * TILE_SIZE + padding, 
                                TILE_SIZE - 2*padding, TILE_SIZE - 2*padding);
                    
                    // Add border to piece
                    g2.setColor(Color.GRAY);
                    g2.drawOval(col * TILE_SIZE + padding, row * TILE_SIZE + padding, 
                                TILE_SIZE - 2*padding, TILE_SIZE - 2*padding);

                    // Draw King Marker
                    if (piece.isKing()) {
                        g2.setColor(Color.RED);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
                        g2.drawString("K", col * TILE_SIZE + TILE_SIZE/2 - 10, row * TILE_SIZE + TILE_SIZE/2 + 10);
                    }
                }
            }
        }

        // Highlight Valid Moves
        g2.setColor(new Color(0, 255, 0, 150));
        for (Move m : validMovesForSelected) {
            int cx = m.end.col * TILE_SIZE + TILE_SIZE / 2;
            int cy = m.end.row * TILE_SIZE + TILE_SIZE / 2;
            g2.fillOval(cx - 10, cy - 10, 20, 20);
        }
    }
}

// --- CONTROLLER (The Interaction) ---

class CheckersController extends MouseAdapter {
    private CheckersModel model;
    private CheckersView view;
    private Point selectedSource;

    public CheckersController(CheckersModel model, CheckersView view) {
        this.model = model;
        this.view = view;
        view.addMouseListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / view.getTileSize();
        int row = e.getY() / view.getTileSize();

        // 1. If clicking on own piece -> Select it
        Piece clickedPiece = model.getPieceAt(row, col);
        Player current = model.getCurrentPlayer();

        if (clickedPiece != null && clickedPiece.getPlayer() == current) {
            selectedSource = new Point(row, col);
            List<Move> allMoves = model.getValidMoves(current);
            List<Move> relevantMoves = new ArrayList<>();
            
            // Filter only moves starting from this piece
            for(Move m : allMoves) {
                if(m.start.equals(row, col)) relevantMoves.add(m);
            }
            view.setSelectedSquare(selectedSource, relevantMoves);
        } 
        // 2. If clicking on empty square -> Try to move
        else if (selectedSource != null) {
            List<Move> allMoves = model.getValidMoves(current);
            Move moveBuild = null;

            // Find if this click corresponds to a valid move from the selected source
            for (Move m : allMoves) {
                if (m.start.equals(selectedSource.row, selectedSource.col) && 
                    m.end.equals(row, col)) {
                    moveBuild = m;
                    break;
                }
            }

            if (moveBuild != null) {
                model.makeMove(moveBuild);
                selectedSource = null;
                view.setSelectedSquare(null, new ArrayList<>());
            }
        }
        
        view.repaint(); // Always refresh
    }
}

// --- MAIN ---

public class CheckersGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Checkers - Israeli Rules");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            CheckersModel model = new CheckersModel();
            CheckersView view = new CheckersView(model);
            new CheckersController(model, view); // Wire them up

            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }
    
}