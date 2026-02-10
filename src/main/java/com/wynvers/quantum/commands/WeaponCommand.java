package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.weapon.DungeonWeapon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Commande pour gérer l'arme de donjon
 */
public class WeaponCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final DungeonWeapon dungeonWeapon;
    
    public WeaponCommand(Quantum plugin) {
        this.plugin = plugin;
        this.dungeonWeapon = plugin.getDungeonWeapon();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "upgrade":
                return handleUpgrade(player);
            case "info":
                return handleInfo(player);
            case "give":
                return handleGive(player);
            default:
                sendHelp(player);
                return true;
        }
    }
    
    private boolean handleUpgrade(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cVous devez tenir une arme dans votre main!");
            return true;
        }
        
        if (!dungeonWeapon.isDungeonWeapon(item)) {
            player.sendMessage("§cCet item n'est pas une arme de donjon!");
            return true;
        }
        
        dungeonWeapon.upgrade(item, player);
        return true;
    }
    
    private boolean handleInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cVous devez tenir une arme dans votre main!");
            return true;
        }
        
        if (!dungeonWeapon.isDungeonWeapon(item)) {
            player.sendMessage("§cCet item n'est pas une arme de donjon!");
            return true;
        }
        
        int level = dungeonWeapon.getLevel(item);
        boolean inDungeon = dungeonWeapon.isInDungeon(player);
        
        player.sendMessage("§e=== Arme de Donjon ===");
        player.sendMessage("§7Niveau: §a" + level);
        player.sendMessage("§7Dans un donjon: " + (inDungeon ? "§aOui" : "§cNon"));
        player.sendMessage("§7Coût d'amélioration: §e" + dungeonWeapon.getUpgradeCost(level) + "$");
        
        return true;
    }
    
    private boolean handleGive(Player player) {
        if (!player.hasPermission("quantum.weapon.give")) {
            player.sendMessage("§cVous n'avez pas la permission!");
            return true;
        }
        
        ItemStack weapon = dungeonWeapon.createWeapon();
        
        if (weapon != null) {
            player.getInventory().addItem(weapon);
            player.sendMessage("§aVous avez reçu une arme de donjon!");
        } else {
            player.sendMessage("§cErreur lors de la création de l'arme!");
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§e=== Commandes Arme de Donjon ===");
        player.sendMessage("§7/weapon upgrade §f- Améliorer l'arme en main");
        player.sendMessage("§7/weapon info §f- Afficher les infos de l'arme");
        player.sendMessage("§7/weapon give §f- Obtenir une arme de donjon");
    }
}
