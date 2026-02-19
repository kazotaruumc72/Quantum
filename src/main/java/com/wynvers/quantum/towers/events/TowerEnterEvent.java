package com.wynvers.quantum.towers.events;

import com.wynvers.quantum.towers.TowerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player enters a tower floor region.
 * Cancelling this event will block the player from entering.
 */
public class TowerEnterEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final TowerConfig tower;
    private final int floor;
    private boolean cancelled = false;

    public TowerEnterEvent(Player player, TowerConfig tower, int floor) {
        this.player = player;
        this.tower = tower;
        this.floor = floor;
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

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
