package com.wynvers.quantum.commands;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

public class QuantumStorageCommand implements CommandExecutor {

    private final Quantum plugin;

    public QuantumStorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sendHelp((Player) sender);
            } else {
                sender.sendMessage("§cUsage: /qstorage <transfer|remove> <item> [amount] [player]");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "transfer":
            case "add":
            case "deposit":
                return handleTransfer(sender, args);

            case "remove":
            case "withdraw":
            case "take":
                return handleRemove(sender, args);

            default:
                if (sender instanceof Player) {
                    sendHelp((Player) sender);
                } else {
                    sender.sendMessage("§cInvalid subcommand!");
                }
                break;
        }

        return true;
    }

    private boolean handleTransfer(CommandSender sender, String[] args) {
        // Console command: /qstorage transfer <nexo:id|minecraft:id> <amount> <player>
        // Admin command: /qstorage transfer <nexo:id|minecraft:id> [amount] - DIRECT add to storage
        // Player command: /qstorage transfer hand|all [amount]
        
        if (sender instanceof ConsoleCommandSender) {
            return handleConsoleTransfer(sender, args);
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("quantum.storage.transfer")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /qstorage transfer <nexo:id|minecraft:id|hand|all> [amount]");
            return true;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        String itemArg = args[1].toLowerCase();

        // Transfer all items from inventory
        if (itemArg.equals("all")) {
            int totalTransferred = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    int amount = item.getAmount();
                    
                    String nexoId = NexoItems.idFromItem(item);
                    if (nexoId != null) {
                        storage.addNexoItem(nexoId, amount);
                    } else {
                        storage.addItem(item.getType(), amount);
                    }
                    
                    totalTransferred += amount;
                    item.setAmount(0);
                }
            }
            storage.save(plugin);
            refreshStorageGUI(player);
            player.sendMessage("§a§l✓ §aTransferred §e" + totalTransferred + " §aitems to storage!");
            return true;
        }

        // Transfer item in hand
        if (itemArg.equals("hand")) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage("§cYou must hold an item in your hand!");
                return true;
            }

            int amount = handItem.getAmount();
            if (args.length >= 3) {
                try {
                    amount = Math.min(Integer.parseInt(args[2]), handItem.getAmount());
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid amount!");
                    return true;
                }
            }

            String nexoId = NexoItems.idFromItem(handItem);
            if (nexoId != null) {
                storage.addNexoItem(nexoId, amount);
                player.sendMessage("§a§l✓ §aTransferred §e" + amount + "x §fnexo:" + nexoId + " §ato storage!");
            } else {
                storage.addItem(handItem.getType(), amount);
                player.sendMessage("§a§l✓ §aTransferred §e" + amount + "x §fminecraft:" + handItem.getType().name() + " §ato storage!");
            }

            handItem.setAmount(handItem.getAmount() - amount);
            storage.save(plugin);
            refreshStorageGUI(player);
            return true;
        }

        // Admin direct transfer: nexo:id or minecraft:id - ADD DIRECTLY TO STORAGE
        boolean result = transferSpecificItemDirect(player, storage, player, itemArg, args.length >= 3 ? args[2] : "1");
        refreshStorageGUI(player);
        return result;
    }

    private boolean handleConsoleTransfer(CommandSender sender, String[] args) {
        // /qstorage transfer <nexo:id|minecraft:id> <amount> <player>
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /qstorage transfer <nexo:id|minecraft:id> <amount> <player>");
            return true;
        }

        String itemArg = args[1];
        String amountStr = args[2];
        String playerName = args[3];

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(target);
        boolean result = transferSpecificItemDirect(sender, storage, target, itemArg, amountStr);
        refreshStorageGUI(target);
        return result;
    }

    /**
     * Admin/Console DIRECT transfer - adds to storage WITHOUT checking inventory
     */
    private boolean transferSpecificItemDirect(CommandSender sender, PlayerStorage storage, Player target, String itemArg, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }

        // Check for nexo: or minecraft: prefix
        if (itemArg.startsWith("nexo:")) {
            String nexoId = itemArg.substring(5);
            if (!NexoItems.exists(nexoId)) {
                sender.sendMessage("§cNexo item not found: §7" + nexoId);
                return true;
            }

            // DIRECT ADD - no inventory check
            storage.addNexoItem(nexoId, amount);
            storage.save(plugin);
            sender.sendMessage("§a§l✓ §aAdded §e" + amount + "x §fnexo:" + nexoId + " §ato " + target.getName() + "'s storage!");
            return true;
        }

        if (itemArg.startsWith("minecraft:")) {
            String materialName = itemArg.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                
                // DIRECT ADD - no inventory check
                storage.addItem(material, amount);
                storage.save(plugin);
                sender.sendMessage("§a§l✓ §aAdded §e" + amount + "x §fminecraft:" + material.name() + " §ato " + target.getName() + "'s storage!");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cMinecraft item not found: §7" + materialName);
            }
            return true;
        }

        // No prefix - try Nexo first, then vanilla
        if (NexoItems.exists(itemArg)) {
            // DIRECT ADD
            storage.addNexoItem(itemArg, amount);
            storage.save(plugin);
            sender.sendMessage("§a§l✓ §aAdded §e" + amount + "x §fnexo:" + itemArg + " §ato " + target.getName() + "'s storage!");
            return true;
        }

        try {
            Material material = Material.valueOf(itemArg.toUpperCase());
            // DIRECT ADD
            storage.addItem(material, amount);
            storage.save(plugin);
            sender.sendMessage("§a§l✓ §aAdded §e" + amount + "x §fminecraft:" + material.name() + " §ato " + target.getName() + "'s storage!");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cItem not found: §7" + itemArg);
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        // Console: /qstorage remove <nexo:id|minecraft:id> <amount> <player>
        // Player: /qstorage remove <nexo:id|minecraft:id> [amount]
        
        if (sender instanceof ConsoleCommandSender) {
            return handleConsoleRemove(sender, args);
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("quantum.storage.remove")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /qstorage remove <nexo:id|minecraft:id> [amount]");
            return true;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        String itemArg = args[1];
        String amountStr = args.length >= 3 ? args[2] : "1";

        boolean result = removeSpecificItem(player, storage, player, itemArg, amountStr);
        refreshStorageGUI(player);
        return result;
    }

    private boolean handleConsoleRemove(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /qstorage remove <nexo:id|minecraft:id> <amount> <player>");
            return true;
        }

        String itemArg = args[1];
        String amountStr = args[2];
        String playerName = args[3];

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return true;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(target);
        boolean result = removeSpecificItem(sender, storage, target, itemArg, amountStr);
        refreshStorageGUI(target);
        return result;
    }

    private boolean removeSpecificItem(CommandSender sender, PlayerStorage storage, Player target, String itemArg, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }

        // Check for nexo: or minecraft: prefix
        if (itemArg.startsWith("nexo:")) {
            String nexoId = itemArg.substring(5);
            return withdrawNexoItem(sender, storage, target, nexoId, amount);
        }

        if (itemArg.startsWith("minecraft:")) {
            String materialName = itemArg.substring(10).toUpperCase();
            try {
                Material material = Material.valueOf(materialName);
                return withdrawVanillaItem(sender, storage, target, material, amount);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cMinecraft item not found: §7" + materialName);
                return true;
            }
        }

        // No prefix - try storage to determine type
        if (storage.getNexoItems().containsKey(itemArg)) {
            return withdrawNexoItem(sender, storage, target, itemArg, amount);
        }

        try {
            Material material = Material.valueOf(itemArg.toUpperCase());
            return withdrawVanillaItem(sender, storage, target, material, amount);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cItem not found in storage: §7" + itemArg);
            return true;
        }
    }

    private boolean withdrawNexoItem(CommandSender sender, PlayerStorage storage, Player target, String nexoId, int amount) {
        int available = storage.getNexoAmount(nexoId);
        int toRemove = Math.min(amount, available);

        if (toRemove <= 0) {
            sender.sendMessage("§c" + target.getName() + " doesn't have this item in storage!");
            return true;
        }

        if (!hasSpace(target, toRemove)) {
            sender.sendMessage("§c" + target.getName() + "'s inventory is full!");
            return true;
        }

        storage.removeNexoItem(nexoId, toRemove);
        giveNexoItems(target, nexoId, toRemove);
        storage.save(plugin);
        sender.sendMessage("§a§l✓ §aWithdrawn §e" + toRemove + "x §fnexo:" + nexoId + " §afrom " + target.getName() + "'s storage!");
        return true;
    }

    private boolean withdrawVanillaItem(CommandSender sender, PlayerStorage storage, Player target, Material material, int amount) {
        int available = storage.getAmount(material);
        int toRemove = Math.min(amount, available);

        if (toRemove <= 0) {
            sender.sendMessage("§c" + target.getName() + " doesn't have this item in storage!");
            return true;
        }

        if (!hasSpace(target, toRemove)) {
            sender.sendMessage("§c" + target.getName() + "'s inventory is full!");
            return true;
        }

        storage.removeItem(material, toRemove);
        giveVanillaItems(target, material, toRemove);
        storage.save(plugin);
        sender.sendMessage("§a§l✓ §aWithdrawn §e" + toRemove + "x §fminecraft:" + material.name() + " §afrom " + target.getName() + "'s storage!");
        return true;
    }

    /**
     * Refresh storage GUI if player has it open
     */
    private void refreshStorageGUI(Player player) {
        if (player == null || !player.isOnline()) return;
        
        Inventory openInv = player.getOpenInventory().getTopInventory();
        if (openInv == null) return;
        
        String title = player.getOpenInventory().getTitle();
        if (title == null) return;
        
        // Check if it's the storage menu
        Menu storageMenu = plugin.getMenuManager().getMenuByTitle(title);
        if (storageMenu != null && storageMenu.getId().equals("storage")) {
            // Re-open the menu to refresh it - pass plugin instance
            Bukkit.getScheduler().runTask(plugin, () -> storageMenu.open(player, plugin));
        }
    }

    private void giveNexoItems(Player player, String nexoId, int amount) {
        int remaining = amount;
        ItemStack nexoItem = NexoItems.itemFromId(nexoId).build();
        int maxStackSize = nexoItem.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = NexoItems.itemFromId(nexoId).build();
            stack.setAmount(stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }

    private void giveVanillaItems(Player player, Material material, int amount) {
        int remaining = amount;
        int maxStackSize = material.getMaxStackSize();

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(material, stackSize);

            player.getInventory().addItem(stack);
            remaining -= stackSize;
        }
    }

    private boolean hasSpace(Player player, int amount) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots * 64 >= amount;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l■ §eQuantum Storage Commands §7(Admin)");
        player.sendMessage("");
        player.sendMessage("§e/qstorage transfer hand [amount] §7- Transfer item in hand");
        player.sendMessage("§e/qstorage transfer all §7- Transfer all inventory");
        player.sendMessage("§e/qstorage transfer <item> <amount> §7- Add directly to storage");
        player.sendMessage("§e/qstorage remove <item> [amount] §7- Remove from storage");
        player.sendMessage("");
        player.sendMessage("§7Item Syntax:");
        player.sendMessage("§fnexo:<id> §7- Nexo custom item");
        player.sendMessage("§fminecraft:<id> §7- Minecraft vanilla item");
        player.sendMessage("");
        player.sendMessage("§7Examples:");
        player.sendMessage("§f/qstorage transfer nexo:afzelia_bark 1 §7- Add 1 bark to storage");
        player.sendMessage("§f/qstorage transfer minecraft:diamond 64 §7- Add 64 diamonds");
        player.sendMessage("§f/qstorage remove nexo:afzelia_bark 5 §7- Remove 5 from storage");
    }
}
