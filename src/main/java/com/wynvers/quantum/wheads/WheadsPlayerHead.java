package com.wynvers.quantum.wheads;

/**
 * Represents a player head from the Wheads API
 */
public class WheadsPlayerHead {
    private final String playerName;
    private final String playerUuid;
    private final String textureValue;
    private final String textureSignature;
    private final long timestamp;

    public WheadsPlayerHead(String playerName, String playerUuid, String textureValue, String textureSignature) {
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.textureValue = textureValue;
        this.textureSignature = textureSignature;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getTextureValue() {
        return textureValue;
    }

    public String getTextureSignature() {
        return textureSignature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "WheadsPlayerHead{" +
                "playerName='" + playerName + '\'' +
                ", playerUuid='" + playerUuid + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
