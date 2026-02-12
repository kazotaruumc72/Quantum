package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.sell.SellSession;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItem {
    
    private final String id;
    private final List<Integer> slots;
    
    // Item properties
    private Material material;
    private String materialString; // For placeholder materials like %quantum_history_{slot}_material%
    private String materialPlaceholder; // Store raw material string for placeholder resolution
    private String nexoId;
    private int itemAmount;
    private String displayName;
    private List<String> lore;
    private String skullOwner;
    private int customModelData;
    private boolean glow;
    private List<ItemFlag> hideFlags;
    
    // Type de slot (quantum_storage, etc.)
    private String type;
    
    // Button type (QUANTUM_CHANGE_MODE, QUANTUM_CHANGE_AMOUNT, etc.)
    private ButtonType buttonType;
    
    // Parameters pour le button type (ex: amount: 5, percentage: 10)
    private Map<String, Object> parameters;
    
    // Mode target pour QUANTUM_CHANGE_MODE (SELL ou STORAGE)
    private String targetMode;
    
    // Amount pour QUANTUM_CHANGE_AMOUNT (+10, -10, etc.)
    private int changeAmount;
    
    // Lore append pour quantum_storage
    private List<String> loreAppend;
    
    // Item statique (vitre, bouton) qui ne doit pas déclencher d'actions storage
    private boolean isStatic = false;
    
    // Actions
    private List<MenuAction> leftClickActions;
    private List<MenuAction> rightClickActions;
    private List<MenuAction> middleClickActions;
    
    // Requirements
    private List<Requirement> viewRequirements;
    private List<Requirement> clickRequirements;
    private String denyMessage;
    
    public MenuItem(String id) {
        this.id = id;
        this.slots = new ArrayList<>();
        this.itemAmount = 1;
        this.changeAmount = 0;
        this.lore = new ArrayList<>();
        this.loreAppend = new ArrayList<>();
        this.leftClickActions = new ArrayList<>();
        this.rightClickActions = new ArrayList<>();
        this.middleClickActions = new ArrayList<>();
        this.viewRequirements = new ArrayList<>();
        this.clickRequirements = new ArrayList<>();
        this.hideFlags = new ArrayList<>();
        this.customModelData = -1;
        this.glow = false;
        this.buttonType = ButtonType.STANDARD;
        this.parameters = new HashMap<>();
    }
    
    // === GETTERS ===
    
    public String getId() {
        return id;
    }
    
    public List<Integer> getSlots() {
        return slots;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public String getMaterialString() {
        return materialString;
    }
    public String getMaterialPlaceholder() {
        return materialPlaceholder;
    }
    
    public String getNexoId() {
        return nexoId;
    }
    
    public int getAmount() {
        return itemAmount;
    }
    
    public int getChangeAmount() {
        return changeAmount;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public String getSkullOwner() {
        return skullOwner;
    }
    
    public int getCustomModelData() {
        return customModelData;
    }
    
    public boolean isGlow() {
        return glow;
    }
    
    public List<ItemFlag> getHideFlags() {
        return hideFlags;
    }
    
    public String getType() {
        return type;
    }
    
    public ButtonType getButtonType() {
        return buttonType;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getTargetMode() {
        return targetMode;
    }
    
    public List<String> getLoreAppend() {
        return loreAppend;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public List<MenuAction> getLeftClickActions() {
        return leftClickActions;
    }
    
    public List<MenuAction> getRightClickActions() {
        return rightClickActions;
    }
    
    public List<MenuAction> getMiddleClickActions() {
        return middleClickActions;
    }
    
    public List<Requirement> getViewRequirements() {
        return viewRequirements;
    }
    
    public List<Requirement> getClickRequirements() {
        return clickRequirements;
    }
    
    public String getDenyMessage() {
        return denyMessage;
    }
    
    public boolean isNexoItem() {
        return nexoId != null && !nexoId.isEmpty();
    }
    
    public boolean isQuantumStorage() {
        return "quantum_storage".equalsIgnoreCase(type);
    }
    
    // === SETTERS ===
    
    public void addSlot(int slot) {
        slots.add(slot);
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public void setMaterialString(String materialString) {
        this.materialString = materialString;
    }
    public void setMaterialPlaceholder(String materialPlaceholder) {
        this.materialPlaceholder = materialPlaceholder;
    }
    
    public void setNexoId(String nexoId) {
        this.nexoId = nexoId;
    }
    
    public void setAmount(int amount) {
        this.itemAmount = Math.max(1, Math.min(64, amount));
    }
    
    public void setChangeAmount(int amount) {
        this.changeAmount = amount;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    
    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }
    
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }
    
    public void setGlow(boolean glow) {
        this.glow = glow;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public void setParameter(String key, Object value) {
        this.parameters.put(key, value);
    }
    
    public void setTargetMode(String targetMode) {
        this.targetMode = targetMode;
    }
    
    public void setLoreAppend(List<String> loreAppend) {
        this.loreAppend = loreAppend;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public void addHideFlag(ItemFlag flag) {
        if (!hideFlags.contains(flag)) {
            hideFlags.add(flag);
        }
    }
    
    public void setDenyMessage(String denyMessage) {
        this.denyMessage = denyMessage;
    }
    
    public void addLeftClickAction(MenuAction action) {
        leftClickActions.add(action);
    }
    
    public void addRightClickAction(MenuAction action) {
        rightClickActions.add(action);
    }
    
    public void addMiddleClickAction(MenuAction action) {
        middleClickActions.add(action);
    }
    
    public void addViewRequirement(Requirement requirement) {
        viewRequirements.add(requirement);
    }
    
    public void addClickRequirement(Requirement requirement) {
        clickRequirements.add(requirement);
    }
    
    /**
     * Check if player meets all requirements to interact with this item
     */
    public boolean meetsRequirements(Player player, Quantum plugin) {
        // Check click requirements (when item is clicked)
        for (Requirement requirement : clickRequirements) {
            if (!requirement.check(player, plugin)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Execute all actions for this item based on click type
     */
    public void executeActions(Player player, Quantum plugin, ClickType clickType) {
        // === QUANTUM_CHANGE_MODE ===
        if (buttonType == ButtonType.QUANTUM_CHANGE_MODE) {
            if (targetMode != null) {
                // Définir le mode spécifique
                try {
                    StorageMode.Mode mode = StorageMode.Mode.valueOf(targetMode.toUpperCase());
                    StorageMode.setMode(player, mode);
                    
                    // Feedback au joueur
                    player.sendMessage("§aMode changé en: §e" + mode.getDisplayName());
                } catch (IllegalArgumentException e) {
                    // Mode invalide, fallback sur toggle
                    StorageMode.toggleMode(player);
                    player.sendMessage("§aMode changé en: §e" + StorageMode.getModeDisplay(player));
                }
            } else {
                // Pas de mode spécifié, toggle par défaut
                StorageMode.toggleMode(player);
                player.sendMessage("§aMode changé en: §e" + StorageMode.getModeDisplay(player));
            }
            
            // ✅ FIX: Fermer et rouvrir le menu pour afficher le nouveau titre
            // (Bukkit ne supporte pas la mise à jour dynamique des titres)
            Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
            if (activeMenu != null) {
                // Fermer l'inventaire actuel
                player.closeInventory();
                
                // Rouvrir le menu après 2 ticks avec le nouveau titre
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    activeMenu.open(player, plugin);
                }, 2L);
            }
            
            return;
        }
        
        // === QUANTUM_CHANGE_AMOUNT ===
        if (buttonType == ButtonType.QUANTUM_CHANGE_AMOUNT) {
            SellSession session = plugin.getSellManager().getSession(player);
            if (session == null) {
                player.sendMessage("§cErreur: Aucune session de vente active.");
                return;
            }
            
            // Modifier la quantité
            // 999999 ou plus = vendre tout
            if (changeAmount >= 999999) {
                session.setQuantity(session.getMaxQuantity());
            } else {
                session.changeQuantity(changeAmount);
            }
            
            // Rafraîchir le menu avec les nouveaux placeholders
            Menu activeMenu = plugin.getMenuManager().getActiveMenu(player);
            if (activeMenu != null) {
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    activeMenu.refresh(player, plugin, session.getPlaceholders());
                }, 1L);
            }
            
            return;
        }
        
        // === QUANTUM_SELL ===
        if (buttonType == ButtonType.QUANTUM_SELL) {
            if (plugin.getSellManager().executeSell(player)) {
                // Vente réussie, fermer le menu et retourner au storage
                player.closeInventory();
                
                // Ouvrir le storage après 2 ticks
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Menu storageMenu = plugin.getMenuManager().getMenu("storage");
                    if (storageMenu != null) {
                        storageMenu.open(player, plugin);
                    }
                }, 2L);
            }
            
            return;
        }
        
        // === ORDER BUTTONS - Déléguer à OrderButtonHandler ===
        if (isOrderButton()) {
            plugin.getOrderButtonHandler().handle(player, this);
            return;
        }
        
        // === ACTIONS STANDARD ===
        List<MenuAction> actionsToExecute = new ArrayList<>();
        
        // Déterminer quelles actions exécuter selon le type de clic
        switch (clickType) {
            case RIGHT:
            case SHIFT_RIGHT:
                if (!rightClickActions.isEmpty()) {
                    actionsToExecute.addAll(rightClickActions);
                } else {
                    // Fallback sur left click si pas de right click
                    actionsToExecute.addAll(leftClickActions);
                }
                break;
                
            case MIDDLE:
                if (!middleClickActions.isEmpty()) {
                    actionsToExecute.addAll(middleClickActions);
                } else {
                    // Fallback sur left click si pas de middle click
                    actionsToExecute.addAll(leftClickActions);
                }
                break;
                
            default:
                // LEFT, SHIFT_LEFT, et tous les autres types
                actionsToExecute.addAll(leftClickActions);
                break;
        }
        
        // Exécuter toutes les actions
        for (MenuAction action : actionsToExecute) {
            action.execute(player, plugin);
        }
    }
    
    /**
     * Vérifie si c'est un bouton d'ordre
     */
    private boolean isOrderButton() {
        return buttonType == ButtonType.QUANTUM_ADJUST_QUANTITY ||
               buttonType == ButtonType.QUANTUM_SET_QUANTITY_MAX ||
               buttonType == ButtonType.QUANTUM_VALIDATE_QUANTITY ||
               buttonType == ButtonType.QUANTUM_ADJUST_PRICE ||
               buttonType == ButtonType.QUANTUM_SET_PRICE_MAX ||
               buttonType == ButtonType.QUANTUM_FINALIZE_ORDER ||
               buttonType == ButtonType.QUANTUM_CANCEL_ORDER;
    }
    
    /**
     * Execute all actions for this item (default = left click)
     */
    public void executeActions(Player player, Quantum plugin) {
        executeActions(player, plugin, ClickType.LEFT);
    }
    /**
     * Convert this MenuItem to a Bukkit ItemStack with placeholder resolution.
     */
    public org.bukkit.inventory.ItemStack toItemStack(com.wynvers.quantum.Quantum plugin) {
        return toItemStack(plugin, null, null);
    }
    
    /**
     * Convert this MenuItem to a Bukkit ItemStack with placeholder support
     * @param plugin The plugin instance
     * @param player The player for placeholder resolution
     * @param customPlaceholders Custom placeholders to resolve
     */
    public org.bukkit.inventory.ItemStack toItemStack(com.wynvers.quantum.Quantum plugin, org.bukkit.entity.Player player, java.util.Map<String, String> customPlaceholders) {
        return toItemStack(plugin, player, customPlaceholders, -1);
    }
    
    /**
     * Convert this MenuItem to a Bukkit ItemStack with placeholder resolution and slot expansion.
     */
    public org.bukkit.inventory.ItemStack toItemStack(com.wynvers.quantum.Quantum plugin, org.bukkit.entity.Player player, java.util.Map<String, String> customPlaceholders, int slotNumber) {
        // If this is a quantum_storage slot, do not create an item here
        // The StorageRenderer will handle it
        if (isQuantumStorage()) {
            return null;
        }
        
        // Resolve material if materialString is set
        Material resolvedMaterial = material;
        if (materialString != null && !materialString.isEmpty() && player != null) {
            // Parse placeholders in material string
            String parsedMaterial = customPlaceholders != null
                ? plugin.getPlaceholderManager().parse(player, materialString, customPlaceholders)
                : plugin.getPlaceholderManager().parse(player, materialString);
            
            // Try to convert to Material
            try {
                resolvedMaterial = Material.valueOf(parsedMaterial.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If placeholder didn't resolve to valid material, skip this item
                return null;
            }
        }
        
        org.bukkit.inventory.ItemStack itemStack;
        
        // Resolve material placeholder if present
        if (materialPlaceholder != null && player != null) {
            // Expand {slot} if slot number is provided
            String expandedMaterialPlaceholder = materialPlaceholder;
            if (slotNumber >= 0) {
                expandedMaterialPlaceholder = materialPlaceholder.replace("{slot}", String.valueOf(slotNumber));
            }
            
            String parsedMaterial = customPlaceholders != null
                ? plugin.getPlaceholderManager().parse(player, expandedMaterialPlaceholder, customPlaceholders)
                : plugin.getPlaceholderManager().parse(player, expandedMaterialPlaceholder);
            
            // Trim whitespace and try to parse the resolved placeholder as a Material
            try {
                resolvedMaterial = Material.valueOf(parsedMaterial.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // If placeholder didn't resolve to a valid material, return null
                plugin.getQuantumLogger().warning("Material placeholder '" + expandedMaterialPlaceholder + 
                    "' in item '" + id + "' resolved to invalid material: '" + parsedMaterial + "'");
                return null;
            }
        }
        
        // Check if this is a Nexo item
        if (isNexoItem()) {
            // Try to create Nexo item
            try {
                itemStack = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId).build();
                itemStack.setAmount(itemAmount);
            } catch (Exception e) {
                // Nexo not available or item not found, fallback to material
                if (resolvedMaterial != null) {
                    itemStack = new org.bukkit.inventory.ItemStack(resolvedMaterial, itemAmount);
                } else {
                    return null;
                }
            }
        } else {
            // Create vanilla Minecraft item
            if (resolvedMaterial == null) return null;
            itemStack = new org.bukkit.inventory.ItemStack(resolvedMaterial, itemAmount);
        }
        
        // Apply metadata (only if not Nexo item or Nexo failed)
        if (!isNexoItem() || itemStack.getType() == resolvedMaterial) {
            org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                // Display name
                if (displayName != null) {
                    meta.setDisplayName(displayName);
                }
                
                // Lore
                if (lore != null && !lore.isEmpty()) {
                    meta.setLore(lore);
                }
                
                // Custom model data
                if (customModelData > 0) {
                    meta.setCustomModelData(customModelData);
                }
                
                // Glow effect (fake enchantment - use durability/unbreaking as most compatible)
                if (glow) {
                    // Try to get a safe enchantment for glow effect
                    Enchantment glowEnchant = Enchantment.getByName("DURABILITY");
                    if (glowEnchant == null) {
                        glowEnchant = Enchantment.getByName("PROTECTION_ENVIRONMENTAL");
                    }
                    if (glowEnchant != null) {
                        meta.addEnchant(glowEnchant, 1, true);
                    }
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                
                // Hide flags (custom tooltips)
                if (!hideFlags.isEmpty()) {
                    for (ItemFlag flag : hideFlags) {
                        meta.addItemFlags(flag);
                    }
                }
                
                // Skull owner (player head)
                if (skullOwner != null && !skullOwner.isEmpty() && 
                    meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                    ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(skullOwner);
                }
                
                // ========================================
                // ✅ FIX CRITIQUE: Ajouter button_type au PDC
                // ========================================
                // Si ce MenuItem a un buttonType non-STANDARD, l'ajouter au PDC
                // Cela permet à MenuListener de détecter les boutons via PDC
                if (buttonType != null && buttonType != ButtonType.STANDARD) {
                    NamespacedKey buttonTypeKey = new NamespacedKey(plugin, "button_type");
                    meta.getPersistentDataContainer().set(
                        buttonTypeKey, 
                        PersistentDataType.STRING, 
                        buttonType.name()
                    );
                }
                
                itemStack.setItemMeta(meta);
            }
        }
        
        return itemStack;
    }
}
