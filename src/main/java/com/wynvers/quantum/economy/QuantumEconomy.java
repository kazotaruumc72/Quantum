package com.wynvers.quantum.economy;

import com.wynvers.quantum.Quantum;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quantum's internal economy implementation for Vault
 * Supports multiple currencies with configurable symbols
 * The primary currency (first in config) is used for Vault integration
 */
public class QuantumEconomy implements Economy {
    
    private final Quantum plugin;
    private final String currencyId;
    private final String currencyName;
    private final String currencyNamePlural;
    private final String symbol;
    private final double startingBalance;
    private final String formatPattern;
    
    public QuantumEconomy(Quantum plugin, String currencyId, String currencyName, 
                          String currencyNamePlural, String symbol, 
                          double startingBalance, String formatPattern) {
        this.plugin = plugin;
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.currencyNamePlural = currencyNamePlural;
        this.symbol = symbol;
        this.startingBalance = startingBalance;
        this.formatPattern = formatPattern;
    }
    
    /**
     * Get the unique identifier of this currency
     */
    public String getCurrencyId() {
        return currencyId;
    }
    
    /**
     * Get the currency symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public String getName() {
        return "Quantum Economy";
    }
    
    @Override
    public boolean hasBankSupport() {
        return false;
    }
    
    @Override
    public int fractionalDigits() {
        return 2;
    }
    
    @Override
    public String format(double amount) {
        String formattedAmount = String.format("%.2f", amount);
        String currency = amount == 1.0 ? currencyName : currencyNamePlural;
        return formatPattern
                .replace("%amount%", formattedAmount)
                .replace("%symbol%", symbol)
                .replace("%currency%", currency);
    }
    
    @Override
    public String currencyNamePlural() {
        return currencyNamePlural;
    }
    
    @Override
    public String currencyNameSingular() {
        return currencyName;
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM quantum_player_balances WHERE uuid = ? AND currency_id = ? LIMIT 1")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, currencyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error checking account existence: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }
    
    @Override
    public boolean hasAccount(String playerName) {
        return false;
    }
    
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return false;
    }
    
    @Override
    public double getBalance(OfflinePlayer player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT balance FROM quantum_player_balances WHERE uuid = ? AND currency_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, currencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error getting balance: " + e.getMessage());
        }
        return 0.0;
    }
    
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }
    
    @Override
    public double getBalance(String playerName) {
        return 0.0;
    }
    
    @Override
    public double getBalance(String playerName, String world) {
        return 0.0;
    }
    
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }
    
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }
    
    @Override
    public boolean has(String playerName, double amount) {
        return false;
    }
    
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return false;
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }
        
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist");
        }
        
        double balance = getBalance(player);
        if (balance < amount) {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE quantum_player_balances SET balance = balance - ? WHERE uuid = ? AND currency_id = ?")) {
            ps.setDouble(1, amount);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, currencyId);
            ps.executeUpdate();
            
            double newBalance = balance - amount;
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error withdrawing money: " + e.getMessage());
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Database error");
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use OfflinePlayer version");
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use OfflinePlayer version");
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }
        
        if (!hasAccount(player)) {
            createPlayerAccount(player);
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE quantum_player_balances SET balance = balance + ? WHERE uuid = ? AND currency_id = ?")) {
            ps.setDouble(1, amount);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, currencyId);
            ps.executeUpdate();
            
            double newBalance = getBalance(player);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error depositing money: " + e.getMessage());
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Database error");
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use OfflinePlayer version");
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use OfflinePlayer version");
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (hasAccount(player)) {
            return false;
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO quantum_player_balances (uuid, currency_id, balance) VALUES (?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, currencyId);
            ps.setDouble(3, startingBalance);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error creating player account: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
    
    /**
     * Supprime le compte économique d'un joueur pour cette monnaie
     */
    public boolean deletePlayerAccount(OfflinePlayer player) {
        if (!hasAccount(player)) {
            return false;
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM quantum_player_balances WHERE uuid = ? AND currency_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, currencyId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getQuantumLogger().error("Error deleting player account: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }
    
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }
    
    // Bank methods - not implemented
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }
}
