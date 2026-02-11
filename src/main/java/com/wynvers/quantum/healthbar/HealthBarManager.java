package com.wynvers.quantum.healthbar;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.wynvers.quantum.Quantum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire de l'affichage des barres de vie des mobs
 * 
 * Utilise des entités TextDisplay (Minecraft 1.19.4+) pour afficher les barres de vie
 * au-dessus des mobs, car les newlines dans les noms custom ne fonctionnent plus
 * dans Minecraft 1.20.5+/1.21+
 */
public class HealthBarManager {
    
    private final Quantum plugin;
    private final Map<UUID, HealthBarMode> playerModes = new HashMap<>();
    private final DecimalFormat percentFormat = new DecimalFormat("0"); // Changed from "0.0" to "0" to show only integers
    private File configFile;
    private FileConfiguration config;
    
    // Configuration des mobs
    private File mobConfigFile;
    private FileConfiguration mobConfig;
    
    // Map pour tracker les TextDisplay entities associées aux mobs
    // Key: UUID du mob, Value: UUID de la TextDisplay
    private final Map<UUID, UUID> mobHealthDisplays = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Cache pour les configurations de mobs pour éviter des lookups YAML répétés
    private final Map<UUID, ConfigurationSection> mobConfigCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Cache pour le status ModelEngine des mobs
    private final Map<UUID, Boolean> modelEngineCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Status du plugin ModelEngine (initialisé une seule fois au démarrage)
    // Note: Si ModelEngine est chargé/déchargé dynamiquement après le démarrage,
    // un redémarrage du serveur sera nécessaire pour mettre à jour ce statut
    private final boolean hasModelEnginePlugin;
    
    // Task ID pour le rafraîchissement périodique des positions
    private int updateTaskId = -1;
    
    public HealthBarManager(Quantum plugin) {
        this.plugin = plugin;
        loadConfig();
        loadMobConfig();
        // Initialiser le cache du plugin ModelEngine dès le départ
        hasModelEnginePlugin = Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
        startPositionUpdateTask();
    }
    
