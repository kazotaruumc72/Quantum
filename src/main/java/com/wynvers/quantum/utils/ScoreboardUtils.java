package com.wynvers.quantum.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class ScoreboardUtils {
    
    // Instance statique de MiniMessage pour parsing
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    // Serializer pour convertir Component en legacy text (§c format)
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = 
        LegacyComponentSerializer.legacySection();
    
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
     * Traduit les codes couleur en supportant à la fois:
     * - MiniMessage format: <gradient:#FFD700:#FFA500>, <bold>, <aqua>, etc.
     * - Legacy format: &c, &a, &l, etc.
     * 
     * @param text Texte à formater
     * @return Texte formaté avec les codes couleur legacy (§)
     */
    public static String color(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Détecter si c'est du MiniMessage (contient < et >)
        if (text.contains("<") && text.contains(">")) {
            try {
                // Parser le MiniMessage en Component
                Component component = MINI_MESSAGE.deserialize(text);
                // Convertir le Component en format legacy (§c)
                return LEGACY_SERIALIZER.serialize(component);
            } catch (Exception e) {
                // Si le parsing échoue, fallback sur legacy
                return ChatColor.translateAlternateColorCodes('&', text);
            }
        }
        
        // Sinon, traiter comme du legacy format (&c -> §c)
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
