package co.edu.uptc.server;

import co.edu.uptc.shared.interfaces.GameService;
import co.edu.uptc.shared.interfaces.GameCallback;
import co.edu.uptc.shared.model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Implementación del servicio RMI de Batalla Naval
 * Demuestra servidor distribuido que coordina múltiples clientes
 */
public class GameServiceImpl extends UnicastRemoteObject implements GameService {
    private static final Logger LOGGER = Logger.getLogger(GameServiceImpl.class.getName());
    
    // Estructuras thread-safe para sistema distribuido
    private final Map<String, Player> players;
    private final Map<String, GameSession> playerToSession;
    private GameSession currentSession;
    private final AtomicInteger playerCounter;
    
    public GameServiceImpl() throws RemoteException {
        super();
        this.players = new ConcurrentHashMap<>();
        this.playerToSession = new ConcurrentHashMap<>();
        this.playerCounter = new AtomicInteger(1);
        LOGGER.info("Servicio RMI de Batalla Naval inicializado");
    }
    
    @Override
    public synchronized String joinGame(String playerName, GameCallback callback) throws RemoteException {
        LOGGER.info("Solicitud de conexión de jugador: " + playerName);
        
        String playerId = "player_" + playerCounter.getAndIncrement();
        Player player = new Player(playerId, playerName, callback);
        
        players.put(playerId, player);
        
        // Buscar o crear sesión de juego distribuida
        GameSession session = findOrCreateSession();
        boolean added = session.addPlayer(player);
        
        if (added) {
            playerToSession.put(playerId, session);
            LOGGER.info("Jugador " + playerName + " (" + playerId + ") conectado al sistema distribuido");
            
            // Notificar al jugador sobre el estado actual
            try {
                callback.onGameEvent("Conectado al servidor. Esperando oponente...");
            } catch (RemoteException e) {
                LOGGER.warning("Error notificando conexión a " + playerId + ": " + e.getMessage());
            }
            
            // Retornar en formato esperado por el cliente
            return "SUCCESS:" + playerId + ":" + session.getSessionId();
        } else {
            // No se pudo añadir
            players.remove(playerId);
            throw new RemoteException("No se pudo unir al juego - Servidor lleno");
        }
    }
    
    @Override
    public boolean placeShip(String playerId, Position start, Position end) throws RemoteException {
        LOGGER.info("Solicitud colocar barco de " + playerId + ": " + start + " a " + end);
        
        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            LOGGER.warning("Sesión no encontrada para jugador: " + playerId);
            return false;
        }
        
        try {
            return session.placeShip(playerId, start, end);
        } catch (Exception e) {
            LOGGER.warning("Error colocando barco para " + playerId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String attack(String playerId, Position target) throws RemoteException {
        LOGGER.info("Ataque de " + playerId + " a posición " + target);
        
        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return "ERROR_SESSION";
        }
        
        try {
            Board.AttackResult result = session.attack(playerId, target);
            if (result != null) {
                return result.name();
            }
            return "NOT_YOUR_TURN";
        } catch (Exception e) {
            LOGGER.warning("Error en ataque de " + playerId + ": " + e.getMessage());
            return "ERROR";
        }
    }
    
    @Override
    public GameStatus getGameStatus(String playerId) throws RemoteException {
        GameSession session = playerToSession.get(playerId);
        if (session == null) {
            return GameStatus.waiting(0);
        }
        
        return session.getGameStatus(playerId);
    }
    
    @Override
    public boolean setPlayerReady(String playerId) throws RemoteException {
        LOGGER.info("Jugador " + playerId + " marcado como listo");
        // Simplificado: siempre retorna true
        return true;
    }
    
    @Override
    public void disconnectPlayer(String playerId) throws RemoteException {
        LOGGER.info("Desconectando jugador: " + playerId);
        
        Player player = players.get(playerId);
        if (player != null) {
            players.remove(playerId);
            playerToSession.remove(playerId);
            
            LOGGER.info("Jugador " + player.getName() + " (" + playerId + ") desconectado del sistema distribuido");
        }
    }
    
    /**
     * Busca una sesión disponible o crea una nueva
     * Maneja la coordinación de sesiones en el sistema distribuido
     */
    private GameSession findOrCreateSession() {
        if (currentSession == null || currentSession.isFull()) {
            String sessionId = "session_" + System.currentTimeMillis();
            currentSession = new GameSession(sessionId);
            LOGGER.info("Nueva sesión de juego distribuida creada: " + sessionId);
        }
        return currentSession;
    }
    
    /**
     * Obtiene estadísticas del servidor distribuido
     */
    public String getServerStats() {
        return String.format("Jugadores conectados: %d, Sesiones activas: %d", 
                           players.size(), 
                           currentSession != null ? 1 : 0);
    }
}
