package com.wynvers.quantum.commands;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerConfig;
import com.wynvers.quantum.towers.TowerManager;
import com.wynvers.quantum.towers.TowerProgress;
import com.wynvers.quantum.towers.storage.PlayerTowerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

/**
 * Tower system command handler
 * /tower progress           - Show progress
 * /tower reset <player>     - Reset progress (admin)
 * /tower tp <tower> <floor> - Teleport to floor (admin)
 * /tower complete <player> <tower> <floor> - Mark floor as complete (admin)
 * /tower reload             - Reload tower configs (admin)
 */
public class TowerCommand implements CommandExecutor {

    private final Quantum plugin;
    private final TowerManager towerManager;

    public TowerCommand(Quantum plugin) {
        this.plugin = plugin;
        this.towerManager = plugin.getTowerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "progress":
                return handleProgress(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "complete":
                return handleComplete(sender, args);
            case "reload":
                return handleReload(sender);
            case "storage":
                return handleStorage(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Show tower progress
     */
    private boolean handleProgress(CommandSender sender, String[] args) {
        Player target;

        // /tower progress [player]
        if (args.length > 1) {
            if (!sender.hasPermission("quantum.tower.progress.others")) {
                sender.sendMessage("§cVous n'avez pas la permission de voir la progression des autres.");
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cVous devez spécifier un joueur depuis la console.");
                return true;
            }
            target = (Player) sender;
        }

        showProgress(sender, target);
        return true;
    }

    /**
     * Display tower progress for a player
     */
    private void showProgress(CommandSender sender, Player target) {
        TowerProgress progress = towerManager.getProgress(target.getUniqueId());
        Map<String, TowerConfig> towers = towerManager.getAllTowers();

        sender.sendMessage("§6§m──────────────────────────────");
        sender.sendMessage("§6§l§nProgression des Tours - " + target.getName());
        sender.sendMessage("");

        // Show progress for each tower
        for (Map.Entry<String, TowerConfig> entry : towers.entrySet()) {
            String towerId = entry.getKey();
            TowerConfig tower = entry.getValue();
            int completed = progress.getFloorProgress(towerId);
            int total = tower.getTotalFloors();

            String status;
            if (completed >= tower.getFinalBossFloor()) {
                status = "§a§l✓ COMPLÉTÉE";
            } else if (completed > 0) {
                status = "§e§lEN COURS";
            } else {
                status = "§7§lNON COMMENCÉE";
            }

            sender.sendMessage(tower.getName() + "§7: §f" + completed + "/" + total + " " + status);

            // Show next boss if in progress
            if (completed > 0 && completed < tower.getFinalBossFloor()) {
                int nextBoss = tower.getNextBossFloor(completed);
                if (nextBoss != -1) {
                    sender.sendMessage("  §7Prochain boss: §cÉtage " + nextBoss);
                }
            }
        }

        sender.sendMessage("");

        // Global stats
        int towersCompleted = progress.getCompletedTowersCount(towers);
        int totalFloors = progress.getTotalFloorsCompleted();
        int maxTowers = towers.size();
        int maxFloors = towers.values().stream()
                .mapToInt(TowerConfig::getTotalFloors)
                .sum();

        sender.sendMessage("§6Tours complétées: §f" + towersCompleted + "/" + maxTowers);
        sender.sendMessage("§6Étages totaux: §f" + totalFloors + "/" + maxFloors);

        // Current location
        String currentTower = progress.getCurrentTower();
        if (currentTower != null) {
            TowerConfig tower = towerManager.getTower(currentTower);
            if (tower != null) {
                sender.sendMessage("");
                sender.sendMessage("§e➤ Actuellement: §f" + tower.getName() + " - Étage " + progress.getCurrentFloor());
            }
        }

        sender.sendMessage("§6§m──────────────────────────────");
    }

    /**
     * Reset player progress
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.reset")) {
            sender.sendMessage("§cVous n'avez pas la permission de reset la progression.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tower reset <joueur> [tower]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[1]);
            return true;
        }

        // Reset specific tower or all
        if (args.length > 2) {
            String towerId = args[2];
            TowerConfig tower = towerManager.getTower(towerId);
            if (tower == null) {
                sender.sendMessage("§cTour introuvable: " + towerId);
                return true;
            }

            towerManager.resetTowerProgress(target.getUniqueId(), towerId);
            sender.sendMessage("§aProgression de " + target.getName() + " reset pour: " + tower.getName());
            target.sendMessage("§eVotre progression pour " + tower.getName() + " a été réinitialisée.");
        } else {
            towerManager.resetProgress(target.getUniqueId());
            sender.sendMessage("§aProgression complète de " + target.getName() + " reset.");
            target.sendMessage("§eVotre progression dans toutes les tours a été réinitialisée.");
        }

        return true;
    }

    /**
     * Teleport to tower floor using the spawn coordinates from towers.yml
     */
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.teleport")) {
            sender.sendMessage("§cVous n'avez pas la permission de téléportation.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /tower tp <tower> <floor>");
            return true;
        }

        Player player = (Player) sender;
        String towerId = args[1];

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            sender.sendMessage("§cTour introuvable: " + towerId);
            sender.sendMessage("§7Tours disponibles: " + String.join(", ", towerManager.getAllTowers().keySet()));
            return true;
        }

        int floor;
        try {
            floor = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNuméro d'étage invalide: " + args[2]);
            return true;
        }

        if (floor < 1 || floor > tower.getTotalFloors()) {
            sender.sendMessage("§cÉtage invalide. Min: 1, Max: " + tower.getTotalFloors());
            return true;
        }

        Location spawnLoc = getFloorSpawnLocation(towerId, floor);
        if (spawnLoc == null) {
            sender.sendMessage("§cAucun point de spawn configuré pour " + tower.getName() + " étage " + floor + ".");
            sender.sendMessage("§7Configurez la section 'spawn' dans towers.yml pour cet étage.");
            return true;
        }

        player.teleport(spawnLoc);
        player.sendMessage("§a§l✓ §aTéléportation vers: §f" + tower.getName() + " §7Étage " + floor);
        towerManager.updateCurrentLocation(player, towerId, floor);
        return true;
    }

