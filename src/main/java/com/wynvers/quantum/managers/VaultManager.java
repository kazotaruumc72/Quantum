package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.economy.QuantumEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class VaultManager {
    
    private final Quantum plugin;
    private Economy economy;
    private boolean enabled;
    private QuantumEconomy quantumEconomy;
    
    public VaultManager(Quantum plugin) {
        this.plugin = plugin;
        this.enabled = false;
        setupEconomy();
    }
    
    /**
     * Configure Vault Economy
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getQuantumLogger().warning("Vault non détecté ! Système de vente désactivé.");
            return;
        }
        
        // Register our own economy provider
        quantumEconomy = new QuantumEconomy(plugin);
        plugin.getServer().getServicesManager().register(
            Economy.class,
            quantumEconomy,
            plugin,
            ServicePriority.Highest
        );
        
        // Use our registered economy
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getQuantumLogger().warning("Erreur lors de l'enregistrement de l'économie Quantum !");
            return;
        }
        
        economy = rsp.getProvider();
        enabled = true;
        plugin.getQuantumLogger().success("✓ Economy system ready!");
        plugin.getQuantumLogger().success("Quantum Economy enregistré avec succès via Vault !");
    }
    
    /**
     * Vérifie si Vault est activé
     */
    public boolean isEnabled() {
        return enabled && economy != null;
    }
    
    /**
     * Récupère le solde d'un joueur
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEnabled()) return 0.0;
        return economy.getBalance(player);
    }
    
    /**
     * Ajoute de l'argent à un joueur
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Retire de l'argent à un joueur
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Vérifie si un joueur a assez d'argent
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }
    
    /**
     * Formate un montant en chaîne de caractères
     */
    public String format(double amount) {
        if (!isEnabled()) return String.format("%.2f", amount);
        return economy.format(amount);
    }
    
    /**
     * Récupère le nom de la monnaie (singulier)
     */
    public String getCurrencyName() {
        if (!isEnabled()) return "$";
        return economy.currencyNameSingular();
    }
    
    /**
     * Récupère le nom de la monnaie (pluriel)
     */
    public String getCurrencyNamePlural() {
        if (!isEnabled()) return "$";
        return economy.currencyNamePlural();
    }
    
    /**
     * Récupère l'instance Economy de Vault
     * @return Economy instance or null if Vault is not enabled
     * @see #isEnabled() to check if Vault is properly configured
     */
    public Economy getEconomy() {
        return economy;
    }
    
    /**
     * Récupère l'instance QuantumEconomy
     * @return QuantumEconomy instance or null if not initialized
     */
    public QuantumEconomy getQuantumEconomy() {
        return quantumEconomy;
    }
}
