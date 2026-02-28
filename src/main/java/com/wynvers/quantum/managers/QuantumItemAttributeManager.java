package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player "active item" context for the Quantum Item Attributes Modifier menu.
 * Tracks which inventory slot is being modified and provides apply / reset helpers.
 */
public class QuantumItemAttributeManager {

    /** Namespace prefix used to identify Quantum-added modifiers (plugin namespace). */
    private static final String MODIFIER_KEY_PREFIX = "quantum_attr_";

    private final Quantum plugin;
    /** Lowercase plugin name cached to avoid repeated allocations in hot paths. */
    private final String pluginNamespace;
    /** player UUID → inventory slot of the item being modified */
    private final Map<UUID, Integer> activeSlots = new HashMap<>();

    public QuantumItemAttributeManager(Quantum plugin) {
        this.plugin = plugin;
        this.pluginNamespace = plugin.getName().toLowerCase();
    }

    // ─────────────────── Context ───────────────────

    public void setActiveSlot(Player player, int slot) {
        activeSlots.put(player.getUniqueId(), slot);
    }

    public int getActiveSlot(Player player) {
        return activeSlots.getOrDefault(player.getUniqueId(), -1);
    }

    public ItemStack getActiveItem(Player player) {
        int slot = getActiveSlot(player);
        if (slot < 0) return null;
        return player.getInventory().getItem(slot);
    }

    public void clearActiveSlot(Player player) {
        activeSlots.remove(player.getUniqueId());
    }

    // ─────────────────── Apply modifier ───────────────────

    /**
     * Apply (or replace) an attribute modifier on the player's active item.
     *
     * @param attributeName config name, e.g. "MOVEMENT_SPEED" or "GENERIC_MOVEMENT_SPEED"
     * @param amount        modifier value
     * @param operationInt  0=ADD_NUMBER, 1=ADD_SCALAR, 2=MULTIPLY_SCALAR_1
     * @param slotStr       equipment slot string: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD (or null/empty)
     */
    public void applyModifier(Player player, String attributeName, double amount, int operationInt, String slotStr) {
        ItemStack item = getActiveItem(player);
        if (item == null || !item.hasItemMeta()) {
            player.sendMessage("§c§l✗ §cAucun item actif à modifier.");
            return;
        }

        Attribute attribute = parseAttribute(attributeName);
        if (attribute == null) {
            player.sendMessage("§c§l✗ §cAttribut inconnu: §e" + attributeName);
            return;
        }

        AttributeModifier.Operation operation;
        AttributeModifier.Operation[] ops = AttributeModifier.Operation.values();
        operation = (operationInt >= 0 && operationInt < ops.length) ? ops[operationInt] : ops[0];

        EquipmentSlotGroup eqSlot = parseEquipmentSlotGroup(slotStr);

        // Unique key per attribute so repeated clicks replace the modifier
        NamespacedKey key = new NamespacedKey(plugin, MODIFIER_KEY_PREFIX + sanitize(attributeName));
        AttributeModifier modifier = new AttributeModifier(key, amount, operation, eqSlot);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Remove any previous quantum modifier for this attribute
        Collection<AttributeModifier> existing = meta.getAttributeModifiers(attribute);
        if (existing != null) {
            for (AttributeModifier mod : new ArrayList<>(existing)) {
                if (isQuantumModifier(mod)) {
                    meta.removeAttributeModifier(attribute, mod);
                }
            }
        }

        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);

        int slot = getActiveSlot(player);
        player.getInventory().setItem(slot, item);

        String opLabel = operationLabel(operationInt);
        player.sendMessage("§a§l✓ §aAttribut §e" + attributeName
                + " §aappliqué! §7(" + opLabel + " §f" + amount + "§7)");
    }

    // ─────────────────── Reset modifiers ───────────────────

    /**
     * Remove ALL Quantum attribute modifiers from the player's active item.
     */
    public void resetModifiers(Player player) {
        ItemStack item = getActiveItem(player);
        if (item == null || !item.hasItemMeta()) {
            player.sendMessage("§c§l✗ §cAucun item actif à réinitialiser.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        boolean changed = false;
        // Use the item's own attribute map rather than iterating the full registry
        com.google.common.collect.Multimap<Attribute, AttributeModifier> allMods = meta.getAttributeModifiers();
        if (allMods != null) {
            for (Map.Entry<Attribute, AttributeModifier> entry : new ArrayList<>(allMods.entries())) {
                if (isQuantumModifier(entry.getValue())) {
                    meta.removeAttributeModifier(entry.getKey(), entry.getValue());
                    changed = true;
                }
            }
        }

        if (changed) {
            item.setItemMeta(meta);
            int slot = getActiveSlot(player);
            player.getInventory().setItem(slot, item);
            player.sendMessage("§a§l✓ §aTous les attributs Quantum ont été réinitialisés!");
        } else {
            player.sendMessage("§7Aucun attribut Quantum à réinitialiser sur cet item.");
        }
    }

    // ─────────────────── Helpers ───────────────────

    private boolean isQuantumModifier(AttributeModifier mod) {
        return mod.getKey().getNamespace().equals(pluginNamespace)
                && mod.getKey().getKey().startsWith(MODIFIER_KEY_PREFIX);
    }

    /**
     * Resolve a config attribute name to a Bukkit {@link Attribute}.
     * Accepts: "MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED", "generic.movement_speed", etc.
     */
    private Attribute parseAttribute(String name) {
        if (name == null || name.isEmpty()) return null;

        String lower = name.toLowerCase();
        java.util.List<String> keysToTry = new ArrayList<>();

        if (lower.contains(".")) {
            // Already a registry-style key, try directly
            keysToTry.add(lower);
        } else if (lower.startsWith("generic_")) {
            // GENERIC_MOVEMENT_SPEED → generic.movement_speed
            keysToTry.add("generic." + lower.substring(8));
        } else {
            // MOVEMENT_SPEED → try generic.movement_speed first, then bare
            keysToTry.add("generic." + lower);
            keysToTry.add(lower);
        }

        for (String keyStr : keysToTry) {
            try {
                Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(keyStr));
                if (attr != null) return attr;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private EquipmentSlotGroup parseEquipmentSlotGroup(String slot) {
        if (slot == null || slot.isEmpty()) return EquipmentSlotGroup.ANY;
        switch (slot.toUpperCase()) {
            case "HAND":
            case "MAIN_HAND": return EquipmentSlotGroup.MAIN_HAND;
            case "OFF_HAND":  return EquipmentSlotGroup.OFF_HAND;
            case "FEET":      return EquipmentSlotGroup.FEET;
            case "LEGS":      return EquipmentSlotGroup.LEGS;
            case "CHEST":     return EquipmentSlotGroup.CHEST;
            case "HEAD":      return EquipmentSlotGroup.HEAD;
            default:          return EquipmentSlotGroup.ANY;
        }
    }

    private String sanitize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    private String operationLabel(int op) {
        switch (op) {
            case 1:  return "ADD_SCALAR";
            case 2:  return "MULTIPLY_SCALAR_1";
            default: return "ADD_NUMBER";
        }
    }
}
