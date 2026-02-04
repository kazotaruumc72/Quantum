package com.wynvers.quantum.utils;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.MenuItem;
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
        
        // For Nexo items, preserve the original tooltip/lore from Nexo configuration
        // Only apply custom modifications if explicitly defined in MenuItem
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            boolean modified = false;
            
            // Display name - only override if explicitly set
            if (menuItem.getDisplayName() != null) {
                String displayName = parsePlaceholders(player, menuItem.getDisplayName());
                meta.setDisplayName(displayName);
                modified = true;
            }
            
            // Lore - only override if explicitly set
            // For Nexo items, this preserves the custom tooltip unless we want to override it
            if (menuItem.getLore() != null && !menuItem.getLore().isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : menuItem.getLore()) {
                    lore.add(parsePlaceholders(player, line));
                }
                meta.setLore(lore);
                modified = true;
            }
            
            // Custom model data - only set if not a Nexo item or explicitly defined
            if (!menuItem.isNexoItem() && menuItem.getCustomModelData() > 0) {
                meta.setCustomModelData(menuItem.getCustomModelData());
                modified = true;
            }
            
            // Skull owner
            if (meta instanceof SkullMeta && menuItem.getSkullOwner() != null) {
                String owner = parsePlaceholders(player, menuItem.getSkullOwner());
                ((SkullMeta) meta).setOwner(owner);
                modified = true;
            }
            
            if (modified) {
                item.setItemMeta(meta);
            }
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
     * Build Nexo custom item with proper API support
     * This method uses Nexo's ItemBuilder which automatically handles:
     * - Custom tooltips defined in Nexo configuration
     * - Custom model data
     * - Item properties and mechanics
     */
    private ItemStack buildNexoItem(MenuItem menuItem) {
        // Check if Nexo is available
        if (!Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            plugin.getQuantumLogger().warning("Nexo not found but menu item requires it: " + menuItem.getId());
            return null;
        }
        
        try {
            // Use Nexo API to get item with full tooltip support
            com.nexomc.nexo.items.ItemBuilder nexoBuilder = NexoItems.itemFromId(menuItem.getNexoId());
            
            if (nexoBuilder == null) {
                plugin.getQuantumLogger().warning("Nexo item not found: " + menuItem.getNexoId());
                return new ItemStack(Material.BARRIER);
            }
            
            // Build the item - this preserves all Nexo properties including custom tooltips
            ItemStack item = nexoBuilder.build();
            
            // Set amount if specified
            if (menuItem.getAmount() > 0) {
                item.setAmount(menuItem.getAmount());
            }
            
            plugin.getQuantumLogger().debug("Successfully built Nexo item: " + menuItem.getNexoId() + " with custom tooltips");
            
            return item;
            
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to build Nexo item: " + menuItem.getNexoId(), e);
            return new ItemStack(Material.BARRIER);
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
