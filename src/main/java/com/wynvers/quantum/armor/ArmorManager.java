package com.wynvers.quantum.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        applyJumpEffect(player); // <- nouveau : rune AGILITY = saut
    }
    
    /**
     * Applique le bonus de vitesse en additionnant les bonus de toutes les pièces
     */
    private void applySpeedBonus(Player player, Map<RuneType, Integer> runes) {
        // Calculer le bonus total de vitesse depuis toutes les pièces d'armure
        double totalSpeedBonus = 1.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.SPEED)) {
                double pieceBonus = RuneType.SPEED.getSpeedBonus(pieceRunes.get(RuneType.SPEED));
                totalSpeedBonus += (pieceBonus - 1.0); // Ajouter le bonus (sans le multiplicateur de base 1.0)
            }
        }

        if (totalSpeedBonus > 1.0) {
            // Appliquer une vitesse en fonction du bonus
            // Minecraft: valeur entre 0 et 1, défaut 0.2
            float newSpeed = Math.min(1.0f, (float) (0.2f + (totalSpeedBonus - 1.0f) * 0.05f));
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
     * Applique le bonus de saut (rune AGILITY)
     */
    private void applyJumpEffect(Player player) {
        double jumpBonus = getJumpBonus(player);
        if (jumpBonus > 0) {
            int amplifier = (int) Math.round(jumpBonus * 5); // à ajuster selon ta courbe
            amplifier = Math.max(0, amplifier - 1); // PotionEffect amplifier commence à 0

            player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST,
                20 * 30,
                amplifier,
                true,
                false,
                false
            ));
        } else {
            // Si plus de rune AGILITY, on retire l'effet
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        }
    }
    
    /**
     * Récupère le bonus de dégâts total pour un joueur en additionnant tous les bonus de chaque pièce
     */
    public double getDamageBonus(Player player) {
        if (!dungeonArmor.hasCompleteArmor(player)) {
            return 1.0;
        }

        double totalBonus = 1.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.FORCE)) {
                double pieceBonus = RuneType.FORCE.getDamageBonus(pieceRunes.get(RuneType.FORCE));
                totalBonus += (pieceBonus - 1.0); // Ajouter le bonus (sans le multiplicateur de base 1.0)
            }
        }

        return totalBonus;
    }
    
    /**
     * Récupère la réduction de dégâts pour un joueur en additionnant tous les bonus de chaque pièce
     */
    public double getDamageReduction(Player player) {
        if (!dungeonArmor.hasCompleteArmor(player)) {
            return 0.0;
        }

        double totalReduction = 0.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.RESISTANCE)) {
                totalReduction += RuneType.RESISTANCE.getDamageReduction(pieceRunes.get(RuneType.RESISTANCE));
            }
        }

        return totalReduction;
    }
    
    /**
     * Récupère la chance de critique pour un joueur en additionnant tous les bonus de chaque pièce
     */
    public double getCriticalChance(Player player) {
        if (!dungeonArmor.hasCompleteArmor(player)) {
            return 0.0;
        }

        double totalChance = 0.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.CRITICAL)) {
                totalChance += RuneType.CRITICAL.getCriticalChance(pieceRunes.get(RuneType.CRITICAL));
            }
        }

        return totalChance;
    }
    
    /**
     * Récupère le pourcentage de vampirisme pour un joueur en additionnant tous les bonus de chaque pièce
     */
    public double getVampirismPercent(Player player) {
        if (!dungeonArmor.hasCompleteArmor(player)) {
            return 0.0;
        }

        double totalVampirism = 0.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.VAMPIRISM)) {
                totalVampirism += RuneType.VAMPIRISM.getVampirismPercent(pieceRunes.get(RuneType.VAMPIRISM));
            }
        }

        return totalVampirism;
    }

    /**
     * Récupère le bonus de saut (AGILITY) pour un joueur en additionnant tous les bonus de chaque pièce
     */
    public double getJumpBonus(Player player) {
        if (!dungeonArmor.hasCompleteArmor(player)) {
            return 0.0;
        }

        double totalJump = 0.0;
        ItemStack[] armorPieces = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armorPieces) {
            Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
            if (pieceRunes.containsKey(RuneType.AGILITY)) {
                totalJump += RuneType.AGILITY.getJumpBonus(pieceRunes.get(RuneType.AGILITY));
            }
        }

        return totalJump;
    }
    
    /**
     * Timer principal pour les effets passifs (régén, etc)
     * Optimisé pour exécuter toutes les 20 ticks (1 seconde) au lieu de chaque tick
     */
    private void startRegenTickTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!dungeonArmor.hasCompleteArmor(player)) {
                        continue;
                    }

                    // Calculer la régénération totale de toutes les pièces d'armure
                    double totalRegenPerSecond = 0.0;
                    ItemStack[] armorPieces = {
                        player.getInventory().getHelmet(),
                        player.getInventory().getChestplate(),
                        player.getInventory().getLeggings(),
                        player.getInventory().getBoots()
                    };

                    for (ItemStack piece : armorPieces) {
                        Map<RuneType, Integer> pieceRunes = dungeonArmor.getAppliedRunesWithLevels(piece);
                        if (pieceRunes.containsKey(RuneType.REGENERATION)) {
                            int level = pieceRunes.get(RuneType.REGENERATION);
                            totalRegenPerSecond += RuneType.REGENERATION.getRegeneration(level);
                        }
                    }

                    if (totalRegenPerSecond > 0) {
                        // Appliquer la régénération directement (tâche exécutée toutes les secondes)
                        double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + totalRegenPerSecond);
                        player.setHealth(newHealth);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Exécuter toutes les 20 ticks (1 seconde) au lieu de chaque tick
    }
    
    /**
     * Efface les bonus d'armure pour un joueur
     */
    public void clearArmorBonuses(Player player) {
        playerRunes.remove(player);
        player.setWalkSpeed(0.2f); // Vitesse par défaut
        player.removePotionEffect(PotionEffectType.JUMP_BOOST); // on retire aussi le jump
    }
}
