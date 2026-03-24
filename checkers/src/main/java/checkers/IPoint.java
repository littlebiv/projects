package checkers;

/**
 * Interface representing a point on the board.
 */
public interface IPoint {
    int getRow();
    int getCol();
    boolean equals(int r, int c);
    @Override
    boolean equals(Object obj);
    @Override
    int hashCode();
    @Override
    String toString();
}
