package com.wynvers.quantum.apartment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an apartment that can be owned by a player.
 * Includes contract deadline, size, zone, and furniture management.
 */
public class Apartment {

    /**
     * Apartment size tiers affecting contract pricing
     */
    public enum Size {
        SMALL("Petit", 1.0),
        MEDIUM("Moyen", 1.5),
        LARGE("Grand", 2.0);

        private final String displayName;
        private final double priceMultiplier;

        Size(String displayName, double priceMultiplier) {
            this.displayName = displayName;
            this.priceMultiplier = priceMultiplier;
        }

        public String getDisplayName() { return displayName; }
        public double getPriceMultiplier() { return priceMultiplier; }
    }

    private final int apartmentId;
    private UUID ownerId;
    private String apartmentName;
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;
    private int tier;
    private boolean isLocked;
    private Size size;
    private String zoneName;
    private LocalDateTime contractDeadline;
    private final List<String> furniture; // Nexo furniture IDs owned

    public Apartment(int apartmentId, UUID ownerId, String apartmentName) {
        this.apartmentId = apartmentId;
        this.ownerId = ownerId;
        this.apartmentName = apartmentName;
        this.tier = 1;
        this.isLocked = false;
        this.size = Size.SMALL;
        this.zoneName = "";
        this.contractDeadline = null;
        this.furniture = new ArrayList<>();
    }

    public int getApartmentId() { return apartmentId; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getApartmentName() { return apartmentName; }
    public void setApartmentName(String apartmentName) { this.apartmentName = apartmentName; }

    public String getWorldName() { return worldName; }

    public void setLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public LocalDateTime getContractDeadline() { return contractDeadline; }
    public void setContractDeadline(LocalDateTime contractDeadline) { this.contractDeadline = contractDeadline; }

    /**
     * Check if the contract has expired
     */
    public boolean isContractExpired() {
        return contractDeadline != null && LocalDateTime.now().isAfter(contractDeadline);
    }

    /**
     * Check if the apartment has an active contract
     */
    public boolean hasActiveContract() {
        return contractDeadline != null && !isContractExpired();
    }

    /**
     * Add days to the contract deadline
     */
    public void addContractDays(long days) {
        if (contractDeadline == null || isContractExpired()) {
            contractDeadline = LocalDateTime.now().plusDays(days);
        } else {
            contractDeadline = contractDeadline.plusDays(days);
        }
    }

    public List<String> getFurniture() { return furniture; }

    public void addFurniture(String furnitureId) {
        furniture.add(furnitureId);
    }

    public boolean removeFurniture(String furnitureId) {
        return furniture.remove(furnitureId);
    }

    /**
     * Move all placed furniture to storage (on contract expiry)
     */
    public void storeAllFurniture() {
        // Furniture list is kept as-is (represents stored items)
        // Actual world removal is handled by ApartmentManager
    }

    /**
     * Get the base price for contract extension (per day)
     */
    public double getBasePricePerDay() {
        return switch (size) {
            case SMALL -> 500.0 / 7.0;
            case MEDIUM -> 750.0 / 7.0;
            case LARGE -> 1000.0 / 7.0;
        };
    }

    /**
     * Get the contract price for a given number of days
     */
    public double getContractPrice(int days) {
        return getBasePricePerDay() * days * size.getPriceMultiplier();
    }
}
