package com.wynvers.quantum.orders;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un item configurable dans le système d'ordres
 */
public class OrderItem {
    private final String itemId;
    private final OrderType type;
    private final double minPrice;
    private final double maxPrice;
    private final String displayName;
    private final Material material;
    private final List<String> lore;

    public OrderItem(String itemId, ConfigurationSection section) {
        this.itemId = itemId;
        
        // Type (catégorie)
        String typeStr = section.getString("type", "quantum_orders_items");
        this.type = OrderType.fromString(typeStr);
        
        // Prix
        this.minPrice = section.getDouble("min", 0.0);
        this.maxPrice = section.getDouble("max", 100.0);
        
        // Display
        this.displayName = section.getString("display_name", itemId);
        
        // Material (optionnel)
        String materialStr = section.getString("material", "BARRIER");
        Material mat;
        try {
            mat = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.BARRIER;
        }
        this.material = mat;
        
        // Lore (optionnel)
        this.lore = section.getStringList("lore");
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
    }

    public String getItemId() {
        return itemId;
    }

    public OrderType getType() {
        return type;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    /**
     * Vérifie si le prix est dans la fourchette acceptable
     */
    public boolean isValidPrice(double price) {
        return price >= minPrice && price <= maxPrice;
    }
}
