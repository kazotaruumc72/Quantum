package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gère les animations Model Engine des mobs de tour
 * Animations disponibles: spawn, idle, walk, attack, death
 */
public class MobAnimationManager implements Listener {
    
    private final Quantum plugin;
    private final Map<UUID, AnimationConfig> mobAnimations = new HashMap<>();
    private boolean modelEngineEnabled = false;
    
    public MobAnimationManager(Quantum plugin) {
        this.plugin = plugin;
        checkModelEngine();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Vérifie si Model Engine est installé
     */
    private void checkModelEngine() {
        modelEngineEnabled = Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
        if (modelEngineEnabled) {
            plugin.getQuantumLogger().success("Model Engine detected - Animations enabled");
        } else {
            plugin.getQuantumLogger().warning("Model Engine not found - Animations disabled");
        }
    }
    
    /**
     * Enregistre un mob avec ses animations
     */
    public void registerMob(LivingEntity mob, String modelId, AnimationConfig animations) {
        if (!modelEngineEnabled || modelId == null) return;
        
        mobAnimations.put(mob.getUniqueId(), animations);
        
        // Appliquer le modèle avec Model Engine
        applyModel(mob, modelId);
        
        // Jouer l'animation de spawn
        if (animations.hasSpawnAnimation()) {
            playAnimation(mob, animations.getSpawnAnimation());
            
            // Après l'animation de spawn, passer à idle
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (mob.isValid() && !mob.isDead()) {
                        startIdleLoop(mob);
                    }
                }
            }.runTaskLater(plugin, 40L); // 2 secondes après le spawn
        } else {
            // Pas d'animation de spawn, démarrer idle immédiatement
            startIdleLoop(mob);
        }
        
        // Stocker le model ID dans les metadata
        mob.setMetadata("model_id", new FixedMetadataValue(plugin, modelId));
    }
    
    /**
     * Applique un modèle Model Engine à une entité
     */
    private void applyModel(Entity entity, String modelId) {
        try {
            // Utiliser la commande Model Engine pour appliquer le modèle
            String command = "meg model set " + entity.getUniqueId() + " " + modelId;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getQuantumLogger().error("Failed to apply model " + modelId + ": " + e.getMessage());
        }
    }
    
    /**
     * Joue une animation sur un mob
     */
    private void playAnimation(Entity entity, String animationName) {
        if (!modelEngineEnabled || animationName == null) return;
        
        try {
            // Commande Model Engine pour jouer une animation
            String command = "meg anim play " + entity.getUniqueId() + " " + animationName;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getQuantumLogger().debug("Failed to play animation " + animationName + ": " + e.getMessage());
        }
    }
    
    /**
     * Démarre la boucle d'animation idle/walk
     */
    private void startIdleLoop(LivingEntity mob) {
        AnimationConfig config = mobAnimations.get(mob.getUniqueId());
        if (config == null) return;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!mob.isValid() || mob.isDead()) {
                    cancel();
                    return;
                }
                
                // Vérifier si le mob se déplace
                if (isMoving(mob)) {
                    if (config.hasWalkAnimation()) {
                        playAnimation(mob, config.getWalkAnimation());
                    }
                } else {
                    if (config.hasIdleAnimation()) {
                        playAnimation(mob, config.getIdleAnimation());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Vérifier toutes les secondes
    }
    
    /**
     * Vérifie si un mob se déplace
     */
    private boolean isMoving(LivingEntity mob) {
        return mob.getVelocity().lengthSquared() > 0.01;
    }
    
    /**
     * Listener pour l'animation d'attaque
     */
    @EventHandler
    public void onMobAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity)) return;
        
        LivingEntity attacker = (LivingEntity) event.getDamager();
        AnimationConfig config = mobAnimations.get(attacker.getUniqueId());
        
        if (config != null && config.hasAttackAnimation()) {
            playAnimation(attacker, config.getAttackAnimation());
            
            // Retourner à idle après l'attaque
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (attacker.isValid() && !attacker.isDead()) {
                        if (config.hasIdleAnimation()) {
                            playAnimation(attacker, config.getIdleAnimation());
                        }
                    }
                }
            }.runTaskLater(plugin, 20L); // 1 seconde après l'attaque
        }
    }
    
    /**
     * Listener pour l'animation de mort
     */
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();
        AnimationConfig config = mobAnimations.get(mob.getUniqueId());
        
        if (config != null && config.hasDeathAnimation()) {
            playAnimation(mob, config.getDeathAnimation());
        }
        
        // Nettoyer les données
        mobAnimations.remove(mob.getUniqueId());
    }
    
    /**
     * Nettoie les animations d'un mob
     */
    public void unregisterMob(UUID mobUuid) {
        mobAnimations.remove(mobUuid);
    }
    
    /**
     * Configuration des animations pour un mob
     */
    public static class AnimationConfig {
        private final String spawnAnimation;
        private final String idleAnimation;
        private final String walkAnimation;
        private final String attackAnimation;
        private final String deathAnimation;
        
        public AnimationConfig(String spawn, String idle, String walk, String attack, String death) {
            this.spawnAnimation = spawn;
            this.idleAnimation = idle;
            this.walkAnimation = walk;
            this.attackAnimation = attack;
            this.deathAnimation = death;
        }
        
        public boolean hasSpawnAnimation() { return spawnAnimation != null && !spawnAnimation.isEmpty(); }
        public boolean hasIdleAnimation() { return idleAnimation != null && !idleAnimation.isEmpty(); }
        public boolean hasWalkAnimation() { return walkAnimation != null && !walkAnimation.isEmpty(); }
        public boolean hasAttackAnimation() { return attackAnimation != null && !attackAnimation.isEmpty(); }
        public boolean hasDeathAnimation() { return deathAnimation != null && !deathAnimation.isEmpty(); }
        
        public String getSpawnAnimation() { return spawnAnimation; }
        public String getIdleAnimation() { return idleAnimation; }
        public String getWalkAnimation() { return walkAnimation; }
        public String getAttackAnimation() { return attackAnimation; }
        public String getDeathAnimation() { return deathAnimation; }
        
        /**
         * Crée une config depuis une section de configuration
         */
        public static AnimationConfig fromConfigSection(org.bukkit.configuration.ConfigurationSection section) {
            if (section == null) return null;
            
            String spawn = section.getString("spawn");
            String idle = section.getString("idle");
            String walk = section.getString("walk");
            String attack = section.getString("attack");
            String death = section.getString("death");
            
            return new AnimationConfig(spawn, idle, walk, attack, death);
        }
    }
}
