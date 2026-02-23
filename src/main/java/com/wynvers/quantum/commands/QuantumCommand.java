package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuantumCommand implements CommandExecutor {

    private final Quantum plugin;
    private final OrdersAdminCommand ordersAdminCommand;
    private final StatsCommand statsCommand;
    private final StorageStatsCommand storageStatsCommand;
    private final QuantumTowerCommand towerCommand;
    private final EconomyCommand economyCommand;
    private final WandCommand wandCommand;

    public QuantumCommand(Quantum plugin) {
        this.plugin = plugin;
        this.ordersAdminCommand = new OrdersAdminCommand(plugin);
        this.statsCommand = new StatsCommand(plugin);
        this.storageStatsCommand = new StorageStatsCommand(plugin);
        this.towerCommand = new QuantumTowerCommand(
            plugin,
            plugin.getTowerManager(),
            plugin.getDoorManager(),
            plugin.getNPCManager()
        );
        this.economyCommand = new EconomyCommand(plugin);
        this.wandCommand = new WandCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Commandes Orders
        if (subCommand.equals("orders")) {
            return ordersAdminCommand.onCommand(sender, command, label, args);
        }

        // Commandes Stats
        if (subCommand.equals("stats") || subCommand.equals("statistics")) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            return statsCommand.onCommand(sender, command, label, newArgs);
        }

        // Commandes Storage Stats
        if (subCommand.equals("storagestats") || subCommand.equals("sstats")) {
            return storageStatsCommand.onCommand(sender, command, label, args);
        }

        // Wand commands - delegate to WandCommand
        if (subCommand.equals("wand")) {
            return wandCommand.onCommand(sender, command, label, args);
        }

        // Tower-related commands - delegate to QuantumTowerCommand
        if (subCommand.equals("tower") || subCommand.equals("door") || subCommand.equals("npc") ||
            subCommand.equals("progress") || subCommand.equals("reset") ||
            subCommand.equals("info")) {
            return towerCommand.onCommand(sender, command, label, args);
        }
        
        // Economy commands - delegate to EconomyCommand
        if (subCommand.equals("eco") || subCommand.equals("economy")) {
            return economyCommand.execute(sender, args);
        }

        switch (subCommand) {
            case "setspawn": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                
                Player player = (Player) sender;
                
                if (!sender.hasPermission("quantum.spawn.set")) {
                    sender.sendMessage("§cYou don't have permission to set spawn.");
                    return true;
                }
                
                if (plugin.getSpawnManager() != null) {
                    Location spawnLoc = player.getLocation();
                    if (plugin.getSpawnManager().setSpawn(spawnLoc)) {
                        sender.sendMessage("§a§l✓ §aSpawn location set at: §e" + 
                            String.format("%.1f, %.1f, %.1f", spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()));
                    } else {
                        sender.sendMessage("§c§l✗ §cFailed to set spawn location.");
                    }
                } else {
                    sender.sendMessage("§c⚠ SpawnManager not loaded!");
                }
                return true;
            }
            
            case "setfirstspawn": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                
                Player player = (Player) sender;
                
                if (!sender.hasPermission("quantum.spawn.setfirst")) {
                    sender.sendMessage("§cYou don't have permission to set first spawn.");
                    return true;
                }
                
                if (plugin.getSpawnManager() != null) {
                    Location firstSpawnLoc = player.getLocation();
                    if (plugin.getSpawnManager().setFirstSpawn(firstSpawnLoc)) {
                        sender.sendMessage("§a§l✓ §aFirst spawn location set at: §e" + 
                            String.format("%.1f, %.1f, %.1f", firstSpawnLoc.getX(), firstSpawnLoc.getY(), firstSpawnLoc.getZ()));
                    } else {
                        sender.sendMessage("§c§l✗ §cFailed to set first spawn location.");
                    }
                } else {
                    sender.sendMessage("§c⚠ SpawnManager not loaded!");
                }
                return true;
            }

            case "reload":
                if (!sender.hasPermission("quantum.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }

                if (args.length == 1) {
                    reloadAll(sender);
                } else {
                    String reloadType = args[1].toLowerCase();

                    switch (reloadType) {
                        case "all":
                            reloadAll(sender);
                            break;

                        case "config":
                        case "config.yml":
                            plugin.reloadConfig();
                            sender.sendMessage("§a§l✓ §aconfig.yml rechargé!");
                            break;

                        // Dungeon armor / runes
                        case "armor":
                        case "runes":
                        case "dungeon":
                        case "dungeon.yml":
                        case "dungeon_armor":
                        case "dungeon_armor.yml":
                            if (plugin.getDungeonArmor() != null) {
                                plugin.getDungeonArmor().reload();
                                sender.sendMessage("§a§l✓ §adungeon_armor.yml (Armures & Runes) rechargé!");
                            } else {
                                sender.sendMessage("§c⚠ DungeonArmor (Runes) non chargé!");
                            }
                            break;

                        case "price":
                        case "price.yml":
                            if (plugin.getPriceManager() != null) {
                                plugin.getPriceManager().reload();
                                sender.sendMessage("§a§l✓ §aprice.yml rechargé!");
                            }
                            break;

                        case "menus":
                        case "menu":
                            if (plugin.getMenuManager() != null) {
                                plugin.getMenuManager().reload();
                                int count = plugin.getMenuManager().getMenuCount();
                                sender.sendMessage("§a§l✓ §a" + count + " menus rechargés!");
                            }
                            break;

                        case "messages":
                        case "messages.yml":
                            if (plugin.getMessageManager() != null) {
                                plugin.getMessageManager().reload();
                                sender.sendMessage("§a§l✓ §amessages.yml rechargé!");
                            }
                            if (plugin.getMessagesManager() != null) {
                                plugin.getMessagesManager().reload();
                            }
                            break;

                        case "messages_gui":
                        case "messages_gui.yml":
                            if (plugin.getGuiMessageManager() != null) {
                                plugin.getGuiMessageManager().reload();
                                sender.sendMessage("§a§l✓ §amessages_gui.yml rechargé!");
                            }
                            break;

                        // ⚠ Ancien "zones" supprimé : plus de reloadConfig / getZoneCount ici

                        case "towers":
                        case "tower":
                            if (plugin.getTowerManager() != null) {
                                plugin.getTowerManager().reload();
                                int towers = plugin.getTowerManager().getTowerCount();
                                sender.sendMessage("§a§l✓ §aTours rechargées! (" + towers + " tours)");
                            }
                            break;

                        case "escrow":
                        case "escrow.yml":
                            if (plugin.getEscrowManager() != null) {
                                plugin.getEscrowManager().reload();
                                int deposits = plugin.getEscrowManager().getEscrowCount();
                                sender.sendMessage("§a§l✓ §aescrow.yml rechargé (" + deposits + " dépôts)");
                            }
                            break;

                        case "orders":
                        case "orders_template.yml":
                            if (plugin.getOrderManager() != null) {
                                plugin.getOrderManager().loadItems();
                                sender.sendMessage("§a§l✓ §aorders_template.yml rechargé!");
                            }
                            break;

                        case "stats":
                        case "statistics":
                        case "statistics.yml":
                            if (plugin.getStatisticsManager() != null) {
                                plugin.getStatisticsManager().loadStatistics();
                                sender.sendMessage("§a§l✓ §astatistics.yml rechargé!");
                            }
                            break;

                        case "storage":
                        case "storage.yml":
                            if (plugin.getStorageManager() != null) {
                                plugin.getStorageManager().reload();
                                sender.sendMessage("§a§l✓ §astorage.yml rechargé!");
                            }
                            break;

                        case "animations":
                        case "animation":
                        case "animations.yml":
                            if (plugin.getAnimationManager() != null) {
                                plugin.getAnimationManager().reload();
                                sender.sendMessage("§a§l✓ §aanimations.yml rechargé!");
                            }
                            break;

                        case "scoreboard":
                        case "scoreboard.yml":
                            if (plugin.getScoreboardConfig() != null) {
                                plugin.getScoreboardConfig().reload();
                                // Vider le cache de couleurs pour forcer le re-parsing
                                com.wynvers.quantum.utils.ScoreboardUtils.clearCache();
                                sender.sendMessage("§a§l✓ §ascoreboard.yml rechargé!");
                            }
                            break;

                        case "towerscoreboard":
                        case "tower_scoreboard":
                            if (plugin.getTowerScoreboardHandler() != null) {
                                plugin.getTowerScoreboardHandler().reload();
                                sender.sendMessage("§a§l✓ §aTower scoreboard rechargé!");
                            }
                            break;

                        case "furniture":
                        case "furniture.yml":
                            if (plugin.getFurnitureManager() != null) {
                                plugin.getFurnitureManager().reload();
                                sender.sendMessage("§a§l✓ §afurniture.yml rechargé!");
                            }
                            break;

                        case "crops":
                        case "custom_crops":
                        case "custom_crops.yml":
                            if (plugin.getCustomCropManager() != null) {
                                plugin.getCustomCropManager().reload();
                                sender.sendMessage("§a§l✓ §acustom_crops.yml rechargé!");
                            }
                            break;

                        case "weapons":
                        case "dungeons_utils":
                        case "dungeons_utils.yml":
                            if (plugin.getDungeonUtils() != null) {
                                plugin.getDungeonUtils().reload();
                                sender.sendMessage("§a§l✓ §adungeons_utils.yml (Armes & Outils de Donjon) rechargé!");
                            }
                            break;

                        case "dungeon_items":
                        case "dungeon_items.yml":
                            if (plugin.getTowerInventoryManager() != null) {
                                plugin.getTowerInventoryManager().reload();
                                sender.sendMessage("§a§l✓ §adungeon_items.yml rechargé!");
                            }
                            break;

                        case "jobs":
                        case "jobs.yml":
                            if (plugin.getJobManager() != null) {
                                plugin.getJobManager().reload();
                                sender.sendMessage("§a§l✓ §ajobs.yml rechargé!");
                            }
                            break;

                        default:
                            sender.sendMessage("§c⚠ Type de reload invalide!");
                            break;
                    }
                }
                break;

            case "version":
            case "ver":
                sender.sendMessage("§6§lQUANTUM §f- Version 2.0.0");
                break;

            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void reloadAll(CommandSender sender) {
        sender.sendMessage("§6§l⟳ §6Rechargement de TOUS les fichiers...");

        plugin.reloadConfig();

        if (plugin.getDungeonArmor() != null) plugin.getDungeonArmor().reload();
        if (plugin.getPriceManager() != null) plugin.getPriceManager().reload();
        if (plugin.getMessageManager() != null) plugin.getMessageManager().reload();
        if (plugin.getMessagesManager() != null) plugin.getMessagesManager().reload();
        if (plugin.getGuiMessageManager() != null) plugin.getGuiMessageManager().reload();
        if (plugin.getMenuManager() != null) plugin.getMenuManager().reload();
        // ⚠ Ancien plugin.getZoneManager().reloadConfig() supprimé
        if (plugin.getTowerManager() != null) plugin.getTowerManager().reload();
        if (plugin.getEscrowManager() != null) plugin.getEscrowManager().reload();
        if (plugin.getOrderManager() != null) plugin.getOrderManager().loadItems();
        if (plugin.getStatisticsManager() != null) plugin.getStatisticsManager().loadStatistics();
        if (plugin.getStorageManager() != null) plugin.getStorageManager().reload();
        if (plugin.getAnimationManager() != null) plugin.getAnimationManager().reload();
        if (plugin.getScoreboardConfig() != null) plugin.getScoreboardConfig().reload();
        if (plugin.getTowerScoreboardHandler() != null) plugin.getTowerScoreboardHandler().reload();
        if (plugin.getFurnitureManager() != null) plugin.getFurnitureManager().reload();
        if (plugin.getCustomCropManager() != null) plugin.getCustomCropManager().reload();
        if (plugin.getDungeonUtils() != null) plugin.getDungeonUtils().reload();
        if (plugin.getTowerInventoryManager() != null) plugin.getTowerInventoryManager().reload();
        if (plugin.getJobManager() != null) plugin.getJobManager().reload();

        // Vider le cache de couleurs après tous les rechargements
        com.wynvers.quantum.utils.ScoreboardUtils.clearCache();

        sender.sendMessage("§a§l✓ §aTout a été rechargé avec succès!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("§e/quantum reload [all|runes|config|towers|price|messages|...]");
        sender.sendMessage("§e/quantum stats [category] §7- Afficher les statistiques");
        sender.sendMessage("§e/quantum storagestats §7- Stats du storage");
        if (sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§e/quantum eco <create|delete|balance|give|take|set> §7- Gestion économie");
        }
        if (sender.hasPermission("quantum.tower.door.wand") || sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§e/quantum wand door §7- Baguette de sélection");
        }
        if (sender.hasPermission("quantum.admin.orders")) {
            sender.sendMessage("§e/quantum orders button <create|delete>categorie <nom>");
        }
        if (plugin.getTowerManager() != null) {
            sender.sendMessage("§e/quantum tower|door|npc §7- Commandes des tours");
        }
    }
}
