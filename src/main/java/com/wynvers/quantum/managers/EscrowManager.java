package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gestionnaire d'escrow (dépôt fiduciaire)
 * 
 * Stocke l'argent des acheteurs lors de la création d'un ordre.
 * L'argent est conservé jusqu'à ce qu'un vendeur accepte l'ordre,
 * ou remboursé si l'ordre est annulé/expiré.
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class EscrowManager {
    
    private final Quantum plugin;
    private final File escrowFile;
    private FileConfiguration escrowConfig;
    
    // Cache en mémoire: orderId -> montant
    private final Map<UUID, Double> escrowCache;
    
    public EscrowManager(Quantum plugin) {
        this.plugin = plugin;
        this.escrowFile = new File(plugin.getDataFolder(), "escrow.yml");
        this.escrowCache = new HashMap<>();
        
        loadEscrow();
    }
    
    /**
     * Charge le fichier escrow.yml
     */
    private void loadEscrow() {
        if (!escrowFile.exists()) {
            try {
                escrowFile.createNewFile();
                plugin.getQuantumLogger().info("Created escrow.yml file");
            } catch (IOException e) {
                plugin.getQuantumLogger().error("Failed to create escrow.yml!");
                e.printStackTrace();
            }
        }
        
        escrowConfig = YamlConfiguration.loadConfiguration(escrowFile);
        
        // Charger tous les montants en cache
        if (escrowConfig.isConfigurationSection("escrow")) {
            for (String key : escrowConfig.getConfigurationSection("escrow").getKeys(false)) {
                try {
                    UUID orderId = UUID.fromString(key);
                    double amount = escrowConfig.getDouble("escrow." + key);
                    escrowCache.put(orderId, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getQuantumLogger().warning("Invalid UUID in escrow.yml: " + key);
                }
            }
        }
        
        plugin.getQuantumLogger().success("✓ EscrowManager loaded with " + escrowCache.size() + " entries");
    }
    
    /**
     * Sauvegarde le fichier escrow.yml
     */
    public void saveEscrow() {
        try {
            escrowConfig.save(escrowFile);
        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to save escrow.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Dépose de l'argent en escrow pour un ordre
     * 
     * @param orderId ID de l'ordre
     * @param amount Montant à déposer
     * @return true si le dépôt a réussi
     */
    public boolean deposit(UUID orderId, double amount) {
        if (orderId == null || amount <= 0) {
            return false;
        }
        
        // Vérifier qu'il n'y a pas déjà un dépôt pour cet ordre
        if (escrowCache.containsKey(orderId)) {
            plugin.getQuantumLogger().warning("Escrow already exists for order " + orderId);
            return false;
        }
        
        // Ajouter au cache
        escrowCache.put(orderId, amount);
        
        // Sauvegarder dans le fichier
        escrowConfig.set("escrow." + orderId.toString(), amount);
        saveEscrow();
        
        plugin.getQuantumLogger().info("Deposited " + amount + "€ in escrow for order " + orderId);
        return true;
    }
    
    /**
     * Retire de l'argent de l'escrow (lorsqu'un vendeur accepte l'ordre)
     * 
     * @param orderId ID de l'ordre
     * @return Montant retiré, ou 0.0 si l'ordre n'existe pas
     */
    public double withdraw(UUID orderId) {
        if (orderId == null) {
            return 0.0;
        }
        
        // Vérifier que le dépôt existe
        if (!escrowCache.containsKey(orderId)) {
            plugin.getQuantumLogger().warning("No escrow found for order " + orderId);
            return 0.0;
        }
        
        // Récupérer le montant
        double amount = escrowCache.get(orderId);
        
        // Supprimer du cache
        escrowCache.remove(orderId);
        
        // Supprimer du fichier
        escrowConfig.set("escrow." + orderId.toString(), null);
        saveEscrow();
        
        plugin.getQuantumLogger().info("Withdrew " + amount + "€ from escrow for order " + orderId);
        return amount;
    }
    
    /**
     * Rembourse l'argent de l'escrow (ordre annulé/expiré)
     * 
     * @param orderId ID de l'ordre
     * @return Montant remboursé, ou 0.0 si l'ordre n'existe pas
     */
    public double refund(UUID orderId) {
        // Même logique que withdraw, mais pour un remboursement
        double amount = withdraw(orderId);
        
        if (amount > 0) {
            plugin.getQuantumLogger().info("Refunded " + amount + "€ from escrow for order " + orderId);
        }
        
        return amount;
    }
    
    /**
     * Vérifie si un dépôt escrow existe pour un ordre
     * 
     * @param orderId ID de l'ordre
     * @return true si un dépôt existe
     */
    public boolean hasDeposit(UUID orderId) {
        return orderId != null && escrowCache.containsKey(orderId);
    }
    
    /**
     * Récupère le montant en escrow pour un ordre
     * 
     * @param orderId ID de l'ordre
     * @return Montant en escrow, ou 0.0 si l'ordre n'existe pas
     */
    public double getAmount(UUID orderId) {
        if (orderId == null) {
            return 0.0;
        }
        
        return escrowCache.getOrDefault(orderId, 0.0);
    }
    
    /**
     * Récupère le montant total en escrow
     * 
     * @return Montant total
     */
    public double getTotalEscrow() {
        return escrowCache.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Récupère le nombre d'ordres en escrow
     * 
     * @return Nombre d'ordres
     */
    public int getEscrowCount() {
        return escrowCache.size();
    }
    
    /**
     * Nettoie tous les dépôts escrow (DANGER: à utiliser avec précaution)
     * 
     * @return Montant total nettoyé
     */
    public double clearAll() {
        double total = getTotalEscrow();
        
        escrowCache.clear();
        escrowConfig.set("escrow", null);
        saveEscrow();
        
        plugin.getQuantumLogger().warning("Cleared all escrow deposits! Total: " + total + "€");
        return total;
    }
    
    /**
     * Recharge le fichier escrow.yml
     */
    public void reload() {
        escrowCache.clear();
        loadEscrow();
        plugin.getQuantumLogger().success("✓ EscrowManager reloaded");
    }
}
