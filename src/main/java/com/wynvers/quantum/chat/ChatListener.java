package com.wynvers.quantum.chat;

import com.wynvers.quantum.Quantum;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener pour gérer les événements de chat
 * Applique le formatage personnalisé aux messages de chat
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class ChatListener implements Listener {
    
    private final Quantum plugin;
    private final ChatManager chatManager;
    
    public ChatListener(Quantum plugin, ChatManager chatManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
    }
    
    /**
     * Gère l'événement de chat asynchrone
     * Utilise HIGHEST priority pour s'exécuter après les autres plugins
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Récupérer le message original
        Component originalMessage = event.message();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(originalMessage);
        
        // Formater le message avec le ChatManager
        Component formattedMessage = chatManager.formatChatMessage(player, plainMessage);
        
        // Définir le renderer pour tous les viewers
        // Note: Le paramètre 'viewer' est intentionnellement ignoré dans cette implémentation
        // car le format est uniforme pour tous les joueurs. Pour un formatage par viewer
        // (par exemple, afficher différents formats selon qui regarde), il faudrait
        // appeler chatManager.formatChatMessage(player, plainMessage) pour chaque viewer
        // dans la lambda au lieu d'utiliser la variable formattedMessage pré-calculée.
        event.renderer((source, sourceDisplayName, message, viewer) -> formattedMessage);
    }
}
