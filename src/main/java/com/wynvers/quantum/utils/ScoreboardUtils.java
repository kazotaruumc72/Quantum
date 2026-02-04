package com.wynvers.quantum.utils;

import org.bukkit.ChatColor;

public class ScoreboardUtils {
    
    /**
     * Génère un caractère invisible unique pour chaque ligne
     * Utilise des caractères Unicode invisibles pour vraiment cacher les numéros rouges
     */
    public static String getInvisibleChar(int index) {
        // Utilise des caractères Unicode invisibles pour éviter les doublons
        // Ces caractères sont réellement invisibles et ne laissent pas de traces
        StringBuilder sb = new StringBuilder();
        
        // Ajoute des espaces de largeur zéro Unicode pour rendre unique
        int remaining = index;
        while (remaining > 0) {
            sb.append("\u200B"); // Zero Width Space
            remaining--;
        }
        
        return sb.toString();
    }
    
    /**
     * Traduit les codes couleur (&c en §c)
     */
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
