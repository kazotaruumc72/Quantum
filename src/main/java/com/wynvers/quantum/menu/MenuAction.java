package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MenuAction {
    
    private final ActionType type;
    private final String value;
    private final java.util.Map<String, String> settings;
    
    public MenuAction(ActionType type, String value, java.util.Map<String, String> settings) {
        this.type = type;
        this.value = value;
        this.settings = settings != null ? settings : new java.util.HashMap<>();
    }

    // Constructeur de compatibilité pour les actions sans settings
    public MenuAction(ActionType type, String value) {
        this(type, value, null);
    }
    
    public ActionType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }

    public java.util.Map<String, String> getSettings() {
        return settings;
    }
    
    public String getSetting(String key) {
        return settings.get(key);
    }
    
    /**
     * Execute this action for a player
     */
    public void execute(Player player, Quantum plugin) {
        String processedValue = processPlaceholders(value, player);
        
        switch (type) {
            case MESSAGE:
                player.sendMessage(processedValue);
                break;
                
            case CONSOLE:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedValue);
                break;
                
            case PLAYER:
                player.performCommand(processedValue);
                break;
                
            case CLOSE:
                player.closeInventory();
                break;
                
            case MENU:
                Menu menu = plugin.getMenuManager().getMenu(processedValue);
                if (menu != null) {
                    menu.open(player, plugin);
                }
                break;
                
            case SOUND:
                playSound(player, processedValue);
                break;
                
            case BROADCAST:
                Bukkit.broadcastMessage(processedValue);
                break;
                
            case ACTIONBAR:
                player.sendActionBar(processedValue);
                break;
                
            case TITLE:
                sendTitle(player, processedValue);
                break;
                
            case EFFECT:
                // TODO: Implement potion effects
                break;
                
            case REFRESH:
                // Rafraîchi le menu sans le fermer (regénère tous les items)
                Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
                if (activeMenu != null) {
                    activeMenu.refresh(player, plugin);
                }
                break;
                
            // Actions de vente
            case SELL_INCREASE:
            case SELL_DECREASE:
            case SELL_SET_MAX:
            case SELL_CONFIRM:
                // Ces actions sont gérées dans ActionExecutor pour accéder aux managers
                plugin.getActionExecutor().executeAction(player, this);
                break;
                
            // Actions de création d'offres
            case QUANTUM_ADJUST_QUANTITY:
            case QUANTUM_SET_QUANTITY_MAX:
            case QUANTUM_VALIDATE_QUANTITY:
            case QUANTUM_ADJUST_PRICE:
            case QUANTUM_SET_PRICE_MAX:
            case QUANTUM_FINALIZE_ORDER:
            case QUANTUM_CANCEL_ORDER:
                // Ces actions sont gérées dans ActionExecutor
                plugin.getActionExecutor().executeAction(player, this);
                break;
        }
    }
    
    private String processPlaceholders(String text, Player player) {
        if (text == null) return "";
        
        // Replace basic placeholders
        text = text.replace("%player%", player.getName());
        text = text.replace("%player_name%", player.getName());
        text = text.replace("%player_uuid%", player.getUniqueId().toString());
        
        // TODO: Integrate PlaceholderAPI when available
        
        return text;
    }
    
    private void playSound(Player player, String soundStr) {
        try {
            String[] parts = soundStr.split(":");
            String soundInput = parts[0];
            
            // Use Registry API instead of deprecated valueOf()
            // Support both enum-style (ENTITY_PLAYER_LEVELUP) and namespaced format (entity.player.levelup)
            String soundName = soundInput.toLowerCase();
            if (soundInput.contains("_")) {
                // Convert from enum-style to namespaced: ENTITY_PLAYER_LEVELUP -> entity.player.levelup
                soundName = soundName.replace("_", ".");
            }
            
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName));
            
            if (sound == null) {
                // Log warning for invalid sounds to aid debugging
                Bukkit.getLogger().warning("[Quantum] Invalid sound: " + soundStr);
                return;
            }
            
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            // Invalid sound format
        }
    }
    
    private void sendTitle(Player player, String titleStr) {
        String[] parts = titleStr.split("\\|");
        String title = parts.length > 0 ? parts[0] : "";
        String subtitle = parts.length > 1 ? parts[1] : "";
        int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
        int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 70;
        int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 20;
        
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * Parse action from string format: [type] value
     * Examples:
     * - [message] Hello!
     * - [console] give %player% diamond 1
     * - [close]
     * - [quantum_adjust_quantity] +5
     * - [quantum_adjust_price] -20
     */
    public static MenuAction parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        input = input.trim();
        
        // Extract type from [brackets]
        if (input.startsWith("[")) {
            int endBracket = input.indexOf("]");
            if (endBracket > 0) {
                String typeStr = input.substring(1, endBracket).toUpperCase();
                String value = input.substring(endBracket + 1).trim();
                
                try {
                    ActionType type = ActionType.valueOf(typeStr);
                    return new MenuAction(type, value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    public enum ActionType {
        MESSAGE,         // Send message to player
        CONSOLE,         // Execute console command
        PLAYER,          // Execute command as player
        CLOSE,           // Close current menu
        MENU,            // Open another menu
        SOUND,           // Play sound
        BROADCAST,       // Broadcast message
        ACTIONBAR,       // Send action bar message
        TITLE,           // Send title
        EFFECT,          // Give potion effect
        REFRESH,         // Refresh current menu (regenerate items)
        
        // Actions de vente
        SELL_INCREASE,   // Augmenter la quantité à vendre
        SELL_DECREASE,   // Diminuer la quantité à vendre
        SELL_SET_MAX,    // Définir quantité max
        SELL_CONFIRM,    // Confirmer la vente
        
        // Actions de création d'offres
        QUANTUM_ADJUST_QUANTITY,   // Ajuster la quantité (+5, +50, -5, -50)
        QUANTUM_SET_QUANTITY_MAX,  // Définir quantité au maximum
        QUANTUM_VALIDATE_QUANTITY, // Valider la quantité et passer au prix
        QUANTUM_ADJUST_PRICE,      // Ajuster le prix (+5%, +20%, -5%, -20%)
        QUANTUM_SET_PRICE_MAX,     // Définir prix au maximum
        QUANTUM_FINALIZE_ORDER,    // Finaliser et créer l'offre
        QUANTUM_CANCEL_ORDER       // Annuler la création d'offre
    }
}
