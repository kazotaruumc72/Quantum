package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.util.*;

public class MenuManager {
    
    private final Quantum plugin;
    private final Map<String, Menu> menus;
    private final Map<String, Menu> commandMenus;  // Command -> Menu mapping
    private final Map<UUID, Menu> activeMenus;     // Player UUID -> Currently open menu
    
    public MenuManager(Quantum plugin) {
        this.plugin = plugin;
        this.menus = new HashMap<>();
        this.commandMenus = new HashMap<>();
        this.activeMenus = new HashMap<>();
        
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
        Menu menu = new Menu(plugin, menuId);        
        // Basic properties - support both "title" and "menu_title"
        String title = config.getString("title");
        if (title == null) {
            title = config.getString("menu_title", "Menu");
        }
        menu.setTitle(color(title));
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
            List<?> slotList = section.getList("slots");
            if (slotList != null) {
                for (Object slotObj : slotList) {
                    if (slotObj instanceof Integer) {
                        item.addSlot((Integer) slotObj);
                    } else if (slotObj instanceof String) {
                        parseSlots((String) slotObj, item);
                    }
                }
            }
        }
        
        // Type de slot (quantum_storage, quantum_change_mode, etc.)
        if (section.contains("type")) {
            String type = section.getString("type");
            item.setType(type);
            
            // Si c'est quantum_change_mode, définir aussi le buttonType
            if ("quantum_change_mode".equalsIgnoreCase(type)) {
                item.setButtonType(ButtonType.QUANTUM_CHANGE_MODE);
                
                // Charger le mode cible si spécifié
                if (section.contains("mode")) {
                    item.setTargetMode(section.getString("mode"));
                }
            }
        }
        
        // Button type (backward compatibility)
        if (section.contains("button_type")) {
            try {
                ButtonType buttonType = ButtonType.valueOf(section.getString("button_type").toUpperCase());
                item.setButtonType(buttonType);
                
                // Si c'est QUANTUM_CHANGE_MODE, charger aussi le mode cible
                if (buttonType == ButtonType.QUANTUM_CHANGE_MODE && section.contains("mode")) {
                    item.setTargetMode(section.getString("mode"));
                }
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid button_type: " + section.getString("button_type"));
            }
        }
        
        // Lore append pour quantum_storage
        if (section.contains("lore_append")) {
            item.setLoreAppend(colorList(section.getStringList("lore_append")));
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
        
        // Glow effect
        if (section.contains("glow")) {
            item.setGlow(section.getBoolean("glow"));
        }
        
        // Hide flags for custom tooltips
        if (section.contains("hide_flags")) {
            List<String> flagStrings = section.getStringList("hide_flags");
            for (String flagStr : flagStrings) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagStr.toUpperCase());
                    item.addHideFlag(flag);
                } catch (IllegalArgumentException e) {
                    plugin.getQuantumLogger().warning("Invalid hide flag: " + flagStr);
                }
            }
        }
        
        // Actions - support both old format (left_click/right_click) and new format (click_actions)
        if (section.contains("click_actions")) {
            List<String> actionStrings = section.getStringList("click_actions");
            for (String actionStr : actionStrings) {
                MenuAction action = MenuAction.parse(actionStr);
                if (action != null) {
                    item.addLeftClickAction(action);  // Par défaut, les click_actions sont pour le clic gauche
                }
            }
        } else {
            // Ancien format
            loadActions(section, "left_click", item, true);
            loadActions(section, "right_click", item, false);
        }
        
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
    
    /**
     * Get menu by its title (for listener detection)
     */
    public Menu getMenuByTitle(String title) {
        if (title == null) return null;
        
        for (Menu menu : menus.values()) {
            // Check static title
            if (title.equals(menu.getTitle())) {
                return menu;
            }
            
            // Check animated title frames
            if (menu.isAnimatedTitle() && menu.getTitleFrames() != null) {
                for (String frame : menu.getTitleFrames()) {
                    if (title.equals(frame)) {
                        return menu;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get active menu for player (more reliable than title matching)
     */
    public Menu getActiveMenu(Player player) {
        UUID uuid = player.getUniqueId();
        Menu menu = activeMenus.get(uuid);
        
        // DEBUG: Log chaque appel à getActiveMenu
        System.out.println("[MENUMANAGER] getActiveMenu(" + player.getName() + ", " + uuid + ") => " + (menu != null ? menu.getId() : "NULL"));
        System.out.println("[MENUMANAGER] activeMenus HashMap taille: " + activeMenus.size());
        if (!activeMenus.isEmpty()) {
            System.out.println("[MENUMANAGER] activeMenus contenu:");
            activeMenus.forEach((key, value) -> System.out.println("  - " + key + " => " + value.getId()));
        }
        
        return menu;
    }
    
    /**
     * Set active menu for player
     */
    public void setActiveMenu(Player player, Menu menu) {
        UUID uuid = player.getUniqueId();
        
        // DEBUG: Log chaque appel à setActiveMenu
        System.out.println("[MENUMANAGER] setActiveMenu(" + player.getName() + ", " + uuid + ", " + (menu != null ? menu.getId() : "NULL") + ")");
        
        if (menu == null) {
            activeMenus.remove(uuid);
            System.out.println("[MENUMANAGER] Menu remové. activeMenus taille: " + activeMenus.size());
        } else {
            activeMenus.put(uuid, menu);
            System.out.println("[MENUMANAGER] Menu mis en cache. activeMenus taille: " + activeMenus.size());
            
            // Vérifier que le menu a bien été inséré
            Menu verify = activeMenus.get(uuid);
            System.out.println("[MENUMANAGER] Vérification: activeMenus.get(" + uuid + ") = " + (verify != null ? verify.getId() : "NULL"));
        }
    }
    
    /**
     * Clear active menu for player
     */
    public void clearActiveMenu(Player player) {
        UUID uuid = player.getUniqueId();
        activeMenus.remove(uuid);
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
        activeMenus.clear();
        loadMenus();
    }
}