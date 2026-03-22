# Checkers Game Project

A complete implementation of the Checkers board game using **Observer Pattern** and **MVC Architecture** in Java with Swing GUI.

## 🎮 About

This project demonstrates professional software engineering practices through a fully-featured checkers game. The codebase has been refactored from a single monolithic file into a modular, extensible architecture following SOLID principles.

## ✨ Features

- **Full Checkers Rules**: Regular moves, jumps, multi-jumps, king promotion
- **Automatic Turn Management**: Game enforces valid moves and turn order
- **Visual Feedback**: Selected pieces and valid moves are highlighted
- **Game Over Detection**: Automatic win detection with victory message
- **Clean UI**: Intuitive Swing-based graphical interface

## 🏗️ Architecture

### Design Patterns
- **Observer Pattern**: Model notifies view of state changes automatically
- **MVC Pattern**: Clear separation between Model, View, and Controller
- **Strategy Pattern**: Interface-based design allows easy extension

### Project Structure
```
projects/
├── new/                          # Source code directory
│   ├── Player.java              # Enum for players
│   ├── Position.java            # Board coordinates
│   ├── Piece.java               # Game piece implementation
│   ├── Move.java                # Move representation
│   ├── GameObserver.java        # Observer interface
│   ├── GameSubject.java         # Subject interface
│   ├── BoardModel.java          # Model interface
│   ├── CheckersModel.java       # Game logic implementation
│   ├── GameView.java            # View interface
│   ├── CheckersView.java        # Swing GUI implementation
│   ├── GameController.java      # Controller interface
│   ├── CheckersController.java  # Input handler
│   ├── BoardUtils.java          # Utility functions
│   ├── CheckersGame.java        # Main entry point
│   └── CheckersFinal.java       # Original code (deprecated)
├── ARCHITECTURE.md              # Detailed architecture docs
├── QUICKSTART.md                # Quick start guide
├── REFACTORING_SUMMARY.md       # Refactoring details
├── run_checkers.bat             # Windows run script
├── run_checkers.sh              # Unix/Linux/Mac run script
└── README.md                    # This file
```

## 🚀 Quick Start

### Windows
```powershell
# Double-click run_checkers.bat
# OR run in PowerShell:
.\run_checkers.bat
```

### Linux/Mac
```bash
# Make script executable
chmod +x run_checkers.sh

# Run the script
./run_checkers.sh
```

### Manual Compilation
```bash
cd new
javac *.java
java CheckersGame
```

## 📖 Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Complete architecture documentation with extension examples
- **[QUICKSTART.md](QUICKSTART.md)** - Compilation and gameplay instructions
- **[REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)** - Before/after comparison and improvements

## 🎯 How to Play

1. **White player** starts the game
2. **Click** on a piece to select it (valid moves shown in green)
3. **Click** on a highlighted square to move
4. **Captures** are mandatory when available
5. **Kings** are crowned when reaching the opposite end
6. **Win** by capturing all opponent pieces or blocking all moves

## 🔧 Extending the Game

The modular architecture makes it easy to add new features:

### Add a Move Logger
```java
public class MoveLogger implements GameObserver {
    @Override
    public void onBoardChanged() {
        System.out.println("Move made!");
    }
    // ... other observer methods
}

// Register with model
model.addObserver(new MoveLogger());
```

### Create an AI Player
```java
public class AIController implements GameController {
    @Override
    public void handleSquareClick(int row, int col) {
        // AI logic here
    }
}
```

### Add Network Play
```java
public class NetworkObserver implements GameObserver {
    @Override
    public void onBoardChanged() {
        // Send game state over network
    }
}
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for more examples.

## 🛠️ Technologies

- **Language**: Java 8+
- **GUI Framework**: Swing
- **Design Patterns**: Observer, MVC, Strategy
- **Architecture**: Interface-based, SOLID principles

## 📊 Code Quality

- **Separation of Concerns**: Each class has a single responsibility
- **Loose Coupling**: Components interact through interfaces
- **High Cohesion**: Related functionality grouped together
- **Testability**: Easy to unit test individual components
- **Extensibility**: New features added without modifying existing code

## 🎓 Learning Outcomes

This project demonstrates:
- ✅ Observer Pattern implementation
- ✅ MVC Architecture
- ✅ Interface-based design
- ✅ SOLID principles
- ✅ Clean code practices
- ✅ Modular architecture
- ✅ Separation of concerns

## 📝 License

Educational project - free to use and modify.

## 🤝 Contributing

This is an educational project. Feel free to:
- Fork and modify
- Add new features
- Create different game variants
- Implement AI players
- Add network multiplayer

## 📧 Questions?

- Check the documentation files listed above
- Review the source code comments
- Examine the class diagram in the architecture docs

---

**Enjoy playing Checkers!** 🎲♟️
