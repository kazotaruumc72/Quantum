package com.wynvers.quantum.jobs;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.wynvers.quantum.Quantum;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Listener pour les actions de métier
 * Gère les événements: break, place, hit, fish, drink, eat, kill
 */
public class JobActionListener implements Listener {
    
    private final Quantum plugin;
    private final JobManager jobManager;
    
    public JobActionListener(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }
    
    /**
     * Action: BREAK - Casser un bloc
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Vérifier si c'est un bloc Nexo
        CustomBlockMechanic blockMechanic = NexoBlocks.customBlockMechanic(block);
        if (blockMechanic != null) {
            String nexoId = blockMechanic.getItemID();
            jobManager.handleNexoInteraction(player, nexoId, false);
            return;
        }
        
        // Sinon, action break normale
        jobManager.handleAction(player, "break", block.getType().name());
    }
    
    /**
     * Action: PLACE - Placer un bloc
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        jobManager.handleAction(player, "place", block.getType().name());
    }
    
    /**
     * Action: HIT - Frapper une entité
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        Entity target = event.getEntity();
        
        // Vérifier si c'est une furniture Nexo
        FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(target);
        if (furnitureMechanic != null) {
            String furnitureId = furnitureMechanic.getItemID();
            jobManager.handleNexoInteraction(player, furnitureId, true);
            return;
        }
        
        // Sinon, action hit normale
        if (target instanceof LivingEntity) {
            jobManager.handleAction(player, "hit", target.getType().name());
        }
    }
    
    /**
     * Action: KILL - Tuer un mob
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) {
            return;
        }
        
        jobManager.handleAction(killer, "kill", entity.getType().name());
    }
    
    /**
     * Action: FISH - Pêcher
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        
        Player player = event.getPlayer();
        Entity caught = event.getCaught();
        
        String caughtType = caught != null ? caught.getType().name() : "FISH";
        jobManager.handleAction(player, "fish", caughtType);
    }
    
    /**
     * Action: DRINK et EAT - Consommer un item
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Material material = item.getType();
        
        // Déterminer si c'est une boisson ou de la nourriture
        if (material.name().contains("POTION") || material == Material.MILK_BUCKET) {
            jobManager.handleAction(player, "drink", material.name());
        } else {
            jobManager.handleAction(player, "eat", material.name());
        }
    }
}
