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
    
    public QuantumCommand(Quantum plugin) {
        this.plugin = plugin;
        this.ordersAdminCommand = new OrdersAdminCommand(plugin);
        this.statsCommand = new StatsCommand(plugin);
        this.storageStatsCommand = new StorageStatsCommand(plugin);
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

                        // ✅ ICI : J'ai ajouté tous les noms possibles pour les Runes et le Donjon
                        case "armor":
                        case "runes":           // Pour les Runes
                        case "dungeon":         // Pour le Donjon
                        case "dungeon.yml":     // Nom du fichier
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
                            
                        case "zones":
                        case "zones.yml":
                            if (plugin.getZoneManager() != null) {
                                plugin.getZoneManager().reloadConfig();
                                int zones = plugin.getZoneManager().getZoneCount();
                                sender.sendMessage("§a§l✓ §azones.yml rechargé! (" + zones + " zones)");
                            }
                            break;
                            
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
        
        // ✅ Reload du système Armure + Runes
        if (plugin.getDungeonArmor() != null) {
            plugin.getDungeonArmor().reload();
        }
        
        if (plugin.getPriceManager() != null) plugin.getPriceManager().reload();
        if (plugin.getMessageManager() != null) plugin.getMessageManager().reload();
        if (plugin.getMessagesManager() != null) plugin.getMessagesManager().reload();
        if (plugin.getGuiMessageManager() != null) plugin.getGuiMessageManager().reload();
        if (plugin.getMenuManager() != null) plugin.getMenuManager().reload();
        if (plugin.getZoneManager() != null) plugin.getZoneManager().reloadConfig();
        if (plugin.getTowerManager() != null) plugin.getTowerManager().reload();
        if (plugin.getEscrowManager() != null) plugin.getEscrowManager().reload();
        if (plugin.getOrderManager() != null) plugin.getOrderManager().loadItems();
        if (plugin.getStatisticsManager() != null) plugin.getStatisticsManager().loadStatistics();
        if (plugin.getStorageManager() != null) plugin.getStorageManager().reload();
        if (plugin.getAnimationManager() != null) plugin.getAnimationManager().reload();
        
        sender.sendMessage("§a§l✓ §aTout a été rechargé avec succès!");
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("§e/quantum reload [all|runes|config|...]");
    }
}
