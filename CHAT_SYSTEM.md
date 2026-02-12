# Quantum Chat System Documentation

## Overview

The Quantum Chat System provides a complete formatted chat solution with support for:
- Custom chat formats based on permissions
- MiniMessage and Legacy color code support
- Placeholder integration
- Permission-based formatting
- Color permission control

## Features

### 🎨 Custom Chat Formats

The chat system supports multiple chat formats that can be assigned based on player permissions:

- **Admin Format** (`quantum.chat.format.admin`) - Special gradient format for administrators
- **Moderator Format** (`quantum.chat.format.mod`) - Special format for moderators
- **VIP Format** (`quantum.chat.format.vip`) - Special format for VIP players
- **Player Format** (`quantum.chat.format.player`) - Default format for regular players

### 🎯 Format Priority

Formats are applied in the following priority order (highest to lowest):
1. Admin
2. Moderator
3. VIP
4. Player (Default)

### 📝 Available Placeholders

The following placeholders can be used in chat formats:

- `%player%` - Player's username
- `%display_name%` - Player's display name
- `%message%` - The actual chat message
- `%rank%` - Player's rank (based on permissions)
- `%level%` - Player's level (from PlaceholderManager)

## Configuration

### messages.yml

The chat configuration is located in `messages.yml` under the `chat` section:

```yaml
chat:
  # General chat format (default)
  format: "<gray>[</gray><gradient:#32b8c6:#1d6880>%rank%</gradient><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> <gray>%message%</gray>"
  
  # Permission-based formats
  formats:
    admin: "<gray>[</gray><gradient:#ff0000:#ff6b00>Admin</gradient><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> %message%"
    mod: "<gray>[</gray><gradient:#00ff00:#00aa00>Modo</gradient><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> %message%"
    vip: "<gray>[</gray><gradient:#ffff00:#ffaa00>VIP</gradient><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> %message%"
    player: "<gray>[</gray><aqua>Joueur</aqua><gray>]</gray> <white>%player%</white> <dark_gray>»</dark_gray> <gray>%message%</gray>"
  
  # Default rank if no permission is found
  default-rank: "Joueur"
```

### Customizing Formats

You can customize the chat formats using:
- **MiniMessage syntax**: `<gradient:#32b8c6:#1d6880>Text</gradient>`, `<green>Text</green>`
- **Legacy color codes**: `&a`, `&b`, `&c`, etc.

## Permissions

### Format Permissions

- `quantum.chat.format.admin` - Use admin chat format
- `quantum.chat.format.mod` - Use moderator chat format
- `quantum.chat.format.vip` - Use VIP chat format
- `quantum.chat.format.player` - Use player chat format (default)

### Color Permission

- `quantum.chat.color` - Allows players to use color codes in their messages
  - Without this permission, all color codes are escaped

### Admin Permissions

- `quantum.chat.admin` - Access to all chat admin commands
- `quantum.chat.reload` - Reload chat configuration

## Commands

### /chat reload

Reloads the chat configuration from `messages.yml`.

**Permission:** `quantum.chat.reload` or `quantum.chat.admin`

**Usage:**
```
/chat reload
```

## Examples

### Example 1: Basic Setup

1. Give players the default format permission:
   ```
   /lp group default permission set quantum.chat.format.player true
   ```

2. Give VIP players color permission and VIP format:
   ```
   /lp group vip permission set quantum.chat.color true
   /lp group vip permission set quantum.chat.format.vip true
   ```

3. Give moderators the mod format:
   ```
   /lp group mod permission set quantum.chat.format.mod true
   ```

### Example 2: Using Colors in Chat

Players with `quantum.chat.color` permission can use:
- MiniMessage: `<red>Hello</red> <green>World</green>`
- Legacy codes: `&aHello &bWorld`

### Example 3: Custom Format

Create a custom format in `messages.yml`:

```yaml
chat:
  formats:
    custom: "<dark_gray>[<gold>★</gold>]</dark_gray> <yellow>%player%</yellow> <dark_gray>→</dark_gray> %message%"
```

Then grant the permission:
```
/lp user PlayerName permission set quantum.chat.format.custom true
```

## Integration

### PlaceholderAPI Integration

The chat system integrates with Quantum's PlaceholderManager to display:
- Player levels
- Job information
- Custom placeholders

### LuckPerms Integration

Use LuckPerms to manage chat format permissions:
```
/lp group vip parent add default
/lp group vip permission set quantum.chat.format.vip true
/lp group vip permission set quantum.chat.color true
```

## Troubleshooting

### Chat format not applying

1. Check if the player has the correct permission:
   ```
   /lp user <player> permission check quantum.chat.format.<format>
   ```

2. Reload the chat configuration:
   ```
   /chat reload
   ```

3. Verify the format exists in `messages.yml`

### Colors not working

- Players need `quantum.chat.color` permission to use colors
- Check if the format in `messages.yml` supports colors (no escaping)

### Permission priority issues

Remember the priority order:
1. Admin (highest priority)
2. Mod
3. VIP
4. Player (lowest priority)

If a player has multiple format permissions, the highest priority format will be used.

## Advanced Usage

### Using with TAB Plugin

Combine with TAB plugin for complete chat experience:
1. Configure TAB for nametags and tablist
2. Use Quantum Chat for in-game chat formatting
3. Both systems can share PlaceholderAPI placeholders

### Custom Rank Names

Modify the rank names in your chat formats:

```yaml
chat:
  formats:
    admin: "<gray>[</gray><red>ADMIN</red><gray>]</gray> ..."
    vip: "<gray>[</gray><gold>VIP+</gold><gray>]</gray> ..."
```

## API Usage

### Getting the ChatManager

```java
Quantum plugin = Quantum.getInstance();
ChatManager chatManager = plugin.getChatManager();
```

### Formatting a Message Programmatically

```java
Player player = ...;
String message = "Hello World!";
Component formatted = chatManager.formatChatMessage(player, message);
player.sendMessage(formatted);
```

### Checking Color Permission

```java
if (chatManager.canUseColors(player)) {
    // Player can use colors
}
```

---

**Made with ❤️ for the Minecraft community**

*Quantum Chat System - Part of the Quantum Advanced Plugin*
