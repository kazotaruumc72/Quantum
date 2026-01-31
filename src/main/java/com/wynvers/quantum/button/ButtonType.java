package com.wynvers.quantum.button;

/**
 * Énumération des types de boutons personnalisés Quantum
 */
public enum ButtonType {
    
    /**
     * Type de bouton par défaut - bouton statique standard
     */
    DEFAULT,
    
    /**
     * Bouton de stockage virtuel
     * Affiche les items stockés avec leur quantité et prix
     * - Items Nexo: avec lore, glyph, tooltip
     * - Items Minecraft: avec quantité
     * Lore append: "Quantité: <amount>" et "Prix: <price>"
     */
    QUANTUM_STORAGE,
    
    /**
     * Bouton de basculement de mode vente
     * Bascule entre les modes STORAGE et VENTE
     * Modifie le placeholder %mode%
     */
    QUANTUM_SELL,
    
    /**
     * Bouton de sélection de pourcentage de vente
     * Permet de définir le pourcentage d'items à vendre
     * Configurable via les paramètres du type de bouton
     * Pourcentages disponibles: 25%, 50%, 75%, 100%
     */
    QUANTUM_SELL_POURCENTAGE;

        /**
     * Bouton de changement de mode
     * Bascule entre les modes STORAGE et VENTE
     * Modifie le placeholder %mode%
     * Paramètre: mode:<mode>
     */
    QUANTUM_CHANGE_MODE;
    
    /**
     * Récupère un type de bouton depuis une chaîne de caractères
     * @param type La chaîne représentant le type
     * @return Le ButtonType correspondant, ou DEFAULT si non trouvé
     */
    public static ButtonType fromString(String type) {
        if (type == null) return DEFAULT;
        
        try {
            return ButtonType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
    
    /**
     * Vérifie si ce type de bouton est un type Quantum personnalisé
     * @return true si c'est un type Quantum, false sinon
     */
    public boolean isQuantumType() {
        return this != DEFAULT;
    }
}
