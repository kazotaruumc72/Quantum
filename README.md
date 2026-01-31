# Quantum

**Advanced Minecraft Plugin with Virtual Storage & Dynamic GUI Builder**

## 🌟 Features

### 📦 Virtual Storage System
- **Unlimited capacity** per material type
- **Animated titles** with customizable frames
- **Nexo item support** (custom items, models, textures)
- **Database storage** (MySQL or SQLite)
- **PlaceholderAPI integration** for dynamic content

### 📝 Dynamic GUI Builder (DeluxeMenus-style)
- **YAML configuration** for menus
- **Animated titles** per menu
- **Custom items** with Nexo support
- **Click actions**:
  - Left/Right click support
  - Multiple action types: `[message]`, `[console]`, `[player]`, `[close]`, `[menu]`, `[sound]`
- **Requirements system**:
  - Permission checks
  - PlaceholderAPI conditions
  - Custom requirements
- **Item properties**:
  - Display name & lore
  - Material or Nexo item
  - Amount
  - Skull owner (player heads)
  - Glyph support
  - Custom model data

### 🔧 Technical Features
- Java 21
- Paper 1.21.4+
- Thread-safe operations
- Async database operations
- Hot-reload configuration

## 📚 Menu Configuration

### Basic Structure
```yaml
menu_title: '&6&lMy Menu'
open_command: mymenu
size: 54

items:
  'item_id':
    slot: 13
    material: DIAMOND
    display_name: '&b&lClick Me!'
    lore:
      - '&7Line 1'
      - '&7Line 2'
    
    left_click:
      actions:
        - '[message] Hello!'
        - '[console] give %player% diamond 1'
```

### Action Types

| Action | Description | Example |
|--------|-------------|----------|
| `[message]` | Send message to player | `[message] &aHello!` |
| `[console]` | Execute console command | `[console] give %player% diamond 1` |
| `[player]` | Execute as player | `[player] spawn` |
| `[close]` | Close current menu | `[close]` |
| `[menu]` | Open another menu | `[menu] shop` |
| `[sound]` | Play sound | `[sound] ENTITY_PLAYER_LEVELUP` |

### Requirement Types

| Type | Description | Example |
|------|-------------|----------|
| `permission:` | Check permission | `permission:quantum.vip` |
| `placeholder:` | PlaceholderAPI condition | `placeholder:%player_level% >= 10` |

### Nexo Integration

```yaml
items:
  'custom_item':
    slot: 22
    nexo_item: 'your_nexo_id'
    # Nexo properties automatically applied!
```

## 🔧 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/quantum` | Main command | `quantum.admin` |
| `/quantum reload` | Reload configuration | `quantum.admin` |
| `/storage` | Open storage | `quantum.storage` |
| `/menu <name>` | Open custom menu | `quantum.menu` |

## ⚙️ Configuration

**config.yml** - Main configuration
**menus/*.yml** - Menu definitions

## 🛠️ Installation

1. Download **Quantum.jar**
2. Place in `plugins/` folder
3. **(Optional)** Install **PlaceholderAPI**
4. **(Optional)** Install **Nexo** for custom items
5. Restart server
6. Configure in `plugins/Quantum/`

## 💻 API Usage

Coming soon!

## 🐛 Support

Create an issue on GitHub!

## 📜 License

All rights reserved - © 2026 Robie