    /**
     * Démarre une tâche périodique pour mettre à jour les positions des TextDisplay
     */
    private void startPositionUpdateTask() {
        // Mise à jour toutes les 3 ticks (~6.6 fois par seconde) pour un mouvement très fluide
        // Optimisé avec seuil de distance pour éviter les mises à jour inutiles
        updateTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Collection pour stocker les entrées invalides à supprimer
            java.util.List<UUID> toRemove = new java.util.ArrayList<>();
            
            // Itérer sur toutes les healthbars actives
            mobHealthDisplays.forEach((mobUUID, displayUUID) -> {
                org.bukkit.entity.Entity mobEntity = Bukkit.getEntity(mobUUID);
                org.bukkit.entity.Entity displayEntity = Bukkit.getEntity(displayUUID);
                
                // Vérifier que les deux entités existent toujours
                if (mobEntity instanceof LivingEntity && mobEntity.isValid() && 
                    displayEntity instanceof TextDisplay && displayEntity.isValid()) {
                    
                    LivingEntity mob = (LivingEntity) mobEntity;
                    TextDisplay display = (TextDisplay) displayEntity;
                    
                    // Calculer l'offset (utiliser cache pour config)
                    ConfigurationSection mobSection = getCachedMobConfig(mob);
                    double yOffset = 0.5; // Défaut
                    
                    // Appliquer l'offset depuis la config (pour tous les mobs, pas seulement ModelEngine)
                    double hologramOffset = getHologramOffset(mobSection);
                    if (hologramOffset > 0) {
                        yOffset = hologramOffset;
                    } else {
                        // Si aucun offset configuré, utiliser la hauteur de l'entité + petit offset
                        // Cela fonctionne mieux pour les mobs ModelEngine avec des modèles de différentes tailles
                        yOffset = mob.getHeight() + 0.3;
                    }
                    
                    // Mettre à jour la position seulement si le mob a bougé de manière significative
                    // Note: distanceSquared retourne le carré de la distance
                    // 0.0004 = (0.02)^2, threshold de 0.02 bloc (2cm) pour un bon équilibre fluidité/performance
                    // Combiné avec 3 ticks d'intervalle, cela donne un mouvement fluide sans surcharge
                    Location newLoc = mob.getLocation().add(0, yOffset, 0);
                    if (newLoc.distanceSquared(display.getLocation()) > 0.0004) {
                        display.teleport(newLoc);
                    }
                } else {
                    // Marquer pour suppression
                    toRemove.add(mobUUID);
                }
            });
            
            // Supprimer les entrées invalides et nettoyer les caches
            toRemove.forEach(this::removeMobFromCaches);
            
        }, 3L, 3L).getTaskId(); // 3 ticks de délai initial, puis toutes les 3 ticks (~6.6 fois/sec)
    }
    
    /**
     * Arrête la tâche de mise à jour et nettoie toutes les TextDisplay
     */
    public void shutdown() {
        if (updateTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
            updateTaskId = -1;
        }
        cleanupAllDisplays();
        // Nettoyer les caches
        mobConfigCache.clear();
        modelEngineCache.clear();
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
        // Nettoyer les caches après reload pour prendre en compte les changements de config
        // Les mobs existants utiliseront les nouvelles configs lors de leur prochaine mise à jour
        mobConfigCache.clear();
        modelEngineCache.clear();
        // Note: hasModelEnginePlugin n'est pas recalculé car le chargement dynamique
        // de plugins n'est pas supporté (redémarrage serveur requis)
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
     * Supprime un mob de tous les caches
     */
    private void removeMobFromCaches(UUID mobUUID) {
        mobHealthDisplays.remove(mobUUID);
        mobConfigCache.remove(mobUUID);
        modelEngineCache.remove(mobUUID);
    }
    
    /**
     * Récupère la configuration d'un mob avec cache
     * Note: Le cache est vidé lors du reload() pour prendre en compte les changements
     * de configuration. Les mobs existants utiliseront les nouvelles configs lors de
     * leur prochaine mise à jour de healthbar.
     */
    private ConfigurationSection getCachedMobConfig(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        return mobConfigCache.computeIfAbsent(uuid, k -> getMobConfig(entity));
    }
    
    /**
     * Récupère le status ModelEngine d'un mob avec cache
     */
    private boolean getModelEngineStatus(LivingEntity mob) {
        UUID uuid = mob.getUniqueId();
        return modelEngineCache.computeIfAbsent(uuid, k -> {
            if (!hasModelEnginePlugin) {
                return false;
            }
            
            try {
                return ModelEngineAPI.getModeledEntity(mob.getUniqueId()) != null;
            } catch (IllegalStateException e) {
                return false;
            }
        });
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
     * Méthode de compatibilité qui délègue à la méthode principale sans indicateur ModelEngine
     * 
     * Generates the formatted health bar for a mob
     * Compatibility method that delegates to the main method without ModelEngine indicator
     */
    public String generateHealthBar(LivingEntity entity, HealthBarMode mode) {
        return generateHealthBar(entity, mode, false);
    }
    
    /**
     * Génère la barre de vie formatée pour un mob avec indicateur ModelEngine optionnel
     * @param entity L'entité pour laquelle générer la barre de vie
     * @param mode Le mode d'affichage (PERCENTAGE, HEARTS, etc.)
     * @param hasModelEngine Si true, ajoute un indicateur visuel pour signaler un modèle ModelEngine
     * @return La chaîne formatée de la barre de vie
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
            case "PERCENTAGE_ONLY":
                return generatePercentageOnlyDisplay(entity, health, maxHealth, percentage, mobSection, hasModelEngine);
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
        
        // Symboles par défaut (la configuration bar a été retirée)
        String filledSymbol = "|";
        String emptySymbol = "|";
        String leftBracket = "[";
        String rightBracket = "]";
        
        StringBuilder bar = new StringBuilder();
        
        // Ajouter l'indicateur ModelEngine au début si applicable
        bar.append(getModelEngineIndicator(hasModelEngine));
        
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
            bar.append(" ").append(color).append(percentFormat.format(percentage));
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
        display.append(getModelEngineIndicator(hasModelEngine));
        
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
        display.append(getModelEngineIndicator(hasModelEngine));
        
        display.append(color).append(percentFormat.format(health))
               .append("§7/")
               .append(percentFormat.format(maxHealth))
               .append(" HP");
        
        return display.toString();
    }
    
    /**
     * Affichage pourcentage uniquement (sans barre)
     * Displays only the percentage number without the health bar visual
     * 
     * Note: health and maxHealth parameters are kept for signature consistency with generateClassicDisplay,
     * even though percentage is pre-calculated and passed in
     */
    private String generatePercentageOnlyDisplay(LivingEntity entity, double health, double maxHealth,
                                                 double percentage, ConfigurationSection mobSection, 
                                                 boolean hasModelEngine) {
        String color = getHealthColor(entity, percentage, mobSection);
        
        StringBuilder display = new StringBuilder();
        
        // Ajouter l'indicateur ModelEngine au début si applicable
        display.append(getModelEngineIndicator(hasModelEngine));
        
        // Afficher uniquement le pourcentage
        display.append(color).append(percentFormat.format(percentage));
        
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
     * Met à jour l'affichage de la barre de vie d'un mob en utilisant une entité TextDisplay
     * 
     * Note: Depuis Minecraft 1.20.5+, les newlines dans setCustomName() ne fonctionnent plus.
     * On utilise donc des TextDisplay entities pour afficher les barres de vie avec un offset vertical précis.
     */
    public void updateMobHealthDisplay(LivingEntity entity, Player viewer) {
        if (entity instanceof Player) return; // Ne pas afficher pour les joueurs
        
        // Vérifier si activé pour ce mob
        if (!isMobEnabled(entity)) {
            // Si désactivé, nettoyer l'affichage existant
            removeHealthDisplay(entity);
            return;
        }
        
        // Vérifier hide_at_full_health
        if (mobConfig.getBoolean("global.hide_at_full_health", false)) {
            if (entity.getHealth() >= entity.getMaxHealth()) {
                removeHealthDisplay(entity);
                return;
            }
        }
        
        ConfigurationSection mobSection = getMobConfig(entity);
        HealthBarMode mode = getMode(viewer);
        
        // Vérifier si l'entité utilise un modèle ModelEngine
        boolean hasModelEngine = false;
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            try {
                hasModelEngine = ModelEngineAPI.getModeledEntity(entity.getUniqueId()) != null;
            } catch (IllegalStateException e) {
                hasModelEngine = false;
            }
        }
        
        String healthBar = generateHealthBar(entity, mode, hasModelEngine);
        
        // Récupérer le nom d'affichage du mob
        String originalName = entity.getCustomName();
        if (originalName == null || originalName.isEmpty()) {
            originalName = getDisplayName(entity, mobSection);
        } else if (!mobConfig.getBoolean("global.override_custom_names", false)) {
            // Backward compatibility: enlever les anciennes newlines si présentes
            // (du système précédent qui utilisait des newlines dans setCustomName)
            originalName = originalName.replace("\n", "").trim();
        }
        
        // Définir le nom custom du mob (sans healthbar)
        entity.setCustomName(originalName);
        entity.setCustomNameVisible(true);
        
        // Calculer l'offset vertical pour tous les mobs
        double baseOffset = 0.5; // Offset par défaut au-dessus de l'entité
        double hologramOffset = getHologramOffset(mobSection);
        if (hologramOffset > 0) {
            baseOffset = hologramOffset;
        } else {
            // Si aucun offset configuré, utiliser la hauteur de l'entité + petit offset
            // Cela fonctionne mieux pour les mobs ModelEngine avec des modèles de différentes tailles
            baseOffset = entity.getHeight() + 0.3;
        }
        
        // Créer ou mettre à jour la TextDisplay entity
        createOrUpdateHealthDisplay(entity, healthBar, baseOffset);
    }
    
    /**
     * Crée ou met à jour une entité TextDisplay pour afficher la healthbar
     */
    private void createOrUpdateHealthDisplay(LivingEntity entity, String healthBar, double yOffset) {
        UUID entityUUID = entity.getUniqueId();
        UUID displayUUID = mobHealthDisplays.get(entityUUID);
        
        TextDisplay display = null;
        
        // Chercher la TextDisplay existante
        if (displayUUID != null) {
            org.bukkit.entity.Entity displayEntity = Bukkit.getEntity(displayUUID);
            if (displayEntity instanceof TextDisplay && displayEntity.isValid()) {
                display = (TextDisplay) displayEntity;
            } else {
                // L'entité n'existe plus, nettoyer
                mobHealthDisplays.remove(entityUUID);
            }
        }
        
        // Créer une nouvelle TextDisplay si nécessaire
        if (display == null) {
            Location spawnLoc = entity.getLocation().add(0, yOffset, 0);
            display = entity.getWorld().spawn(spawnLoc, TextDisplay.class);
            
            // Configuration de la TextDisplay
            display.setBillboard(Display.Billboard.CENTER); // Toujours face au joueur
            display.setSeeThrough(false);
            display.setDefaultBackground(false);
            display.setPersistent(false); // Ne pas sauvegarder avec le monde
            
            // Enregistrer l'UUID
            mobHealthDisplays.put(entityUUID, display.getUniqueId());
        }
        
        // Mettre à jour le texte
        Component healthBarComponent = LegacyComponentSerializer.legacySection().deserialize(healthBar);
        display.text(healthBarComponent);
        
        // Mettre à jour la position pour suivre le mob
        Location newLoc = entity.getLocation().add(0, yOffset, 0);
        display.teleport(newLoc);
    }
    
    /**
     * Supprime l'affichage de healthbar pour un mob
     */
    public void removeHealthDisplay(LivingEntity entity) {
        UUID entityUUID = entity.getUniqueId();
        UUID displayUUID = mobHealthDisplays.get(entityUUID);
        
        if (displayUUID != null) {
            org.bukkit.entity.Entity displayEntity = Bukkit.getEntity(displayUUID);
            if (displayEntity != null && displayEntity.isValid()) {
                displayEntity.remove();
            }
        }
        
        // Nettoyer les caches pour ce mob
        removeMobFromCaches(entityUUID);
    }
    
    /**
     * Nettoie tous les affichages de healthbar
     */
    public void cleanupAllDisplays() {
        for (UUID displayUUID : mobHealthDisplays.values()) {
            org.bukkit.entity.Entity displayEntity = Bukkit.getEntity(displayUUID);
            if (displayEntity != null && displayEntity.isValid()) {
                displayEntity.remove();
            }
        }
        mobHealthDisplays.clear();
        // Nettoyer tous les caches
        mobConfigCache.clear();
        modelEngineCache.clear();
    }
    
    /**
     * Récupère l'offset vertical pour l'hologramme de healthbar depuis la configuration
     */
    private double getHologramOffset(ConfigurationSection mobSection) {
        if (mobSection != null && mobSection.contains("hologram_offset")) {
            return mobSection.getDouble("hologram_offset", 0.0);
        }
        // Backward compatibility: check for old modelengine_offset
        if (mobSection != null && mobSection.contains("modelengine_offset")) {
            return mobSection.getDouble("modelengine_offset", 0.0);
        }
        // Utiliser l'offset par défaut de la configuration globale
        double defaultOffset = mobConfig.getDouble("global.default_hologram_offset", 0.0);
        if (defaultOffset == 0.0) {
            // Backward compatibility: check for old default_modelengine_offset
            defaultOffset = mobConfig.getDouble("global.default_modelengine_offset", 0.0);
        }
        return defaultOffset;
    }
    
    /**
     * Récupère l'indicateur visuel ModelEngine formaté si applicable
     * @param hasModelEngine Si l'entité utilise un modèle ModelEngine
     * @return L'indicateur formaté avec couleur et espace, ou chaîne vide si pas de ModelEngine
     */
    private String getModelEngineIndicator(boolean hasModelEngine) {
        if (!hasModelEngine) {
            return "";
        }
        
        String meIndicator = mobConfig.getString("symbols.modelengine.indicator", "⚙");
        String meColor = mobConfig.getString("symbols.modelengine.color", "&7").replace("&", "§");
        return meColor + meIndicator + " ";
    }
    
    /**
     * Applique un modèle ModelEngine à une entité si configuré dans mob_healthbar.yml
     * @param entity L'entité à laquelle appliquer le modèle
     * @return true si un modèle a été appliqué avec succès, false sinon
     */
    public boolean applyModelEngineModel(LivingEntity entity) {
        // Vérifier si ModelEngine est disponible
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") == null) {
            return false;
        }
        
        // Récupérer la configuration du mob
        ConfigurationSection mobSection = getMobConfig(entity);
        if (mobSection == null || !mobSection.contains("model")) {
            return false;
        }
        
        String modelId = mobSection.getString("model");
        if (modelId == null || modelId.isEmpty()) {
            return false;
        }
        
        try {
            // Vérifier si l'entité a déjà un modèle
            // Retourne false silencieusement car c'est un cas normal (modèle déjà appliqué par un autre système)
            if (ModelEngineAPI.getModeledEntity(entity.getUniqueId()) != null) {
                return false; // Déjà un modèle appliqué, pas besoin de logger
            }
            
            // Créer et appliquer le modèle
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
            
            if (activeModel == null) {
                plugin.getQuantumLogger().warning("ModelEngine model not found: " + modelId + " for mob: " + entity.getType());
                return false;
            }
            
            modeledEntity.addModel(activeModel, true);
            
            plugin.getQuantumLogger().info("Applied ModelEngine model '" + modelId + "' to " + entity.getType() + " at " + entity.getLocation());
            return true;
            
        } catch (Exception e) {
            plugin.getQuantumLogger().warning("Failed to apply ModelEngine model '" + modelId + "' to " + entity.getType() + ": " + e.getMessage());
            return false;
        }
    }
}
