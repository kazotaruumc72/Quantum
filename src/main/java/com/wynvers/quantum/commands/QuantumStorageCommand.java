package com.wynvers.quantum.commands;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QuantumStorageCommand implements CommandExecutor {

    private final Quantum plugin;

    public QuantumStorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "transfer":
            case "add":
            case "deposit":
                handleTransfer(player, args);
                break;

            case "remove":
            case "withdraw":
            case "take":
                handleRemove(player, args);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleTransfer(Player player, String[] args) {
        if (!player.hasPermission("quantum.storage.transfer")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return;
        }

        // /qstorage transfer <item> [amount]
        // /qstorage transfer hand [amount]
        // /qstorage transfer all

        if (args.length < 2) {
            player.sendMessage("§cUsage: /qstorage transfer <item|hand|all> [amount]");
            return;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        String itemArg = args[1].toLowerCase();

        // Transfer all items from inventory
        if (itemArg.equals("all")) {
            int totalTransferred = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    int amount = item.getAmount();
                    
                    // Check if Nexo item
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
            player.sendMessage("§a§l✓ §aTransferred §e" + totalTransferred + " §aitems to storage!");
            return;
        }

        // Transfer item in hand
        if (itemArg.equals("hand")) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage("§cYou must hold an item in your hand!");
                return;
            }

            int amount = handItem.getAmount();
            if (args.length >= 3) {
                try {
                    amount = Math.min(Integer.parseInt(args[2]), handItem.getAmount());
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid amount!");
                    return;
                }
            }

            // Check if Nexo item
            String nexoId = NexoItems.idFromItem(handItem);
            if (nexoId != null) {
                storage.addNexoItem(nexoId, amount);
                player.sendMessage("§a§l✓ §aTransferred §e" + amount + "x §f" + nexoId + " §ato storage!");
            } else {
                storage.addItem(handItem.getType(), amount);
                player.sendMessage("§a§l✓ §aTransferred §e" + amount + "x §f" + handItem.getType().name() + " §ato storage!");
            }

            handItem.setAmount(handItem.getAmount() - amount);
            storage.save(plugin);
            return;
        }

        // Transfer specific item by name/ID
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount!");
                return;
            }
        }

        // Try Nexo item first
        if (NexoItems.exists(itemArg)) {
            int transferred = removeFromInventory(player, itemArg, amount, true);
            if (transferred > 0) {
                storage.addNexoItem(itemArg, transferred);
                storage.save(plugin);
                player.sendMessage("§a§l✓ §aTransferred §e" + transferred + "x §f" + itemArg + " §ato storage!");
            } else {
                player.sendMessage("§cYou don't have this item in your inventory!");
            }
            return;
        }

        // Try vanilla item
        try {
            Material material = Material.valueOf(itemArg.toUpperCase());
            int transferred = removeFromInventory(player, material.name(), amount, false);
            if (transferred > 0) {
                storage.addItem(material, transferred);
                storage.save(plugin);
                player.sendMessage("§a§l✓ §aTransferred §e" + transferred + "x §f" + material.name() + " §ato storage!");
            } else {
                player.sendMessage("§cYou don't have this item in your inventory!");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cItem not found: §7" + itemArg);
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (!player.hasPermission("quantum.storage.remove")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return;
        }

        // /qstorage remove <item> [amount]

        if (args.length < 2) {
            player.sendMessage("§cUsage: /qstorage remove <item> [amount]");
            return;
        }

        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        String itemArg = args[1].toLowerCase();

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount!");
                return;
            }
        }

        // Try Nexo item first
        if (storage.getNexoItems().containsKey(itemArg)) {
            int available = storage.getNexoAmount(itemArg);
            int toRemove = Math.min(amount, available);

            if (toRemove <= 0) {
                player.sendMessage("§cYou don't have this item in storage!");
                return;
            }

            // Check inventory space
            if (!hasSpace(player, toRemove)) {
                player.sendMessage("§cYour inventory is full!");
                return;
            }

            // Remove from storage
            storage.removeNexoItem(itemArg, toRemove);

            // Give Nexo items to player
            giveNexoItems(player, itemArg, toRemove);

            storage.save(plugin);
            player.sendMessage("§a§l✓ §aWithdrawn §e" + toRemove + "x §f" + itemArg + " §afrom storage!");
            return;
        }

        // Try vanilla item
        try {
            Material material = Material.valueOf(itemArg.toUpperCase());
            int available = storage.getAmount(material);
            int toRemove = Math.min(amount, available);

            if (toRemove <= 0) {
                player.sendMessage("§cYou don't have this item in storage!");
                return;
            }

            // Check inventory space
            if (!hasSpace(player, toRemove)) {
                player.sendMessage("§cYour inventory is full!");
                return;
            }

            // Remove from storage
            storage.removeItem(material, toRemove);

            // Give vanilla items to player
            giveVanillaItems(player, material, toRemove);

            storage.save(plugin);
            player.sendMessage("§a§l✓ §aWithdrawn §e" + toRemove + "x §f" + material.name() + " §afrom storage!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cItem not found: §7" + itemArg);
        }
    }

    private int removeFromInventory(Player player, String identifier, int maxAmount, boolean isNexo) {
        int removed = 0;
        int remaining = maxAmount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (remaining <= 0) break;

            boolean matches = false;
            if (isNexo) {
                String nexoId = NexoItems.idFromItem(item);
                matches = nexoId != null && nexoId.equals(identifier);
            } else {
                matches = item.getType().name().equalsIgnoreCase(identifier);
            }

            if (matches) {
                int toRemove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - toRemove);
                removed += toRemove;
                remaining -= toRemove;
            }
        }

        return removed;
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
        player.sendMessage("§6§l■ §eQuantum Storage Commands");
        player.sendMessage("");
        player.sendMessage("§e/qstorage transfer <item|hand|all> [amount] §7- Transfer items to storage");
        player.sendMessage("§e/qstorage remove <item> [amount] §7- Remove items from storage");
        player.sendMessage("");
        player.sendMessage("§7Examples:");
        player.sendMessage("§f/qstorage transfer hand §7- Transfer item in hand");
        player.sendMessage("§f/qstorage transfer all §7- Transfer all items");
        player.sendMessage("§f/qstorage transfer diamond 64 §7- Transfer 64 diamonds");
        player.sendMessage("§f/qstorage remove diamond 32 §7- Remove 32 diamonds");
    }
}
