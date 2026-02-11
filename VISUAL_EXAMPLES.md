# Visual Examples - Job Preview System

This document shows visual examples of how the new job preview system works.

## 🎮 In-Game Examples

### Example 1: Basic Preview (Action Bar)

**Scenario**: Player right-clicks on a WHOLE oak tree with Lumberjack job

```
Action Bar Display:
█ ⛏ Bûcheron » +10 XP │ +5.0$
```

**What it means**:
- `█` Structure is in WHOLE state (full health)
- `⛏ Bûcheron` Player's job (with color)
- `+10 XP` Experience points to gain
- `+5.0$` Money to gain

---

### Example 2: Preview with Active Boosters

**Scenario**: Player has XP Booster x1.5 and Money Booster x2.0 active

```
Action Bar Display:
█ ⛏ Bûcheron » +15 XP ✦ │ +10.0$ ✦
```

**What it means**:
- `+15 XP ✦` XP with booster applied (10 * 1.5 = 15)
- `+10.0$ ✦` Money with booster applied (5 * 2.0 = 10)
- `✦` Indicates an active booster

---

### Example 3: Degraded Structure

**Scenario**: Player right-clicks on a DAMAGED tree

```
Action Bar Display:
▒ ⛏ Bûcheron » +5 XP │ +2.0$
```

**What it means**:
- `▒` Structure is DAMAGED (50% health)
- Lower rewards due to degraded state

---

### Example 4: Invalid Structure

**Scenario**: Lumberjack player right-clicks on an ore deposit

```
Action Bar Display:
⚠ Structure invalide pour votre métier
```

**What it means**:
- Structure is not valid for the player's job
- No action will be performed on left-click

---

### Example 5: No Job Selected

**Scenario**: Player without a job right-clicks on a structure

```
Action Bar Display:
⚠ Aucun métier sélectionné
```

**What it means**:
- Player needs to select a job first with `/job select <job>`

---

## 📊 Detailed Preview Command Examples

### Example 1: Basic Preview

**Command**: `/job rewards preview`

```
╔═══════════════════════════════════════╗
║  Aperçu des Récompenses              ║
╚═══════════════════════════════════════╝

Métier: ⛏ Bûcheron
Niveau: 5/100
XP: 85/110
[███████████████████████░░░░░░░░░░░] 77.3%

▸ Prochaines récompenses:

  ◆ Niveau 10 (135 XP restants)
    • 💰 100$ d'argent
    • 📦 magic_log x5 (Item Nexo)

  ◆ Niveau 15 (589 XP restants)
    • ✦ Booster XP x1.5 - 1h (Donjon uniquement)

  ◆ Niveau 20 (1247 XP restants)
    • ✦ Booster Argent x2.0 - 2h

Utilisez /job rewards pour voir plus de récompenses.
```

---

### Example 2: Extended Preview

**Command**: `/job rewards preview 5`

```
╔═══════════════════════════════════════╗
║  Aperçu des Récompenses              ║
╚═══════════════════════════════════════╝

Métier: ⛏ Bûcheron
Niveau: 3/100
XP: 45/121
[███████████░░░░░░░░░░░░░░░░░░░░░░░░] 37.2%

▸ Prochaines récompenses:

  ◆ Niveau 5 (197 XP restants)
    • ⚙ Commande spéciale
    • 💰 100$ d'argent

  ◆ Niveau 10 (752 XP restants)
    • ⚙ Action joueur
    • 📦 magic_log x5 (Item Nexo)

  ◆ Niveau 15 (1341 XP restants)
    • ✦ Booster XP x1.5 - 1h (Donjon uniquement)

  ◆ Niveau 20 (1999 XP restants)
    • ✦ Booster Argent x2.0 - 2h

  ◆ Niveau 25 (2723 XP restants)
    • ⚔ EnchantedAxe x1 (Item MythicMobs)

Utilisez /job rewards pour voir plus de récompenses.
```

---

### Example 3: High Level Player

**Command**: `/job rewards preview`

**Scenario**: Player at level 95

```
╔═══════════════════════════════════════╗
║  Aperçu des Récompenses              ║
╚═══════════════════════════════════════╝

Métier: ⛏ Bûcheron
Niveau: 95/100
XP: 12450/62891
[███████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░] 19.8%

▸ Prochaines récompenses:

  ◆ Niveau 100 (251452 XP restants)
    • 💰 10000$ d'argent
    • ⚔ LegendaryAxe x1 (Item MythicMobs)
    • ⚙ Commande spéciale

Utilisez /job rewards pour voir plus de récompenses.
```

---

## 💬 Chat Examples

### Example 1: Basic Rewards List

**Command**: `/job rewards`

```
════════════════════════════════════
✦ Prochaines Récompenses
════════════════════════════════════

▸ Niveau 5:
  • 💰 100$
  • ⚙ Commande spéciale

▸ Niveau 10:
  • 📦 magic_log x5 (Nexo)
  • ⚙ Action joueur

▸ Niveau 15:
  • ✦ Booster XP x1.5 (60 min) (Donjon)

▸ Niveau 20:
  • ✦ Booster $ x2.0 (120 min)

▸ Niveau 25:
  • ⚔ EnchantedAxe x1 (MythicMobs)

Astuce: Utilisez /job rewards preview pour un aperçu détaillé!
```

