package co.edu.uptc.client;

import co.edu.uptc.shared.model.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel súper simple del tablero 10x10 para Batalla Naval
 * Interfaz CLARA: Mi tablero vs Tablero enemigo
 */
public class BoardPanel extends JPanel {
    private static final int BOARD_SIZE = 10;
    private static final int CELL_SIZE = 40;
    
    // Colores claros y obvios
    private static final Color WATER_COLOR = new Color(173, 216, 230);     // Azul claro
    private static final Color SHIP_COLOR = new Color(105, 105, 105);      // Gris barco
    private static final Color HIT_COLOR = Color.RED;                      // Rojo impacto
    private static final Color MISS_COLOR = Color.WHITE;                   // Blanco fallo
    private static final Color UNKNOWN_COLOR = new Color(0, 191, 255);     // Azul profundo
    
    private final GameWindow parentWindow;
    private final CellState[][] board;
    private final boolean isMyBoard;  // true = mi tablero, false = tablero enemigo
    
    // Variables para colocación manual de barcos
    private boolean isHorizontal = true;
    private int[] shipSizes = {5, 4, 3, 3, 2}; // Tamaños de barcos estándar
    private int currentShipIndex = 0; // Índice del barco actual a colocar
    private int shipsPlaced = 0; // Contador de barcos colocados
    
    private boolean attackMode = false;
    
    // Estados de celda súper simples
    enum CellState {
        WATER,      // Agua normal
        SHIP,       // Barco (solo visible en mi tablero)
        HIT,        // Impacto confirmado
        MISS,       // Fallo confirmado  
        UNKNOWN     // No atacado aún (solo en tablero enemigo)
    }
    
    public BoardPanel(GameWindow parentWindow, boolean isMyBoard) {
        this.parentWindow = parentWindow;
        this.isMyBoard = isMyBoard;
        this.board = new CellState[BOARD_SIZE][BOARD_SIZE];
        
        initializeBoard();
        setupMouseHandlers();
        
        setPreferredSize(new Dimension(
            BOARD_SIZE * CELL_SIZE + 1, 
            BOARD_SIZE * CELL_SIZE + 1
        ));
        
        setBackground(Color.BLUE);
    }
    
