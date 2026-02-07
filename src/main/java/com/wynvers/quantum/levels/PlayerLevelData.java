package com.wynvers.quantum.levels;

import java.util.UUID;

public class PlayerLevelData {

    private final UUID uuid;
    private int level;
    private int exp;

    public PlayerLevelData(UUID uuid, int level, int exp) {
        this.uuid = uuid;
        this.level = level;
        this.exp = exp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
