package com.wynvers.quantum.storage.upgrades;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe parente pour tous les améliorateurs de storage
 * Tous les upgrades sont stackables et modifient le comportement du storage
 */
public abstract class StorageUpgrade {
    
    protected final String id;
    protected int level;
    
    public StorageUpgrade(String id) {
        this.id = id;
        this.level = 0;
    }
    
    /**
     * Obtenir l'ID unique de l'upgrade
     */
    public String getId() {
        return id;
    }
    
    /**
     * Obtenir le niveau actuel de l'upgrade
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Augmenter le niveau de l'upgrade
     */
    public void upgrade() {
        this.level++;
    }
    
    /**
     * Définir le niveau de l'upgrade
     */
    public void setLevel(int level) {
        this.level = level;
    }
    
    /**
     * Obtenir le nom affiché de l'upgrade
     */
    public abstract String getDisplayName();
    
    /**
     * Obtenir la description de l'upgrade
     */
    public abstract String getDescription();
    
    /**
     * Obtenir le coût pour acheter la prochaine version
     */
    public abstract double getCost();
}