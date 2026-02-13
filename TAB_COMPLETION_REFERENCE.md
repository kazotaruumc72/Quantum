# Tab Completion Reference

## Overview

This document provides a reference for the tab completion system in the Quantum plugin. The complete tab completion functions are documented in `tab.yml`.

## What is Tab Completion?

Tab completion (also known as auto-completion) allows players to press the TAB key while typing commands to see available options and automatically complete command arguments. This makes commands easier to use and helps players discover available features.

## File Structure

### `tab.yml`

This file documents all tab completion functions available in the Quantum plugin. It includes:

- **30 main commands** with their tab completers
- **Hierarchical argument completion** for multi-level commands
- **Dynamic completions** that fetch data in real-time
- **Permission-based filtering** for security
- **Complete documentation** of all arguments and options

## Command Categories

The tab completion functions are organized into the following categories:

### 1. Admin & Core Commands
- `/quantum` - Main command with extensive sub-commands
- Includes reload, stats, tower management, NPC management, and more

### 2. Tower Commands
- `/tower` - Tower progression system
- Supports progress tracking, teleportation, and admin functions

### 3. Armor & Equipment
- `/armor` - Dungeon armor management
- `/rune` - Rune item management
- `/weapon` - Dungeon weapon management
- `/tool` - Upgradeable tools (pickaxe, axe, hoe)

### 4. Storage & Inventory
- `/storage` - Virtual storage access
- `/qstorage` - Admin storage management

### 5. Jobs System
- `/job` - Player job management
- `/jobadmin` - Admin job management

### 6. Experience & Economy
- `/qexp` - Player experience management

### 7. Homes & Teleportation
- `/home` - Teleport to saved homes
- `/sethome` - Save home locations
- `/delhome` - Delete home locations
- `/spawn` - Teleport to spawn
- `/apartment` - Apartment management

### 8. UI & Display
- `/menu` - Custom menu system
- `/healthbar` - Health display settings
- `/qscoreboard` - Scoreboard visibility
- `/chat` - Chat system management
- `/tabedit` - TAB header/footer editor

### 9. Shop & Trading
- `/offre` - Create buy orders
- `/recherche` - Search for orders
- `/rechercher` - Browse order categories

### 10. Zone & Region
- `/zonegui` - Zone configuration interface
- `/zoneexit` - Force zone exit

### 11. Gamemode Shortcuts
- `/gmc` - Creative mode
- `/gms` - Survival mode
- `/gmsp` - Spectator mode
- `/gma` - Adventure mode

## Dynamic Completions

Many tab completers fetch suggestions dynamically from the server:

| Source | Description | Example |
|--------|-------------|---------|
| `tower_ids` | Available tower IDs | obsidian_tower, crystal_tower |
| `floor_numbers` | Floor numbers for a tower | 1, 2, 3, ... 25 |
| `player_names` | Online player names | Kazotaruu_, PlayerName |
| `job_ids` | Available job IDs | miner, farmer, blacksmith |
| `rune_types` | Rune type enum values | FIRE, WATER, EARTH, AIR |
| `order_items` | Items from orders system | All tradeable items |
| `player_homes` | Player's saved homes | home, base, farm |
| `tab_groups` | TAB display groups | elite, mvp+, mvp, vip+, vip |
| `worldguard_regions` | WorldGuard regions | spawn, arena, shop |
| `menu_names` | Available menus | main, shop, settings |

## Permission-Based Filtering

Tab completers automatically filter suggestions based on player permissions:

- **quantum.admin.orders** - Shows "orders" sub-command in `/quantum`
- **quantum.tower.progress.others** - Shows other player names in `/tower progress`
- **quantum.admin** - Shows admin-only sub-commands
- **quantum.tab.edit** - Required for `/tabedit` completions
- **quantum.zone.configure** - Required for `/zonegui` completions

This ensures players only see options they have permission to use.

## Features

### 1. Hierarchical Completion
Commands with multiple levels support nested tab completion:
```
/quantum tower tp <TAB>
  → obsidian_tower, crystal_tower, ...
/quantum tower tp obsidian_tower <TAB>
  → 1, 2, 3, 4, 5, ...
```

### 2. Case-Insensitive Matching
Most completers accept input in any case:
```
/quantum relo<TAB> → reload
/quantum RELO<TAB> → reload
```

### 3. Partial Matching
Tab completion filters results based on what you've typed:
```
/quantum st<TAB> → stats, statistics, storagestats
/quantum sto<TAB> → storagestats
```

### 4. Placeholder Text
Some completions show placeholder text to guide users:
```
/rune give <TAB> → FIRE, WATER, <model_id>, ...
```

### 5. Context-Aware Suggestions
Completions adapt based on previous arguments:
```
/armor equip <TAB> → helmet, chestplate, leggings, boots
/armor equip helmet <TAB> → common, uncommon, rare, epic, legendary
```

## Implementation

The tab completion system is implemented through dedicated TabCompleter classes:

### Tab Completer Files

Located in these packages:
- `com.wynvers.quantum.tabcompleters.*` - Main tab completers
- `com.wynvers.quantum.commands.*TabCompleter` - Command-specific completers
- `com.wynvers.quantum.jobs.*TabCompleter` - Job system completers

### Key Implementation Classes

1. **QuantumTabCompleter** - Main `/quantum` command
2. **TowerTabCompleter** - Tower system commands
3. **JobTabCompleter** - Job system commands
4. **TabEditCommand** - TAB system editor (includes tab completion)
5. And 20+ other specialized completers

## Usage Examples

### Example 1: Creating a Buy Order
```
/offre <TAB>
  → Shows all available items from orders_template.yml
/offre wheat <TAB>
  → 1, 8, 16, 32, 64, 128, 256 (quantity suggestions)
/offre wheat 64 <TAB>
  → 10, 50, 100, 500, 1000 (price suggestions)
```

### Example 2: Teleporting to Tower Floor
```
/quantum tower tp <TAB>
  → obsidian_tower, crystal_tower (available towers)
/quantum tower tp obsidian_tower <TAB>
  → 1, 2, 3, 4, 5 (available floors)
```

### Example 3: Managing Jobs
```
/job select <TAB>
  → miner, farmer, blacksmith (available jobs)
/jobadmin set <TAB>
  → Kazotaruu_, Player2, Player3 (online players)
/jobadmin set Kazotaruu_ <TAB>
  → miner, farmer, blacksmith (available jobs)
```

## Benefits

1. **Improved User Experience** - Players can discover commands without reading documentation
2. **Reduced Errors** - Auto-completion prevents typos and invalid arguments
3. **Faster Command Entry** - Quick completion saves typing time
4. **Discovery** - Players learn about available options through tab completion
5. **Guidance** - Placeholder text and suggestions guide users through complex commands

## Maintenance

When adding new commands or modifying existing ones:

1. Create or update the corresponding TabCompleter class
2. Register the tab completer in `plugin.yml` or in the main plugin class
3. Update `tab.yml` to document the new completion functions
4. Test tab completion with various permission levels
5. Ensure dynamic completions fetch data correctly

## Related Files

- **tab.yml** - Complete tab completion function documentation
- **plugin.yml** - Command definitions and permissions
- **src/main/java/com/wynvers/quantum/tabcompleters/** - Tab completer implementations

## Notes

- All tab completers are registered when the plugin loads
- Dynamic data is fetched in real-time (no caching)
- Tab completers handle null/error cases gracefully
- Console commands show appropriate player selection
- Tab completion works with command aliases

---

For detailed information about each command's tab completion options, see `tab.yml`.
