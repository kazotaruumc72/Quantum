package com.wynvers.quantum.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.sell.SellSession;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Menu {
 
    private final String id;
    private final Quantum plugin;
    private String title;
    private int size;
    private String openCommand;
 
    // Animated title
    private boolean animatedTitle;
    private List<String> titleFrames;
    private int titleSpeed;
 
    // Items
    private final Map<String, MenuItem> items;
    
    // Storage renderer pour slots quantum_storage
    private StorageRenderer storageRenderer;
    
    // MiniMessage parser pour les titres modernes
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
 
    public Menu(Quantum plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.size = 54;
        this.items = new HashMap<>();
        this.titleFrames = new ArrayList<>();
        this.titleSpeed = 10;
        this.storageRenderer = new StorageRenderer(plugin);
    }
 
    // === GETTERS ===
 
    public String getId() {
        return id;
    }
 
    public String getTitle() {
        return title;
    }
 
    public int getSize() {
        return size;
    }
 
    public String getOpenCommand() {
        return openCommand;
    }
 
    public boolean hasAnimatedTitle() {
        return animatedTitle;
    }
    
    public boolean isAnimatedTitle() {
        return animatedTitle;
    }
 
    public List<String> getTitleFrames() {
        return titleFrames;
    }
 
    public int getTitleSpeed() {
        return titleSpeed;
    }
 
    public Map<String, MenuItem> getItems() {
        return items;
    }
 
    public MenuItem getItem(String id) {
        return items.get(id);
    }
    
    /**
     * Get menu item at specific slot
     */
    public MenuItem getItemAt(int slot) {
        for (MenuItem item : items.values()) {
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }
 
    // === SETTERS ===
 
    public void setTitle(String title) {
        this.title = title;
    }
 
    public void setSize(int size) {
        // Validate size (must be multiple of 9, between 9 and 54)
        if (size % 9 != 0 || size < 9 || size > 54) {
            this.size = 54;
        } else {
            this.size = size;
        }
    }
 
    public void setOpenCommand(String openCommand) {
        this.openCommand = openCommand;
    }
 
    public void setAnimatedTitle(boolean animatedTitle) {
        this.animatedTitle = animatedTitle;
    }
 
    public void setTitleFrames(List<String> titleFrames) {
        this.titleFrames = titleFrames;
    }
 
    public void setTitleSpeed(int titleSpeed) {
        this.titleSpeed = Math.max(1, titleSpeed);
    }
 
    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
    }
    
    /**
     * Parse title avec support MiniMessage + Legacy
     * Si le titre contient '<', c'est du MiniMessage
     * Sinon, c'est du legacy avec '&'
     */
    private String parseTitle(String rawTitle) {
        if (rawTitle == null) return "Menu";
        
        // Détecter MiniMessage (présence de '<')
        if (rawTitle.contains("<")) {
            try {
                Component component = miniMessage.deserialize(rawTitle);
                return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection()
                    .serialize(component);
            } catch (Exception e) {
                // Fallback sur legacy si erreur
                return ChatColor.translateAlternateColorCodes('&', rawTitle);
            }
        } else {
            // Legacy color codes
            return ChatColor.translateAlternateColorCodes('&', rawTitle);
        }
    }
    
    /**
     * Open this menu for a player
     */
    public void open(Player player, Quantum plugin) {
        open(player, plugin, null);
    }
    
    /**
     * Open this menu for a player avec placeholders personnalisés
     */
    public void open(Player player, Quantum plugin, Map<String, String> customPlaceholders) {
        plugin.getMenuManager().setActiveMenu(player, this);
        
        String parsedTitle = customPlaceholders != null 
            ? plugin.getPlaceholderManager().parse(player, title, customPlaceholders)
            : plugin.getPlaceholderManager().parse(player, title);
        
        // PATCH: Appliquer MiniMessage + Legacy
        parsedTitle = parseTitle(parsedTitle);
        
        Inventory inventory = Bukkit.createInventory(null, size, parsedTitle);
 
        populateInventory(inventory, player, customPlaceholders);
        
        player.openInventory(inventory);
        
        if (animatedTitle && titleFrames != null && !titleFrames.isEmpty()) {
            plugin.getAnimationManager().startAnimation(player, titleFrames, titleSpeed);
        }
    }
    
    /**
     * Rafraîchit le menu sans le fermer
     * Utile pour mettre à jour le contenu dynamique (mode de stockage, items, etc.)
     */
    public void refresh(Player player, Quantum plugin) {
        refresh(player, plugin, null);
    }
    
    /**
     * Rafraîchit le menu avec placeholders personnalisés
     */
    public void refresh(Player player, Quantum plugin, Map<String, String> customPlaceholders) {
        // Récupérer l'inventaire actuellement ouvert
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        
        // Vérifier que c'est bien ce menu qui est ouvert
        if (currentInventory == null || currentInventory.getSize() != size) {
            return;
        }
        
        // Repeupler l'inventaire avec les données à jour
        populateInventory(currentInventory, player, customPlaceholders);
    }
    
    // Additional methods needed by MenuManager
 
    public MenuItem getMenuItem(int slot) {
        // Find the MenuItem that contains this slot
        for (MenuItem item : items.values()) {
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }
 
    public List<String> getAnimatedTitles() {
        return titleFrames;
    }
 
    public long getTitleUpdateInterval() {
        return titleSpeed;
    }
 
    public void updateTitle(Player player, String newTitle) {
        // Note: Bukkit doesn't support dynamically updating inventory titles
        // This is a limitation of the Bukkit API
    }
 
    /**
     * Remplit l'inventaire avec les items du menu
     * Gère aussi les slots quantum_storage avec le StorageRenderer
     */
    public void populateInventory(Inventory inventory) {
        populateInventory(inventory, null);
    }
    
    /**
     * Remplit l'inventaire avec les items du menu pour un joueur spécifique
     */
    public void populateInventory(Inventory inventory, Player player) {
        populateInventory(inventory, player, null);
    }
    
    /**
     * Remplit l'inventaire avec les items du menu pour un joueur spécifique avec placeholders
     */
    public void populateInventory(Inventory inventory, Player player, Map<String, String> customPlaceholders) {
        inventory.clear();
        
        // Récupérer la session sell si elle existe
        SellSession sellSession = player != null ? plugin.getSellManager().getSession(player) : null;
        
        // Récupérer la session order si elle existe
        OrderCreationSession orderSession = player != null ? plugin.getOrderCreationManager().getSession(player) : null;
        
        // Premièrement, remplir les items standards (non-quantum_storage)
        for (MenuItem item : items.values()) {
            if (item.getSlots().isEmpty()) continue;
            
            // Si c'est un slot quantum_storage, le StorageRenderer s'en occupera
            if (item.isQuantumStorage()) {
                continue;
            }
            
            // Si c'est un quantum_sell_item ET qu'il y a une session, utiliser l'item de la session
            if (item.getButtonType() == ButtonType.QUANTUM_SELL_ITEM && sellSession != null) {
                ItemStack sellItem = sellSession.getItemToSell().clone();
                
                // Définir le nombre d'items (max 64 si plus)
                int displayAmount = Math.min(sellSession.getQuantity(), 64);
                sellItem.setAmount(displayAmount);
                
                // Parser les placeholders dans le display name et lore si présents dans le MenuItem
                ItemMeta meta = sellItem.getItemMeta();
                if (meta != null) {
                    // Utiliser le display name et lore du MenuItem s'ils existent
                    if (item.getDisplayName() != null) {
                        String parsedName = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, item.getDisplayName(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, item.getDisplayName());
                        meta.setDisplayName(parsedName);
                    }
                    
                    if (item.getLore() != null && !item.getLore().isEmpty()) {
                        List<String> parsedLore = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, item.getLore());
                        meta.setLore(parsedLore);
                    }
                    
                    sellItem.setItemMeta(meta);
                }
                
                // Placer l'item dans tous les slots configurés
                for (int slot : item.getSlots()) {
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, sellItem.clone());
                    }
                }
                
                continue;
            }
            
            // === PATCH: QUANTUM_ORDER_DISPLAY_ITEM ===
            // Si c'est un quantum_order_display_item ET qu'il y a une session, utiliser l'item de la session
            if (item.getButtonType() == ButtonType.QUANTUM_ORDER_DISPLAY_ITEM && orderSession != null) {
                ItemStack orderItem = orderSession.getDisplayItem();
                
                if (orderItem != null) {
                    // Détecter si c'est un item Nexo
                    String nexoId = NexoItems.idFromItem(orderItem);
                    ItemStack finalItem;
                    
                    if (nexoId != null) {
                        // === C'est un item NEXO ===
                        // Utiliser ItemBuilder pour préserver les métadonnées Nexo
                        ItemBuilder builder = NexoItems.itemFromId(nexoId);
                        
                        if (builder != null) {
                            // Construire l'item avec la lore personnalisée
                            if (item.getLore() != null && !item.getLore().isEmpty()) {
                                List<String> parsedLore = customPlaceholders != null
                                    ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                                    : plugin.getPlaceholderManager().parse(player, item.getLore());
                                
                                // Ajouter la lore du menu à la lore existante de Nexo
                                builder.setLore(parsedLore);
                            }
                            
                            finalItem = builder.build();
                            finalItem.setAmount(1);
                        } else {
                            // Fallback si ItemBuilder échoue
                            finalItem = orderItem.clone();
                            finalItem.setAmount(1);
                        }
                    } else {
                        // === Item VANILLA ===
                        // Utiliser la méthode classique avec setItemMeta()
                        finalItem = orderItem.clone();
                        finalItem.setAmount(1);
                        
                        ItemMeta meta = finalItem.getItemMeta();
                        if (meta != null) {
                            if (item.getDisplayName() != null) {
                                String parsedName = customPlaceholders != null
                                    ? plugin.getPlaceholderManager().parse(player, item.getDisplayName(), customPlaceholders)
                                    : plugin.getPlaceholderManager().parse(player, item.getDisplayName());
                                meta.setDisplayName(parsedName);
                            }
                            
                            if (item.getLore() != null && !item.getLore().isEmpty()) {
                                List<String> parsedLore = customPlaceholders != null
                                    ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                                    : plugin.getPlaceholderManager().parse(player, item.getLore());
                                meta.setLore(parsedLore);
                            }
                            
                            finalItem.setItemMeta(meta);
                        }
                    }
                    
                    // Placer l'item dans tous les slots configurés
                    for (int slot : item.getSlots()) {
                        if (slot >= 0 && slot < size) {
                            inventory.setItem(slot, finalItem.clone());
                        }
                    }
                    
                    continue;
                }
            }
 
            // Créer l'ItemStack depuis le MenuItem
            ItemStack itemStack = item.toItemStack(plugin);
            if (itemStack == null) continue;
            
            // Parser les placeholders dans le display name et la lore si un joueur est fourni
            if (player != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    // Parser le display name
                    if (meta.hasDisplayName()) {
                        String parsedName = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, meta.getDisplayName(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, meta.getDisplayName());
                        meta.setDisplayName(parsedName);
                    }
                    
                    // Parser la lore
                    if (meta.hasLore()) {
                        List<String> parsedLore = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, meta.getLore(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, meta.getLore());
                        meta.setLore(parsedLore);
                    }
                    
                    itemStack.setItemMeta(meta);
                }
            }
 
            // Placer l'item dans tous les slots configurés
            for (int slot : item.getSlots()) {
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, itemStack.clone()); // Clone pour éviter les problèmes de référence
                }
            }
        }
        
        // Ensuite, remplir les slots quantum_storage avec les items du joueur
        if (player != null) {
            renderStorageSlots(player, inventory);
        }
    }
    
    /**
     * Render storage slots for a player
     */
    private void renderStorageSlots(Player player, Inventory inventory) {
        // Trouver le premier item avec lore_append pour obtenir la config
        StorageRenderer.LoreAppendConfig loreConfig = null;
        
        for (MenuItem item : items.values()) {
            if (item.isQuantumStorage() && item.getLoreAppend() != null && !item.getLoreAppend().isEmpty()) {
                loreConfig = new StorageRenderer.LoreAppendConfig(item.getLoreAppend());
                break;
            }
        }
        
        // Utiliser le StorageRenderer pour remplir les slots
        storageRenderer.renderStorageSlots(player, inventory, this, loreConfig);
    }
}
