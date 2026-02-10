package com.wynvers.quantum.towers;

import com.wynvers.quantum.mobs.MobSkillConfig;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration d'un spawner de mob dans une tour + méthode de spawn avec ModelEngine.
 */
public class TowerSpawnerConfig {

    // ex: "tower_water:1:slime_pack_1"
    private String fullId;

    // ID du modèle ModelEngine (blueprint), ex: "slime_basic", "spider_melee_am"
    private String mobId;

    // Nom affiché au-dessus du mob, ex: "&bAraignée de méllée"
    private String displayName;

    private EntityType type;
    private double baseHealth;
    private int damage;
    private int baseAmount;
    private int intervalSeconds;
    private int maxAlive;

    // Scaling
    private int healthPerRunPercent;
    private int amountPerRun;
    private int maxExtraAmount;

    // Skills
    private List<MobSkillConfig> skills = new ArrayList<>();

    // Animations (clé logique -> nom d'animation ModelEngine)
    private Map<String, String> animations = new HashMap<>();

    // Région de spawn (optionnelle)
    private SpawnRegion spawnRegion;

    // Offset vertical pour la barre de vie (en blocs)
    // Utilisé pour positionner correctement la healthbar au-dessus des modèles ModelEngine
    private double healthbarOffset;

    public TowerSpawnerConfig(String fullId, ConfigurationSection section) {
        this.fullId = fullId;
        loadFromConfig(section);
    }

    private void loadFromConfig(ConfigurationSection section) {
        // Type d'entité Vanilla (ZOMBIE, SKELETON, etc.)
        this.type = parseEntityType(section.getString("type", "ZOMBIE"));

        // ID du modèle ModelEngine (blueprint)
        this.mobId = section.getString("model", "default");

        // Nom affiché (pour nametag + healthbar)
        this.displayName = section.getString("display_name", "&fMob");

        this.baseHealth = section.getDouble("base_health", 20.0);
        this.damage = section.getInt("damage", 1);
        this.baseAmount = section.getInt("amount", 1);
        this.intervalSeconds = section.getInt("interval", 60);
        this.maxAlive = section.getInt("max_alive", 10);
        
        // Offset de la barre de vie (par défaut 0.0 = hauteur automatique)
        this.healthbarOffset = section.getDouble("healthbar_offset", 0.0);

        // Scaling
        ConfigurationSection scalingSec = section.getConfigurationSection("scaling");
        if (scalingSec != null) {
            this.healthPerRunPercent = scalingSec.getInt("health_per_run_percent", 5);
            this.amountPerRun = scalingSec.getInt("amount_per_run", 1);
            this.maxExtraAmount = scalingSec.getInt("max_extra_amount", 5);
        }

        // Skills
        if (section.contains("skills")) {
            List<?> rawSkills = section.getList("skills");
            if (rawSkills != null) {
                for (Object obj : rawSkills) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        skills.add(new MobSkillConfig(map));
                    }
                }
            }
        }

        // Animations
        ConfigurationSection animSec = section.getConfigurationSection("animations");
        if (animSec != null) {
            for (String key : animSec.getKeys(false)) {
                animations.put(key, animSec.getString(key));
            }
        }

        // Région de spawn
        ConfigurationSection regionSec = section.getConfigurationSection("region");
        if (regionSec != null) {
            String world = regionSec.getString("world");
            double x1 = regionSec.getDouble("x1");
            double y1 = regionSec.getDouble("y1");
            double z1 = regionSec.getDouble("z1");
            double x2 = regionSec.getDouble("x2");
            double y2 = regionSec.getDouble("y2");
            double z2 = regionSec.getDouble("z2");
            if (world != null) {
                this.spawnRegion = new SpawnRegion(world, x1, y1, z1, x2, y2, z2);
            }
        }
    }

    private EntityType parseEntityType(String str) {
        try {
            return EntityType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EntityType.ZOMBIE;
        }
    }

    // ============ GETTERS ============

    public String getFullId() {
        return fullId;
    }

    public String getId() {
        return fullId;
    }

    /**
     * ID du modèle ModelEngine (blueprint).
     */
    public String getMobId() {
        return mobId;
    }

    /**
     * Display name configuré dans towers.yml (nametag + healthbar).
     */
    public String getDisplayName() {
        return displayName;
    }

    public EntityType getType() {
        return type;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public int getDamageHalfHearts() {
        return damage;
    }

    public int getBaseAmount() {
        return baseAmount;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getMaxAlive() {
        return maxAlive;
    }

    public List<MobSkillConfig> getSkills() {
        return skills;
    }

    public Map<String, String> getAnimations() {
        return animations;
    }

    public SpawnRegion getSpawnRegion() {
        return spawnRegion;
    }

    /**
     * Retourne l'offset vertical pour la barre de vie (en blocs).
     * Utilisé pour positionner correctement la healthbar au-dessus des modèles ModelEngine.
     */
    public double getHealthbarOffset() {
        return healthbarOffset;
    }

    /**
     * Calcule la santé du mob en fonction du nombre de runs.
     */
    public double getHealthForRuns(int runs) {
        if (runs <= 0) return baseHealth;
        double multiplier = 1.0 + (runs * healthPerRunPercent / 100.0);
        return baseHealth * multiplier;
    }

    /**
     * Calcule le nombre de mobs à spawner en fonction du nombre de runs.
     */
    public int getAmountForRuns(int runs) {
        if (runs <= 0) return baseAmount;
        int extra = Math.min(runs * amountPerRun, maxExtraAmount);
        return baseAmount + extra;
    }

    // ============ INTÉGRATION MODELENGINE ============

    /**
     * Spawn un mob de ce spawner à une position donnée avec ModelEngine 4.
     *
     * @param location Où spawn
     * @param runs     Nombre de runs (pour le scaling de la vie / amount)
     * @return         L'entité vivante spawn (celle qui porte le modèle)
     */
    public LivingEntity spawnWithModelEngine(Location location, int runs) {
        double health = getHealthForRuns(runs);
        int damageValue = getDamageHalfHearts();

        // Spawn vanilla
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);

        // Stats - Utilisation de MAX_HEALTH (pas GENERIC_MAX_HEALTH)
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        }
        entity.setHealth(health);

        // Stats - Utilisation de ATTACK_DAMAGE (pas GENERIC_ATTACK_DAMAGE)
        if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damageValue);
        }

        // Nom / nametag
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
        entity.setCustomNameVisible(true);

        // On ne veut voir que le modèle
        entity.setInvisible(true);
        
        // ===== DÉSACTIVER LE BURN AU SOLEIL =====
        // Les zombies de tour sont des mobs spéciaux qui ne brûlent pas au soleil
        if (entity instanceof org.bukkit.entity.Zombie) {
            org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) entity;
            zombie.setShouldBurnInDay(false);
        }

        // ===== ModelEngine 4 =====
        // Crée une ModeledEntity à partir de l'entité Bukkit
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);

        // Crée un ActiveModel via l'ID du blueprint (ex: "spider_melee_am")
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(mobId);

        // Ajoute le modèle à l'entité (true = visible immédiatement)
        modeledEntity.addModel(activeModel, true);

        // NOTE: setBaseVisible n'existe pas dans ModelEngine R4.1.0
        // Le setInvisible(true) sur l'entité Vanilla suffit

        return entity;
    }
}
