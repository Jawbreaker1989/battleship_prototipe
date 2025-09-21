package co.edu.uptc.client;

import co.edu.uptc.shared.model.Position;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Ventana principal SÚPER SIMPLE del juego Batalla Naval
 * Dos tableros lado a lado: TU FLOTA vs ENEMIGO
 */
public class GameWindow extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(GameWindow.class.getName());
    
    private final GameController controller;
    private BoardPanel myBoard;      // Mi tablero (solo para ver mis barcos)
    private BoardPanel enemyBoard;   // Tablero enemigo (para atacar)
    
    // Componentes GUI simplificados
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JTextArea messageArea;
    private JButton connectButton;
    
    // Estado GUI
    private String playerName;
    
    public GameWindow(Registry registry) throws Exception {
        this.controller = new GameController(registry);
        controller.initialize();
        controller.setGameWindow(this);
        
        initializeGUI();
        
        // Auto-conectar con nombre automático después de inicializar GUI
        autoConnect();
    }
    
    private void autoConnect() {
        SwingUtilities.invokeLater(() -> {
            // Generar nombre automático
            String autoName = "Jugador" + (System.currentTimeMillis() % 1000);
            playerName = autoName;
            
            // Conectar automáticamente
            try {
                controller.connectPlayer(autoName);
            } catch (Exception ex) {
                LOGGER.severe("Error en auto-conexión: " + ex.getMessage());
                showError("Error conectando automáticamente: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Intenta reconectar al servidor
     */
    private void reconnect() {
        SwingUtilities.invokeLater(() -> {
            showMessage("🔄 Intentando reconectar...");
            updateStatus("🔄 Reconectando...");
            
            String autoName = "Jugador" + (System.currentTimeMillis() % 1000);
            playerName = autoName;
            
            try {
                controller.connectPlayer(autoName);
                connectButton.setText("✅ Conectado");
                connectButton.setEnabled(false);
            } catch (Exception ex) {
                LOGGER.severe("Error en reconexión: " + ex.getMessage());
                showError("Error reconectando: " + ex.getMessage());
                connectButton.setText("❌ Reintentar");
            }
        });
    }
    
    private void initializeGUI() {
        setTitle("🚢 BATALLA NAVAL - " + playerName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Panel superior - Estado del juego
        JPanel topPanel = createStatusPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central - TABLEROS LADO A LADO
        JPanel centerPanel = createGameBoards();
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel derecho - Controles de barcos + Mensajes
        JPanel rightPanel = createControlPanel();
        add(rightPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        
        LOGGER.info("GUI inicializada");
    }
    
    private JPanel createGameBoards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // MI TABLERO (izquierda)
        JPanel myBoardPanel = new JPanel(new BorderLayout());
        myBoardPanel.setBorder(BorderFactory.createTitledBorder("🏠 TU FLOTA"));
        myBoard = new BoardPanel(this, true);  // true = mi tablero
        myBoardPanel.add(myBoard, BorderLayout.CENTER);
        
        // TABLERO ENEMIGO (derecha)
        JPanel enemyBoardPanel = new JPanel(new BorderLayout());
        enemyBoardPanel.setBorder(BorderFactory.createTitledBorder("🎯 ATACAR ENEMIGO"));
        enemyBoard = new BoardPanel(this, false); // false = tablero enemigo
        enemyBoardPanel.add(enemyBoard, BorderLayout.CENTER);
        
        panel.add(myBoardPanel);
        panel.add(enemyBoardPanel);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        
        // Panel izquierdo para el status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.DARK_GRAY);
        
        statusLabel = new JLabel("🔌 Desconectado");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusPanel.add(statusLabel);
        
        // Panel derecho para controles de conexión
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(Color.DARK_GRAY);
        
        connectButton = new JButton("🔗 Reconectar");
        connectButton.setBackground(new Color(70, 130, 180));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        connectButton.addActionListener(e -> reconnect());
        controlPanel.add(connectButton);
        
        // Panel central para turno
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(Color.DARK_GRAY);
        
        turnLabel = new JLabel("");
        turnLabel.setForeground(Color.YELLOW);
        turnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        centerPanel.add(turnLabel);
        
        panel.add(statusPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 400));
        
        // Panel superior - Controles de barcos
        JPanel shipControlPanel = createShipControls();
        panel.add(shipControlPanel, BorderLayout.NORTH);
        
        // Panel inferior - Mensajes
        JPanel messagePanel = createMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createShipControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("🚢 COLOCAR BARCOS"));
        panel.setBackground(new Color(240, 248, 255));
        
        // Título mejorado
        JLabel titleLabel = new JLabel("<html><center>🎯 Haz clic en TU TABLERO<br>para colocar barcos</center></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para cambiar orientación mejorado
        JButton orientationButton = new JButton("🔄 Orientación: Horizontal");
        orientationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        orientationButton.setBackground(new Color(70, 130, 180));
        orientationButton.setForeground(Color.WHITE);
        orientationButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        orientationButton.addActionListener(e -> {
            myBoard.toggleOrientation();
            orientationButton.setText(myBoard.isHorizontal() ? 
                "🔄 Orientación: Horizontal" : "🔄 Orientación: Vertical");
        });
        panel.add(orientationButton);
        panel.add(Box.createVerticalStrut(15));
        
        // Lista de barcos por colocar con colores
        JLabel shipsLabel = new JLabel("🛡️ Barcos por colocar:");
        shipsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shipsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(shipsLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Barcos por colocar con indicadores visuales
        String[] shipDescriptions = {
            "🚢 Portaaviones (5 casillas)",
            "⚓ Acorazado (4 casillas)", 
            "🛥️ Crucero (3 casillas)",
            "🚤 Submarino (3 casillas)",
            "⛵ Destructor (2 casillas)"
        };
        
        for (String shipDesc : shipDescriptions) {
            JLabel shipLabel = new JLabel(shipDesc);
            shipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            panel.add(shipLabel);
        }
        
        panel.add(Box.createVerticalStrut(15));
        
        // Indicador de progreso
        JLabel progressLabel = new JLabel("Progreso: 0/5 barcos");
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        progressLabel.setForeground(Color.BLUE);
        panel.add(progressLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para listo mejorado
        JButton readyButton = new JButton("✅ ¡LISTO PARA JUGAR!");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setBackground(new Color(34, 139, 34));
        readyButton.setForeground(Color.WHITE);
        readyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        readyButton.addActionListener(e -> {
            // Verificar que todos los barcos estén colocados
            if (myBoard.allShipsPlaced()) {
                showMessage("✅ Todos los barcos colocados. Esperando oponente...");
                readyButton.setEnabled(false);
                readyButton.setText("⏳ Esperando...");
            } else {
                showMessage("❌ Debes colocar todos los barcos primero (5 en total)");
            }
        });
        panel.add(readyButton);
        
        return panel;
    }
    
    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📢 Mensajes del Juego"));
        panel.setPreferredSize(new Dimension(250, 400));
        
        messageArea = new JTextArea(20, 15);
        messageArea.setEditable(false);
        messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        messageArea.setBackground(new Color(25, 25, 25));
        messageArea.setForeground(new Color(0, 255, 0));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        
        // Agregar mensaje de bienvenida
        messageArea.setText("🎮 ¡Bienvenido a Batalla Naval!\n" +
                           "📋 Instrucciones:\n" +
                           "1. Coloca 5 barcos en tu tablero\n" +
                           "2. Cambia orientación con el botón\n" +
                           "3. Haz clic para atacar al enemigo\n" +
                           "4. ¡Hunde todos sus barcos!\n\n");
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // === Métodos para interacción con el controlador ===
    
    public void onEnemyCellClicked(int x, int y) {
        Position pos = new Position(x, y);
        if (controller.isConnected()) {
            controller.attack(pos);
        }
    }
    
    // === Métodos para actualizar GUI desde controlador ===
    
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
    
    public void showError(String error) {
        SwingUtilities.invokeLater(() -> {
            showMessage("ERROR: " + error);
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }
    
    public void setTurnIndicator(boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            if (isMyTurn) {
                turnLabel.setText("🎯 ES TU TURNO - ¡ATACA!");
                turnLabel.setForeground(Color.GREEN);
                enemyBoard.setAttackMode(true);
            } else {
                turnLabel.setText("⏳ Turno del oponente...");
                turnLabel.setForeground(Color.RED);
                enemyBoard.setAttackMode(false);
            }
        });
    }
    
    public void markEnemyAttack(Position target, String result) {
        SwingUtilities.invokeLater(() -> {
            enemyBoard.markAttack(target, result);
        });
    }
    
    // === Getters ===
    
    public String getPlayerName() {
        return playerName;
    }
    
    public BoardPanel getMyBoard() {
        return myBoard;
    }
    
    public BoardPanel getEnemyBoard() {
        return enemyBoard;
    }
}