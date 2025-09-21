package co.edu.uptc.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tablero de batalla naval 10x10
 * Serializable para sincronización en sistema distribuido
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final int SIZE = 10;
    
    private final List<Ship> ships;
    private final boolean[][] attacks; // true = ya atacado
    private final boolean[][] hits;    // true = impacto
    
    public Board() {
        this.ships = new ArrayList<>();
        this.attacks = new boolean[SIZE][SIZE];
        this.hits = new boolean[SIZE][SIZE];
    }
    
    public boolean placeShip(Position start, Position end) {
        Ship newShip = new Ship(start, end);
        
        // Verificar que no se superponga con otros barcos
        for (Ship existingShip : ships) {
            for (Position pos : newShip.getPositions()) {
                if (existingShip.occupiesPosition(pos)) {
                    return false;
                }
            }
        }
        
        ships.add(newShip);
        return true;
    }
    
    /**
     * Recibe un ataque remoto y retorna el resultado
     * @param position Posición del ataque
     * @return Resultado del ataque para comunicación distribuida
     */
    public AttackResult receiveAttack(Position position) {
        int x = position.getX();
        int y = position.getY();
        
        if (attacks[y][x]) {
            return AttackResult.ALREADY_ATTACKED;
        }
        
        attacks[y][x] = true;
        
        // Buscar si hay un barco en esta posición
        for (Ship ship : ships) {
            if (ship.occupiesPosition(position)) {
                ship.hit(position);
                hits[y][x] = true;
                
                if (ship.isSunk()) {
                    if (allShipsSunk()) {
                        return AttackResult.SUNK_AND_GAME_OVER;
                    }
                    return AttackResult.SUNK;
                }
                return AttackResult.HIT;
            }
        }
        
        return AttackResult.MISS;
    }
    
    public boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }
    
    public boolean hasShipAt(Position position) {
        return ships.stream().anyMatch(ship -> ship.occupiesPosition(position));
    }
    
    public boolean isAttacked(Position position) {
        return attacks[position.getY()][position.getX()];
    }
    
    public boolean isHit(Position position) {
        return hits[position.getY()][position.getX()];
    }
    
    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }
    
    public int getShipCount() {
        return ships.size();
    }
    
    /**
     * Enum para resultados de ataque en sistema distribuido
     * Serializable para transferencia RMI
     */
    public enum AttackResult {
        HIT("Impacto"),
        MISS("Agua"),
        SUNK("Barco hundido"),
        SUNK_AND_GAME_OVER("Último barco hundido - Juego terminado"),
        ALREADY_ATTACKED("Ya atacado");
        
        private final String description;
        
        AttackResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
