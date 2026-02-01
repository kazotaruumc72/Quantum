package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class QuantumCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final OrdersAdminCommand ordersAdminCommand;
    
    public QuantumCommand(Quantum plugin) {
        this.plugin = plugin;
        this.ordersAdminCommand = new OrdersAdminCommand(plugin);
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
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("quantum.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission!");
                    return true;
                }
                
                // Si aucun argument spécifique, reload tout
                if (args.length == 1) {
                    plugin.reloadPlugin();
                    sender.sendMessage("§a§l✓ §aQuantum rechargé complètement!");
                } else {
                    String reloadType = args[1].toLowerCase();
                    
                    switch (reloadType) {
                        case "all":
                            plugin.reloadPlugin();
                            sender.sendMessage("§a§l✓ §aQuantum rechargé complètement!");
                            break;
                            
                        case "price":
                        case "price.yml":
                        case "prices":
                            if (plugin.getPriceManager() != null) {
                                plugin.getPriceManager().reload();
                                sender.sendMessage("§a§l✓ §aFichier price.yml rechargé!");
                            } else {
                                sender.sendMessage("§c⚠ PriceManager non initialisé!");
                            }
                            break;
                            
                        case "config":
                        case "config.yml":
                            plugin.reloadConfig();
                            sender.sendMessage("§a§l✓ §aFichier config.yml rechargé!");
                            break;
                            
                        case "menus":
                        case "menu":
                            if (plugin.getMenuManager() != null) {
                                plugin.getMenuManager().reload();
                                sender.sendMessage("§a§l✓ §aMenus rechargés!");
                            }
                            break;
                            
                        case "messages":
                        case "messages.yml":
                            if (plugin.getMessagesManager() != null) {
                                plugin.getMessagesManager().reload();
                                sender.sendMessage("§a§l✓ §aMessages rechargés!");
                            }
                            break;
                            
                        default:
                            sender.sendMessage("§c⚠ Type de reload invalide!");
                            sender.sendMessage("§eUtilisation: /quantum reload <all|price|config|menus|messages>");
                            break;
                    }
                }
                break;
                
            case "version":
            case "ver":
                sender.sendMessage("§8────────────────────────────");
                sender.sendMessage("§6§lQUANTUM §f- Advanced Storage");
                sender.sendMessage("§7Version: §e1.0.0");
                sender.sendMessage("§7Auteur: §eKazotaruu_");
                sender.sendMessage("§7Menus: §e" + plugin.getMenuManager().getMenuCount());
                sender.sendMessage("§8────────────────────────────");
                break;
                
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8────────────────────────────────");
        sender.sendMessage("§6§lCOMMANDES QUANTUM");
        sender.sendMessage("");
        sender.sendMessage("§e/quantum reload [type] §7- Recharger la configuration");
        sender.sendMessage("  §7Types: §eall, price, config, menus, messages");
        sender.sendMessage("§e/quantum version §7- Afficher les infos de version");
        sender.sendMessage("§e/quantum help §7- Afficher cette aide");
        sender.sendMessage("§e/storage §7- Ouvrir le stockage virtuel");
        sender.sendMessage("§e/menu <nom> §7- Ouvrir un menu personnalisé");
        
        if (sender.hasPermission("quantum.admin.orders")) {
            sender.sendMessage("");
            sender.sendMessage("§6§lCOMMANDES ADMIN ORDRES");
            sender.sendMessage("§e/quantum orders button createcategorie <nom>");
            sender.sendMessage("  §7- Créer une nouvelle catégorie d'ordres");
            sender.sendMessage("§e/quantum orders button deletecategorie <nom>");
            sender.sendMessage("  §7- Supprimer une catégorie d'ordres");
        }
        
        sender.sendMessage("§8────────────────────────────────");
    }
}
