package com.wynvers.quantum.examples;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.MessageManager;
import com.wynvers.quantum.managers.GuiMessageManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exemples concrets d'utilisation des MessageManager et GuiMessageManager
 * 
 * Ce fichier montre comment utiliser les systèmes de messages de manière optimale.
 * 
 * @author Kazotaruu_
 * @version 2.0
 */
public class MessageSystemExample {
    
    private final Quantum plugin;
    private final MessageManager messageManager;
    private final GuiMessageManager guiMessageManager;
    
    public MessageSystemExample(Quantum plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.guiMessageManager = plugin.getGuiMessageManager();
    }
    
    // ========================================
    // EXEMPLES MESSAGE MANAGER (SYSTÈME)
    // ========================================
    
    /**
     * Exemple 1: Message simple sans placeholder
     */
    public void exempleMessageSimple(Player player) {
        // Méthode 1: Via le manager directement
        messageManager.sendPrefixedMessage(player, "system.no-permission");
        
        // Méthode 2: Via les méthodes helper rapides
        messageManager.sendNoPermission(player);
    }
    
    /**
     * Exemple 2: Message avec placeholders
     */
    public void exempleMessageAvecPlaceholders(Player player, String itemName, int quantity, double price) {
        // Créer la map de placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemName);
        placeholders.put("quantity", String.valueOf(quantity));
        placeholders.put("price", String.format("%.2f", price));
        
        // Envoyer le message
        messageManager.sendPrefixedMessage(player, "sell.success", placeholders);
    }
    
    /**
     * Exemple 3: Utilisation des méthodes helper optimisées
     */
    public void exempleMethodesHelper(Player player) {
        // Vente réussie
        messageManager.sendSellSuccess(player, "Pomme", 64, 128.50);
        
        // Stock insuffisant
        messageManager.sendInsufficientStock(player, 10);
        
        // Ordre créé
        messageManager.sendOrderCreated(player, "Diamant", 64, 6400.00);
        
        // Ordre accepté (vendeur)
        messageManager.sendOrderAccepted(player, "Diamant", 64, "PlayerName", 6400.00);
        
        // Ordre complété (acheteur)
        messageManager.sendOrderCompleted(player, "Diamant", 64, "SellerName", 6400.00);
    }
    
    /**
     * Exemple 4: Récupérer un message brut pour le logger
     */
    public void exempleMessageBrut() {
        String message = messageManager.getRawMessage("system.prefix");
        plugin.getQuantumLogger().info(message);
    }
    
    /**
     * Exemple 5: Convertir un message en Component (Adventure API)
     */
    public void exempleComponent(Player player) {
        // Support MiniMessage
        String miniMessage = "<gradient:#FF5555:#FFFF55>Message Coloré!</gradient>";
        player.sendMessage(messageManager.toComponent(miniMessage));
        
        // Support Legacy
        String legacyMessage = "&a&lMessage Legacy&r &7(gris)";
        player.sendMessage(messageManager.toComponent(legacyMessage));
    }
    
    // ========================================
    // EXEMPLES GUI MESSAGE MANAGER
    // ========================================
    
    /**
     * Exemple 6: Récupérer un titre de menu
     */
    public String exempleTitreMenu() {
        // Titre Storage avec mode
        String storageTitle = guiMessageManager.getStorageTitle("Vente");
        
        // Titre Sell avec nom d'item
        String sellTitle = guiMessageManager.getSellTitle("Pomme");
        
        // Titre Order Quantity
        String orderQuantityTitle = guiMessageManager.getOrderQuantityTitle();
        
        // Titre d'une catégorie spécifique
        String categoryTitle = guiMessageManager.getOrdersCategoryTitle("Cultures");
        
        return storageTitle;
    }
    
    /**
     * Exemple 7: Récupérer une lore de bouton
     */
    public List<String> exempleLoreSimple() {
        // Lore du bouton Mode Storage avec placeholder
        return guiMessageManager.getModeStorageLore("Vente");
    }
    
    /**
     * Exemple 8: Récupérer une lore avec plusieurs placeholders
     */
    public List<String> exempleLoreComplexe() {
        // Lore d'un slot de storage avec prix
        int quantity = 64;
        double price = 2.50;
        double totalPrice = quantity * price;
        
        return guiMessageManager.getStorageSlotLore(quantity, price, totalPrice);
    }
    
