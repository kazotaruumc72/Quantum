package com.wynvers.quantum.menu;

public enum ButtonType {
    /**
     * Bouton standard sans comportement sp\u00e9cial
     */
    STANDARD,
    
    /**
     * Bouton pour changer le mode de storage (STORAGE <-> SELL)
     */
    QUANTUM_CHANGE_MODE,
    
    /**
     * Slots de storage quantum (items stock\u00e9s)
     */
    QUANTUM_STORAGE,
    
    /**
     * Bouton pour modifier la quantit\u00e9 d'items \u00e0 vendre (+10, -10, etc.)
     */
    QUANTUM_CHANGE_AMOUNT,
    
    /**
     * Bouton pour confirmer la vente
     */
    QUANTUM_SELL
}
