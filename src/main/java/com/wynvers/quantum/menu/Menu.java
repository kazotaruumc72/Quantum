package com.wynvers.quantum.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.orders.OrderCreationSession;
import com.wynvers.quantum.sell.SellSession;
import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Menu {
 
    private final String id;
    private final Quantum plugin;
    private String title;
    private int size;
    private String openCommand;
 
    // Animated title
    private boolean animatedTitle;
    private List<String> titleFrames;
    private int titleSpeed;
 
    // Items
    private final Map<String, MenuItem> items;
    
    // Storage renderer pour slots quantum_storage
    private StorageRenderer storageRenderer;
    
    // Tower Storage renderer pour slots quantum_tower_storage
    private TowerStorageRenderer towerStorageRenderer;
    
    // MiniMessage parser pour les titres modernes
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
 
    public Menu(Quantum plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.size = 54;
        this.items = new HashMap<>();
        this.titleFrames = new ArrayList<>();
        this.titleSpeed = 10;
        this.storageRenderer = new StorageRenderer(plugin);
        this.towerStorageRenderer = new TowerStorageRenderer(plugin);
    }
 
    // === GETTERS ===
 
    public String getId() {
        return id;
    }
 
    public String getTitle() {
        return title;
    }
 
    public int getSize() {
        return size;
    }
 
    public String getOpenCommand() {
        return openCommand;
    }
 
    public boolean hasAnimatedTitle() {
        return animatedTitle;
    }
    
    public boolean isAnimatedTitle() {
        return animatedTitle;
    }
 
    public List<String> getTitleFrames() {
        return titleFrames;
    }
 
    public int getTitleSpeed() {
        return titleSpeed;
    }
 
    public Map<String, MenuItem> getItems() {
        return items;
    }
 
    public MenuItem getItem(String id) {
        return items.get(id);
    }
    
    /**
     * Get menu item at specific slot
     */
    public MenuItem getItemAt(int slot) {
        for (MenuItem item : items.values()) {
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }
 
    // === SETTERS ===
 
    public void setTitle(String title) {
        this.title = title;
    }
 
    public void setSize(int size) {
        // Validate size (must be multiple of 9, between 9 and 54)
        if (size % 9 != 0 || size < 9 || size > 54) {
            this.size = 54;
        } else {
            this.size = size;
        }
    }
 
    public void setOpenCommand(String openCommand) {
        this.openCommand = openCommand;
    }
 
    public void setAnimatedTitle(boolean animatedTitle) {
        this.animatedTitle = animatedTitle;
    }
 
    public void setTitleFrames(List<String> titleFrames) {
        this.titleFrames = titleFrames;
    }
 
    public void setTitleSpeed(int titleSpeed) {
        this.titleSpeed = Math.max(1, titleSpeed);
    }
 
    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
    }
    
    /**
     * Parse title avec support MiniMessage + Legacy
     * Si le titre contient '<', c'est du MiniMessage
     * Sinon, c'est du legacy avec '&'
     */
    private String parseTitle(String rawTitle) {
        if (rawTitle == null) return "Menu";
        
        // Détecter MiniMessage (présence de '<')
        if (rawTitle.contains("<")) {
            try {
                Component component = miniMessage.deserialize(rawTitle);
                return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacySection()
                    .serialize(component);
            } catch (Exception e) {
                // Fallback sur legacy si erreur
                return ChatColor.translateAlternateColorCodes('&', rawTitle);
            }
        } else {
            // Legacy color codes
            return ChatColor.translateAlternateColorCodes('&', rawTitle);
        }
    }
    
    /**
     * Open this menu for a player
     */
    public void open(Player player, Quantum plugin) {
        open(player, plugin, null);
    }
    
    /**
     * Open this menu for a player avec placeholders personnalisés
     */
    public void open(Player player, Quantum plugin, Map<String, String> customPlaceholders) {
        plugin.getMenuManager().setActiveMenu(player, this);
        
        String parsedTitle = customPlaceholders != null 
            ? plugin.getPlaceholderManager().parse(player, title, customPlaceholders)
            : plugin.getPlaceholderManager().parse(player, title);
        
        // PATCH: Appliquer MiniMessage + Legacy
        parsedTitle = parseTitle(parsedTitle);
        
        Inventory inventory = Bukkit.createInventory(null, size, parsedTitle);
 
        populateInventory(inventory, player, customPlaceholders);
        
        player.openInventory(inventory);
        
        if (animatedTitle && titleFrames != null && !titleFrames.isEmpty()) {
            plugin.getAnimationManager().startAnimation(player, titleFrames, titleSpeed);
        }
    }
    
    /**
     * Rafraîchit le menu sans le fermer
     * Utile pour mettre à jour le contenu dynamique (mode de stockage, items, etc.)
     */
    public void refresh(Player player, Quantum plugin) {
        refresh(player, plugin, null);
    }
    
    /**
     * Rafraîchit le menu avec placeholders personnalisés
     */
    public void refresh(Player player, Quantum plugin, Map<String, String> customPlaceholders) {
        // Récupérer l'inventaire actuellement ouvert
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        
        // Vérifier que c'est bien ce menu qui est ouvert
        if (currentInventory == null || currentInventory.getSize() != size) {
            return;
        }
        
        // Repeupler l'inventaire avec les données à jour
        populateInventory(currentInventory, player, customPlaceholders);
    }
    
    // Additional methods needed by MenuManager
 
    public MenuItem getMenuItem(int slot) {
        // Find the MenuItem that contains this slot
        for (MenuItem item : items.values()) {
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }
 
    public List<String> getAnimatedTitles() {
        return titleFrames;
    }
 
    public long getTitleUpdateInterval() {
        return titleSpeed;
    }
 
    public void updateTitle(Player player, String newTitle) {
        // Note: Bukkit doesn't support dynamically updating inventory titles
        // This is a limitation of the Bukkit API
    }
 
    /**
     * Remplit l'inventaire avec les items du menu
     * Gère aussi les slots quantum_storage avec le StorageRenderer
     */
    public void populateInventory(Inventory inventory) {
        populateInventory(inventory, null);
    }
    
    /**
     * Remplit l'inventaire avec les items du menu pour un joueur spécifique
     */
    public void populateInventory(Inventory inventory, Player player) {
        populateInventory(inventory, player, null);
    }
    
    /**
     * Récupère tous les ordres d'une catégorie depuis orders.yml
     * @param category Nom de la catégorie (ex: "autre", "minerais", etc.)
     * @return Liste des ordres sous forme de Map
     */
    private List<Map<String, Object>> getOrdersForCategory(String category) {
        List<Map<String, Object>> orders = new ArrayList<>();
        
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            return orders;
        }
        
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        
        ConfigurationSection categorySection = ordersConfig.getConfigurationSection(category);
        if (categorySection == null) {
            return orders;
        }
        
        // Parcourir tous les ordres de cette catégorie
        for (String orderId : categorySection.getKeys(false)) {
            String path = category + "." + orderId;
            
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("orderer", ordersConfig.getString(path + ".orderer", "Unknown"));
            orderData.put("orderer_uuid", ordersConfig.getString(path + ".orderer_uuid"));
            orderData.put("item", ordersConfig.getString(path + ".item", "minecraft:stone"));
            orderData.put("quantity", ordersConfig.getInt(path + ".quantity", 0));
            orderData.put("price_per_unit", ordersConfig.getDouble(path + ".price_per_unit", 0.0));
            orderData.put("total_price", ordersConfig.getDouble(path + ".total_price", 0.0));
            orderData.put("created_at", ordersConfig.getLong(path + ".created_at", System.currentTimeMillis()));
            
            orders.add(orderData);
        }
        
        // ✅ TRI PAR NOM D'ITEM (alphabétique), PUIS PAR PRIX (du plus cher au moins cher)
        orders.sort((o1, o2) -> {
            String item1 = (String) o1.get("item");
            String item2 = (String) o2.get("item");
            
            // D'abord comparer les noms d'items
            int itemComparison = item1.compareTo(item2);
            if (itemComparison != 0) {
                return itemComparison; // Items différents -> tri alphabétique
            }
            
            // Même item -> trier par prix total (du plus cher au moins cher)
            double price1 = (double) o1.get("total_price");
            double price2 = (double) o2.get("total_price");
            return Double.compare(price2, price1); // Ordre décroissant
        });
        
        return orders;
    }
    
    /**
     * Extrait le nom de catégorie depuis l'ID du menu
     * Ex: "orders_autre" -> "autre"
     */
    private String getCategoryFromMenuId() {
        if (id.startsWith("orders_")) {
            return id.substring(7); // Enlever "orders_"
        }
        return null;
    }
    
    /**
     * Crée un ItemStack pour un ordre
     */
    private ItemStack createOrderItemStack(Map<String, Object> orderData, MenuItem menuItem, Player player) {
        String itemId = (String) orderData.get("item");
        int quantity = (int) orderData.get("quantity");
        double pricePerUnit = (double) orderData.get("price_per_unit");
        double totalPrice = (double) orderData.get("total_price");
        String orderer = (String) orderData.get("orderer");
        
        ItemStack itemStack;
        String displayName;
        
        // Créer l'item (Nexo ou vanilla)
        if (itemId.startsWith("nexo:")) {
            String nexoId = itemId.substring(5);
            itemStack = NexoItems.itemFromId(nexoId).build();
            if (itemStack == null) {
                itemStack = new ItemStack(Material.STONE);
            }
        } else if (itemId.startsWith("minecraft:")) {
            String materialName = itemId.substring(10).toUpperCase();
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.STONE;
            }
            itemStack = new ItemStack(material);
        } else {
            itemStack = new ItemStack(Material.STONE);
        }
        
        // Obtenir le nom de l'item (Nexo ou vanilla)
        ItemMeta baseMeta = itemStack.getItemMeta();
        if (baseMeta != null && baseMeta.hasDisplayName()) {
            displayName = baseMeta.getDisplayName();
        } else {
            // Formater joliment le nom de l'item
            if (itemId.startsWith("nexo:")) {
                displayName = ChatColor.WHITE + itemId.substring(5).replace("_", " ");
            } else {
                displayName = ChatColor.WHITE + itemId.substring(10).replace("_", " ");
            }
        }
        
        // Appliquer les métadonnées du menu si disponibles
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // PATCH: NE PAS écraser le display name Nexo - le conserver tel quel
            // (Le displayName Nexo est déjà dans l'item)
            
            // Créer la lore avec les informations de l'ordre
            List<String> lore = new ArrayList<>();
            
            // Conserver la lore Nexo existante
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
            }
            
            // Si le MenuItem a une lore, l'utiliser comme base
            if (menuItem.getLore() != null && !menuItem.getLore().isEmpty()) {
                lore.addAll(plugin.getPlaceholderManager().parse(player, menuItem.getLore()));
            } else {
                // Lore par défaut
                lore.add("");
                lore.add(ChatColor.GRAY + "Quantité recherchée: " + ChatColor.WHITE + quantity);
                lore.add(ChatColor.GRAY + "Prix unitaire: " + ChatColor.GOLD + String.format("%.2f", pricePerUnit) + "$");
                lore.add(ChatColor.GRAY + "Prix total: " + ChatColor.GREEN + String.format("%.2f", totalPrice) + "$");
                lore.add("");
                lore.add(ChatColor.YELLOW + "⚡ Commandé par: " + ChatColor.WHITE + orderer);
                lore.add("");
                lore.add(ChatColor.AQUA + "» Clic droit: Vendre (mode Vente)");
            }
            
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }
    
    /**
     * Remplit l'inventaire avec les items du menu pour un joueur spécifique avec placeholders
     */
    public void populateInventory(Inventory inventory, Player player, Map<String, String> customPlaceholders) {
        inventory.clear();
        
        // Récupérer la session sell si elle existe
        SellSession sellSession = player != null ? plugin.getSellManager().getSession(player) : null;
        
        // Récupérer la session order si elle existe
        OrderCreationSession orderSession = player != null ? plugin.getOrderCreationManager().getSession(player) : null;
        
        // Détecter si c'est un menu d'ordres (orders_autre, orders_minerais, etc.)
        String category = getCategoryFromMenuId();
        List<Map<String, Object>> categoryOrders = category != null ? getOrdersForCategory(category) : new ArrayList<>();
        
        // Index pour parcourir les ordres
        int orderIndex = 0;
        
        // Premièrement, remplir les items standards (non-quantum_storage)
        for (MenuItem item : items.values()) {
            if (item.getSlots().isEmpty()) continue;

            // Vérifier les view requirements (ex: permission admin pour bouton reset)
            if (player != null && !item.getViewRequirements().isEmpty()) {
                boolean visible = true;
                for (Requirement req : item.getViewRequirements()) {
                    if (!req.check(player, plugin)) {
                        visible = false;
                        break;
                    }
                }
                if (!visible) continue;
            }
            
            // Si c'est un slot quantum_storage, le StorageRenderer s'en occupera
            if (item.isQuantumStorage()) {
                continue;
            }

            // Si c'est un slot quantum_tower_storage, le TowerStorageRenderer s'en occupera
            if (item.isQuantumTowerStorage()) {
                continue;
            }
            
            // === NOUVEAU: Gestion des QUANTUM_ORDERS_ITEM ===
            // Si c'est un quantum_orders_item, afficher les ordres de la catégorie
            if (item.getButtonType() == ButtonType.QUANTUM_ORDERS_ITEM) {
                // Pour chaque slot configuré, essayer de placer un ordre
                for (int slot : item.getSlots()) {
                    if (slot >= 0 && slot < size && orderIndex < categoryOrders.size()) {
                        Map<String, Object> orderData = categoryOrders.get(orderIndex);
                        ItemStack orderItem = createOrderItemStack(orderData, item, player);
                        inventory.setItem(slot, orderItem);
                        orderIndex++;
                    }
                }
                continue;
            }
            
            // Si c'est un quantum_sell_item ET qu'il y a une session, utiliser l'item de la session
            if (item.getButtonType() == ButtonType.QUANTUM_SELL_ITEM && sellSession != null) {
                ItemStack sellItem = sellSession.getItemToSell().clone();
                
                // Définir le nombre d'items (max 64 si plus)
                int displayAmount = Math.min(sellSession.getQuantity(), 64);
                sellItem.setAmount(displayAmount);
                
                // Parser les placeholders dans le display name et lore si présents dans le MenuItem
                ItemMeta meta = sellItem.getItemMeta();
                if (meta != null) {
                    // Utiliser le display name et lore du MenuItem s'ils existent
                    if (item.getDisplayName() != null) {
                        String parsedName = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, item.getDisplayName(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, item.getDisplayName());
                        meta.setDisplayName(parsedName);
                    }
                    
                    if (item.getLore() != null && !item.getLore().isEmpty()) {
                        List<String> parsedLore = customPlaceholders != null
                            ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                            : plugin.getPlaceholderManager().parse(player, item.getLore());
                        meta.setLore(parsedLore);
                    }
                    
                    sellItem.setItemMeta(meta);
                }
                
                // Placer l'item dans tous les slots configurés
                for (int slot : item.getSlots()) {
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, sellItem.clone());
                    }
                }
                
                continue;
            }
            
            // === PATCH: QUANTUM_ORDER_DISPLAY_ITEM (Enhanced with full Nexo support) ===
            if (item.getButtonType() == ButtonType.QUANTUM_ORDER_DISPLAY_ITEM && orderSession != null) {
                ItemStack orderItem = orderSession.getDisplayItem();
                
                if (orderItem != null) {
                    // Détecter si c'est un item Nexo
                    String nexoId = NexoItems.idFromItem(orderItem);
                    ItemStack finalItem;
                    
                    if (nexoId != null) {
                        // === C'est un item NEXO ===
                        // Cloner l'item pour préserver TOUTES les métadonnées Nexo
                        finalItem = orderItem.clone();
                        finalItem.setAmount(1);
                        
                        // Ajouter UNIQUEMENT la lore sans toucher au display name ni autres métadonnées
                        if (item.getLore() != null && !item.getLore().isEmpty()) {
                            ItemMeta meta = finalItem.getItemMeta();
                            if (meta != null) {
                                // Parser la lore du menu
                                List<String> parsedLore = customPlaceholders != null
                                    ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                                    : plugin.getPlaceholderManager().parse(player, item.getLore());
                                
                                // Récupérer la lore existante de Nexo
                                List<String> existingLore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                                
                                // Combiner: lore Nexo + lore du menu
                                existingLore.addAll(parsedLore);
                                meta.setLore(existingLore);
                                
                                finalItem.setItemMeta(meta);
                            }
                        }
                    } else {
                        // === Item VANILLA ===
                        finalItem = orderItem.clone();
                        finalItem.setAmount(1);
                        
                        ItemMeta meta = finalItem.getItemMeta();
                        if (meta != null) {
                            // Pour vanilla, appliquer la lore du menu
                            if (item.getLore() != null && !item.getLore().isEmpty()) {
                                List<String> parsedLore = customPlaceholders != null
                                    ? plugin.getPlaceholderManager().parse(player, item.getLore(), customPlaceholders)
                                    : plugin.getPlaceholderManager().parse(player, item.getLore());
                                meta.setLore(parsedLore);
                            }
                            
                            finalItem.setItemMeta(meta);
                        }
                    }
                    
                    // Placer l'item dans tous les slots configurés
                    for (int slot : item.getSlots()) {
                        if (slot >= 0 && slot < size) {
                            inventory.setItem(slot, finalItem.clone());
                        }
                    }
                    
                    continue;
                }
            }
 
            // Gestion dynamique des boutons sell_all (teinture verte/rouge selon permission)
            if (player != null && (item.getButtonType() == ButtonType.QUANTUM_STORAGE_SELL_ALL
                    || item.getButtonType() == ButtonType.QUANTUM_TOWER_STORAGE_SELL_ALL)) {
                boolean hasPerm = player.hasPermission("quantum.storage.sellall");
                Material dyeMaterial = hasPerm ? Material.GREEN_DYE : Material.RED_DYE;
                ItemStack sellAllItem = new ItemStack(dyeMaterial);
                ItemMeta sellMeta = sellAllItem.getItemMeta();
                if (sellMeta != null) {
                    String name = item.getDisplayName() != null
                            ? ChatColor.translateAlternateColorCodes('&', item.getDisplayName())
                            : (hasPerm ? "§a§lVendre tout" : "§c§lVendre tout");
                    sellMeta.setDisplayName(parsePlaceholder(player, name, customPlaceholders));
                    if (item.getLore() != null && !item.getLore().isEmpty()) {
                        List<String> parsedLore = parsePlaceholders(player, item.getLore(), customPlaceholders);
                        if (!hasPerm) {
                            parsedLore = new ArrayList<>(parsedLore);
                            parsedLore.add("§c§l✗ §cVous n'avez pas la permission.");
                        }
                        sellMeta.setLore(parsedLore);
                    }
                    sellAllItem.setItemMeta(sellMeta);
                }
                for (int slot : item.getSlots()) {
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, sellAllItem.clone());
                    }
                }
                continue;
            }

            // Créer l'ItemStack depuis le MenuItem
            ItemStack itemStack = item.toItemStack(plugin, player, customPlaceholders);
            if (itemStack == null) continue;
            
            // Parse placeholders in display name and lore if a player is provided
            if (player != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    // Parse display name
                    if (meta.hasDisplayName()) {
                        String parsedName = parsePlaceholder(player, meta.getDisplayName(), customPlaceholders);
                        meta.setDisplayName(parsedName);
                    }
                    
                    // Parse lore
                    if (meta.hasLore()) {
                        List<String> parsedLore = parsePlaceholders(player, meta.getLore(), customPlaceholders);
                        meta.setLore(parsedLore);
                    }
                    
                    itemStack.setItemMeta(meta);
                }
            }
 
            // Place the item in all configured slots
            for (int slot : item.getSlots()) {
                if (slot >= 0 && slot < size) {
                    // Check if the item has a material placeholder with {slot}
                    String matPlaceholder = item.getMaterialPlaceholder();
                    ItemStack slotItemStack;
                    
                    if (matPlaceholder != null && matPlaceholder.contains("{slot}")) {
                        // Create a new item with slot-specific material
                        slotItemStack = item.toItemStack(plugin, player, customPlaceholders, slot);
                        
                        // Parse placeholders in display name and lore, expanding {slot}
                        if (slotItemStack != null && player != null) {
                            ItemMeta meta = slotItemStack.getItemMeta();
                            if (meta != null) {
                                // Expand and parse display name
                                if (meta.hasDisplayName()) {
                                    String expandedName = meta.getDisplayName().replace("{slot}", String.valueOf(slot));
                                    String parsedName = parsePlaceholder(player, expandedName, customPlaceholders);
                                    meta.setDisplayName(parsedName);
                                }
                                
                                // Expand and parse lore
                                if (meta.hasLore()) {
                                    List<String> expandedLore = new ArrayList<>();
                                    for (String loreLine : meta.getLore()) {
                                        expandedLore.add(loreLine.replace("{slot}", String.valueOf(slot)));
                                    }
                                    List<String> parsedLore = parsePlaceholders(player, expandedLore, customPlaceholders);
                                    meta.setLore(parsedLore);
                                }
                                
                                slotItemStack.setItemMeta(meta);
                            }
                        }
                    } else {
                        // Use the already-parsed itemStack
                        slotItemStack = itemStack.clone();
                    }
                    
                    if (slotItemStack != null) {
                        inventory.setItem(slot, slotItemStack);
                    }
                }
            }
        }
        
        // Ensuite, remplir les slots quantum_storage avec les items du joueur
        if (player != null) {
            if (hasQuantumStorageSlots()) {
                renderStorageSlots(player, inventory);
            }
            if (hasQuantumTowerStorageSlots()) {
                renderTowerStorageSlots(player, inventory);
            }
        }
    }
    
    /**
     * Parse placeholders with optional custom placeholder map
     */
    private String parsePlaceholder(Player player, String text, Map<String, String> customPlaceholders) {
        return customPlaceholders != null
            ? plugin.getPlaceholderManager().parse(player, text, customPlaceholders)
            : plugin.getPlaceholderManager().parse(player, text);
    }
    
    /**
     * Parse placeholders in a list with optional custom placeholder map
     */
    private List<String> parsePlaceholders(Player player, List<String> texts, Map<String, String> customPlaceholders) {
        return customPlaceholders != null
            ? plugin.getPlaceholderManager().parse(player, texts, customPlaceholders)
            : plugin.getPlaceholderManager().parse(player, texts);
    }
    
    /**
     * Returns true if this menu has at least one quantum_storage type slot
     */
    private boolean hasQuantumStorageSlots() {
        for (MenuItem item : items.values()) {
            if (item.isQuantumStorage()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this menu has at least one quantum_tower_storage type slot
     */
    private boolean hasQuantumTowerStorageSlots() {
        for (MenuItem item : items.values()) {
            if (item.isQuantumTowerStorage()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Render storage slots for a player
     */
    private void renderStorageSlots(Player player, Inventory inventory) {
        StorageRenderer.LoreAppendConfig loreConfig = null;
        
        for (MenuItem item : items.values()) {
            if (item.isQuantumStorage() && item.getLoreAppend() != null && !item.getLoreAppend().isEmpty()) {
                loreConfig = new StorageRenderer.LoreAppendConfig(item.getLoreAppend());
                break;
            }
        }
        
        storageRenderer.renderStorageSlots(player, inventory, this, loreConfig);
    }

    /**
     * Render tower storage slots for a player
     */
    private void renderTowerStorageSlots(Player player, Inventory inventory) {
        TowerStorageRenderer.LoreAppendConfig loreConfig = null;

        for (MenuItem item : items.values()) {
            if (item.isQuantumTowerStorage() && item.getLoreAppend() != null && !item.getLoreAppend().isEmpty()) {
                loreConfig = new TowerStorageRenderer.LoreAppendConfig(item.getLoreAppend());
                break;
            }
        }

        towerStorageRenderer.renderStorageSlots(player, inventory, this, loreConfig);
    }
}