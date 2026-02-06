package checkers;

import javax.swing.*;

/**
 * Main application class for launching the Checkers game.
 * Uses MVC architecture with Observer pattern.
 */
public class CheckersGame {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CheckersGame::createAndShowGUI);
    }
    
    private static void createAndShowGUI() {
        // Create main frame
        JFrame frame = new JFrame("Checkers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create MVC components
        IBoardModel model = new CheckersModel();
        IGameView view = new CheckersView(model);
        IGameController controller = new CheckersController(model, view, frame);
        
        // Add view to frame
        if (view instanceof JPanel) {
            frame.add((JPanel) view);
        }
        
        // Configure and show frame
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
