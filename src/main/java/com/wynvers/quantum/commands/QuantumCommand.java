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
        
        // Déléguer les commandes orders au OrdersAdminCommand
        if (subCommand.equals("orders")) {
            return ordersAdminCommand.onCommand(sender, command, label, args);
        }
        
        // Déléguer les commandes stats au StatsCommand
        if (subCommand.equals("stats") || subCommand.equals("statistics")) {
            // Créer un nouveau tableau d'arguments sans le "stats"
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            return statsCommand.onCommand(sender, command, label, newArgs);
        }
        
        // Déléguer les commandes storagestats au StorageStatsCommand
        if (subCommand.equals("storagestats") || subCommand.equals("sstats")) {
            return storageStatsCommand.onCommand(sender, command, label, args);
        }
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("quantum.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                
                // Si aucun argument spécifique, reload tout
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
                            
                        case "price":
                        case "price.yml":
                        case "prices":
                            if (plugin.getPriceManager() != null) {
                                plugin.getPriceManager().reload();
                                sender.sendMessage("§a§l✓ §aprice.yml rechargé!");
                            } else {
                                sender.sendMessage("§c⚠ PriceManager non initialisé!");
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
                        case "gui":
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
                            } else {
                                sender.sendMessage("§c⚠ WorldGuard non disponible!");
                            }
                            break;
                            
                        case "towers":
                        case "tower":
                            if (plugin.getTowerManager() != null) {
                                plugin.getTowerManager().reload();
                                int towers = plugin.getTowerManager().getTowerCount();
                                sender.sendMessage("§a§l✓ §aTours rechargées! (" + towers + " tours)");
                            } else {
                                sender.sendMessage("§c⚠ Tour system non disponible!");
                            }
                            break;
                            
                        case "escrow":
                        case "escrow.yml":
                            if (plugin.getEscrowManager() != null) {
                                plugin.getEscrowManager().reload();
                                int deposits = plugin.getEscrowManager().getEscrowCount();
                                double total = plugin.getEscrowManager().getTotalEscrow();
                                sender.sendMessage("§a§l✓ §aescrow.yml rechargé!");
                                sender.sendMessage("§7  » " + deposits + " dépôts (" + total + "€)");
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
                            sender.sendMessage("§7Fichiers disponibles:");
                            sender.sendMessage("§e  • all §7(tous les fichiers)");
                            sender.sendMessage("§e  • config.yml");
                            sender.sendMessage("§e  • price.yml");
                            sender.sendMessage("§e  • messages.yml");
                            sender.sendMessage("§e  • messages_gui.yml");
                            sender.sendMessage("§e  • zones.yml");
                            sender.sendMessage("§e  • escrow.yml");
                            sender.sendMessage("§e  • orders_template.yml");
                            sender.sendMessage("§e  • statistics.yml");
                            sender.sendMessage("§e  • menus §7(dossier menus/)");
                            sender.sendMessage("§e  • towers §7(système de tours)");
                            break;
                    }
                }
                break;
                
            case "version":
            case "ver":
                sender.sendMessage("§8────────────────────────────");
                sender.sendMessage("§6§lQUANTUM §f- Advanced Storage");
                sender.sendMessage("§7Version: §e2.0.0");
                sender.sendMessage("§7Auteur: §eKazotaruu_");
                sender.sendMessage("§7Menus: §e" + plugin.getMenuManager().getMenuCount());
                if (plugin.getTowerManager() != null) {
                    sender.sendMessage("§7Tours: §e" + plugin.getTowerManager().getTowerCount());
                }
                if (plugin.getZoneManager() != null) {
                    sender.sendMessage("§7Zones: §e" + plugin.getZoneManager().getZoneCount());
                }
                sender.sendMessage("§8────────────────────────────");
                break;
                
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Recharge tous les fichiers de configuration
     */
    private void reloadAll(CommandSender sender) {
        sender.sendMessage("§6§l⟳ §6Rechargement de tous les fichiers...");
        sender.sendMessage("");
        
        // 1. Config principal
        plugin.reloadConfig();
        sender.sendMessage("§a§l✓ §aconfig.yml");
        
        // 2. Prices
        if (plugin.getPriceManager() != null) {
            plugin.getPriceManager().reload();
            sender.sendMessage("§a§l✓ §aprice.yml");
        }
        
        // 3. Messages
        if (plugin.getMessageManager() != null) {
            plugin.getMessageManager().reload();
            sender.sendMessage("§a§l✓ §amessages.yml");
        }
        if (plugin.getMessagesManager() != null) {
            plugin.getMessagesManager().reload();
        }
        
        // 4. GUI Messages
        if (plugin.getGuiMessageManager() != null) {
            plugin.getGuiMessageManager().reload();
            sender.sendMessage("§a§l✓ §amessages_gui.yml");
        }
        
        // 5. Menus
        if (plugin.getMenuManager() != null) {
            plugin.getMenuManager().reload();
            int count = plugin.getMenuManager().getMenuCount();
            sender.sendMessage("§a§l✓ §a" + count + " menus rechargés");
        }
        
        // 6. Zones
        if (plugin.getZoneManager() != null) {
            plugin.getZoneManager().reloadConfig();
            int zones = plugin.getZoneManager().getZoneCount();
            sender.sendMessage("§a§l✓ §azones.yml (" + zones + " zones)");
        }
        
        // 7. Tours
        if (plugin.getTowerManager() != null) {
            plugin.getTowerManager().reload();
            int towers = plugin.getTowerManager().getTowerCount();
            sender.sendMessage("§a§l✓ §aTours (" + towers + " tours)");
        }
        
        // 8. Escrow
        if (plugin.getEscrowManager() != null) {
            plugin.getEscrowManager().reload();
            int deposits = plugin.getEscrowManager().getEscrowCount();
            sender.sendMessage("§a§l✓ §aescrow.yml (" + deposits + " dépôts)");
        }
        
        // 9. Orders
        if (plugin.getOrderManager() != null) {
            plugin.getOrderManager().loadItems();
            sender.sendMessage("§a§l✓ §aorders_template.yml");
        }
        
        // 10. Statistics
        if (plugin.getStatisticsManager() != null) {
            plugin.getStatisticsManager().loadStatistics();
            sender.sendMessage("§a§l✓ §astatistics.yml");
        }
        
        // 11. Storage & Animation
        if (plugin.getStorageManager() != null) {
            plugin.getStorageManager().reload();
        }
        if (plugin.getAnimationManager() != null) {
            plugin.getAnimationManager().reload();
        }
        
        sender.sendMessage("");
        sender.sendMessage("§a§l✓ §aQuantum rechargé complètement!");
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8────────────────────────────────");
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("");
        sender.sendMessage("§e/quantum reload [fichier] §7- Recharger la config");
        sender.sendMessage("  §7Fichiers: §eall, config, price, messages,");
        sender.sendMessage("  §7zones, escrow, orders, stats, menus, towers");
        sender.sendMessage("§e/quantum stats [category] §7- Afficher les stats");
        sender.sendMessage("§e/quantum storagestats §7- Stats du storage");
        sender.sendMessage("§e/quantum version §7- Infos de version");
        sender.sendMessage("§e/quantum help §7- Afficher cette aide");
        sender.sendMessage("");
        sender.sendMessage("§e/storage §7- Ouvrir le stockage virtuel");
        sender.sendMessage("§e/menu <nom> §7- Ouvrir un menu");
        sender.sendMessage("§e/tower §7- Commandes des tours");
        
        if (sender.hasPermission("quantum.admin.orders")) {
            sender.sendMessage("");
            sender.sendMessage("§6§lCOMMANDES ADMIN ORDRES");
            sender.sendMessage("§e/quantum orders button createcategorie <nom>");
            sender.sendMessage("§e/quantum orders button deletecategorie <nom>");
        }
        
        sender.sendMessage("§8────────────────────────────────");
    }
}
