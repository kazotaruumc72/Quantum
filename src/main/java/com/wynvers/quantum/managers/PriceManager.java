package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire des prix des items depuis price.yml
 */
public class PriceManager {

    private final Quantum plugin;
    private FileConfiguration priceConfig;
    private final Map<String, Double> priceCache;

    public PriceManager(Quantum plugin) {
        this.plugin = plugin;
        this.priceCache = new HashMap<>();
        loadPrices();
    }

    /**
     * Charge les prix depuis price.yml
     */
    public void loadPrices() {
        File priceFile = new File(plugin.getDataFolder(), "price.yml");
        
        if (!priceFile.exists()) {
            plugin.saveResource("price.yml", false);
        }
        
        priceConfig = YamlConfiguration.loadConfiguration(priceFile);
        priceCache.clear();
        
        // Charger tous les prix dans le cache
        for (String key : priceConfig.getKeys(false)) {
            double price = priceConfig.getDouble(key, 0.0);
            priceCache.put(key.toLowerCase(), price);
        }
        
        plugin.getQuantumLogger().info("Chargé " + priceCache.size() + " prix depuis price.yml");
    }

    /**
     * Récupère le prix d'un item
     * @param itemId L'ID de l'item (nexo:id ou MATERIAL)
     * @return Le prix, ou 0.0 si non défini
     */
    public double getPrice(String itemId) {
        if (itemId == null) return 0.0;
        return priceCache.getOrDefault(itemId.toLowerCase(), 0.0);
    }

    /**
     * Récupère le prix formaté d'un item
     * @param itemId L'ID de l'item
     * @return Prix formaté (ex: "1,500.00$")
     */
    public String getFormattedPrice(String itemId) {
        double price = getPrice(itemId);
        return formatPrice(price);
    }

    /**
     * Formate un prix en chaîne
     * @param price Le prix
     * @return Prix formaté (ex: "1,500.00$")
     */
    public String formatPrice(double price) {
        if (price == 0.0) {
            return "§c⚠ Prix non défini";
        }
        return String.format("§6%.2f$", price);
    }

    /**
     * Définit le prix d'un item
     * @param itemId L'ID de l'item
     * @param price Le nouveau prix
     */
    public void setPrice(String itemId, double price) {
        if (itemId == null) return;
        priceCache.put(itemId.toLowerCase(), price);
        priceConfig.set(itemId.toLowerCase(), price);
        
        try {
            File priceFile = new File(plugin.getDataFolder(), "price.yml");
            priceConfig.save(priceFile);
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Erreur lors de la sauvegarde de price.yml: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un item a un prix défini
     * @param itemId L'ID de l'item
     * @return true si le prix existe
     */
    public boolean hasPrice(String itemId) {
        if (itemId == null) return false;
        return priceCache.containsKey(itemId.toLowerCase()) && priceCache.get(itemId.toLowerCase()) > 0.0;
    }

    /**
     * Recharge les prix depuis le fichier
     */
    public void reload() {
        loadPrices();
    }
}
