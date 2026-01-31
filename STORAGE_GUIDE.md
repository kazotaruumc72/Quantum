# Quantum Storage System - Complete Guide

## 📦 **Storage GUI Overview**

### **Access Levels**

#### **👥 Players (View-Only)**
- Can **VIEW** all stored items and quantities
- **Cannot** drag & drop or click items in GUI
- Must use `/qstorage` commands to manage items
- Real-time PlaceholderAPI updates

#### **🔑 Admins (`quantum.admin` permission)**
- Full GUI interaction (drag & drop, click withdraw)
- Can use commands OR GUI
- Can manage storage for other players

#### **🖥️ Console**
- Full command access with player targeting
- Perfect for automated systems and scripts
- Can transfer/remove items for any player

---

## 💻 **How to Use Storage**

### **Opening Storage**
```
/storage
```
or
```
/menu storage
```

### **For Players: Command-Based Management**

#### **Depositing Items (Adding to Storage)**

```bash
# Transfer item in hand
/qstorage transfer hand [amount]

# Transfer all inventory
/qstorage transfer all

# Transfer specific Nexo item
/qstorage transfer nexo:custom_sword 10

# Transfer specific Minecraft item
/qstorage transfer minecraft:diamond 64

# Auto-detection (checks Nexo first, then Minecraft)
/qstorage transfer diamond 64
```

**Examples:**
```
/qstorage transfer hand          # Transfer all items in hand
/qstorage transfer hand 32       # Transfer 32 items from hand
/qstorage transfer all           # Transfer entire inventory
/qstorage transfer diamond 64    # Transfer 64 diamonds
```

#### **Withdrawing Items (Taking from Storage)**

```bash
# Remove specific Nexo item
/qstorage remove nexo:custom_sword 5

# Remove specific Minecraft item
/qstorage remove minecraft:diamond 32

# Auto-detection
/qstorage remove emerald 16
```

**Examples:**
```
/qstorage remove diamond 32        # Remove 32 diamonds
/qstorage remove nexo:ruby 10      # Remove 10 custom rubies
/qstorage remove iron_ingot 128    # Remove 128 iron ingots
```

### **For Admins: GUI Interaction**

Admins with `quantum.admin` permission can interact directly with the GUI:

#### **Depositing Items (GUI)**

**Method 1: Drag & Drop**
- Hold an item on your cursor
- Click on any empty slot in the storage menu
- **Left Click** - Deposit all items from cursor
- **Right Click** - Deposit 1 item from cursor

**Method 2: Shift-Click**
- While storage menu is open
- **Shift + Left Click** an item in your inventory
- Item automatically deposits to storage

#### **Withdrawing Items (GUI)**

**Click Storage Items:**
- **Left Click** - Withdraw 1 full stack (up to 64)
- **Right Click** - Withdraw 1 item
- **Shift + Left Click** - Withdraw ALL of that item type

### **For Console: Targeted Commands**

```bash
# Transfer items to player's storage
qstorage transfer minecraft:diamond 64 Notch
qstorage transfer nexo:custom_item 10 Steve

# Remove items from player's storage
qstorage remove minecraft:diamond 64 Notch
qstorage remove nexo:custom_item 10 Steve
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
      - ""
      - "&7Use &a/qstorage remove nexo:ruby <amt>"
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
      - "&7Use &a/qstorage remove diamond <amt>"
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

  # Info button
  info:
    material: BOOK
    display_name: "&e&lStorage Info"
    lore:
      - ""
      - "&7This storage is &cview-only&7."
      - ""
      - "&7To manage items:"
      - "&a  • /qstorage transfer <item> <amt>"
      - "&a  • /qstorage remove <item> <amt>"
      - ""
      - "&7Admins with &equantum.admin&7:"
      - "&e  • Can drag & drop items"
      - "&e  • Can click to withdraw"
    slots:
      - 4

  # Diamond storage display (read-only for players)
  diamond_slot:
    material: DIAMOND
    display_name: "&b&lDiamond Storage"
    lore:
      - "&7Stored: &f%quantum_amt_minecraft-diamond%"
      - ""
      - "&7For admins only:"
      - "&e  Left Click - Withdraw 64"
      - "&e  Right Click - Withdraw 1"
      - ""
      - "&7Players: &a/qstorage remove diamond <amt>"
    slots:
      - 10

  # Nexo custom item display
  custom_sword:
    nexo_item: "custom_sword"
    display_name: "&c&lCustom Sword Storage"
    lore:
      - "&7Amount: &e%quantum_amt_nexo-custom_sword%"
      - ""
      - "&7Use &a/qstorage remove nexo:custom_sword <amt>"
    slots:
      - 11

  # Gold storage with dynamic lore
  gold_storage:
    material: GOLD_INGOT
    display_name: "&e&lGold Storage"
    lore:
      - "&7Current: &f%quantum_amt_minecraft-gold_ingot%"
      - ""
      - "&7Use &a/qstorage remove gold_ingot <amt>"
    slots:
      - 12

  # Resource counter
  resource_counter:
    material: CHEST
    display_name: "&e&lYour Resources"
    lore:
      - ""
      - "&7Diamonds: &b%quantum_amt_minecraft-diamond%"
      - "&7Gold: &e%quantum_amt_minecraft-gold_ingot%"
      - "&7Iron: &f%quantum_amt_minecraft-iron_ingot%"
      - "&7Emeralds: &a%quantum_amt_minecraft-emerald%"
      - ""
      - "&aStorage is unlimited!"
      - "&7Use &e/qstorage&7 to manage items"
    slots:
      - 49

  # Commands info
  commands_info:
    material: COMMAND_BLOCK
    display_name: "&6&lAvailable Commands"
    lore:
      - ""
      - "&a/qstorage transfer <item> <amount>"
      - "&7  → Deposit items to storage"
      - ""
      - "&a/qstorage remove <item> <amount>"
      - "&7  → Withdraw items from storage"
      - ""
      - "&7Examples:"
      - "&e  /qstorage transfer diamond 64"
      - "&e  /qstorage remove gold_ingot 32"
    slots:
      - 52

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
- An admin clicks items in the GUI (if you're that admin)
- Console executes commands affecting your storage

### **Smart Item Detection**

Quantum automatically detects:
- ✅ Nexo custom items (via NexoItems API)
- ✅ Minecraft vanilla items
- ✅ Items with custom model data
- ✅ Items with enchantments
- ✅ Items with NBT data

### **Admin Click Behavior**

| Action (Admins Only) | Result |
|--------|--------|
| **Left Click** item in storage | Withdraw 1 stack (64 max) |
| **Right Click** item in storage | Withdraw 1 item |
| **Shift + Left Click** item in storage | Withdraw ALL of that type |
| **Left Click** with item on cursor | Deposit all items |
| **Right Click** with item on cursor | Deposit 1 item |
| **Shift + Click** item in player inventory | Deposit to storage |

**Players:** All these actions are blocked - use commands instead!

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
      - ""
      - "&7Use &a/qstorage&7 commands!"
    slots:
      - 4
```

