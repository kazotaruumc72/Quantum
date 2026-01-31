package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.PriceManager;
import com.wynvers.quantum.storage.StorageItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe responsable du rendu des items du storage dans les menus
 * Gère le type quantum_storage et l'append de lore (quantité + prix)
 */
public class StorageRenderer {

    private final Quantum plugin;
    private final PriceManager priceManager;

    public StorageRenderer(Quantum plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
    }

    /**
     * Remplit les slots quantum_storage dans l'inventaire avec les items du joueur
     * @param player Le joueur
     * @param inventory L'inventaire du menu
     * @param menu Le menu à remplir
     * @param loreAppendConfig Configuration du lore à ajouter (peut être null)
     */
    public void renderStorageSlots(Player player, Inventory inventory, Menu menu, LoreAppendConfig loreAppendConfig) {
        // Récupérer tous les items du storage du joueur
        Map<String, StorageItem> storageItems = plugin.getStorageManager().getPlayerStorage(player.getUniqueId());
        
        // Trouver tous les MenuItem de type quantum_storage
        List<MenuItem> storageSlotItems = new ArrayList<>();
        for (MenuItem item : menu.getItems().values()) {
            if (item.getType() != null && item.getType().equalsIgnoreCase("quantum_storage")) {
                storageSlotItems.add(item);
            }
        }
        
        if (storageSlotItems.isEmpty()) {
            return; // Pas de slots quantum_storage dans ce menu
        }
        
        // Récupérer les slots disponibles
        List<Integer> availableSlots = new ArrayList<>();
        for (MenuItem item : storageSlotItems) {
            availableSlots.addAll(item.getSlots());
        }
        
        // Remplir les slots avec les items du storage
        int slotIndex = 0;
        for (Map.Entry<String, StorageItem> entry : storageItems.entrySet()) {
            if (slotIndex >= availableSlots.size()) {
                break; // Plus de slots disponibles
            }
            
            StorageItem storageItem = entry.getValue();
            int slot = availableSlots.get(slotIndex);
            
            // Créer l'ItemStack à afficher
            ItemStack displayItem = createStorageDisplayItem(storageItem, loreAppendConfig);
            if (displayItem != null) {
                inventory.setItem(slot, displayItem);
                slotIndex++;
            }
        }
    }

    /**
     * Crée un ItemStack pour afficher un item du storage avec le lore appendé
     * @param storageItem L'item du storage
     * @param loreAppendConfig Configuration du lore à ajouter
     * @return L'ItemStack à afficher
     */
    private ItemStack createStorageDisplayItem(StorageItem storageItem, LoreAppendConfig loreAppendConfig) {
        ItemStack itemStack;
        
        // Créer l'item (Nexo ou Minecraft)
        if (storageItem.isNexoItem()) {
            try {
                itemStack = com.nexomc.nexo.api.NexoItems.itemFromId(storageItem.getItemId()).build();
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("Item Nexo introuvable: " + storageItem.getItemId());
                return null;
            }
        } else {
            try {
                org.bukkit.Material material = org.bukkit.Material.valueOf(storageItem.getItemId().toUpperCase());
                itemStack = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                plugin.getQuantumLogger().warning("Matériau Minecraft invalide: " + storageItem.getItemId());
                return null;
            }
        }
        
        // Ajouter le lore avec quantité et prix
        if (loreAppendConfig != null) {
            appendStorageLore(itemStack, storageItem, loreAppendConfig);
        }
        
        return itemStack;
    }

    /**
     * Ajoute le lore de quantité et prix à l'item
     * @param itemStack L'ItemStack
     * @param storageItem L'item du storage
     * @param config Configuration du lore
     */
    private void appendStorageLore(ItemStack itemStack, StorageItem storageItem, LoreAppendConfig config) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Ajouter une ligne vide pour séparer
        if (!lore.isEmpty()) {
            lore.add("");
        }
        
        // Remplacer les placeholders dans chaque ligne de lore
        for (String loreLine : config.getLoreLines()) {
            String processedLine = processLorePlaceholders(loreLine, storageItem);
            lore.add(ChatColor.translateAlternateColorCodes('&', processedLine));
        }
        
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * Remplace les placeholders dans une ligne de lore
     * @param line La ligne de lore
     * @param storageItem L'item du storage
     * @return La ligne avec placeholders remplacés
     */
    private String processLorePlaceholders(String line, StorageItem storageItem) {
        String processed = line;
        
        // %quantity% - Quantité d'items stockés
        processed = processed.replace("%quantity%", formatQuantity(storageItem.getAmount()));
        
        // %price% - Prix unitaire de l'item
        String itemId = storageItem.isNexoItem() ? storageItem.getItemId() : storageItem.getItemId().toUpperCase();
        double price = priceManager.getPrice(itemId);
        processed = processed.replace("%price%", priceManager.formatPrice(price));
        
        // %total_price% - Prix total (quantité * prix unitaire)
        double totalPrice = storageItem.getAmount() * price;
        processed = processed.replace("%total_price%", priceManager.formatPrice(totalPrice));
        
        return processed;
    }

    /**
     * Formate la quantité d'items (avec séparateurs de milliers)
     * @param quantity La quantité
     * @return Chaîne formatée
     */
    private String formatQuantity(long quantity) {
        if (quantity >= 1_000_000_000) {
            return String.format("§a%.2fB", quantity / 1_000_000_000.0);
        } else if (quantity >= 1_000_000) {
            return String.format("§a%.2fM", quantity / 1_000_000.0);
        } else if (quantity >= 1_000) {
            return String.format("§a%.2fK", quantity / 1_000.0);
        } else {
            return "§a" + quantity;
        }
    }

    /**
     * Configuration du lore à ajouter aux items du storage
     */
    public static class LoreAppendConfig {
        private final List<String> loreLines;
        
        public LoreAppendConfig(List<String> loreLines) {
            this.loreLines = loreLines != null ? loreLines : new ArrayList<>();
        }
        
        public List<String> getLoreLines() {
            return loreLines;
        }
    }
}
