# TAB System Architecture

## System Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      QUANTUM TAB SYSTEM                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Player Join │─────▶│  TABListener │─────▶│  TABManager  │
└──────────────┘      └──────────────┘      └──────────────┘
                             │                      │
                             │ Delay 1s             │
                             ▼                      ▼
                      ┌──────────────┐      ┌──────────────┐
                      │Check if still│      │Get Player    │
                      │online        │      │Group         │
                      └──────────────┘      └──────────────┘
                                                   │
                    ┌──────────────────────────────┼───────────────┐
                    │                              │               │
                    ▼                              ▼               ▼
            ┌──────────────┐             ┌──────────────┐ ┌──────────────┐
            │Check quantum.│             │Check quantum.│ │Check quantum.│
            │tab.elite     │             │tab.mvp+      │ │tab.vip       │
            └──────────────┘             └──────────────┘ └──────────────┘
                    │                              │               │
                    └──────────────────────────────┴───────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Get Group Config  │
                            │(header + footer) │
                            └──────────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Replace           │
                            │Placeholders      │
                            └──────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
          ┌──────────────┐   ┌──────────────┐  ┌──────────────┐
          │%quantum_     │   │%server_      │  │PlaceholderAPI│
          │level%, job%  │   │online%, tps% │  │placeholders  │
          └──────────────┘   └──────────────┘  └──────────────┘
                    │                  │                  │
                    └──────────────────┴──────────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Apply MiniMessage │
                            │Formatting        │
                            └──────────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Send to Player    │
                            │(Header + Footer) │
                            └──────────────────┘
```

## Configuration Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    CONFIGURATION SYSTEM                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│Server Start  │─────▶│TABManager    │─────▶│Load Config   │
└──────────────┘      │Initialize    │      │tab_config.yml│
                      └──────────────┘      └──────────────┘
                                                   │
                                                   ▼
                                          ┌──────────────┐
                                          │Parse Groups  │
                                          │(6 default)   │
                                          └──────────────┘
                                                   │
                    ┌──────────────────────────────┼───────────────┐
                    │                              │               │
                    ▼                              ▼               ▼
            ┌──────────────┐             ┌──────────────┐ ┌──────────────┐
            │Elite Group   │             │MVP+ Group    │ │VIP Group     │
            │quantum.tab.  │             │quantum.tab.  │ │quantum.tab.  │
            │elite         │             │mvp+          │ │vip           │
            └──────────────┘             └──────────────┘ └──────────────┘
                    │                              │               │
                    └──────────────────────────────┴───────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Register          │
                            │Placeholders      │
                            └──────────────────┘
                                       │
                                       ▼
                            ┌──────────────────┐
                            │Start Refresh     │
                            │Task (every 5s)   │
                            └──────────────────┘
```

## Command Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      COMMAND SYSTEM                             │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│Admin executes│─────▶│TabEditCommand│─────▶│Parse Args    │
│/tabedit      │      └──────────────┘      └──────────────┘
└──────────────┘             │
                             │
          ┌──────────────────┼──────────────────┬─────────────┐
          │                  │                  │             │
          ▼                  ▼                  ▼             ▼
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ ┌──────────┐
  │/tabedit list │  │/tabedit      │  │/tabedit      │ │/tabedit  │
  │              │  │reload        │  │header <grp>  │ │footer    │
  └──────────────┘  └──────────────┘  └──────────────┘ └──────────┘
          │                  │                  │             │
          ▼                  ▼                  ▼             ▼
  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐
  │Show all      │  │Reload config │  │Show current OR modify:   │
  │groups with   │  │from file     │  │- add <text>              │
  │permissions   │  │              │  │- remove <line>           │
  └──────────────┘  └──────────────┘  │- set <line> <text>       │
                             │         │- clear                   │
                             │         └──────────────────────────┘
                             │                  │
                             │                  ▼
                             │         ┌──────────────┐
                             │         │Save to       │
                             │         │tab_config.yml│
                             │         └──────────────┘
                             │                  │
                             └──────────────────┘
                                      │
                                      ▼
                            ┌──────────────────┐
                            │Update all online │
                            │players           │
                            └──────────────────┘
