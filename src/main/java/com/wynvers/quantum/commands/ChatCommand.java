package com.wynvers.quantum.commands;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.chat.ChatManager;
import com.wynvers.quantum.managers.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Commande pour gérer le système de chat
 * /chat reload - Recharge la configuration du chat
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChatCommand implements CommandExecutor {
    
    private final Quantum plugin;
    private final ChatManager chatManager;
    private final MessageManager messageManager;
    
    public ChatCommand(Quantum plugin, ChatManager chatManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
        this.messageManager = messageManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        // Sous-commande reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("quantum.chat.reload")) {
                messageManager.sendPrefixedMessage(sender, "system.no-permission", new HashMap<>());
                return true;
            }
            
            chatManager.reload();
            messageManager.sendPrefixedMessage(sender, "chat.reload-success", new HashMap<>());
            return true;
        }
        
        // Afficher l'aide
        sender.sendMessage("§6=== Commandes Chat Quantum ===");
        sender.sendMessage("§e/chat reload §7- Recharger la configuration du chat");
        
        return true;
    }
}
