package com.wynvers.quantum.jobs;

import com.wynvers.quantum.Quantum;
import io.github.pigaut.orestack.api.event.GeneratorHarvestEvent;
import io.github.pigaut.orestack.api.event.GeneratorMineEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Écoute les événements Orestack pour le système de métiers
 * Permet aux joueurs de gagner de l'XP en interagissant avec les générateurs Orestack
 */
public class OrestackJobListener implements Listener {

    private final Quantum plugin;
    private final JobManager jobManager;

    public OrestackJobListener(Quantum plugin, JobManager jobManager) {
        this.plugin = plugin;
        this.jobManager = jobManager;
    }

    /**
     * Gère les événements de récolte (clic droit) sur les générateurs Orestack
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGeneratorHarvest(GeneratorHarvestEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Traiter l'interaction avec le générateur
        // Utiliser "generator" comme ID car tous ces événements sont pour des générateurs
        jobManager.handleOrestackStructure(player, "generator");
    }

    /**
     * Gère les événements de minage de générateurs Orestack
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGeneratorMine(GeneratorMineEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Traiter l'interaction avec le générateur
        // Utiliser "generator" comme ID car tous ces événements sont pour des générateurs
        jobManager.handleOrestackStructure(player, "generator");
    }
}