### **Example 2: Command Shortcut Buttons**

Create buttons that execute withdrawal commands:

```yaml
items:
  withdraw_diamonds_button:
    material: DIAMOND
    display_name: "&b&lWithdraw 64 Diamonds"
    lore:
      - "&7Available: &f%quantum_amt_minecraft-diamond%"
      - ""
      - "&aClick to withdraw 64!"
    slots:
      - 20
    click_actions:
      - "[CONSOLE] qstorage remove minecraft:diamond 64 %player_name%"
      - "[MESSAGE] &aWithdrawn 64 diamonds!"
      - "[SOUND] ENTITY_PLAYER_LEVELUP"
    requirements:
      - "quantum_amt_minecraft-diamond >= 64"
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
      - "&7You're approaching the limit!"
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

**Examples:**
```
qstorage transfer minecraft:diamond 64 Notch
qstorage remove nexo:ruby 10 Steve
```

---

## 💡 **Tips & Best Practices**

1. **Use placeholders in lore** to show real-time storage amounts
2. **Create command shortcut buttons** for quick withdrawals
3. **Add requirements** to hide buttons when items unavailable
4. **Players use commands** - GUI is for viewing only
5. **Admins use GUI** - full drag & drop support
6. **Console commands** perfect for automated systems

---

## 🎮 **Player Experience**

### **Visual Feedback**

- ✅ Sound plays on command success
- ✅ Chat messages confirm actions
- ✅ Placeholders update in real-time
- ✅ GUI refreshes automatically

### **Error Handling**

- "Inventory is full" if no space to withdraw
- "Not in storage" if item doesn't exist
- "No permission" for restricted actions
- "Storage is view-only" for GUI interaction attempts

---

## 🔑 **Permissions**

```yaml
quantum.storage.use        # View storage GUI and use commands
quantum.storage.transfer   # Transfer items to storage
quantum.storage.remove     # Remove items from storage
quantum.admin              # Full GUI interaction + admin commands
```

---

## 📚 **Integration Examples**

### **With Shop Systems**

```yaml
items:
  buy_button:
    material: DIAMOND
    display_name: "&b&lBuy 64 Diamonds"
    lore:
      - "&7Price: &6$1000"
      - ""
      - "&aClick to purchase!"
    click_actions:
      - "[CONSOLE] eco take %player_name% 1000"
      - "[CONSOLE] qstorage transfer minecraft:diamond 64 %player_name%"
      - "[MESSAGE] &aPurchased 64 diamonds!"
    requirements:
      - "money >= 1000"
```

### **With Quests/Rewards**

```yaml
items:
  quest_reward:
    nexo_item: "legendary_sword"
    display_name: "&c&lQuest Reward"
    click_actions:
      - "[CONSOLE] qstorage transfer nexo:legendary_sword 1 %player_name%"
      - "[MESSAGE] &aReward claimed!"
    requirements:
      - "permission: quests.completed.dragon"
```

---

## 🚀 **Performance**

- **Database-backed** - all storage persists
- **Async operations** - no server lag
- **Efficient caching** - instant placeholder updates
- **Command-based** - no GUI overhead for players

---

**Quantum Storage - Efficient command-based storage with admin GUI support!** 🎉
