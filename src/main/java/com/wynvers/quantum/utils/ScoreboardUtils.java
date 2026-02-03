package com.wynvers.quantum.utils;

import org.bukkit.ChatColor;

public class ScoreboardUtils {
    
    // Caractères invisibles pour éviter les doublons et cacher les numéros
    private static final String[] INVISIBLE_CHARS = {
        "§0§r", "§1§r", "§2§r", "§3§r", "§4§r", "§5§r", "§6§r", "§7§r",
        "§8§r", "§9§r", "§a§r", "§b§r", "§c§r", "§d§r", "§e§r", "§f§r"
    };
    
    /**
     * Génère un caractère invisible unique pour chaque ligne
     */
    public static String getInvisibleChar(int index) {
        if (index < INVISIBLE_CHARS.length) {
            return INVISIBLE_CHARS[index];
        }
        // Pour plus de 16 lignes, on combine les caractères
        int first = index % INVISIBLE_CHARS.length;
        int second = index / INVISIBLE_CHARS.length;
        return INVISIBLE_CHARS[first] + INVISIBLE_CHARS[second % INVISIBLE_CHARS.length];
    }
    
    /**
     * Traduit les codes couleur (&c en §c)
     */
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
