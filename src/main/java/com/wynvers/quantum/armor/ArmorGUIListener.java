package com.wynvers.quantum.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorGUIListener implements Listener {
    
    private final ArmorGUI gui;
    private final Map<String, String> playerArmorSlot = new HashMap<>();
    private final Map<String, RuneType> playerSelectedRune = new HashMap<>();
    
    public ArmorGUIListener(ArmorGUI gui) {
        this.gui = gui;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // GUI principal
        if (title.contains("§6§l⚔ ARMURE DE DONJON ⚔")) {
            event.setCancelled(true);
            handleMainGUIClick(player, event.getSlot());
            return;
        }
        
        // GUI sélection runes
        if (title.contains("§d§l⚔ RUNES -")) {
            event.setCancelled(true);
            handleRuneSelectionClick(player, event.getSlot(), title);
            return;
        }
        
        // GUI sélection niveau
        if (title.contains("- NIVEAU")) {
            event.setCancelled(true);
            handleRuneLevelClick(player, event.getSlot());
            return;
        }
    }
    
    private void handleMainGUIClick(Player player, int slot) {
        String armorSlot = ArmorGUI.getArmorSlotFromPosition(slot);
        
        if (armorSlot != null) {
            gui.openRuneSelectionGUI(player, armorSlot);
            playerArmorSlot.put(player.getName(), armorSlot);
        }
    }
    
    private void handleRuneSelectionClick(Player player, int slot, String title) {
        // Bouton retour
        if (slot == 49) {
            gui.openMainGUI(player);
            return;
        }
        
        // Récupérer la rune cliquée
        RuneType rune = ArmorGUI.getRuneFromSlot(slot);
        if (rune == null) return;
        
        String armorSlot = playerArmorSlot.get(player.getName());
        if (armorSlot == null) return;
        
        // Sauvegarder la rune sélectionnée
        playerSelectedRune.put(player.getName(), rune);
        
        // Ouvrir le GUI de sélection de niveau
        gui.openRuneLevelGUI(player, armorSlot, rune);
    }
    
    private void handleRuneLevelClick(Player player, int slot) {
        // Bouton retour
        if (slot == 22) {
            String armorSlot = playerArmorSlot.get(player.getName());
            if (armorSlot != null) {
                gui.openRuneSelectionGUI(player, armorSlot);
            }
            return;
        }
        
        // Sélection du niveau
        int level = -1;
        if (slot == 11) level = 1;
        else if (slot == 13) level = 2;
        else if (slot == 15) level = 3;
        
        if (level == -1) return;
        
        String armorSlot = playerArmorSlot.get(player.getName());
        RuneType rune = playerSelectedRune.get(player.getName());
        
        if (armorSlot != null && rune != null) {
            player.closeInventory();
            gui.applyRune(player, armorSlot, rune, level);
        }
    }
}
