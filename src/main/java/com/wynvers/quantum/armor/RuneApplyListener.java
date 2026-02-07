package com.wynvers.quantum.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RuneApplyListener implements Listener {

    private final RuneItem runeItem;
    private final Random random = new Random();

    public RuneApplyListener(RuneItem runeItem) {
        this.runeItem = runeItem;
    }

    @EventHandler
    public void onRuneDragAndDrop(InventoryClickEvent event) {
        // On ne gère que les clics d'un joueur
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // On ne gère que l'inventaire du joueur (pas les GUIs custom, coffres, etc.)
        if (event.getClickedInventory() == null
                || event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        // Item sur le curseur (doit être une rune)
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            return;
        }

        // On vérifie que c'est bien une rune via RuneItem
        if (!runeItem.isRune(cursor)) {
            return;
        }

        // Item cliqué (doit être une armure)
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        // Vérifier que c'est une pièce d'armure
        if (!isArmorPiece(current.getType())) {
            return;
        }

        // À ce stade : curseur = rune, slot cliqué = armure => on applique
        event.setCancelled(true); // empêche l'échange vanilla

        // Détecter le type et niveau de la rune (à adapter selon ton système)
        // Pour l'instant on utilise des valeurs par défaut
        RuneType runeType = detectRuneType(cursor);
        int level = detectRuneLevel(cursor);

        int result = runeItem.applyRuneOnArmor(cursor, current, runeType, level);
        
        if (result == -1) {
            player.sendMessage("§cCette rune ne peut pas être appliquée ici (déjà runée ou invalide).");
            return;
        }

        if (result == 0) {
            player.sendMessage("§cLa rune a échoué et a été détruite.");
        } else {
            player.sendMessage("§aLa rune a été appliquée avec succès sur ton armure !");
        }

        // Consommer la rune
        int amount = cursor.getAmount();
        if (amount <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(amount - 1);
            event.setCursor(cursor);
        }
    }

    private boolean isArmorPiece(Material type) {
        String name = type.name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }

    private RuneType detectRuneType(ItemStack runeItem) {
        // À implémenter selon ton système (via PDC, nom, lore, etc.)
        // Par défaut on retourne HEALTH
        return RuneType.HEALTH;
    }

    private int detectRuneLevel(ItemStack runeItem) {
        // À implémenter selon ton système
        // Par défaut on retourne 1
        return 1;
    }
}
