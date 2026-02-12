package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.tab.TABManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command executor for TAB system management
 * Allows editing headers and footers for different permission groups
 */
public class TabEditCommand implements CommandExecutor, TabCompleter {
    
    private final Quantum plugin;
    
    public TabEditCommand(Quantum plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check permission
        if (!sender.hasPermission("quantum.tab.edit")) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
            return true;
        }
        
        // Check if TAB is enabled
        if (plugin.getTabManager() == null || !plugin.getTabManager().isEnabled()) {
            sender.sendMessage(Component.text("Le système TAB n'est pas activé.", NamedTextColor.RED));
            return true;
        }
        
        // Usage: /tabedit <header|footer|reload|list> [group] [line] [text...]
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "list":
                return handleList(sender);
            case "header":
            case "footer":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /tabedit " + subCommand + " <group>", NamedTextColor.RED));
                    return true;
                }
                return handleHeaderFooter(sender, subCommand, args[1], Arrays.copyOfRange(args, 2, args.length));
            default:
                sendUsage(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        plugin.getTabManager().reload();
        sender.sendMessage(Component.text("Configuration TAB rechargée avec succès!", NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        TABManager tabManager = plugin.getTabManager();
        var groups = tabManager.getGroups();
        
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Groupes TAB disponibles:", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        
        for (String groupName : groups.keySet()) {
            TABManager.TabGroup group = groups.get(groupName);
            Component message = Component.text("• ", NamedTextColor.GRAY)
                .append(Component.text(groupName, NamedTextColor.YELLOW))
                .append(Component.text(" - Permission: ", NamedTextColor.GRAY))
                .append(Component.text(group.getPermission().isEmpty() ? "Aucune" : group.getPermission(), NamedTextColor.AQUA));
            sender.sendMessage(message);
        }
        
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        return true;
    }
    
    private boolean handleHeaderFooter(CommandSender sender, String type, String groupName, String[] lineArgs) {
        TABManager tabManager = plugin.getTabManager();
        YamlConfiguration config = tabManager.getConfig();
        
        // Check if group exists
        if (!tabManager.getGroups().containsKey(groupName)) {
            sender.sendMessage(Component.text("Le groupe '" + groupName + "' n'existe pas.", NamedTextColor.RED));
            sender.sendMessage(Component.text("Utilisez /tabedit list pour voir les groupes disponibles.", NamedTextColor.YELLOW));
            return true;
        }
        
        // If no line args, show current configuration
        if (lineArgs.length == 0) {
            showGroupConfig(sender, groupName, type);
            return true;
        }
        
        // Subcommands: add <text>, remove <lineNum>, set <lineNum> <text>, clear
        String action = lineArgs[0].toLowerCase();
        
        switch (action) {
            case "add":
                if (lineArgs.length < 2) {
                    sender.sendMessage(Component.text("Usage: /tabedit " + type + " " + groupName + " add <text>", NamedTextColor.RED));
                    return true;
                }
                return handleAdd(sender, type, groupName, String.join(" ", Arrays.copyOfRange(lineArgs, 1, lineArgs.length)));
                
            case "remove":
                if (lineArgs.length < 2) {
                    sender.sendMessage(Component.text("Usage: /tabedit " + type + " " + groupName + " remove <lineNumber>", NamedTextColor.RED));
                    return true;
                }
                try {
                    int lineNum = Integer.parseInt(lineArgs[1]);
                    return handleRemove(sender, type, groupName, lineNum);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Numéro de ligne invalide.", NamedTextColor.RED));
                    return true;
                }
                
            case "set":
                if (lineArgs.length < 3) {
                    sender.sendMessage(Component.text("Usage: /tabedit " + type + " " + groupName + " set <lineNumber> <text>", NamedTextColor.RED));
                    return true;
                }
                try {
                    int lineNum = Integer.parseInt(lineArgs[1]);
                    String text = String.join(" ", Arrays.copyOfRange(lineArgs, 2, lineArgs.length));
                    return handleSet(sender, type, groupName, lineNum, text);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Numéro de ligne invalide.", NamedTextColor.RED));
                    return true;
                }
                
            case "clear":
                return handleClear(sender, type, groupName);
                
            default:
                sender.sendMessage(Component.text("Action invalide. Utilisez: add, remove, set, clear", NamedTextColor.RED));
                return true;
        }
    }
    
    private void showGroupConfig(CommandSender sender, String groupName, String type) {
        TABManager.TabGroup group = plugin.getTabManager().getGroups().get(groupName);
        List<String> lines = type.equals("header") ? group.getHeader() : group.getFooter();
        
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        sender.sendMessage(Component.text(type.toUpperCase() + " du groupe " + groupName + ":", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        
        for (int i = 0; i < lines.size(); i++) {
            sender.sendMessage(Component.text((i + 1) + ". ", NamedTextColor.GRAY)
                .append(Component.text(lines.get(i), NamedTextColor.WHITE)));
        }
        
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Actions: add, remove <n>, set <n> <text>, clear", NamedTextColor.YELLOW));
    }
    
    private boolean handleAdd(CommandSender sender, String type, String groupName, String text) {
        YamlConfiguration config = plugin.getTabManager().getConfig();
        String path = "groups." + groupName + "." + type;
        
        List<String> lines = config.getStringList(path);
        lines.add(text);
        config.set(path, lines);
        
        plugin.getTabManager().saveConfig();
        plugin.getTabManager().reload();
        
        sender.sendMessage(Component.text("Ligne ajoutée au " + type + " du groupe " + groupName + "!", NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String type, String groupName, int lineNum) {
        YamlConfiguration config = plugin.getTabManager().getConfig();
        String path = "groups." + groupName + "." + type;
        
        List<String> lines = config.getStringList(path);
        
        if (lineNum < 1 || lineNum > lines.size()) {
            sender.sendMessage(Component.text("Numéro de ligne invalide (1-" + lines.size() + ")", NamedTextColor.RED));
            return true;
        }
        
        lines.remove(lineNum - 1);
        config.set(path, lines);
        
        plugin.getTabManager().saveConfig();
        plugin.getTabManager().reload();
        
        sender.sendMessage(Component.text("Ligne supprimée du " + type + " du groupe " + groupName + "!", NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleSet(CommandSender sender, String type, String groupName, int lineNum, String text) {
        YamlConfiguration config = plugin.getTabManager().getConfig();
        String path = "groups." + groupName + "." + type;
        
        List<String> lines = config.getStringList(path);
        
        if (lineNum < 1 || lineNum > lines.size()) {
            sender.sendMessage(Component.text("Numéro de ligne invalide (1-" + lines.size() + ")", NamedTextColor.RED));
            return true;
        }
        
        lines.set(lineNum - 1, text);
        config.set(path, lines);
        
        plugin.getTabManager().saveConfig();
        plugin.getTabManager().reload();
        
        sender.sendMessage(Component.text("Ligne modifiée dans le " + type + " du groupe " + groupName + "!", NamedTextColor.GREEN));
        return true;
    }
    
    private boolean handleClear(CommandSender sender, String type, String groupName) {
        YamlConfiguration config = plugin.getTabManager().getConfig();
        String path = "groups." + groupName + "." + type;
        
        config.set(path, new ArrayList<String>());
        
        plugin.getTabManager().saveConfig();
        plugin.getTabManager().reload();
        
        sender.sendMessage(Component.text(type.toUpperCase() + " du groupe " + groupName + " effacé!", NamedTextColor.GREEN));
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Commandes TAB Edit:", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/tabedit list", NamedTextColor.YELLOW)
            .append(Component.text(" - Liste les groupes disponibles", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit reload", NamedTextColor.YELLOW)
            .append(Component.text(" - Recharge la configuration", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit header <group>", NamedTextColor.YELLOW)
            .append(Component.text(" - Voir le header d'un groupe", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit footer <group>", NamedTextColor.YELLOW)
            .append(Component.text(" - Voir le footer d'un groupe", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit header <group> add <text>", NamedTextColor.YELLOW)
            .append(Component.text(" - Ajouter une ligne", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit header <group> remove <n>", NamedTextColor.YELLOW)
            .append(Component.text(" - Supprimer une ligne", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit header <group> set <n> <text>", NamedTextColor.YELLOW)
            .append(Component.text(" - Modifier une ligne", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/tabedit header <group> clear", NamedTextColor.YELLOW)
            .append(Component.text(" - Effacer tout", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.AQUA));
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("quantum.tab.edit")) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("header", "footer", "reload", "list"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("header") || args[0].equalsIgnoreCase("footer"))) {
            if (plugin.getTabManager() != null && plugin.getTabManager().isEnabled()) {
                completions.addAll(plugin.getTabManager().getGroups().keySet());
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("header") || args[0].equalsIgnoreCase("footer"))) {
            completions.addAll(Arrays.asList("add", "remove", "set", "clear"));
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
