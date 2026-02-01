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
    private final Map<String, Menu> commandMenus;
    private final Map<UUID, Menu> activeMenus;
    
    public MenuManager(Quantum plugin) {
        this.plugin = plugin;
        this.menus = new HashMap<>();
        this.commandMenus = new HashMap<>();
        this.activeMenus = new HashMap<>();
        
        loadMenus();
    }
    
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
    
    private Menu loadMenu(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String menuId = file.getName().replace(".yml", "");
        Menu menu = new Menu(plugin, menuId);
        
        String title = config.getString("title");
        if (title == null) {
            title = config.getString("menu_title", "Menu");
        }
        menu.setTitle(color(title));
        menu.setSize(config.getInt("size", 54));
        menu.setOpenCommand(config.getString("open_command"));
        
        if (config.contains("animated_title")) {
            ConfigurationSection animSection = config.getConfigurationSection("animated_title");
            if (animSection != null && animSection.getBoolean("enabled", false)) {
                menu.setAnimatedTitle(true);
                menu.setTitleFrames(colorList(animSection.getStringList("frames")));
                menu.setTitleSpeed(animSection.getInt("speed", 10));
            }
        }
        
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
    
    private MenuItem loadMenuItem(ConfigurationSection section, String itemId) {
        if (section == null) return null;
        
        MenuItem item = new MenuItem(itemId);
        
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
        
        // Gérer d'abord le champ 'type' (ancienne syntaxe)
        if (section.contains("type")) {
            String type = section.getString("type");
            item.setType(type);
            
            if ("quantum_change_mode".equalsIgnoreCase(type)) {
                item.setButtonType(ButtonType.QUANTUM_CHANGE_MODE);
                
                // Accepter 'mode' OU 'target_mode'
                if (section.contains("target_mode")) {
                    item.setTargetMode(section.getString("target_mode"));
                } else if (section.contains("mode")) {
                    item.setTargetMode(section.getString("mode"));
                }
            }
        }
        
        // Gérer le champ 'button_type' (nouvelle syntaxe)
        if (section.contains("button_type")) {
            try {
                ButtonType buttonType = ButtonType.valueOf(section.getString("button_type").toUpperCase());
                item.setButtonType(buttonType);
                
                // Pour QUANTUM_CHANGE_MODE, accepter 'target_mode' OU 'mode'
                if (buttonType == ButtonType.QUANTUM_CHANGE_MODE) {
                    if (section.contains("target_mode")) {
                        item.setTargetMode(section.getString("target_mode"));
                    } else if (section.contains("mode")) {
                        item.setTargetMode(section.getString("mode"));
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Invalid button_type: " + section.getString("button_type"));
            }
        }
        
        if (section.contains("lore_append")) {
            item.setLoreAppend(colorList(section.getStringList("lore_append")));
        }
        
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
        
        if (section.contains("glow")) {
            item.setGlow(section.getBoolean("glow"));
        }
        
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
        
        // Support pour les actions:
        // 1. click_actions: [] (ancienne syntaxe simple)
        // 2. left_click_actions: [] (DeluxeMenus)
        // 3. left_click.actions: [] (Quantum original)
        if (section.contains("click_actions")) {
            List<String> actionStrings = section.getStringList("click_actions");
            for (String actionStr : actionStrings) {
                MenuAction action = MenuAction.parse(actionStr);
                if (action != null) {
                    item.addLeftClickAction(action);
                }
            }
        }
        
        // Supporter left_click_actions (style DeluxeMenus)
        if (section.contains("left_click_actions")) {
            List<String> actionStrings = section.getStringList("left_click_actions");
            for (String actionStr : actionStrings) {
                MenuAction action = MenuAction.parse(actionStr);
                if (action != null) {
                    item.addLeftClickAction(action);
                }
            }
        } else {
            // Supporter left_click.actions (style original)
            loadActions(section, "left_click", item, true);
        }
        
        // Supporter right_click_actions (style DeluxeMenus)
        if (section.contains("right_click_actions")) {
            List<String> actionStrings = section.getStringList("right_click_actions");
            for (String actionStr : actionStrings) {
                MenuAction action = MenuAction.parse(actionStr);
                if (action != null) {
                    item.addRightClickAction(action);
                }
            }
        } else {
            // Supporter right_click.actions (style original)
            loadActions(section, "right_click", item, false);
        }
        
        loadRequirements(section, "view_requirements", item, true);
        loadRequirements(section, "click_requirements", item, false);
        
        return item;
    }
    
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
    
    private String color(String text) {
        return text == null ? null : ChatColor.translateAlternateColorCodes('&', text);
    }
    
    private List<String> colorList(List<String> texts) {
        List<String> colored = new ArrayList<>();
        for (String text : texts) {
            colored.add(color(text));
        }
        return colored;
    }
    
    public Menu getMenu(String id) {
        return menus.get(id);
    }
    
    public Menu getMenuByCommand(String command) {
        return commandMenus.get(command.toLowerCase());
    }
    
    public Menu getMenuByTitle(String title) {
        if (title == null) return null;
        
        for (Menu menu : menus.values()) {
            if (title.equals(menu.getTitle())) {
                return menu;
            }
            
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
    
    public Menu getActiveMenu(Player player) {
        return activeMenus.get(player.getUniqueId());
    }
    
    public void setActiveMenu(Player player, Menu menu) {
        UUID uuid = player.getUniqueId();
        
        if (menu == null) {
            activeMenus.remove(uuid);
        } else {
            activeMenus.put(uuid, menu);
        }
    }
    
    public void clearActiveMenu(Player player) {
        activeMenus.remove(player.getUniqueId());
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
