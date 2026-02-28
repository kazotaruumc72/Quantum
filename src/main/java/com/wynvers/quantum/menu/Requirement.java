package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Requirement {
    
    private final RequirementType type;
    private final String value;
    
    public Requirement(RequirementType type, String value) {
        this.type = type;
        this.value = value;
    }
    
    public RequirementType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Check if player meets this requirement
     */
    public boolean check(Player player, Quantum plugin) {
        switch (type) {
            case PERMISSION:
                return player.hasPermission(value);
                
            case PLACEHOLDER:
                // TODO: Implement PlaceholderAPI check when available
                return true;
                
            case MONEY:
                // TODO: Implement Vault check when available
                return true;
                
            case ITEM:
                return checkItem(player);
                
            case EXP:
                return checkExp(player);
                
            case LUCKPERMS_GROUP:
                return checkLuckPermsGroup(player);
                
            case LUCKPERMS_PERM:
                return checkLuckPermsPerm(player);
                
            default:
                return false;
        }
    }
    
    private boolean checkItem(Player player) {
        // Format: MATERIAL:AMOUNT or item_id:amount
        String[] parts = value.split(":");
        if (parts.length < 2) return false;
        
        try {
            String itemId = parts[0];
            int required = Integer.parseInt(parts[1]);
            
            // Try vanilla material
            try {
                Material material = Material.valueOf(itemId.toUpperCase());
                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == material) {
                        count += item.getAmount();
                    }
                }
                return count >= required;
            } catch (IllegalArgumentException e) {
                // Not a vanilla material, might be Nexo
                // TODO: Implement Nexo check
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean checkExp(Player player) {
        try {
            int required = Integer.parseInt(value);
            return player.getLevel() >= required;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean checkLuckPermsGroup(Player player) {
        if (!isLuckPermsAvailable()) return false;
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;
            return user.getPrimaryGroup().equalsIgnoreCase(value)
                    || user.getCachedData().getPermissionData()
                           .checkPermission("group." + value.toLowerCase()).result().asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkLuckPermsPerm(Player player) {
        if (!isLuckPermsAvailable()) return false;
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;
            return user.getCachedData().getPermissionData()
                       .checkPermission(value).result().asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isLuckPermsAvailable() {
        return Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
    }

    /**
     * Parse requirement from string format: type:value
     * Examples:
     * - permission:quantum.vip
     * - placeholder:%player_level% >= 10
     */
    public static Requirement parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        int colonIndex = input.indexOf(":");
        if (colonIndex <= 0) {
            return null;
        }
        
        String typeStr = input.substring(0, colonIndex).toUpperCase();
        String value = input.substring(colonIndex + 1).trim();
        
        try {
            RequirementType type = RequirementType.valueOf(typeStr);
            return new Requirement(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public enum RequirementType {
        PERMISSION,       // permission:quantum.vip
        PLACEHOLDER,      // placeholder:%player_level% >= 10
        MONEY,            // money:1000 (requires Vault)
        ITEM,             // item:DIAMOND:10 (requires X amount of item)
        EXP,              // exp:30 (requires X exp levels)
        LUCKPERMS_GROUP,  // luckperms_group:vip (requires player to be in LuckPerms group)
        LUCKPERMS_PERM    // luckperms_perm:some.permission (checks via LuckPerms API)
    }
}
