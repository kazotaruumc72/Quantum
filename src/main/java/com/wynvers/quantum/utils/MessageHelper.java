package com.wynvers.quantum.utils;

import com.wynvers.quantum.Quantum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé des messages depuis messages.yml
 * Support MiniMessage et placeholders personnalisés
 */
public class MessageHelper {
    
    private final Quantum plugin;
    private FileConfiguration messagesConfig;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    
    public MessageHelper(Quantum plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacySection();
        loadMessages();
    }
    
    /**
     * Charge ou recharge messages.yml
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Récupère un message brut depuis messages.yml
     * @param path Chemin YAML (ex: "orders.seller-success-header")
     * @return Le message brut ou une erreur si introuvable
     */
    public String getRawMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message introuvable: " + path);
            return "<red>[Message manquant: " + path + "]";
        }
        return message;
    }
    
    /**
     * Récupère un message formaté avec placeholders
     * @param path Chemin YAML
     * @param placeholders Map de placeholders {key -> value}
     * @return Message formaté en legacy color codes
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String raw = getRawMessage(path);
        
        // Remplacer les placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        // Parser MiniMessage -> Legacy
        try {
            Component component = miniMessage.deserialize(raw);
            return legacySerializer.serialize(component);
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du parsing MiniMessage pour: " + path);
            return raw; // Fallback
        }
    }
    
    /**
     * Récupère un message sans placeholders
     */
    public String getMessage(String path) {
        return getMessage(path, null);
    }
    
    /**
     * Récupère le prefix configuré
     */
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    /**
     * Envoie un message à un joueur/sender AVEC prefix
     * @param sender Le destinataire
     * @param path Chemin YAML du message
     * @param placeholders Placeholders optionnels
     */
    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        sender.sendMessage(getPrefix() + message);
    }
    
    /**
     * Envoie un message AVEC prefix sans placeholders
     */
    public void send(CommandSender sender, String path) {
        send(sender, path, null);
    }
    
    /**
     * Envoie un message SANS prefix
     * @param sender Le destinataire
     * @param path Chemin YAML du message
     * @param placeholders Placeholders optionnels
     */
    public void sendRaw(CommandSender sender, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        sender.sendMessage(message);
    }
    
    /**
     * Envoie un message SANS prefix et sans placeholders
     */
    public void sendRaw(CommandSender sender, String path) {
        sendRaw(sender, path, null);
    }
    
    /**
     * Builder pour messages avec plusieurs lignes
     */
    public MessageBuilder builder(CommandSender sender) {
        return new MessageBuilder(sender);
    }
    
    /**
     * Classe interne pour construire des messages multi-lignes
     */
    public class MessageBuilder {
        private final CommandSender sender;
        private boolean usePrefix = true;
        private final StringBuilder messageBuilder = new StringBuilder();
        
        public MessageBuilder(CommandSender sender) {
            this.sender = sender;
        }
        
        /**
         * Active ou désactive le prefix pour TOUTES les lignes
         */
        public MessageBuilder withPrefix(boolean usePrefix) {
            this.usePrefix = usePrefix;
            return this;
        }
        
        /**
         * Ajoute une ligne depuis messages.yml
         */
        public MessageBuilder line(String path, Map<String, String> placeholders) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append("\n");
            }
            if (usePrefix) {
                messageBuilder.append(getPrefix());
            }
            messageBuilder.append(getMessage(path, placeholders));
            return this;
        }
        
        /**
         * Ajoute une ligne sans placeholders
         */
        public MessageBuilder line(String path) {
            return line(path, null);
        }
        
        /**
         * Ajoute une ligne vide
         */
        public MessageBuilder blank() {
            if (messageBuilder.length() > 0) {
                messageBuilder.append("\n");
            }
            return this;
        }
        
        /**
         * Envoie le message construit
         */
        public void send() {
            sender.sendMessage(messageBuilder.toString());
        }
    }
}
