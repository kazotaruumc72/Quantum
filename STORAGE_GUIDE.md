# Quantum Storage System - Complete Guide

## 📦 **Interactive Storage GUI**

The storage menu is now **fully interactive** - you can click items to deposit and withdraw them directly!

### **How to Use Storage GUI**

#### **Opening Storage**
```
/storage
```
or
```
/menu storage
```

#### **Depositing Items (Adding to Storage)**

**Method 1: Drag & Drop**
- Hold an item on your cursor
- Click on any empty slot in the storage menu
- **Left Click** - Deposit all items from cursor
- **Right Click** - Deposit 1 item from cursor

**Method 2: Shift-Click**
- While storage menu is open
- **Shift + Left Click** an item in your inventory
- Item automatically deposits to storage

**Method 3: Commands** (still available)
```
/qstorage transfer hand [amount]
/qstorage transfer all
/qstorage transfer nexo:custom_sword 10
/qstorage transfer minecraft:diamond 64
```

#### **Withdrawing Items (Taking from Storage)**

**Method 1: Click Storage Items**
- **Left Click** - Withdraw 1 full stack (up to 64)
- **Right Click** - Withdraw 1 item
- **Shift + Left Click** - Withdraw ALL of that item type

**Method 2: Commands** (still available)
```
/qstorage remove nexo:custom_sword 5
/qstorage remove minecraft:diamond 32
```

---

## 📊 **PlaceholderAPI Integration**

### **Available Placeholders**

Quantum provides placeholders to display storage amounts anywhere in your server!

#### **Nexo Items**
```
%quantum_amt_nexo-<item_id>%
```

**Examples:**
```
%quantum_amt_nexo-custom_sword%
%quantum_amt_nexo-ruby%
%quantum_amt_nexo-magic_wand%
```

**Usage in menu lore:**
```yaml
items:
  ruby_display:
    material: EMERALD
    display_name: "&cRuby Count"
    lore:
      - "&7You have &e%quantum_amt_nexo-ruby% &7rubies"
      - "&7in your storage!"
```

#### **Minecraft Items**
```
%quantum_amt_minecraft-<material>%
```

**Examples:**
```
%quantum_amt_minecraft-diamond%
%quantum_amt_minecraft-iron_ingot%
%quantum_amt_minecraft-oak_log%
%quantum_amt_minecraft-gold_block%
```

**Usage in menu lore:**
```yaml
items:
  diamond_display:
    material: DIAMOND
    display_name: "&bDiamond Storage"
    lore:
      - "&7Amount: &f%quantum_amt_minecraft-diamond%"
      - ""
      - "&aClick to withdraw!"
```

#### **Automatic Detection (No Prefix)**

You can also use placeholders without the prefix - Quantum will auto-detect:
```
%quantum_amt_custom_sword%    → Checks Nexo first, then Minecraft
%quantum_amt_diamond%         → Checks Nexo first, then Minecraft
```

---

## 🎨 **Storage Menu Configuration**

### **Example Storage GUI with Placeholders**

`plugins/Quantum/menus/storage.yml`

```yaml
title: "&6&lStorage - &eNode: %nodeX%"
size: 54

# Animated title (optional)
animated_title:
  enabled: true
  frames:
    - "&6&lStorage &f- &aReady"
    - "&6&lStorage &f- &eLoading"
  speed: 10

items:
  # Border decoration
  border:
    material: GRAY_STAINED_GLASS_PANE
    display_name: " "
    slots:
      - 0-8
      - 9,17,18,26,27,35,36,44
      - 45-53

  # Diamond storage display (clickable!)
  diamond_slot:
    material: DIAMOND
    display_name: "&b&lDiamond Storage"
    lore:
      - "&7Stored: &f%quantum_amt_minecraft-diamond%"
      - ""
      - "&aLeft Click &7- Withdraw 64"
      - "&aRight Click &7- Withdraw 1"
      - "&aShift Click &7- Withdraw ALL"
    slots:
      - 10
    # Items at this slot are interactive - no click actions needed!

  # Nexo custom item display (clickable!)
  custom_sword:
    nexo_item: "custom_sword"
    display_name: "&c&lCustom Sword Storage"
    lore:
      - "&7Amount: &e%quantum_amt_nexo-custom_sword%"
      - ""
      - "&eClick to withdraw"
    slots:
      - 11

  # Gold storage with dynamic lore
  gold_storage:
    material: GOLD_INGOT
    display_name: "&e&lGold Storage"
    lore:
      - "&7Current: &f%quantum_amt_minecraft-gold_ingot%"
      - "&7Value: &6$%quantum_amt_minecraft-gold_ingot * 10%"
    slots:
      - 12

  # Info button (not clickable - has actions)
  info:
    material: BOOK
    display_name: "&e&lHow to Use"
    lore:
      - "&7Drag items to deposit"
      - "&7Click items to withdraw"
      - ""
      - "&aLeft Click &7- Withdraw stack"
      - "&aRight Click &7- Withdraw 1"
      - "&aShift Click &7- Withdraw all"
    slots:
      - 49
    click_actions:
      - "[MESSAGE] &aStorage system ready!"
      - "[SOUND] ENTITY_PLAYER_LEVELUP"

  # Close button
  close:
    material: BARRIER
    display_name: "&c&lClose"
    slots:
      - 53
    click_actions:
      - "[CLOSE]"
```

