package com.wynvers.quantum.storage.upgrades;

public class StoragePlusUpgrade extends StorageUpgrade {
    private static final int BASE_STORAGE = 200;

    public StoragePlusUpgrade() {
        super("storage_plus");
        this.level = 0;
    }

    @Override
    public String getDisplayName() {
        return "§9§lStockage Plus";
    }

    @Override
    public String getDescription() {
        return "§7Ajoute 200 items supplémentaires par niveau";
    }

    @Override
    public double getCost() {
        return 10000.0 * (level + 1);
    }

    public int getAdditionalStorage() {
        return BASE_STORAGE * level;
    }

    public int getTotalCapacity() {
        return 200 + getAdditionalStorage();
    }

    public boolean canUpgrade() {
        return true;
    }
}