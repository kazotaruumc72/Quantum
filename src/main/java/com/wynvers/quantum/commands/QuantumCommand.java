package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class QuantumCommand implements CommandExecutor {

    private final Quantum plugin;
    private final OrdersAdminCommand ordersAdminCommand;
    private final StatsCommand statsCommand;
    private final StorageStatsCommand storageStatsCommand;
    private final QuantumTowerCommand towerCommand;
    private final StructureCommand structureCommand;

    public QuantumCommand(Quantum plugin) {
        this.plugin = plugin;
        this.ordersAdminCommand = new OrdersAdminCommand(plugin);
        this.statsCommand = new StatsCommand(plugin);
        this.storageStatsCommand = new StorageStatsCommand(plugin);
        this.towerCommand = new QuantumTowerCommand(
            plugin, 
            plugin.getTowerManager(), 
            plugin.getDoorManager(), 
            plugin.getNPCManager(), 
            plugin.getLootManager()
        );
        this.structureCommand = new StructureCommand(plugin, plugin.getStructureSelectionManager());
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
        
        // Structure commands - delegate to StructureCommand
        if (subCommand.equals("structure")) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            return structureCommand.onCommand(sender, command, label, newArgs);
        }
        
        // Tower-related commands - delegate to QuantumTowerCommand
        if (subCommand.equals("tower") || subCommand.equals("door") || subCommand.equals("npc") ||
            subCommand.equals("progress") || subCommand.equals("reset") || 
            subCommand.equals("info") || subCommand.equals("mobspawnzone")) {
            return towerCommand.onCommand(sender, command, label, args);
        }

        switch (subCommand) {
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

                        case "mobskills":
                        case "mob_skills":
                        case "mob_skills.yml":
                            if (plugin.getMobSkillManager() != null) {
                                plugin.getMobSkillManager().reload();
                                sender.sendMessage("§a§l✓ §amob_skills.yml rechargé!");
                            }
                            break;

                        case "mobanimations":
                        case "mob_animations":
                            if (plugin.getMobAnimationManager() != null) {
                                plugin.getMobAnimationManager().reload();
                                sender.sendMessage("§a§l✓ §aMob animations rechargées!");
                            }
                            break;

                        case "healthbar":
                        case "healthbars":
                        case "mob_healthbar":
                        case "mob_healthbar.yml":
                            if (plugin.getHealthBarManager() != null) {
                                plugin.getHealthBarManager().reload();
                                sender.sendMessage("§a§l✓ §amob_healthbar.yml rechargé!");
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

                        case "tools":
                        case "tools.yml":
                            if (plugin.getToolManager() != null) {
                                plugin.getToolManager().reload();
                                sender.sendMessage("§a§l✓ §atools.yml rechargé!");
                            }
                            break;

                        case "structures":
                        case "structures.yml":
                            if (plugin.getStructureManager() != null) {
                                plugin.getStructureManager().reload();
                                sender.sendMessage("§a§l✓ §astructures.yml rechargé!");
                            }
                            break;

                        case "weapon":
                        case "weapons":
                        case "dungeon_weapon":
                        case "dungeon_weapon.yml":
                            if (plugin.getDungeonWeapon() != null) {
                                plugin.getDungeonWeapon().reload();
                                sender.sendMessage("§a§l✓ §adungeon_weapon.yml rechargé!");
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
        if (plugin.getMobSkillManager() != null) plugin.getMobSkillManager().reload();
        if (plugin.getMobAnimationManager() != null) plugin.getMobAnimationManager().reload();
        if (plugin.getHealthBarManager() != null) plugin.getHealthBarManager().reload();
        if (plugin.getFurnitureManager() != null) plugin.getFurnitureManager().reload();
        if (plugin.getCustomCropManager() != null) plugin.getCustomCropManager().reload();
        if (plugin.getToolManager() != null) plugin.getToolManager().reload();
        if (plugin.getStructureManager() != null) plugin.getStructureManager().reload();
        if (plugin.getDungeonWeapon() != null) plugin.getDungeonWeapon().reload();

        sender.sendMessage("§a§l✓ §aTout a été rechargé avec succès!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("§e/quantum reload [all|runes|config|towers|price|messages|...]");
        sender.sendMessage("§e/quantum stats [category] §7- Afficher les statistiques");
        sender.sendMessage("§e/quantum storagestats §7- Stats du storage");
        if (sender.hasPermission("quantum.structure.wand") || sender.hasPermission("quantum.admin")) {
            sender.sendMessage("§e/quantum structure <wand|create> §7- Gestion des structures");
        }
        if (sender.hasPermission("quantum.admin.orders")) {
            sender.sendMessage("§e/quantum orders button <create|delete>categorie <nom>");
        }
        if (plugin.getTowerManager() != null) {
            sender.sendMessage("§e/quantum tower|door|npc §7- Commandes des tours");
        }
    }
}
