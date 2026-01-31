package com.wynvers.quantum.menu;

import org.bukkit.Material;

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
    
    // Actions
    private List<MenuAction> leftClickActions;
    private List<MenuAction> rightClickActions;
    private List<MenuAction> middleClickActions;
    
    // Requirements
    private List<Requirement> viewRequirements;
    private List<Requirement> clickRequirements;
    
    public MenuItem(String id) {
        this.id = id;
        this.slots = new ArrayList<>();
        this.amount = 1;
        this.lore = new ArrayList<>();
        this.leftClickActions = new ArrayList<>();
        this.rightClickActions = new ArrayList<>();
        this.middleClickActions = new ArrayList<>();
        this.viewRequirements = new ArrayList<>();
        this.clickRequirements = new ArrayList<>();
        this.customModelData = -1;
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
    
    public boolean isNexoItem() {
        return nexoId != null && !nexoId.isEmpty();
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
}
