package com.wynvers.quantum.towers;

import org.bukkit.entity.EntityType;

/**
 * Represents a single mob-kill requirement for a tower floor door.
 *
 * <p>Only MythicMobs format is supported in {@code towers.yml}:
 * <ul>
 *   <li>{@code mm:MobId:amount}  – a MythicMobs mob type</li>
 * </ul>
 *
 * <p>Examples:
 * <pre>
 * mob_kills_required:
 *   - 'mm:SkeletonKing:5'
 *   - 'mm:WaterGuard:10'
 * </pre>
 */
public class FloorMobRequirement {

    public enum MobSource { MYTHICMOBS, MINECRAFT }

    private final MobSource source;
    /** MythicMobs mob type ID (only when source == MYTHICMOBS) */
    private final String mythicId;
    /** Bukkit EntityType (only when source == MINECRAFT) */
    private final EntityType entityType;
    private final int amount;

    private FloorMobRequirement(MobSource source, String mythicId, EntityType entityType, int amount) {
        this.source = source;
        this.mythicId = mythicId;
        this.entityType = entityType;
        this.amount = amount;
    }

    /**
     * Parse a raw YAML string into a {@link FloorMobRequirement}.
     *
     * @param raw the raw string from towers.yml, e.g. {@code "mm:SkeletonKing:5"}
     * @return the parsed requirement, or {@code null} if the string is malformed
     */
    public static FloorMobRequirement parse(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String trimmed = raw.trim();

        // MythicMobs format: mm:<mobId>:<amount>
        if (!trimmed.toLowerCase().startsWith("mm:")) {
            return null; // Only MythicMobs format is supported
        }

        String[] parts = trimmed.split(":", 3);
        if (parts.length != 3) return null;
        String mobId = parts[1];
        int amount = parseAmount(parts[2]);
        if (mobId.isEmpty() || amount <= 0) return null;
        return new FloorMobRequirement(MobSource.MYTHICMOBS, mobId, null, amount);
    }

    private static int parseAmount(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns a stable key used to look up kills in {@link TowerProgress}.
     * <ul>
     *   <li>MythicMobs: {@code "mm:<mythicId>"}</li>
     *   <li>Minecraft:  {@code "mc:<ENTITY_TYPE>"}</li>
     * </ul>
     */
    public String getKey() {
        if (source == MobSource.MYTHICMOBS) {
            return "mm:" + mythicId;
        }
        return "mc:" + entityType.name();
    }

    public MobSource getSource() { return source; }
    public String getMythicId() { return mythicId; }
    public EntityType getEntityType() { return entityType; }
    public int getAmount() { return amount; }

    @Override
    public String toString() {
        if (source == MobSource.MYTHICMOBS) return "mm:" + mythicId + ":" + amount;
        return entityType.name().toLowerCase() + ":" + amount;
    }
}
