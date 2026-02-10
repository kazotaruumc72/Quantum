package com.wynvers.quantum.weapon;

import com.nexomc.nexo.api.NexoItems;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.wynvers.quantum.Quantum;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Système d'arme de donjon améliorable
 * Utilisable uniquement dans les zones de donjon (WorldGuard)
 */
public class DungeonWeapon {
    
    private final Quantum plugin;
    private final NamespacedKey weaponKey;
    private final NamespacedKey levelKey;
    private YamlConfiguration config;
    private Set<String> dungeonRegions;
    
    public DungeonWeapon(Quantum plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "dungeon_weapon");
        this.levelKey = new NamespacedKey(plugin, "weapon_level");
        this.dungeonRegions = new HashSet<>();
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "dungeon_weapon.yml");
        if (!configFile.exists()) {
            plugin.saveResource("dungeon_weapon.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Charger les régions de donjon
        List<String> regions = config.getStringList("dungeon_regions");
        dungeonRegions = new HashSet<>(regions);
        
        plugin.getLogger().info("✓ Dungeon Weapon system loaded! (" + dungeonRegions.size() + " dungeon regions)");
    }
    
    public void reload() {
        loadConfig();
    }
    
    /**
     * Crée une arme de donjon au niveau 1
     */
    public ItemStack createWeapon() {
        var builder = NexoItems.itemFromId("dungeon_sword_level1");
        if (builder == null) return null;
        
        ItemStack weapon = builder.build();
        if (weapon == null) return null;
        
        ItemMeta meta = weapon.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, 1);
            
            // Ajouter les enchantements du niveau 1
            addEnchantments(meta, 1);
            
            weapon.setItemMeta(meta);
        }
        
        return weapon;
    }
    
    /**
     * Vérifie si un item est une arme de donjon
     */
    public boolean isDungeonWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        return meta.getPersistentDataContainer().has(weaponKey, PersistentDataType.BYTE);
    }
    
    /**
     * Récupère le niveau de l'arme
     */
    public int getLevel(ItemStack item) {
        if (!isDungeonWeapon(item)) return 0;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        
        Integer level = meta.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level != null ? level : 1;
    }
    
    /**
     * Améliore l'arme au niveau suivant
     */
    public boolean upgrade(ItemStack weapon, Player player) {
        if (!isDungeonWeapon(weapon)) return false;
        
        int currentLevel = getLevel(weapon);
        if (currentLevel >= 10) {
            String message = config.getString("messages.weapon_max_level", "&cCette arme est déjà au niveau maximum!");
            player.sendMessage(message.replace('&', '§'));
            return false;
        }
        
        int nextLevel = currentLevel + 1;
        
        // Vérifier le coût
        int cost = getUpgradeCost(currentLevel);
        
        // TODO: Vérifier l'argent du joueur avec Vault
        
        // Mettre à jour le Nexo ID
        String newNexoId = "dungeon_sword_level" + nextLevel;
        var builder = NexoItems.itemFromId(newNexoId);
        if (builder == null) return false;
        
        ItemStack newWeapon = builder.build();
        if (newWeapon == null) return false;
        
        ItemMeta newMeta = newWeapon.getItemMeta();
        if (newMeta != null) {
            newMeta.getPersistentDataContainer().set(weaponKey, PersistentDataType.BYTE, (byte) 1);
            newMeta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, nextLevel);
            
            // Ajouter les enchantements
            addEnchantments(newMeta, nextLevel);
            
            newWeapon.setItemMeta(newMeta);
        }
        
        // Remplacer l'arme
        weapon.setType(newWeapon.getType());
        weapon.setItemMeta(newWeapon.getItemMeta());
        
        String message = config.getString("messages.weapon_upgraded", "&a✓ Arme de donjon améliorée au niveau {level}!");
        message = message.replace("{level}", String.valueOf(nextLevel));
        player.sendMessage(message.replace('&', '§'));
        
        return true;
    }
    
    /**
     * Ajoute les enchantements selon le niveau
     */
    private void addEnchantments(ItemMeta meta, int level) {
        String section;
        if (level >= 7) {
            section = "level_7_to_10";
        } else if (level >= 4) {
            section = "level_4_to_6";
        } else {
            section = "level_1_to_3";
        }
        
        List<?> enchants = config.getList("weapon.enchantments." + section);
        if (enchants != null) {
            for (Object obj : enchants) {
                if (obj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> enchantMap = (java.util.Map<String, Object>) obj;
                    String enchantName = (String) enchantMap.get("enchant");
                    int enchantLevel = ((Number) enchantMap.get("level")).intValue();
                    
                    try {
                        // Use NamespacedKey for compatibility with newer Minecraft versions
                        NamespacedKey key = NamespacedKey.minecraft(enchantName.toLowerCase());
                        Enchantment enchantment = Enchantment.getByKey(key);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, enchantLevel, true);
                        } else {
                            plugin.getLogger().warning("Invalid enchantment: " + enchantName);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error applying enchantment " + enchantName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Récupère le coût d'amélioration
     */
    public int getUpgradeCost(int currentLevel) {
        return config.getInt("weapon.upgrade_costs." + currentLevel + "_to_" + (currentLevel + 1), 2000);
    }
    
    /**
     * Vérifie si le joueur est dans un donjon
     */
    public boolean isInDungeon(Player player) {
        return isInDungeon(player.getLocation());
    }
    
    /**
     * Vérifie si une location est dans un donjon
     */
    public boolean isInDungeon(Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            
            com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
            ApplicableRegionSet regions = query.getApplicableRegions(loc);
            
            for (ProtectedRegion region : regions) {
                if (dungeonRegions.contains(region.getId())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // WorldGuard non disponible ou erreur
        }
        
        return false;
    }
    
    /**
     * Affiche le message d'interdiction
     */
    public void showDungeonOnlyMessage(Player player) {
        String title = config.getString("messages.outside_dungeon_title", "&c✘ INTERDIT ✘");
        String subtitle = config.getString("messages.outside_dungeon_subtitle", "&7Vous ne pouvez pas tuer de mobs avec cette épée hors du donjon");
        String chat = config.getString("messages.outside_dungeon_chat", "&cVous ne pouvez pas utiliser cette arme en dehors d'un donjon!");
        
        player.sendTitle(
            title.replace('&', '§'),
            subtitle.replace('&', '§'),
            10, 70, 20
        );
        player.sendMessage(chat.replace('&', '§'));
    }
}
