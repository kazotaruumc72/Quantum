package com.wynvers.quantum.menu;

/**
 * Types de boutons spéciaux dans les menus
 */
public enum ButtonType {
    NONE,                    // Bouton normal (par défaut)
    STANDARD,                // Bouton standard (alias de NONE)
    QUANTUM_CHANGE_MODE,     // Bouton pour changer le mode (vente/achat)
    QUANTUM_CHANGE_AMOUNT,   // Bouton pour changer la quantité
    QUANTUM_SELL,            // Bouton de vente (ancien alias)
    QUANTUM_SELL_ITEM,       // Bouton pour vendre un item
    QUANTUM_ORDERS_ITEM      // Bouton dynamique pour afficher les ordres d'une catégorie
}
