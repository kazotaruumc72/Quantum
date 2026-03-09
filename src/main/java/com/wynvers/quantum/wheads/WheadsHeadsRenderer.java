package com.wynvers.quantum.wheads;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import com.wynvers.quantum.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Renders player heads from Wheads API into menu slots
 * Similar to StorageRenderer but for player heads
 */
public class WheadsHeadsRenderer {

    private final Quantum plugin;
    private final WheadsAPI wheadsAPI;
    private final NamespacedKey headUuidKey;
    private final NamespacedKey headNameKey;

    public WheadsHeadsRenderer(Quantum plugin) {
        this.plugin = plugin;
        this.wheadsAPI = plugin.getWheadsAPI();
        this.headUuidKey = new NamespacedKey(plugin, "wheads_head_uuid");
        this.headNameKey = new NamespacedKey(plugin, "wheads_head_name");
    }

    /**
     * Configuration for lore append
     */
    public static class LoreAppendConfig {
        private final List<String> loreTemplate;

        public LoreAppendConfig(List<String> loreTemplate) {
            this.loreTemplate = loreTemplate;
        }

        public List<String> getLoreTemplate() {
            return loreTemplate;
        }
    }

    /**
     * Render player heads into wheads_heads slots
     */
    public void renderPlayerHeads(Player player, Inventory inventory, Menu menu, LoreAppendConfig loreConfig) {
        if (!wheadsAPI.isEnabled()) {
            plugin.getLogger().warning("Cannot render player heads - Wheads API is not enabled");
            return;
        }

        // Get all player heads from Wheads API
        List<WheadsPlayerHead> heads = wheadsAPI.getAllPlayerHeads();

        // Find slots designated for wheads heads
        List<Integer> wheadsSlots = new ArrayList<>();
        for (MenuItem menuItem : menu.getItems().values()) {
            if (menuItem.isWheadsPlayerHead()) {
                wheadsSlots.addAll(menuItem.getSlots());
            }
        }

        if (wheadsSlots.isEmpty()) {
            return;
        }

        // Render heads into slots
        int index = 0;
        for (int slot : wheadsSlots) {
            if (index >= heads.size()) {
                break;
            }

            WheadsPlayerHead head = heads.get(index);
            ItemStack headItem = createPlayerHeadItem(head, loreConfig);

            if (headItem != null) {
                inventory.setItem(slot, headItem);
            }

            index++;
        }
    }

    /**
     * Create an ItemStack for a player head
     */
    private ItemStack createPlayerHeadItem(WheadsPlayerHead head, LoreAppendConfig loreConfig) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta == null) {
            return null;
        }

        try {
            // Set the player profile with texture
            PlayerProfile profile = createPlayerProfile(head);
            meta.setOwnerProfile(profile);

            // Set display name
            meta.setDisplayName("§e" + head.getPlayerName());

            // Set lore
            List<String> lore = new ArrayList<>();
            if (loreConfig != null && loreConfig.getLoreTemplate() != null) {
                for (String line : loreConfig.getLoreTemplate()) {
                    String parsed = line
                            .replace("%head_owner%", head.getPlayerName())
                            .replace("%head_uuid%", head.getPlayerUuid());
                    lore.add(parsed);
                }
            } else {
                lore.add("§7Player: §f" + head.getPlayerName());
                lore.add("§7UUID: §f" + head.getPlayerUuid());
                lore.add("");
                lore.add("§e► Click to interact");
            }
            meta.setLore(lore);

            // Store head UUID in PDC for click handling
            meta.getPersistentDataContainer().set(headUuidKey, PersistentDataType.STRING, head.getPlayerUuid());
            meta.getPersistentDataContainer().set(headNameKey, PersistentDataType.STRING, head.getPlayerName());

            item.setItemMeta(meta);
            return item;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating player head item for " + head.getPlayerName(), e);
            return null;
        }
    }

    /**
     * Create a PlayerProfile with custom texture
     */
    private PlayerProfile createPlayerProfile(WheadsPlayerHead head) {
        try {
            // Create a player profile
            UUID uuid = UUID.fromString(head.getPlayerUuid());
            PlayerProfile profile = plugin.getServer().createPlayerProfile(uuid, head.getPlayerName());

            // Set custom texture if available
            if (head.getTextureValue() != null && !head.getTextureValue().isEmpty()) {
                PlayerTextures textures = profile.getTextures();

                // Decode base64 texture and create URL
                // Minecraft texture URLs follow the pattern: http://textures.minecraft.net/texture/{hash}
                String textureUrl = decodeTextureUrl(head.getTextureValue());
                if (textureUrl != null) {
                    textures.setSkin(new URL(textureUrl));
                    profile.setTextures(textures);
                }
            }

            return profile;

        } catch (IllegalArgumentException e) {
            // Invalid UUID format, create random one
            UUID randomUuid = UUID.randomUUID();
            return plugin.getServer().createPlayerProfile(randomUuid, head.getPlayerName());
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid texture URL for head: " + head.getPlayerName(), e);
            UUID uuid = UUID.randomUUID();
            return plugin.getServer().createPlayerProfile(uuid, head.getPlayerName());
        }
    }

    /**
     * Decode base64 texture value to get texture URL
     * This is a placeholder - actual implementation depends on Wheads API format
     */
    private String decodeTextureUrl(String textureValue) {
        try {
            // If textureValue is already a URL, return it
            if (textureValue.startsWith("http://") || textureValue.startsWith("https://")) {
                return textureValue;
            }

            // If it's a base64 encoded value, decode it
            // This is a simplified version - actual implementation may vary
            byte[] decoded = java.util.Base64.getDecoder().decode(textureValue);
            String decodedStr = new String(decoded);

            // Extract URL from JSON format: {"textures":{"SKIN":{"url":"..."}}}
            if (decodedStr.contains("\"url\"")) {
                int urlStart = decodedStr.indexOf("\"url\":\"") + 7;
                int urlEnd = decodedStr.indexOf("\"", urlStart);
                if (urlStart > 7 && urlEnd > urlStart) {
                    return decodedStr.substring(urlStart, urlEnd);
                }
            }

            return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error decoding texture value", e);
            return null;
        }
    }

    /**
     * Get NamespacedKey for head UUID
     */
    public NamespacedKey getHeadUuidKey() {
        return headUuidKey;
    }

    /**
     * Get NamespacedKey for head name
     */
    public NamespacedKey getHeadNameKey() {
        return headNameKey;
    }
}
