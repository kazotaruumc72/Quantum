package io.github.pigaut.voxel.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Stub class for VoxelSpigot PlayerEvent
 * This stub is provided because VoxelSpigot is not available in Maven repositories
 * The actual implementation will be provided by the Orestack plugin at runtime
 */
public abstract class PlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
