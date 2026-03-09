# Wheads API Integration

This document describes the integration of the Wheads player heads API into the Quantum plugin.

## Overview

The Wheads integration allows Quantum to display player heads from the Wheads plugin in a custom menu. Players can view, get information about, and receive player heads through the `/wheads` command.

## Features

- **Dynamic Player Head Display**: Automatically fetches and displays player heads from the Wheads plugin
- **Interactive Menu**: Players can interact with heads using left-click and right-click
- **Permission-Based Access**: Fine-grained permissions control who can view and obtain heads
- **Seamless Integration**: Works alongside existing Quantum menu systems

## Components

### 1. Data Model

**`WheadsPlayerHead.java`**
- Represents a player head with all necessary metadata
- Stores player name, UUID, texture value, and texture signature
- Includes timestamp for tracking when heads were fetched

### 2. API Integration

**`WheadsAPI.java`**
- Manages connection to the Wheads plugin
- Detects if Wheads is installed and enabled
- Provides methods to fetch player heads:
  - `getAllPlayerHeads()` - Get all available heads
  - `getPlayerHeadsByCategory(String category)` - Get heads by category
  - `searchPlayerHeads(String query)` - Search for specific heads
  - `getPlayerHead(String uuid)` - Get a specific head by UUID

### 3. Rendering System

**`WheadsHeadsRenderer.java`**
- Renders player heads into menu slots
- Similar architecture to `StorageRenderer` and `TowerStorageRenderer`
- Creates ItemStack player heads with custom textures
- Stores head metadata in PersistentDataContainer for click handling
- Supports lore customization via `lore_append` configuration

### 4. Menu Configuration

**`wheads_heads.yml`**
- Defines the wheads menu layout
- Command: `/wheads`
- 36 slots for player heads (rows 2-5)
- Border decoration and close button
- Configurable lore with placeholders:
  - `%head_owner%` - Player name
  - `%head_uuid%` - Player UUID

### 5. Event Handling

**`MenuListener.java` - `handleWheadsMenu()`**
- Processes clicks on player heads
- **Left-click**: Gives the player the head (requires `quantum.wheads.get` permission)
- **Right-click**: Shows head information (player name and UUID)
- Properly handles static buttons (close, navigation, etc.)

### 6. Menu Integration

**Updated Files:**
- `Menu.java` - Added `wheadsHeadsRenderer` and rendering logic
- `MenuItem.java` - Added `isWheadsPlayerHead()` method
- `ButtonType.java` - Added wheads button types:
  - `WHEADS_HEAD_CLICK`
  - `WHEADS_NEXT_PAGE`
  - `WHEADS_PREV_PAGE`
  - `WHEADS_SEARCH`
  - `WHEADS_FILTER_CATEGORY`

### 7. Plugin Initialization

**`Quantum.java`**
- Initializes `WheadsAPI` on plugin enable
- Extracts `wheads_heads.yml` menu resource
- Provides getter method: `getWheadsAPI()`

**`plugin.yml`**
- Added `Wheads` to `softdepend` list

## Configuration

### Two Configuration Methods

Quantum supports two methods for displaying player heads in menus:

#### Method 1: Material-Based (Recommended for Simplicity)

Use standard `material: PLAYER_HEAD` with `skull_owner` for specific player heads. This method is simpler and doesn't require the Wheads plugin.

**Example:**
```yaml
example_head:
  slot: 9
  material: PLAYER_HEAD
  skull_owner: 'Notch'  # Player name or PlaceholderAPI placeholder
  display_name: '&e&lNotch'
  lore:
    - '&7Player: &fNotch'
    - ' '
    - '&e► Left-click to get head'
  left_click_actions:
    - '[give] minecraft:player_head{SkullOwner:"Notch"} 1'
    - '[message] &aVous avez reçu la tête de Notch!'
```

**Features:**
- ✅ Simple configuration
- ✅ No external plugin required
- ✅ Supports PlaceholderAPI placeholders (`%player%`, etc.)
- ✅ Works with standard menu actions
- ❌ Each head needs individual configuration

**Using Placeholders:**
```yaml
player_own_head:
  slot: 10
  material: PLAYER_HEAD
  skull_owner: '%player%'  # Shows current player's head
  display_name: '&b&l%player%'
  lore:
    - '&7Your own head!'
    - '&7Player: &f%player%'
```

#### Method 2: Dynamic API-Based (For Wheads Integration)

Use `type: wheads_player_head` for dynamic rendering from the Wheads plugin API.

**Example:**
```yaml
player_heads_slots:
  slots: [9, 10, 11, ..., 44]  # Multiple slots filled dynamically
  type: wheads_player_head
  lore_append:
    - ' '
    - '&7Player: &f%head_owner%'
    - '&7UUID: &8%head_uuid%'
    - ' '
    - '&e► Left-click to get head'
    - '&e► Right-click for info'
```

**Features:**
- ✅ Multiple heads from single configuration
- ✅ Automatically fetches heads from Wheads API
- ✅ Dynamic lore with head-specific placeholders
- ✅ Special click handling via MenuListener
- ❌ Requires Wheads plugin installed
- ❌ More complex setup

