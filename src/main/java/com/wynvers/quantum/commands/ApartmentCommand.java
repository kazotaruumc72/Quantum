package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.apartment.Apartment;
import com.wynvers.quantum.apartment.ApartmentDoorManager;
import com.wynvers.quantum.apartment.ApartmentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Apartment command handler - Full implementation
 *
 * /apartment - View apartment info
 * /apartment create <name> [small|medium|large] - Create apartment
 * /apartment upgrade - Upgrade apartment tier
 * /apartment invite <player> - Invite visitor
 * /apartment remove <player> - Remove visitor
 * /apartment lock/unlock - Toggle lock
 * /apartment tp - Teleport to apartment
 * /apartment contrat adddeadline - Open deadline menu
 * /apartment catalogue - Open personal furniture catalogue
 */
public class ApartmentCommand implements CommandExecutor {

    private final Quantum plugin;
    private final ApartmentManager apartmentManager;
    private final ApartmentDoorManager doorManager;

    public ApartmentCommand(Quantum plugin, ApartmentManager apartmentManager, ApartmentDoorManager doorManager) {
        this.plugin = plugin;
        this.apartmentManager = apartmentManager;
        this.doorManager = doorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être utilisée que par un joueur.");
            return true;
        }

        if (args.length == 0) {
            showInfo(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(player, args);
            case "upgrade" -> handleUpgrade(player);
            case "invite" -> handleInvite(player, args);
            case "remove" -> handleRemove(player, args);
            case "lock" -> handleLock(player, true);
            case "unlock" -> handleLock(player, false);
            case "tp", "teleport" -> handleTeleport(player);
            case "contrat" -> handleContract(player, args);
            case "catalogue" -> handleCatalogue(player);
            case "door", "porte" -> handleDoor(player, args);
            default -> player.sendMessage("§cCommande inconnue. Utilisez §f/apartment §cpour l'aide.");
        }

        return true;
    }

    private void showInfo(Player player) {
        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());

        player.sendMessage("§6§l=== Système d'Appartements ===");

        if (apt == null) {
            player.sendMessage("§7Vous n'avez pas d'appartement.");
            player.sendMessage("§7Utilisez §f/apartment create <nom> §7pour en créer un.");
        } else {
            player.sendMessage("§7Nom: §f" + apt.getApartmentName());
            player.sendMessage("§7Taille: §f" + apt.getSize().getDisplayName());
            player.sendMessage("§7Zone: §f" + (apt.getZoneName().isEmpty() ? "Non définie" : apt.getZoneName()));
            player.sendMessage("§7Tier: §f" + apt.getTier());
            player.sendMessage("§7Contrat: §f" + apartmentManager.getFormattedDeadline(apt));
            player.sendMessage("§7Mobilier: §f" + apt.getFurniture().size() + " pièces");
            player.sendMessage("§7Verrouillé: §f" + (apt.isLocked() ? "§cOui" : "§aNon"));
        }