    private void initializeBoard() {
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (isMyBoard) {
                    board[x][y] = CellState.WATER;  // Mi tablero empieza vacío
                } else {
                    board[x][y] = CellState.UNKNOWN; // Tablero enemigo desconocido
                }
            }
        }
    }
    
    private void setupMouseHandlers() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Position pos = getPositionFromMouse(e);
                if (pos != null) {
                    if (isMyBoard && !attackMode) {
                        // Modo colocación de barcos en mi tablero
                        tryPlaceShip(pos.getX(), pos.getY());
                    } else if (!isMyBoard && attackMode) {
                        // Modo ataque en tablero enemigo
                        if (board[pos.getX()][pos.getY()] == CellState.UNKNOWN) {
                            parentWindow.onEnemyCellClicked(pos.getX(), pos.getY());
                        }
                    }
                }
            }
        };
        
        addMouseListener(mouseHandler);
    }
    
    private Position getPositionFromMouse(MouseEvent e) {
        int x = e.getX() / CELL_SIZE;
        int y = e.getY() / CELL_SIZE;
        
        if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
            return new Position(x, y);
        }
        return null;
    }
    
    public void markAttack(Position target, String result) {
        int x = target.getX();
        int y = target.getY();
        
        if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
            if (result.contains("HIT") || result.contains("SUNK")) {
                board[x][y] = CellState.HIT;
            } else if (result.contains("MISS")) {
                board[x][y] = CellState.MISS;
            }
            repaint();
        }
    }
    
    public void setAttackMode(boolean enabled) {
        this.attackMode = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : 
                           Cursor.getDefaultCursor());
    }
    
    // === MÉTODOS PARA COLOCACIÓN MANUAL DE BARCOS ===
    
    public void toggleOrientation() {
        this.isHorizontal = !this.isHorizontal;
    }
    
    public boolean isHorizontal() {
        return this.isHorizontal;
    }
    
    public boolean allShipsPlaced() {
        return shipsPlaced >= shipSizes.length;
    }
    
    private void tryPlaceShip(int x, int y) {
        if (!isMyBoard || allShipsPlaced()) {
            return;
        }
        
        int shipSize = shipSizes[currentShipIndex];
        if (canPlaceShip(x, y, shipSize, isHorizontal)) {
            placeShip(x, y, shipSize, isHorizontal);
            shipsPlaced++;
            currentShipIndex++;
            parentWindow.showMessage("✅ Barco colocado! " + 
                (allShipsPlaced() ? "Todos los barcos listos!" : 
                "Siguiente: " + (shipSizes.length - shipsPlaced) + " barcos restantes"));
            repaint();
        } else {
            parentWindow.showMessage("❌ No se puede colocar el barco aquí");
        }
    }
    
    private boolean canPlaceShip(int x, int y, int size, boolean horizontal) {
        // Verificar que el barco quepa en el tablero
        if (horizontal) {
            if (x + size > BOARD_SIZE) return false;
        } else {
            if (y + size > BOARD_SIZE) return false;
        }
        
        // Verificar que no haya colisión con otros barcos
        for (int i = 0; i < size; i++) {
            int checkX = horizontal ? x + i : x;
            int checkY = horizontal ? y : y + i;
            
            if (board[checkX][checkY] == CellState.SHIP) {
                return false;
            }
            
            // Verificar espacios adyacentes (regla de no barcos pegados)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int adjX = checkX + dx;
                    int adjY = checkY + dy;
                    
                    if (adjX >= 0 && adjX < BOARD_SIZE && adjY >= 0 && adjY < BOARD_SIZE) {
                        if (board[adjX][adjY] == CellState.SHIP) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private void placeShip(int x, int y, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int shipX = horizontal ? x + i : x;
            int shipY = horizontal ? y : y + i;
            board[shipX][shipY] = CellState.SHIP;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar celdas
        drawCells(g2d);
        
        // Dibujar grid
        drawGrid(g2d);
        
        // Dibujar coordenadas
        drawCoordinates(g2d);
    }
    
    private void drawCells(Graphics2D g2d) {
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                Color cellColor = getCellColor(board[x][y]);
                
                g2d.setColor(cellColor);
                g2d.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, 
                           CELL_SIZE - 1, CELL_SIZE - 1);
                
                // Agregar símbolo visual
                drawCellSymbol(g2d, x, y, board[x][y]);
            }
        }
    }
    
    private Color getCellColor(CellState state) {
        switch (state) {
            case WATER: return WATER_COLOR;
            case SHIP: return isMyBoard ? SHIP_COLOR : UNKNOWN_COLOR;
            case HIT: return HIT_COLOR;
            case MISS: return MISS_COLOR;
            case UNKNOWN: return UNKNOWN_COLOR;
            default: return WATER_COLOR;
        }
    }
    
    private void drawCellSymbol(Graphics2D g2d, int x, int y, CellState state) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        String symbol = "";
        switch (state) {
            case WATER:
                symbol = ""; // Agua sin símbolo
                break;
            case SHIP:
                if (isMyBoard) symbol = "⚓"; // Solo mostrar barcos en mi tablero
                break;
            case HIT:
                symbol = "💥";
                break;
            case MISS:
                symbol = "○";
                break;
            case UNKNOWN:
                symbol = "?";
                break;
        }
        
        if (!symbol.isEmpty()) {
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x * CELL_SIZE + (CELL_SIZE - fm.stringWidth(symbol)) / 2;
            int textY = y * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2;
            g2d.drawString(symbol, textX, textY);
        }
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // Líneas verticales
        for (int x = 0; x <= BOARD_SIZE; x++) {
            int xPos = x * CELL_SIZE;
            g2d.drawLine(xPos, 0, xPos, BOARD_SIZE * CELL_SIZE);
        }
        
        // Líneas horizontales
        for (int y = 0; y <= BOARD_SIZE; y++) {
            int yPos = y * CELL_SIZE;
            g2d.drawLine(0, yPos, BOARD_SIZE * CELL_SIZE, yPos);
        }
    }
    
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Números en columnas (parte superior)
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int x = i * CELL_SIZE + (CELL_SIZE - fm.stringWidth(label)) / 2;
            g2d.drawString(label, x, -5);
        }
        
        // Letras en filas (lado izquierdo)
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf((char) ('A' + i));
            FontMetrics fm = g2d.getFontMetrics();
            int y = i * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2;
            g2d.drawString(label, -15, y);
        }
    }
}