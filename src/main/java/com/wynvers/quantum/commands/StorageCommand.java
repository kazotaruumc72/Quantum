package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.storage.StorageMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StorageCommand implements CommandExecutor {

    private final Quantum plugin;

    public StorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("quantum.storage")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }
        
        // Sous-commande togglemode
        if (args.length > 0 && args[0].equalsIgnoreCase("togglemode")) {
            StorageMode.toggleMode(player);
            return true;
        }

        // Open storage menu from storage.yml
        Menu storageMenu = plugin.getMenuManager().getMenu("storage");
        if (storageMenu != null) {
            storageMenu.open(player, plugin);
        } else {
            player.sendMessage("§cErreur: Menu storage non trouvé!");
        }

        return true;
    }
}
