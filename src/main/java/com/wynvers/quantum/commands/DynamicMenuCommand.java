package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DynamicMenuCommand implements CommandExecutor {

    private final Quantum plugin;
    private final Menu menu;

    public DynamicMenuCommand(Quantum plugin, Menu menu) {
        this.plugin = plugin;
        this.menu = menu;
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

        menu.open(player, plugin);
        return true;
    }
}
