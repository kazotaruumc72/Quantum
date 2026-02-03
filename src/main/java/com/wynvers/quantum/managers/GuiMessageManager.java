package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Gestionnaire des messages GUI provenant de messages_gui.yml
 * Support complet de MiniMessage et Legacy colors
 * 
 * @author Kazotaruu_
 * @version 2.0
 */
public class GuiMessageManager {
    
    private final Quantum plugin;
    private FileConfiguration guiMessagesConfig;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    
    // Cache des messages pour optimisation
    private final Map<String, String> messageCache;
    private final Map<String, List<String>> listCache;
    
    public GuiMessageManager(Quantum plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        this.messageCache = new HashMap<>();
        this.listCache = new HashMap<>();
        
        loadGuiMessages();
    }
    
    /**
     * Charge ou recharge le fichier messages_gui.yml
     */
    public void loadGuiMessages() {
        File guiMessagesFile = new File(plugin.getDataFolder(), "messages_gui.yml");
        
        // Créer le fichier s'il n'existe pas
        if (!guiMessagesFile.exists()) {
            plugin.saveResource("messages_gui.yml", false);
        }
        
        guiMessagesConfig = YamlConfiguration.loadConfiguration(guiMessagesFile);
        messageCache.clear();
        listCache.clear();
        
        plugin.getLogger().log(Level.INFO, "Messages GUI chargés depuis messages_gui.yml");
    }
    
    /**
     * Récupère un message brut sans traitement
     * 
     * @param path Chemin du message (ex: "titles.storage")
     * @return Message brut ou path si non trouvé
     */
    public String getRawMessage(String path) {
        // Vérifier le cache
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }
        
        String message = guiMessagesConfig.getString(path);
        
        if (message == null) {
            plugin.getLogger().warning("Message GUI manquant: " + path);
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
     * Récupère une liste de messages bruts
     */
    public List<String> getRawMessageList(String path) {
        // Vérifier le cache
        if (listCache.containsKey(path)) {
            return new ArrayList<>(listCache.get(path));
        }
        
        List<String> messages = guiMessagesConfig.getStringList(path);
        
        if (messages.isEmpty()) {
            plugin.getLogger().warning("Liste de messages GUI manquante: " + path);
            return new ArrayList<>();
        }
        
        // Mettre en cache
        listCache.put(path, new ArrayList<>(messages));
        return messages;
    }
    
    /**
     * Récupère une liste de messages avec placeholders
     */
    public List<String> getMessageList(String path, Map<String, String> placeholders) {
        List<String> messages = getRawMessageList(path);
        
        if (placeholders == null || placeholders.isEmpty()) {
            return messages;
        }
        
        List<String> result = new ArrayList<>();
        for (String message : messages) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            result.add(message);
        }
        
        return result;
    }
    
    /**
     * Récupère une liste sans placeholders
     */
    public List<String> getMessageList(String path) {
        return getMessageList(path, null);
    }
    
    /**
     * Convertit un message en Component (Adventure API)
     * Support MiniMessage et Legacy
     */
    public Component toComponent(String message) {
        // Détecter si c'est du MiniMessage (contient < et >)
        if (message.contains("<") && message.contains(">")) {
            try {
                return miniMessage.deserialize(message);
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur MiniMessage GUI: " + message);
                return legacySerializer.deserialize(message);
            }
        }
        
        // Sinon utiliser Legacy
        return legacySerializer.deserialize(message);
    }
    
    /**
     * Convertit une liste de messages en Components
     */
    public List<Component> toComponentList(List<String> messages) {
        return messages.stream()
            .map(this::toComponent)
            .toList();
    }
    
    // ========================================
    // MÉTHODES RAPIDES POUR TITRES DE MENUS
    // ========================================
    
    /**
     * Récupère le titre du menu Storage
     */
    public String getStorageTitle(String mode) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode_simple", mode);
        return getMessage("titles.storage", placeholders);
    }
    
    /**
     * Récupère le titre du menu Sell
     */
    public String getSellTitle(String itemName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item_name", itemName);
        return getMessage("titles.sell", placeholders);
    }
    
    /**
     * Récupère le titre du menu Order Quantity
     */
    public String getOrderQuantityTitle() {
        return getMessage("titles.order-quantity");
    }
    
    /**
     * Récupère le titre du menu Order Price
     */
    public String getOrderPriceTitle() {
        return getMessage("titles.order-price");
    }
    
    /**
     * Récupère le titre du menu Order Confirm
     */
    public String getOrderConfirmTitle() {
        return getMessage("titles.order-confirm");
    }
    
    /**
     * Récupère le titre du menu Orders Categories
     */
    public String getOrdersCategoriesTitle() {
        return getMessage("titles.orders-categories");
    }
    
    /**
     * Récupère le titre d'un menu de catégorie spécifique
     */
    public String getOrdersCategoryTitle(String category) {
        return getMessage("titles.orders-" + category.toLowerCase());
    }
    
    // ========================================
    // MÉTHODES POUR LORE DE MENUS
    // ========================================
    
    /**
     * Récupère la lore du bouton Mode Storage
     */
    public List<String> getModeStorageLore(String currentMode) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode_simple", currentMode);
        return getMessageList("storage.mode-storage.lore", placeholders);
    }
    
    /**
     * Récupère la lore du bouton Mode Recherche
     */
    public List<String> getModeRechercheLore(String currentMode) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode_simple", currentMode);
        return getMessageList("storage.mode-recherche.lore", placeholders);
    }
    
    /**
     * Récupère la lore du bouton Mode Sell
     */
    public List<String> getModeSellLore(String currentMode) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode_simple", currentMode);
        return getMessageList("storage.mode-sell.lore", placeholders);
    }
    
    /**
     * Récupère la lore d'un slot de storage
     */
    public List<String> getStorageSlotLore(int quantity, double price, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("price", String.format("%.2f", price));
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        return getMessageList("storage.storage-slot-lore", placeholders);
    }
    
    /**
     * Récupère la lore de l'item display du menu Sell
     */
    public List<String> getSellItemDisplayLore(String itemName, int maxQuantity, int quantity, double pricePerUnit, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item_name", itemName);
        placeholders.put("max_quantity", String.valueOf(maxQuantity));
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("price_per_unit", String.format("%.2f", pricePerUnit));
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        return getMessageList("sell.item-display.lore", placeholders);
    }
    
    /**
     * Récupère la lore d'un item d'ordre dans les sous-menus
     */
    public List<String> getOrderItemLore(String orderer, int quantity, double price, double totalPrice) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("orderer", orderer);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("price", String.format("%.2f", price));
        placeholders.put("total_price", String.format("%.2f", totalPrice));
        return getMessageList("orders-submenu.order-item-lore", placeholders);
    }
    
    /**
     * Récupère le nom d'un bouton
     */
    public String getButtonName(String section, String button) {
        return getMessage(section + "." + button + ".name");
    }
    
    /**
     * Récupère le nom d'un bouton avec placeholders
     */
    public String getButtonName(String section, String button, Map<String, String> placeholders) {
        return getMessage(section + "." + button + ".name", placeholders);
    }
    
    /**
     * Récupère la lore d'un bouton
     */
    public List<String> getButtonLore(String section, String button) {
        return getMessageList(section + "." + button + ".lore");
    }
    
    /**
     * Récupère la lore d'un bouton avec placeholders
     */
    public List<String> getButtonLore(String section, String button, Map<String, String> placeholders) {
        return getMessageList(section + "." + button + ".lore", placeholders);
    }
    
    /**
     * Recharge les messages GUI
     */
    public void reload() {
        loadGuiMessages();
    }
}