```

## Permission Priority System

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRIORITY CHECKING                            │
└─────────────────────────────────────────────────────────────────┘

Player joins server
        │
        ▼
┌──────────────────┐
│Check permissions │
│in priority order │
└──────────────────┘
        │
        ▼
┌──────────────────────────────────────────────────────────┐
│ Priority Order (from tab_config.yml):                    │
│ 1. elite   → quantum.tab.elite                          │
│ 2. mvp+    → quantum.tab.mvp+                           │
│ 3. mvp     → quantum.tab.mvp                            │
│ 4. vip+    → quantum.tab.vip+                           │
│ 5. vip     → quantum.tab.vip                            │
│ 6. default → (no permission required)                    │
└──────────────────────────────────────────────────────────┘
        │
        ▼
┌──────────────────┐
│Player has        │───Yes───┐
│quantum.tab.elite?│         │
└──────────────────┘         │
        │ No                 │
        ▼                    │
┌──────────────────┐         │
│Player has        │───Yes───┤
│quantum.tab.mvp+? │         │
└──────────────────┘         │
        │ No                 │
        ▼                    │
       ...                   │
        │                    │
        ▼                    │
┌──────────────────┐         │
│Use default group │         │
│(no permission)   │         │
└──────────────────┘         │
        │                    │
        └────────────────────┘
                 │
                 ▼
        ┌──────────────┐
        │Apply group's │
        │header/footer │
        └──────────────┘
```

## Data Flow - Placeholders

```
┌─────────────────────────────────────────────────────────────────┐
│                  PLACEHOLDER SYSTEM                             │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐
│Raw text from     │
│config file       │
└──────────────────┘
        │
        ▼
"<gold>Niveau:</> %quantum_level%"
        │
        ▼
┌──────────────────────────────────────────────────────────┐
│ Placeholder Replacement Process:                         │
│                                                           │
│ 1. Quantum Placeholders:                                 │
│    - %quantum_level%      → "42"                         │
│    - %quantum_job%        → "Mineur"                     │
│    - %quantum_job_level%  → "15"                         │
│    - %quantum_tower%      → "Tour Obsidienne"            │
│    - %quantum_tower_floor% → "8"                         │
│                                                           │
│ 2. Server Placeholders:                                  │
│    - %server_online%      → "24"                         │
│    - %server_max_players% → "100"                        │
│    - %server_tps%         → "19.8"                       │
│                                                           │
│ 3. Player Placeholders:                                  │
│    - %player_name%        → "Kazotaruu_"                 │
│    - %player_displayname% → "§6[Elite] Kazotaruu_"      │
│    - %player_ping%        → "42"                         │
│                                                           │
│ 4. PlaceholderAPI (if installed):                        │
│    - %vault_eco_balance%  → "12500.50"                   │
│    - %luckperms_prefix%   → "§6[Elite]"                  │
│    - Any other PAPI placeholder                          │
└──────────────────────────────────────────────────────────┘
        │
        ▼
"<gold>Niveau:</> 42"
        │
        ▼
┌──────────────────┐
│MiniMessage       │
│Formatting        │
└──────────────────┘
        │
        ▼
Component with colors and formatting applied
        │
        ▼
┌──────────────────┐
│Display to player │
└──────────────────┘
```

## File Structure

```
Quantum/
├── src/main/java/com/wynvers/quantum/
│   ├── tab/
│   │   ├── TABManager.java       ← Core system
│   │   └── TABListener.java      ← Event handling
│   ├── commands/
│   │   └── TabEditCommand.java   ← In-game editing
│   └── Quantum.java               ← Plugin main (registration)
│
├── src/main/resources/
│   ├── tab_config.yml             ← Configuration file
│   └── plugin.yml                 ← Commands & permissions
│
└── docs/
    ├── TAB_SYSTEM.md              ← User documentation
    └── TAB_IMPLEMENTATION_SUMMARY.md ← Technical summary
```

## Integration Points

```
┌─────────────────────────────────────────────────────────────────┐
│                    PLUGIN INTEGRATIONS                          │
└─────────────────────────────────────────────────────────────────┘

        Quantum Plugin
              │
    ┌─────────┼─────────┬─────────┐
    │         │         │         │
    ▼         ▼         ▼         ▼
┌────────┐┌────────┐┌────────┐┌────────┐
│  TAB   ││LuckPerms││ PAPI  ││ Vault  │
│v5.5.0+ ││        ││       ││        │
└────────┘└────────┘└────────┘└────────┘
    │         │         │         │
    │         │         │         │
    ▼         ▼         ▼         ▼
┌──────────────────────────────────────┐
│ Provides:                            │
│ • TAB API for header/footer          │
│ • Permission groups                  │
│ • Additional placeholders            │
│ • Economy data                       │
└──────────────────────────────────────┘
```

---

**Note:** This architecture diagram shows the complete data flow and integration points of the TAB system. All components are implemented and functional.
