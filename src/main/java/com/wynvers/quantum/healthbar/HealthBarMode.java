package com.wynvers.quantum.healthbar;

/**
 * Mode d'affichage de la barre de vie
 */
public enum HealthBarMode {
    PERCENTAGE,  // Affiche en pourcentage (100%)
    HEARTS;      // Affiche en cœurs (❤❤❤)
    
    public static HealthBarMode fromString(String mode) {
        try {
            return valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PERCENTAGE; // Par défaut
        }
    }
}
