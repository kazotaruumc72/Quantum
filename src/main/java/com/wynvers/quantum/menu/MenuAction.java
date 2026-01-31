package com.wynvers.quantum.menu;

public class MenuAction {
    
    private final ActionType type;
    private final String value;
        private final java.util.Map<String, String> settings;
    
    public MenuAction(ActionType type, String value, java.util.Map<String, String> settings) {        this.type = type;
        this.value = value;
                                                                                                      this.settings = settings != null ? settings : new java.util.HashMap<>();
    }

        // Constructeur de compatibilité pour les actions sans settings
    public MenuAction(ActionType type, String value) {
        this(type, value, null);
    }
    
    public ActionType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }

        public java.util.Map<String, String> getSettings() {
        return settings;
    }
    
    public String getSetting(String key) {
        return settings.get(key);
    }
    
    /**
     * Parse action from string format: [type] value
     * Examples:
     * - [message] Hello!
     * - [console] give %player% diamond 1
     * - [close]
     */
    public static MenuAction parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        input = input.trim();
        
        // Extract type from [brackets]
        if (input.startsWith("[")) {
            int endBracket = input.indexOf("]");
            if (endBracket > 0) {
                String typeStr = input.substring(1, endBracket).toUpperCase();
                String value = input.substring(endBracket + 1).trim();
                
                try {
                    ActionType type = ActionType.valueOf(typeStr);
                    return new MenuAction(type, value);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    public enum ActionType {
        MESSAGE,      // Send message to player
        CONSOLE,      // Execute console command
        PLAYER,       // Execute command as player
        CLOSE,        // Close current menu
        MENU,         // Open another menu
        SOUND,        // Play sound
        BROADCAST,    // Broadcast message
        ACTIONBAR,    // Send action bar message
        TITLE,        // Send title
        EFFECT        // Give potion effect
    }
}

