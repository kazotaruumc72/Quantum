package com.wynvers.quantum.healthbar;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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
    
    // Espacement vertical ajouté par chaque ligne de texte (newline)
    // Chaque '\n' ajoute environ 0.3 blocs de hauteur verticale
    // Cette valeur est une approximation basée sur le rendu client
    private static final double VERTICAL_SPACING_PER_NEWLINE = 0.3;
    
    private final Quantum plugin;
    private final Map<UUID, HealthBarMode> playerModes = new HashMap<>();
    private final DecimalFormat percentFormat = new DecimalFormat("0.0");
    private File configFile;
    private FileConfiguration config;
    
    // Configuration des mobs
    private File mobConfigFile;
    private FileConfiguration mobConfig;
    
    public HealthBarManager(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
        loadMobConfig();
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
     * Charge la configuration des mobs
     */
    private void loadMobConfig() {
        mobConfigFile = new File(plugin.getDataFolder(), "mob_healthbar.yml");
        
        if (!mobConfigFile.exists()) {
            plugin.saveResource("mob_healthbar.yml", false);
        }
        
        mobConfig = YamlConfiguration.loadConfiguration(mobConfigFile);
    }
    
    /**
     * Recharge les configurations
     */
    public void reload() {
        loadConfig();
        loadMobConfig();
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
     * Vérifie si le système est activé pour un type de mob
     */
    private boolean isMobEnabled(LivingEntity entity) {
        // Vérifier la config globale
        if (!mobConfig.getBoolean("global.enabled", true)) {
            return false;
        }
        
        // Vérifier si le mob a un nom custom dans la config
        String customName = entity.getCustomName();
        if (customName != null) {
            customName = customName.replace("§", "&"); // Normaliser les codes couleur
            if (customName.contains("\n")) {
                customName = customName.split("\n")[0]; // Extraire juste le nom
            }
            customName = customName.replaceAll("&[0-9a-fk-or]", ""); // Retirer les codes couleur
            
            if (mobConfig.contains("\"" + customName + "\"")) {
                return mobConfig.getBoolean("\"" + customName + "\".enabled", true);
            }
        }
        
        // Vérifier par type de mob
        String mobType = entity.getType().name();
        return mobConfig.getBoolean(mobType + ".enabled", true);
    }
    
    /**
     * Récupère la configuration d'un mob
     */
    private ConfigurationSection getMobConfig(LivingEntity entity) {
        // Chercher d'abord par nom custom
        String customName = entity.getCustomName();
        if (customName != null) {
            customName = customName.replace("§", "&");
            if (customName.contains("\n")) {
                customName = customName.split("\n")[0];
            }
            customName = customName.replaceAll("&[0-9a-fk-or]", "");
            
            ConfigurationSection section = mobConfig.getConfigurationSection("\"" + customName + "\"");
            if (section != null) {
                return section;
            }
        }
        
        // Sinon par type
        String mobType = entity.getType().name();
        return mobConfig.getConfigurationSection(mobType);
    }
    
    /**
     * Génère la barre de vie formatée pour un mob
     */
    public String generateHealthBar(LivingEntity entity, HealthBarMode mode) {
        return generateHealthBar(entity, mode, false);
    }
    
    /**
     * Génère la barre de vie formatée pour un mob avec indicateur ModelEngine optionnel
     */
    public String generateHealthBar(LivingEntity entity, HealthBarMode mode, boolean hasModelEngine) {
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        double percentage = (health / maxHealth) * 100.0;
        
        ConfigurationSection mobSection = getMobConfig(entity);
        
        // Récupérer le format depuis la config si disponible
        String format = "CLASSIC";
        if (mobSection != null && mobSection.contains("format")) {
            format = mobSection.getString("format", "CLASSIC");
        }
        
        // Override avec le mode du joueur si pas de format spécifique
        if (format.equals("CLASSIC") && mode == HealthBarMode.HEARTS) {
            format = "HEARTS";
        }
        
        // Générer selon le format
        switch (format.toUpperCase()) {
            case "HEARTS":
                return generateHeartsDisplay(entity, health, maxHealth, mobSection, hasModelEngine);
            case "NUMERIC":
                return generateNumericDisplay(entity, health, maxHealth, mobSection, hasModelEngine);
            case "BOSS_BAR":
            case "CLASSIC":
            default:
                return generateClassicDisplay(entity, health, maxHealth, percentage, mobSection, hasModelEngine);
        }
    }
    
    /**
     * Affichage classique avec barre de couleur
     */
    private String generateClassicDisplay(LivingEntity entity, double health, double maxHealth, 
                                          double percentage, ConfigurationSection mobSection, boolean hasModelEngine) {
        String color = getHealthColor(entity, percentage, mobSection);
        
        // Longueur de la barre depuis la config
        int barLength = 20;
        if (mobSection != null && mobSection.contains("bar_length")) {
            barLength = mobSection.getInt("bar_length", 20);
        }
        
        int filledBars = (int) ((health / maxHealth) * barLength);
        
        // Symboles depuis la config
        String filledSymbol = mobConfig.getString("symbols.bar.filled", "|");
        String emptySymbol = mobConfig.getString("symbols.bar.empty", "|");
        String leftBracket = mobConfig.getString("symbols.bar.left_bracket", "[");
        String rightBracket = mobConfig.getString("symbols.bar.right_bracket", "]");
        
        StringBuilder bar = new StringBuilder();
        
        // Ajouter l'indicateur ModelEngine au début si applicable
        if (hasModelEngine) {
            String meIndicator = mobConfig.getString("symbols.modelengine.indicator", "⚙");
            String meColor = mobConfig.getString("symbols.modelengine.color", "&7").replace("&", "§");
            bar.append(meColor).append(meIndicator).append(" ");
        }
        
        bar.append("§8").append(leftBracket);
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append(color).append(filledSymbol);
            } else {
                bar.append("§7").append(emptySymbol);
            }
        }
        bar.append("§8" + rightBracket);
        
        // Afficher pourcentage si activé
        boolean showPercentage = true;
        if (mobSection != null && mobSection.contains("show_percentage")) {
            showPercentage = mobSection.getBoolean("show_percentage", true);
        }
        
        if (showPercentage) {
            bar.append(" ").append(color).append(percentFormat.format(percentage)).append("%");
        }
        
        // Afficher numérique si activé
        boolean showNumeric = false;
        if (mobSection != null && mobSection.contains("show_numeric")) {
            showNumeric = mobSection.getBoolean("show_numeric", false);
        }
        
        if (showNumeric) {
            bar.append(" ").append(color)
               .append(percentFormat.format(health))
               .append("§7/")
               .append(percentFormat.format(maxHealth))
               .append(" HP");
        }
        
        return bar.toString();
    }
    
    /**
     * Affichage en cœurs
     */
    private String generateHeartsDisplay(LivingEntity entity, double health, double maxHealth,
                                        ConfigurationSection mobSection, boolean hasModelEngine) {
        double percentage = (health / maxHealth) * 100.0;
        String color = getHealthColor(entity, percentage, mobSection);
        
        // Symboles depuis la config
        String fullHeart = mobConfig.getString("symbols.hearts.full", "❤");
        String halfHeart = mobConfig.getString("symbols.hearts.half", "♡");
        String emptyHeart = mobConfig.getString("symbols.hearts.empty", "♡");
        
        // Convertir la vie en cœurs (1 cœur = 2 HP)
        double hearts = health / 2.0;
        double maxHearts = maxHealth / 2.0;
        
        int fullHearts = (int) hearts;
        boolean hasHalfHeart = (hearts - fullHearts) >= 0.5;
        
        StringBuilder display = new StringBuilder();
        
        // Ajouter l'indicateur ModelEngine au début si applicable
        if (hasModelEngine) {
            String meIndicator = mobConfig.getString("symbols.modelengine.indicator", "⚙");
            String meColor = mobConfig.getString("symbols.modelengine.color", "&7").replace("&", "§");
            display.append(meColor).append(meIndicator).append(" ");
        }
        
        display.append(color);
        
        // Afficher les cœurs pleins
        for (int i = 0; i < fullHearts; i++) {
            display.append(fullHeart);
        }
        
        // Afficher le demi-cœur si nécessaire
        if (hasHalfHeart) {
            display.append("§c").append(halfHeart);
        }
        
        // Afficher les cœurs vides restants (max 10 cœurs affichés)
        int displayedHearts = fullHearts + (hasHalfHeart ? 1 : 0);
        int maxDisplayHearts = Math.min((int) maxHearts, 10);
        
        for (int i = displayedHearts; i < maxDisplayHearts; i++) {
            display.append("§7").append(emptyHeart);
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
     * Affichage numérique uniquement
     */
    private String generateNumericDisplay(LivingEntity entity, double health, double maxHealth,
                                         ConfigurationSection mobSection, boolean hasModelEngine) {
        double percentage = (health / maxHealth) * 100.0;
        String color = getHealthColor(entity, percentage, mobSection);
        
        StringBuilder display = new StringBuilder();
        
        // Ajouter l'indicateur ModelEngine au début si applicable
        if (hasModelEngine) {
            String meIndicator = mobConfig.getString("symbols.modelengine.indicator", "⚙");
            String meColor = mobConfig.getString("symbols.modelengine.color", "&7").replace("&", "§");
            display.append(meColor).append(meIndicator).append(" ");
        }
        
        display.append(color).append(percentFormat.format(health))
               .append("§7/")
               .append(percentFormat.format(maxHealth))
               .append(" HP");
        
        return display.toString();
    }
    
    /**
     * Retourne la couleur selon le pourcentage de vie et la config du mob
     */
    private String getHealthColor(LivingEntity entity, double percentage, ConfigurationSection mobSection) {
        // Utiliser les seuils de couleur de la config si disponibles
        if (mobSection != null && mobSection.contains("color_thresholds")) {
            ConfigurationSection thresholds = mobSection.getConfigurationSection("color_thresholds");
            if (thresholds != null) {
                // Parcourir les seuils dans l'ordre décroissant
                for (int threshold = 100; threshold >= 0; threshold -= 5) {
                    if (percentage >= threshold && thresholds.contains(String.valueOf(threshold))) {
                        return thresholds.getString(String.valueOf(threshold)).replace("&", "§");
                    }
                }
            }
        }
        
        // Couleurs par défaut
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
     * Récupère le nom d'affichage d'un mob depuis la config
     */
    private String getDisplayName(LivingEntity entity, ConfigurationSection mobSection) {
        if (mobSection != null && mobSection.contains("display_name")) {
            return mobSection.getString("display_name").replace("&", "§");
        }
        
        // Nom par défaut
        String mobType = entity.getType().name().replace("_", " ");
        return "§f" + mobType.substring(0, 1).toUpperCase() + mobType.substring(1).toLowerCase();
    }
    
    /**
     * Met à jour le nom custom d'un mob avec sa barre de vie
     */
    public void updateMobHealthDisplay(LivingEntity entity, Player viewer) {
        if (entity instanceof Player) return; // Ne pas afficher pour les joueurs
        
        // Vérifier si activé pour ce mob
        if (!isMobEnabled(entity)) return;
        
        // Vérifier hide_at_full_health
        if (mobConfig.getBoolean("global.hide_at_full_health", false)) {
            if (entity.getHealth() >= entity.getMaxHealth()) {
                return;
            }
        }
        
        ConfigurationSection mobSection = getMobConfig(entity);
        HealthBarMode mode = getMode(viewer);
        
        // Vérifier si l'entité utilise un modèle ModelEngine
        boolean hasModelEngine = false;
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            try {
                // ModelEngineAPI.getModeledEntity() retourne null si l'entité n'a pas de modèle
                hasModelEngine = ModelEngineAPI.getModeledEntity(entity.getUniqueId()) != null;
            } catch (IllegalStateException e) {
                // ModelEngine n'est pas complètement chargé
                hasModelEngine = false;
            }
        }
        
        String healthBar = generateHealthBar(entity, mode, hasModelEngine);
        
        // Récupérer le nom d'origine du mob
        String originalName = entity.getCustomName();
        if (originalName != null && originalName.contains("\n")) {
            originalName = originalName.split("\n")[0];
        }
        
        // Si pas de nom custom et qu'on ne doit pas override
        if (originalName == null || originalName.isEmpty()) {
            originalName = getDisplayName(entity, mobSection);
        } else if (!mobConfig.getBoolean("global.override_custom_names", false)) {
            // Garder le nom custom d'origine
        }
        
        // Calculer l'offset pour les modèles ModelEngine
        String offsetNewlines = "";
        if (hasModelEngine) {
            double offset = getModelEngineOffset(mobSection);
            if (offset > 0) {
                // Ajouter des lignes vides proportionnelles à l'offset
                // Chaque '\n' ajoute environ VERTICAL_SPACING_PER_NEWLINE blocs de hauteur
                int numLines = (int) Math.round(offset / VERTICAL_SPACING_PER_NEWLINE);
                offsetNewlines = "\n".repeat(Math.max(0, numLines));
            }
        }
        
        // Construire le nouveau nom avec la barre de vie et l'offset
        String newName = offsetNewlines + originalName + "\n" + healthBar;
        
        entity.setCustomName(newName);
        entity.setCustomNameVisible(true);
    }
    
    /**
     * Récupère l'offset vertical pour les modèles ModelEngine depuis la configuration
     */
    private double getModelEngineOffset(ConfigurationSection mobSection) {
        if (mobSection != null && mobSection.contains("modelengine_offset")) {
            return mobSection.getDouble("modelengine_offset", 0.0);
        }
        // Utiliser l'offset par défaut de la configuration globale
        return mobConfig.getDouble("global.default_modelengine_offset", 0.0);
    }
}
