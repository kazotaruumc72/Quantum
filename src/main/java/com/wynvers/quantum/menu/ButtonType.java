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
    QUANTUM_ORDER_DISPLAY_ITEM,     // Bouton pour afficher l'item sélectionné dans la création d'ordre
    QUANTUM_ORDERS_ITEM,            // Bouton dynamique pour afficher les ordres d'une catégorie
    
    // === ORDER CREATION BUTTONS ===
    QUANTUM_ADJUST_QUANTITY,        // Ajuster quantité d'ordre (param: amount)
    QUANTUM_SET_QUANTITY_MAX,       // Définir quantité au max
    QUANTUM_VALIDATE_QUANTITY,      // Valider quantité et passer au prix
    QUANTUM_ADJUST_PRICE,           // Ajuster prix d'ordre (param: percentage)
    QUANTUM_SET_PRICE_MAX,          // Définir prix au max
    QUANTUM_FINALIZE_ORDER,         // Finaliser et créer l'ordre
    QUANTUM_CANCEL_ORDER,           // Annuler création d'ordre
    
    // === ORDER TRANSACTION BUTTONS ===
    QUANTUM_ORDER_CONFIRM_DISPLAY,  // Afficher l'item dans le menu de confirmation
    QUANTUM_CONFIRM_ORDER_SELL,     // Accepter la vente (exécuter la transaction)
    QUANTUM_CANCEL_ORDER_CONFIRM,   // Refuser la vente (retour au menu catégorie)
    
    // === PAGINATION BUTTONS (NEW) ===
    QUANTUM_ORDERS_NEXT_PAGE,       // Page suivante dans les menus d'ordres
    QUANTUM_ORDERS_PREV_PAGE,       // Page précédente dans les menus d'ordres
    QUANTUM_STORAGE_NEXT_PAGE,      // Page suivante dans le storage
    QUANTUM_STORAGE_PREV_PAGE,      // Page précédente dans le storage
    
    // === FILTER BUTTONS (NEW) ===
    QUANTUM_STORAGE_SEARCH,         // Rechercher un item dans le storage
    QUANTUM_STORAGE_FILTER_TYPE,    // Filtrer par type (Nexo/Minecraft/Tous)
    QUANTUM_STORAGE_SORT,           // Changer le mode de tri
    QUANTUM_STORAGE_RESET_FILTERS,  // Réinitialiser tous les filtres
    
    // === HISTORY & STATS BUTTONS (NEW) ===
    QUANTUM_VIEW_HISTORY,           // Ouvrir l'historique des transactions
    QUANTUM_VIEW_STATS,             // Ouvrir les statistiques de trading
    QUANTUM_HISTORY_FILTER_BUY,     // Filtrer historique: achats seulement
    QUANTUM_HISTORY_FILTER_SELL,    // Filtrer historique: ventes seulement
    QUANTUM_HISTORY_FILTER_ALL,     // Filtrer historique: tout
    QUANTUM_STATS_PERIOD_TODAY,     // Stats: aujourd'hui
    QUANTUM_STATS_PERIOD_WEEK,      // Stats: cette semaine
    QUANTUM_STATS_PERIOD_MONTH,     // Stats: ce mois
    QUANTUM_STATS_PERIOD_ALL,       // Stats: global

    // === TOWER STORAGE BUTTONS ===
    QUANTUM_TOWER_CHANGE_MODE,          // Bouton pour changer le mode du tower storage
    QUANTUM_TOWER_STORAGE_NEXT_PAGE,    // Page suivante dans le tower storage
    QUANTUM_TOWER_STORAGE_PREV_PAGE,    // Page précédente dans le tower storage
    QUANTUM_TOWER_STORAGE_SEARCH,       // Rechercher un item dans le tower storage
    QUANTUM_TOWER_STORAGE_FILTER_TYPE,  // Filtrer par type dans le tower storage
    QUANTUM_TOWER_STORAGE_SORT,         // Changer le mode de tri du tower storage
    QUANTUM_TOWER_STORAGE_RESET_FILTERS, // Réinitialiser les filtres du tower storage

    // === STORAGE UPGRADE BUTTONS ===
    QUANTUM_STORAGE_UPGRADE_MULTIPLIER, // Upgrade multiplicateur de vente
    QUANTUM_STORAGE_UPGRADE_STACK,      // Upgrade taille des stacks
    QUANTUM_STORAGE_UPGRADE_PAGE,       // Upgrade nombre de pages
    QUANTUM_STORAGE_SELL_ALL,           // Vendre tout le contenu du storage
    QUANTUM_TOWER_STORAGE_SELL_ALL,     // Vendre tout le contenu du tower storage

    // === TOWER STORAGE UPGRADE BUTTONS ===
    QUANTUM_TOWER_STORAGE_UPGRADE_MULTIPLIER, // Upgrade multiplicateur de vente (tower storage)
    QUANTUM_TOWER_STORAGE_UPGRADE_STACK,      // Upgrade taille des stacks (tower storage)
    QUANTUM_TOWER_STORAGE_UPGRADE_PAGE        // Upgrade nombre de pages (tower storage)
}
