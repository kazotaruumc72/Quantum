package com.wynvers.quantum.tools;

import com.wynvers.quantum.Quantum;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de base pour les outils améliorables
 */
public abstract class UpgradeableTool {

    protected final JavaPlugin plugin;
    protected final ToolType toolType;
    protected final NamespacedKey levelKey;
    protected final NamespacedKey typeKey;

    public UpgradeableTool(JavaPlugin plugin, ToolType toolType) {
        this.plugin = plugin;
        this.toolType = toolType;
        this.levelKey = new NamespacedKey(plugin, "tool_level");
        this.typeKey = new NamespacedKey(plugin, "tool_type");
    }

    /**
     * Vérifie si un item est un outil Quantum de ce type
     */
    public boolean isQuantumTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String type = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        return toolType.name().equals(type);
    }

    /**
     * Récupère le niveau d'un outil
     */
    public int getLevel(ItemStack item) {
        if (!isQuantumTool(item)) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        Integer level = meta.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level != null ? level : 1;
    }

    /**
     * Définit le niveau d'un outil
     */
    public void setLevel(ItemStack item, int level) {
        if (!isQuantumTool(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    /**
     * Met à jour le lore d'un outil avec les informations de niveau et description
     */
    protected void updateLore(ItemStack item, int level, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();

        String toolConfig = toolType.name().toLowerCase();

        // Get display name
        String displayName = config.getString(toolConfig + ".display_name");
        if (displayName != null) {
            meta.setDisplayName(displayName.replace('&', '§'));
        }

        // Get description from config
        List<String> description = config.getStringList(toolConfig + ".description");
        if (!description.isEmpty()) {
            for (String line : description) {
                lore.add(line.replace('&', '§'));
            }
            lore.add("");
        }

        // Separator
        String separator = config.getString("messages.lore.separator", "&7&m-------------------");
        lore.add(separator.replace('&', '§'));

        // Level information
        int maxLevel = config.getInt(toolConfig + ".max_level", 100);
        String levelFormat = config.getString("messages.lore.level_format", "&8│ &6&lNIVEAU &r&e{level} &8│ &3&lMAX: &r&b{max_level} &8│");
        levelFormat = levelFormat.replace("{level}", String.valueOf(level));
        levelFormat = levelFormat.replace("{max_level}", String.valueOf(maxLevel));
        lore.add(levelFormat.replace('&', '§'));

        // Footer
        String footer = config.getString("messages.lore.footer", "&7Outil Quantique");
        lore.add(separator.replace('&', '§'));
        lore.add(footer.replace('&', '§'));
        lore.add(separator.replace('&', '§'));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Crée un nouvel outil au niveau 1
     */
    public abstract ItemStack createTool();

    /**
     * Active la compétence de l'outil
     */
    public abstract void activateSkill(ItemStack tool, Object... args);

    /**
     * Améliore l'outil au niveau suivant
     */
    public abstract boolean upgrade(ItemStack tool, int currentLevel);

    public ToolType getToolType() {
        return toolType;
    }
}
