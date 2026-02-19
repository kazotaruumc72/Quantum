package com.wynvers.quantum.towers.events;

import com.wynvers.quantum.towers.TowerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player completes an entire tower (clears the final boss floor).
 * This event is not cancellable – use {@link TowerFloorCompleteEvent} if you
 * want to intercept the final floor completion itself.
 */
public class TowerCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final TowerConfig tower;
    /** How many times the player has now completed this tower (1 = first clear). */
    private final int runs;

    public TowerCompleteEvent(Player player, TowerConfig tower, int runs) {
        this.player = player;
        this.tower = tower;
        this.runs = runs;
    }

    public Player getPlayer() {
        return player;
    }

    public TowerConfig getTower() {
        return tower;
    }

    public String getTowerId() {
        return tower.getId();
    }

    /** @return total run count for this player on this tower after this completion */
    public int getRuns() {
        return runs;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
