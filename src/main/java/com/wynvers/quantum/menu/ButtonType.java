package com.wynvers.quantum.menu;

/**
 * Types de boutons spéciaux dans les menus
 */
public enum ButtonType {
    NONE,                           // Bouton normal (par défaut)
    STANDARD,                       // Bouton standard (alias de NONE)
    QUANTUM_CHANGE_MODE,            // Bouton pour changer le mode (vente/achat)
    QUANTUM_CHANGE_AMOUNT,          // Bouton pour changer la quantité
    QUANTUM_SELL,                   // Bouton de vente (ancien alias)
    QUANTUM_SELL_ITEM,              // Bouton pour vendre un item
    QUANTUM_ORDERS_ITEM,            // Bouton dynamique pour afficher les ordres d'une catégorie
    
    // === ORDER CREATION BUTTONS ===
    QUANTUM_ADJUST_QUANTITY,        // Ajuster quantité d'ordre (param: amount)
    QUANTUM_SET_QUANTITY_MAX,       // Définir quantité au max
    QUANTUM_VALIDATE_QUANTITY,      // Valider quantité et passer au prix
    QUANTUM_ADJUST_PRICE,           // Ajuster prix d'ordre (param: percentage)
    QUANTUM_SET_PRICE_MAX,          // Définir prix au max
    QUANTUM_FINALIZE_ORDER,         // Finaliser et créer l'ordre
    QUANTUM_CANCEL_ORDER            // Annuler création d'ordre
}
