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
    
    // Estado GUI
    private String playerName;
    
    public GameWindow(Registry registry) throws Exception {
        this.controller = new GameController(registry);
        controller.initialize();
        controller.setGameWindow(this);
        
        // Auto-conectar con nombre automático
        autoConnect();
        
        initializeGUI();
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
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.DARK_GRAY);
        
        statusLabel = new JLabel("Conectando...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(statusLabel);
        
        panel.add(Box.createHorizontalStrut(20));
        
        turnLabel = new JLabel("");
        turnLabel.setForeground(Color.YELLOW);
        turnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.add(turnLabel);
        
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
        
        // Título
        JLabel titleLabel = new JLabel("🎯 Haz clic en TU TABLERO para colocar barcos");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para cambiar orientación
        JButton orientationButton = new JButton("🔄 Horizontal");
        orientationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        orientationButton.addActionListener(e -> {
            myBoard.toggleOrientation();
            orientationButton.setText(myBoard.isHorizontal() ? "🔄 Horizontal" : "🔄 Vertical");
        });
        panel.add(orientationButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Lista de barcos por colocar
        JLabel shipsLabel = new JLabel("Barcos restantes:");
        shipsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(shipsLabel);
        
        // Barcos por colocar
        JLabel ship1 = new JLabel("🚢 Portaaviones (5 casillas)");
        JLabel ship2 = new JLabel("🚢 Acorazado (4 casillas)");
        JLabel ship3 = new JLabel("🚢 Crucero (3 casillas)");
        JLabel ship4 = new JLabel("🚢 Submarino (3 casillas)");
        JLabel ship5 = new JLabel("🚢 Destructor (2 casillas)");
        
        ship1.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship2.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship3.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship4.setAlignmentX(Component.CENTER_ALIGNMENT);
        ship5.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(ship1);
        panel.add(ship2);
        panel.add(ship3);
        panel.add(ship4);
        panel.add(ship5);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Botón para listo
        JButton readyButton = new JButton("✅ ¡LISTO PARA JUGAR!");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setBackground(Color.GREEN);
        readyButton.setForeground(Color.WHITE);
        readyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        readyButton.addActionListener(e -> {
            // Verificar que todos los barcos estén colocados
            if (myBoard.allShipsPlaced()) {
                showMessage("✅ Todos los barcos colocados. Esperando oponente...");
                readyButton.setEnabled(false);
            } else {
                showMessage("❌ Debes colocar todos los barcos primero");
            }
        });
        panel.add(readyButton);
        
        return panel;
    }
    
    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📢 Mensajes"));
        panel.setPreferredSize(new Dimension(250, 400));
        
        messageArea = new JTextArea(20, 15);
        messageArea.setEditable(false);
        messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        messageArea.setBackground(Color.BLACK);
        messageArea.setForeground(Color.GREEN);
        
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