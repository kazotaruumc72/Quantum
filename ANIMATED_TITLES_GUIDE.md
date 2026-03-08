# Animated and Conditional Title System

## Overview

The Quantum plugin now supports advanced title configurations for GUI menus with two distinct modes:

1. **SIMPLY** - Simple titles with optional animation frames
2. **CONDITIONNAL** - Conditional titles that change based on player requirements

## Configuration Format

### SIMPLY Type (Animated Titles)

The SIMPLY type allows you to create animated menu titles with configurable frame delays.

```yaml
title:
  title_type: SIMPLY
  title: "&6&lExample Menu"
  title_animation: true  # false by default
  delay_animation: 2  # in seconds (delay between frames)
  animations:
    - animation_1:
        frame: "&aExample"
    - animation_2:
        frame: "&bExample"
    - animation_3:
        frame: "&cExample"

size: 45
```

**Parameters:**

- `title_type: SIMPLY` - Specifies the simple title type
- `title` - The base title text (used when animation is disabled)
- `title_animation` - Enable/disable animation (default: false)
- `delay_animation` - Delay between animation frames in seconds (default: 2)
- `animations` - List of animation frames
  - Each animation entry has a unique identifier (e.g., `animation_1`, `animation_2`)
  - `frame` - The title text for this animation frame

### CONDITIONNAL Type (Conditional Titles)

The CONDITIONNAL type allows you to display different titles based on player conditions. The first matching condition is used.

```yaml
title:
  title_type: CONDITIONNAL
  conditions:
    # First condition: Check if player has permission
    perm_1:
      - type: has permission
        permission: 'quantum.vip'
        title: "&dVIP Menu"

    # Second condition: Check placeholder value
    perm_2:
      - type: placeholder
        input: '%player_name%'
        output: 'Kazotaruu_'
        title: "&eKazotaruu's Special Menu"

    # Third condition: Check if player has specific item
    perm_3:
      - type: has item
        material: "DIAMOND"
        amount: 10
        title: "&bDiamond Holder Menu"

    # Default condition (no requirements, always matches)
    default:
      - title: "&7Default Menu"

size: 45
```

**Condition Types:**

#### 1. Has Permission

Checks if a player has a specific permission.

```yaml
perm_check:
  - type: has permission
    permission: 'permission.exemple'
    title: "&dPermission Title"
```

#### 2. Placeholder

Checks if a PlaceholderAPI placeholder matches a specific value.

```yaml
placeholder_check:
  - type: placeholder
    input: '%player_name%'
    output: 'Kazotaruu_'
    title: "&eSpecial Title"
```

#### 3. Has Item

Checks if a player has a specific item in their inventory.

```yaml
item_check:
  - type: has item
    material: "TEXT"        # Material name, placeholder, or argument
    data: #                 # Optional: item data value
    modeldata: #            # Optional: CustomModelData value
    amount: #               # Required amount of items
    name: "TEXT"            # Optional: item display name
    lore:                   # Optional: item lore (can be a list or single string)
      - "TEXT"
    name_contains: boolean  # If true, matches items containing the name
    name_ignorecase: boolean # If true, ignores case when matching name
    lore_contains: boolean  # If true, matches items containing the lore text
    lore_ignorecase: boolean # If true, ignores case when matching lore
    title: "&bItem Title"
```

**Note:** For the has item check, if you want to match only vanilla items with no custom properties, set `name`, `lore`, and `modeldata` to empty/null.

## Backward Compatibility

The plugin maintains full backward compatibility with the existing title configuration format:

### Legacy Format (Still Supported)

```yaml
# Simple string title
menu_title: '&6&lExample Menu'

# Or using 'title' key
title: '&6&lExample Menu'

# Legacy animated title
animated_title:
  enabled: true
  frames:
    - '&6&l✦ &e&lEXAMPLE MENU &6&l✦'
    - '&e&l✦ &6&lEXAMPLE MENU &e&l✦'
  speed: 10  # ticks (not seconds)

size: 54
```

