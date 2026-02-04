package com.wynvers.quantum.armor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmorCommand implements CommandExecutor, TabCompleter {
    
    private final DungeonArmor armorManager;
    private final ArmorGUI armorGUI;
    
    public ArmorCommand(DungeonArmor armorManager, ArmorGUI armorGUI) {
        this.armorManager = armorManager;
        this.armorGUI = armorGUI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "gui":
                armorGUI.openMainGUI(player);
                break;
                
            case "give":
                if (!player.hasPermission("quantum.armor.give")) {
                    player.sendMessage("§c✖ Vous n'avez pas la permission.");
                    return true;
                }
                
                if (args.length < 2) {
                    player.sendMessage("§c✖ Usage: /armure give <helmet|chestplate|leggings|boots>");
                    return true;
                }
                
                ItemStack armor = armorManager.createArmorPiece(args[1].toLowerCase());
                if (armor != null) {
                    player.getInventory().addItem(armor);
                    player.sendMessage("§a✔ Pièce d'armure reçue !");
                } else {
                    player.sendMessage("§c✖ Pièce d'armure invalide !");
                }
                break;
                
            case "reload":
                if (!player.hasPermission("quantum.armor.reload")) {
                    player.sendMessage("§c✖ Vous n'avez pas la permission.");
                    return true;
                }
                
                armorManager.reload();
                RuneType.init(armorManager.plugin);
                player.sendMessage("§a✔ Configuration rechargée !");
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l⚔ ARMURE DE DONJON ⚔");
        player.sendMessage("");
        player.sendMessage("§e/armure gui §7- Ouvrir le GUI de gestion");
        player.sendMessage("§e/armure give <type> §7- Recevoir une pièce d'armure");
        player.sendMessage("§e/armure reload §7- Recharger la configuration");
        player.sendMessage("");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("gui", "give", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList("helmet", "chestplate", "leggings", "boots"));
        }
        
        return completions;
    }
}
