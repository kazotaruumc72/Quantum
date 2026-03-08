# Quantum Menu Builder - Client-Side Web Interface

## Overview

The Quantum Menu Builder is a client-side web interface that allows administrators to visually create and edit menus for the Quantum plugin without manually editing YAML files.

## Features

- **Visual Menu Editor**: Drag-and-drop interface for creating menus
- **Real-time Preview**: See your menu as you build it
- **Item Configuration**: Full control over menu items including materials, lore, actions, and button types
- **YAML Generation**: Automatically generates proper YAML configuration files
- **Hot Reload**: Changes are immediately available in-game after saving
- **No Client Mod Required**: Works entirely in a web browser

## Accessing the Interface

1. **Start your Minecraft server** with the Quantum plugin installed
2. **Open your web browser** and navigate to:
   ```
   http://localhost:8080
   ```
   (Replace `8080` with your configured port if different)

3. **You should see** the Menu Builder interface with a list of existing menus

## Configuration

Edit `plugins/Quantum/config.yml` to configure the menu builder:

```yaml
menu-builder:
  # Enable the web interface (default: true)
  enabled: true

  # Port for the web server (default: 8080)
  port: 8080

  # Admin token for API access (optional, leave empty to disable)
  admin-token: "your-secure-token-here"
```

### Configuration Options

- **enabled**: Set to `false` to disable the web interface entirely
- **port**: The HTTP port for the web server (must not conflict with other services)
- **admin-token**: Optional bearer token for API authentication (recommended for production)

## Using the Menu Builder

### Creating a New Menu

1. Click the **"+ New Menu"** button in the sidebar
2. Enter a unique **Menu ID** (e.g., `my_custom_menu`)
3. The editor will open with a blank menu

### Configuring Menu Properties

At the top of the editor, you can configure:

- **Menu Title**: The display title (supports color codes with `&` or MiniMessage with `<>`)
- **Size**: Number of slots (9, 18, 27, 36, 45, or 54)
- **Open Command**: The command to open this menu (e.g., `/mymenu`)

### Adding Menu Items

1. **Click on a slot** in the inventory grid
2. The **Item Editor Modal** will open
3. Configure the item properties:

#### Basic Properties

- **Slot(s)**: Which slots this item appears in
  - Single slot: `0`
  - Multiple slots: `0,1,2`
  - Range: `0-8`
- **Material**: Minecraft material name (e.g., `DIAMOND`, `STONE`)
- **Nexo Item ID**: Custom item ID from Nexo (optional)
- **Display Name**: The item's name (supports color codes)
- **Lore**: Item description (one line per row)

#### Advanced Properties

- **Button Type**: Special quantum button types
  - `QUANTUM_CHANGE_MODE`: Switch storage modes
  - `QUANTUM_STORAGE_SELL_ALL`: Sell all items
  - `QUANTUM_CHANGE_AMOUNT`: Adjust quantities
  - And many more...

- **Type**: Special slot types
  - `quantum_storage`: Dynamic storage display
  - `quantum_tower_storage`: Tower storage display
  - `quantum_orders_item`: Order listings
  - `quantum_sell_item`: Sell session item

- **Click Actions**: Commands to execute on click
  - Left Click Actions: Executed on left click
  - Right Click Actions: Executed on right click

#### Action Syntax

Actions use the format: `[ACTION_TYPE] value`

Common actions:
- `[close]` - Close the menu
- `[menu] other_menu` - Open another menu
- `[message] &aHello!` - Send a message to the player
- `[console] give %player% diamond 1` - Run a console command
- `[sound] ENTITY_PLAYER_LEVELUP` - Play a sound
- `[refresh]` - Refresh the current menu

#### Visual Effects

- **Glow Effect**: Makes the item glow
- **Custom Model Data**: For resource pack custom models

### Saving Your Menu

1. Click the **"Save Menu"** button at the top
2. The menu will be saved to `plugins/Quantum/menus/{menu_id}.yml`
3. The plugin will automatically reload all menus
4. Your menu is now available in-game!

### Deleting a Menu

1. Select the menu you want to delete
2. Click the **"Delete Menu"** button
3. Confirm the deletion
4. The menu file will be removed and menus will reload

## API Reference

The menu builder provides a REST API for programmatic access:

### Endpoints

#### GET /api/menus
Get all menus

**Response:**
```json
[
  {
    "id": "storage",
    "menu_title": "&fQuantum Storage",
    "size": 54,
    "open_command": "storage",
    "items": { ... }
  }
]
```

#### GET /api/menus/{menuId}
Get a specific menu

**Response:**
```json
{
  "id": "storage",
  "menu_title": "&fQuantum Storage",
  "size": 54,
  "open_command": "storage",
  "items": { ... }
}
```

#### POST /api/menus
Create a new menu

**Request Body:**
```json
{
  "id": "my_menu",
  "menu_title": "&6My Menu",
  "size": 54,
  "open_command": "mymenu",
  "items": { ... }
}
```

