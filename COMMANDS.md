# Quantum - Commands Documentation

## Storage Commands

### `/qstorage` (Aliases: `/quantumstorage`, `/qs`)

Manage your virtual storage with support for Nexo and Minecraft items.

#### Transfer Commands

Transfer items from your inventory to storage:

```
/qstorage transfer hand [amount]
/qstorage transfer all
/qstorage transfer <item> [amount]
/qstorage transfer nexo:<id> [amount]
/qstorage transfer minecraft:<id> [amount]
```

**Examples:**
```
/qstorage transfer hand           # Transfer item in main hand
/qstorage transfer hand 32        # Transfer 32 items from hand
/qstorage transfer all            # Transfer all inventory items
/qstorage transfer diamond 64     # Transfer 64 diamonds (auto-detect)
/qstorage transfer nexo:custom_sword 10    # Transfer 10 Nexo custom swords
/qstorage transfer minecraft:diamond 64    # Transfer 64 Minecraft diamonds
```

**Console Usage:**
```
/qstorage transfer nexo:<id> <amount> <player>
/qstorage transfer minecraft:<id> <amount> <player>
```

**Console Examples:**
```
/qstorage transfer minecraft:diamond 64 Notch
/qstorage transfer nexo:custom_item 10 Steve
```

#### Remove Commands

Withdraw items from storage to your inventory:

```
/qstorage remove <item> [amount]
/qstorage remove nexo:<id> [amount]
/qstorage remove minecraft:<id> [amount]
```

**Examples:**
```
/qstorage remove diamond 32       # Remove 32 diamonds (auto-detect)
/qstorage remove nexo:custom_sword 5      # Remove 5 Nexo custom swords
/qstorage remove minecraft:emerald 16     # Remove 16 Minecraft emeralds
```

**Console Usage:**
```
/qstorage remove nexo:<id> <amount> <player>
/qstorage remove minecraft:<id> <amount> <player>
```

**Console Examples:**
```
/qstorage remove minecraft:diamond 64 Notch
/qstorage remove nexo:custom_item 10 Steve
```

### Syntax Explanation

#### Prefix System

- **`nexo:<id>`** - Explicitly specifies a Nexo custom item
- **`minecraft:<id>`** - Explicitly specifies a Minecraft vanilla item
- **No prefix** - Auto-detection (tries Nexo first, then Minecraft)

**Why use prefixes?**
- Prevents conflicts if a Nexo item has the same ID as a Minecraft material
- Forces the system to use the correct item type
- Useful in console commands where context is not available

#### Tab Completion

The command supports full tab completion:

1. **Subcommands**: `transfer`, `remove`
2. **Special keywords**: `hand`, `all` (for transfer only)
3. **Prefixes**: `nexo:`, `minecraft:`
4. **Nexo items**: All available Nexo item IDs
5. **Vanilla materials**: Common Minecraft materials
6. **Amounts**: `1`, `16`, `32`, `64`
7. **Player names**: Online players (console only)

## Permissions

```yaml
quantum.storage.transfer    # Allow transferring items to storage
quantum.storage.remove       # Allow removing items from storage
```

## Menu Configuration Features

### Custom Model Data

```yaml
items:
  custom_item:
    slot: 10
    material: DIAMOND_SWORD
    custom_model_data: 1001  # Your custom model ID from resource pack
    display_name: '&bCustom Sword'
```

### Glow Effect

Add enchantment glow without showing enchantments:

```yaml
items:
  glowing_item:
    slot: 11
    material: DIAMOND
    glow: true  # Adds glow effect
    display_name: '&e&lGlowing Diamond'
```

### Hide Flags (Custom Tooltips)

Hide specific item information from tooltips:

```yaml
items:
  custom_potion:
    slot: 12
    material: POTION
    display_name: '&5Magic Potion'
    hide_flags:
      - HIDE_POTION_EFFECTS  # Hide potion effects
      - HIDE_ATTRIBUTES      # Hide attributes
      - HIDE_ENCHANTS        # Hide enchantments
      - HIDE_UNBREAKABLE     # Hide unbreakable tag
```

**Available Hide Flags:**
- `HIDE_ENCHANTS` - Hide enchantments
- `HIDE_ATTRIBUTES` - Hide attribute modifiers
- `HIDE_UNBREAKABLE` - Hide unbreakable tag
- `HIDE_DESTROYS` - Hide "Can destroy" block list
- `HIDE_PLACED_ON` - Hide "Can be placed on" block list
- `HIDE_POTION_EFFECTS` - Hide potion effects
- `HIDE_DYE` - Hide dye color on leather armor

### Nexo Integration

```yaml
items:
  nexo_item:
    slot: 13
    nexo_item: your_nexo_item_id  # Use your Nexo item ID
    display_name: '&dCustom Nexo Item'
    glow: true  # Can add glow to Nexo items
```

### Complete Example

```yaml
menu_title: '&6&lCustom Shop'
size: 27

items:
  # Custom model item with glow
  premium_sword:
    slot: 11
    material: DIAMOND_SWORD
    custom_model_data: 1001
    display_name: '&b&lPremium Sword'
    lore:
      - '&7A legendary weapon!'
      - '&7Price: &e1000 coins'
    glow: true
    hide_flags:
      - HIDE_ENCHANTS
      - HIDE_ATTRIBUTES
    left_click:
      actions:
        - '[console] give %player% custom_sword'
        - '[message] &aYou purchased the Premium Sword!'
  
  # Nexo custom item
  magic_staff:
    slot: 13
    nexo_item: magic_staff
    display_name: '&5&lMagic Staff'
    lore:
      - '&7A powerful magical weapon'
      - '&7Price: &e5000 coins'
    glow: true
    left_click:
      actions:
        - '[console] give %player% nexo:magic_staff'
        - '[message] &aYou purchased the Magic Staff!'
  
  # Close button
  close:
    slot: 22
    material: BARRIER
    display_name: '&c&lClose'
    left_click:
      actions:
        - '[close]'
```

## Tips & Best Practices

1. **Use prefixes in console commands** to avoid ambiguity
2. **Test custom model data IDs** with your resource pack
3. **Combine glow with hide_flags** for clean custom items
4. **Use HIDE_ATTRIBUTES** to clean up vanilla item tooltips
5. **Tab completion** makes commands faster and reduces errors

## Troubleshooting

### "Item not found"
- Verify the Nexo item ID exists: `/nexo items`
- Check spelling of vanilla materials
- Use tab completion to ensure correct item IDs

### "Nexo item not found"
- Ensure Nexo plugin is installed and loaded
- Verify the item ID is correct
- Check Nexo configuration files

### Custom model not showing
- Verify players have the resource pack installed
- Check custom_model_data ID matches your resource pack
- Ensure resource pack is properly applied

### Items can be moved in GUI
- This should be prevented automatically
- Report if you can still move items (shouldn't be possible)
