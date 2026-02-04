package com.wynvers.quantum.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    
    public void error(String message, Exception e) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + message);
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Exception: " + e.getMessage());
        
        // Print stack trace to console
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        for (String line : stackTrace.split("\n")) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + line);
        }
    }
    
    public void debug(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.AQUA + "[DEBUG] " + message);
    }
}
