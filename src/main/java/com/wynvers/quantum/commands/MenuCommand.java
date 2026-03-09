package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {

    private final Quantum plugin;

    public MenuCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player!");
            return true;
        }

        if (!player.hasPermission("quantum.menu.open")) {
            plugin.getMessageManager().sendMessage(player, "system.no-permission");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /menu <menu_name>");
            player.sendMessage("§7Available menus: " + String.join(", ", plugin.getMenuManager().getMenuNames()));
            return true;
        }

        String menuName = args[0];
        Menu menu = plugin.getMenuManager().getMenu(menuName);

        if (menu == null) {
            player.sendMessage("§cMenu not found: " + menuName);
            player.sendMessage("§7Available menus: " + String.join(", ", plugin.getMenuManager().getMenuNames()));
            return true;
        }

        menu.open(player, plugin);
        return true;
    }
}
