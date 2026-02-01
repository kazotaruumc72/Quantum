package com.wynvers.quantum.sell;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.StorageManager;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère les sessions de vente des joueurs
 */
public class SellManager {
    
    private final Quantum plugin;
    private final Map<UUID, SellSession> sessions;
    
    public SellManager(Quantum plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }
    
    /**
     * Crée une session de vente pour un joueur
     */
    public SellSession createSession(Player player, ItemStack item, int maxQuantity, double pricePerUnit) {
        SellSession session = new SellSession(player.getUniqueId(), item, maxQuantity, pricePerUnit);
        sessions.put(player.getUniqueId(), session);
        return session;
    }
    
    /**
     * Récupère la session de vente d'un joueur
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
     * Modifie la quantité dans la session
     */
    public boolean changeQuantity(Player player, int amount) {
        SellSession session = getSession(player);
        if (session == null) return false;
        
        session.changeQuantity(amount);
        return true;
    }
    
    /**
     * Récupère le display name d'un item (avec support Nexo)
     */
    private String getItemDisplayName(ItemStack item) {
        // Vérifier le display name direct
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        
        // Vérifier si c'est un item Nexo
        String nexoId = NexoItems.idFromItem(item);
        if (nexoId != null) {
            try {
                ItemStack nexoItem = NexoItems.itemFromId(nexoId).build();
                if (nexoItem.hasItemMeta() && nexoItem.getItemMeta().hasDisplayName()) {
                    return nexoItem.getItemMeta().getDisplayName();
                }
                return nexoId;
            } catch (Exception e) {
                return nexoId;
            }
        }
        
        // Fallback sur le type vanilla
        return item.getType().name().toLowerCase().replace('_', ' ');
    }
    
    /**
     * Exécute la vente
     */
    public boolean executeSell(Player player) {
        SellSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cErreur: Aucune session de vente active.");
            return false;
        }
        
        // Vérifier que Vault est activé
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage("§cErreur: Le système économique n'est pas disponible.");
            return false;
        }
        
        // Récupérer le storage du joueur
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        ItemStack itemToSell = session.getItemToSell();
        int quantity = session.getQuantity();
        
        // Déterminer si c'est un item Nexo ou vanilla
        String nexoId = NexoItems.idFromItem(itemToSell);
        
        // Vérifier que le joueur a toujours les items
        boolean hasEnough;
        if (nexoId != null) {
            hasEnough = storage.hasNexoItem(nexoId, quantity);
        } else {
            hasEnough = storage.hasItem(itemToSell.getType(), quantity);
        }
        
        if (!hasEnough) {
            player.sendMessage("§cErreur: Vous n'avez plus assez de cet item en stock.");
            removeSession(player);
            return false;
        }
        
        // Retirer les items du storage
        if (nexoId != null) {
            storage.removeNexoItem(nexoId, quantity);
        } else {
            storage.removeItem(itemToSell.getType(), quantity);
        }
        
        // Sauvegarder le storage
        storage.save(plugin);
        
        // Ajouter l'argent au joueur
        double totalPrice = session.getTotalPrice();
        plugin.getVaultManager().deposit(player, totalPrice);
        
        // Récupérer le vrai display name
        String itemDisplayName = getItemDisplayName(itemToSell);
        
        // Créer les placeholders pour les messages
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(quantity));
        placeholders.put("item", itemDisplayName);
        placeholders.put("total_price", String.format("%.2f$", totalPrice));
        
        // Messages de succès avec placeholders
        player.sendMessage(plugin.getMessagesManager().get("sell.success-title", placeholders));
        player.sendMessage(plugin.getMessagesManager().get("sell.success-sold", placeholders));
        player.sendMessage(plugin.getMessagesManager().get("sell.success-received", placeholders));
        
        // Supprimer la session
        removeSession(player);
        
        return true;
    }
    
    /**
     * Nettoie toutes les sessions
     */
    public void clearAllSessions() {
        sessions.clear();
    }
}
