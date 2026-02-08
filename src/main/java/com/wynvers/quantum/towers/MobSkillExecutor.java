package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Exécute automatiquement les skills des mobs spawned dans les tours
 * Gère les intervals, les effets, et l'affichage des titles
 */
public class MobSkillExecutor {
    
    private final Quantum plugin;
    private final Map<UUID, List<BukkitTask>> mobSkillTasks = new HashMap<>();
    private final TemporaryStructureManager structureManager;
    
    public MobSkillExecutor(Quantum plugin) {
        this.plugin = plugin;
        this.structureManager = new TemporaryStructureManager(plugin);
    }
    
    /**
     * Démarre l'exécution automatique des skills pour un mob
     * @param mob Le mob qui aura les skills
     * @param skills Liste des skills depuis towers.yml
     */
    public void startSkills(LivingEntity mob, List<Map<String, Object>> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        
        List<BukkitTask> tasks = new ArrayList<>();
        
        for (Map<String, Object> skillData : skills) {
            String skillId = (String) skillData.get("id");
            if (skillId == null) continue;
            
            // Intervalle en secondes
            int intervalSeconds = skillData.containsKey("interval") 
                ? ((Number) skillData.get("interval")).intValue() 
                : 30;
            
            // Créer la tâche répétée pour ce skill
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!mob.isValid() || mob.isDead()) {
                        cancel();
                        return;
                    }
                    
                    // Exécuter le skill
                    executeSkill(mob, skillId, skillData);
                }
            }.runTaskTimer(plugin, 20L, intervalSeconds * 20L);
            
            tasks.add(task);
        }
        
        mobSkillTasks.put(mob.getUniqueId(), tasks);
    }
    
    /**
     * Arrête tous les skills d'un mob
     * @param mob Le mob
     */
    public void stopSkills(LivingEntity mob) {
        stopSkills(mob.getUniqueId());
    }
    
    /**
     * Arrête tous les skills d'un mob par UUID
     * @param mobUuid UUID du mob
     */
    public void stopSkills(UUID mobUuid) {
        List<BukkitTask> tasks = mobSkillTasks.remove(mobUuid);
        if (tasks != null) {
            tasks.forEach(BukkitTask::cancel);
        }
    }
    
    /**
     * Exécute un skill spécifique
     * @param mob Le mob qui utilise le skill
     * @param skillId ID du skill
     * @param skillData Données du skill depuis towers.yml
     */
    private void executeSkill(LivingEntity mob, String skillId, Map<String, Object> skillData) {
        Location mobLoc = mob.getLocation();
        
        // Afficher le title via MobSkillManager
        MobSkillManager skillManager = plugin.getMobSkillManager();
        if (skillManager != null) {
            skillManager.showSkillToNearby(mob, skillId);
        }
        
        // Exécuter l'effet selon le skill
        switch (skillId.toLowerCase()) {
            case "summon":
                executeSummon(mob, skillData);
                break;
                
            case "healing":
                executeHealing(mob, skillData);
                break;
                
            case "fire":
                executeFire(mob, skillData);
                break;
                
            case "fireprison":
                executeFirePrison(mob, skillData);
                break;
                
            case "ice":
                executeIce(mob, skillData);
                break;
                
            case "slowness":
                executeSlowness(mob, skillData);
                break;
                
            case "iceberg":
                executeIceberg(mob, skillData);
                break;
                
            case "thunder":
                executeThunder(mob, skillData);
                break;
                
            case "poisonous":
                executePoisonous(mob, skillData);
                break;
                
            case "wither":
                executeWither(mob, skillData);
                break;
                
            default:
                plugin.getQuantumLogger().warning("Unknown skill effect: " + skillId);
        }
    }
    
    // ==================== SKILL EFFECTS ====================
    
    /**
     * SUMMON - Invoque d'autres mobs du même type
     */
    private void executeSummon(LivingEntity mob, Map<String, Object> skillData) {
        int amount = skillData.containsKey("amount") 
            ? ((Number) skillData.get("amount")).intValue() 
            : 2;
        
        Location loc = mob.getLocation();
        
        for (int i = 0; i < amount; i++) {
            Location spawnLoc = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-3, 3),
                0,
                ThreadLocalRandom.current().nextDouble(-3, 3)
            );
            
            LivingEntity summon = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, mob.getType());
            summon.setMaxHealth(mob.getMaxHealth() * 0.5); // 50% de la vie du summoner
            summon.setHealth(summon.getMaxHealth());
        }
    }
    
    /**
     * HEALING - Le mob se soigne
     */
    private void executeHealing(LivingEntity mob, Map<String, Object> skillData) {
        int amount = skillData.containsKey("amount") 
            ? ((Number) skillData.get("amount")).intValue() 
            : 4; // en demi-cœurs
        
        double newHealth = Math.min(mob.getHealth() + amount, mob.getMaxHealth());
        mob.setHealth(newHealth);
    }
    
    /**
     * FIRE - Met les joueurs proches en feu
     */
    private void executeFire(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        getValidTargetPlayers(mob.getLocation(), 10).forEach(player -> {
            player.setFireTicks(duration * 20);
        });
    }
    
    /**
     * FIREPRISON - Crée une prison de feu autour des joueurs proches
     */
    private void executeFirePrison(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 4; // secondes
        
        getValidTargetPlayers(mob.getLocation(), 10).forEach(player -> {
            createFireRing(player.getLocation(), duration);
            player.setFireTicks(duration * 20);
        });
    }
    
    /**
     * ICE - Gèle les joueurs (slowness)
     */
    private void executeIce(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        getValidTargetPlayers(mob.getLocation(), 10).forEach(player -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, 
                duration * 20, 
                2 // Niveau 3
            ));
            
            // Particules de neige
            player.getWorld().spawnParticle(
                org.bukkit.Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0),
                50, 0.5, 0.5, 0.5, 0.01
            );
        });
    }
    
    /**
     * SLOWNESS - Applique l'effet de ralentissement aux joueurs proches
     */
    private void executeSlowness(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        int level = skillData.containsKey("level") 
            ? ((Number) skillData.get("level")).intValue() - 1 
            : 1; // Niveau 2 par défaut (index 1)
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 10.0; // rayon en blocs
        
        getValidTargetPlayers(mob.getLocation(), radius).forEach(player -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, 
                duration * 20, 
                level
            ));
        });
    }
    
    /**
     * ICEBERG - Crée une cage/prison de glace autour du joueur
     * Le joueur est piégé à l'intérieur et la cage disparait après la durée configurée
     */
    private void executeIceberg(LivingEntity mob, Map<String, Object> skillData) {
        // Durée de la cage (défaut 8 secondes)
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 8; // secondes
        
        // Rayon de la cage (défaut 2 blocs)
        int radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).intValue() 
            : 2;
        
        // Hauteur de la cage (défaut 4 blocs)
        int height = skillData.containsKey("height") 
            ? ((Number) skillData.get("height")).intValue() 
            : 4;
        
        getValidTargetPlayers(mob.getLocation(), 15).forEach(player -> {
            Location playerLoc = player.getLocation();
            
            // Placer la cage de glace autour du joueur
            structureManager.placeIceCage(
                playerLoc,
                radius,
                height,
                Material.PACKED_ICE,
                duration
            );
            
            // Effet de ralentissement
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, 
                duration * 20, 
                3 // Niveau 4 - très lent
            ));
            
            // Particules de glace
            player.getWorld().spawnParticle(
                org.bukkit.Particle.SNOWFLAKE,
                playerLoc.clone().add(0, 1, 0),
                150, radius, height / 2.0, radius, 0.05
            );
            
            // Son de glace
            player.playSound(playerLoc, org.bukkit.Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        });
    }
    
    /**
     * THUNDER - Foudre sur les joueurs proches
     */
    private void executeThunder(LivingEntity mob, Map<String, Object> skillData) {
        getValidTargetPlayers(mob.getLocation(), 15).forEach(player -> {
            player.getWorld().strikeLightningEffect(player.getLocation());
            player.damage(4.0); // 2 cœurs de dégâts
        });
    }
    
    /**
     * POISONOUS - Empoisonne les joueurs proches
     */
    private void executePoisonous(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        getValidTargetPlayers(mob.getLocation(), 10).forEach(player -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.POISON, 
                duration * 20, 
                1 // Niveau 2
            ));
        });
    }
    
    /**
     * WITHER - Applique l'effet wither
     */
    private void executeWither(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 4; // secondes
        
        getValidTargetPlayers(mob.getLocation(), 10).forEach(player -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.WITHER, 
                duration * 20, 
                1 // Niveau 2
            ));
        });
    }
    
    // ==================== HELPERS ====================
    
    /**
     * Récupère tous les joueurs dans un rayon (ancien - déprécié)
     * @deprecated Utiliser getValidTargetPlayers() à la place
     */
    @Deprecated
    private List<Player> getNearbyPlayers(Location center, double radius) {
        return getValidTargetPlayers(center, radius);
    }
    
    /**
     * Récupère tous les joueurs VALIDES dans un rayon
     * Exclut les joueurs en créatif et spectateur
     * 
     * @param center Centre de la zone
     * @param radius Rayon en blocs
     * @return Liste des joueurs valides (survie/aventure)
     */
    private List<Player> getValidTargetPlayers(Location center, double radius) {
        if (center.getWorld() == null) {
            return new ArrayList<>();
        }
        
        return center.getWorld().getNearbyEntities(center, radius, radius, radius)
            .stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .filter(player -> {
                GameMode mode = player.getGameMode();
                // Exclure créatif et spectateur
                return mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Crée un anneau de feu autour d'une location
     */
    private void createFireRing(Location center, int durationSeconds) {
        List<Location> fireBlocks = new ArrayList<>();
        
        // Créer un cercle de feu
        double radius = 3.0;
        for (double angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            double x = center.getX() + radius * Math.cos(radians);
            double z = center.getZ() + radius * Math.sin(radians);
            
            Location fireLoc = new Location(center.getWorld(), x, center.getY(), z);
            if (fireLoc.getBlock().getType() == Material.AIR) {
                fireLoc.getBlock().setType(Material.FIRE);
                fireBlocks.add(fireLoc);
            }
        }
        
        // Retirer le feu après la durée
        new BukkitRunnable() {
            @Override
            public void run() {
                fireBlocks.forEach(loc -> {
                    if (loc.getBlock().getType() == Material.FIRE) {
                        loc.getBlock().setType(Material.AIR);
                    }
                });
            }
        }.runTaskLater(plugin, durationSeconds * 20L);
    }
    
    /**
     * Nettoie toutes les tâches actives
     */
    public void shutdown() {
        mobSkillTasks.values().forEach(tasks -> tasks.forEach(BukkitTask::cancel));
        mobSkillTasks.clear();
        
        // Restaurer toutes les structures temporaires
        if (structureManager != null) {
            structureManager.restoreAll();
        }
    }
    
    /**
     * Restaure toutes les structures temporaires actives
     */
    public void restoreAllTemporaryStructures() {
        if (structureManager != null) {
            structureManager.restoreAll();
        }
    }
}
