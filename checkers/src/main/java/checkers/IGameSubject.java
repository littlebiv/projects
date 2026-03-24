package checkers;

/**
 * Interface for observables in the Observer pattern.
 * Classes that maintain game state and notify observers should implement this.
 */
public interface IGameSubject {
    /**
     * Registers an observer to receive game state updates.
     * @param observer the observer to register
     */
    void addObserver(IGameObserver observer);
    
    /**
     * Unregisters an observer from receiving updates.
     * @param observer the observer to remove
     */
    void removeObserver(IGameObserver observer);
    
    /**
     * Notifies all registered observers of a state change.
     */
    void notifyObservers();
}