**Differences:**

- Legacy animated_title uses `speed` in **ticks** (20 ticks = 1 second)
- New system uses `delay_animation` in **seconds** for easier configuration
- Legacy format continues to work without any changes needed

## Examples

### Example 1: Simple Animated Title

```yaml
title:
  title_type: SIMPLY
  title: "&6&lShop"
  title_animation: true
  delay_animation: 1
  animations:
    - frame_1:
        frame: "&e&l★ &6&lSHOP &e&l★"
    - frame_2:
        frame: "&6&l★ &e&lSHOP &6&l★"
```

### Example 2: VIP Menu with Fallback

```yaml
title:
  title_type: CONDITIONNAL
  conditions:
    vip_title:
      - type: has permission
        permission: 'server.vip'
        title: "&d&l♦ VIP Shop ♦"

    default_title:
      - title: "&7&lShop"
```

### Example 3: Dynamic Title Based on Items

```yaml
title:
  title_type: CONDITIONNAL
  conditions:
    rich_player:
      - type: has item
        material: "DIAMOND_BLOCK"
        amount: 64
        title: "&b&lDiamond Tycoon"

    regular_player:
      - type: has item
        material: "DIAMOND"
        amount: 1
        title: "&a&lDiamond Owner"

    default:
      - title: "&7&lMenu"
```

## Technical Details

### Implementation

- **TitleConfig Class**: Manages title configuration (SIMPLY or CONDITIONNAL)
- **Menu.resolveTitleFromConfig()**: Resolves the appropriate title based on player conditions
- **MenuManager.loadTitleConfig()**: Parses the new configuration format
- **Backward Compatibility**: Legacy `animated_title` format is automatically detected and used

### Animation System

- Animations use the existing AnimationManager
- Frame updates are handled via Bukkit scheduler
- Placeholders are supported in all title frames
- Animation stops automatically when the menu is closed

### Condition Evaluation

Conditions are evaluated in the order they appear in the configuration. The first condition where **all requirements are met** is used. This allows for fallback logic:

1. Check most specific conditions first (e.g., VIP + special item)
2. Check less specific conditions (e.g., VIP only)
3. Provide a default condition at the end with no requirements

## Tips and Best Practices

1. **Order Matters**: Place more specific conditions before general ones
2. **Always Provide a Default**: Include a fallback condition with no requirements
3. **Test Conditions**: Use `/quantum reload` to test configuration changes
4. **Animation Timing**: Keep `delay_animation` between 1-3 seconds for best UX
5. **Color Codes**: Both legacy (`&`) and MiniMessage (`<gradient>`) formats are supported
6. **Placeholders**: All PlaceholderAPI placeholders work in titles and frames

## Troubleshooting

### Title Not Animating

- Check that `title_animation: true` is set
- Verify that `animations` list is not empty
- Ensure `delay_animation` is greater than 0

### Wrong Conditional Title Showing

- Check the order of conditions (first match wins)
- Verify that requirements are correctly configured
- Test individual requirements with debug logging

### Legacy Animated Title Not Working

- Legacy format only works when new TitleConfig is not present
- Ensure `animated_title.enabled: true` is set
- Check that `frames` list contains valid strings

## Migration Guide

### From Legacy to New Format

**Before:**
```yaml
menu_title: '&6&lShop'
animated_title:
  enabled: true
  frames:
    - '&e&lShop'
    - '&6&lShop'
  speed: 20
```

**After (New Format):**
```yaml
title:
  title_type: SIMPLY
  title: "&6&lShop"
  title_animation: true
  delay_animation: 1  # 20 ticks = 1 second
  animations:
    - frame_1:
        frame: "&e&lShop"
    - frame_2:
        frame: "&6&lShop"
```

## Future Enhancements

Potential future additions to the title system:

- Per-condition animation support
- Multiple requirement combinations (AND/OR logic)
- Time-based conditions (show different titles at different times)
- Custom requirement plugins support
- Title transition effects
