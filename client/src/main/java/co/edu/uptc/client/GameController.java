package co.edu.uptc.client;

import co.edu.uptc.shared.interfaces.GameService;
import co.edu.uptc.shared.model.*;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Controlador simple que coordina la comunicación RMI y la GUI
 */
public class GameController {
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
    private static final String SERVICE_NAME = "GameService";
    
    private final Registry registry;
    private GameService gameService;
    private GameCallbackImpl callback;
    private GameWindow gameWindow;
    
    private String playerId;
    private String sessionId;
    private boolean isMyTurn = false;
    
    public GameController(Registry registry) {
        this.registry = registry;
    }
    
    /**
     * Inicializa la conexión RMI y callbacks
     */
    public void initialize() throws Exception {
        // Buscar servicio RMI
        gameService = (GameService) registry.lookup(SERVICE_NAME);
        LOGGER.info("Servicio RMI encontrado: " + SERVICE_NAME);
        
        // Crear callback para recibir notificaciones
        callback = new GameCallbackImpl(this);
        LOGGER.info("Callback RMI creado");
    }
    
    /**
     * Conecta un jugador al juego
     */
    public void connectPlayer(String playerName) {
        // Verificar si ya está conectado
        if (playerId != null && sessionId != null) {
            SwingUtilities.invokeLater(() -> 
                gameWindow.showMessage("Ya estás conectado como: " + playerName));
            return;
        }
        
        try {
            String result = gameService.joinGame(playerName, callback);
            
            if (result.startsWith("SUCCESS:")) {
                // Parsear respuesta: "SUCCESS:playerId:sessionId"
                String[] parts = result.split(":");
                playerId = parts[1];
                sessionId = parts[2];
                
                SwingUtilities.invokeLater(() -> {
                    gameWindow.showMessage("Conectado como: " + playerName);
                    gameWindow.updateStatus("Conectado - Esperando oponente...");
                    
                    // El usuario ahora coloca los barcos manualmente
                    gameWindow.showMessage("🚢 Coloca tus barcos haciendo clic en TU TABLERO");
                });
                
                LOGGER.info("Conectado exitosamente - ID: " + playerId + ", Sesión: " + sessionId);
                
            } else {
                SwingUtilities.invokeLater(() -> 
                    gameWindow.showError("Error conectando: " + result));
            }
            
        } catch (RemoteException e) {
            LOGGER.severe("Error en RMI al conectar: " + e.getMessage());
            SwingUtilities.invokeLater(() -> 
                gameWindow.showError("Error de comunicación: " + e.getMessage()));
        }
    }
    
    /**
     * Coloca un barco en el tablero
     */
    public void placeShip(Position start, Position end) {
        if (playerId == null) return;
        
        try {
            boolean success = gameService.placeShip(playerId, start, end);
            
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    gameWindow.showMessage("Barco colocado en " + start + " - " + end);
                } else {
                    gameWindow.showMessage("No se pudo colocar el barco");
                }
            });
            
        } catch (RemoteException e) {
            LOGGER.severe("Error colocando barco: " + e.getMessage());
            SwingUtilities.invokeLater(() -> 
                gameWindow.showError("Error comunicación: " + e.getMessage()));
        }
    }
    
    /**
     * Realiza un ataque
     */
    public void attack(Position target) {
        if (playerId == null || !isMyTurn) {
            SwingUtilities.invokeLater(() -> 
                gameWindow.showMessage("No es tu turno"));
            return;
        }
        
        try {
            String result = gameService.attack(playerId, target);
            
            SwingUtilities.invokeLater(() -> {
                switch (result) {
                    case "HIT":
                        gameWindow.showMessage("¡Impacto en " + target + "!");
                        break;
                    case "MISS":
                        gameWindow.showMessage("Agua en " + target);
                        break;
                    case "SUNK":
                        gameWindow.showMessage("¡Barco hundido en " + target + "!");
                        break;
                    case "VICTORY":
                        gameWindow.showMessage("¡VICTORIA! Has ganado");
                        break;
                    case "NOT_YOUR_TURN":
                        gameWindow.showMessage("No es tu turno");
                        break;
                    default:
                        gameWindow.showMessage("Resultado: " + result);
                        break;
                }
            });
            
        } catch (RemoteException e) {
            LOGGER.severe("Error atacando: " + e.getMessage());
            SwingUtilities.invokeLater(() -> 
                gameWindow.showError("Error comunicación: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene estado actual del juego
     */
    public void refreshGameStatus() {
        if (playerId == null) return;
        
        try {
            GameStatus status = gameService.getGameStatus(playerId);
            handleStatusChange(status);
            
        } catch (RemoteException e) {
            LOGGER.severe("Error obteniendo estado: " + e.getMessage());
        }
    }
    
    // === Métodos para manejar callbacks del servidor ===
    
    public void handleGameEvent(String message) {
        SwingUtilities.invokeLater(() -> gameWindow.showMessage(message));
        
        // Activar modo ataque cuando inicie el juego
        if (message.contains("¡Juego iniciado!")) {
            SwingUtilities.invokeLater(() -> {
                boolean myTurn = message.contains(gameWindow.getPlayerName());
                gameWindow.setTurnIndicator(myTurn);
            });
        }
        
        // Detectar cambios de turno
        if (message.contains("Turno de:")) {
            String playerName = gameWindow.getPlayerName();
            isMyTurn = message.contains(playerName);
            
            SwingUtilities.invokeLater(() -> {
                gameWindow.setTurnIndicator(isMyTurn);
            });
        }
    }
    
    public void handleStatusChange(GameStatus status) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.updateStatus("Estado: " + status.getPhase());
        });
    }
    
    public void handleTurnChange(boolean isMyTurn, String currentPlayerName) {
        SwingUtilities.invokeLater(() -> {
            this.isMyTurn = isMyTurn;
            gameWindow.setTurnIndicator(isMyTurn);
            
            String message = isMyTurn ? "¡Tu turno!" : "Turno de " + currentPlayerName;
            gameWindow.showMessage(message);
        });
    }
    
    public void handleAttackResult(Position target, String result, String message) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.markEnemyAttack(target, result);
            gameWindow.showMessage(message);
        });
    }
    
    // === Getters y Setters ===
    
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return playerId != null;
    }
}
