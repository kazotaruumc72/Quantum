package com.wynvers.quantum.towers.events;

import com.wynvers.quantum.towers.TowerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player leaves a tower region.
 */
public class TowerLeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final TowerConfig tower;
    private final int floor;

    public TowerLeaveEvent(Player player, TowerConfig tower, int floor) {
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
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
