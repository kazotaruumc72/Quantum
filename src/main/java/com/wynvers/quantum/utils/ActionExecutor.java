package com.wynvers.quantum.utils;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuAction;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionExecutor {
    
    private final Quantum plugin;
    
    public ActionExecutor(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Execute all actions for player
     */
    public void executeActions(Player player, List<MenuAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        for (MenuAction action : actions) {
            executeAction(player, action);
        }
    }
    
    /**
     * Execute single action
     */
    public void executeAction(Player player, MenuAction action) {
        String value = parsePlaceholders(player, action.getValue());
        
        switch (action.getType()) {
            case MESSAGE:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case CONSOLE:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                break;
                
            case PLAYER:
                player.performCommand(value);
                break;
                
            case CLOSE:
                player.closeInventory();
                break;
                
            case MENU:
                openMenu(player, value);
                break;
                
            case SOUND:
                playSound(player, value);
                break;
                
            case BROADCAST:
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case ACTIONBAR:
                player.sendActionBar(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case TITLE:
                sendTitle(player, value);
                break;
                
            case EFFECT:
                applyEffect(player, value);
                break;
                
            // Actions de vente
            case SELL_INCREASE:
                handleSellIncrease(player, value);
                break;
                
            case SELL_DECREASE:
                handleSellDecrease(player, value);
                break;
                
            case SELL_SET_MAX:
                handleSellSetMax(player);
                break;
                
            case SELL_CONFIRM:
                handleSellConfirm(player);
                break;
        }
    }
    
    /**
     * Augmente la quantité à vendre
     */
    private void handleSellIncrease(Player player, String value) {
        SellSession session = plugin.getSellManager().getSession(player);
        if (session == null) {
            player.sendMessage(plugin.getMessagesManager().get("sell.no-session"));
            return;
        }
        
        // Parser la valeur (par défaut 1)
        int amount = 1;
        try {
            if (value != null && !value.isEmpty()) {
                amount = Integer.parseInt(value.trim());
            }
        } catch (NumberFormatException e) {
            amount = 1;
        }
        
        // Augmenter la quantité
        session.changeQuantity(amount);
        
        // Son de feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        
        // Rafraîchir le menu avec les nouveaux placeholders
        refreshSellMenu(player);
    }
    
    /**
     * Diminue la quantité à vendre
     */
    private void handleSellDecrease(Player player, String value) {
        SellSession session = plugin.getSellManager().getSession(player);
        if (session == null) {
            player.sendMessage(plugin.getMessagesManager().get("sell.no-session"));
            return;
        }
        
        // Parser la valeur (par défaut -1)
        int amount = -1;
        try {
            if (value != null && !value.isEmpty()) {
                amount = -Integer.parseInt(value.trim());
            }
        } catch (NumberFormatException e) {
            amount = -1;
        }
        
        // Diminuer la quantité
        session.changeQuantity(amount);
        
        // Son de feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
        
        // Rafraîchir le menu
        refreshSellMenu(player);
    }
    
    /**
     * Définit la quantité au maximum
     */
    private void handleSellSetMax(Player player) {
        SellSession session = plugin.getSellManager().getSession(player);
        if (session == null) {
            player.sendMessage(plugin.getMessagesManager().get("sell.no-session"));
            return;
        }
        
        // Définir au max
        session.setQuantity(session.getMaxQuantity());
        
        // Son de feedback
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        // Rafraîchir le menu
        refreshSellMenu(player);
    }
    
    /**
     * Confirme la vente
     */
    private void handleSellConfirm(Player player) {
        SellSession session = plugin.getSellManager().getSession(player);
        if (session == null) {
            player.sendMessage(plugin.getMessagesManager().get("sell.no-session"));
            return;
        }
        
        // Vérifier que Vault est activé
        if (!plugin.getVaultManager().isEnabled()) {
            player.sendMessage(plugin.getMessagesManager().get("sell.economy-disabled"));
            return;
        }
        
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        // Déterminer si c'est un item Nexo ou vanilla
        String nexoId = com.nexomc.nexo.api.NexoItems.idFromItem(session.getItemToSell());
        
        int availableInStorage;
        if (nexoId != null) {
            availableInStorage = storage.getNexoAmount(nexoId);
        } else {
            availableInStorage = storage.getAmount(session.getItemToSell().getType());
        }
        
        // Vérifier que le joueur a toujours assez d'items
        if (availableInStorage < session.getQuantity()) {
            player.sendMessage(plugin.getMessagesManager().get("sell.not-enough-stock"));
            player.closeInventory();
            plugin.getSellManager().removeSession(player);
            return;
        }
        
        // Retirer les items du storage
        if (nexoId != null) {
            storage.removeNexoItem(nexoId, session.getQuantity());
        } else {
            storage.removeItem(session.getItemToSell().getType(), session.getQuantity());
        }
        storage.save(plugin);
        
        // Donner l'argent au joueur
        double totalPrice = session.getTotalPrice();
        plugin.getVaultManager().deposit(player, totalPrice);
        
        // Obtenir le display name de l'item
        String itemDisplayName;
        if (session.getItemToSell().hasItemMeta() && session.getItemToSell().getItemMeta().hasDisplayName()) {
            itemDisplayName = session.getItemToSell().getItemMeta().getDisplayName();
        } else if (nexoId != null) {
            // Récupérer le display name depuis Nexo
            org.bukkit.inventory.ItemStack nexoItem = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId).build();
            if (nexoItem.hasItemMeta() && nexoItem.getItemMeta().hasDisplayName()) {
                itemDisplayName = nexoItem.getItemMeta().getDisplayName();
            } else {
                itemDisplayName = nexoId;
            }
        } else {
            itemDisplayName = session.getItemToSell().getType().name().toLowerCase().replace('_', ' ');
        }
        
        // Créer les placeholders pour les messages
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(session.getQuantity()));
        placeholders.put("item", itemDisplayName);
        placeholders.put("total_price", String.format("%.2f$", totalPrice));
        
        // Messages de succès avec le display name
        player.sendMessage(plugin.getMessagesManager().get("sell.success-title", placeholders));
        player.sendMessage(plugin.getMessagesManager().get("sell.success-sold", placeholders));
        player.sendMessage(plugin.getMessagesManager().get("sell.success-received", placeholders));
        
        // Son de succès
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Fermer le menu et supprimer la session
        player.closeInventory();
        plugin.getSellManager().removeSession(player);
    }
    
    /**
     * Rafraîchit le menu de vente avec les placeholders mis à jour
     */
    private void refreshSellMenu(Player player) {
        SellSession session = plugin.getSellManager().getSession(player);
        if (session == null) return;
        
        Menu sellMenu = plugin.getMenuManager().getMenu("sell");
        if (sellMenu != null) {
            // Rafraîchir avec les nouveaux placeholders
            sellMenu.refresh(player, plugin, session.getPlaceholders());
        }
    }
    
    /**
     * Parse placeholders in value
     */
    private String parsePlaceholders(Player player, String value) {
        if (plugin.getPlaceholderManager() != null) {
            return plugin.getPlaceholderManager().parse(player, value);
        }
        return value.replace("%player%", player.getName());
    }
    
    /**
     * Open another menu
     */
    private void openMenu(Player player, String menuId) {
        Menu menu = plugin.getMenuManager().getMenu(menuId);
        if (menu != null) {
            menu.open(player, plugin);
        } else {
            plugin.getQuantumLogger().warning("Menu not found: " + menuId);
        }
    }
    
    /**
     * Play sound
     */
    private void playSound(Player player, String soundDef) {
        try {
            String[] parts = soundDef.split(":");
            Sound sound = Sound.valueOf(parts[0].toUpperCase());
            
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Invalid sound: " + soundDef);
        }
    }
    
    /**
     * Send title
     * Format: title|subtitle or just title
     */
    private void sendTitle(Player player, String titleDef) {
        String[] parts = titleDef.split("\\|");
        String title = ChatColor.translateAlternateColorCodes('&', parts[0]);
        String subtitle = parts.length > 1 ? ChatColor.translateAlternateColorCodes('&', parts[1]) : "";
        
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    /**
     * Apply potion effect
     * Format: EFFECT:duration:amplifier
     */
    private void applyEffect(Player player, String effectDef) {
        try {
            String[] parts = effectDef.split(":");
            org.bukkit.potion.PotionEffectType effect = org.bukkit.potion.PotionEffectType.getByName(parts[0]);
            
            if (effect == null) return;
            
            int duration = parts.length > 1 ? Integer.parseInt(parts[1]) * 20 : 600;
            int amplifier = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(effect, duration, amplifier));
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Invalid effect: " + effectDef);
        }
    }
}
