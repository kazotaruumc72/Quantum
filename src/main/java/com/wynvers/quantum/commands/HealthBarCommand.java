package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.healthbar.HealthBarManager;
import com.wynvers.quantum.healthbar.HealthBarMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /healthbar pour changer le mode d'affichage de vie des mobs
 */
public class HealthBarCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final HealthBarManager healthBarManager;
    
    public HealthBarCommand(Quantum plugin, HealthBarManager healthBarManager) {
        this.plugin = plugin;
        this.healthBarManager = healthBarManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Sans argument, afficher l'aide
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String modeArg = args[0].toLowerCase();
        
        switch (modeArg) {
            case "pourcentage":
            case "percentage":
            case "percent":
            case "%":
                healthBarManager.setMode(player, HealthBarMode.PERCENTAGE);
                player.sendMessage("§a§l[HealthBar] §aMode changé : §fPourcentage");
                player.sendMessage("§7Les barres de vie s'afficheront en pourcentage.");
                updateNearbyMobs(player);
                break;
                
            case "coeurs":
            case "coeur":
            case "hearts":
            case "heart":
            case "❤":
                healthBarManager.setMode(player, HealthBarMode.HEARTS);
                player.sendMessage("§a§l[HealthBar] §aMode changé : §fCœurs ❤");
                player.sendMessage("§7Les barres de vie s'afficheront en cœurs.");
                updateNearbyMobs(player);
                break;
                
            case "info":
            case "status":
                HealthBarMode currentMode = healthBarManager.getMode(player);
                player.sendMessage("§6§m══════════════════════════════");
                player.sendMessage("§6§lHealthBar - Statut");
                player.sendMessage("");
                player.sendMessage("§7Mode actuel : §f" + 
                    (currentMode == HealthBarMode.HEARTS ? "Cœurs ❤" : "Pourcentage %"));
                player.sendMessage("");
                player.sendMessage("§7Utilisez §e/healthbar <mode> §7pour changer.");
                player.sendMessage("§6§m══════════════════════════════");
                break;
                
            default:
                player.sendMessage("§c§l[HealthBar] §cMode inconnu : " + modeArg);
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Affiche l'aide de la commande
     */
    private void sendHelp(Player player) {
        HealthBarMode currentMode = healthBarManager.getMode(player);
        
        player.sendMessage("§6§m══════════════════════════════");
        player.sendMessage("§6§lHealthBar - Affichage de vie des mobs");
        player.sendMessage("");
        player.sendMessage("§7Mode actuel : §f" + 
            (currentMode == HealthBarMode.HEARTS ? "Cœurs ❤" : "Pourcentage %"));
        player.sendMessage("");
        player.sendMessage("§eCommandes disponibles :");
        player.sendMessage("§7/healthbar pourcentage §8- Afficher en %");
        player.sendMessage("§7/healthbar coeurs §8- Afficher en cœurs ❤");
        player.sendMessage("§7/healthbar info §8- Voir le mode actuel");
        player.sendMessage("");
        player.sendMessage("§8Exemples :");
        player.sendMessage("§8● Pourcentage : §a[||||||||||||||||||||] 100%");
        player.sendMessage("§8● Cœurs : §c❤❤❤❤❤§7♡♡♡♡♡");
        player.sendMessage("§6§m══════════════════════════════");
    }
    
    /**
     * Met à jour l'affichage de tous les mobs proches après changement de mode
     */
    private void updateNearbyMobs(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.getWorld().getLivingEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> entity.getLocation().distance(player.getLocation()) <= 50)
                .forEach(entity -> healthBarManager.updateMobHealthDisplay(entity, player));
        }, 2L);
    }
}