    /**
     * Read spawn location from towers.yml for a given tower floor.
     */
    private Location getFloorSpawnLocation(String towerId, int floor) {
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        ConfigurationSection spawnSection = config.getConfigurationSection(
                "towers." + towerId + ".floors." + floor + ".spawn");
        if (spawnSection == null) return null;

        String worldName = spawnSection.getString("world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(
                world,
                spawnSection.getDouble("x"),
                spawnSection.getDouble("y"),
                spawnSection.getDouble("z"),
                (float) spawnSection.getDouble("yaw", 0.0),
                (float) spawnSection.getDouble("pitch", 0.0)
        );
    }

    /**
     * Mark a floor as complete for a player (admin command).
     * Triggers all rewards and events as if the player cleared the floor normally.
     */
    private boolean handleComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.complete")) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cUsage: /tower complete <joueur> <tower> <floor>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[1]);
            return true;
        }

        String towerId = args[2];
        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            sender.sendMessage("§cTour introuvable: " + towerId);
            return true;
        }

        int floor;
        try {
            floor = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNuméro d'étage invalide: " + args[3]);
            return true;
        }

        if (floor < 1 || floor > tower.getTotalFloors()) {
            sender.sendMessage("§cÉtage invalide. Min: 1, Max: " + tower.getTotalFloors());
            return true;
        }

        towerManager.completeFloor(target, towerId, floor);
        if (!target.equals(sender)) {
            sender.sendMessage("§aÉtage §f" + floor + " §ade §f" + tower.getName() + " §amarqué comme complété pour §f" + target.getName() + "§a.");
        }
        return true;
    }

    /**
     * Reload tower configurations
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("quantum.tower.reload")) {
            sender.sendMessage("§cVous n'avez pas la permission de reload.");
            return true;
        }

        towerManager.reload();
        sender.sendMessage("§aConfigurations des tours rechargées! (" + towerManager.getTowerCount() + " tours)");
        return true;
    }

    /**
     * Handle tower storage commands
     */
    private boolean handleStorage(CommandSender sender, String[] args) {
        // /tower storage add <item> <amount> [player]
        if (args.length >= 2 && args[1].equalsIgnoreCase("add")) {
            return handleStorageAdd(sender, args);
        }

        // /tower storage  →  open menu
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }

        if (!player.hasPermission("quantum.tower.storage")) {
            plugin.getMessageManager().sendMessage(player, "system.no-permission");
            return true;
        }

        com.wynvers.quantum.menu.Menu towerStorageMenu = plugin.getMenuManager().getMenu("tower_storage");
        if (towerStorageMenu != null) {
            towerStorageMenu.open(player, plugin);
        } else {
            plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open");
        }

        return true;
    }

    /**
     * /tower storage add <nexo:id|minecraft:id> <amount> [player]
     * Admin command to add items directly to a player's tower storage.
     */
    private boolean handleStorageAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.storage.add")) {
            sender.sendMessage("§cVous n'avez pas la permission d'ajouter des items au tower storage.");
            return true;
        }

        // args: [storage, add, <item>, <amount>, [player]]
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /tower storage add <nexo:id|minecraft:id> <amount> [joueur]");
            return true;
        }

        String itemArg = args[2];
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMontant invalide: " + args[3]);
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cLe montant doit être supérieur à 0.");
            return true;
        }

        // Determine target player
        Player target;
        if (args.length >= 5) {
            target = Bukkit.getPlayer(args[4]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable: " + args[4]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cVous devez spécifier un joueur depuis la console.");
                return true;
            }
            target = (Player) sender;
        }

        PlayerTowerStorage storage = plugin.getTowerStorageManager().getStorage(target);

        if (itemArg.startsWith("nexo:")) {
            String nexoId = itemArg.substring(5);
            if (!NexoItems.exists(nexoId)) {
                sender.sendMessage("§cItem Nexo introuvable: §7" + nexoId);
                return true;
            }
            storage.addNexoItem(nexoId, amount);
            plugin.getTowerStorageManager().save(target.getUniqueId());
            sender.sendMessage("§a§l✓ §aAjouté §e" + amount + "x §fnexo:" + nexoId + " §au tower storage de §f" + target.getName() + "§a.");
            return true;
        }

        if (itemArg.startsWith("minecraft:")) {
            String materialName = itemArg.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                storage.addItem(material, amount);
                plugin.getTowerStorageManager().save(target.getUniqueId());
                sender.sendMessage("§a§l✓ §aAjouté §e" + amount + "x §fminecraft:" + material.name().toLowerCase() + " §au tower storage de §f" + target.getName() + "§a.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cItem Minecraft introuvable: §7" + materialName);
            }
            return true;
        }

        // No prefix — try Nexo first, then vanilla
        if (NexoItems.exists(itemArg)) {
            storage.addNexoItem(itemArg, amount);
            plugin.getTowerStorageManager().save(target.getUniqueId());
            sender.sendMessage("§a§l✓ §aAjouté §e" + amount + "x §fnexo:" + itemArg + " §au tower storage de §f" + target.getName() + "§a.");
            return true;
        }

        try {
            Material material = Material.valueOf(itemArg.toUpperCase());
            storage.addItem(material, amount);
            plugin.getTowerStorageManager().save(target.getUniqueId());
            sender.sendMessage("§a§l✓ §aAjouté §e" + amount + "x §fminecraft:" + material.name().toLowerCase() + " §au tower storage de §f" + target.getName() + "§a.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cItem introuvable: §7" + itemArg);
        }

        return true;
    }

    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§m──────────────────────────────");
        sender.sendMessage("§6§l§nCommandes Tour");
        sender.sendMessage("");
        sender.sendMessage("§e/tower progress §7- Voir votre progression");
        sender.sendMessage("§e/tower progress <joueur> §7- Voir la progression d'un joueur");
        sender.sendMessage("§e/tower storage §7- Ouvrir le tower storage");

        if (sender.hasPermission("quantum.tower.storage.add")) {
            sender.sendMessage("§e/tower storage add <item> <montant> [joueur] §7- Ajouter des items au tower storage");
        }

        if (sender.hasPermission("quantum.tower.reset")) {
            sender.sendMessage("§e/tower reset <joueur> §7- Réinitialiser la progression");
            sender.sendMessage("§e/tower reset <joueur> <tower> §7- Réinitialiser une tour");
        }

        if (sender.hasPermission("quantum.tower.teleport")) {
            sender.sendMessage("§e/tower tp <tower> <floor> §7- Téléportation à un étage");
        }

        if (sender.hasPermission("quantum.tower.complete")) {
            sender.sendMessage("§e/tower complete <joueur> <tower> <floor> §7- Marquer un étage comme complété");
        }

        if (sender.hasPermission("quantum.tower.reload")) {
            sender.sendMessage("§e/tower reload §7- Recharger les configurations");
        }

        sender.sendMessage("§6§m──────────────────────────────");
    }
}
