package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TowerDamageListener implements Listener {
    
    private final Quantum plugin;

    public TowerDamageListener(Quantum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity damager)) return;
        if (!(event.getEntity() instanceof Player)) return;

        PersistentDataContainer pdc = damager.getPersistentDataContainer();
        String spawnerId = pdc.get(new NamespacedKey(plugin, "tower_spawner"), PersistentDataType.STRING);
        if (spawnerId == null) return;

        TowerSpawnerConfig config = plugin.getTowerManager().getSpawnerManager().getSpawnerConfig(spawnerId);
        if (config == null) return;

        double hearts = config.getDamageHalfHearts() / 2.0;
        event.setDamage(hearts);
    }
}
