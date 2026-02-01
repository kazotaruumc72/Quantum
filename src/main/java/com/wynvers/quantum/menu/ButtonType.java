package com.wynvers.quantum.menu;

public enum ButtonType {
    /**
     * Bouton standard sans comportement spécial
     */
    STANDARD,
    
    /**
     * Bouton pour changer le mode de storage (STORAGE <-> SELL)
     */
    QUANTUM_CHANGE_MODE,
    
    /**
     * Slots de storage quantum (items stockés)
     */
    QUANTUM_STORAGE,
    
    /**
     * Bouton pour modifier la quantité d'items à vendre (+10, -10, etc.)
     */
    QUANTUM_CHANGE_AMOUNT,
    
    /**
     * Bouton pour confirmer la vente
     */
    QUANTUM_SELL,
    
    /**
     * Item à vendre (affiché dynamiquement depuis la SellSession)
     */
    QUANTUM_SELL_ITEM
}
