package fr.robie.quantum.utils;

import fr.robie.quantum.Quantum;
import fr.robie.quantum.menu.Menu;
import fr.robie.quantum.menu.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

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
            // TODO: Open menu GUI
            plugin.getQuantumLogger().debug("Opening menu: " + menuId + " for " + player.getName());
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
