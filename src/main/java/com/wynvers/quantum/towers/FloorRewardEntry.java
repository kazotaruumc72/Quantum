package com.wynvers.quantum.towers;

/**
 * Represents a single selectable reward entry shown in the floor reward selection menu.
 *
 * <p>Types:
 * <ul>
 *   <li>{@link Type#NEXO} – a custom item delivered via the Nexo API</li>
 *   <li>{@link Type#MYTHIC} – an item delivered via the MythicMobs API</li>
 *   <li>{@link Type#COMMAND} – a console command (auto-executed, not user-selectable)</li>
 * </ul>
 */
public class FloorRewardEntry {

    public enum Type { NEXO, MYTHIC, COMMAND }

    private final Type type;
    private final String id;       // nexo/mythic id or raw command string
    private final int amount;
    private final double chance;
    private final String message;  // optional message shown on claim (may be null)

    public FloorRewardEntry(Type type, String id, int amount, double chance, String message) {
        this.type = type;
        this.id = id;
        this.amount = amount;
        this.chance = chance;
        this.message = message;
    }

    public Type getType() { return type; }
    public String getId() { return id; }
    public int getAmount() { return amount; }
    public double getChance() { return chance; }
    public String getMessage() { return message; }
}
