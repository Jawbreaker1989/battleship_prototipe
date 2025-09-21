package co.edu.uptc.client;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Punto de entrada del cliente RMI
 * Conecta al servidor y lanza la GUI simple
 */
public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1100;
    
    public static void main(String[] args) {
        // Obtener parámetros de conexión
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        
        LOGGER.info("Conectando al servidor RMI en " + host + ":" + port);
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Buscar registro RMI
                Registry registry = LocateRegistry.getRegistry(host, port);
                
                // Crear y mostrar ventana del juego
                GameWindow gameWindow = new GameWindow(registry);
                gameWindow.setVisible(true);
                
                LOGGER.info("Cliente iniciado correctamente");
                
            } catch (Exception e) {
                LOGGER.severe("Error iniciando cliente: " + e.getMessage());
                
                // Mostrar error al usuario
                JOptionPane.showMessageDialog(null, 
                    "Error conectando al servidor:\n" + e.getMessage() + 
                    "\n\nVerifica que el servidor esté ejecutándose en " + host + ":" + port,
                    "Error de Conexión", 
                    JOptionPane.ERROR_MESSAGE);
                
                System.exit(1);
            }
        });
    }
}
