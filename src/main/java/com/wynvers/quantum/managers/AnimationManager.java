package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationManager {
    
    private final Quantum plugin;
    private final Map<UUID, BukkitTask> activeTasks;
    private final Map<UUID, String> lastTitles; // Pour éviter les rafraîchissements inutiles
    
    public AnimationManager(Quantum plugin) {
        this.plugin = plugin;
        this.activeTasks = new ConcurrentHashMap<>();
        this.lastTitles = new ConcurrentHashMap<>();
    }
    
    /**
     * Start animated title for player
     */
    public void startAnimation(Player player, List<String> frames, int speed) {
        // Stop existing animation if any
        stopAnimation(player);
        
        if (frames == null || frames.isEmpty()) {
            return;
        }
        
        // Single frame = no animation
        if (frames.size() == 1) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int currentFrame = 0;
            
            @Override
            public void run() {
                if (!player.isOnline() || player.getOpenInventory() == null) {
                    stopAnimation(player);
                    return;
                }
                
                // Traiter les placeholders dans le titre
                String title = frames.get(currentFrame);
                title = plugin.getPlaceholderManager().parse(player, title);
                
                // Seulement rafraîchir si le titre a changé
                String lastTitle = lastTitles.get(uuid);
                if (!title.equals(lastTitle)) {
                    updateTitle(player, title);
                    lastTitles.put(uuid, title);
                }
                
                currentFrame = (currentFrame + 1) % frames.size();
            }
        }, 0L, speed * 20L); // Convertir secondes en ticks (1 sec = 20 ticks)
        
        activeTasks.put(uuid, task);
    }
    
    /**
     * Update inventory title by recreating inventory
     * Note: This causes a brief flicker but doesn't require external dependencies
     */
    private void updateTitle(Player player, String newTitle) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return;
        
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) return;
        
        // Déterminer quel menu est ouvert
        Menu menu = plugin.getMenuManager().getMenuByTitle(view.getTitle());
        if (menu == null) return;
        
        // Créer un nouvel inventaire avec le nouveau titre
        Inventory newInventory = Bukkit.createInventory(null, topInventory.getSize(), newTitle);
        
        // Copier tous les items de l'ancien inventaire
        for (int i = 0; i < topInventory.getSize(); i++) {
            newInventory.setItem(i, topInventory.getItem(i));
        }
        
        // Réouvrir l'inventaire avec le nouveau titre
        player.openInventory(newInventory);
    }
    
    /**
     * Stop animation for player
     */
    public void stopAnimation(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = activeTasks.remove(uuid);
        lastTitles.remove(uuid);
        
        if (task != null) {
            task.cancel();
        }
    }
    
    /**
     * Stop all animations
     */
    public void stopAll() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
        lastTitles.clear();
    }
    
    /**
     * Reload (stop all)
     */
    public void reload() {
        stopAll();
    }
}
