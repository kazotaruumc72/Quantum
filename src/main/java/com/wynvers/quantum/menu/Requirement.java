package com.wynvers.quantum.menu;

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
        PERMISSION,   // permission:quantum.vip
        PLACEHOLDER,  // placeholder:%player_level% >= 10
        MONEY,        // money:1000 (requires Vault)
        ITEM,         // item:DIAMOND:10 (requires X amount of item)
        EXP           // exp:30 (requires X exp levels)
    }
}
