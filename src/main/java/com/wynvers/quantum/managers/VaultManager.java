package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {
    
    private final Quantum plugin;
    private Economy economy;
    private boolean enabled;
    
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
            plugin.getQuantumLogger().warning("Vault non d\u00e9tect\u00e9 ! Syst\u00e8me de vente d\u00e9sactiv\u00e9.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getQuantumLogger().warning("Aucun plugin d'\u00e9conomie trouv\u00e9 ! Installez un plugin comme EssentialsX.");
            return;
        }
        
        economy = rsp.getProvider();
        enabled = true;
        plugin.getQuantumLogger().success("Vault connect\u00e9 avec succ\u00e8s ! Plugin d'\u00e9conomie: " + economy.getName());
    }
    
    /**
     * V\u00e9rifie si Vault est activ\u00e9
     */
    public boolean isEnabled() {
        return enabled && economy != null;
    }
    
    /**
     * R\u00e9cup\u00e8re le solde d'un joueur
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEnabled()) return 0.0;
        return economy.getBalance(player);
    }
    
    /**
     * Ajoute de l'argent \u00e0 un joueur
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Retire de l'argent \u00e0 un joueur
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * V\u00e9rifie si un joueur a assez d'argent
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }
    
    /**
     * Formate un montant en cha\u00eene de caract\u00e8res
     */
    public String format(double amount) {
        if (!isEnabled()) return String.format("%.2f", amount);
        return economy.format(amount);
    }
    
    /**
     * R\u00e9cup\u00e8re le nom de la monnaie (singulier)
     */
    public String getCurrencyName() {
        if (!isEnabled()) return "$";
        return economy.currencyNameSingular();
    }
    
    /**
     * R\u00e9cup\u00e8re le nom de la monnaie (pluriel)
     */
    public String getCurrencyNamePlural() {
        if (!isEnabled()) return "$";
        return economy.currencyNamePlural();
    }
    
    /**
     * R\u00e9cup\u00e8re l'instance Economy de Vault
     * @return Economy instance or null
     */
    public Economy getEconomy() {
        return economy;
    }
}
