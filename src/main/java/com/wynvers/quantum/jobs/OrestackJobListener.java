package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import io.github.pigaut.orestack.api.event.OrestackBreakEvent;
import io.github.pigaut.orestack.api.event.OrestackPlaceEvent;
import io.github.pigaut.orestack.api.event.OrestackRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Écoute les événements Orestack pour le système de métiers
 * Permet aux joueurs de gagner de l'XP en interagissant avec les structures Orestack
 */
public class OrestackJobListener implements Listener {

    private final Quantum plugin;
    private final JobManager jobManager;

    public OrestackJobListener(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }

    /**
     * Gère les événements de clic droit sur les structures Orestack
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOrestackRightClick(OrestackRightClickEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Récupérer l'ID de la structure Orestack
        String structureId = event.getStructure().getStructureId();
        if (structureId == null) return;

        // Traiter l'interaction avec la structure
        jobManager.handleOrestackStructure(player, structureId);
    }

    /**
     * Gère les événements de casse de structures Orestack
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOrestackBreak(OrestackBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Récupérer l'ID de la structure Orestack
        String structureId = event.getStructure().getStructureId();
        if (structureId == null) return;

        // Traiter l'interaction avec la structure
        jobManager.handleOrestackStructure(player, structureId);
    }

    /**
     * Gère les événements de placement de structures Orestack
     * (optionnel - si certains jobs permettent de placer des structures)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOrestackPlace(OrestackPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Récupérer l'ID de la structure Orestack
        String structureId = event.getStructure().getStructureId();
        if (structureId == null) return;

        // Traiter l'interaction avec la structure
        // Note: Vous pouvez ajouter une logique différente pour le placement si nécessaire
        jobManager.handleOrestackStructure(player, structureId);
    }
}