#### PUT /api/menus/{menuId}
Update an existing menu

**Request Body:**
```json
{
  "menu_title": "&6Updated Title",
  "size": 54,
  "open_command": "mymenu",
  "items": { ... }
}
```

#### DELETE /api/menus/{menuId}
Delete a menu

**Response:**
```json
{
  "success": true,
  "message": "Menu deleted successfully"
}
```

#### POST /api/menus/reload
Reload all menus

**Response:**
```json
{
  "success": true,
  "message": "Menus reloaded successfully"
}
```

### Authentication

If `admin-token` is set in the config, include it in API requests:

```
Authorization: Bearer your-secure-token-here
```

## Security Considerations

### Local Development
- By default, the web interface is accessible on `localhost` only
- Safe for single-player or local testing

### Production Servers
1. **Enable Authentication**: Set a strong `admin-token` in config.yml
2. **Use a Reverse Proxy**: Place behind nginx/Apache with HTTPS
3. **Firewall**: Restrict access to trusted IP addresses
4. **Disable When Not Needed**: Set `enabled: false` in config.yml

### Recommended Setup for Production

```yaml
menu-builder:
  enabled: true
  port: 8080
  admin-token: "use-a-long-random-secure-token-here-32-chars-minimum"
```

Then use a reverse proxy like nginx:

```nginx
location /menu-builder/ {
    auth_basic "Menu Builder";
    auth_basic_user_file /etc/nginx/.htpasswd;
    proxy_pass http://localhost:8080/;
}
```

## Troubleshooting

### Port Already in Use
If you see `Address already in use`, another application is using port 8080:
1. Change the port in config.yml
2. Restart the server

### Web Interface Not Loading
1. Check that `enabled: true` in config.yml
2. Check server logs for errors
3. Verify your firewall isn't blocking the port
4. Try accessing from `http://127.0.0.1:8080` instead of `localhost`

### Changes Not Appearing In-Game
1. Click "Save Menu" in the web interface
2. The server automatically reloads menus
3. If still not working, run `/quantum reload` in-game

### Cannot Create/Edit Menus
1. Check file permissions on `plugins/Quantum/menus/` directory
2. Ensure the server has write access
3. Check server logs for errors

## Advanced Usage

### Importing Existing Menus
Existing menu YAML files in `plugins/Quantum/menus/` are automatically loaded and can be edited through the web interface.

### Exporting Menus
Menu changes are saved directly to YAML files in `plugins/Quantum/menus/`, which can be copied to other servers.

### Batch Operations
Use the REST API with scripts to batch create or modify menus:

```bash
# Example: Create multiple menus
curl -X POST http://localhost:8080/api/menus \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d @menu1.json

curl -X POST http://localhost:8080/api/menus \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d @menu2.json
```

## Technical Details

### Architecture
- **Backend**: Lightweight HTTP server using Java's built-in `HttpServer`
- **Frontend**: Vanilla HTML/CSS/JavaScript (no build tools required)
- **Storage**: Direct YAML file manipulation
- **Hot Reload**: Automatic menu manager reload after changes

### Supported Menu Features
- All standard Minecraft materials
- Nexo custom items
- Placeholder expansion (PlaceholderAPI)
- Custom model data
- Item glow effects
- All Quantum button types
- Dynamic content (storage, orders, etc.)
- Action system (commands, menu navigation, etc.)
- View and click requirements

### Browser Compatibility
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Any modern browser with ES6 support

## Examples

### Simple Navigation Menu

```yaml
menu_title: '&6Navigation Menu'
size: 27
open_command: nav

items:
  storage_button:
    slot: 10
    material: CHEST
    display_name: '&eOpen Storage'
    lore:
      - '&7Click to open your storage'
    left_click_actions:
      - '[menu] storage'
      - '[sound] UI_BUTTON_CLICK'

  shop_button:
    slot: 12
    material: EMERALD
    display_name: '&aShop'
    left_click_actions:
      - '[menu] shop'

  close_button:
    slot: 22
    material: BARRIER
    display_name: '&cClose'
    left_click_actions:
      - '[close]'
```

### Dynamic Storage Menu

```yaml
menu_title: '&fQuantum Storage'
size: 54
open_command: storage

items:
  storage_slots:
    type: quantum_storage
    slots: [9-44]
    lore_append:
      - '&7Qty: %quantity%'
      - '&eClick to withdraw'

  mode_button:
    slot: 0
    material: LIME_WOOL
    display_name: '&aMode: Storage'
    button_type: QUANTUM_CHANGE_MODE
    mode: STORAGE
```

## Support

For issues, questions, or feature requests:
1. Check the [GitHub Issues](https://github.com/kazotaruumc72/Quantum/issues)
2. Ask in the server's Discord
3. Consult the main Quantum plugin documentation

## License

This menu builder is part of the Quantum plugin and follows the same license.
