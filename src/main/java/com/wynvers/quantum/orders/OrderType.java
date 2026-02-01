package com.wynvers.quantum.orders;

/**
 * Types de catégories d'ordres
 */
public enum OrderType {
    CULTURES("quantum_orders_cultures", "Cultures"),
    LOOTS("quantum_orders_loots", "Loots"),
    ITEMS("quantum_orders_items", "Items"),
    ARMURES("quantum_orders_armures", "Armures"),
    OUTILS("quantum_orders_outils", "Outils");

    private final String id;
    private final String displayName;

    OrderType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convertit une chaîne en OrderType
     */
    public static OrderType fromString(String str) {
        for (OrderType type : values()) {
            if (type.id.equalsIgnoreCase(str)) {
                return type;
            }
        }
        return ITEMS; // Par défaut
    }
}
