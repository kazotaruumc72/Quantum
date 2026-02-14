package com.wynvers.quantum.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Pattern;

public class ScoreboardUtils {
    
    // Instance statique de MiniMessage pour parsing
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    // Pattern pour détecter les tags MiniMessage (ex: <red>, <bold>, <gradient:#FFF:#000>, </red>, <#FF0000>)
    private static final Pattern MINIMESSAGE_TAG_PATTERN = Pattern.compile(
        "</?[a-z_#][a-z0-9_:#.]*(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE
    );
    
    // Serializer pour convertir Component en legacy text (§c format)
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = 
        LegacyComponentSerializer.legacySection();
    
    // Cache LRU pour les résultats de color() - optimisation performance
    // Thread-safe pour éviter les problèmes de concurrence avec les mises à jour asynchrones
    private static final int CACHE_SIZE = 256;
    private static final Map<String, String> COLOR_CACHE = Collections.synchronizedMap(
        new LinkedHashMap<String, String>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > CACHE_SIZE;
            }
        }
    );
    
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
        
        // Vérifier le cache d'abord pour éviter le re-parsing
        String cached = COLOR_CACHE.get(text);
        if (cached != null) {
            return cached;
        }
        
        // Détecter si c'est du MiniMessage (contient des tags)
        // Utilise un pattern regex pour détecter tout tag MiniMessage valide
        boolean isMiniMessage = false;
        int openBracket = text.indexOf('<');
        if (openBracket >= 0 && text.indexOf('>', openBracket) > openBracket) {
            isMiniMessage = MINIMESSAGE_TAG_PATTERN.matcher(text).find();
        }
        
        String result;
        if (isMiniMessage) {
            try {
                // Parser le MiniMessage en Component
                Component component = MINI_MESSAGE.deserialize(text);
                // Convertir le Component en format legacy (§c)
                result = LEGACY_SERIALIZER.serialize(component);
            } catch (Exception e) {
                // Si le parsing échoue, fallback sur legacy
                result = ChatColor.translateAlternateColorCodes('&', text);
            }
        } else {
            // Sinon, traiter comme du legacy format (&c -> §c)
            result = ChatColor.translateAlternateColorCodes('&', text);
        }
        
        // Mettre en cache le résultat
        COLOR_CACHE.put(text, result);
        return result;
    }
    
    /**
     * Vide le cache de coloration (utile lors du rechargement de la config)
     */
    public static void clearCache() {
        COLOR_CACHE.clear();
    }
}
