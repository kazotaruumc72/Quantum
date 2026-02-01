package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Commande admin pour gérer les catégories d'ordres
 * /quantum orders button createcategorie <categorie>
 * /quantum orders button deletecategorie <categorie>
 */
public class OrdersAdminCommand implements CommandExecutor {
    private final Quantum plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public OrdersAdminCommand(Quantum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Vérifier la permission admin
        if (!sender.hasPermission("quantum.admin.orders")) {
            sender.sendMessage(mm.deserialize("<red>Vous n'avez pas la permission d'utiliser cette commande."));
            return true;
        }

        // /quantum orders button createcategorie <categorie>
        // /quantum orders button deletecategorie <categorie>
        if (args.length < 4) {
            sendUsage(sender);
            return true;
        }

        if (!args[0].equalsIgnoreCase("orders") || !args[1].equalsIgnoreCase("button")) {
            return false;
        }

        String action = args[2].toLowerCase();
        String categorie = args[3].toLowerCase();

        switch (action) {
            case "createcategorie":
                createCategorie(sender, categorie);
                break;
            case "deletecategorie":
                deleteCategorie(sender, categorie);
                break;
            default:
                sendUsage(sender);
        }

        return true;
    }

    private void createCategorie(CommandSender sender, String categorie) {
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        sender.sendMessage(mm.deserialize(
            "<#32b8c6>➤ Création de la catégorie <white>" + categorie + "</white>"
        ));
        sender.sendMessage("");

        // 1. Vérifier si la catégorie existe déjà
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            sender.sendMessage(mm.deserialize("<red>✗ Fichier orders_template.yml introuvable!"));
            return;
        }

        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        if (template.contains(categorie)) {
            sender.sendMessage(mm.deserialize("<red>✗ La catégorie <white>" + categorie + "</white> existe déjà!"));
            sender.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            return;
        }

        // 2. Ajouter la catégorie dans orders_template.yml
        List<String> defaultItems = new ArrayList<>();
        defaultItems.add("minecraft:stone");
        defaultItems.add("minecraft:dirt");
        template.set(categorie + ".items", defaultItems);

        try {
            template.save(templateFile);
            sender.sendMessage(mm.deserialize("<green>✓ Catégorie ajoutée dans orders_template.yml"));
        } catch (IOException e) {
            sender.sendMessage(mm.deserialize("<red>✗ Erreur lors de la sauvegarde du template!"));
            e.printStackTrace();
            return;
        }

