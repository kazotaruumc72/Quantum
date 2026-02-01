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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            "<#32b8c6>➜ Création de la catégorie <white>" + categorie + "</white>"
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
        Map<String, Object> defaultItem = new HashMap<>();
        defaultItem.put("min_price", 10.0);
        defaultItem.put("max_price", 100.0);
        
        Map<String, Object> items = new HashMap<>();
        items.put("minecraft:stone", defaultItem);
        
        template.set(categorie + ".items", items);

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
        
        // 4. Ajouter automatiquement le bouton dans orders_categories.yml
        File categoriesFile = new File(plugin.getDataFolder(), "menus/orders_categories.yml");
        if (!categoriesFile.exists()) {
            sender.sendMessage(mm.deserialize("<red>✗ Fichier orders_categories.yml introuvable!"));
        } else {
            if (addButtonToCategories(categoriesFile, categorie)) {
                sender.sendMessage(mm.deserialize("<green>✓ Bouton ajouté dans orders_categories.yml"));
            } else {
                sender.sendMessage(mm.deserialize("<yellow>⚠ Le bouton existe déjà dans orders_categories.yml"));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<green>✓ Catégorie <white>" + categorie + "</white> créée avec succès!"));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<gray>Prochaines étapes:"));
        sender.sendMessage(mm.deserialize("<gray>1. Éditez <white>orders_template.yml</white> pour ajouter des items"));
        sender.sendMessage(mm.deserialize("<gray>2. Éditez <white>menus/orders_" + categorie + ".yml</white> pour personnaliser le menu"));
        sender.sendMessage(mm.deserialize("<gray>3. Exécutez <white>/quantum reload</white>"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }
    
    /**
     * Ajoute automatiquement un bouton dans le menu orders_categories.yml
     */
    private boolean addButtonToCategories(File categoriesFile, String categorie) {
        YamlConfiguration categoriesMenu = YamlConfiguration.loadConfiguration(categoriesFile);
        
        // Vérifier si le bouton existe déjà
        String buttonPath = "items." + categorie;
        if (categoriesMenu.contains(buttonPath)) {
            return false; // Bouton existe déjà
        }
        
        // Trouver le prochain slot disponible (commence à 9)
        int nextSlot = findNextAvailableSlot(categoriesMenu);
        if (nextSlot == -1) {
            return false; // Plus de slot disponible
        }
        
        String capitalizedCategorie = categorie.substring(0, 1).toUpperCase() + categorie.substring(1);
        
        // Créer le bouton
        categoriesMenu.set(buttonPath + ".slot", nextSlot);
        categoriesMenu.set(buttonPath + ".material", "PAPER");
        categoriesMenu.set(buttonPath + ".custom_model_data", getCustomModelData(categorie));
        categoriesMenu.set(buttonPath + ".display_name", "<gradient:#32b8c6:#1d6880>" + capitalizedCategorie + "</gradient>");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<gray>Offres d'achat de " + categorie);
        lore.add("");
        lore.add("<yellow>► Cliquez pour voir les offres</yellow>");
        categoriesMenu.set(buttonPath + ".lore", lore);
        
        List<String> leftClick = new ArrayList<>();
        leftClick.add("[menu] orders_" + categorie);
        categoriesMenu.set(buttonPath + ".left_click_actions", leftClick);
        
        try {
            categoriesMenu.save(categoriesFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trouve le prochain slot disponible dans le menu des catégories
     */
    private int findNextAvailableSlot(YamlConfiguration config) {
        List<Integer> usedSlots = new ArrayList<>();
        
        // Récupérer tous les slots utilisés
        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                if (config.contains("items." + key + ".slot")) {
                    usedSlots.add(config.getInt("items." + key + ".slot"));
                }
            }
        }
        
        // Chercher le premier slot libre entre 9 et 44
        for (int i = 9; i <= 44; i++) {
            if (!usedSlots.contains(i)) {
                return i;
            }
        }
        
        return -1; // Aucun slot disponible
    }
    
    /**
     * Retourne un custom_model_data en fonction du nom de la catégorie
     */
    private int getCustomModelData(String categorie) {
        switch (categorie.toLowerCase()) {
            case "cultures": return 1001;
            case "loots": return 1002;
            case "items": return 1003;
            case "potions": return 1004;
            case "armures": return 1005;
            case "outils": return 1006;
            default: return 1000 + (categorie.hashCode() % 100);
        }
    }

    private void deleteCategorie(CommandSender sender, String categorie) {
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
        sender.sendMessage(mm.deserialize(
            "<red>➜ Suppression de la catégorie <white>" + categorie + "</white>"
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
        
        // 4. Supprimer le bouton de orders_categories.yml
        File categoriesFile = new File(plugin.getDataFolder(), "menus/orders_categories.yml");
        if (categoriesFile.exists()) {
            if (removeButtonFromCategories(categoriesFile, categorie)) {
                sender.sendMessage(mm.deserialize("<green>✓ Bouton supprimé de orders_categories.yml"));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<green>✓ Catégorie <white>" + categorie + "</white> supprimée!"));
        sender.sendMessage("");
        sender.sendMessage(mm.deserialize("<gray>Exécutez <white>/quantum reload</white> pour appliquer les changements"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#32b8c6:#1d6880>══════════════════════════════════</gradient>"
        ));
    }
    
    /**
     * Supprime le bouton d'une catégorie du menu orders_categories.yml
     */
    private boolean removeButtonFromCategories(File categoriesFile, String categorie) {
        YamlConfiguration categoriesMenu = YamlConfiguration.loadConfiguration(categoriesFile);
        
        String buttonPath = "items." + categorie;
        if (!categoriesMenu.contains(buttonPath)) {
            return false; // Bouton n'existe pas
        }
        
        categoriesMenu.set(buttonPath, null);
        
        try {
            categoriesMenu.save(categoriesFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
            "<#32b8c6>➜ Gestion des catégories d'ordres"
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
