package com.wynvers.quantum.storage.upgrades;

public class MultiplierUpgrade extends StorageUpgrade {

    private static final double[] MULTIPLIERS = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};

    public MultiplierUpgrade() {
        super("multiplier");
        this.level = 0;
    }

    @Override
    public String getDisplayName() {
        return "§e§lMultiplicateur de Gains";
    }

    @Override
    public String getDescription() {
        return "§7Multiplie vos revenus de vente jusqu'à x5";
    }

    @Override
    public double getCost() {
        return 5000.0 * (level + 1);
    }

    public double getMultiplier() {
        if (level >= MULTIPLIERS.length) {
            return MULTIPLIERS[MULTIPLIERS.length - 1];
        }
        return MULTIPLIERS[level];
    }

    public double getNextMultiplier() {
        if (level + 1 >= MULTIPLIERS.length) {
            return MULTIPLIERS[MULTIPLIERS.length - 1];
        }
        return MULTIPLIERS[level + 1];
    }

    public boolean isMaxed() {
        return level >= MULTIPLIERS.length - 1;
    }
}