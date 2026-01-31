package com.wynvers.quantum.menu;

import com.wynvers.quantum.Quantum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {

    private final Quantum plugin;
    private final Map<String, Menu> menus;
    private final Map<Player, Menu> openMenus;
    private final Map<Player, Integer> titleAnimationTasks;

    public MenuManager(Quantum plugin) {
        this.plugin = plugin;
        this.menus = new HashMap<>();
        this.openMenus = new HashMap<>();
        this.titleAnimationTasks = new HashMap<>();
        loadMenus();
    }

    public void loadMenus() {
        menus.clear();
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (menuFiles == null || menuFiles.length == 0) {
            plugin.getLogger().info("No menu files found in menus folder");
            return;
        }

        for (File file : menuFiles) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                Menu menu = Menu.fromConfig(plugin, menuName, config);
                menus.put(menuName, menu);
                plugin.getLogger().info("Loaded menu: " + menuName);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load menu: " + menuName);
                e.printStackTrace();
            }
        }
    }

    public void openMenu(String menuName, Player player) {
        Menu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuName);
            return;
        }

        // Close any existing menu
        closeMenu(player);

        // Open the menu
        menu.open(player);
        openMenus.put(player, menu);

        // Start title animation if the menu has animated titles
        if (menu.hasAnimatedTitle()) {
            startTitleAnimation(player, menu);
        }
    }

    public void closeMenu(Player player) {
        openMenus.remove(player);
        stopTitleAnimation(player);
    }

    public Menu getOpenMenu(Player player) {
        return openMenus.get(player);
    }

    private void startTitleAnimation(Player player, Menu menu) {
        stopTitleAnimation(player); // Stop any existing animation

        List<String> titles = menu.getAnimatedTitles();
        if (titles == null || titles.isEmpty()) return;

        int taskId = new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!openMenus.containsKey(player) || openMenus.get(player) != menu) {
                    cancel();
                    return;
                }

                String title = ChatColor.translateAlternateColorCodes('&', titles.get(index));
                player.getOpenInventory().getTopInventory().clear();
                menu.updateTitle(player, title);
                menu.populateInventory(player.getOpenInventory().getTopInventory());

                index = (index + 1) % titles.size();
            }
        }.runTaskTimer(plugin, 0L, menu.getTitleUpdateInterval()).getTaskId();

        titleAnimationTasks.put(player, taskId);
    }

    private void stopTitleAnimation(Player player) {
        Integer taskId = titleAnimationTasks.remove(player);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void reload() {
        // Cancel all animations
        for (Integer taskId : titleAnimationTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        titleAnimationTasks.clear();
        openMenus.clear();
        loadMenus();
    }
}