---

## 🎯 **Advanced Features**

### **Real-Time Updates**

The storage GUI **automatically refreshes** when:
- You use `/qstorage transfer` command
- You use `/qstorage remove` command
- You click items in the GUI
- You drag items to the GUI

### **Smart Item Detection**

Quantum automatically detects:
- ✅ Nexo custom items (via NexoItems API)
- ✅ Minecraft vanilla items
- ✅ Items with custom model data
- ✅ Items with enchantments
- ✅ Items with NBT data

### **Click Behavior**

| Action | Result |
|--------|--------|
| **Left Click** item in storage | Withdraw 1 stack (64 max) |
| **Right Click** item in storage | Withdraw 1 item |
| **Shift + Left Click** item in storage | Withdraw ALL of that type |
| **Left Click** with item on cursor | Deposit all items |
| **Right Click** with item on cursor | Deposit 1 item |
| **Shift + Click** item in player inventory | Deposit to storage |

---

## 📝 **Usage Examples**

### **Example 1: Resource Counter**

Show how many resources a player has:

```yaml
items:
  resource_counter:
    material: CHEST
    display_name: "&e&lYour Resources"
    lore:
      - "&7Diamonds: &b%quantum_amt_minecraft-diamond%"
      - "&7Gold: &e%quantum_amt_minecraft-gold_ingot%"
      - "&7Iron: &f%quantum_amt_minecraft-iron_ingot%"
      - "&7Emeralds: &a%quantum_amt_minecraft-emerald%"
    slots:
      - 4
```

### **Example 2: Custom Item Shop**

Display Nexo items with storage count:

```yaml
items:
  custom_sword_shop:
    nexo_item: "custom_sword"
    display_name: "&c&lLegendary Sword"
    lore:
      - "&7Price: &6$5000"
      - "&7In Storage: &e%quantum_amt_nexo-custom_sword%"
      - ""
      - "&aClick to purchase!"
    slots:
      - 20
    click_actions:
      - "[CONSOLE] eco take %player_name% 5000"
      - "[CONSOLE] qstorage transfer nexo:custom_sword 1 %player_name%"
```

### **Example 3: Storage Limits Display**

Show storage capacity warnings:

```yaml
items:
  capacity_warning:
    material: REDSTONE
    display_name: "&c&lStorage Alert"
    lore:
      - "&7Redstone: &c%quantum_amt_minecraft-redstone%"
      - "&7Max: &e10000"
    slots:
      - 13
    requirements:
      - "quantum_amt_minecraft-redstone >= 9000"
    # Only shows when player has 9000+ redstone
```

---

## 🔧 **Commands Reference**

### **Player Commands**

```
/storage                                    → Open storage GUI
/qstorage transfer hand [amount]           → Transfer item in hand
/qstorage transfer all                     → Transfer all inventory items
/qstorage transfer nexo:<id> [amount]     → Transfer Nexo item
/qstorage transfer minecraft:<id> [amount] → Transfer Minecraft item
/qstorage remove nexo:<id> [amount]       → Remove Nexo item
/qstorage remove minecraft:<id> [amount]   → Remove Minecraft item
```

### **Console Commands**

```
qstorage transfer nexo:<id> <amount> <player>
qstorage transfer minecraft:<id> <amount> <player>
qstorage remove nexo:<id> <amount> <player>
qstorage remove minecraft:<id> <amount> <player>
```

---

## 💡 **Tips & Best Practices**

1. **Use placeholders in lore** to show real-time storage amounts
2. **Combine with requirements** to create conditional displays
3. **Empty slots are interactive** - perfect for drag & drop
4. **Decorated slots with items** execute click actions
5. **Background items** should have no click actions
6. **Storage slots** (10-44 excluding borders) are perfect for item display

---

## 🎮 **Player Experience**

### **Visual Feedback**

- ✅ Sound plays on deposit/withdrawal
- ✅ Chat messages confirm actions
- ✅ GUI refreshes automatically
- ✅ Placeholders update in real-time

### **Error Handling**

- "Inventory is full" if no space to withdraw
- "Not in storage" if item doesn't exist
- "No permission" for restricted actions

---

## 📚 **Integration Examples**

### **With DeluxeMenus-style Actions**

```yaml
items:
  withdraw_button:
    material: DIAMOND
    display_name: "&b&lQuick Withdraw"
    lore:
      - "&7Available: &f%quantum_amt_minecraft-diamond%"
    click_actions:
      - "[CONSOLE] qstorage remove minecraft:diamond 64 %player_name%"
      - "[MESSAGE] &aWithdrawn 64 diamonds!"
      - "[SOUND] ENTITY_PLAYER_LEVELUP"
```

### **With Requirements**

```yaml
items:
  full_storage_warning:
    material: BARRIER
    display_name: "&c&lStorage Full!"
    requirements:
      - "quantum_amt_minecraft-diamond >= 10000"
    click_actions:
      - "[MESSAGE] &cYou've reached the display limit!"
```

---

## 🚀 **Performance**

- **Database-backed** - all storage persists
- **Async operations** - no server lag
- **Efficient caching** - instant placeholder updates
- **Smart refresh** - only when needed

---

**Quantum Storage - The most advanced storage system for Minecraft!** 🎉