        player.sendMessage("");
        player.sendMessage("§8Commandes disponibles:");
        player.sendMessage("§8- §f/apartment create <nom> [petit|moyen|grand]");
        player.sendMessage("§8- §f/apartment contrat adddeadline");
        player.sendMessage("§8- §f/apartment catalogue");
        player.sendMessage("§8- §f/apartment tp §7- Téléportation");
        player.sendMessage("§8- §f/apartment invite/remove <joueur>");
        player.sendMessage("§8- §f/apartment lock/unlock");
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("quantum.apartment.create")) {
            player.sendMessage("§cVous n'avez pas la permission de créer un appartement.");
            return;
        }

        if (apartmentManager.hasApartment(player.getUniqueId())) {
            player.sendMessage("§cVous avez déjà un appartement !");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUtilisation: §f/apartment create <nom> [petit|moyen|grand]");
            return;
        }

        String name = args[1];

        // Validate apartment name (alphanumeric + underscore, max 32 chars)
        if (name.length() > 32 || !name.matches("[a-zA-Z0-9_-]+")) {
            player.sendMessage("§cNom invalide. Utilisez uniquement des lettres, chiffres, - et _ (max 32 caractères).");
            return;
        }

        Apartment.Size size = Apartment.Size.SMALL;

        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "moyen", "medium" -> size = Apartment.Size.MEDIUM;
                case "grand", "large" -> size = Apartment.Size.LARGE;
            }
        }

        Apartment apt = apartmentManager.createApartment(player.getUniqueId(), name, size, "");
        if (apt != null) {
            player.sendMessage("§a✔ Appartement §f" + name + " §acréé ! (Taille: " + size.getDisplayName() + ")");
            player.sendMessage("§7Contrat initial: 30 jours");
            player.sendMessage("§7Utilisez §f/appart contrat adddeadline §7pour prolonger.");
        } else {
            player.sendMessage("§cErreur lors de la création de l'appartement.");
        }
    }

    private void handleUpgrade(Player player) {
        if (!player.hasPermission("quantum.apartment.upgrade")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return;
        }

        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());
        if (apt == null) {
            player.sendMessage("§cVous n'avez pas d'appartement.");
            return;
        }

        apt.setTier(apt.getTier() + 1);
        apartmentManager.saveApartment(apt);
        player.sendMessage("§a✔ Appartement amélioré au tier §f" + apt.getTier() + "§a !");
    }

    private void handleInvite(Player player, String[] args) {
        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());
        if (apt == null) {
            player.sendMessage("§cVous n'avez pas d'appartement.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUtilisation: §f/apartment invite <joueur>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        if (apartmentManager.addVisitor(apt.getApartmentId(), target.getUniqueId(), false)) {
            player.sendMessage("§a✔ §f" + target.getName() + " §aa été invité dans votre appartement.");
            target.sendMessage("§a✔ Vous avez été invité dans l'appartement de §f" + player.getName() + "§a !");
        } else {
            player.sendMessage("§cErreur lors de l'invitation.");
        }
    }

    private void handleRemove(Player player, String[] args) {
        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());
        if (apt == null) {
            player.sendMessage("§cVous n'avez pas d'appartement.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUtilisation: §f/apartment remove <joueur>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        if (apartmentManager.removeVisitor(apt.getApartmentId(), target.getUniqueId())) {
            player.sendMessage("§a✔ §f" + target.getName() + " §aa été retiré de votre appartement.");
        } else {
            player.sendMessage("§cErreur lors du retrait.");
        }
    }

    private void handleLock(Player player, boolean lock) {
        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());
        if (apt == null) {
            player.sendMessage("§cVous n'avez pas d'appartement.");
            return;
        }

        apt.setLocked(lock);
        apartmentManager.saveApartment(apt);
        player.sendMessage(lock ? "§c🔒 Appartement verrouillé." : "§a🔓 Appartement déverrouillé.");
    }

    private void handleTeleport(Player player) {
        Apartment apt = apartmentManager.getPlayerApartment(player.getUniqueId());
        if (apt == null) {
            player.sendMessage("§cVous n'avez pas d'appartement.");
            return;
        }

        if (apt.getWorldName() == null || apt.getWorldName().isEmpty()) {
            player.sendMessage("§cVotre appartement n'a pas de point de téléportation défini.");
            return;
        }

        var world = Bukkit.getWorld(apt.getWorldName());
        if (world == null) {
            player.sendMessage("§cLe monde de votre appartement est indisponible.");
            return;
        }

        org.bukkit.Location loc = new org.bukkit.Location(world, apt.getX(), apt.getY(), apt.getZ(), apt.getYaw(), apt.getPitch());
        player.teleport(loc);
        player.sendMessage("§a✔ Téléporté à votre appartement !");
    }

    private void handleContract(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: §f/apartment contrat adddeadline");
            return;
        }

        if (args[1].equalsIgnoreCase("adddeadline")) {
            // Open the deadline_adder menu
            if (plugin.getMenuManager() != null) {
                plugin.getMenuManager().openMenu(player, "deadline_adder");
            } else {
                player.sendMessage("§cLe système de menus n'est pas disponible.");
            }
        } else {
            player.sendMessage("§cSous-commande inconnue. Utilisez: §f/apartment contrat adddeadline");
        }
    }

    private void handleCatalogue(Player player) {
        // Open personal catalogue menu
        if (plugin.getMenuManager() != null) {
            plugin.getMenuManager().openMenu(player, "personnal_catalogue");
        } else {
            player.sendMessage("§cLe système de menus n'est pas disponible.");
        }
    }

    // ──────── DOOR ────────

    private void handleDoor(Player player, String[] args) {
        if (!player.hasPermission("quantum.apartment.door.admin")) {
            player.sendMessage("§cVous n'avez pas la permission de gérer les portes d'appartement.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUtilisation:");
            player.sendMessage("§f/apartment door wand §7- Obtenir la hache de sélection");
            player.sendMessage("§f/apartment door set <id> §7- Enregistrer la porte");
            player.sendMessage("§f/apartment door delete <id> §7- Supprimer la porte");
            player.sendMessage("§f/apartment door open <id> §7- Ouvrir la porte manuellement");
            return;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "wand" -> {
                player.getInventory().addItem(ApartmentDoorManager.createWand());
                player.sendMessage("§b§l[AptDoor] §7Hache de sélection ajoutée à votre inventaire.");
            }
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUtilisation: §f/apartment door set <id>");
                    return;
                }
                int aptId = parseApartmentId(player, args[2]);
                if (aptId < 0) return;
                doorManager.createDoor(player, aptId);
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUtilisation: §f/apartment door delete <id>");
                    return;
                }
                int aptId = parseApartmentId(player, args[2]);
                if (aptId < 0) return;
                if (doorManager.deleteDoor(aptId)) {
                    player.sendMessage("§a§l[AptDoor] §aPorte supprimée pour l'appartement §f" + aptId + "§a.");
                } else {
                    player.sendMessage("§cAucune porte configurée pour l'appartement §f" + aptId + "§c.");
                }
            }
            case "open" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUtilisation: §f/apartment door open <id>");
                    return;
                }
                int aptId = parseApartmentId(player, args[2]);
                if (aptId < 0) return;
                if (doorManager.getDoorConfig(aptId) == null) {
                    player.sendMessage("§cAucune porte configurée pour l'appartement §f" + aptId + "§c.");
                    return;
                }
                doorManager.openDoor(aptId, player);
                player.sendMessage("§a§l[AptDoor] §aPorte ouverte pour l'appartement §f" + aptId + "§a.");
            }
            default -> player.sendMessage("§cSous-commande inconnue. Utilisez §f/apartment door §cpour l'aide.");
        }
    }

    /**
     * Parses an apartment id from a string and sends an error message on failure.
     *
     * @return the parsed id, or -1 on failure
     */
    private int parseApartmentId(Player player, String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            player.sendMessage("§cL'identifiant d'appartement doit être un nombre entier.");
            return -1;
        }
    }
}
