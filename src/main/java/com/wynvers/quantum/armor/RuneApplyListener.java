package com.wynvers.quantum.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class RuneApplyListener implements Listener {

    private final RuneItem runeItem;

    public RuneApplyListener(RuneItem runeItem) {
        this.runeItem = runeItem;
    }

    @EventHandler
    public void onRuneDragAndDrop(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER) return;

        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) return;
        if (!runeItem.isRune(cursor)) return;

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        String name = current.getType().name();
        if (!name.endsWith("_HELMET") && !name.endsWith("_CHESTPLATE") 
            && !name.endsWith("_LEGGINGS") && !name.endsWith("_BOOTS")) return;

        event.setCancelled(true);

        RuneType runeType = RuneType.FORCE;
        int level = 1;

        int result = runeItem.applyRuneOnArmor(cursor, current, runeType, level);
        
        if (result == -1) {
            player.sendMessage("§cArmure déjà runée ou rune invalide.");
            return;
        }

        if (result == 0) {
            player.sendMessage("§cLa rune a échoué et a été détruite.");
        } else {
            player.sendMessage("§aRune appliquée avec succès !");
        }

        int amount = cursor.getAmount();
        if (amount <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(amount - 1);
            event.setCursor(cursor);
        }
    }
}
