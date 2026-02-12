package com.wynvers.quantum.betterhud;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common BetterHud operations in Quantum.
 * Provides convenience methods and optimized patterns.
 */
public class BetterHudUtil {
    
    /**
     * Create a variable map for popups from key-value pairs.
     * Optimized method to reduce boilerplate code.
     * 
     * @param pairs Alternating key-value pairs (must be even number of arguments)
     * @return Map of variables
     */
    public static Map<String, String> createVariables(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide even number of arguments for key-value pairs");
        }
        
        Map<String, String> variables = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            variables.put(pairs[i], pairs[i + 1]);
        }
        return variables;
    }
    
    /**
     * Create a variable map with a single entry.
     * 
     * @param key The variable key
     * @param value The variable value
     * @return Map with single entry
     */
    public static Map<String, String> singleVariable(String key, String value) {
        Map<String, String> variables = new HashMap<>();
        variables.put(key, value);
        return variables;
    }
    
    /**
     * Format a number for display in HUD with suffixes (K, M, B).
     * Optimized for better readability in limited HUD space.
     * 
     * @param number The number to format
     * @return Formatted string
     */
    public static String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else {
            return String.format("%.1fB", number / 1000000000.0);
        }
    }
    
    /**
     * Format a percentage for HUD display.
     * 
     * @param current Current value
     * @param max Maximum value
     * @return Formatted percentage string
     */
    public static String formatPercentage(double current, double max) {
        if (max <= 0) return "0%";
        double percentage = (current / max) * 100.0;
        return String.format("%.0f%%", Math.min(100, Math.max(0, percentage)));
    }
    
    /**
     * Get a health bar string using Unicode characters.
     * Optimized for visual HUD display.
     * 
     * @param current Current health
     * @param max Maximum health
     * @param length Number of bars to display
     * @return Unicode health bar string
     */
    public static String getHealthBar(double current, double max, int length) {
        if (max <= 0) return "";
        
        double ratio = current / max;
        int filled = (int) Math.ceil(ratio * length);
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append('█');
            } else {
                bar.append('░');
            }
        }
        return bar.toString();
    }
    
    /**
     * Get a colored health bar based on health percentage.
     * 
     * @param current Current health
     * @param max Maximum health
     * @param length Number of bars
     * @return Colored health bar string
     */
    public static String getColoredHealthBar(double current, double max, int length) {
        if (max <= 0) return "";
        
        double ratio = current / max;
        ChatColor color;
        
        if (ratio > 0.66) {
            color = ChatColor.GREEN;
        } else if (ratio > 0.33) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }
        
        return color + getHealthBar(current, max, length) + ChatColor.RESET;
    }
    
    /**
     * Strip color codes from text for HUD display.
     * Some HUD elements may not support color codes.
     * 
     * @param text Text with color codes
     * @return Text without color codes
     */
    public static String stripColors(String text) {
        return ChatColor.stripColor(text);
    }
    
    /**
     * Truncate text to fit HUD display limits.
     * 
     * @param text Text to truncate
     * @param maxLength Maximum length
     * @param suffix Suffix to add if truncated (e.g., "...")
     * @return Truncated text
     */
    public static String truncate(String text, int maxLength, String suffix) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - suffix.length()) + suffix;
    }
    
    /**
     * Truncate text to fit HUD display (default suffix: "...").
     * 
     * @param text Text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    public static String truncate(String text, int maxLength) {
        return truncate(text, maxLength, "...");
    }
}
