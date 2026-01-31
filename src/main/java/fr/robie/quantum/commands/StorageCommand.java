package fr.robie.quantum.commands;

import fr.robie.quantum.Quantum;
import fr.robie.quantum.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageCommand implements CommandExecutor {
    
    private final Quantum plugin;
    
    public StorageCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("quantum.storage")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }
        
        // Get player storage
        PlayerStorage storage = plugin.getStorageManager().getStorage(player);
        
        // Open storage GUI
        openStorageGUI(player, storage);
        
        player.sendMessage("§a§l✓ §aOpening virtual storage...");
        plugin.getQuantumLogger().debug("Opening storage for: " + player.getName());
        
        return true;
    }
    
    private void openStorageGUI(Player player, PlayerStorage storage) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lVirtual Storage");
        
        int slot = 0;
        
        // Add vanilla items
        for (Map.Entry<Material, Integer> entry : storage.getVanillaItems().entrySet()) {
            if (slot >= 54) break;
            
            Material material = entry.getKey();
            int amount = entry.getValue();
            
            ItemStack item = new ItemStack(material, Math.min(amount, 64));
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Amount: §e" + formatAmount(amount));
                lore.add("");
                lore.add("§e▶ Left-Click to withdraw 1");
                lore.add("§e▶ Right-Click to withdraw 64");
                lore.add("§e▶ Shift+Click to withdraw all");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            inv.setItem(slot++, item);
        }
        
        player.openInventory(inv);
    }
    
    private String formatAmount(int amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        } else {
            return String.valueOf(amount);
        }
    }
}
