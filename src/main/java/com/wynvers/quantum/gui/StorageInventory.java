package com.wynvers.quantum.gui;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageInventory {

    private final Quantum plugin;
    private final Player player;
    private final PlayerStorage storage;
    private Inventory inventory;
    private int page = 0;
    private final int ITEMS_PER_PAGE = 45; // 5 rows of 9 slots

    public StorageInventory(Quantum plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.storage = plugin.getStorageManager().getStorage(player.getUniqueId());
    }

    /**
     * Open the storage GUI for the player
     */
    public void open() {
        open(0);
    }

    /**
     * Open the storage GUI at a specific page
     */
    public void open(int page) {
        this.page = page;
        inventory = Bukkit.createInventory(null, 54, "§8§lStorage - Page " + (page + 1));
        
        // Fill inventory with storage items
        fillInventory();
        
        // Add navigation buttons
        addNavigationButtons();
        
        player.openInventory(inventory);
    }

    /**
     * Fill the inventory with items from storage
     */
    private void fillInventory() {
        Map<Material, Integer> vanillaItems = storage.getVanillaItems();
        Map<String, Integer> nexoItems = storage.getNexoItems();
        
        List<ItemStack> allItems = new ArrayList<>();
        
        // Add vanilla items
        for (Map.Entry<Material, Integer> entry : vanillaItems.entrySet()) {
            ItemStack item = new ItemStack(entry.getKey());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + formatName(entry.getKey().name()));
                List<String> lore = new ArrayList<>();
                lore.add("§7Quantité: §f" + entry.getValue());
                lore.add("");
                lore.add("§eClick gauche: §fRetirer 1");
                lore.add("§eClick droit: §fRetirer 64");
                lore.add("§eShift + Click: §fRetirer tout");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            allItems.add(item);
        }
        
        // Add Nexo items (placeholder for now)
        for (Map.Entry<String, Integer> entry : nexoItems.entrySet()) {
            ItemStack item = new ItemStack(Material.DIAMOND);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b[Nexo] " + entry.getKey());
                List<String> lore = new ArrayList<>();
                lore.add("§7Quantité: §f" + entry.getValue());
                lore.add("");
                lore.add("§eClick gauche: §fRetirer 1");
                lore.add("§eClick droit: §fRetirer 64");
                lore.add("§eShift + Click: §fRetirer tout");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            allItems.add(item);
        }
        
        // Calculate pagination
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());
        
        // Add items to inventory
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(slot++, allItems.get(i));
        }
    }

    /**
     * Add navigation buttons to the inventory
     */
    private void addNavigationButtons() {
        // Previous page button
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e← Page précédente");
                prev.setItemMeta(meta);
            }
            inventory.setItem(48, prev);
        }
        
        // Info button
        ItemStack info = new ItemStack(Material.CHEST);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lStorage Info");
            List<String> lore = new ArrayList<>();
            lore.add("§7Items uniques: §f" + (storage.getVanillaItems().size() + storage.getNexoItems().size()));
            lore.add("§7Page: §f" + (page + 1));
            meta.setLore(lore);
            info.setItemMeta(meta);
        }
        inventory.setItem(49, info);
        
        // Next page button
        int totalItems = storage.getVanillaItems().size() + storage.getNexoItems().size();
        if ((page + 1) * ITEMS_PER_PAGE < totalItems) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§eP age suivante →");
                next.setItemMeta(nextMeta);
            }
            inventory.setItem(50, next);
        }
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c✖ Fermer");
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(53, close);
    }

    /**
     * Format material name to readable text
     */
    private String formatName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)));
            formatted.append(word.substring(1));
            formatted.append(" ");
        }
        return formatted.toString().trim();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }

    public PlayerStorage getStorage() {
        return storage;
    }
}
