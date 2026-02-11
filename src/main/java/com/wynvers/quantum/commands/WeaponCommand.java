package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.weapon.DungeonWeapon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
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
            plugin.getMessageManager().sendMessage(player, "weapons.no-weapon-in-hand");
            return true;
        }
        
        if (!dungeonWeapon.isDungeonWeapon(item)) {
            plugin.getMessageManager().sendMessage(player, "weapons.not-dungeon-weapon");
            return true;
        }
        
        dungeonWeapon.upgrade(item, player);
        return true;
    }
    
    private boolean handleInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(player, "weapons.no-weapon-in-hand");
            return true;
        }
        
        if (!dungeonWeapon.isDungeonWeapon(item)) {
            plugin.getMessageManager().sendMessage(player, "weapons.not-dungeon-weapon");
            return true;
        }
        
        int level = dungeonWeapon.getLevel(item);
        boolean inDungeon = dungeonWeapon.isInDungeon(player);
        
        plugin.getMessageManager().sendMessage(player, "weapons.info.header");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("level", String.valueOf(level));
        plugin.getMessageManager().sendMessage(player, "weapons.info.level", placeholders);
        
        placeholders.clear();
        placeholders.put("in_dungeon", inDungeon ? "<green>Oui</green>" : "<red>Non</red>");
        plugin.getMessageManager().sendMessage(player, "weapons.info.in-dungeon", placeholders);
        
        placeholders.clear();
        placeholders.put("cost", String.valueOf(dungeonWeapon.getUpgradeCost(level)));
        plugin.getMessageManager().sendMessage(player, "weapons.info.upgrade-cost", placeholders);
        
        return true;
    }
    
    private boolean handleGive(Player player) {
        if (!player.hasPermission("quantum.weapon.give")) {
            plugin.getMessageManager().sendMessage(player, "weapons.no-permission");
            return true;
        }
        
        ItemStack weapon = dungeonWeapon.createWeapon();
        
        if (weapon != null) {
            player.getInventory().addItem(weapon);
            plugin.getMessageManager().sendMessage(player, "weapons.received");
        } else {
            plugin.getMessageManager().sendMessage(player, "weapons.creation-error");
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        plugin.getMessageManager().sendMessage(player, "weapons.help.header");
        for (String command : plugin.getMessageManager().getMessageList("weapons.help.commands")) {
            player.sendMessage(plugin.getMessageManager().toComponent(command));
        }
    }
}
