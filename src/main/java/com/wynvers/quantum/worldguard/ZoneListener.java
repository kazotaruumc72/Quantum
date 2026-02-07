package com.wynvers.quantum.worldguard;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.towers.TowerScoreboardHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * ZoneListener simplifié.
 *
 * L'ancien système de zones à kills a été retiré.
 * Le nouveau ZoneManager gère directement les entrées/sorties de tours.
 *
 * Ce listener ne s'occupe plus que de nettoyer correctement
 * le scoreboard de tour quand un joueur se déconnecte.
 */
public class ZoneListener implements Listener {

    private final Quantum plugin;
    private final TowerScoreboardHandler scoreboardHandler;

    public ZoneListener(Quantum plugin) {
        this.plugin = plugin;
        this.scoreboardHandler = plugin.getTowerScoreboardHandler();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (scoreboardHandler != null && scoreboardHandler.hasTowerScoreboard(player)) {
            scoreboardHandler.disableTowerScoreboard(player);
        }
    }
}
