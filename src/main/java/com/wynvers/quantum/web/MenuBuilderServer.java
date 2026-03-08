package com.wynvers.quantum.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.menu.Menu;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Web server for the client-side menu builder interface
 * Provides REST API endpoints and serves the web UI
 */
public class MenuBuilderServer {

    private final Quantum plugin;
    private HttpServer server;
    private final Gson gson;
    private final int port;

    public MenuBuilderServer(Quantum plugin, int port) {
        this.plugin = plugin;
        this.port = port;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Start the web server
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            // Register API endpoints
            server.createContext("/api/menus", new MenusHandler());
            server.createContext("/api/menus/reload", new ReloadHandler());

            // Serve static files (HTML, CSS, JS)
            server.createContext("/", new StaticFileHandler());

            server.start();
            plugin.getQuantumLogger().success("Menu Builder web server started on port " + port);
            plugin.getQuantumLogger().info("Access the menu builder at: http://localhost:" + port);

        } catch (IOException e) {
            plugin.getQuantumLogger().error("Failed to start menu builder web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the web server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getQuantumLogger().info("Menu Builder web server stopped");
        }
    }

    /**
     * Handler for menu API endpoints
     */
    private class MenusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                // Extract menu ID from path if present
                String menuId = null;
                if (path.startsWith("/api/menus/") && path.length() > "/api/menus/".length()) {
                    menuId = path.substring("/api/menus/".length());
                }

