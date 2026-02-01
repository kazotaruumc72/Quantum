package com.wynvers.quantum.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wynvers.quantum.Quantum;
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
     * Open this menu for a player
     */
    public void open(Player player, Quantum plugin) {
        plugin.getMenuManager().setActiveMenu(player, this);
        
        String parsedTitle = plugin.getPlaceholderManager().parse(player, title);
        
        Inventory inventory = Bukkit.createInventory(null, size, parsedTitle);
 
        populateInventory(inventory, player);
        
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
        // Récupérer l'inventaire actuellement ouvert
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        
        // Vérifier que c'est bien ce menu qui est ouvert
        if (currentInventory == null || currentInventory.getSize() != size) {
            return;
        }
        
        // Repeupler l'inventaire avec les données à jour
        populateInventory(currentInventory, player);
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
        inventory.clear();
        
        // Premièrement, remplir les items standards (non-quantum_storage)
        for (MenuItem item : items.values()) {
            if (item.getSlots().isEmpty()) continue;
            
            // Si c'est un slot quantum_storage, le StorageRenderer s'en occupera
            if (item.isQuantumStorage()) {
                continue;
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
                        String parsedName = plugin.getPlaceholderManager().parse(player, meta.getDisplayName());
                        meta.setDisplayName(parsedName);
                    }
                    
                    // Parser la lore
                    if (meta.hasLore()) {
                        List<String> parsedLore = new ArrayList<>();
                        for (String loreLine : meta.getLore()) {
                            parsedLore.add(plugin.getPlaceholderManager().parse(player, loreLine));
                        }
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
