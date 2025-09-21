package co.edu.uptc.server;

import co.edu.uptc.shared.model.*;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * Sesión simple de juego entre 2 jugadores
 * Coordina la partida distribuida con lógica mínima
 */
public class GameSession {
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());
    
    private final String sessionId;
    private Player player1;
    private Player player2;
    private String currentTurn; // ID del jugador actual
    private GameStatus.GamePhase phase;
    
    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.phase = GameStatus.GamePhase.WAITING;
    }
    
    /**
     * Añade un jugador a la sesión
     */
    public synchronized boolean addPlayer(Player player) {
        if (player1 == null) {
            player1 = player;
            notifyPlayer(player, "Esperando segundo jugador...");
            return true;
        } else if (player2 == null) {
            player2 = player;
            phase = GameStatus.GamePhase.PLACING_SHIPS;
            
            // Notificar a ambos jugadores
            notifyPlayer(player1, "Jugador 2 conectado: " + player2.getName());
            notifyPlayer(player2, "Conectado contra: " + player1.getName());
            notifyBothPlayers("¡Coloquen sus barcos!");
            
            return true;
        }
        return false; // Sesión llena
    }
    
    /**
     * Coloca un barco para un jugador
     */
    public synchronized boolean placeShip(String playerId, Position start, Position end) {
        Player player = getPlayer(playerId);
        if (player == null || phase != GameStatus.GamePhase.PLACING_SHIPS) {
            return false;
        }
        
        try {
            boolean placed = player.getBoard().placeShip(start, end);
            
            if (placed) {
                notifyPlayer(player, "Barco colocado");
                
                // Verificar si ambos están listos para jugar
                if (bothPlayersReady()) {
                    startGame();
                }
            }
            
            return placed;
            
        } catch (Exception e) {
            LOGGER.warning("Error colocando barco: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Realiza un ataque
     */
    public synchronized Board.AttackResult attack(String playerId, Position target) {
        if (phase != GameStatus.GamePhase.PLAYING || !playerId.equals(currentTurn)) {
            return null; // No es tu turno
        }
        
        Player attacker = getPlayer(playerId);
        Player defender = getOpponent(playerId);
        
        if (attacker == null || defender == null) {
            return null;
        }
        
        Board.AttackResult result = defender.getBoard().receiveAttack(target);
        
        // Notificar resultado
        notifyPlayer(attacker, "Atacaste " + target + ": " + result.getDescription());
        notifyPlayer(defender, attacker.getName() + " atacó " + target + ": " + result.getDescription());
        
        // Verificar victoria
        if (result == Board.AttackResult.SUNK_AND_GAME_OVER) {
            phase = GameStatus.GamePhase.FINISHED;
            notifyBothPlayers("¡" + attacker.getName() + " GANA!");
            return result;
        }
        
        // Cambiar turno solo si falló
        if (result == Board.AttackResult.MISS) {
            switchTurn();
        }
        
        return result;
    }
    
    /**
     * Obtiene el estado del juego para un jugador
     */
    public GameStatus getGameStatus(String playerId) {
        String player1Name = player1 != null ? player1.getName() : null;
        String player2Name = player2 != null ? player2.getName() : null;
        Player requestingPlayer = getPlayer(playerId);
        
        int playersConnected = (player1 != null ? 1 : 0) + (player2 != null ? 1 : 0);
        
        switch (phase) {
            case WAITING:
                return GameStatus.waiting(playersConnected);
            case PLACING_SHIPS:
                return GameStatus.placingShips(playersConnected);
            case PLAYING:
                Player current = getPlayer(currentTurn);
                String currentName = current != null ? current.getName() : "";
                boolean isMyTurn = requestingPlayer != null && currentTurn.equals(requestingPlayer.getId());
                return GameStatus.playing(currentName, isMyTurn);
            case FINISHED:
                // Determinar ganador
                String winner = "Juego terminado";
                return GameStatus.finished(winner);
            default:
                return GameStatus.waiting(playersConnected);
        }
    }
    
    // Métodos auxiliares simples
    private Player getPlayer(String playerId) {
        if (player1 != null && player1.getId().equals(playerId)) return player1;
        if (player2 != null && player2.getId().equals(playerId)) return player2;
        return null;
    }
    
    private Player getOpponent(String playerId) {
        if (player1 != null && player1.getId().equals(playerId)) return player2;
        if (player2 != null && player2.getId().equals(playerId)) return player1;
        return null;
    }
    
    private void startGame() {
        phase = GameStatus.GamePhase.PLAYING;
        currentTurn = player1.getId(); // Player1 siempre empieza
        notifyBothPlayers("¡Juego iniciado! " + player1.getName() + " ataca primero.");
    }
    
    private void switchTurn() {
        currentTurn = currentTurn.equals(player1.getId()) ? player2.getId() : player1.getId();
        Player current = getPlayer(currentTurn);
        notifyBothPlayers("Turno de: " + current.getName());
    }
    
    private boolean bothPlayersReady() {
        // Simplificado: asumimos listos si ambos tienen al menos 1 barco
        return player1 != null && player1.getBoard().getShipCount() > 0 &&
               player2 != null && player2.getBoard().getShipCount() > 0;
    }
    
    private void notifyPlayer(Player player, String message) {
        try {
            player.getCallback().onGameEvent(message);
        } catch (RemoteException e) {
            LOGGER.warning("Error notificando a " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private void notifyBothPlayers(String message) {
        if (player1 != null) notifyPlayer(player1, message);
        if (player2 != null) notifyPlayer(player2, message);
    }
    
    // Getters simples
    public String getSessionId() { return sessionId; }
    public boolean isFull() { return player1 != null && player2 != null; }
    public boolean isEmpty() { return player1 == null && player2 == null; }
}
