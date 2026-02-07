package com.wynvers.quantum.armor;

import org.bukkit.Bukkit;
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
        Player player = (Player) event.getWhoClicked();
        
        // DEBUG 1 : On affiche TOUJOURS quand on clique
        player.sendMessage("§7[DEBUG] Clic détecté !");

        // Vérifier l'inventaire
        if (event.getInventory().getType() != InventoryType.PLAYER) {
            player.sendMessage("§c[DEBUG] Pas l'inventaire joueur : " + event.getInventory().getType());
            return;
        }

        // Vérifier l'item sur le curseur
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            player.sendMessage("§c[DEBUG] Curseur vide");
            return;
        }
        
        player.sendMessage("§7[DEBUG] Curseur : " + cursor.getType());

        // Vérifier si c'est une rune
        boolean isRune = runeItem.isRune(cursor);
        player.sendMessage("§7[DEBUG] Est-ce une rune ? " + isRune);
        
        if (!isRune) {
            player.sendMessage("§c[DEBUG] Ce n'est PAS une rune valide !");
            player.sendMessage("§c[DEBUG] Meta : " + (cursor.hasItemMeta() ? "OUI" : "NON"));
            if (cursor.hasItemMeta()) {
                player.sendMessage("§c[DEBUG] PDC check : " + runeItem.getSuccessChance(cursor));
            }
            return;
        }

        // Récupérer l'armure
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            player.sendMessage("§c[DEBUG] Pas d'item sous le clic");
            return;
        }

        // Vérifier si c'est une armure
        String name = current.getType().name();
        boolean isArmor = name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") 
                       || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
        
        player.sendMessage("§7[DEBUG] Item cible : " + name + " | Armure ? " + isArmor);
        
        if (!isArmor) {
            player.sendMessage("§c[DEBUG] Ce n'est pas une armure !");
            return;
        }

        // Tout est bon, on applique !
        player.sendMessage("§a[DEBUG] Application de la rune...");
        event.setCancelled(true);

        RuneType runeType = RuneType.FORCE;
        int level = 1;

        int result = runeItem.applyRuneOnArmor(cursor, current, runeType, level);

        if (result == 1) {
            player.sendMessage("§a✔ Rune appliquée !");
        } else if (result == 0) {
            player.sendMessage("§c✘ La rune a échoué !");
        } else {
            player.sendMessage("§c✘ Armure déjà runée !");
        }

        // Consommer la rune
        if (cursor.getAmount() <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(cursor.getAmount() - 1);
            event.setCursor(cursor);
        }
    }
}
