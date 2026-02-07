package com.wynvers.quantum.armor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
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
        // DEBUG : Affiche dans la console ce qui se passe
        Bukkit.getConsoleSender().sendMessage("§7[DEBUG] Click détecté : " + event.getAction() + " | Curseur: " + event.getCursor() + " | Current: " + event.getCurrentItem());

        if (!(event.getWhoClicked() instanceof Player player)) return;

        // IMPORTANT : Accepter plusieurs types d'actions (clic gauche, droit, etc.)
        InventoryAction action = event.getAction();
        if (action != InventoryAction.PLACE_ALL && 
            action != InventoryAction.PLACE_ONE && 
            action != InventoryAction.SWAP_WITH_CURSOR &&
            action != InventoryAction.PICKUP_ALL) {
            return;
        }

        // Vérifier que c'est l'inventaire du joueur (pas un coffre, pas un menu)
        if (event.getInventory().getType() != InventoryType.PLAYER && 
            event.getInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        // Récupérer l'item sur le curseur (la rune)
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) return;

        // Vérifier que c'est une rune
        if (!runeItem.isRune(cursor)) {
            return;
        }

        // Récupérer l'item cliqué (l'armure)
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        // Vérifier que c'est une armure
        String name = current.getType().name();
        if (!name.endsWith("_HELMET") && !name.endsWith("_CHESTPLATE") 
            && !name.endsWith("_LEGGINGS") && !name.endsWith("_BOOTS")) {
            return;
        }

        // DEBUG
        Bukkit.getConsoleSender().sendMessage("§a[DEBUG] Rune détectée sur armure ! Application...");

        // Annuler l'action vanilla (pour pas que la rune soit juste posée dans le slot)
        event.setCancelled(true);

        // Appliquer la rune
        RuneType runeType = RuneType.FORCE; // Par défaut, à améliorer
        int level = 1; // Par défaut, à améliorer

        int result = runeItem.applyRuneOnArmor(cursor, current, runeType, level);

        if (result == -1) {
            player.sendMessage("§cCette armure a déjà une rune !");
            return;
        }

        if (result == 0) {
            player.sendMessage("§cLa rune a échoué et a été détruite !");
        } else {
            player.sendMessage("§aLa rune a été appliquée avec succès !");
        }

        // Consommer la rune (retirer du curseur)
        int amount = cursor.getAmount();
        if (amount <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(amount - 1);
            event.setCursor(cursor);
        }
    }
}
