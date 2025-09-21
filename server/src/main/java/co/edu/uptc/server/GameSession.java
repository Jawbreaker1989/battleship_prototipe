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
            notifyPlayer(player, "🔗 Conectado al servidor. Esperando segundo jugador... (1/2)");
            LOGGER.info("Primer jugador conectado: " + player.getName());
            return true;
        } else if (player2 == null) {
            player2 = player;
            phase = GameStatus.GamePhase.PLACING_SHIPS;
            
            // Notificar a ambos jugadores con mejor mensaje
            notifyPlayer(player1, "🎮 ¡Segundo jugador conectado! Oponente: " + player2.getName());
            notifyPlayer(player2, "🎮 ¡Conectado exitosamente! Oponente: " + player1.getName());
            notifyBothPlayers("🚢 ¡Ambos jugadores conectados! Ahora coloquen sus 5 barcos en el tablero.");
            
            LOGGER.info("Segundo jugador conectado: " + player.getName() + ". Sesión completa.");
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
                notifyPlayer(player, "✅ Barco colocado correctamente (" + player.getBoard().getShipCount() + "/5)");
                
                // Verificar si ambos están listos para jugar
                if (bothPlayersReady()) {
                    notifyBothPlayers("🎉 ¡Ambos jugadores han colocado sus barcos!");
                    startGame();
                } else {
                    // Informar progreso al jugador
                    int shipsCount = player.getBoard().getShipCount();
                    if (shipsCount < 5) {
                        notifyPlayer(player, "📊 Progreso: " + shipsCount + "/5 barcos colocados");
                    } else {
                        notifyPlayer(player, "⏳ Todos tus barcos colocados. Esperando al oponente...");
                    }
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
        
        // Notificaciones más claras para el inicio del juego
        notifyBothPlayers("🎯 ¡Juego iniciado! Todos los barcos colocados.");
        notifyPlayer(player1, "🔥 ¡Es tu turno! Haz clic en el tablero enemigo para atacar.");
        notifyPlayer(player2, "⏳ Turno de " + player1.getName() + ". Espera tu turno...");
        
        LOGGER.info("Juego iniciado - Turno inicial: " + player1.getName());
    }
    
    private void switchTurn() {
        currentTurn = currentTurn.equals(player1.getId()) ? player2.getId() : player1.getId();
        Player current = getPlayer(currentTurn);
        Player waiting = getOpponent(currentTurn);
        
        // Notificaciones más específicas por jugador
        notifyPlayer(current, "🔥 ¡Es tu turno! Haz clic en el tablero enemigo para atacar.");
        notifyPlayer(waiting, "⏳ Turno de " + current.getName() + ". Espera tu turno...");
        
        LOGGER.info("Cambio de turno - Ahora ataca: " + current.getName());
    }
    
    private boolean bothPlayersReady() {
        // Verificar que ambos tengan exactamente 5 barcos para más realismo
        return player1 != null && player1.getBoard().getShipCount() >= 5 &&
               player2 != null && player2.getBoard().getShipCount() >= 5;
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
