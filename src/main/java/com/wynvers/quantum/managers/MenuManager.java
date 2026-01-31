package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MenuManager {
    
    private final Quantum plugin;
    private final Map<String, Menu> menus;
    private final Map<String, Menu> commandMenus;  // Command -> Menu mapping
    
    public MenuManager(Quantum plugin) {
        this.plugin = plugin;
        this.menus = new HashMap<>();
        this.commandMenus = new HashMap<>();
        
        loadMenus();
    }
    
    /**
     * Load all menus from menus/ folder
     */
    private void loadMenus() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
            plugin.saveResource("menus/example.yml", false);
        }
        
        File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files == null || files.length == 0) {
            plugin.getQuantumLogger().warning("No menu files found in menus/ folder");
            return;
        }
        
        int loaded = 0;
        for (File file : files) {
            try {
                Menu menu = loadMenu(file);
                if (menu != null) {
                    menus.put(menu.getId(), menu);
                    
                    // Register command if specified
                    if (menu.getOpenCommand() != null) {
                        commandMenus.put(menu.getOpenCommand().toLowerCase(), menu);
                    }
                    
                    loaded++;
                }
            } catch (Exception e) {
                plugin.getQuantumLogger().error("Failed to load menu: " + file.getName());
                e.printStackTrace();
            }
        }
        
        plugin.getQuantumLogger().success("Loaded " + loaded + " menus");
    }
    
    /**
     * Load single menu from file
     */
    private Menu loadMenu(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String menuId = file.getName().replace(".yml", "");
        Menu menu = new Menu(menuId);
        
        // Basic properties
        menu.setTitle(color(config.getString("menu_title", "Menu")));
        menu.setSize(config.getInt("size", 54));
        menu.setOpenCommand(config.getString("open_command"));
        
        // Animated title
        if (config.contains("animated_title")) {
            ConfigurationSection animSection = config.getConfigurationSection("animated_title");
            if (animSection != null && animSection.getBoolean("enabled", false)) {
                menu.setAnimatedTitle(true);
                menu.setTitleFrames(colorList(animSection.getStringList("frames")));
                menu.setTitleSpeed(animSection.getInt("speed", 10));
            }
        }
        
        // Load items
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemId : itemsSection.getKeys(false)) {
                MenuItem item = loadMenuItem(itemsSection.getConfigurationSection(itemId), itemId);
                if (item != null) {
                    menu.addItem(item);
                }
            }
        }
        
        return menu;
    }
    
    /**
     * Load menu item from config section
     */
    private MenuItem loadMenuItem(ConfigurationSection section, String itemId) {
        if (section == null) return null;
        
        MenuItem item = new MenuItem(itemId);
        
        // Slots
        if (section.contains("slot")) {
            item.addSlot(section.getInt("slot"));
        }
        if (section.contains("slots")) {
            for (String slotDef : section.getStringList("slots")) {
                parseSlots(slotDef, item);
            }
        }
        
        // Material or Nexo item
        if (section.contains("nexo_item")) {
            item.setNexoId(section.getString("nexo_item"));
        } else if (section.contains("material")) {
            try {
                Material material = Material.valueOf(section.getString("material").toUpperCase());
                item.setMaterial(material);
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid material: " + section.getString("material"));
                return null;
            }
        }
        
        // Properties
        item.setAmount(section.getInt("amount", 1));
        if (section.contains("display_name")) {
            item.setDisplayName(color(section.getString("display_name")));
        }
        if (section.contains("lore")) {
            item.setLore(colorList(section.getStringList("lore")));
        }
        if (section.contains("skull_owner")) {
            item.setSkullOwner(section.getString("skull_owner"));
        }
        if (section.contains("custom_model_data")) {
            item.setCustomModelData(section.getInt("custom_model_data"));
        }
        
        // Actions
        loadActions(section, "left_click", item, true);
        loadActions(section, "right_click", item, false);
        
        // Requirements
        loadRequirements(section, "view_requirements", item, true);
        loadRequirements(section, "click_requirements", item, false);
        
        return item;
    }
    
    /**
     * Parse slot definitions (supports ranges like 0-8)
     */
    private void parseSlots(String slotDef, MenuItem item) {
        if (slotDef.contains("-")) {
            String[] parts = slotDef.split("-");
            try {
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                for (int i = start; i <= end; i++) {
                    item.addSlot(i);
                }
            } catch (NumberFormatException e) {
                plugin.getQuantumLogger().warning("Invalid slot range: " + slotDef);
            }
        } else {
            try {
                item.addSlot(Integer.parseInt(slotDef.trim()));
            } catch (NumberFormatException e) {
                plugin.getQuantumLogger().warning("Invalid slot: " + slotDef);
            }
        }
    }
    
    /**
     * Load actions from section
     */
    private void loadActions(ConfigurationSection section, String path, MenuItem item, boolean leftClick) {
        if (!section.contains(path)) return;
        
        ConfigurationSection actionSection = section.getConfigurationSection(path);
        if (actionSection == null) return;
        
        List<String> actionStrings = actionSection.getStringList("actions");
        for (String actionStr : actionStrings) {
            MenuAction action = MenuAction.parse(actionStr);
            if (action != null) {
                if (leftClick) {
                    item.addLeftClickAction(action);
                } else {
                    item.addRightClickAction(action);
                }
            }
        }
    }
    
    /**
     * Load requirements from section
     */
    private void loadRequirements(ConfigurationSection section, String path, MenuItem item, boolean view) {
        if (!section.contains(path)) return;
        
        List<String> reqStrings = section.getStringList(path);
        for (String reqStr : reqStrings) {
            Requirement req = Requirement.parse(reqStr);
            if (req != null) {
                if (view) {
                    item.addViewRequirement(req);
                } else {
                    item.addClickRequirement(req);
                }
            }
        }
    }
    
    /**
     * Color single string
     */
    private String color(String text) {
        return text == null ? null : ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Color list of strings
     */
    private List<String> colorList(List<String> texts) {
        List<String> colored = new ArrayList<>();
        for (String text : texts) {
            colored.add(color(text));
        }
        return colored;
    }
    
    // === PUBLIC API ===
    
    public Menu getMenu(String id) {
        return menus.get(id);
    }
    
    public Menu getMenuByCommand(String command) {
        return commandMenus.get(command.toLowerCase());
    }
    
    public Collection<Menu> getAllMenus() {
        return menus.values();
    }
    
    public int getMenuCount() {
        return menus.size();
    }
    
    public void reload() {
        menus.clear();
        commandMenus.clear();
        loadMenus();
    }
}
