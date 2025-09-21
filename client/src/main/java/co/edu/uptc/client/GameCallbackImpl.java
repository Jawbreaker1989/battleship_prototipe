package co.edu.uptc.client;

import co.edu.uptc.shared.interfaces.GameCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * Implementación simple de callbacks RMI para recibir notificaciones del servidor
 */
public class GameCallbackImpl extends UnicastRemoteObject implements GameCallback {
    private static final Logger LOGGER = Logger.getLogger(GameCallbackImpl.class.getName());
    
    private final GameController controller;
    
    public GameCallbackImpl(GameController controller) throws RemoteException {
        super();
        this.controller = controller;
    }
    
    @Override
    public void onGameEvent(String message) throws RemoteException {
        LOGGER.info("Evento del juego: " + message);
        
        // Delegar al controlador para actualizar la GUI
        controller.handleGameEvent(message);
    }
    
    @Override
    public void onPlayerJoined(String playerName) throws RemoteException {
        LOGGER.info("Jugador conectado: " + playerName);
        
        controller.handleGameEvent("Jugador conectado: " + playerName);
    }
    
    @Override
    public void onTurnChanged(boolean isMyTurn, String currentPlayerName) throws RemoteException {
        LOGGER.info("Cambio de turno - Es mi turno: " + isMyTurn + ", Jugador actual: " + currentPlayerName);
        
        controller.handleTurnChange(isMyTurn, currentPlayerName);
    }
    
    @Override
    public void onGameEnded(String winner) throws RemoteException {
        LOGGER.info("Juego terminado - Ganador: " + winner);
        
        controller.handleGameEvent("¡Juego terminado! Ganador: " + winner);
    }
    
    @Override
    public void onOpponentDisconnected() throws RemoteException {
        LOGGER.info("Oponente desconectado");
        
        controller.handleGameEvent("Oponente desconectado");
    }
}
