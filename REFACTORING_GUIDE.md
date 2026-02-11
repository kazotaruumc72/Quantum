# Message Refactoring Guide - Quantum Plugin

## Overview
This guide documents the refactoring process to centralize all hardcoded messages to `messages.yml` and implement MiniMessage formatting across the Quantum plugin.

## Completed Work

### Files Refactored (8 files)
1. ✅ **OrderCreationManager.java** - Order creation system with escrow
2. ✅ **StorageListener.java** - Storage operations (sell/withdraw/order creation)
3. ✅ **ToolCommand.java** - Tool management and info display
4. ✅ **HealthBarCommand.java** - Health bar display mode switching
5. ✅ **WeaponCommand.java** - Dungeon weapon management
6. ✅ **StorageCommand.java** - Storage menu access
7. ✅ **MenuCommand.java** - Menu system
8. ✅ **ZoneExitCommand.java** - Legacy command (disabled)

### Message Sections Added to messages.yml
- **tools** - Pickaxe, axe, hoe messages with info display
- **healthbar** - Mode change messages (percentage/hearts)
- **storage-advanced** - Withdraw, sell, inventory operations
- **order-creation** - Order creation workflow messages
- **weapons** - Dungeon weapon management
- **armor** - Dungeon armor management (structure ready)
- **towers** - Tower system messages (structure ready)
- **commands** - General command messages

## Refactoring Pattern

### Before (Hardcoded)
```java
player.sendMessage("§cError message here");
player.sendMessage("§aSuccess: §e" + value + "§a!");
```

### After (Using MessageManager)
```java
// Simple message
plugin.getMessageManager().sendMessage(player, "section.message-key");

// Message with placeholders
Map<String, String> placeholders = new HashMap<>();
placeholders.put("value", String.valueOf(value));
plugin.getMessageManager().sendMessage(player, "section.message-key", placeholders);

// When reusing placeholders map, clear between uses
placeholders.clear();
placeholders.put("other_value", otherValue);
plugin.getMessageManager().sendMessage(player, "section.other-message", placeholders);
```

### Important Notes
1. **Player-only checks**: Console messages can stay hardcoded (plain text)
2. **Placeholder format**: Use `%key%` in messages.yml
3. **MiniMessage format**: Use `<green>`, `<red>`, `<gradient:...>` tags
4. **Legacy support**: MessageManager auto-detects and supports `&` codes
5. **Map reuse**: Always clear() placeholder maps before reusing

## Remaining Work

### High Priority Files (200+ messages)
1. **QuantumTowerCommand.java** - 133 hardcoded messages
   - Tower management, teleportation, spawn/door setup
   - Complex UI with many status messages
   
2. **ArmorCommand.java** - 239 lines with many messages
   - Armor creation, rune management
   - Info display for equipped armor

3. **StatsCommand.java** - 46 messages
   - Player statistics display
   - Multiple formatted stat sections

### Medium Priority Files (15-42 messages)
4. **QuantumStorageCommand.java** - 42 messages
5. **QexpCommand.java** - 22 messages
6. **StorageStatsCommand.java** - 15 messages
7. **TradingStatisticsManager.java** - 15 messages

### Remaining Files (~26 files)
- **Armor System**: ArmorGUI.java, ChestListener.java, RuneApplyListener.java, RuneItem.java, DungeonArmor.java
- **Tower System**: TowerNPCManager.java, TowerDoorManager.java, TowerManager.java, TowerCommand.java
- **Orders**: OrderButtonHandler.java, OrderAcceptanceHandler.java, OrderPaginationHandler.java, OrderMenuHandler.java
- **Levels**: PlayerLevelManager.java
- **Listeners**: MenuListener.java, TowerKillListener.java, DoorSelectionListener.java, SpawnSelectionListener.java
- **Storage**: StorageUpgradeManager.java, StorageFilterHandler.java
- **Other**: ButtonHandler.java, SellManager.java, ActionExecutor.java, etc.

## Messages.yml Structure

### Current Sections
```yaml
system:           # General system messages
sell:             # Item selling
research:         # Buy order creation
storage:          # Storage operations
orders:           # Order system (seller/buyer)
offre:            # /offre command
quantum:          # Admin commands
error:            # Error messages
info:             # Informational messages
tools:            # Tool commands ✅ NEW
healthbar:        # Health bar modes ✅ NEW
storage-advanced: # Advanced storage ✅ NEW
order-creation:   # Order creation ✅ NEW
weapons:          # Weapon system ✅ NEW
armor:            # Armor system ✅ NEW
towers:           # Tower system ✅ NEW
commands:         # General commands ✅ NEW
```

### Adding New Messages

1. **Identify the category** - Find or create appropriate section
2. **Choose a key** - Use kebab-case: `section.message-key`
3. **Write the message** - Use MiniMessage format
4. **Add placeholders** - Use `%placeholder%` format

Example:
```yaml
tools:
  upgrade-success: "<green>✔ <white>%tool_name%</white> amélioré au niveau <yellow>%level%</yellow>!</green>"
  upgrade-failed: "<red>✖ Amélioration échouée. Coût: <gold>%cost%$</gold></red>"
```

## Testing Checklist

For each refactored file:
- [ ] All hardcoded messages moved to messages.yml
- [ ] Message keys follow naming convention
- [ ] Placeholders work correctly
- [ ] MiniMessage formatting renders properly
- [ ] No compilation errors
- [ ] No null pointer exceptions
- [ ] Player-only checks handled appropriately

## Benefits of This Refactoring

1. **Centralized Management** - All messages in one file
2. **Easy Translation** - Can create messages_fr.yml, messages_en.yml, etc.
3. **Modern Formatting** - MiniMessage supports gradients, hover text, click actions
4. **Consistency** - Standardized message format across plugin
5. **Maintainability** - Easy to update messages without touching code
6. **No Recompilation** - Messages can be changed via config reload

## Statistics

- **Total Files with Hardcoded Messages**: ~40 files
- **Files Refactored**: 8 files (20%)
- **Files Remaining**: ~32 files (80%)
- **Hardcoded Messages Moved**: ~100 messages
- **Hardcoded Messages Remaining**: ~556 messages
- **Message Sections Created**: 10+ sections

## Next Steps

1. **Priority 1**: Refactor QuantumTowerCommand (133 messages)
2. **Priority 2**: Refactor ArmorCommand (239 lines)
3. **Priority 3**: Refactor StatsCommand (46 messages)
4. **Priority 4**: Batch refactor remaining command files
5. **Priority 5**: Refactor listener and manager files
6. **Final**: Full testing and validation

## Code Review Status
✅ All code review feedback addressed
✅ Placeholder handling fixed
✅ Message keys verified
✅ Security scan passed (0 vulnerabilities)

## Contact
For questions about this refactoring, refer to:
- MessageManager.java - Main message system implementation
- MessageSystemExample.java - Usage examples
- messages.yml - Current message definitions
