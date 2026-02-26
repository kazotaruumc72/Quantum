package com.wynvers.quantum.jobs;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.database.DatabaseManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Gestionnaire du système de métiers
 * - Gère les métiers, niveaux, XP
 * - Utilise la même courbe d'XP que le système de donjon
 * - Gère les récompenses automatiques
 * - Support des boosters
 */
public class JobManager {
    
    private final Quantum plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Job> jobs;
    private final Map<UUID, JobData> playerJobs;  // Job actif du joueur
    private final Map<UUID, List<ActiveBooster>> activeBoosters;
    private YamlConfiguration config;
    private ActionPreview actionPreview;
    
    public JobManager(Quantum plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.jobs = new HashMap<>();
        this.playerJobs = new HashMap<>();
        this.activeBoosters = new HashMap<>();
        
        loadConfig();
        createTables();
        startBoosterCheckTask();
        
        // Initialize ActionPreview
        this.actionPreview = new ActionPreview(plugin, this);
        
        plugin.getLogger().info("✓ Jobs system loaded! (" + jobs.size() + " jobs available)");
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!configFile.exists()) {
            plugin.saveResource("jobs.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadJobs();
    }
    
    private void loadJobs() {
        jobs.clear();
        
        ConfigurationSection jobsSection = config.getConfigurationSection("jobs");
        if (jobsSection == null) return;
        
        for (String jobId : jobsSection.getKeys(false)) {
            ConfigurationSection jobSection = jobsSection.getConfigurationSection(jobId);
            if (jobSection == null) continue;
            
            String displayName = jobSection.getString("display_name", jobId);
            List<String> description = jobSection.getStringList("description");
            String icon = jobSection.getString("icon", "minecraft:STONE");
            int maxLevel = jobSection.getInt("max_level", 100);

            // Load per-item reward maps for on hit generators, Nexo blocks and Nexo furniture
            Map<String, double[]> validOnHitGenerators = loadItemRewardMap(jobSection, "valid_on_hit_generators");
            Map<String, double[]> validNexoBlocks = loadItemRewardMap(jobSection, "valid_nexo_blocks");
            Map<String, double[]> validNexoFurniture = loadItemRewardMap(jobSection, "valid_nexo_furniture");

            // Load per-mob reward maps for vanilla and MythicMobs mobs
            Map<String, double[]> mobRewards = loadItemRewardMap(jobSection, "mob_rewards");
            Map<String, double[]> mmobsRewards = loadItemRewardMap(jobSection, "valid_mmobs_mob");

            // Charger les actions autorisées
            Map<String, Boolean> allowedActions = new HashMap<>();
            ConfigurationSection actionsSection = jobSection.getConfigurationSection("allowed_actions");
            if (actionsSection != null) {
                for (String actionType : actionsSection.getKeys(false)) {
                    allowedActions.put(actionType, actionsSection.getBoolean(actionType));
                }
            }

            Job job = new Job(jobId, displayName, description, icon, maxLevel,
                validOnHitGenerators, validNexoBlocks, validNexoFurniture,
                mobRewards, mmobsRewards, allowedActions);
            
            // Charger les récompenses de niveau
            ConfigurationSection rewardsSection = jobSection.getConfigurationSection("level_rewards");
            if (rewardsSection != null) {
                for (String levelStr : rewardsSection.getKeys(false)) {
                    int level = Integer.parseInt(levelStr);
                    List<Map<?, ?>> rewardMaps = rewardsSection.getMapList(levelStr);
                    
                    for (Map<?, ?> rewardMap : rewardMaps) {
                        String type = (String) rewardMap.get("type");
                        String value = (String) rewardMap.get("value");
                        int amount = rewardMap.containsKey("amount") ? ((Number) rewardMap.get("amount")).intValue() : 1;
                        int duration = rewardMap.containsKey("duration") ? ((Number) rewardMap.get("duration")).intValue() : 0;
                        boolean dungeonOnly = rewardMap.containsKey("dungeon_only") && (Boolean) rewardMap.get("dungeon_only");
                        
                        JobReward reward = new JobReward(type, value, amount, duration, dungeonOnly);
                        job.addLevelReward(level, reward);
                    }
                }
            }
            
            jobs.put(jobId, job);
        }
    }

    /**
     * Loads a map of item/mob IDs to their per-item [exp, money] rewards from a config section.
     * Each key maps to a sub-section with optional "exp" and "money" fields (-1 = use global default).
     */
    private Map<String, double[]> loadItemRewardMap(ConfigurationSection parent, String sectionKey) {
        Map<String, double[]> result = new HashMap<>();
        ConfigurationSection section = parent.getConfigurationSection(sectionKey);
        if (section == null) return result;
        for (String itemId : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(itemId);
            double exp = (itemSection != null) ? itemSection.getDouble("exp", -1) : -1;
            double money = (itemSection != null) ? itemSection.getDouble("money", -1) : -1;
            result.put(itemId, new double[]{exp, money});
        }
        return result;
    }

    /**
     * Resolves the base [exp, money] rewards using per-item overrides with global fallback.
     * Per-item values of -1 mean "use global default". Returns null if no rewards are available.
     * Array index 0 = exp (int cast), index 1 = money.
     */
    private double[] resolveRewards(double[] itemReward, ConfigurationSection globalRewards) {
        if (globalRewards == null && (itemReward == null || (itemReward[0] < 0 && itemReward[1] < 0))) {
            return null;
        }
        double exp = (itemReward != null && itemReward[0] >= 0)
            ? itemReward[0]
            : (globalRewards != null ? globalRewards.getDouble("exp", 0) : 0);
        double money = (itemReward != null && itemReward[1] >= 0)
            ? itemReward[1]
            : (globalRewards != null ? globalRewards.getDouble("money", 0.0) : 0.0);
        return new double[]{exp, money};
    }

    private void createTables() {
        try (Connection conn = databaseManager.getConnection()) {
            // Table pour les données de métier des joueurs
            String createJobsTable = "CREATE TABLE IF NOT EXISTS quantum_player_jobs (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "job_id VARCHAR(50) NOT NULL," +
                    "level INT NOT NULL DEFAULT 1," +
                    "exp INT NOT NULL DEFAULT 0" +
                    ")";
            
            try (PreparedStatement ps = conn.prepareStatement(createJobsTable)) {
                ps.executeUpdate();
            }
            
            plugin.getLogger().info("✓ Jobs database tables created/verified");
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to create jobs tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calcul de l'XP requise pour passer au niveau suivant
     * Utilise la même formule que le système de donjon (PlayerLevelManager)
     */
    public int getRequiredExp(int level) {
        if (level <= 0) return 100;
        return (int) Math.floor(100 * Math.pow(1.1, level - 1));
    }
    
    /**
     * Charge les données de métier d'un joueur depuis la base de données
     */
    public void loadPlayer(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT job_id, level, exp FROM quantum_player_jobs WHERE uuid = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String jobId = rs.getString("job_id");
                        int level = rs.getInt("level");
                        int exp = rs.getInt("exp");
                        
                        playerJobs.put(uuid, new JobData(uuid, jobId, level, exp));
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to load job data for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sauvegarde les données de métier d'un joueur
     */
    public void savePlayer(UUID uuid) {
        JobData data = playerJobs.get(uuid);
        if (data == null) return;
        
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO quantum_player_jobs (uuid, job_id, level, exp) VALUES (?, ?, ?, ?) " +
                          "ON DUPLICATE KEY UPDATE job_id = ?, level = ?, exp = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, data.getJobId());
                ps.setInt(3, data.getLevel());
                ps.setInt(4, data.getExp());
                ps.setString(5, data.getJobId());
                ps.setInt(6, data.getLevel());
                ps.setInt(7, data.getExp());
                
                ps.executeUpdate();
            }
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to save job data for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Décharge les données d'un joueur du cache
     */
    public void unloadPlayer(UUID uuid) {
        playerJobs.remove(uuid);
        activeBoosters.remove(uuid);
    }
    
    /**
     * Définit le métier d'un joueur
     */
    public boolean setJob(UUID uuid, String jobId) {
        if (!jobs.containsKey(jobId)) {
            return false;
        }
        
        // Créer ou remplacer les données de métier
        JobData data = new JobData(uuid, jobId, 1, 0);
        playerJobs.put(uuid, data);
        
        return true;
    }
    
    /**
     * Supprime le métier d'un joueur
     */
    public boolean removeJob(UUID uuid) {
        JobData data = playerJobs.remove(uuid);
        if (data == null) {
            return false;
        }
        
        // Supprimer également les boosters actifs
        activeBoosters.remove(uuid);
        
        // Supprimer de la base de données
        try (Connection conn = databaseManager.getConnection()) {
            String query = "DELETE FROM quantum_player_jobs WHERE uuid = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
            
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to remove job data for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    /**
     * Récupère le métier actif d'un joueur
     */
    public JobData getPlayerJob(UUID uuid) {
        return playerJobs.get(uuid);
    }
    
    /**
     * Récupère la définition d'un métier
     */
    public Job getJob(String jobId) {
        return jobs.get(jobId);
    }
    
    /**
     * Récupère tous les métiers disponibles
     */
    public Collection<Job> getAllJobs() {
        return jobs.values();
    }
    
    /**
     * Ajoute de l'XP au métier du joueur
     */
    public void addExp(UUID uuid, int amount) {
        JobData data = playerJobs.get(uuid);
        if (data == null) return;
        
        Job job = jobs.get(data.getJobId());
        if (job == null) return;
        
        int exp = data.getExp() + amount;
        int level = data.getLevel();
        
        // Gérer les montées de niveau
        int needed = getRequiredExp(level);
        while (exp >= needed && level < job.getMaxLevel()) {
            exp -= needed;
            level++;
            needed = getRequiredExp(level);
            
            // Notification de level up
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                final int newLevel = level;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String message = config.getString("messages.job_level_up", "")
                        .replace("{job_name}", job.getDisplayName())
                        .replace("{level}", String.valueOf(newLevel));
                    player.sendMessage(message);
                });
                
                // Donner les récompenses de niveau
                giveRewards(player, job, newLevel);
            }
        }
        
        // Vérifier si le niveau max est atteint
        if (level >= job.getMaxLevel()) {
            level = job.getMaxLevel();
            exp = 0;
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                String message = config.getString("messages.job_max_level", "")
                    .replace("{job_name}", job.getDisplayName());
                player.sendMessage(message);
            }
        }
        
        data.setLevel(level);
        data.setExp(exp);
    }
    
    /**
     * Donne les récompenses pour un niveau atteint
     */
    private void giveRewards(Player player, Job job, int level) {
        List<JobReward> rewards = job.getLevelRewards(level);
        if (rewards.isEmpty()) return;
        
        for (JobReward reward : rewards) {
            giveReward(player, reward, job);
        }
    }
    
    /**
     * Donne une récompense spécifique à un joueur
     */
    private void giveReward(Player player, JobReward reward, Job job) {
        String type = reward.getType();
        String value = reward.getValue();
        int amount = reward.getAmount();
        
        switch (type) {
            case "console_command":
                String consoleCmd = value.replace("{player}", player.getName());
                Bukkit.getScheduler().runTask(plugin, () -> 
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd)
                );
                break;
                
            case "player_command":
                String playerCmd = value.replace("{player}", player.getName());
                Bukkit.getScheduler().runTask(plugin, () -> 
                    player.performCommand(playerCmd)
                );
                break;
                
            case "money":
                Economy economy = plugin.getVaultManager().getEconomy();
                if (economy != null) {
                    double moneyAmount = Double.parseDouble(value);
                    economy.depositPlayer(player, moneyAmount);
                    String message = config.getString("messages.job_reward_received", "")
                        .replace("{reward}", moneyAmount + "$");
                    player.sendMessage(message);
                }
                break;
                
            case "nexo_item":
                ItemStack nexoItem = NexoItems.itemFromId(value).build();
                if (nexoItem != null) {
                    nexoItem.setAmount(amount);
                    player.getInventory().addItem(nexoItem);
                    String message = config.getString("messages.job_reward_received", "")
                        .replace("{reward}", "Nexo Item: " + value);
                    player.sendMessage(message);
                } else {
                    plugin.getLogger().warning("Failed to give Nexo item (invalid ID): " + value);
                }
                break;
                
            case "mythicmobs_item":
                try {
                    ItemStack mmItem = MythicBukkit.inst().getItemManager().getItemStack(value);
                    if (mmItem != null) {
                        mmItem.setAmount(amount);
                        player.getInventory().addItem(mmItem);
                        String message = config.getString("messages.job_reward_received", "")
                            .replace("{reward}", "MythicMobs Item: " + value);
                        player.sendMessage(message);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to give MythicMobs item: " + value);
                }
                break;
                
            case "exp_booster":
                activateBooster(player.getUniqueId(), "exp_booster", 
                    Double.parseDouble(value), reward.getDuration() * 1000L, reward.isDungeonOnly());
                String expMessage = config.getString("messages.booster_activated", "")
                    .replace("{booster_name}", "Booster d'XP")
                    .replace("{multiplier}", value);
                player.sendMessage(expMessage);
                break;
                
            case "money_booster":
                activateBooster(player.getUniqueId(), "money_booster", 
                    Double.parseDouble(value), reward.getDuration() * 1000L, reward.isDungeonOnly());
                String moneyMessage = config.getString("messages.booster_activated", "")
                    .replace("{booster_name}", "Booster d'Argent")
                    .replace("{multiplier}", value);
                player.sendMessage(moneyMessage);
                break;
        }
    }
    
    /**
     * Active un booster pour un joueur
     */
    public void activateBooster(UUID uuid, String boosterType, double multiplier, long duration, boolean dungeonOnly) {
        long expirationTime = System.currentTimeMillis() + duration;
        ActiveBooster booster = new ActiveBooster(uuid, boosterType, multiplier, dungeonOnly, expirationTime);
        
        activeBoosters.computeIfAbsent(uuid, k -> new ArrayList<>()).add(booster);
    }
    
    /**
     * Récupère le multiplicateur d'XP actif pour un joueur
     */
    public double getExpMultiplier(UUID uuid, boolean inDungeon) {
        List<ActiveBooster> boosters = activeBoosters.get(uuid);
        if (boosters == null || boosters.isEmpty()) return 1.0;

        double multiplier = 1.0;
        for (ActiveBooster booster : boosters) {
            if (booster.getBoosterType().equals("exp_booster") && !booster.isExpired()) {
                if (!booster.isDungeonOnly() || inDungeon) {
                    multiplier *= booster.getMultiplier();
                }
            }
        }

        return multiplier;
    }

    /**
     * Récupère le multiplicateur d'XP actif pour un joueur avec bonus d'outil de donjon
     */
    public double getExpMultiplier(Player player, boolean inDungeon) {
        double baseMultiplier = getExpMultiplier(player.getUniqueId(), inDungeon);

        // Check for dungeon utils bonus
        if (plugin.getDungeonUtils() != null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (plugin.getDungeonUtils().isDungeonUtil(item)) {
                if (plugin.getDungeonUtils().canUseForJob(player, item)) {
                    double dungeonBonus = plugin.getDungeonUtils().getJobExpBonus(item);
                    baseMultiplier *= dungeonBonus;
                }
            }
        }

        return baseMultiplier;
    }

    /**
     * Récupère le multiplicateur d'argent actif pour un joueur
     */
    public double getMoneyMultiplier(UUID uuid, boolean inDungeon) {
        List<ActiveBooster> boosters = activeBoosters.get(uuid);
        if (boosters == null || boosters.isEmpty()) return 1.0;

        double multiplier = 1.0;
        for (ActiveBooster booster : boosters) {
            if (booster.getBoosterType().equals("money_booster") && !booster.isExpired()) {
                if (!booster.isDungeonOnly() || inDungeon) {
                    multiplier *= booster.getMultiplier();
                }
            }
        }

        return multiplier;
    }

    /**
     * Récupère le multiplicateur d'argent actif pour un joueur avec bonus d'outil de donjon
     */
    public double getMoneyMultiplier(Player player, boolean inDungeon) {
        double baseMultiplier = getMoneyMultiplier(player.getUniqueId(), inDungeon);

        // Check for dungeon utils bonus
        if (plugin.getDungeonUtils() != null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (plugin.getDungeonUtils().isDungeonUtil(item)) {
                if (plugin.getDungeonUtils().canUseForJob(player, item)) {
                    double dungeonBonus = plugin.getDungeonUtils().getJobMoneyBonus(item);
                    baseMultiplier *= dungeonBonus;
                }
            }
        }

        return baseMultiplier;
    }
    
    /**
     * Traite une action générique pour un job
     * @param player Le joueur qui effectue l'action
     * @param actionType Type d'action (break, place, hit, fish, drink, eat, kill)
     * @param itemId ID de l'item/block concerné (optionnel)
     */
    public void handleAction(Player player, String actionType, String itemId) {
        JobData jobData = playerJobs.get(player.getUniqueId());
        if (jobData == null) {
            return; // Pas de message si pas de job, on laisse le joueur jouer normalement
        }
        
        Job job = jobs.get(jobData.getJobId());
        if (job == null) return;
        
        // Vérifier si l'action est autorisée pour ce métier
        if (!job.isActionAllowed(actionType)) {
            return; // Action non autorisée pour ce job, on ignore silencieusement
        }
        
        // For mob-related actions, check for per-mob reward overrides
        double[] mobReward = null;
        if ((actionType.equals("kill") || actionType.equals("hit")) && itemId != null) {
            mobReward = job.getMobReward(itemId);
        }

        double[] resolved = resolveRewards(mobReward, null);
        if (resolved == null) return;

        int baseExp = (int) resolved[0];
        double baseMoney = resolved[1];
        
        // Vérifier si le joueur est dans un donjon
        boolean inDungeon = isPlayerInDungeon(player);

        // Appliquer les multiplicateurs (includes dungeon utils bonus)
        double expMultiplier = getExpMultiplier(player, inDungeon);
        double moneyMultiplier = getMoneyMultiplier(player, inDungeon);
        
        int finalExp = (int) (baseExp * expMultiplier);
        double finalMoney = baseMoney * moneyMultiplier;
        
        // Donner XP et argent
        if (finalExp > 0) {
            addExp(player.getUniqueId(), finalExp);
        }
        
        Economy economy = plugin.getVaultManager().getEconomy();
        if (economy != null && finalMoney > 0) {
            economy.depositPlayer(player, finalMoney);
        }
        
        // Message si des récompenses ont été données
        if (finalExp > 0 || finalMoney > 0) {
            String message = config.getString("messages.action_performed", 
                "&7+{exp} XP {job_name} &7| +{money}$")
                .replace("{exp}", String.valueOf(finalExp))
                .replace("{job_name}", job.getDisplayName())
                .replace("{money}", String.format("%.1f", finalMoney))
                .replace("{action}", actionType);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    /**
     * Vérifie si un joueur est dans un donjon
     */
    private boolean isPlayerInDungeon(Player player) {
        // Vérifier si le joueur est dans une région de tour/donjon
        if (plugin.getTowerManager() != null) {
            return plugin.getTowerManager().getPlayerTower(player) != null;
        }
        return false;
    }
    
    /**
     * Tâche périodique pour vérifier l'expiration des boosters
     */
    private void startBoosterCheckTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new ArrayList<>(activeBoosters.keySet())) {
                List<ActiveBooster> boosters = activeBoosters.get(uuid);
                if (boosters == null) continue;
                
                boosters.removeIf(booster -> {
                    if (booster.isExpired()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            String boosterName = booster.getBoosterType().equals("exp_booster") ? 
                                "Booster d'XP" : "Booster d'Argent";
                            String message = config.getString("messages.booster_expired", "")
                                .replace("{booster_name}", boosterName);
                            player.sendMessage(message);
                        }
                        return true;
                    }
                    return false;
                });
                
                if (boosters.isEmpty()) {
                    activeBoosters.remove(uuid);
                }
            }
        }, 20L * 60L, 20L * 60L);  // Every 60 seconds
    }
    
    /**
     * Récupère le classement d'un joueur pour son métier actuel
     * @return Position dans le classement (1 = premier), ou -1 si pas de métier
     */
    public int getPlayerRank(UUID uuid) {
        JobData playerData = playerJobs.get(uuid);
        if (playerData == null) {
            return -1;
        }
        
        String jobId = playerData.getJobId();
        
        // Récupérer tous les joueurs avec le même métier depuis la base de données
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT uuid, level, exp FROM quantum_player_jobs WHERE job_id = ? ORDER BY level DESC, exp DESC";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, jobId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    int rank = 1;
                    while (rs.next()) {
                        if (rs.getString("uuid").equals(uuid.toString())) {
                            return rank;
                        }
                        rank++;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get player rank: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Récupère le top N des joueurs pour un métier donné
     * @param jobId ID du métier
     * @param limit Nombre de joueurs à récupérer
     * @return Liste des données de joueurs, triée par niveau décroissant
     */
    public List<JobData> getTopPlayers(String jobId, int limit) {
        List<JobData> topPlayers = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT uuid, job_id, level, exp FROM quantum_player_jobs WHERE job_id = ? ORDER BY level DESC, exp DESC LIMIT ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, jobId);
                ps.setInt(2, limit);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String job = rs.getString("job_id");
                        int level = rs.getInt("level");
                        int exp = rs.getInt("exp");
                        
                        topPlayers.add(new JobData(uuid, job, level, exp));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get top players: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Récupère les meilleurs joueurs tous métiers confondus (par niveau total)
     */
    public Map<UUID, Integer> getGlobalTopPlayers(int limit) {
        Map<UUID, Integer> topPlayers = new LinkedHashMap<>();
        
        // Validate limit parameter
        if (limit <= 0) {
            plugin.getQuantumLogger().warning("Invalid limit for getGlobalTopPlayers: " + limit);
            return topPlayers;
        }
        
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT uuid, SUM(level) as total_levels FROM quantum_player_jobs GROUP BY uuid ORDER BY total_levels DESC LIMIT ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, limit);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int totalLevels = rs.getInt("total_levels");
                        topPlayers.put(uuid, totalLevels);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Failed to get global top players: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Récupère les boosters actifs d'un joueur
     */
    public List<ActiveBooster> getActiveBoosters(UUID uuid) {
        return activeBoosters.getOrDefault(uuid, new ArrayList<>());
    }
    
    public void reload() {
        loadConfig();
    }
    
    public YamlConfiguration getConfig() {
        return config;
    }
    
    /**
     * Récupère le système de preview d'actions
     */
    public ActionPreview getActionPreview() {
        return actionPreview;
    }
}
