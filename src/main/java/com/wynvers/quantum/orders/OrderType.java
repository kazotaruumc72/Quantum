package com.wynvers.quantum.orders;

/**
 * Types de catégories d'ordres
 */
public enum OrderType {
    CULTURES("quantum_orders_cultures", "🌾 Cultures"),
    LOOTS("quantum_orders_loots", "💎 Loots"),
    ITEMS("quantum_orders_items", "📦 Items"),
    POTIONS("quantum_orders_potions", "🧪 Potions"),
    ARMURES("quantum_orders_armures", "🛡️ Armures"),
    OUTILS("quantum_orders_outils", "⚒️ Outils");

    private final String typeId;
    private final String displayName;

    OrderType(String typeId, String displayName) {
        this.typeId = typeId;
        this.displayName = displayName;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Récupère un type depuis son ID
     */
    public static OrderType fromString(String typeId) {
        if (typeId == null) return ITEMS; // Défaut
        
        for (OrderType type : values()) {
            if (type.typeId.equalsIgnoreCase(typeId)) {
                return type;
            }
        }
        
        return ITEMS; // Défaut si non trouvé
    }
}
