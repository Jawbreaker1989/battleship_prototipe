package co.edu.uptc.shared.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI para callbacks bidireccionales servidor → cliente
 * Demuestra notificaciones distribuidas en tiempo real
 */
public interface GameCallback extends Remote {
    
    /**
     * Notifica eventos del juego distribuido al cliente
     * @param message Mensaje del evento
     * @throws RemoteException Error en comunicación RMI
     */
    void onGameEvent(String message) throws RemoteException;
    
    /**
     * Notifica cuando otro jugador se conecta al sistema distribuido
     * @param playerName Nombre del jugador que se conectó
     * @throws RemoteException Error en comunicación RMI
     */
    void onPlayerJoined(String playerName) throws RemoteException;
    
    /**
     * Notifica cambio de turno - Sincronización distribuida
     * @param isMyTurn true si es el turno del cliente
     * @param currentPlayerName nombre del jugador actual
     * @throws RemoteException Error en comunicación RMI
     */
    void onTurnChanged(boolean isMyTurn, String currentPlayerName) throws RemoteException;
    
    /**
     * Notifica fin del juego distribuido
     * @param winner Nombre del jugador ganador
     * @throws RemoteException Error en comunicación RMI
     */
    void onGameEnded(String winner) throws RemoteException;
    
    /**
     * Notifica desconexión de oponente
     * @throws RemoteException Error en comunicación RMI
     */
    void onOpponentDisconnected() throws RemoteException;
}
