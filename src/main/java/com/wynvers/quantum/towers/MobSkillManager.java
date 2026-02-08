package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.utils.ScoreboardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère l'affichage des titles/subtitles quand un mob utilise un skill
 * Configure via mob_skills.yml
 */
public class MobSkillManager {
    
    private final Quantum plugin;
    private FileConfiguration config;
    
    private boolean enabled;
    private double displayRadius;
    private int fadeIn;
    private int stay;
    private int fadeOut;
    
    // Cache des skills : skillId -> (title, subtitle)
    private final Map<String, SkillDisplay> skills;
    
    public MobSkillManager(Quantum plugin) {
        this.plugin = plugin;
        this.skills = new HashMap<>();
        loadConfig();
    }
    
    /**
     * Charge la configuration depuis mob_skills.yml
     */
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "mob_skills.yml");
        if (!configFile.exists()) {
            plugin.saveResource("mob_skills.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Paramètres généraux
        enabled = config.getBoolean("enabled", true);
        displayRadius = config.getDouble("display_radius", 20.0);
        
        ConfigurationSection timingSection = config.getConfigurationSection("title_timing");
        if (timingSection != null) {
            fadeIn = timingSection.getInt("fade_in", 10);
            stay = timingSection.getInt("stay", 40);
            fadeOut = timingSection.getInt("fade_out", 10);
        } else {
            fadeIn = 10;
            stay = 40;
            fadeOut = 10;
        }
        
        // Charger tous les skills
        skills.clear();
        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection != null) {
            for (String skillId : skillsSection.getKeys(false)) {
                ConfigurationSection skillSection = skillsSection.getConfigurationSection(skillId);
                if (skillSection != null) {
                    String title = skillSection.getString("title", "");
                    String subtitle = skillSection.getString("subtitle", "");
                    
                    skills.put(skillId.toLowerCase(), new SkillDisplay(title, subtitle));
                }
            }
        }
        
        plugin.getQuantumLogger().success("✓ Mob Skills loaded! (" + skills.size() + " skills configured)");
    }
    
    /**
     * Recharge la configuration
     */
    public void reload() {
        loadConfig();
        plugin.getQuantumLogger().success("Mob skills config reloaded!");
    }
    
    /**
     * Affiche un skill à un joueur spécifique
     * @param player Joueur cible
     * @param skillId ID du skill (ex: "fireball", "healing")
     */
    public void showSkill(Player player, String skillId) {
        if (!enabled || player == null || skillId == null) {
            return;
        }
        
        SkillDisplay display = skills.get(skillId.toLowerCase());
        if (display == null) {
            plugin.getQuantumLogger().warning("Unknown skill: " + skillId);
            return;
        }
        
        sendTitle(player, display.title, display.subtitle);
    }
    
    /**
     * Affiche un skill à tous les joueurs proches d'une entité (le mob)
     * @param entity Entité (mob) qui utilise le skill
     * @param skillId ID du skill
     */
    public void showSkillToNearby(Entity entity, String skillId) {
        if (!enabled || entity == null || skillId == null) {
            return;
        }
        
        showSkillToNearby(entity.getLocation(), skillId);
    }
    
    /**
     * Affiche un skill à tous les joueurs proches d'une location
     * @param location Position du skill
     * @param skillId ID du skill
     */
    public void showSkillToNearby(Location location, String skillId) {
        if (!enabled || location == null || location.getWorld() == null || skillId == null) {
            return;
        }
        
        SkillDisplay display = skills.get(skillId.toLowerCase());
        if (display == null) {
            plugin.getQuantumLogger().warning("Unknown skill: " + skillId);
            return;
        }
        
        // Envoyer le title à tous les joueurs dans le rayon
        location.getWorld().getNearbyEntities(location, displayRadius, displayRadius, displayRadius)
            .stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .forEach(player -> sendTitle(player, display.title, display.subtitle));
    }
    
    /**
     * Affiche un skill à tous les joueurs dans une tour spécifique
     * @param towerId ID de la tour
     * @param skillId ID du skill
     */
    public void showSkillToTower(String towerId, String skillId) {
        if (!enabled || towerId == null || skillId == null) {
            return;
        }
        
        SkillDisplay display = skills.get(skillId.toLowerCase());
        if (display == null) {
            plugin.getQuantumLogger().warning("Unknown skill: " + skillId);
            return;
        }
        
        TowerManager towerManager = plugin.getTowerManager();
        if (towerManager == null) {
            return;
        }
        
        // Envoyer à tous les joueurs connectés qui sont dans cette tour
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            TowerProgress progress = towerManager.getProgress(player.getUniqueId());
            if (progress != null && towerId.equals(progress.getCurrentTower())) {
                sendTitle(player, display.title, display.subtitle);
            }
        });
    }
    
    /**
     * Envoie le title au joueur (Adventure API)
     * @param player Joueur
     * @param titleText Texte du title
     * @param subtitleText Texte du subtitle
     */
    private void sendTitle(Player player, String titleText, String subtitleText) {
        // Convertir les codes couleur Minecraft (&) en composants Adventure
        String coloredTitle = ScoreboardUtils.color(titleText);
        String coloredSubtitle = ScoreboardUtils.color(subtitleText);
        
        Component title = Component.text(coloredTitle);
        Component subtitle = Component.text(coloredSubtitle);
        
        // Créer le title avec les timings configurés
        Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50L),   // fadeIn en ticks -> ms
            Duration.ofMillis(stay * 50L),     // stay en ticks -> ms
            Duration.ofMillis(fadeOut * 50L)   // fadeOut en ticks -> ms
        );
        
        Title displayTitle = Title.title(title, subtitle, times);
        
        // Envoyer au joueur
        player.showTitle(displayTitle);
    }
    
    /**
     * Vérifie si un skill existe
     * @param skillId ID du skill
     * @return true si le skill existe
     */
    public boolean hasSkill(String skillId) {
        return skills.containsKey(skillId.toLowerCase());
    }
    
    /**
     * Récupère tous les IDs de skills disponibles
     * @return Set des IDs
     */
    public java.util.Set<String> getSkillIds() {
        return skills.keySet();
    }
    
    /**
     * Classe interne pour stocker les données d'affichage d'un skill
     */
    private static class SkillDisplay {
        final String title;
        final String subtitle;
        
        SkillDisplay(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public double getDisplayRadius() {
        return displayRadius;
    }
}
