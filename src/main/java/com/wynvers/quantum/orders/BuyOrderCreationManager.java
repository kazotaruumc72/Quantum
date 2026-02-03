package com.wynvers.quantum.orders;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère la création d'ordres d'ACHAT
 * 
 * Fonctionnement:
 * 1. Joueur veut ACHETER des items
 * 2. Choisit item + quantité + prix par unité
 * 3. Son ARGENT est mis en ESCROW
 * 4. D'autres joueurs (vendeurs) voient l'ordre dans /market ACHAT
 * 5. Vendeur clique pour VENDRE ses items
 * 6. Items retirés du storage vendeur
 * 7. Argent transféré au vendeur
 * 8. Items ajoutés au storage acheteur
 * 
 * @author Kazotaruu_
 * @version 1.0
 */
public class BuyOrderCreationManager implements Listener {
    
    private final Quantum plugin;
    private final Map<UUID, BuyOrderCreationSession> sessions;
    
    public BuyOrderCreationManager(Quantum plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Démarre une session de création d'ordre d'achat
     */
    public void startSession(Player player, String itemId, String category) {
        // Vérifier si une session existe déjà
        if (sessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "⚠ Vous avez déjà une création d'ordre en cours!");
            player.sendMessage(ChatColor.GRAY + "Tapez " + ChatColor.YELLOW + "cancel " + ChatColor.GRAY + "pour annuler.");
            return;
        }
        
        // Créer la session
        BuyOrderCreationSession session = new BuyOrderCreationSession(player.getUniqueId(), itemId, category);
        sessions.put(player.getUniqueId(), session);
        
        // Messages de démarrage
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "[≈≈≈] " + ChatColor.GOLD + "Création d'ordre d'ACHAT " + ChatColor.DARK_GRAY + "[≈≈≈]");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Vous voulez " + ChatColor.GREEN + "ACHETER" + ChatColor.GRAY + ": " + ChatColor.WHITE + formatItemName(itemId));
        player.sendMessage(ChatColor.GRAY + "Catégorie: " + ChatColor.YELLOW + category);
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "➜ Combien voulez-vous ACHETER?");
        player.sendMessage(ChatColor.GRAY + "Tapez la quantité dans le chat (ex: " + ChatColor.YELLOW + "64" + ChatColor.GRAY + ")");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Tapez " + ChatColor.RED + "cancel " + ChatColor.GRAY + "pour annuler.");
        player.sendMessage("");
    }
    
    /**
     * Écoute les messages du chat pour la création d'ordres
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Vérifier si le joueur a une session active
        if (!sessions.containsKey(uuid)) {
            return;
        }
        
        event.setCancelled(true);
        String message = event.getMessage().trim();
        BuyOrderCreationSession session = sessions.get(uuid);
        
        // Commande d'annulation
        if (message.equalsIgnoreCase("cancel")) {
            sessions.remove(uuid);
            player.sendMessage(ChatColor.RED + "✖ Création d'ordre annulée.");
            return;
        }
        
        // Traiter selon l'étape actuelle
        switch (session.getCurrentStep()) {
            case QUANTITY:
                handleQuantityInput(player, session, message);
                break;
                
            case PRICE:
                handlePriceInput(player, session, message);
                break;
        }
    }
    
    /**
     * Gère l'input de la quantité
     */
    private void handleQuantityInput(Player player, BuyOrderCreationSession session, String input) {
        try {
            int quantity = Integer.parseInt(input);
            
            // Validation
            if (quantity <= 0) {
                player.sendMessage(ChatColor.RED + "⚠ La quantité doit être supérieure à 0!");
                return;
            }
            
            if (quantity > 999999) {
                player.sendMessage(ChatColor.RED + "⚠ Quantité maximale: 999,999");
                return;
            }
            
            // Enregistrer la quantité
            session.setQuantity(quantity);
            session.setCurrentStep(OrderCreationStep.PRICE);
            
            // Message suivant
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "✓ Quantité définie: " + ChatColor.YELLOW + quantity + "x");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "➜ Quel est votre prix par unité?");
            player.sendMessage(ChatColor.GRAY + "Tapez le prix dans le chat (ex: " + ChatColor.YELLOW + "10.5" + ChatColor.GRAY + ")");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Le coût total sera: " + ChatColor.GOLD + quantity + " x [votre prix]$");
            player.sendMessage("");
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "⚠ Quantité invalide! Entrez un nombre entier.");
            player.sendMessage(ChatColor.GRAY + "Exemple: " + ChatColor.YELLOW + "64");
        }
    }
    
    /**
     * Gère l'input du prix
     */
    private void handlePriceInput(Player player, BuyOrderCreationSession session, String input) {
        try {
            double pricePerUnit = Double.parseDouble(input);
            
            // Validation
            if (pricePerUnit <= 0) {
                player.sendMessage(ChatColor.RED + "⚠ Le prix doit être supérieur à 0!");
                return;
            }
            
            if (pricePerUnit > 1000000) {
                player.sendMessage(ChatColor.RED + "⚠ Prix maximal par unité: 1,000,000$");
                return;
            }
            
            // Calculer le coût total
            double totalCost = pricePerUnit * session.getQuantity();
            
            // Vérifier que le joueur a assez d'argent
            if (!plugin.getVaultManager().has(player, totalCost)) {
                player.sendMessage(ChatColor.RED + "⚠ Argent insuffisant!");
                player.sendMessage(ChatColor.GRAY + "Coût total: " + ChatColor.GOLD + String.format("%.2f", totalCost) + "$");
                player.sendMessage(ChatColor.GRAY + "Votre solde: " + ChatColor.GOLD + String.format("%.2f", plugin.getVaultManager().getBalance(player)) + "$");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "Tapez un prix inférieur ou " + ChatColor.RED + "cancel " + ChatColor.GRAY + "pour annuler.");
                return;
            }
            
            // Enregistrer le prix
            session.setPricePerUnit(pricePerUnit);
            
            // Créer l'ordre (exécution synchrone)
            Bukkit.getScheduler().runTask(plugin, () -> {
                createBuyOrder(player, session, totalCost);
            });
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "⚠ Prix invalide! Entrez un nombre décimal.");
            player.sendMessage(ChatColor.GRAY + "Exemples: " + ChatColor.YELLOW + "10 " + ChatColor.GRAY + "ou " + ChatColor.YELLOW + "10.5");
        }
    }
    
    /**
     * Crée l'ordre d'achat final
     */
    private void createBuyOrder(Player player, BuyOrderCreationSession session, double totalCost) {
        UUID uuid = player.getUniqueId();
        
        // Retirer l'argent du joueur
        if (!plugin.getVaultManager().withdraw(player, totalCost)) {
            player.sendMessage(ChatColor.RED + "⚠ Erreur lors du retrait de l'argent!");
            sessions.remove(uuid);
            return;
        }
        
        // Générer un UUID pour l'ordre
        UUID orderUUID = UUID.randomUUID();
        
        // Déposer l'argent en escrow
        plugin.getEscrowManager().deposit(orderUUID, totalCost);
        
        // Sauvegarder l'ordre dans orders.yml
        File ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        YamlConfiguration ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        
        String path = session.getCategory() + "." + orderUUID.toString();
        ordersConfig.set(path + ".type", "BUY");
        ordersConfig.set(path + ".orderer", player.getName());
        ordersConfig.set(path + ".orderer_uuid", uuid.toString());
        ordersConfig.set(path + ".item", session.getItemId());
        ordersConfig.set(path + ".quantity", session.getQuantity());
        ordersConfig.set(path + ".price_per_unit", session.getPricePerUnit());
        ordersConfig.set(path + ".total_price", totalCost);
        ordersConfig.set(path + ".status", "ACTIVE");
        ordersConfig.set(path + ".created_at", System.currentTimeMillis());
        
        try {
            ordersConfig.save(ordersFile);
            
            // Messages de succès
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + "[≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈]");
            player.sendMessage("");
            player.sendMessage("  " + ChatColor.GREEN + ChatColor.BOLD + "✓ ORDRE D'ACHAT CRÉÉ !");
            player.sendMessage("");
            player.sendMessage("  " + ChatColor.GRAY + "Item: " + ChatColor.WHITE + formatItemName(session.getItemId()));
            player.sendMessage("  " + ChatColor.GRAY + "Quantité: " + ChatColor.YELLOW + session.getQuantity() + "x");
            player.sendMessage("  " + ChatColor.GRAY + "Prix/u: " + ChatColor.GOLD + String.format("%.2f", session.getPricePerUnit()) + "$");
            player.sendMessage("  " + ChatColor.GRAY + "Coût total: " + ChatColor.GOLD + String.format("%.2f", totalCost) + "$");
            player.sendMessage("");
            player.sendMessage("  " + ChatColor.DARK_GRAY + "→ " + ChatColor.GRAY + "Votre argent est en sécurité (escrow)");
            player.sendMessage("  " + ChatColor.DARK_GRAY + "→ " + ChatColor.GRAY + "Les vendeurs verront votre ordre");
            player.sendMessage("  " + ChatColor.DARK_GRAY + "→ " + ChatColor.GRAY + "Tapez " + ChatColor.YELLOW + "/market " + ChatColor.GRAY + "pour gérer vos ordres");
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + "[≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈≈]");
            player.sendMessage("");
            
            // Logger
            plugin.getQuantumLogger().info("[BUY_ORDER] Created by " + player.getName());
            plugin.getQuantumLogger().info("  Item: " + session.getItemId());
            plugin.getQuantumLogger().info("  Quantity: " + session.getQuantity() + "x");
            plugin.getQuantumLogger().info("  Price: " + String.format("%.2f", session.getPricePerUnit()) + "$/u");
            plugin.getQuantumLogger().info("  Total Cost: " + String.format("%.2f", totalCost) + "$");
            plugin.getQuantumLogger().info("  UUID: " + orderUUID);
            
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "⚠ Erreur lors de la sauvegarde de l'ordre!");
            plugin.getQuantumLogger().error("Failed to save buy order: " + e.getMessage());
            
            // Rembourser le joueur
            plugin.getEscrowManager().withdraw(orderUUID);
            plugin.getVaultManager().deposit(player, totalCost);
        }
        
        // Supprimer la session
        sessions.remove(uuid);
    }
    
    /**
     * Formate joliment un itemId
     */
    private String formatItemName(String itemId) {
        if (itemId.startsWith("nexo:")) {
            return "[Nexo] " + itemId.substring(5).replace("_", " ");
        } else if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10).replace("_", " ");
        }
        return itemId;
    }
    
    /**
     * Annule toutes les sessions actives (pour le reload)
     */
    public void cancelAllSessions() {
        for (UUID uuid : sessions.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "✖ Création d'ordre annulée (plugin rechargé).");
            }
        }
        sessions.clear();
    }
    
    /**
     * Étapes de création d'ordre
     */
    public enum OrderCreationStep {
        QUANTITY,  // Demande de quantité
        PRICE      // Demande de prix
    }
    
    /**
     * Session de création d'ordre d'achat
     */
    public static class BuyOrderCreationSession {
        private final UUID playerUUID;
        private final String itemId;
        private final String category;
        private int quantity;
        private double pricePerUnit;
        private OrderCreationStep currentStep;
        
        public BuyOrderCreationSession(UUID playerUUID, String itemId, String category) {
            this.playerUUID = playerUUID;
            this.itemId = itemId;
            this.category = category;
            this.currentStep = OrderCreationStep.QUANTITY;
        }
        
        // Getters & Setters
        public UUID getPlayerUUID() { return playerUUID; }
        public String getItemId() { return itemId; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
        public OrderCreationStep getCurrentStep() { return currentStep; }
        public void setCurrentStep(OrderCreationStep currentStep) { this.currentStep = currentStep; }
    }
}
