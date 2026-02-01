package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    
    private final String id;
    private final List<Integer> slots;
    
    // Item properties
    private Material material;
    private String nexoId;
    private int amount;
    private String displayName;
    private List<String> lore;
    private String skullOwner;
    private int customModelData;
    private boolean glow;
    private List<ItemFlag> hideFlags;
    
    // Type de slot (quantum_storage, etc.)
    private String type;
    
    // Button type (QUANTUM_CHANGE_MODE, etc.)
    private ButtonType buttonType;
    
    // Mode target pour QUANTUM_CHANGE_MODE (SELL ou STORAGE)
    private String targetMode;
    
    // Lore append pour quantum_storage
    private List<String> loreAppend;
    
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
        this.amount = 1;
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
    
    public String getNexoId() {
        return nexoId;
    }
    
    public int getAmount() {
        return amount;
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
    
    public String getTargetMode() {
        return targetMode;
    }
    
    public List<String> getLoreAppend() {
        return loreAppend;
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
    
    public void setNexoId(String nexoId) {
        this.nexoId = nexoId;
    }
    
    public void setAmount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
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
    
    public void setTargetMode(String targetMode) {
        this.targetMode = targetMode;
    }
    
    public void setLoreAppend(List<String> loreAppend) {
        this.loreAppend = loreAppend;
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
        // Si c'est un bouton QUANTUM_CHANGE_MODE, changer vers le mode spécifié
        if (buttonType == ButtonType.QUANTUM_CHANGE_MODE) {
            if (targetMode != null) {
                // Définir le mode spécifique
                try {
                    StorageMode mode = StorageMode.valueOf(targetMode.toUpperCase());
                    StorageMode.setMode(player, mode);
                } catch (IllegalArgumentException e) {
                    // Mode invalide, fallback sur toggle
                    StorageMode.toggleMode(player);
                }
            } else {
                // Pas de mode spécifié, toggle par défaut
                StorageMode.toggleMode(player);
            }
            
            // Rouvrir le menu pour rafraîchir l'affichage
            Menu storageMenu = plugin.getMenuManager().getMenu("storage");
            if (storageMenu != null) {
                storageMenu.open(player, plugin);
            }
            return;
        }
        
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
     * Execute all actions for this item (default = left click)
     */
    public void executeActions(Player player, Quantum plugin) {
        executeActions(player, plugin, ClickType.LEFT);
    }

    /**
     * Convert this MenuItem to a Bukkit ItemStack
     */
    public org.bukkit.inventory.ItemStack toItemStack(com.wynvers.quantum.Quantum plugin) {
        // Si c'est un slot quantum_storage, ne pas créer d'item ici
        // Le StorageRenderer s'en occupera
        if (isQuantumStorage()) {
            return null;
        }
        
        org.bukkit.inventory.ItemStack itemStack;
        
        // Check if this is a Nexo item
        if (isNexoItem()) {
            // Try to create Nexo item
            try {
                itemStack = com.nexomc.nexo.api.NexoItems.itemFromId(nexoId).build();
                itemStack.setAmount(amount);
            } catch (Exception e) {
                // Nexo not available or item not found, fallback to material
                if (material != null) {
                    itemStack = new org.bukkit.inventory.ItemStack(material, amount);
                } else {
                    return null;
                }
            }
        } else {
            // Create vanilla Minecraft item
            if (material == null) return null;
            itemStack = new org.bukkit.inventory.ItemStack(material, amount);
        }
        
        // Apply metadata (only if not Nexo item or Nexo failed)
        if (!isNexoItem() || itemStack.getType() == material) {
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
                
                // Handle skull owner
                if (skullOwner != null && meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                    ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(skullOwner);
                }
                
                itemStack.setItemMeta(meta);
            }
        }
        
        return itemStack;
    }
}