                switch (method) {
                    case "GET":
                        if (menuId != null) {
                            handleGetMenu(exchange, menuId);
                        } else {
                            handleGetAllMenus(exchange);
                        }
                        break;
                    case "POST":
                        handleCreateMenu(exchange);
                        break;
                    case "PUT":
                        if (menuId != null) {
                            handleUpdateMenu(exchange, menuId);
                        } else {
                            sendError(exchange, 400, "Menu ID required for update");
                        }
                        break;
                    case "DELETE":
                        if (menuId != null) {
                            handleDeleteMenu(exchange, menuId);
                        } else {
                            sendError(exchange, 400, "Menu ID required for delete");
                        }
                        break;
                    default:
                        sendError(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error handling menu API request: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }

        private void handleGetAllMenus(HttpExchange exchange) throws IOException {
            List<Map<String, Object>> menusData = new ArrayList<>();

            File menusFolder = new File(plugin.getDataFolder(), "menus");
            if (menusFolder.exists() && menusFolder.isDirectory()) {
                File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                            Map<String, Object> menuData = new HashMap<>();
                            menuData.put("id", file.getName().replace(".yml", ""));
                            menuData.put("menu_title", config.getString("menu_title", config.getString("title", "Menu")));
                            menuData.put("size", config.getInt("size", 54));
                            menuData.put("open_command", config.getString("open_command", ""));

                            // Convert items section to map
                            Map<String, Object> items = new HashMap<>();
                            if (config.contains("items")) {
                                for (String key : config.getConfigurationSection("items").getKeys(false)) {
                                    items.put(key, config.getConfigurationSection("items." + key).getValues(false));
                                }
                            }
                            menuData.put("items", items);

                            menusData.add(menuData);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load menu file: " + file.getName());
                        }
                    }
                }
            }

            sendJson(exchange, 200, menusData);
        }

        private void handleGetMenu(HttpExchange exchange, String menuId) throws IOException {
            File menuFile = new File(plugin.getDataFolder(), "menus/" + menuId + ".yml");

            if (!menuFile.exists()) {
                sendError(exchange, 404, "Menu not found: " + menuId);
                return;
            }

            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(menuFile);
                Map<String, Object> menuData = new HashMap<>();
                menuData.put("id", menuId);
                menuData.put("menu_title", config.getString("menu_title", config.getString("title", "Menu")));
                menuData.put("size", config.getInt("size", 54));
                menuData.put("open_command", config.getString("open_command", ""));

                // Convert items section to map
                Map<String, Object> items = new HashMap<>();
                if (config.contains("items")) {
                    for (String key : config.getConfigurationSection("items").getKeys(false)) {
                        items.put(key, config.getConfigurationSection("items." + key).getValues(false));
                    }
                }
                menuData.put("items", items);

                sendJson(exchange, 200, menuData);
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to load menu: " + e.getMessage());
            }
        }

        private void handleCreateMenu(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Map<String, Object> menuData = gson.fromJson(requestBody, Map.class);

            String menuId = (String) menuData.get("id");
            if (menuId == null || menuId.trim().isEmpty()) {
                sendError(exchange, 400, "Menu ID is required");
                return;
            }

            File menuFile = new File(plugin.getDataFolder(), "menus/" + menuId + ".yml");
            if (menuFile.exists()) {
                sendError(exchange, 409, "Menu already exists: " + menuId);
                return;
            }

            try {
                saveMenuToFile(menuFile, menuData);

                // Reload menus
                plugin.getMenuManager().reload();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Menu created successfully");
                response.put("id", menuId);

                sendJson(exchange, 201, response);
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to create menu: " + e.getMessage());
            }
        }

        private void handleUpdateMenu(HttpExchange exchange, String menuId) throws IOException {
            String requestBody = readRequestBody(exchange);
            Map<String, Object> menuData = gson.fromJson(requestBody, Map.class);

            File menuFile = new File(plugin.getDataFolder(), "menus/" + menuId + ".yml");

            try {
                saveMenuToFile(menuFile, menuData);

                // Reload menus
                plugin.getMenuManager().reload();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Menu updated successfully");
                response.put("id", menuId);

                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to update menu: " + e.getMessage());
            }
        }

        private void handleDeleteMenu(HttpExchange exchange, String menuId) throws IOException {
            File menuFile = new File(plugin.getDataFolder(), "menus/" + menuId + ".yml");

            if (!menuFile.exists()) {
                sendError(exchange, 404, "Menu not found: " + menuId);
                return;
            }

            try {
                if (menuFile.delete()) {
                    // Reload menus
                    plugin.getMenuManager().reload();

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Menu deleted successfully");

                    sendJson(exchange, 200, response);
                } else {
                    sendError(exchange, 500, "Failed to delete menu file");
                }
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to delete menu: " + e.getMessage());
            }
        }

        private void saveMenuToFile(File file, Map<String, Object> menuData) throws IOException {
            YamlConfiguration config = new YamlConfiguration();

            config.set("menu_title", menuData.get("menu_title"));
            config.set("size", menuData.get("size"));
            config.set("open_command", menuData.get("open_command"));

            // Save items
            Map<String, Object> items = (Map<String, Object>) menuData.get("items");
            if (items != null) {
                for (Map.Entry<String, Object> entry : items.entrySet()) {
                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                    for (Map.Entry<String, Object> itemEntry : itemData.entrySet()) {
                        config.set("items." + entry.getKey() + "." + itemEntry.getKey(), itemEntry.getValue());
                    }
                }
            }

            // Ensure parent directory exists
            file.getParentFile().mkdirs();

            config.save(file);
        }
    }

    /**
     * Handler for reload endpoint
     */
    private class ReloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                plugin.getMenuManager().reload();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Menus reloaded successfully");

                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to reload menus: " + e.getMessage());
            }
        }
    }

    /**
     * Handler for static files (HTML, CSS, JS)
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Default to index.html
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Prevent directory traversal
            if (path.contains("..")) {
                sendError(exchange, 403, "Forbidden");
                return;
            }

            // Map to resource path
            String resourcePath = "web-menu-builder" + path;

            try (InputStream is = plugin.getResource(resourcePath)) {
                if (is == null) {
                    sendError(exchange, 404, "Not found");
                    return;
                }

                // Determine content type
                String contentType = getContentType(path);

                byte[] content = is.readAllBytes();

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error serving static file " + path + ": " + e.getMessage());
                sendError(exchange, 500, "Internal server error");
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (path.endsWith(".json")) return "application/json; charset=utf-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "application/octet-stream";
        }
    }

    /**
     * Send JSON response
     */
    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Send error response
     */
    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        sendJson(exchange, statusCode, error);
    }

    /**
     * Read request body as string
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
