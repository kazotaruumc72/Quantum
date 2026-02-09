// This class handles the auto-sell upgrade for the Quantum game.
package com.wynvers.quantum.storage.upgrades;

public class AutoSellUpgrade {
    private int sellAmount;
    private int cost;

    public AutoSellUpgrade(int sellAmount, int cost) {
        this.sellAmount = sellAmount;
        this.cost = cost;
    }

    public int getSellAmount() {
        return sellAmount;
    }

    public int getCost() {
        return cost;
    }

    public void sell() {
        // Logic to sell items automatically
    }
}