package co.edu.uptc.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa un barco en el juego distribuido
 * Serializable para transferencia entre cliente-servidor RMI
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final List<Position> positions;
    private final List<Boolean> hits;
    private final int size;
    
    public Ship(Position start, Position end) {
        this.positions = calculatePositions(start, end);
        this.size = positions.size();
        this.hits = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            hits.add(false);
        }
    }
    
    private List<Position> calculatePositions(Position start, Position end) {
        List<Position> result = new ArrayList<>();
        
        if (start.getX() == end.getX()) {
            // Vertical
            int minY = Math.min(start.getY(), end.getY());
            int maxY = Math.max(start.getY(), end.getY());
            for (int y = minY; y <= maxY; y++) {
                result.add(new Position(start.getX(), y));
            }
        } else if (start.getY() == end.getY()) {
            // Horizontal
            int minX = Math.min(start.getX(), end.getX());
            int maxX = Math.max(start.getX(), end.getX());
            for (int x = minX; x <= maxX; x++) {
                result.add(new Position(x, start.getY()));
            }
        } else {
            throw new IllegalArgumentException("El barco debe ser horizontal o vertical");
        }
        
        return result;
    }
    
    public boolean occupiesPosition(Position position) {
        return positions.contains(position);
    }
    
    public boolean hit(Position position) {
        int index = positions.indexOf(position);
        if (index >= 0 && !hits.get(index)) {
            hits.set(index, true);
            return true;
        }
        return false;
    }
    
    public boolean isSunk() {
        return hits.stream().allMatch(hit -> hit);
    }
    
    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }
    
    public int getSize() {
        return size;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ship ship = (Ship) obj;
        return Objects.equals(positions, ship.positions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(positions);
    }
}