---

### Example 2: Job Information

**Command**: `/job`

```
=== Votre Métier ===
⛏ Bûcheron - Niveau 5/100
Expérience: 85/110
[████████████████████░░░░░░░░░░░░░░░░░░░]
```

---

### Example 3: Job Selection

**Command**: `/job select lumberjack`

```
✓ Vous avez sélectionné le métier: ⛏ Bûcheron
```

---

### Example 4: Level Up

**Scenario**: Player reaches level 10

```
✦ Niveau supérieur! Vous êtes maintenant ⛏ Bûcheron niveau 10!
✓ Récompense débloquée: 100$
✓ Récompense débloquée: Nexo Item: magic_log
```

---

## 🎨 Structure State Progression

### Visual Representation

```
WHOLE (100%)
█ ⛏ Bûcheron » +10 XP │ +5.0$
↓ [Left-Click]

GOOD (75%)
▓ ⛏ Bûcheron » +7 XP │ +3.0$
↓ [Left-Click]

DAMAGED (50%)
▒ ⛏ Bûcheron » +5 XP │ +2.0$
↓ [Left-Click]

STUMP (25%)
░ ⛏ Bûcheron » +2 XP │ +1.0$
↓ [Left-Click]

[Structure Removed]
```

---

## 🎯 Progress Bar Examples

### Different Progress Levels

```
0% - Empty
[░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░] 0.0%

25% - Quarter
[███████░░░░░░░░░░░░░░░░░░░░░░░] 25.0%

50% - Half
[███████████████░░░░░░░░░░░░░░░] 50.0%

75% - Three Quarters
[██████████████████████░░░░░░░░] 75.0%

100% - Full
[██████████████████████████████] 100.0%
```

---

## 📱 Complete Workflow Example

### Scenario: New Player Starting Lumberjack Job

```
1. Player: /job list
   System: [Shows all available jobs]

2. Player: /job select lumberjack
   System: ✓ Vous avez sélectionné le métier: ⛏ Bûcheron

3. Player: /job
   System: 
   === Votre Métier ===
   ⛏ Bûcheron - Niveau 1/100
   Expérience: 0/100
   [░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░]

4. Player: /job rewards preview
   System: [Shows detailed preview with next 3 rewards]

5. Player: *Right-clicks on oak tree*
   Action Bar: █ ⛏ Bûcheron » +10 XP │ +5.0$

6. Player: *Left-clicks on oak tree*
   Chat: +10 XP ⛏ Bûcheron | +5.0$
   Action Bar: Updated preview (tree now in GOOD state)

7. Player: *Continues harvesting*
   [XP accumulates, tree degrades through states]

8. Player reaches Level 5:
   System: 
   ✦ Niveau supérieur! Vous êtes maintenant ⛏ Bûcheron niveau 5!
   ✓ Récompense débloquée: 100$
   ✓ Récompense débloquée: Commande spéciale
```

---

## 🔄 Comparison: Before vs After

### Before (Old System)
```
Player: *Left-clicks tree*
Chat: +10 XP Lumberjack | +5$

Player: /job rewards
Chat:
=== Prochaines Récompenses ===
Niveau 10:
  - 100$
  - Item Nexo: magic_log x5
Niveau 15:
  - Booster XP x1.5 (60 min)
```

### After (New System)
```
Player: *Right-clicks tree*
Action Bar: █ ⛏ Bûcheron » +10 XP │ +5.0$

Player: *Left-clicks tree*
Chat: +10 XP ⛏ Bûcheron | +5.0$

Player: /job rewards preview
Chat:
╔═══════════════════════════════════════╗
║  Aperçu des Récompenses              ║
╚═══════════════════════════════════════╝

Métier: ⛏ Bûcheron
Niveau: 5/100
XP: 85/110
[███████████████████████░░░░░░░░░░░] 77.3%

▸ Prochaines récompenses:

  ◆ Niveau 10 (135 XP restants)
    • 💰 100$ d'argent
    • 📦 magic_log x5 (Item Nexo)
```

---

## 💡 Tips for Players

1. **Use Right-Click** to preview rewards before harvesting
2. **Use `/job rewards preview`** for detailed progression tracking
3. **Watch for the ✦ symbol** to know when boosters are active
4. **Check structure state icons** (█ ▓ ▒ ░) to know reward amounts
5. **Plan your progression** using the XP calculations shown

---

## 🎓 Admin Tips

1. Configure custom messages in `jobs.yml`
2. Add more reward types as needed
3. Balance XP and money rewards based on structure states
4. Use the preview system to test job configurations
5. Monitor player feedback on reward visibility

---

This visual guide demonstrates all the new features of the job preview system, making it easy for both players and administrators to understand and use the enhanced functionality!
