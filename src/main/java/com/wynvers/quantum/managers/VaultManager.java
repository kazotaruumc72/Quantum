package com.wynvers.quantum.managers;

import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.economy.QuantumEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VaultManager {
    
    private final Quantum plugin;
    private Economy economy;
    private boolean enabled;
    private QuantumEconomy quantumEconomy;
    
    // Multi-currency support: currencyId -> QuantumEconomy instance
    private final Map<String, QuantumEconomy> currencies = new LinkedHashMap<>();
    
    public VaultManager(Quantum plugin) {
        this.plugin = plugin;
        this.enabled = false;
        setupEconomy();
    }
    
    /**
     * Configure Vault Economy with multi-currency support
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getQuantumLogger().warning("Vault non détecté ! Système de vente désactivé.");
            return;
        }
        
        // Load currencies from config
        loadCurrencies();
        
        if (currencies.isEmpty()) {
            plugin.getQuantumLogger().warning("Aucune monnaie configurée ! Utilisation des valeurs par défaut.");
            // Fallback to default currency
            quantumEconomy = new QuantumEconomy(plugin, "dollar", "Dollar", "Dollars", "$", 0.0, "%amount%%symbol%");
            currencies.put("dollar", quantumEconomy);
        } else {
            // Primary currency is the first one defined
            quantumEconomy = currencies.values().iterator().next();
        }
        
        // Register primary currency as Vault provider
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
        plugin.getQuantumLogger().success("✓ Economy system ready! (" + currencies.size() + " monnaie(s))");
        for (Map.Entry<String, QuantumEconomy> entry : currencies.entrySet()) {
            QuantumEconomy eco = entry.getValue();
            String primary = (entry.getKey().equals(quantumEconomy.getCurrencyId())) ? " §a(principale)" : "";
            plugin.getQuantumLogger().info("  " + eco.getSymbol() + " " + eco.currencyNameSingular() + "/" + eco.currencyNamePlural() + primary);
        }
    }
    
    /**
     * Load currencies from config.yml economy section
     */
    private void loadCurrencies() {
        ConfigurationSection economySection = plugin.getConfig().getConfigurationSection("economy.currencies");
        if (economySection == null) {
            return;
        }
        
        for (String currencyId : economySection.getKeys(false)) {
            ConfigurationSection currencyConfig = economySection.getConfigurationSection(currencyId);
            if (currencyConfig == null) continue;
            
            String name = currencyConfig.getString("name", currencyId);
            String namePlural = currencyConfig.getString("name-plural", name + "s");
            String symbol = currencyConfig.getString("symbol", "$");
            double startingBalance = currencyConfig.getDouble("starting-balance", 0.0);
            String format = currencyConfig.getString("format", "%amount%%symbol%");
            
            QuantumEconomy eco = new QuantumEconomy(plugin, currencyId, name, namePlural, symbol, startingBalance, format);
            currencies.put(currencyId, eco);
        }
    }
    
    /**
     * Vérifie si Vault est activé
     */
    public boolean isEnabled() {
        return enabled && economy != null;
    }
    
    /**
     * Récupère le solde d'un joueur (monnaie principale)
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEnabled()) return 0.0;
        return economy.getBalance(player);
    }
    
    /**
     * Récupère le solde d'un joueur pour une monnaie spécifique
     */
    public double getBalance(OfflinePlayer player, String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return 0.0;
        return eco.getBalance(player);
    }
    
    /**
     * Ajoute de l'argent à un joueur (monnaie principale)
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Ajoute de l'argent à un joueur pour une monnaie spécifique
     */
    public boolean deposit(OfflinePlayer player, double amount, String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return false;
        return eco.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Retire de l'argent à un joueur (monnaie principale)
     */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Retire de l'argent à un joueur pour une monnaie spécifique
     */
    public boolean withdraw(OfflinePlayer player, double amount, String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return false;
        return eco.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Vérifie si un joueur a assez d'argent (monnaie principale)
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }
    
    /**
     * Vérifie si un joueur a assez d'argent pour une monnaie spécifique
     */
    public boolean has(OfflinePlayer player, double amount, String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return false;
        return eco.has(player, amount);
    }
    
    /**
     * Formate un montant en chaîne de caractères (monnaie principale)
     */
    public String format(double amount) {
        if (!isEnabled()) return String.format("%.2f", amount);
        return economy.format(amount);
    }
    
    /**
     * Formate un montant pour une monnaie spécifique
     */
    public String format(double amount, String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return String.format("%.2f", amount);
        return eco.format(amount);
    }
    
    /**
     * Récupère le nom de la monnaie principale (singulier)
     */
    public String getCurrencyName() {
        if (!isEnabled()) return "$";
        return economy.currencyNameSingular();
    }
    
    /**
     * Récupère le nom de la monnaie principale (pluriel)
     */
    public String getCurrencyNamePlural() {
        if (!isEnabled()) return "$";
        return economy.currencyNamePlural();
    }
    
    /**
     * Récupère le symbole de la monnaie principale
     */
    public String getSymbol() {
        if (quantumEconomy == null) return "$";
        return quantumEconomy.getSymbol();
    }
    
    /**
     * Récupère le symbole d'une monnaie spécifique
     */
    public String getSymbol(String currencyId) {
        QuantumEconomy eco = currencies.get(currencyId);
        if (eco == null) return "$";
        return eco.getSymbol();
    }
    
    /**
     * Récupère l'instance Economy de Vault
     */
    public Economy getEconomy() {
        return economy;
    }
    
    /**
     * Récupère l'instance QuantumEconomy principale
     */
    public QuantumEconomy getQuantumEconomy() {
        return quantumEconomy;
    }
    
    /**
     * Récupère une monnaie spécifique par son identifiant
     * @param currencyId l'identifiant de la monnaie (ex: "dollar", "token")
     * @return l'instance QuantumEconomy, ou null si non trouvée
     */
    public QuantumEconomy getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }
    
    /**
     * Récupère toutes les monnaies disponibles
     * @return Map non modifiable des monnaies (id -> QuantumEconomy)
     */
    public Map<String, QuantumEconomy> getCurrencies() {
        return Collections.unmodifiableMap(currencies);
    }
    
    /**
     * Récupère les identifiants de toutes les monnaies disponibles
     */
    public Set<String> getCurrencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }
}
