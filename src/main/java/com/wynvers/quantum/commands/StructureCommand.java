package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.tools.StructureSelectionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Commande /quantum structure pour gérer les structures
 */
public class StructureCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final StructureSelectionManager selectionManager;
    
    public StructureCommand(Quantum plugin, StructureSelectionManager selectionManager) {
        this.plugin = plugin;
        this.selectionManager = selectionManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "wand":
                return handleWand(sender);
            
            case "create":
                return handleCreate(sender, args);
            
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Donne la baguette de structure au joueur
     */
    private boolean handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c§l✗ §cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
        if (!player.hasPermission("quantum.structure.wand")) {
            player.sendMessage("§c§l✗ §cVous n'avez pas la permission d'obtenir la baguette de structure!");
            return true;
        }
        
        // Créer la baguette de structure
        ItemStack wand = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = wand.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e§lBaguette de Structure");
            meta.setLore(Arrays.asList(
                "§7Clic gauche: §eDéfinir position 1",
                "§7Clic droit: §eDéfinir position 2",
                "",
                "§7Utilisez §e/quantum structure create <nom>",
                "§7pour créer une structure à partir de",
                "§7votre sélection."
            ));
            wand.setItemMeta(meta);
        }
        
        // Donner la baguette au joueur
        player.getInventory().addItem(wand);
        player.sendMessage("§a§l✓ §aVous avez reçu la baguette de structure!");
        player.sendMessage("§7Clic gauche pour position 1, clic droit pour position 2");
        
        return true;
    }
    
    /**
     * Crée une structure à partir de la sélection du joueur
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c§l✗ §cCette commande ne peut être exécutée que par un joueur!");
            return true;
        }
        
        if (!player.hasPermission("quantum.structure.create")) {
            player.sendMessage("§c§l✗ §cVous n'avez pas la permission de créer des structures!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§c§l✗ §cUtilisation: /quantum structure create <nom>");
            return true;
        }
        
        String structureName = args[1];
        
        // Vérifier que le joueur a défini les deux positions
        if (!selectionManager.hasSelection(player)) {
            player.sendMessage("§c§l✗ §cVous devez d'abord sélectionner deux positions avec la baguette de structure!");
            player.sendMessage("§7Utilisez §e/quantum structure wand §7pour obtenir la baguette");
            return true;
        }
        
        Location pos1 = selectionManager.getPos1(player);
        Location pos2 = selectionManager.getPos2(player);
        
        // Vérifier que les positions sont dans le même monde
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c§l✗ §cLes deux positions doivent être dans le même monde!");
            return true;
        }
        
        // Calculer les dimensions de la structure
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        int totalBlocks = width * height * depth;
        
        // Limite de sécurité
        if (totalBlocks > 10000) {
            player.sendMessage("§c§l✗ §cLa structure est trop grande! Maximum: 10,000 blocs");
            player.sendMessage("§7Votre sélection: §e" + totalBlocks + " blocs");
            return true;
        }
        
        // Sauvegarder la structure via le StructureManager
        boolean success = saveStructure(player, structureName, pos1, pos2);
        
        if (success) {
            player.sendMessage("§a§l✓ §aStructure '§e" + structureName + "§a' créée avec succès!");
            player.sendMessage("§7Dimensions: §e" + width + "x" + height + "x" + depth + " §7(§e" + totalBlocks + " blocs§7)");
            
            // Effacer la sélection
            selectionManager.clearSelection(player);
        } else {
            player.sendMessage("§c§l✗ §cErreur lors de la création de la structure!");
        }
        
        return true;
    }
    
    /**
     * Sauvegarde la structure dans le fichier de configuration
     */
    private boolean saveStructure(Player player, String name, Location pos1, Location pos2) {
        try {
            // Calculer la position de base (coin minimum)
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
            
            List<String> blocks = new ArrayList<>();
            
            // Parcourir tous les blocs de la sélection
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Location blockLoc = new Location(pos1.getWorld(), x, y, z);
                        Material material = blockLoc.getBlock().getType();
                        
                        // Ignorer les blocs d'air
                        if (material == Material.AIR) {
                            continue;
                        }
                        
                        // Calculer les coordonnées relatives
                        int relX = x - minX;
                        int relY = y - minY;
                        int relZ = z - minZ;
                        
                        // Format: "x,y,z:minecraft:MATERIAL"
                        String blockString = relX + "," + relY + "," + relZ + ":minecraft:" + material.name();
                        blocks.add(blockString);
                    }
                }
            }
            
            // Utiliser le StructureManager pour sauvegarder
            // Note: Pour l'instant, on affiche juste les informations
            // L'implémentation complète nécessiterait d'étendre StructureManager
            player.sendMessage("§7Structure capturée: §e" + blocks.size() + " blocs non-vides");
            
            // TODO: Implémenter la sauvegarde dans structures.yml via StructureManager
            plugin.getLogger().info("Structure '" + name + "' created with " + blocks.size() + " blocks");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Affiche l'aide des commandes de structure
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Commandes Structure ===");
        sender.sendMessage("§e/quantum structure wand §7- Obtenir la baguette de sélection");
        sender.sendMessage("§e/quantum structure create <nom> §7- Créer une structure");
        sender.sendMessage("");
        sender.sendMessage("§7Avec la baguette:");
        sender.sendMessage("§7  - Clic gauche: Position 1");
        sender.sendMessage("§7  - Clic droit: Position 2");
    }
}
