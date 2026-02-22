package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Charge et gère la configuration du bestiaire (mobs.yml).
 * Définit pour chaque type de mob l'XP donnée au joueur,
 * à chaque pièce d'armure de donjon, et à l'arme de donjon.
 */
public class MobConfig {

    private final Quantum plugin;
    private YamlConfiguration config;

    // Valeurs par défaut
    private int defaultPlayerExp;
    private final Map<String, Integer> defaultArmorExp = new HashMap<>();
    private int defaultWeaponExp;

    // Config par mob: mobKey -> MobReward
    private final Map<String, MobReward> mobRewards = new HashMap<>();

    public MobConfig(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "mobs.yml");
        if (!configFile.exists()) {
            plugin.saveResource("mobs.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Charger les valeurs par défaut
        ConfigurationSection defaults = config.getConfigurationSection("defaults");
        if (defaults != null) {
            defaultPlayerExp = defaults.getInt("player_exp", 5);
            ConfigurationSection armorDefaults = defaults.getConfigurationSection("armor_exp");
            if (armorDefaults != null) {
                for (String piece : armorDefaults.getKeys(false)) {
                    defaultArmorExp.put(piece, armorDefaults.getInt(piece, 1));
                }
            }
            defaultWeaponExp = defaults.getInt("weapon_exp", 2);
        } else {
            defaultPlayerExp = 5;
            defaultWeaponExp = 2;
        }
        // S'assurer que les 4 pièces ont une valeur par défaut
        defaultArmorExp.putIfAbsent("helmet", 1);
        defaultArmorExp.putIfAbsent("chestplate", 1);
        defaultArmorExp.putIfAbsent("leggings", 1);
        defaultArmorExp.putIfAbsent("boots", 1);

        // Charger les récompenses par mob
        mobRewards.clear();
        ConfigurationSection mobsSection = config.getConfigurationSection("mobs");
        if (mobsSection != null) {
            for (String mobKey : mobsSection.getKeys(false)) {
                ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobKey);
                if (mobSection == null) continue;

                int playerExp = mobSection.getInt("player_exp", defaultPlayerExp);
                int weaponExp = mobSection.getInt("weapon_exp", defaultWeaponExp);

                Map<String, Integer> armorExp = new HashMap<>(defaultArmorExp);
                ConfigurationSection armorSection = mobSection.getConfigurationSection("armor_exp");
                if (armorSection != null) {
                    for (String piece : armorSection.getKeys(false)) {
                        armorExp.put(piece, armorSection.getInt(piece, defaultArmorExp.getOrDefault(piece, 1)));
                    }
                }

                mobRewards.put(mobKey.toUpperCase(), new MobReward(playerExp, armorExp, weaponExp));
            }
        }

        plugin.getQuantumLogger().success("✓ Mob bestiary loaded! (" + mobRewards.size() + " mobs configured)");
    }

    public void reload() {
        loadConfig();
    }

    /**
     * Retourne la récompense pour un mob vanilla (ex: "ZOMBIE").
     */
    public MobReward getReward(String mobKey) {
        MobReward reward = mobRewards.get(mobKey.toUpperCase());
        if (reward != null) return reward;
        return new MobReward(defaultPlayerExp, defaultArmorExp, defaultWeaponExp);
    }

    /**
     * Retourne la récompense pour un mob MythicMobs (ex: "mm:SkeletonKing").
     */
    public MobReward getMythicReward(String mythicId) {
        // Essayer avec le préfixe mm:
        MobReward reward = mobRewards.get("MM:" + mythicId.toUpperCase());
        if (reward != null) return reward;
        return new MobReward(defaultPlayerExp, defaultArmorExp, defaultWeaponExp);
    }

    /**
     * Contient les récompenses XP pour un type de mob.
     */
    public static class MobReward {
        private final int playerExp;
        private final Map<String, Integer> armorExp;
        private final int weaponExp;

        public MobReward(int playerExp, Map<String, Integer> armorExp, int weaponExp) {
            this.playerExp = playerExp;
            this.armorExp = new HashMap<>(armorExp);
            this.weaponExp = weaponExp;
        }

        public int getPlayerExp() { return playerExp; }
        public int getWeaponExp() { return weaponExp; }
        public int getArmorExp(String piece) {
            return armorExp.getOrDefault(piece.toLowerCase(), 1);
        }
    }
}
