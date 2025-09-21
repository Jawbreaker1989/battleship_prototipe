package co.edu.uptc.server;

import co.edu.uptc.shared.interfaces.GameCallback;
import co.edu.uptc.shared.model.Board;

/**
 * Jugador simple en el servidor RMI
 * Mantiene información básica para la comunicación distribuida
 */
public class Player {
    private final String id;
    private final String name;
    private final GameCallback callback;
    private final Board board;
    private boolean ready;
    
    public Player(String id, String name, GameCallback callback) {
        this.id = id;
        this.name = name;
        this.callback = callback;
        this.board = new Board();
        this.ready = false;
    }
    
    // Getters simples
    public String getId() { return id; }
    public String getName() { return name; }
    public GameCallback getCallback() { return callback; }
    public Board getBoard() { return board; }
    public boolean isReady() { return ready; }
    
    // Setters simples
    public void setReady(boolean ready) { this.ready = ready; }
    
    @Override
    public String toString() {
        return "Player{" + name + " (" + id + "), ready=" + ready + "}";
    }
}
