package com.wynvers.quantum.listeners;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.armor.RuneItem;
import com.wynvers.quantum.armor.RuneType;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Listener pour gérer le drag-n-drop de runes sur les armures de donjon
 */
public class ArmorGUIListener implements Listener {

    private final Quantum plugin;
    private final RuneItem runeItem;
    private final DungeonArmor dungeonArmor;
    private final Random random = new Random();

    public ArmorGUIListener(Quantum plugin) {
        this.plugin = plugin;
        this.runeItem = new RuneItem(plugin);
        this.dungeonArmor = plugin.getDungeonArmor();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        // Vérifier si le joueur fait un drag-n-drop
        if (cursor == null || clicked == null) return;

        // Vérifier si c'est une rune qu'on déplace
        if (!runeItem.isRune(cursor)) return;

        // Vérifier si on la dépose sur une pièce d'armure de donjon
        if (!dungeonArmor.isDungeonArmor(clicked)) return;

        // Annuler l'événement normal
        event.setCancelled(true);

        // Récupérer le taux de réussite de la rune
        int successChance = runeItem.getSuccessChance(cursor);
        int roll = random.nextInt(101); // 0-100

        boolean success = roll <= successChance;

        if (success) {
            // Appliquer la rune sur l'armure
            // TODO: Détecter le type et niveau de rune depuis le NBT
            // Pour l'instant, message temporaire
            player.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.BOLD + "RÉUSSITE !" + ChatColor.RESET + ChatColor.GREEN + " La rune a été appliquée ! (" + successChance + "% de chance)");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            
            // Supprimer la rune du curseur
            player.setItemOnCursor(null);
        } else {
            // Échec - la rune est détruite
            player.sendMessage(ChatColor.RED + "✖ " + ChatColor.BOLD + "ÉCHEC !" + ChatColor.RESET + ChatColor.RED + " La rune a été détruite... (" + successChance + "% de chance)");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
            
            // Supprimer la rune du curseur
            player.setItemOnCursor(null);
        }
    }
}
