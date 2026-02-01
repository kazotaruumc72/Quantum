package com.wynvers.quantum.sell;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * G\u00e8re les sessions de vente des joueurs
 */
public class SellManager {
    
    private final Quantum plugin;
    private final Map<UUID, SellSession> sessions;
    
    public SellManager(Quantum plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }
    
    /**
     * Cr\u00e9e une session de vente pour un joueur
     */
    public SellSession createSession(Player player, ItemStack item, int maxQuantity, double pricePerUnit) {
        SellSession session = new SellSession(player.getUniqueId(), item, maxQuantity, pricePerUnit);
        sessions.put(player.getUniqueId(), session);
        return session;
    }
    
    /**
     * R\u00e9cup\u00e8re la session de vente d'un joueur
     */
    public SellSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }
    
    /**
     * Supprime la session de vente d'un joueur
     */
    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
    
    /**
     * Modifie la quantit\u00e9 dans la session
     */
    public boolean changeQuantity(Player player, int amount) {
        SellSession session = getSession(player);
        if (session == null) return false;
        
        session.changeQuantity(amount);
        return true;
    }
    
    /**
     * Ex\u00e9cute la vente
     */
    public boolean executeSell(Player player) {
        SellSession session = getSession(player);
        if (session == null) {
            player.sendMessage("\u00a7cErreur: Aucune session de vente active.");
            return false;
        }
        
        // V\u00e9rifier que Vault est activ\u00e9
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage("\u00a7cErreur: Le syst\u00e8me \u00e9conomique n'est pas disponible.");
            return false;
        }
        
        // V\u00e9rifier que le joueur a toujours les items
        StorageManager storageManager = plugin.getStorageManager();
        ItemStack itemToSell = session.getItemToSell();
        int quantity = session.getQuantity();
        
        if (!storageManager.hasItem(player, itemToSell, quantity)) {
            player.sendMessage("\u00a7cErreur: Vous n'avez plus assez de cet item en stock.");
            removeSession(player);
            return false;
        }
        
        // Retirer les items du storage
        if (!storageManager.removeItem(player, itemToSell, quantity)) {
            player.sendMessage("\u00a7cErreur: Impossible de retirer les items du storage.");
            return false;
        }
        
        // Ajouter l'argent au joueur
        double totalPrice = session.getTotalPrice();
        plugin.getVaultManager().deposit(player, totalPrice);
        
        // Message de succ\u00e8s
        String formattedPrice = plugin.getVaultManager().format(totalPrice);
        player.sendMessage("\u00a7a\u2713 Vente r\u00e9ussie !");
        player.sendMessage("\u00a7fVous avez vendu \u00a7e" + quantity + "x \u00a7f" + getItemName(itemToSell));
        player.sendMessage("\u00a7fVous avez re\u00e7u \u00a76" + formattedPrice);
        
        // Supprimer la session
        removeSession(player);
        
        return true;
    }
    
    /**
     * R\u00e9cup\u00e8re le nom d'un item
     */
    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().toLowerCase().replace('_', ' ');
    }
    
    /**
     * Nettoie toutes les sessions
     */
    public void clearAllSessions() {
        sessions.clear();
    }
}
