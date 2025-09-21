package co.edu.uptc.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Punto de entrada del servidor distribuido
 * Demuestra configuración de RMI Registry y publicación de servicios
 */
public class ServerMain {
    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
    private static final int RMI_PORT = 1100;
    private static final String SERVICE_NAME = "GameService";
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando Servidor de Batalla Naval Distribuido...\n");
            
            // Crear e inicializar el servicio RMI
            GameServiceImpl gameService = new GameServiceImpl();
            System.out.println("✅ Servicio de juego creado");
            
            // Crear registro RMI - Componente clave de sistemas distribuidos
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("✅ Registro RMI creado en puerto " + RMI_PORT);
            
            // Publicar el servicio en el registro - Permite lookup remoto
            registry.bind(SERVICE_NAME, gameService);
            System.out.println("✅ Servicio publicado como '" + SERVICE_NAME + "'");
            
            // Mostrar información del servidor distribuido
            System.out.println("\n╔═══════════════════════════════════════════════════════╗");
            System.out.println("║           SERVIDOR BATALLA NAVAL DISTRIBUIDO         ║");
            System.out.println("╠═══════════════════════════════════════════════════════╣");
            System.out.println("║ 🌐 Puerto RMI: " + RMI_PORT + "                                   ║");
            System.out.println("║ 📡 Servicio: " + SERVICE_NAME + "                        ║");
            System.out.println("║ 🎮 Capacidad: 2 jugadores simultáneos               ║");
            System.out.println("║ 📊 Estado: Esperando conexiones de clientes...       ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");
            
            System.out.println("\n🔗 Los clientes pueden conectarse usando:");
            System.out.println("   Host: localhost (o IP del servidor)");
            System.out.println("   Puerto: " + RMI_PORT);
            System.out.println("   Servicio: " + SERVICE_NAME);
            
            System.out.println("\n⚠️  Para detener el servidor presiona Ctrl+C");
            
            // Configurar shutdown hook para limpieza
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Deteniendo servidor distribuido...");
                LOGGER.info("Servidor RMI detenido correctamente");
            }));
            
            LOGGER.info("Servidor RMI iniciado correctamente en puerto " + RMI_PORT);
            
            // Mantener el servidor en ejecución
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Error crítico iniciando servidor RMI: " + e.getMessage());
            LOGGER.severe("Error crítico en el servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
