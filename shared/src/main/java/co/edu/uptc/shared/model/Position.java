package co.edu.uptc.shared.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object inmutable para coordenadas del tablero
 * Serializable para transferencia en sistemas distribuidos RMI
 */
public final class Position implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int x;
    private final int y;
    
    public Position(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            throw new IllegalArgumentException("Posición fuera del tablero: (" + x + "," + y + ")");
        }
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    public boolean isAdjacent(Position other) {
        int dx = Math.abs(this.x - other.x);
        int dy = Math.abs(this.y - other.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
