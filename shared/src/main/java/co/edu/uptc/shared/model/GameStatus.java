package co.edu.uptc.shared.model;

import java.io.Serializable;

/**
 * Estado del juego distribuido - Transferible vía RMI
 * Sincroniza estado entre servidor y múltiples clientes
 */
public final class GameStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final GamePhase phase;
    private final String currentPlayerName;
    private final boolean isMyTurn;
    private final int playersConnected;
    private final String winner;
    private final String statusMessage;
    
    public GameStatus(GamePhase phase, String currentPlayerName, boolean isMyTurn, 
                     int playersConnected, String winner, String statusMessage) {
        this.phase = phase;
        this.currentPlayerName = currentPlayerName;
        this.isMyTurn = isMyTurn;
        this.playersConnected = playersConnected;
        this.winner = winner;
        this.statusMessage = statusMessage;
    }
    
    // Factory methods para estados comunes del sistema distribuido
    public static GameStatus waiting(int playersConnected) {
        String message = playersConnected == 0 ? "Esperando jugadores..." : 
                        "Esperando segundo jugador... (" + playersConnected + "/2)";
        return new GameStatus(GamePhase.WAITING, null, false, playersConnected, null, message);
    }
    
    public static GameStatus placingShips(int playersConnected) {
        return new GameStatus(GamePhase.PLACING_SHIPS, null, false, playersConnected, null,
                            "Coloca tus barcos en el tablero");
    }
    
    public static GameStatus playing(String currentPlayerName, boolean isMyTurn) {
        String message = isMyTurn ? "¡Tu turno! Ataca el tablero enemigo" : 
                        "Turno de " + currentPlayerName + " - Espera...";
        return new GameStatus(GamePhase.PLAYING, currentPlayerName, isMyTurn, 2, null, message);
    }
    
    public static GameStatus finished(String winner) {
        String message = "¡Juego terminado! Ganador: " + winner;
        return new GameStatus(GamePhase.FINISHED, null, false, 2, winner, message);
    }
    
    // Getters
    public GamePhase getPhase() { return phase; }
    public String getCurrentPlayerName() { return currentPlayerName; }
    public boolean isMyTurn() { return isMyTurn; }
    public int getPlayersConnected() { return playersConnected; }
    public String getWinner() { return winner; }
    public String getStatusMessage() { return statusMessage; }
    
    public boolean isGameReady() {
        return playersConnected == 2;
    }
    
    public boolean isGameFinished() {
        return phase == GamePhase.FINISHED;
    }
    
    public boolean canPlaceShips() {
        return phase == GamePhase.PLACING_SHIPS;
    }
    
    public boolean canAttack() {
        return phase == GamePhase.PLAYING && isMyTurn;
    }
    
    /**
     * Fases del juego distribuido
     */
    public enum GamePhase {
        WAITING("Esperando jugadores"),
        PLACING_SHIPS("Colocando barcos"),
        PLAYING("Jugando"),
        FINISHED("Juego terminado");
        
        private final String description;
        
        GamePhase(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return "GameStatus{" +
                "phase=" + phase +
                ", currentPlayer='" + currentPlayerName + '\'' +
                ", isMyTurn=" + isMyTurn +
                ", playersConnected=" + playersConnected +
                ", winner='" + winner + '\'' +
                ", message='" + statusMessage + '\'' +
                '}';
    }
}
