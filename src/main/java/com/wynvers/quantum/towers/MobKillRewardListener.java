package com.wynvers.quantum.towers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.armor.DungeonArmor;
import com.wynvers.quantum.weapon.DungeonWeapon;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Distribue l'XP aux joueurs, pièces d'armure de donjon et arme de donjon
 * lorsqu'un mob est tué, en se basant sur la config mobs.yml (bestiaire).
 */
public class MobKillRewardListener implements Listener {

    private final Quantum plugin;
    private final MobConfig mobConfig;

    public MobKillRewardListener(Quantum plugin, MobConfig mobConfig) {
        this.plugin = plugin;
        this.mobConfig = mobConfig;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        // Déterminer le type de mob et la récompense
        MobConfig.MobReward reward = resolveReward(entity);

        // 1) XP joueur (système de niveaux Quantum)
        int playerExp = reward.getPlayerExp();
        if (playerExp > 0 && plugin.getPlayerLevelManager() != null) {
            plugin.getPlayerLevelManager().addExp(killer.getUniqueId(), playerExp);
        }

        // 2) XP armure de donjon (chaque pièce équipée)
        DungeonArmor dungeonArmor = plugin.getDungeonArmor();
        if (dungeonArmor != null) {
            applyArmorExp(killer, dungeonArmor, reward, "helmet",     killer.getInventory().getHelmet());
            applyArmorExp(killer, dungeonArmor, reward, "chestplate", killer.getInventory().getChestplate());
            applyArmorExp(killer, dungeonArmor, reward, "leggings",   killer.getInventory().getLeggings());
            applyArmorExp(killer, dungeonArmor, reward, "boots",      killer.getInventory().getBoots());
        }

        // 3) XP arme de donjon (arme en main)
        DungeonWeapon dungeonWeapon = plugin.getDungeonWeapon();
        if (dungeonWeapon != null) {
            ItemStack mainHand = killer.getInventory().getItemInMainHand();
            if (dungeonWeapon.isDungeonWeapon(mainHand)) {
                int weaponExp = reward.getWeaponExp();
                for (int i = 0; i < weaponExp; i++) {
                    dungeonArmor.addKillExperience(mainHand);
                }
            }
        }
    }

    /**
     * Détermine la récompense pour le mob tué (MythicMobs ou vanilla).
     */
    private MobConfig.MobReward resolveReward(LivingEntity entity) {
        // Vérifier MythicMobs
        try {
            var activeMob = MythicBukkit.inst().getMobManager()
                    .getActiveMob(entity.getUniqueId()).orElse(null);
            if (activeMob != null) {
                return mobConfig.getMythicReward(activeMob.getMobType());
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
            // MythicMobs pas disponible – traiter comme vanilla
        }

        // Mob vanilla
        return mobConfig.getReward(entity.getType().name());
    }

    /**
     * Applique l'XP d'armure à une pièce d'armure de donjon équipée.
     */
    private void applyArmorExp(Player player, DungeonArmor dungeonArmor,
                               MobConfig.MobReward reward, String piece, ItemStack armor) {
        if (armor == null || !dungeonArmor.isDungeonArmor(armor)) return;

        int armorExp = reward.getArmorExp(piece);
        for (int i = 0; i < armorExp; i++) {
            dungeonArmor.addKillExperience(armor);
        }
    }
}
