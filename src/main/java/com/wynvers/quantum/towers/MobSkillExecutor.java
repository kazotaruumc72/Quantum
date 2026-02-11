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
    
    // Cache for nearby players to reduce repeated lookups
    private final Map<UUID, List<Player>> nearbyPlayersCache = new HashMap<>();
    private final Map<UUID, Long> cacheTimestamp = new HashMap<>();
    private static final long CACHE_VALIDITY_MS = 500; // Cache valid for 500ms
    
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
        
        // Clean up cache for this mob
        nearbyPlayersCache.remove(mobUuid);
        cacheTimestamp.remove(mobUuid);
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
                
            case "blindness":
                executeBlindness(mob, skillData);
                break;
                
            case "darkness":
                executeDarkness(mob, skillData);
                break;
                
            case "explosion":
                executeExplosion(mob, skillData);
                break;
                
            case "lightning":
                executeLightning(mob, skillData);
                break;
                
            case "teleport":
                executeTeleport(mob, skillData);
                break;
                
            case "knockback":
                executeKnockback(mob, skillData);
                break;
                
            case "pull":
                executePull(mob, skillData);
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
    
    /**
     * BLINDNESS - Applique la cécité aux joueurs proches
     */
    private void executeBlindness(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 10.0; // rayon en blocs
        
        getValidTargetPlayers(mob.getLocation(), radius).forEach(player -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, 
                duration * 20, 
                0 // Niveau 1
            ));
        });
    }
    
    /**
     * DARKNESS - Applique l'effet d'obscurité aux joueurs proches (Minecraft 1.19+)
     */
    private void executeDarkness(LivingEntity mob, Map<String, Object> skillData) {
        int duration = skillData.containsKey("duration") 
            ? ((Number) skillData.get("duration")).intValue() 
            : 5; // secondes
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 10.0; // rayon en blocs
        
        getValidTargetPlayers(mob.getLocation(), radius).forEach(player -> {
            // Vérifier si l'effet DARKNESS existe (Minecraft 1.19+)
            try {
                PotionEffectType darknessType = PotionEffectType.getByName("DARKNESS");
                if (darknessType != null) {
                    player.addPotionEffect(new PotionEffect(
                        darknessType, 
                        duration * 20, 
                        0 // Niveau 1
                    ));
                } else {
                    // Fallback: utiliser blindness pour versions anciennes
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS, 
                        duration * 20, 
                        0
                    ));
                }
            } catch (Exception e) {
                // Fallback si DARKNESS n'existe pas
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS, 
                    duration * 20, 
                    0
                ));
            }
        });
    }
    
    /**
     * EXPLOSION - Crée une explosion à la position du mob
     */
    private void executeExplosion(LivingEntity mob, Map<String, Object> skillData) {
        double damage = skillData.containsKey("damage") 
            ? ((Number) skillData.get("damage")).doubleValue() 
            : 10.0; // dégâts
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 3.0; // rayon en blocs
        
        Location mobLoc = mob.getLocation();
        
        // Créer l'explosion visuelle (sans dégâts de terrain)
        mobLoc.getWorld().createExplosion(mobLoc, 0.0f, false, false);
        
        // Appliquer les dégâts manuellement aux joueurs proches
        getValidTargetPlayers(mobLoc, radius).forEach(player -> {
            double distance = player.getLocation().distance(mobLoc);
            double damageMultiplier = 1.0 - (distance / radius);
            double finalDamage = damage * Math.max(0.1, damageMultiplier);
            
            player.damage(finalDamage);
            
            // Effet de knockback
            org.bukkit.util.Vector direction = player.getLocation().toVector()
                .subtract(mobLoc.toVector())
                .normalize()
                .multiply(1.5);
            player.setVelocity(direction);
        });
        
        // Particules d'explosion
        mobLoc.getWorld().spawnParticle(
            org.bukkit.Particle.EXPLOSION_EMITTER,
            mobLoc,
            3, 0.5, 0.5, 0.5, 0.1
        );
        
        // Son d'explosion
        mobLoc.getWorld().playSound(mobLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }
    
    /**
     * LIGHTNING - Fait tomber la foudre sur les joueurs proches
     */
    private void executeLightning(LivingEntity mob, Map<String, Object> skillData) {
        double damage = skillData.containsKey("damage") 
            ? ((Number) skillData.get("damage")).doubleValue() 
            : 5.0; // dégâts par défaut
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 15.0; // rayon en blocs
        
        getValidTargetPlayers(mob.getLocation(), radius).forEach(player -> {
            Location playerLoc = player.getLocation();
            
            // Foudre visuelle
            playerLoc.getWorld().strikeLightningEffect(playerLoc);
            
            // Dégâts
            player.damage(damage);
            
            // Effet sonore
            player.playSound(playerLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        });
    }
    
    /**
     * TELEPORT - Téléporte le mob aléatoirement près d'un joueur (max 5 blocks)
     */
    private void executeTeleport(LivingEntity mob, Map<String, Object> skillData) {
        double maxDistance = 5.0; // Limite fixe à 5 blocs comme demandé
        
        List<Player> targets = getValidTargetPlayers(mob.getLocation(), 20.0);
        if (targets.isEmpty()) {
            return;
        }
        
        // Choisir un joueur aléatoire comme cible
        Player target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
        Location targetLoc = target.getLocation();
        Location mobLoc = mob.getLocation();
        
        // Générer une position aléatoire dans un rayon de 5 blocs
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double distance = ThreadLocalRandom.current().nextDouble() * maxDistance;
        
        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;
        
        Location teleportLoc = targetLoc.clone().add(offsetX, 0, offsetZ);
        
        // Garder la hauteur proche de la position actuelle du mob pour rester dans la tour
        // Chercher un emplacement sûr dans une plage de +/- 3 blocs verticalement
        teleportLoc.setY(mobLoc.getY());
        
        // Trouver un bloc solide sous le mob (max 5 blocs vers le bas)
        for (int i = 0; i < 5; i++) {
            Location checkLoc = teleportLoc.clone().subtract(0, i, 0);
            if (checkLoc.getBlock().getType().isSolid()) {
                teleportLoc.setY(checkLoc.getY() + 1);
                break;
            }
        }
        
        // Vérifier que la destination est sûre (pas dans un mur)
        if (teleportLoc.getBlock().getType().isSolid() || 
            teleportLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            // Destination non sûre, annuler la téléportation
            return;
        }
        
        // Particules au départ
        mob.getWorld().spawnParticle(
            org.bukkit.Particle.PORTAL,
            mob.getLocation().add(0, 1, 0),
            50, 0.5, 0.5, 0.5, 0.5
        );
        
        // Téléportation
        mob.teleport(teleportLoc);
        
        // Particules à l'arrivée
        mob.getWorld().spawnParticle(
            org.bukkit.Particle.PORTAL,
            teleportLoc.add(0, 1, 0),
            50, 0.5, 0.5, 0.5, 0.5
        );
        
        // Son de téléportation
        mob.getWorld().playSound(teleportLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    
    /**
     * KNOCKBACK - Repousse les joueurs loin du mob
     */
    private void executeKnockback(LivingEntity mob, Map<String, Object> skillData) {
        double strength = skillData.containsKey("strength") 
            ? ((Number) skillData.get("strength")).doubleValue() 
            : 2.0; // force du knockback
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 8.0; // rayon en blocs
        
        Location mobLoc = mob.getLocation();
        
        getValidTargetPlayers(mobLoc, radius).forEach(player -> {
            // Calculer le vecteur de direction (depuis mob vers joueur)
            org.bukkit.util.Vector direction = player.getLocation().toVector()
                .subtract(mobLoc.toVector())
                .normalize()
                .multiply(strength);
            
            // Ajouter un peu de hauteur pour le knockback
            direction.setY(0.5);
            
            // Appliquer le knockback
            player.setVelocity(direction);
            
            // Particules
            player.getWorld().spawnParticle(
                org.bukkit.Particle.EXPLOSION,
                player.getLocation(),
                20, 0.5, 0.5, 0.5, 0.1
            );
        });
        
        // Son de knockback
        mobLoc.getWorld().playSound(mobLoc, org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.8f);
    }
    
    /**
     * PULL - Attire les joueurs vers le mob
     */
    private void executePull(LivingEntity mob, Map<String, Object> skillData) {
        double strength = skillData.containsKey("strength") 
            ? ((Number) skillData.get("strength")).doubleValue() 
            : 2.0; // force de l'attraction
        
        double radius = skillData.containsKey("radius") 
            ? ((Number) skillData.get("radius")).doubleValue() 
            : 15.0; // rayon en blocs
        
        Location mobLoc = mob.getLocation();
        
        getValidTargetPlayers(mobLoc, radius).forEach(player -> {
            // Calculer le vecteur de direction (depuis joueur vers mob)
            org.bukkit.util.Vector direction = mobLoc.toVector()
                .subtract(player.getLocation().toVector())
                .normalize()
                .multiply(strength);
            
            // Appliquer l'attraction
            player.setVelocity(direction);
            
            // Particules
            player.getWorld().spawnParticle(
                org.bukkit.Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                30, 0.3, 0.5, 0.3, 0.05
            );
        });
        
        // Son d'attraction
        mobLoc.getWorld().playSound(mobLoc, org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.5f);
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
     * Uses caching to reduce overhead
     * 
     * @param center Centre de la zone
     * @param radius Rayon en blocs
     * @return Liste des joueurs valides (survie/aventure)
     */
    private List<Player> getValidTargetPlayers(Location center, double radius) {
        if (center.getWorld() == null) {
            return new ArrayList<>();
        }
        
        // For skills execution, use grid-based caching with 4-block precision
        // This provides good balance between cache hits and accuracy
        int gridX = (int) Math.floor(center.getX() / 4.0);
        int gridY = (int) Math.floor(center.getY() / 4.0);
        int gridZ = (int) Math.floor(center.getZ() / 4.0);
        UUID cacheKey = UUID.nameUUIDFromBytes((center.getWorld().getName() + 
            ":" + gridX + ":" + gridY + ":" + gridZ).getBytes());
        
        long now = System.currentTimeMillis();
        Long lastUpdate = cacheTimestamp.get(cacheKey);
        
        // Check if cache is still valid
        if (lastUpdate != null && (now - lastUpdate) < CACHE_VALIDITY_MS) {
            List<Player> cached = nearbyPlayersCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        
        // Cache miss or expired, fetch fresh data
        List<Player> players = center.getWorld().getNearbyEntities(center, radius, radius, radius)
            .stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .filter(player -> {
                GameMode mode = player.getGameMode();
                // Exclure créatif et spectateur
                return mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR;
            })
            .collect(Collectors.toList());
        
        // Update cache
        nearbyPlayersCache.put(cacheKey, players);
        cacheTimestamp.put(cacheKey, now);
        
        return players;
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
        
        // Clean up caches
        nearbyPlayersCache.clear();
        cacheTimestamp.clear();
        
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
