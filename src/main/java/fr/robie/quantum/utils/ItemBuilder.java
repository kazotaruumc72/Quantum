package fr.robie.quantum.utils;

import fr.robie.quantum.Quantum;
import fr.robie.quantum.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    
    private final Quantum plugin;
    
    public ItemBuilder(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Build ItemStack from MenuItem
     */
    public ItemStack build(Player player, MenuItem menuItem) {
        ItemStack item;
        
        // Check if Nexo item
        if (menuItem.isNexoItem()) {
            item = buildNexoItem(menuItem);
        } else {
            item = buildVanillaItem(menuItem);
        }
        
        if (item == null) {
            return new ItemStack(Material.BARRIER);
        }
        
        // Apply meta
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Display name
            if (menuItem.getDisplayName() != null) {
                String displayName = parsePlaceholders(player, menuItem.getDisplayName());
                meta.setDisplayName(displayName);
            }
            
            // Lore
            if (menuItem.getLore() != null && !menuItem.getLore().isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : menuItem.getLore()) {
                    lore.add(parsePlaceholders(player, line));
                }
                meta.setLore(lore);
            }
            
            // Custom model data
            if (menuItem.getCustomModelData() > 0) {
                meta.setCustomModelData(menuItem.getCustomModelData());
            }
            
            // Skull owner
            if (meta instanceof SkullMeta && menuItem.getSkullOwner() != null) {
                String owner = parsePlaceholders(player, menuItem.getSkullOwner());
                ((SkullMeta) meta).setOwner(owner);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Build vanilla item
     */
    private ItemStack buildVanillaItem(MenuItem menuItem) {
        if (menuItem.getMaterial() == null) {
            return null;
        }
        
        return new ItemStack(menuItem.getMaterial(), menuItem.getAmount());
    }
    
    /**
     * Build Nexo custom item
     */
    private ItemStack buildNexoItem(MenuItem menuItem) {
        // Check if Nexo is available
        if (!Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            plugin.getQuantumLogger().warning("Nexo not found but menu item requires it: " + menuItem.getId());
            return null;
        }
        
        try {
            // Use Nexo API to get item
            // This is a placeholder - actual implementation depends on Nexo version
            // Example: return NexoItems.itemFromId(menuItem.getNexoId()).build();
            
            plugin.getQuantumLogger().debug("Building Nexo item: " + menuItem.getNexoId());
            
            // Fallback to barrier if Nexo API not available
            return new ItemStack(Material.BARRIER);
            
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to build Nexo item: " + menuItem.getNexoId());
            return null;
        }
    }
    
    /**
     * Parse placeholders in text
     */
    private String parsePlaceholders(Player player, String text) {
        if (plugin.getPlaceholderManager() != null) {
            return plugin.getPlaceholderManager().parse(player, text);
        }
        return ChatColor.translateAlternateColorCodes('&', text.replace("%player%", player.getName()));
    }
}
