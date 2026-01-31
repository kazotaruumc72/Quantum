package com.wynvers.quantum.utils;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Requirement;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class RequirementChecker {
    
    private final Quantum plugin;
    
    public RequirementChecker(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if player meets all requirements
     */
    public boolean meetsRequirements(Player player, List<Requirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }
        
        for (Requirement req : requirements) {
            if (!meetsRequirement(player, req)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check single requirement
     */
    public boolean meetsRequirement(Player player, Requirement requirement) {
        switch (requirement.getType()) {
            case PERMISSION:
                return checkPermission(player, requirement.getValue());
                
            case PLACEHOLDER:
                return checkPlaceholder(player, requirement.getValue());
                
            case MONEY:
                return checkMoney(player, requirement.getValue());
                
            case ITEM:
                return checkItem(player, requirement.getValue());
                
            case EXP:
                return checkExp(player, requirement.getValue());
                
            default:
                return false;
        }
    }
    
    /**
     * Check permission requirement
     */
    private boolean checkPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
    
    /**
     * Check placeholder requirement (supports comparisons)
     * Examples:
     * - %player_level% >= 10
     * - %vault_eco_balance% > 1000
     * - %player_name% == Notch
     */
    private boolean checkPlaceholder(Player player, String condition) {
        if (plugin.getPlaceholderManager() == null || !plugin.getPlaceholderManager().isEnabled()) {
            return false;
        }
        
        // Parse condition: %placeholder% operator value
        String parsed = PlaceholderAPI.setPlaceholders(player, condition);
        
        // Try to evaluate as comparison
        if (parsed.contains(">=")) {
            return evaluateComparison(parsed, ">=");
        } else if (parsed.contains("<=")) {
            return evaluateComparison(parsed, "<=");
        } else if (parsed.contains("==")) {
            return evaluateEquals(parsed);
        } else if (parsed.contains("!=")) {
            return !evaluateEquals(parsed.replace("!=", "=="));
        } else if (parsed.contains(">")) {
            return evaluateComparison(parsed, ">");
        } else if (parsed.contains("<")) {
            return evaluateComparison(parsed, "<");
        }
        
        return false;
    }
    
    /**
     * Evaluate numeric comparison
     */
    private boolean evaluateComparison(String expression, String operator) {
        String[] parts = expression.split(operator);
        if (parts.length != 2) return false;
        
        try {
            double left = Double.parseDouble(parts[0].trim());
            double right = Double.parseDouble(parts[1].trim());
            
            switch (operator) {
                case ">":
                    return left > right;
                case "<":
                    return left < right;
                case ">=":
                    return left >= right;
                case "<=":
                    return left <= right;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Evaluate string equality
     */
    private boolean evaluateEquals(String expression) {
        String[] parts = expression.split("==");
        if (parts.length != 2) return false;
        
        return parts[0].trim().equalsIgnoreCase(parts[1].trim());
    }
    
    /**
     * Check money requirement (requires Vault)
     */
    private boolean checkMoney(Player player, String amount) {
        // TODO: Implement Vault integration
        return true;
    }
    
    /**
     * Check item requirement
     * Format: MATERIAL:amount
     */
    private boolean checkItem(Player player, String requirement) {
        String[] parts = requirement.split(":");
        if (parts.length != 2) return false;
        
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(parts[0].toUpperCase());
            int requiredAmount = Integer.parseInt(parts[1]);
            
            int playerAmount = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    playerAmount += item.getAmount();
                }
            }
            
            return playerAmount >= requiredAmount;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check exp level requirement
     */
    private boolean checkExp(Player player, String level) {
        try {
            int requiredLevel = Integer.parseInt(level);
            return player.getLevel() >= requiredLevel;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
