package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.storage.StorageUpgradeManager;
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
    private final QuantumStorageCommand storageAdminCommand;

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
        this.storageAdminCommand = new QuantumStorageCommand(plugin);
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

        // Storage command - delegate to storage menu
        if (subCommand.equals("storage")) {
            return handleStorage(sender, command, args);
        }

        // Storage upgrades commands
        if (subCommand.equals("storages")) {
            return handleStorages(sender, args);
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
        if (plugin.getFurnitureManager() != null) plugin.getFurnitureManager().reload();
        if (plugin.getCustomCropManager() != null) plugin.getCustomCropManager().reload();
        if (plugin.getDungeonUtils() != null) plugin.getDungeonUtils().reload();
        if (plugin.getTowerInventoryManager() != null) plugin.getTowerInventoryManager().reload();
        if (plugin.getJobManager() != null) plugin.getJobManager().reload();

        sender.sendMessage("§a§l✓ §aTout a été rechargé avec succès!");
    }

    private boolean handleStorage(CommandSender sender, Command command, String[] args) {
        // No type specified – open the storage selector menu (players) or show usage (console)
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cUsage: /quantum storage <tower|classic>");
                sender.sendMessage("§7  tower: add|remove   §7|  §7classic: transfer|remove");
                return true;
            }
            Menu selectorMenu = plugin.getMenuManager().getMenu("storage_selector");
            if (selectorMenu != null) {
                selectorMenu.open(player, plugin);
            } else {
                // Fallback if menu file is missing: show usage
                player.sendMessage("§cUsage: /quantum storage <tower|classic>");
                player.sendMessage("§7  tower: add|remove   §7|  §7classic: transfer|remove");
            }
            return true;
        }

        String storageType = args[1].toLowerCase();

        if (storageType.equals("tower")) {
            return handleTowerStorage(sender, command, args);
        } else if (storageType.equals("classic") || storageType.equals("classique")) {
            return handleClassicStorage(sender, command, args);
        } else {
            sender.sendMessage("§cType de storage invalide. Valeurs possibles: §etower§c, §eclassic");
            return true;
        }
    }

    private boolean handleTowerStorage(CommandSender sender, Command command, String[] args) {
        // /quantum storage tower                              → open tower storage GUI
        // /quantum storage tower add <item> <amount> [player] → add to tower storage
        // /quantum storage tower remove <item> <amount> [player] → remove from tower storage

        if (args.length == 2) {
            // Open tower storage GUI
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
                return true;
            }
            if (!player.hasPermission("quantum.tower.storage")) {
                plugin.getMessageManager().sendMessage(player, "system.no-permission");
                return true;
            }
            Menu towerStorageMenu = plugin.getMenuManager().getMenu("tower_storage");
            if (towerStorageMenu != null) {
                towerStorageMenu.open(player, plugin);
            } else {
                plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open");
            }
            return true;
        }

        // Delegate tower storage admin ops to TowerCommand via ["storage", <subcommand>, ...]
        String[] towerArgs = new String[args.length - 1];
        towerArgs[0] = "storage";
        System.arraycopy(args, 2, towerArgs, 1, args.length - 2);
        return new TowerCommand(plugin).onCommand(sender, command, "quantum", towerArgs);
    }

    private boolean handleClassicStorage(CommandSender sender, Command command, String[] args) {
        // /quantum storage classic                           → open classic storage GUI
        // /quantum storage classic transfer <item> [amount]  → transfer to classic storage
        // /quantum storage classic remove <item> [amount]    → remove from classic storage

        if (args.length == 2) {
            // Open classic storage GUI
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
                return true;
            }
            if (!player.hasPermission("quantum.storage.use")) {
                plugin.getMessageManager().sendMessage(player, "system.no-permission");
                return true;
            }
            Menu storageMenu = plugin.getMenuManager().getMenu("storage");
            if (storageMenu != null) {
                storageMenu.open(player, plugin);
            } else {
                plugin.getMessageManager().sendMessage(player, "error.menu.failed-to-open");
            }
            return true;
        }

        // Delegate classic storage admin ops to QuantumStorageCommand via [<subcommand>, ...]
        String[] classicArgs = new String[args.length - 2];
        System.arraycopy(args, 2, classicArgs, 0, args.length - 2);
        return storageAdminCommand.onCommand(sender, command, "quantum", classicArgs);
    }

    private boolean handleStorages(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        // /quantum storages upgrade <classic|tower> <multiplicateur|stack|page> [joueur]
        if (args.length >= 4 && args[1].equalsIgnoreCase("upgrade")) {
            String storageType = args[2].toLowerCase();
            String upgradeType = args[3].toLowerCase();
            Player target = null;

            if (args.length >= 5) {
                target = org.bukkit.Bukkit.getPlayer(args[4]);
                if (target == null) {
                    sender.sendMessage("§cJoueur introuvable: " + args[4]);
                    return true;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("§cUsage: /quantum storages upgrade <classic|tower> <multiplicateur|stack|page> <joueur>");
                return true;
            }

            StorageUpgradeManager manager;
            if (storageType.equals("tower")) {
                manager = plugin.getTowerStorageUpgradeManager();
            } else if (storageType.equals("classic") || storageType.equals("classique")) {
                manager = plugin.getStorageUpgradeManager();
            } else {
                sender.sendMessage("§cType de storage invalide. Valeurs possibles: §eclassic§c (ou §eclassique§c), §etower");
                return true;
            }

            switch (upgradeType) {
                case "multiplicateur":
                    manager.upgradeMultiplier(target, plugin);
                    if (!target.equals(sender)) {
                        sender.sendMessage("§aMultiplicateur §7(" + storageType + ")§a de " + target.getName() + " amélioré!");
                    }
                    break;
                case "stack":
                    manager.upgradeStack(target, plugin);
                    if (!target.equals(sender)) {
                        sender.sendMessage("§aStack §7(" + storageType + ")§a de " + target.getName() + " amélioré!");
                    }
                    break;
                case "page":
                    manager.upgradePage(target, plugin);
                    if (!target.equals(sender)) {
                        sender.sendMessage("§aPages §7(" + storageType + ")§a de " + target.getName() + " améliorées!");
                    }
                    break;
                default:
                    sender.sendMessage("§cType d'upgrade inconnu. Valeurs possibles: multiplicateur, stack, page");
            }
            return true;
        }

        sender.sendMessage("§6§lCOMMANDES STORAGES");
        sender.sendMessage("§e/quantum storages upgrade <classic|tower> multiplicateur [joueur] §7- Améliore le multiplicateur de vente (+x0.5)");
        sender.sendMessage("§e/quantum storages upgrade <classic|tower> stack [joueur] §7- Améliore le stack max (+200 items)");
        sender.sendMessage("§e/quantum storages upgrade <classic|tower> page [joueur] §7- Améliore le nombre de pages (+1, max 5)");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("§e/quantum reload [all|runes|config|towers|price|messages|...]");
        sender.sendMessage("§e/quantum storage §7- Sélectionner un storage (tower/classic)");
        sender.sendMessage("§e/quantum storage <tower|classic> §7- Ouvrir directement un storage");
        sender.sendMessage("§e/quantum stats [category] §7- Afficher les statistiques");
        sender.sendMessage("§e/quantum storagestats §7- Stats du storage");
        if (sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§e/quantum eco <create|delete|balance|give|take|set> §7- Gestion économie");
            sender.sendMessage("§e/quantum storages upgrade <classic|tower> <multiplicateur|stack|page> [joueur] §7- Upgrades de storage");
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
