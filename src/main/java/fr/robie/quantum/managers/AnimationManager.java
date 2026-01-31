package fr.robie.quantum.managers;

import fr.robie.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationManager {
    
    private final Quantum plugin;
    private final Map<UUID, BukkitTask> activeTasks;
    
    public AnimationManager(Quantum plugin) {
        this.plugin = plugin;
        this.activeTasks = new ConcurrentHashMap<>();
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
                
                String title = frames.get(currentFrame);
                updateTitle(player, title);
                
                currentFrame = (currentFrame + 1) % frames.size();
            }
        }, 0L, speed);
        
        activeTasks.put(uuid, task);
    }
    
    /**
     * Update inventory title (via reflection or packet)
     */
    private void updateTitle(Player player, String title) {
        // Note: Title updating requires NMS or ProtocolLib
        // For now, we'll skip the actual update
        // This would be implemented with proper packet manipulation
    }
    
    /**
     * Stop animation for player
     */
    public void stopAnimation(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = activeTasks.remove(uuid);
        
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
    }
    
    /**
     * Reload (stop all)
     */
    public void reload() {
        stopAll();
    }
}
