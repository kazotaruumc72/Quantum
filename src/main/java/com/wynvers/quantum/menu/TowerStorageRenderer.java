package com.wynvers.quantum.menu;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.PriceManager;
import com.wynvers.quantum.towers.storage.PlayerTowerStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Gère le rendu des items du tower storage dans les slots quantum_tower_storage
 */
public class TowerStorageRenderer {

    private final Quantum plugin;
    private final PriceManager priceManager;
    private final NamespacedKey itemIdKey;

    public TowerStorageRenderer(Quantum plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
        this.itemIdKey = new NamespacedKey(plugin, "tower_quantum_item_id");
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }

    /**
     * Configuration pour lore_append
     */
    public static class LoreAppendConfig {
        private final List<String> loreTemplate;

        public LoreAppendConfig(List<String> loreTemplate) {
            this.loreTemplate = loreTemplate;
        }

        public List<String> getLoreTemplate() {
            return loreTemplate;
        }
    }

    /**
     * Remplit les slots quantum_tower_storage avec les items du joueur
     */
    public void renderStorageSlots(Player player, Inventory inventory, Menu menu, LoreAppendConfig loreConfig) {
        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(player.getUniqueId());
        if (storage == null) {
            return;
        }

        List<TowerStorageItemDisplay> items = new ArrayList<>();

        for (Map.Entry<Material, Integer> entry : storage.getVanillaItems().entrySet()) {
            items.add(new TowerStorageItemDisplay(entry.getKey(), null, entry.getValue()));
        }

        for (Map.Entry<String, Integer> entry : storage.getNexoItems().entrySet()) {
            items.add(new TowerStorageItemDisplay(null, entry.getKey(), entry.getValue()));
        }

        List<Integer> quantumSlots = new ArrayList<>();
        for (MenuItem menuItem : menu.getItems().values()) {
            if (menuItem.isQuantumTowerStorage()) {
                quantumSlots.addAll(menuItem.getSlots());
            }
        }

        int index = 0;
        for (int slot : quantumSlots) {
            if (index >= items.size()) {
                break;
            }

            TowerStorageItemDisplay item = items.get(index);
            ItemStack displayStack = createDisplayItem(player, item, loreConfig);

            if (displayStack != null) {
                inventory.setItem(slot, displayStack);
            }

            index++;
        }
    }

    private ItemStack createDisplayItem(Player player, TowerStorageItemDisplay item, LoreAppendConfig loreConfig) {
        ItemStack stack;
        String itemId;

        if (item.nexoId != null) {
            try {
                com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(item.nexoId);

                if (itemBuilder == null) {
                    plugin.getQuantumLogger().warning("Nexo ItemBuilder is null for tower storage item: " + item.nexoId);
                    return null;
                }

                stack = itemBuilder.build();

                if (stack == null) {
                    plugin.getQuantumLogger().warning("Failed to build Nexo tower storage item: " + item.nexoId);
                    return null;
                }

                itemId = "nexo:" + item.nexoId;
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("Failed to create Nexo tower storage item: " + item.nexoId + " - " + e.getMessage());
                return null;
            }
        } else if (item.material != null) {
            stack = new ItemStack(item.material);
            itemId = "minecraft:" + item.material.name().toLowerCase();
        } else {
            return null;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, itemId);

            if (loreConfig != null && loreConfig.getLoreTemplate() != null) {
                List<String> currentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

                if (!currentLore.isEmpty()) {
                    currentLore.add("");
                }

                for (String loreLine : loreConfig.getLoreTemplate()) {
                    String processedLine = replacePlaceholders(loreLine, item, player);
                    currentLore.add(ChatColor.translateAlternateColorCodes('&', processedLine));
                }

                meta.setLore(currentLore);
            }

            stack.setItemMeta(meta);
        }

        return stack;
    }

    private String replacePlaceholders(String text, TowerStorageItemDisplay item, Player player) {
        String result = text;

        result = result.replace("%quantity%", formatNumber(item.quantity));

        String priceKey;
        if (item.nexoId != null) {
            priceKey = item.nexoId;
        } else {
            priceKey = item.material.name().toLowerCase();
        }

        // Appliquer le multiplicateur de vente du joueur
        double multiplier = plugin.getTowerStorageUpgradeManager().getSellMultiplier(player);

        double price = priceManager.getPrice(priceKey) * multiplier;
        result = result.replace("%price%", priceManager.formatPrice(price));

        double totalPrice = price * item.quantity;
        result = result.replace("%total_price%", priceManager.formatPrice(totalPrice));

        return result;
    }

    private String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1_000_000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1_000_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else {
            return String.format("%.1fB", number / 1_000_000_000.0);
        }
    }

    private static class TowerStorageItemDisplay {
        private final Material material;
        private final String nexoId;
        private final int quantity;

        public TowerStorageItemDisplay(Material material, String nexoId, int quantity) {
            this.material = material;
            this.nexoId = nexoId;
            this.quantity = quantity;
        }
    }
}
