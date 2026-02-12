package com.wynvers.quantum.managers;

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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Gestionnaire des messages système provenant de messages.yml
 * Support complet de MiniMessage et Legacy colors
 * 
 * @author Kazotaruu_
 * @version 2.0
 */
public class MessageManager {
    
    private final Quantum plugin;
    private FileConfiguration messagesConfig;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    
    // Cache des messages pour optimisation
    private final Map<String, String> messageCache;
    
    public MessageManager(Quantum plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        this.messageCache = new HashMap<>();
        
        loadMessages();
    }
    
    /**
     * Charge ou recharge le fichier messages.yml
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // Créer le fichier s'il n'existe pas
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();
        
        plugin.getLogger().log(Level.INFO, "Messages système chargés depuis messages.yml");
    }
    
    /**
     * Récupère un message brut sans traitement
     * 
     * @param path Chemin du message (ex: "system.prefix")
     * @return Message brut ou path si non trouvé
     */
    public String getRawMessage(String path) {
        // Vérifier le cache
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }
        
        String message = messagesConfig.getString(path);
        
        if (message == null) {
            plugin.getLogger().warning("Message manquant: " + path);
            return path;
        }
        
        // Mettre en cache
        messageCache.put(path, message);
        return message;
    }
    
    /**
     * Récupère un message avec remplacement de placeholders
     * 
     * @param path Chemin du message
     * @param placeholders Map des placeholders (clé sans %)
     * @return Message formatté
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getRawMessage(path);
        
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        
        return message;
    }
    
    /**
     * Récupère un message sans placeholders
     */
    public String getMessage(String path) {
        return getMessage(path, null);
    }
    
    /**
     * Convertit un message en Component (Adventure API)
     * Support MiniMessage et Legacy
     * 
     * @param message Message à convertir
     * @return Component Adventure
     */
    public Component toComponent(String message) {
        // Détecter si c'est du MiniMessage (contient < et >)
        if (message.contains("<") && message.contains(">")) {
            try {
                return miniMessage.deserialize(message);
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur MiniMessage: " + message);
                return legacySerializer.deserialize(message);
            }
        }
        
        // Sinon utiliser Legacy
        return legacySerializer.deserialize(message);
    }
    
    /**
     * Envoie un message à un joueur
     * 
     * @param player Joueur cible
     * @param path Chemin du message
     * @param placeholders Placeholders
     */
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        Component component = toComponent(message);
        player.sendMessage(component);
    }
    
    /**
     * Envoie un message sans placeholders
     */
    public void sendMessage(Player player, String path) {
        sendMessage(player, path, null);
    }
    
    /**
     * Envoie un message avec préfixe
     */
    public void sendPrefixedMessage(Player player, String path, Map<String, String> placeholders) {
        String prefix = getMessage("system.prefix");
        String message = getMessage(path, placeholders);
        Component component = toComponent(prefix + message);
        player.sendMessage(component);
    }
    
    /**
     * Envoie un message avec préfixe sans placeholders
     */
    public void sendPrefixedMessage(Player player, String path) {
        sendPrefixedMessage(player, path, null);
    }
    
    /**
     * Envoie un message avec préfixe (pour CommandSender)
     */
    public void sendPrefixedMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        String prefix = getMessage("system.prefix");
        String message = getMessage(path, placeholders);
        Component component = toComponent(prefix + message);
        sender.sendMessage(component);
    }
    
    /**
     * Envoie un message avec préfixe sans placeholders (pour CommandSender)
     */
    public void sendPrefixedMessage(CommandSender sender, String path) {
        sendPrefixedMessage(sender, path, null);
    }
    
    /**
     * Récupère une liste de messages
     */
    public List<String> getMessageList(String path) {
        return messagesConfig.getStringList(path);
    }
    
    /**
     * Convertit une liste de messages en Components
     */
    public List<Component> getComponentList(String path) {
        return getMessageList(path).stream()
            .map(this::toComponent)
            .toList();
    }
    
    // ========================================
    // MÉTHODES RAPIDES POUR MESSAGES FRÉQUENTS
    // ========================================
    
    /**
     * Message: Pas de permission
     */
    public void sendNoPermission(Player player) {
        sendPrefixedMessage(player, "system.no-permission");
    }
    
    /**
     * Message: Commande joueur seulement
     */
    public void sendPlayerOnly(Player player) {
        sendPrefixedMessage(player, "system.player-only");
    }
    
    /**
     * Message: Reload succès
     */
    public void sendReloadSuccess(Player player) {
        sendPrefixedMessage(player, "system.reload-success");
    }
    
    /**
     * Message: Reload erreur
     */
    public void sendReloadError(Player player) {
        sendPrefixedMessage(player, "system.reload-error");
    }
    
    /**
     * Message: Vente réussie
     */
    public void sendSellSuccess(Player player, String item, int quantity, double price) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("price", String.format("%.2f", price));
        
        sendPrefixedMessage(player, "sell.success", placeholders);
    }
    
    /**
     * Message: Stock insuffisant
     */
    public void sendInsufficientStock(Player player, int current) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("current", String.valueOf(current));
        
        sendPrefixedMessage(player, "sell.insufficient-stock", placeholders);
    }
    
    /**
     * Message: Offre créée
     */
    public void sendOrderCreated(Player player, String item, int quantity, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        
        sendPrefixedMessage(player, "research.order-created", placeholders);
    }
    
    /**
     * Message: Ordre accepté (vendeur)
     */
    public void sendOrderAccepted(Player seller, String item, int quantity, String buyer, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("buyer", buyer);
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        
        sendPrefixedMessage(seller, "orders.seller.order-accepted", placeholders);
    }
    
    /**
     * Message: Ordre complété (acheteur)
     */
    public void sendOrderCompleted(Player buyer, String item, int quantity, String seller, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("seller", seller);
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        
        sendPrefixedMessage(buyer, "orders.buyer.order-completed", placeholders);
    }
    
    /**
     * Recharge les messages
     */
    public void reload() {
        loadMessages();
    }
}
