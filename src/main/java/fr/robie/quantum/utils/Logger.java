package fr.robie.quantum.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    
    private final String prefix;
    
    public Logger(String pluginName) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', 
            "&8[&6" + pluginName + "&8]&r ");
    }
    
    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GRAY + message);
    }
    
    public void success(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + message);
    }
    
    public void warning(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + message);
    }
    
    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + message);
    }
    
    public void debug(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.AQUA + "[DEBUG] " + message);
    }
}