    /**
     * Exemple 9: Récupérer nom et lore d'un bouton générique
     */
    public void exempleButtonGeneric() {
        // Nom du bouton
        String buttonName = guiMessageManager.getButtonName("storage", "mode-storage");
        
        // Lore du bouton
        List<String> buttonLore = guiMessageManager.getButtonLore("storage", "mode-storage");
        
        // Avec placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode_simple", "Vente");
        
        String nameWithPlaceholders = guiMessageManager.getButtonName("storage", "mode-storage", placeholders);
        List<String> loreWithPlaceholders = guiMessageManager.getButtonLore("storage", "mode-storage", placeholders);
    }
    
    /**
     * Exemple 10: Lore du menu Sell (item display)
     */
    public List<String> exempleLoreSell(String itemName, int maxQuantity) {
        int currentQuantity = 32;
        double pricePerUnit = 2.50;
        double totalPrice = currentQuantity * pricePerUnit;
        
        return guiMessageManager.getSellItemDisplayLore(
            itemName, 
            maxQuantity, 
            currentQuantity, 
            pricePerUnit, 
            totalPrice
        );
    }
    
    /**
     * Exemple 11: Lore d'un item d'ordre dans les sous-menus
     */
    public List<String> exempleLoreOrder(String ordererName) {
        int quantity = 64;
        double price = 100.00;
        double totalPrice = quantity * price;
        
        return guiMessageManager.getOrderItemLore(ordererName, quantity, price, totalPrice);
    }
    
    /**
     * Exemple 12: Convertir une liste de messages en Components
     */
    public void exempleLoreComponent(Player player, ItemStack item) {
        // Récupérer la lore
        List<String> lore = guiMessageManager.getModeStorageLore("Vente");
        
        // Convertir en Components pour Adventure API
        List<net.kyori.adventure.text.Component> components = guiMessageManager.toComponentList(lore);
        
        // Utiliser avec un ItemMeta (exemple)
        // itemMeta.lore(components);
    }
    
    // ========================================
    // EXEMPLES AVANCÉS
    // ========================================
    
    /**
     * Exemple 13: Message personnalisé avec plusieurs placeholders
     */
    public void exempleMessageComplexe(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", "Diamant");
        placeholders.put("quantity", "64");
        placeholders.put("buyer", "PlayerName");
        placeholders.put("total_price", "6400.00");
        
        // Envoyer un message avec tous les placeholders
        messageManager.sendPrefixedMessage(player, "orders.seller.order-accepted", placeholders);
    }
    
    /**
     * Exemple 14: Reload des messages
     */
    public void exempleReload() {
        // Recharger tous les messages système
        messageManager.reload();
        
        // Recharger tous les messages GUI
        guiMessageManager.reload();
        
        plugin.getQuantumLogger().success("Messages rechargés!");
    }
    
    /**
     * Exemple 15: Utilisation dans un menu personnalisé
     */
    public void exempleMenuComplet(Player player, org.bukkit.inventory.Inventory inventory) {
        // 1. Définir le titre du menu
        String title = guiMessageManager.getStorageTitle("Vente");
        
        // 2. Créer un bouton avec nom et lore
        String buttonName = guiMessageManager.getButtonName("storage", "mode-storage");
        List<String> buttonLore = guiMessageManager.getModeStorageLore("Vente");
        
        // 3. Convertir en Components si nécessaire
        net.kyori.adventure.text.Component nameComponent = guiMessageManager.toComponent(buttonName);
        List<net.kyori.adventure.text.Component> loreComponents = guiMessageManager.toComponentList(buttonLore);
        
        // 4. Appliquer à l'ItemStack
        // itemMeta.displayName(nameComponent);
        // itemMeta.lore(loreComponents);
        
        // 5. Envoyer un message de confirmation
        messageManager.sendPrefixedMessage(player, "system.reload-success");
    }
    
    /**
     * Exemple 16: Gestion des erreurs avec messages
     */
    public void exempleGestionErreurs(Player player, int stockActuel, int stockRequis) {
        if (stockActuel < stockRequis) {
            // Stock insuffisant
            messageManager.sendInsufficientStock(player, stockActuel);
            return;
        }
        
        // Succès
        messageManager.sendSellSuccess(player, "Pomme", stockRequis, stockRequis * 2.50);
    }
    
    /**
     * Exemple 17: Messages multilingues (si configurés dans les YML)
     */
    public void exempleMultilingue(Player player) {
        // Les messages sont configurés dans messages.yml et messages_gui.yml
        // Le système charge automatiquement la langue configurée
        
        // Français (par défaut)
        messageManager.sendNoPermission(player);
        
        // Pour changer de langue, modifiez les fichiers YML et reload
        // messageManager.reload();
    }
}
