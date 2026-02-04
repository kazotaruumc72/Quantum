package com.wynvers.quantum.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gére l'armure de donjon, l'application des runes et les bonus
 * - Applique les bonus des runes aux joueurs
 * - Gère la régénération et les effets passifs
 * - Synchronise les bonuses avec l'armure équipée
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ArmorManager {
    
    private final JavaPlugin plugin;
    private final DungeonArmor dungeonArmor;
    private final Map<Player, Map<RuneType, Integer>> playerRunes = new WeakHashMap<>();
    private final Map<Player, Integer> playerRegenTicks = new WeakHashMap<>();
    
    public ArmorManager(JavaPlugin plugin, DungeonArmor dungeonArmor) {
        this.plugin = plugin;
        this.dungeonArmor = dungeonArmor;
        startRegenTickTimer();
    }
    
    /**
     * Applique les bonus des runes au joueur (vitesse, régén, etc)
     */
    public void applyArmorBonuses(Player player) {
        // Vérifier si le joueur a l'armure complète
        if (!dungeonArmor.hasCompleteArmor(player)) {
            clearArmorBonuses(player);
            return;
        }
        
        // Fusionner les runes de toutes les pièces
        Map<RuneType, Integer> combinedRunes = new HashMap<>();
        ItemStack[] armor = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };
        
        for (ItemStack piece : armor) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            for (Map.Entry<RuneType, Integer> entry : pieceRunes.entrySet()) {
                // Prendre le niveau le plus élevé si la rune apparaît plusieurs fois
                combinedRunes.put(entry.getKey(), Math.max(
                    combinedRunes.getOrDefault(entry.getKey(), 0),
                    entry.getValue()
                ));
            }
        }
        
        playerRunes.put(player, combinedRunes);
        
        // Appliquer les bonus
        applySpeedBonus(player, combinedRunes);
        applyResistanceBonus(player, combinedRunes);
        initializeRegenTicks(player);
    }
    
    /**
     * Applique le bonus de vitesse
     */
    private void applySpeedBonus(Player player, Map<RuneType, Integer> runes) {
        if (runes.containsKey(RuneType.SPEED)) {
            int level = runes.get(RuneType.SPEED);
            double speedBonus = RuneType.SPEED.getSpeedBonus(level);
            
            // Appliquer une vitesse en fonction du bonus
            // Minecraft: valeur entre 0 et 1, défaut 0.1
            float newSpeed = Math.min(1.0f, (float) ((speedBonus - 1.0) * 0.1f + 0.1f));
            player.setWalkSpeed(newSpeed);
        }
    }
    
    /**
     * Applique le bonus de résistance
     */
    private void applyResistanceBonus(Player player, Map<RuneType, Integer> runes) {
        // Note: Les dommages sont appliqués dans le listener de dégâts
        // Ici on stocke juste le bonus pour accès rapide
        if (runes.containsKey(RuneType.RESISTANCE)) {
            int level = runes.get(RuneType.RESISTANCE);
            // Le bonus est récupéré via getDamageReduction() quand nécessaire
        }
    }
    
    /**
     * Récupère le bonus de dégâts total pour un joueur
     */
    public double getDamageBonus(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.FORCE)) {
            return RuneType.FORCE.getDamageBonus(runes.get(RuneType.FORCE));
        }
        return 1.0;
    }
    
    /**
     * Récupère la réduction de dégâts pour un joueur
     */
    public double getDamageReduction(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.RESISTANCE)) {
            return RuneType.RESISTANCE.getDamageReduction(runes.get(RuneType.RESISTANCE));
        }
        return 0.0;
    }
    
    /**
     * Récupère la chance de critique pour un joueur
     */
    public double getCriticalChance(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.CRITICAL)) {
            return RuneType.CRITICAL.getCriticalChance(runes.get(RuneType.CRITICAL));
        }
        return 0.0;
    }
    
    /**
     * Récupère le pourcentage de vampirisme pour un joueur
     */
    public double getVampirismPercent(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.VAMPIRISM)) {
            return RuneType.VAMPIRISM.getVampirismPercent(runes.get(RuneType.VAMPIRISM));
        }
        return 0.0;
    }
    
    /**
     * Récupère le pourcentage de retour de dégâts (thorns) pour un joueur
     */
    public double getThornsPercent(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.THORNS)) {
            return RuneType.THORNS.getThornsPercent(runes.get(RuneType.THORNS));
        }
        return 0.0;
    }
    
    /**
     * Récupère le multiplicateur d'XP pour un joueur
     */
    public double getXpMultiplier(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.WISDOM)) {
            return RuneType.WISDOM.getXpMultiplier(runes.get(RuneType.WISDOM));
        }
        return 1.0;
    }
    
    /**
     * Récupère la chance de loot rare pour un joueur
     */
    public double getRareLootChance(Player player) {
        Map<RuneType, Integer> runes = playerRunes.getOrDefault(player, new HashMap<>());
        if (runes.containsKey(RuneType.LUCK)) {
            return RuneType.LUCK.getRareLootChance(runes.get(RuneType.LUCK));
        }
        return 0.0;
    }
    
    /**
     * Initialise le compteur de ticks pour la régén
     */
    private void initializeRegenTicks(Player player) {
        playerRegenTicks.put(player, 0);
    }
    
    /**
     * Timer principal pour les effets passifs (régén, etc)
     */
    private void startRegenTickTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Map<RuneType, Integer> runes = playerRunes.get(player);
                    if (runes == null || runes.isEmpty()) {
                        continue;
                    }
                    
                    // Vérifier si le joueur a la régénération
                    if (runes.containsKey(RuneType.REGENERATION)) {
                        int level = runes.get(RuneType.REGENERATION);
                        double regenPerSecond = RuneType.REGENERATION.getRegeneration(level);
                        
                        // Appliquer la régénération tous les 20 ticks (1 seconde)
                        int ticks = playerRegenTicks.getOrDefault(player, 0);
                        if (ticks >= 20) {
                            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + regenPerSecond);
                            player.setHealth(newHealth);
                            playerRegenTicks.put(player, 0);
                        } else {
                            playerRegenTicks.put(player, ticks + 1);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    /**
     * Efface les bonus d'armure pour un joueur
     */
    public void clearArmorBonuses(Player player) {
        playerRunes.remove(player);
        playerRegenTicks.remove(player);
        player.setWalkSpeed(0.2f); // Vitesse par défaut
    }
}
