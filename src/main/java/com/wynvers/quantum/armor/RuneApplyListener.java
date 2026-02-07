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
    private final DungeonArmor dungeonArmor;
    private final Random random = new Random();

    public RuneApplyListener(RuneItem runeItem, DungeonArmor dungeonArmor) {
        this.runeItem = runeItem;
        this.dungeonArmor = dungeonArmor;
    }

    @EventHandler
    public void onRuneDragAndDrop(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // On ne gère que l'inventaire du joueur (PLAYER / CRAFTING)
        if (event.getClickedInventory() == null) {
            return;
        }
        InventoryType type = event.getClickedInventory().getType();
        if (type != InventoryType.PLAYER && type != InventoryType.CRAFTING) {
            return;
        }

        // 1) Item sur le curseur = doit être une rune
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            return;
        }

        if (!runeItem.isRune(cursor)) {
            return;
        }

        // 2) Item cliqué = doit être une ARMURE DE DONJON (pas vanilla)
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        // Ici on utilise ta logique existante pour détecter l'armure de donjon
        if (!dungeonArmor.isDungeonArmor(current)) {
            return;
        }

        // À partir d'ici : curseur = rune valide, current = armure de donjon valide
        event.setCancelled(true);

        // On récupère le pourcentage stocké dans la rune
        int successChance = runeItem.getSuccessChance(cursor);
        if (successChance < 0) {
            player.sendMessage("§cCette rune est invalide (pas de taux de réussite).");
            return;
        }

        int roll = random.nextInt(100) + 1; // 1-100

        if (roll > successChance) {
            // Échec : on détruit la rune, on ne touche pas à l’armure
            player.sendMessage("§cLa rune a échoué (" + roll + "% / " + successChance + "%) et a été détruite.");
            consumeOneRune(event, cursor);
            return;
        }

        // Succès : on applique une rune sur l’armure de donjon via DungeonArmor
        // Pour l’instant, on applique FORCE niveau 1 en dur
        // (on pourra plus tard lire le type/niveau exact depuis le PDC de la rune)
        RuneType runeType = RuneType.FORCE;
        int level = 1;

        boolean applied = dungeonArmor.applyRune(current, runeType, level);
        if (!applied) {
            player.sendMessage("§cImpossible d'appliquer la rune (slot plein ou déjà présente).");
            return;
        }

        player.sendMessage("§aLa rune a été appliquée avec succès sur ton armure de donjon !");
        consumeOneRune(event, cursor);
    }

    private void consumeOneRune(InventoryClickEvent event, ItemStack cursor) {
        int amount = cursor.getAmount();
        if (amount <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(amount - 1);
            event.setCursor(cursor);
        }
    }
}
