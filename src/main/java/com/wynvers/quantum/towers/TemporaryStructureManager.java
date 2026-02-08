package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Gère la restoration temporaire des structures (schematics) placés par les skills
 */
public class TemporaryStructureManager {
    
    private final Quantum plugin;
    private final Map<UUID, List<PlacedBlock>> activeStructures = new HashMap<>();
    private final Map<UUID, BukkitTask> restorationTasks = new HashMap<>();
    
    public TemporaryStructureManager(Quantum plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Place une structure temporaire et planifie sa restoration
     * 
     * @param center Centre de la structure
     * @param radius Rayon de la structure
     * @param height Hauteur de la structure
     * @param material Matériau à placer
     * @param durationSeconds Durée avant restoration
     * @return UUID de la structure pour tracking
     */
    public UUID placeTemporaryStructure(Location center, int radius, int height, 
                                         Material material, int durationSeconds) {
        UUID structureId = UUID.randomUUID();
        List<PlacedBlock> placedBlocks = new ArrayList<>();
        
        // Sauvegarder les blocs existants et placer les nouveaux
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Vérifier si dans le cercle
                if (x * x + z * z > radius * radius) continue;
                
                for (int y = 0; y < height; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    
                    // Sauvegarder l'état original
                    PlacedBlock placed = new PlacedBlock(blockLoc, block.getState());
                    
                    // Ne remplacer que l'air, l'eau et la lave (pas les blocs solides)
                    Material originalType = block.getType();
                    if (originalType.isAir() || originalType == Material.WATER || 
                        originalType == Material.LAVA || originalType == Material.FIRE) {
                        
                        placedBlocks.add(placed);
                        
                        // Placer le nouveau bloc
                        block.setType(material);
                    }
                }
            }
        }
        
        if (placedBlocks.isEmpty()) {
            return null;
        }
        
        activeStructures.put(structureId, placedBlocks);
        
        // Planifier la restoration
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                restoreStructure(structureId);
            }
        }.runTaskLater(plugin, durationSeconds * 20L);
        
        restorationTasks.put(structureId, task);
        
        return structureId;
    }
    
    /**
     * Place un pilier vertical temporaire (version simplifiée d'iceberg)
     * 
     * @param base Position de base (au sol)
     * @param height Hauteur du pilier
     * @param material Matériau
     * @param durationSeconds Durée avant restoration
     * @return UUID de la structure
     */
    public UUID placeTemporaryPillar(Location base, int height, Material material, int durationSeconds) {
        UUID structureId = UUID.randomUUID();
        List<PlacedBlock> placedBlocks = new ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            Location blockLoc = base.clone().add(0, y, 0);
            Block block = blockLoc.getBlock();
            
            // Sauvegarder l'état original
            PlacedBlock placed = new PlacedBlock(blockLoc, block.getState());
            
            // Ne remplacer que l'air, l'eau et la lave
            Material originalType = block.getType();
            if (originalType.isAir() || originalType == Material.WATER || 
                originalType == Material.LAVA || originalType == Material.FIRE) {
                
                placedBlocks.add(placed);
                block.setType(material);
            }
        }
        
        if (placedBlocks.isEmpty()) {
            return null;
        }
        
        activeStructures.put(structureId, placedBlocks);
        
        // Applique aussi l'effet slow dans la zone
        for (Player player : base.getWorld().getNearbyEntities(base, 5, 5, 5).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .filter(p -> p.getGameMode() != org.bukkit.GameMode.CREATIVE && 
                             p.getGameMode() != org.bukkit.GameMode.SPECTATOR)
                .toList()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOWNESS, 
                durationSeconds * 20, 
                1
            ));
        }
        
        // Planifier la restoration
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                restoreStructure(structureId);
            }
        }.runTaskLater(plugin, durationSeconds * 20L);
        
        restorationTasks.put(structureId, task);
        
        return structureId;
    }
    
    /**
     * Restaure une structure à son état original
     * @param structureId UUID de la structure
     */
    public void restoreStructure(UUID structureId) {
        List<PlacedBlock> blocks = activeStructures.remove(structureId);
        if (blocks == null) return;
        
        BukkitTask task = restorationTasks.remove(structureId);
        if (task != null) {
            task.cancel();
        }
        
        // Restaurer bloc par bloc
        for (PlacedBlock placed : blocks) {
            Location loc = placed.getLocation();
            BlockState originalState = placed.getOriginalState();
            
            if (loc.getWorld() != null) {
                Block currentBlock = loc.getBlock();
                BlockState newState = loc.getBlock().getState();
                
                // Ne restaurer que si le bloc n'a pas été modifié par autre chose
                // (optionnel - ici on restaure toujours)
                originalState.update(true, false);
            }
        }
    }
    
    /**
     * Restaure toutes les structures actives (utilisé au shutdown)
     */
    public void restoreAll() {
        new ArrayList<>(activeStructures.keySet()).forEach(this::restoreStructure);
    }
    
    /**
     * Classe interne pour stocker un bloc placé et son état original
     */
    private static class PlacedBlock {
        private final Location location;
        private final BlockState originalState;
        
        public PlacedBlock(Location location, BlockState originalState) {
            this.location = location.clone();
            this.originalState = originalState;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public BlockState getOriginalState() {
            return originalState;
        }
    }
}
