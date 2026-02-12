package com.wynvers.quantum.chat;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.MessageManager;
import com.wynvers.quantum.managers.PlaceholderManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire du système de chat formaté
 * Gère les formats de chat personnalisés avec permissions et placeholders
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChatManager {
    
    private final Quantum plugin;
    private final MessageManager messageManager;
    private final PlaceholderManager placeholderManager;
    
    public ChatManager(Quantum plugin, MessageManager messageManager, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.placeholderManager = placeholderManager;
    }
    
    /**
     * Formate un message de chat selon les permissions du joueur
     * 
     * @param player Le joueur qui envoie le message
     * @param message Le message à formater
     * @return Le composant formaté
     */
    public Component formatChatMessage(Player player, String message) {
        // Déterminer le format à utiliser
        String format = getChatFormat(player);
        
        // Créer les placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("display_name", player.getDisplayName());
        placeholders.put("message", processMessage(player, message));
        placeholders.put("rank", getPlayerRank(player));
        
        // Ajouter les placeholders du PlaceholderManager si disponible
        if (placeholderManager != null) {
            placeholders.put("level", placeholderManager.getPlaceholder(player.getUniqueId(), "level"));
        } else {
            placeholders.put("level", "1");
        }
        
        // Remplacer les placeholders dans le format
        String formattedMessage = format;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            formattedMessage = formattedMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        // Convertir en Component
        return messageManager.toComponent(formattedMessage);
    }
    
    /**
     * Obtient le format de chat approprié pour un joueur
     * 
     * @param player Le joueur
     * @return Le format de chat
     */
    private String getChatFormat(Player player) {
        // Vérifier les formats personnalisés par permission (dans l'ordre de priorité)
        String[] formatPriority = {"admin", "mod", "vip", "player"};
        
        for (String formatName : formatPriority) {
            if (player.hasPermission("quantum.chat.format." + formatName)) {
                String format = messageManager.getRawMessage("chat.formats." + formatName);
                if (format != null && !format.equals("chat.formats." + formatName)) {
                    return format;
                }
            }
        }
        
        // Format par défaut
        String defaultFormat = messageManager.getRawMessage("chat.format");
        if (defaultFormat == null || defaultFormat.equals("chat.format")) {
            // Fallback si le format n'existe pas dans messages.yml
            return "<gray>[</gray><aqua>%rank%</aqua><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> <gray>%message%</gray>";
        }
        
        return defaultFormat;
    }
    
    /**
     * Obtient le rang d'un joueur
     * 
     * @param player Le joueur
     * @return Le rang
     */
    private String getPlayerRank(Player player) {
        // Vérifier les permissions pour déterminer le rang
        if (player.hasPermission("quantum.chat.format.admin")) {
            return "Admin";
        } else if (player.hasPermission("quantum.chat.format.mod")) {
            return "Modo";
        } else if (player.hasPermission("quantum.chat.format.vip")) {
            return "VIP";
        } else {
            // Rang par défaut
            String defaultRank = messageManager.getRawMessage("chat.default-rank");
            if (defaultRank == null || defaultRank.equals("chat.default-rank")) {
                return "Joueur";
            }
            return defaultRank;
        }
    }
    
    /**
     * Traite le message du joueur (gère les couleurs et formats)
     * 
     * @param player Le joueur
     * @param message Le message brut
     * @return Le message traité
     */
    private String processMessage(Player player, String message) {
        // Si le joueur a la permission d'utiliser des couleurs
        if (player.hasPermission("quantum.chat.color")) {
            // Le message peut contenir des codes couleur MiniMessage ou Legacy
            return message;
        }
        
        // Sinon, échapper les codes couleur (remplacer < par &lt; pour MiniMessage)
        return message.replace("<", "&lt;").replace("&", "&amp;");
    }
    
    /**
     * Vérifie si un joueur peut utiliser des couleurs dans le chat
     * 
     * @param player Le joueur
     * @return true si le joueur peut utiliser des couleurs
     */
    public boolean canUseColors(Player player) {
        return player.hasPermission("quantum.chat.color");
    }
    
    /**
     * Recharge la configuration du chat
     */
    public void reload() {
        messageManager.loadMessages();
        plugin.getLogger().info("Configuration du chat rechargée");
    }
}