### Menu Configuration

The wheads menu is configured in `plugins/Quantum/menus/wheads_heads.yml`:

```yaml
menu_title: '&6&lWheads - Player Heads'
size: 54
open_command: wheads

items:
  player_heads_slots:
    slots: [9, 10, 11, ..., 44]  # 36 slots for heads
    type: wheads_player_head
    lore_append:
      - ' '
      - '&7Player: &f%head_owner%'
      - '&7UUID: &8%head_uuid%'
      - ' '
      - '&e► Left-click to get head'
      - '&e► Right-click for info'
```

### Permissions

- `quantum.wheads.view` - View the wheads menu (opens with `/wheads`)
- `quantum.wheads.get` - Obtain player heads by clicking on them

## Usage

### For Players

1. **Open the menu**: `/wheads`
2. **View heads**: Browse through available player heads
3. **Get a head**: Left-click on a head (requires permission)
4. **View info**: Right-click on a head to see details

### For Administrators

1. **Installation**:
   - Install the Wheads plugin on your server
   - Restart the server (Quantum will auto-detect Wheads)

2. **Configuration**:
   - Edit `menus/wheads_heads.yml` to customize the menu
   - Modify lore templates, menu title, and layout

3. **Permissions**:
   - Grant `quantum.wheads.view` to players who should access the menu
   - Grant `quantum.wheads.get` to players who can obtain heads

## Architecture

### Integration Pattern

The Wheads integration follows the same architectural pattern as the existing Storage and Tower Storage systems:

```
Menu.yml (Configuration)
    ↓
MenuItem (type: wheads_player_head)
    ↓
Menu.populateInventory()
    ↓
WheadsHeadsRenderer.renderPlayerHeads()
    ↓
WheadsAPI.getAllPlayerHeads()
    ↓
Wheads Plugin API
```

### Click Handling Flow

```
Player clicks head
    ↓
MenuListener.onInventoryClick()
    ↓
MenuListener.handleWheadsMenu()
    ↓
Check PDC for head UUID/name
    ↓
Execute action (give head or show info)
```

## API Integration Notes

The `WheadsAPI.java` class contains placeholder code for the actual Wheads plugin API integration:

```java
// TODO: Integrate with actual Wheads API when available
// For now, this is a placeholder that would call Wheads plugin methods
// Example: heads = wheadsPlugin.getHeadsManager().getAllHeads();
```

To complete the integration, you'll need to:

1. Examine the Wheads plugin API documentation
2. Replace the placeholder methods with actual Wheads API calls
3. Map Wheads head data to `WheadsPlayerHead` objects

## Future Enhancements

Potential improvements for future versions:

1. **Pagination**: Add next/previous page buttons for large head collections
2. **Search**: Implement head search functionality
3. **Categories**: Support Wheads categories if available
4. **Caching**: Cache head data to reduce API calls
5. **Custom Textures**: Support for custom texture URLs
6. **Head Preview**: Show larger preview on hover
7. **Favorites**: Allow players to mark favorite heads

## Troubleshooting

### Wheads Not Detected

If Quantum doesn't detect Wheads:
1. Verify Wheads is installed in `plugins/` folder
2. Check server logs for Wheads startup messages
3. Ensure Wheads loads before or with Quantum (check load order)
4. Restart the server completely

### Menu Not Opening

If `/wheads` doesn't work:
1. Check `menus/wheads_heads.yml` exists
2. Verify the menu loaded: check logs for "Loading menu: wheads_heads"
3. Grant player `quantum.wheads.view` permission
4. Try reloading Quantum: `/quantum reload`

### No Heads Displayed

If the menu opens but shows no heads:
1. Verify Wheads API integration is complete (see API Integration Notes)
2. Check server console for errors from WheadsAPI
3. Ensure Wheads has heads available
4. Enable debug logging to see API calls

## Technical Details

### Dependencies

- **Paper/Spigot**: 1.21.11+
- **Java**: 21+
- **Wheads Plugin**: Required (softdepend)
- **Quantum**: 1.0.1+

### Files Modified

- `src/main/java/com/wynvers/quantum/Quantum.java`
- `src/main/java/com/wynvers/quantum/menu/Menu.java`
- `src/main/java/com/wynvers/quantum/menu/MenuItem.java`
- `src/main/java/com/wynvers/quantum/menu/ButtonType.java`
- `src/main/java/com/wynvers/quantum/listeners/MenuListener.java`
- `src/main/resources/plugin.yml`

### Files Created

- `src/main/java/com/wynvers/quantum/wheads/WheadsPlayerHead.java`
- `src/main/java/com/wynvers/quantum/wheads/WheadsAPI.java`
- `src/main/java/com/wynvers/quantum/wheads/WheadsHeadsRenderer.java`
- `src/main/resources/menus/wheads_heads.yml`

## Credits

- **Integration Design**: Based on Quantum's existing renderer pattern
- **Menu System**: Leverages Quantum's dynamic menu builder
- **Wheads Plugin**: External dependency for player head data

---

For more information about Quantum's menu system, see [MENU_BUILDER.md](MENU_BUILDER.md).