        // 3. Créer le fichier menu orders_<categorie>.yml
        File menuFile = new File(plugin.getDataFolder(), "menus/orders_" + categorie + ".yml");
        if (menuFile.exists()) {
            sender.sendMessage(mm.deserialize("<yellow>⚠ Le menu orders_" + categorie + ".yml existe déjà"));
        } else {
            createMenuFile(menuFile, categorie);
            sender.sendMessage(mm.deserialize("<green>✓ Menu orders_" + categorie + ".yml créé"));
        }

        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<green>✓ Catégorie <white>" + categorie + "</white> créée avec succès!"));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<gray>Prochaines étapes:"));
        sender.sendMessage(mm.deserialize("<gray>1. Éditez <white>orders_template.yml</white> pour ajouter des items"));
        sender.sendMessage(mm.deserialize("<gray>2. Éditez <white>menus/orders_" + categorie + ".yml</white> pour personnaliser le menu"));
        sender.sendMessage(mm.deserialize("<gray>3. Ajoutez un bouton dans <white>orders_categories.yml</white>"));
        sender.sendMessage(mm.deserialize("<gray>4. Exécutez <white>/quantum reload</white>"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }

    private void deleteCategorie(CommandSender sender, String categorie) {
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        sender.sendMessage(mm.deserialize(
            "<red>➤ Suppression de la catégorie <white>" + categorie + "</white>"
        ));
        sender.sendMessage("");

        // 1. Vérifier si la catégorie existe
        File templateFile = new File(plugin.getDataFolder(), "orders_template.yml");
        if (!templateFile.exists()) {
            sender.sendMessage(mm.deserialize("<red>✗ Fichier orders_template.yml introuvable!"));
            return;
        }

        YamlConfiguration template = YamlConfiguration.loadConfiguration(templateFile);
        if (!template.contains(categorie)) {
            sender.sendMessage(mm.deserialize("<red>✗ La catégorie <white>" + categorie + "</white> n'existe pas!"));
            sender.sendMessage(mm.deserialize(
                "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
            ));
            return;
        }

        // 2. Supprimer du template
        template.set(categorie, null);
        try {
            template.save(templateFile);
            sender.sendMessage(mm.deserialize("<green>✓ Catégorie supprimée de orders_template.yml"));
        } catch (IOException e) {
            sender.sendMessage(mm.deserialize("<red>✗ Erreur lors de la sauvegarde du template!"));
            e.printStackTrace();
            return;
        }

        // 3. Supprimer le fichier menu
        File menuFile = new File(plugin.getDataFolder(), "menus/orders_" + categorie + ".yml");
        if (menuFile.exists()) {
            if (menuFile.delete()) {
                sender.sendMessage(mm.deserialize("<green>✓ Fichier orders_" + categorie + ".yml supprimé"));
            } else {
                sender.sendMessage(mm.deserialize("<red>✗ Impossible de supprimer le fichier menu!"));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<green>✓ Catégorie <white>" + categorie + "</white> supprimée!"));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<yellow>⚠ N'oubliez pas de:"));
        sender.sendMessage(mm.deserialize("<gray>1. Retirer le bouton de <white>orders_categories.yml</white>"));
        sender.sendMessage(mm.deserialize("<gray>2. Exécuter <white>/quantum reload</white>"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }

    private void createMenuFile(File file, String categorie) {
        String capitalizedCategorie = categorie.substring(0, 1).toUpperCase() + categorie.substring(1);
        
        YamlConfiguration menu = new YamlConfiguration();
        menu.set("title", "<gradient:#32b8c6:#1d6880>Ordres - " + capitalizedCategorie + "</gradient>");
        menu.set("size", 54);
        
        // Filler
        menu.set("items.filler.slots", new int[]{0,1,2,3,4,5,6,7,8,45,46,47,48,50,51,52,53});
        menu.set("items.filler.material", "BLACK_STAINED_GLASS_PANE");
        menu.set("items.filler.display_name", " ");
        
        // Back button
        menu.set("items.back.slot", 49);
        menu.set("items.back.material", "PAPER");
        menu.set("items.back.custom_model_data", 1000);
        menu.set("items.back.display_name", "<yellow>← Retour</yellow>");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add("<gray>Retour au menu des catégories");
        menu.set("items.back.lore", backLore);
        List<String> backActions = new ArrayList<>();
        backActions.add("[menu] orders_categories");
        menu.set("items.back.left_click_actions", backActions);
        
        // Orders items
        menu.set("items.orders_" + categorie + ".slots", new int[]{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44});
        menu.set("items.orders_" + categorie + ".material", "BARRIER");
        menu.set("items.orders_" + categorie + ".display_name", "%item_display_name%");
        menu.set("items.orders_" + categorie + ".button_type", "QUANTUM_ORDERS_ITEM");
        menu.set("items.orders_" + categorie + ".type", categorie);
        
        List<String> loreAppend = new ArrayList<>();
        loreAppend.add(" ");
        loreAppend.add("&8─────────────────────");
        loreAppend.add("&fChercheur: &b%orderer%");
        loreAppend.add("&fQuantité: &a%quantity%");
        loreAppend.add("&fPrix unitaire: &6%price%");
        loreAppend.add("&fPrix total: &e%total_price%");
        loreAppend.add("&8─────────────────────");
        loreAppend.add(" ");
        loreAppend.add("&7▸ Clic droit: &eVendre");
        menu.set("items.orders_" + categorie + ".lore_append", loreAppend);
        
        try {
            menu.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        sender.sendMessage(mm.deserialize(
            "<#32b8c6>➤ Gestion des catégories d'ordres"
        ));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize(
            "<white>/quantum orders button createcategorie <categorie>"
        ));
        sender.sendMessage(mm.deserialize(
            "<gray>  → Crée une nouvelle catégorie d'ordres"
        ));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize(
            "<white>/quantum orders button deletecategorie <categorie>"
        ));
        sender.sendMessage(mm.deserialize(
            "<gray>  → Supprime une catégorie d'ordres existante"
        ));
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }
}
