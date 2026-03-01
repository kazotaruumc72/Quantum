package com.wynvers.quantum.menu;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.managers.PriceManager;
import com.wynvers.quantum.storage.PlayerStorage;
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
 * Gère le rendu des items du storage dans les slots quantum_storage
 */
public class StorageRenderer {
    
    private final Quantum plugin;
    private final PriceManager priceManager;
    private final NamespacedKey itemIdKey;
    
    public StorageRenderer(Quantum plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
        this.itemIdKey = new NamespacedKey(plugin, "quantum_item_id");
    }
    
    /**
     * Clé pour accéder au itemId depuis un ItemStack
     */
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
     * Remplit les slots quantum_storage avec les items du joueur
     */
    public void renderStorageSlots(Player player, Inventory inventory, Menu menu, LoreAppendConfig loreConfig) {
        // Récupérer le storage du joueur
        PlayerStorage storage = plugin.getStorageManager().getStorage(player.getUniqueId());
        if (storage == null) {
            return;
        }
        
        // Collecter tous les items du storage
        List<StorageItemDisplay> items = new ArrayList<>();
        
        // Items vanilla
        for (Map.Entry<Material, Integer> entry : storage.getVanillaItems().entrySet()) {
            items.add(new StorageItemDisplay(
                entry.getKey(),
                null,
                entry.getValue()
            ));
        }
        
        // Items Nexo
        for (Map.Entry<String, Integer> entry : storage.getNexoItems().entrySet()) {
            items.add(new StorageItemDisplay(
                null,
                entry.getKey(),
                entry.getValue()
            ));
        }
        
        // Trouver les slots quantum_storage
        List<Integer> quantumSlots = new ArrayList<>();
        for (MenuItem menuItem : menu.getItems().values()) {
            if (menuItem.isQuantumStorage()) {
                quantumSlots.addAll(menuItem.getSlots());
            }
        }
        
        // Vérifier si le GUI est plein (tous les slots remplis)
        if (items.size() >= quantumSlots.size() && quantumSlots.size() > 0) {
            // Envoyer titre + sous-titre depuis messages.yml
            String title = plugin.getMessagesManager().get("storage.full-title", false);
            String subtitle = plugin.getMessagesManager().get("storage.full-subtitle", false);
            
            // Envoyer le titre (1.20.6 compatible)
            player.sendTitle(title, subtitle, 10, 70, 20);
        }
        
        // Remplir les slots avec les items
        int index = 0;
        for (int slot : quantumSlots) {
            if (index >= items.size()) {
                break; // Plus d'items à afficher
            }
            
            StorageItemDisplay item = items.get(index);
            ItemStack displayStack = createDisplayItem(player, item, loreConfig);
            
            if (displayStack != null) {
                inventory.setItem(slot, displayStack);
            }
            
            index++;
        }
    }
    
    /**
     * Crée l'ItemStack d'affichage avec le lore personnalisé
     */
    private ItemStack createDisplayItem(Player player, StorageItemDisplay item, LoreAppendConfig loreConfig) {
        ItemStack stack;
        String itemId;
        
        // Créer l'item (Nexo ou vanilla)
        if (item.nexoId != null) {
            // Utiliser l'API Nexo selon la documentation officielle
            try {
                // NexoItems.itemFromId retourne un ItemBuilder
                com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(item.nexoId);
                
                if (itemBuilder == null) {
                    plugin.getQuantumLogger().warning("Nexo ItemBuilder is null for item: " + item.nexoId);
                    return null;
                }
                
                // Construire l'ItemStack avec build()
                stack = itemBuilder.build();
                
                if (stack == null) {
                    plugin.getQuantumLogger().warning("Failed to build Nexo item: " + item.nexoId);
                    return null;
                }
                
                itemId = "nexo:" + item.nexoId;
            } catch (Exception e) {
                plugin.getQuantumLogger().warning("Failed to create Nexo item: " + item.nexoId + " - " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else if (item.material != null) {
            stack = new ItemStack(item.material);
            itemId = "minecraft:" + item.material.name().toLowerCase();
        } else {
            return null;
        }
        
        // Ajouter le lore personnalisé ET stocker l'itemId dans PersistentDataContainer
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // STOCKER L'ITEM ID DANS LE PDC
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, itemId);
            
            // Ajouter le lore avec les placeholders remplacés
            if (loreConfig != null && loreConfig.getLoreTemplate() != null) {
                List<String> currentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                
                // Ajouter une ligne vide avant le nouveau lore
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
    
    /**
     * Remplace les placeholders dans le lore
     * 
     * PATCH: Extraire l'ID propre pour PriceManager
     * PriceManager attend:
     * - Nexo: "afzelia_bark" (PAS "nexo:afzelia_bark")
     * - Vanilla: "stone" (PAS "minecraft:stone")
     */
    private String replacePlaceholders(String text, StorageItemDisplay item, Player player) {
        String result = text;
        
        // %quantity% - Quantité formatée
        result = result.replace("%quantity%", formatNumber(item.quantity));
        
        // === PATCH: EXTRACTION CORRECTE DE L'ID POUR PRICE LOOKUP ===
        String priceKey;
        if (item.nexoId != null) {
            // Nexo: utiliser juste l'ID sans le préfixe "nexo:"
            priceKey = item.nexoId;
        } else {
            // Vanilla: utiliser juste le nom du material sans "minecraft:"
            priceKey = item.material.name().toLowerCase();
        }
        
        // Appliquer le multiplicateur de vente du joueur
        double multiplier = plugin.getStorageUpgradeManager().getSellMultiplier(player);
        
        // %price% - Prix unitaire (avec multiplicateur)
        double price = priceManager.getPrice(priceKey) * multiplier;
        result = result.replace("%price%", priceManager.formatPrice(price));
        
        // %total_price% - Prix total (avec multiplicateur)
        double totalPrice = price * item.quantity;
        result = result.replace("%total_price%", priceManager.formatPrice(totalPrice));
        
        return result;
    }
    
    /**
     * Formate un nombre avec K, M, B pour les grands nombres
     */
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
    
    /**
     * Classe interne pour représenter un item du storage à afficher
     */
    private static class StorageItemDisplay {
        private final Material material;
        private final String nexoId;
        private final int quantity;
        
        public StorageItemDisplay(Material material, String nexoId, int quantity) {
            this.material = material;
            this.nexoId = nexoId;
            this.quantity = quantity;
        }
    }
}
