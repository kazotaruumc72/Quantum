package com.wynvers.quantum.worldguard.gui;

import com.wynvers.quantum.Quantum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for managing WorldGuard zone settings
 */
public class ZoneSettingsGUI implements Listener {
    
    private final Quantum plugin;
    private final ZoneGUIManager zoneManager;
    
    public ZoneSettingsGUI(Quantum plugin, ZoneGUIManager zoneManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Open zone settings menu for a specific region
     */
    public void openZoneSettings(Player player, String regionName) {
        if (!player.hasPermission("quantum.zone.configure")) {
            player.sendMessage(Component.text("You don't have permission to configure zones.", NamedTextColor.RED));
            return;
        }
        
        ZoneConfig config = zoneManager.getZoneConfig(regionName);
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Zone Settings: " + regionName, NamedTextColor.DARK_PURPLE));
        
        // PVP Toggle
        ItemStack pvpItem = new ItemStack(config.isPvpEnabled() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.displayName(Component.text("PVP", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> pvpLore = new ArrayList<>();
        pvpLore.add(Component.text("Status: " + (config.isPvpEnabled() ? "Enabled" : "Disabled"), 
            config.isPvpEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        pvpLore.add(Component.text("Click to toggle", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        pvpMeta.lore(pvpLore);
        pvpItem.setItemMeta(pvpMeta);
        inv.setItem(10, pvpItem);
        
        // Mob Spawning Toggle
        ItemStack mobItem = new ItemStack(config.isMobSpawning() ? Material.ZOMBIE_HEAD : Material.BARRIER);
        ItemMeta mobMeta = mobItem.getItemMeta();
        mobMeta.displayName(Component.text("Mob Spawning", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> mobLore = new ArrayList<>();
        mobLore.add(Component.text("Status: " + (config.isMobSpawning() ? "Enabled" : "Disabled"), 
            config.isMobSpawning() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        mobLore.add(Component.text("Click to toggle", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        mobMeta.lore(mobLore);
        mobItem.setItemMeta(mobMeta);
        inv.setItem(12, mobItem);
        
        // Mob Selector
        ItemStack selectorItem = new ItemStack(Material.SPAWNER);
        ItemMeta selectorMeta = selectorItem.getItemMeta();
        selectorMeta.displayName(Component.text("Mob Selector", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        List<Component> selectorLore = new ArrayList<>();
        selectorLore.add(Component.text("Configure which mobs can spawn", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        selectorLore.add(Component.text("Click to open", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        selectorMeta.lore(selectorLore);
        selectorItem.setItemMeta(selectorMeta);
        inv.setItem(14, selectorItem);
        
        // Save Button
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.displayName(Component.text("Save Changes", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        saveMeta.lore(List.of(Component.text("Click to save and apply", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        saveItem.setItemMeta(saveMeta);
        inv.setItem(49, saveItem);
        
        // Cancel Button
        ItemStack cancelItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        cancelMeta.lore(List.of(Component.text("Close without saving", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        cancelItem.setItemMeta(cancelMeta);
        inv.setItem(45, cancelItem);
        
        player.openInventory(inv);
    }
    
    /**
     * Open mob selector menu
     */
    public void openMobSelector(Player player, String regionName) {
        if (!player.hasPermission("quantum.zone.configure")) {
            return;
        }
        
        ZoneConfig config = zoneManager.getZoneConfig(regionName);
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Mob Selector: " + regionName, NamedTextColor.DARK_PURPLE));
        
        // Common hostile mobs
        EntityType[] commonMobs = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.CAVE_SPIDER,
            EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON,
            EntityType.PHANTOM, EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.RAVAGER
        };
        
        int slot = 0;
        for (EntityType mob : commonMobs) {
            if (slot >= 45) break;
            
            boolean allowed = config.isMobAllowed(mob);
            Material material = allowed ? Material.GREEN_WOOL : Material.RED_WOOL;
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(formatMobName(mob), allowed ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Status: " + (allowed ? "Allowed" : "Denied"), 
                allowed ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click to toggle", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot, item);
            slot++;
        }
        
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text("Back", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        backItem.setItemMeta(backMeta);
        inv.setItem(49, backItem);
        
        player.openInventory(inv);
    }
    
    private String formatMobName(EntityType type) {
        String name = type.name().replace('_', ' ');
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (formatted.length() > 0) formatted.append(" ");
            formatted.append(word.substring(0, 1).toUpperCase());
            formatted.append(word.substring(1).toLowerCase());
        }
        return formatted.toString();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Component titleComponent = event.getView().title();
        String title = titleComponent.toString();
        
        if (!title.contains("Zone Settings") && !title.contains("Mob Selector")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        // Extract region name from title
        String regionName = extractRegionName(title);
        if (regionName == null) return;
        
        ZoneConfig config = zoneManager.getZoneConfig(regionName);
        ItemStack clickedItem = event.getCurrentItem();
        
        if (title.contains("Zone Settings")) {
            handleZoneSettingsClick(player, config, event.getSlot(), clickedItem);
        } else if (title.contains("Mob Selector")) {
            handleMobSelectorClick(player, config, event.getSlot(), clickedItem);
        }
    }
    
    private void handleZoneSettingsClick(Player player, ZoneConfig config, int slot, ItemStack item) {
        switch (slot) {
            case 10: // PVP toggle
                config.setPvpEnabled(!config.isPvpEnabled());
                openZoneSettings(player, config.getRegionName());
                player.sendMessage(Component.text("PVP " + (config.isPvpEnabled() ? "enabled" : "disabled"), 
                    config.isPvpEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
                break;
            
            case 12: // Mob spawning toggle
                config.setMobSpawning(!config.isMobSpawning());
                openZoneSettings(player, config.getRegionName());
                player.sendMessage(Component.text("Mob spawning " + (config.isMobSpawning() ? "enabled" : "disabled"), 
                    config.isMobSpawning() ? NamedTextColor.GREEN : NamedTextColor.RED));
                break;
            
            case 14: // Mob selector
                openMobSelector(player, config.getRegionName());
                break;
            
            case 49: // Save
                zoneManager.saveZoneConfig(config);
                player.sendMessage(Component.text("Zone configuration saved!", NamedTextColor.GREEN));
                player.closeInventory();
                break;
            
            case 45: // Cancel
                player.sendMessage(Component.text("Changes cancelled", NamedTextColor.RED));
                player.closeInventory();
                break;
        }
    }
    
    private void handleMobSelectorClick(Player player, ZoneConfig config, int slot, ItemStack item) {
        if (slot == 49) { // Back button
            openZoneSettings(player, config.getRegionName());
            return;
        }
        
        if (slot >= 45) return; // Ignore bottom row except back button
        
        // Get mob type from item display name
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        Component displayName = meta.displayName();
        if (displayName == null) return;
        
        String mobName = displayName.toString();
        
        // Find EntityType from formatted name
        EntityType[] commonMobs = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.CAVE_SPIDER,
            EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON,
            EntityType.PHANTOM, EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.RAVAGER
        };
        
        for (EntityType mob : commonMobs) {
            if (formatMobName(mob).equalsIgnoreCase(mobName) || mobName.contains(formatMobName(mob))) {
                // Toggle mob allowed/denied
                if (config.isMobAllowed(mob)) {
                    config.addDeniedMob(mob);
                } else {
                    config.addAllowedMob(mob);
                }
                openMobSelector(player, config.getRegionName());
                return;
            }
        }
    }
    
    private String extractRegionName(String title) {
        // Extract region name from title like "Zone Settings: region_name" or "Mob Selector: region_name"
        int colonIndex = title.indexOf(':');
        if (colonIndex != -1 && colonIndex + 2 < title.length()) {
            return title.substring(colonIndex + 2).trim();
        }
        return null;
    }
}
