package com.wynvers.quantum.healthbar;

import com.wynvers.quantum.Quantum;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire de l'affichage des barres de vie des mobs
 */
public class HealthBarManager {
    
    private final Quantum plugin;
    private final Map<UUID, HealthBarMode> playerModes = new HashMap<>();
    private final DecimalFormat percentFormat = new DecimalFormat("0.0");
    private File configFile;
    private FileConfiguration config;
    
    public HealthBarManager(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Charge la configuration des préférences des joueurs
     */
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "healthbar.yml");
        
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Charger les préférences
        if (config.contains("preferences")) {
            for (String uuidStr : config.getConfigurationSection("preferences").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                String modeStr = config.getString("preferences." + uuidStr);
                playerModes.put(uuid, HealthBarMode.fromString(modeStr));
            }
        }
    }
    
    /**
     * Sauvegarde la configuration
     */
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère le mode d'affichage d'un joueur
     */
    public HealthBarMode getMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), HealthBarMode.PERCENTAGE);
    }
    
    /**
     * Définit le mode d'affichage d'un joueur
     */
    public void setMode(Player player, HealthBarMode mode) {
        playerModes.put(player.getUniqueId(), mode);
        config.set("preferences." + player.getUniqueId().toString(), mode.name());
        saveConfig();
    }
    
    /**
     * Génère la barre de vie formatée pour un mob
     */
    public String generateHealthBar(LivingEntity entity, HealthBarMode mode) {
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        double percentage = (health / maxHealth) * 100.0;
        
        if (mode == HealthBarMode.HEARTS) {
            return generateHeartsDisplay(health, maxHealth);
        } else {
            return generatePercentageDisplay(health, maxHealth, percentage);
        }
    }
    
    /**
     * Affichage en pourcentage avec barre de couleur
     */
    private String generatePercentageDisplay(double health, double maxHealth, double percentage) {
        String color = getHealthColor(percentage);
        
        // Barre graphique
        int barLength = 20;
        int filledBars = (int) ((health / maxHealth) * barLength);
        
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append(color).append("|");
            } else {
                bar.append("§7|");
            }
        }
        bar.append("§8]");
        
        // Texte
        String healthText = color + percentFormat.format(percentage) + "%";
        
        return bar.toString() + " " + healthText;
    }
    
    /**
     * Affichage en cœurs
     */
    private String generateHeartsDisplay(double health, double maxHealth) {
        double percentage = (health / maxHealth) * 100.0;
        String color = getHealthColor(percentage);
        
        // Convertir la vie en cœurs (1 cœur = 2 HP)
        double hearts = health / 2.0;
        double maxHearts = maxHealth / 2.0;
        
        int fullHearts = (int) hearts;
        boolean hasHalfHeart = (hearts - fullHearts) >= 0.5;
        
        StringBuilder display = new StringBuilder(color);
        
        // Afficher les cœurs pleins
        for (int i = 0; i < fullHearts; i++) {
            display.append("❤");
        }
        
        // Afficher le demi-cœur si nécessaire
        if (hasHalfHeart) {
            display.append("§c♡"); // Demi-cœur
        }
        
        // Afficher les cœurs vides restants (max 10 cœurs affichés)
        int displayedHearts = fullHearts + (hasHalfHeart ? 1 : 0);
        int maxDisplayHearts = Math.min((int) maxHearts, 10);
        
        for (int i = displayedHearts; i < maxDisplayHearts; i++) {
            display.append("§7♡");
        }
        
        // Ajouter le total si > 10 cœurs
        if (maxHearts > 10) {
            display.append("§7 (")
                   .append(color)
                   .append(percentFormat.format(hearts))
                   .append("§7/")
                   .append(percentFormat.format(maxHearts))
                   .append("§7)");
        }
        
        return display.toString();
    }
    
    /**
     * Retourne la couleur selon le pourcentage de vie
     */
    private String getHealthColor(double percentage) {
        if (percentage >= 75) {
            return "§a"; // Vert
        } else if (percentage >= 50) {
            return "§e"; // Jaune
        } else if (percentage >= 25) {
            return "§6"; // Orange
        } else {
            return "§c"; // Rouge
        }
    }
    
    /**
     * Met à jour le nom custom d'un mob avec sa barre de vie
     */
    public void updateMobHealthDisplay(LivingEntity entity, Player viewer) {
        if (entity instanceof Player) return; // Ne pas afficher pour les joueurs
        
        HealthBarMode mode = getMode(viewer);
        String healthBar = generateHealthBar(entity, mode);
        
        // Récupérer le nom d'origine du mob (s'il en a un)
        String originalName = entity.getCustomName();
        if (originalName != null && originalName.contains("\n")) {
            // Si le nom contient déjà une barre de vie, on extrait juste le nom
            originalName = originalName.split("\n")[0];
        }
        
        // Construire le nouveau nom avec la barre de vie
        String newName;
        if (originalName != null && !originalName.isEmpty()) {
            newName = originalName + "\n" + healthBar;
        } else {
            // Si pas de nom custom, utiliser le type du mob
            String mobType = entity.getType().name().replace("_", " ");
            mobType = mobType.substring(0, 1).toUpperCase() + mobType.substring(1).toLowerCase();
            newName = "§f" + mobType + "\n" + healthBar;
        }
        
        entity.setCustomName(newName);
        entity.setCustomNameVisible(true);
    }
}
