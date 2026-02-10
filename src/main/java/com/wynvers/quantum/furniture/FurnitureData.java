package com.wynvers.quantum.furniture;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un furniture avec ses propriétés de réapparition et drops
 */
public class FurnitureData {
    
    private final String furnitureId;
    private final String nexoId;
    private final int respawnTime; // en secondes
    private final List<AlternativeFurniture> alternativeFurniture;
    private final List<FurnitureDrop> drops;
    
    public FurnitureData(String furnitureId, String nexoId, int respawnTime) {
        this.furnitureId = furnitureId;
        this.nexoId = nexoId;
        this.respawnTime = respawnTime;
        this.alternativeFurniture = new ArrayList<>();
        this.drops = new ArrayList<>();
    }
    
    public void addAlternativeFurniture(String furnitureId, double chance) {
        this.alternativeFurniture.add(new AlternativeFurniture(furnitureId, chance));
    }
    
    public void addDrop(FurnitureDrop drop) {
        this.drops.add(drop);
    }
    
    public String getFurnitureId() {
        return furnitureId;
    }
    
    public String getNexoId() {
        return nexoId;
    }
    
    public int getRespawnTime() {
        return respawnTime;
    }
    
    public List<AlternativeFurniture> getAlternativeFurniture() {
        return alternativeFurniture;
    }
    
    public List<FurnitureDrop> getDrops() {
        return drops;
    }
    
    /**
     * Sélectionne quel furniture va apparaître (original ou alternatif)
     */
    public String selectFurnitureToSpawn() {
        for (AlternativeFurniture alt : alternativeFurniture) {
            if (Math.random() * 100 < alt.getChance()) {
                return alt.getFurnitureId();
            }
        }
        return furnitureId; // Retourne le furniture original par défaut
    }
    
    /**
     * Classe interne pour les furnitures alternatifs
     */
    public static class AlternativeFurniture {
        private final String furnitureId;
        private final double chance;
        
        public AlternativeFurniture(String furnitureId, double chance) {
            this.furnitureId = furnitureId;
            this.chance = chance;
        }
        
        public String getFurnitureId() {
            return furnitureId;
        }
        
        public double getChance() {
            return chance;
        }
    }
}
