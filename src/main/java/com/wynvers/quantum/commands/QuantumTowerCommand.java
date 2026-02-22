package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.listeners.DoorSelectionListener;
import com.wynvers.quantum.towers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Commande principale du système de tours
 * Gère: portes, NPC, téléportation, progression, zones
 */
public class QuantumTowerCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final TowerManager towerManager;
    private final TowerDoorManager doorManager;
    private final TowerNPCManager npcManager;
    private final SpawnSelectionManager selectionManager;
    
    public QuantumTowerCommand(Quantum plugin, TowerManager towerManager, 
                              TowerDoorManager doorManager, TowerNPCManager npcManager) {
        this.plugin = plugin;
        this.towerManager = towerManager;
        this.doorManager = doorManager;
        this.npcManager = npcManager;
        this.selectionManager = plugin.getSpawnSelectionManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "tower":
                return handleTower(sender, args);
            case "door":
                return handleDoor(sender, args);
            case "npc":
                return handleNPC(sender, args);
            case "progress":
                return handleProgress(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "reload":
                return handleReload(sender);
            case "mobspawnzone":
                return handleMobSpawnZone(sender, args);
            default:
                sendMainHelp(sender);
                return true;
        }
    }
    
    // ==================== TOWER COMMANDS ====================
    
    private boolean handleTower(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /quantum tower <etage|tp|info>");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "étage":
            case "etage":
            case "tp":
                return handleTowerTeleport(sender, args);
            case "info":
                return handleTowerInfo(sender, args);
            default:
                sender.sendMessage("§cAction inconnue: " + action);
                return true;
        }
    }
    
    private boolean handleTowerTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /quantum tower etage <tower_id> <floor>");
            return true;
        }
        
        Player player = (Player) sender;
        String towerId = args[2];
        
        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            player.sendMessage("§c§l[Tour] §cTour introuvable: " + towerId);
            return true;
        }
        
        int floor;
        try {
            floor = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c§l[Tour] §cÉtage invalide: " + args[3]);
            return true;
        }
        
        if (floor < 1 || floor > tower.getTotalFloors()) {
            player.sendMessage("§c§l[Tour] §cÉtage invalide. Min: 1, Max: " + tower.getTotalFloors());
            return true;
        }
        
        // Charger le spawn point depuis towers.yml
        Location spawnLoc = getFloorSpawnLocation(towerId, floor);
        if (spawnLoc == null) {
            player.sendMessage("§c§l[Tour] §cAucun point de spawn configuré pour cet étage!");
            player.sendMessage("§7Ajoutez la section 'spawn' dans towers.yml pour l'étage " + floor);
            return true;
        }
        
        // Téléporter
        player.teleport(spawnLoc);
        player.sendMessage("§a§l✓ §aTéléportation vers: §f" + tower.getName() + " §7Étage " + floor);
        
        // Mettre à jour la location actuelle
        towerManager.updateCurrentLocation(player, towerId, floor);
        
        return true;
    }
    
    private Location getFloorSpawnLocation(String towerId, int floor) {
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) return null;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        String path = "towers." + towerId + ".floors." + floor + ".spawn";
        
        ConfigurationSection spawnSection = config.getConfigurationSection(path);
        if (spawnSection == null) return null;
        
        String worldName = spawnSection.getString("world");
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
    
    private boolean handleTowerInfo(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /quantum tower info <tower_id>");
            return true;
        }
        
        String towerId = args[2];
        TowerConfig tower = towerManager.getTower(towerId);
        
        if (tower == null) {
            sender.sendMessage("§cTour introuvable: " + towerId);
            return true;
        }
        
        sender.sendMessage("§6§m══════════════════════════════");
        sender.sendMessage("§6§l" + tower.getName());
        sender.sendMessage("");
        sender.sendMessage("§7ID: §f" + towerId);
        sender.sendMessage("§7Étages: §f" + tower.getTotalFloors());
        sender.sendMessage("§7Boss final: §cÉtage " + tower.getFinalBossFloor());
        sender.sendMessage("§7Niveau requis: §f" + tower.getMinLevel() + " - " + tower.getMaxLevel());
        sender.sendMessage("§6§m══════════════════════════════");
        
        return true;
    }
    
    // ==================== DOOR COMMANDS ====================
    
    private boolean handleDoor(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /quantum door <wand|create|delete|list>");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "wand":
                return handleDoorWand(sender);
            case "create":
                return handleDoorCreate(sender, args);
            case "delete":
                return handleDoorDelete(sender, args);
            case "list":
                return handleDoorList(sender);
            default:
                sender.sendMessage("§cAction inconnue: " + action);
                return true;
        }
    }
    
    private boolean handleDoorWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        if (!sender.hasPermission("quantum.tower.door.wand")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        Player player = (Player) sender;
        player.getInventory().addItem(DoorSelectionListener.createWand());
        player.sendMessage("§a§l[Doors] §aHache de sélection reçue!");
        player.sendMessage("§7Clic gauche: Position 1 | Clic droit: Position 2");
        
        return true;
    }
    
    private boolean handleDoorCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        if (!sender.hasPermission("quantum.tower.door.create")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /quantum door create <tower_id> <floor>");
            return true;
        }
        
        Player player = (Player) sender;
        String towerId = args[2];
        
        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            player.sendMessage("§c§l[Doors] §cTour introuvable: " + towerId);
            return true;
        }
        
        int floor;
        try {
            floor = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c§l[Doors] §cÉtage invalide: " + args[3]);
            return true;
        }
        
        if (doorManager.createDoor(player, towerId, floor)) {
            player.sendMessage("§a§l[Doors] §aPorte créée avec succès!");
        }
        
        return true;
    }
    
    private boolean handleDoorDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.door.delete")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /quantum door delete <tower_id> <floor>");
            return true;
        }
        
        String towerId = args[2];
        int floor;
        
        try {
            floor = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§l[Doors] §cÉtage invalide: " + args[3]);
            return true;
        }
        
        if (doorManager.deleteDoor(towerId, floor)) {
            sender.sendMessage("§a§l[Doors] §aPorte supprimée!");
        } else {
            sender.sendMessage("§c§l[Doors] §cAucune porte trouvée pour cet étage.");
        }
        
        return true;
    }
    
    private boolean handleDoorList(CommandSender sender) {
        sender.sendMessage("§6§m══════════════════════════════");
        sender.sendMessage("§6§lListe des portes");
        sender.sendMessage("");
        
        for (String doorId : doorManager.getAllDoorIds()) {
            sender.sendMessage("§7- §f" + doorId);
        }
        
        sender.sendMessage("§6§m══════════════════════════════");
        return true;
    }
    
    // ==================== NPC COMMANDS ====================
    
    private boolean handleNPC(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /quantum npc <set|remove|list>");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "set":
                return handleNPCSet(sender, args);
            case "remove":
                return handleNPCRemove(sender, args);
            case "list":
                return handleNPCList(sender);
            default:
                sender.sendMessage("§cAction inconnue: " + action);
                return true;
        }
    }
    
    private boolean handleNPCSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        if (!sender.hasPermission("quantum.tower.npc.set")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /quantum npc set goto <tower_id> <floor> [model_id]");
            return true;
        }
        
        if (!args[2].equals("goto")) {
            sender.sendMessage("§cType de NPC invalide. Utilisez 'goto'.");
            return true;
        }
        
        Player player = (Player) sender;
        String towerId = args[3];
        
        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            player.sendMessage("§c§l[NPC] §cTour introuvable: " + towerId);
            return true;
        }
        
        int floor;
        try {
            floor = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c§l[NPC] §cÉtage invalide: " + args[4]);
            return true;
        }
        
        String modelId = args.length > 5 ? args[5] : null;
        
        if (npcManager.createNPC(player, towerId, floor, modelId)) {
            player.sendMessage("§a§l[NPC] §aNPC créé avec succès!");
        }
        
        return true;
    }
    
    private boolean handleNPCRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.npc.remove")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /quantum npc remove <uuid>");
            return true;
        }
        
        UUID npcUuid;
        try {
            npcUuid = UUID.fromString(args[2]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c§l[NPC] §cUUID invalide!");
            return true;
        }
        
        if (npcManager.deleteNPC(npcUuid)) {
            sender.sendMessage("§a§l[NPC] §aNPC supprimé!");
        } else {
            sender.sendMessage("§c§l[NPC] §cNPC introuvable.");
        }
        
        return true;
    }
    
    private boolean handleNPCList(CommandSender sender) {
        sender.sendMessage("§6§m══════════════════════════════");
        sender.sendMessage("§6§lListe des NPC");
        sender.sendMessage("");
        
        for (Map.Entry<UUID, TowerNPCManager.NPCConfig> entry : npcManager.getAllNPCs().entrySet()) {
            TowerNPCManager.NPCConfig config = entry.getValue();
            sender.sendMessage("§7- §f" + config.getTowerId() + " §7étage §f" + config.getFloor());
            sender.sendMessage("  §8UUID: " + entry.getKey());
        }
        
        sender.sendMessage("§6§m══════════════════════════════");
        return true;
    }
    
    // ==================== ZONE COMMANDS ====================
    
    private boolean handleMobSpawnZone(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        if (!sender.hasPermission("quantum.tower.mobspawnzone")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }

        // /quantum mobspawnzone create <tower_id> <floor>
        if (args.length < 4 || !args[1].equalsIgnoreCase("create")) {
            sender.sendMessage("§cUsage: /quantum mobspawnzone create <tower_id> <floor>");
            return true;
        }

        Player player = (Player) sender;
        String towerId = args[2];

        TowerConfig tower = towerManager.getTower(towerId);
        if (tower == null) {
            player.sendMessage("§c§l[Zone] §cTour introuvable: " + towerId);
            return true;
        }

        int floor;
        try {
            floor = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c§l[Zone] §cÉtage invalide: " + args[3]);
            return true;
        }

        if (floor < 1 || floor > tower.getTotalFloors()) {
            player.sendMessage("§c§l[Zone] §cÉtage invalide. Min: 1, Max: " + tower.getTotalFloors());
            return true;
        }

        // Récupérer la sélection (Pos1 / Pos2) faite avec la hache en netherite
        Location pos1 = selectionManager.getPos1(player.getUniqueId());
        Location pos2 = selectionManager.getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            player.sendMessage("§c§l[Zone] §cVous devez d'abord définir Pos1 et Pos2 avec la hache en netherite.");
            player.sendMessage("§7Clic gauche bloc = Pos1, Clic droit bloc = Pos2.");
            return true;
        }

        if (pos1.getWorld() == null || pos2.getWorld() == null ||
                !pos1.getWorld().getName().equals(pos2.getWorld().getName())) {
            player.sendMessage("§c§l[Zone] §cPos1 et Pos2 doivent être dans le même monde.");
            return true;
        }

        String worldName = pos1.getWorld().getName();

        double x1 = Math.min(pos1.getX(), pos2.getX());
        double y1 = Math.min(pos1.getY(), pos2.getY());
        double z1 = Math.min(pos1.getZ(), pos2.getZ());
        double x2 = Math.max(pos1.getX(), pos2.getX());
        double y2 = Math.max(pos1.getY(), pos2.getY());
        double z2 = Math.max(pos1.getZ(), pos2.getZ());

        // Enregistrer la zone dans towers.yml
        File towersFile = new File(plugin.getDataFolder(), "towers.yml");
        if (!towersFile.exists()) {
            player.sendMessage("§c§l[Zone] §cFichier towers.yml introuvable.");
            return true;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        String regionPath = "towers." + towerId + ".floors." + floor + ".region";
        config.set(regionPath + ".world", worldName);
        config.set(regionPath + ".x1", x1);
        config.set(regionPath + ".y1", y1);
        config.set(regionPath + ".z1", z1);
        config.set(regionPath + ".x2", x2);
        config.set(regionPath + ".y2", y2);
        config.set(regionPath + ".z2", z2);

        try {
            config.save(towersFile);
        } catch (Exception e) {
            player.sendMessage("§c§l[Zone] §cErreur lors de l'enregistrement de towers.yml: " + e.getMessage());
            return true;
        }

        // Recharger les tours pour prendre en compte la nouvelle région
        towerManager.reload();
        player.sendMessage("§a§l[Zone] §aZone enregistrée pour " + towerId + " étage " + floor + ".");

        return true;
    }
    
    // ==================== OTHER COMMANDS ====================
    
    private boolean handleProgress(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length > 1) {
            if (!sender.hasPermission("quantum.tower.progress.others")) {
                sender.sendMessage("§cVous n'avez pas la permission!");
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
    
    private void showProgress(CommandSender sender, Player target) {
        TowerProgress progress = towerManager.getProgress(target.getUniqueId());
        Map<String, TowerConfig> towers = towerManager.getAllTowers();
        
        sender.sendMessage("§6§m══════════════════════════════");
        sender.sendMessage("§6§l§nProgression - " + target.getName());
        sender.sendMessage("");
        
        for (Map.Entry<String, TowerConfig> entry : towers.entrySet()) {
            String towerId = entry.getKey();
            TowerConfig tower = entry.getValue();
            int completed = progress.getFloorProgress(towerId);
            int total = tower.getTotalFloors();
            
            String status = completed >= tower.getFinalBossFloor() ? "§a§l✓" : "§e➞";
            sender.sendMessage(status + " §f" + tower.getName() + "§7: " + completed + "/" + total);
        }
        
        sender.sendMessage("§6§m══════════════════════════════");
    }
    
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.tower.reset")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /quantum reset <player> [tower_id]");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[1]);
            return true;
        }
        
        if (args.length > 2) {
            String towerId = args[2];
            towerManager.resetTowerProgress(target.getUniqueId(), towerId);
            sender.sendMessage("§aProgression reset pour: " + towerId);
        } else {
            towerManager.resetProgress(target.getUniqueId());
            sender.sendMessage("§aProgression totale reset.");
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("quantum.tower.reload")) {
            sender.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        towerManager.reload();
        doorManager.loadDoors();
        npcManager.loadNPCs();
        if (plugin.getMobConfig() != null) {
            plugin.getMobConfig().reload();
        }
        
        sender.sendMessage("§a§l✓ §aConfigurations rechargées!");
        return true;
    }
    
    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage("§6§m══════════════════════════════");
        sender.sendMessage("§6§l§nQuantum - Système de Tours");
        sender.sendMessage("");
        sender.sendMessage("§e/quantum tower etage <id> <floor> §8- Téléportation");
        sender.sendMessage("§e/quantum door <wand|create|delete|list> §8- Gestion portes");
        sender.sendMessage("§e/quantum npc <set|remove|list> §8- Gestion NPC");
        sender.sendMessage("§e/quantum mobspawnzone create <id> <floor> §8- Définir zone");
        sender.sendMessage("§e/quantum progress [player] §8- Voir progression");
        sender.sendMessage("§e/quantum reset <player> §8- Reset progression");
        sender.sendMessage("§e/quantum reload §8- Recharger configs");
        sender.sendMessage("§6§m══════════════════════════════");
    }
}
