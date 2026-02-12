# Placeholder API Integration Fix

## Problem
The plugin was logging errors like:
```
[07:01:40 INFO]: [Quantum] Invalid material: %quantum_history_{slot}_material%
```

This occurred because the `MenuManager` tried to convert material names to Minecraft's `Material` enum during menu loading (at startup), before placeholders could be resolved. The `history.yml` menu uses dynamic placeholders that should only be resolved at runtime when displayed to a player.

## Solution
The fix implements deferred material resolution:

### 1. MenuItem Class Changes
- Added `materialString` field to store placeholder material names
- Enhanced `toItemStack()` method to accept `player` and `customPlaceholders` parameters
- Placeholders in materials are now resolved at runtime using `PlaceholderManager`
- Only after placeholder resolution, the string is converted to `Material` enum

### 2. MenuManager Class Changes  
- Updated material loading to detect placeholders (strings containing `%`)
- Placeholder materials are stored as `materialString` instead of attempting immediate conversion
- Non-placeholder materials still work as before (immediate conversion)

### 3. Menu Class Changes
- Modified `populateInventory()` to pass player and customPlaceholders to `toItemStack()`
- This enables runtime placeholder resolution for all menu items

### 4. PlaceholderManager Class Changes
- Added `handleHistoryPlaceholder()` method for transaction history placeholders
- Supports placeholders like:
  - `%quantum_history_total%` - Total transaction count
  - `%quantum_history_buy_count%` - Buy transaction count
  - `%quantum_history_sell_count%` - Sell transaction count
  - `%quantum_history_buy_total%` - Total purchase amount
  - `%quantum_history_sell_total%` - Total sales amount
  - Pagination placeholders with sensible defaults

## Usage Example

### Before (Would Fail)
```yaml
items:
  my_item:
    material: "%some_placeholder%"  # ERROR: Invalid material
```

### After (Works Correctly)
```yaml
items:
  my_item:
    material: "%some_placeholder%"  # ✓ Stored as string, resolved at runtime
```

When the menu is opened for a player:
1. PlaceholderManager resolves `%some_placeholder%` → e.g., "DIAMOND"
2. String "DIAMOND" is converted to `Material.DIAMOND`
3. ItemStack is created with the correct material

## Transaction Template Pattern

The `history.yml` uses a special `{slot}` pattern:
```yaml
transaction_template:
  material: "%quantum_history_{slot}_material%"
  slots: [10,11,12,13,14,...]
```

The `{slot}` variable should be substituted when opening the menu. For example, when implementing the history menu command:

```java
// Get transactions for the player
List<Transaction> transactions = historyManager.getPlayerHistory(player, null, 21);

// Create custom placeholders for each slot
Map<String, String> customPlaceholders = new HashMap<>();
for (int i = 0; i < transactions.size(); i++) {
    Transaction t = transactions.get(i);
    int slot = slots[i]; // e.g., 10, 11, 12...
    
    customPlaceholders.put("quantum_history_" + slot + "_material", t.getMaterial());
    customPlaceholders.put("quantum_history_" + slot + "_item_name", t.getItemName());
    customPlaceholders.put("quantum_history_" + slot + "_quantity", String.valueOf(t.quantity));
    // etc...
}

// Open menu with custom placeholders
historyMenu.open(player, plugin, customPlaceholders);
```

## Testing

To test this fix:

1. Start the server with the updated plugin
2. Check logs - should NOT see "Invalid material" errors for placeholder materials
3. Open a menu that uses placeholder materials (once history menu is implemented)
4. Verify items display correctly with resolved materials

## Backward Compatibility

✓ All existing menus continue to work as before
✓ Static material values (e.g., `material: "DIAMOND"`) work unchanged
✓ Only materials with `%` are handled differently (stored for later resolution)

## Files Modified

- `src/main/java/com/wynvers/quantum/menu/MenuItem.java`
- `src/main/java/com/wynvers/quantum/managers/MenuManager.java`
- `src/main/java/com/wynvers/quantum/menu/Menu.java`
- `src/main/java/com/wynvers/quantum/managers/PlaceholderManager.java`

## Security

No security vulnerabilities introduced (verified with CodeQL).
