package com.wynvers.quantum.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
    
    public Menu(Quantum plugin, String id) {
                this.plugin = plugin;this.id = id;
        this.size = 54;
        this.items = new HashMap<>();
        this.titleFrames = new ArrayList<>();
        this.titleSpeed = 10;
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
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Populate inventory with items from config
        for (MenuItem item : items.values()) {
            if (item.getSlots().isEmpty()) continue;
            
            // Create ItemStack from MenuItem
            org.bukkit.inventory.ItemStack itemStack = item.toItemStack(plugin);
            if (itemStack == null) continue;
            
            // Place item in all configured slots
            for (int slot : item.getSlots()) {
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, itemStack);
                }
            }
        }        
        player.openInventory(inventory);
    }

    // Additional methods needed by MenuManager
    
    public MenuItem getMenuItem(int slot) {
        return null; // This will need to be implemented properly
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
    
    public void populateInventory(Inventory inventory) {
        inventory.clear();
        for (MenuItem item : items.values()) {
            if (item.getSlots().isEmpty()) continue;
            
            org.bukkit.inventory.ItemStack itemStack = item.toItemStack(plugin);
            if (itemStack == null) continue;
            
            for (int slot : item.getSlots()) {
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, itemStack);
                }
            }
        }
    }


    // === STATIC FACTORY METHOD ===
    
    public static Menu fromConfig(Quantum plugin, String menuName, FileConfiguration config) {
        Menu menu = new Menu(plugin, menuName);
        
        // Load basic properties
        String menuTitle = config.getString("menu_title", "&8Menu");
        menu.setTitle(menuTitle);
        
        int menuSize = config.getInt("menu_size", 54);
        menu.setSize(menuSize);
        
        String openCommand = config.getString("open_command");
        menu.setOpenCommand(openCommand);
        
        // Load animated title
        boolean animatedTitle = config.getBoolean("animated_title", false);
        menu.setAnimatedTitle(animatedTitle);
        
        if (animatedTitle) {
            List<String> titleFrames = config.getStringList("title_frames");
            menu.setTitleFrames(titleFrames);
            
            int titleSpeed = config.getInt("title_speed", 10);
            menu.setTitleSpeed(titleSpeed);
        }
        
        // Load items
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                if (itemConfig != null) {
                    MenuItem menuItem = MenuItem.fromConfig(plugin, itemKey, itemConfig);
                    List<Integer> slots = menuItem.getSlots();
                    for (int slot : slots) {
                        menu.items.put(slot, menuItem);
                    }
                }
            }
        }
        
        return menu;
    }

}



