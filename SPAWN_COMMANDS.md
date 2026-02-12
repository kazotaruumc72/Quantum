# Spawn Commands Documentation

This document describes the spawn commands added to the Quantum plugin.

## Commands

### 1. `/quantum setfirstspawn`
Sets the first spawn location where new players will be teleported when they join the server for the first time.

**Usage:** `/quantum setfirstspawn`

**Permission:** `quantum.spawn.setfirst` (default: op)

**Description:** 
- Must be executed by a player (not from console)
- Sets the first spawn point at the player's current location
- New players will automatically be teleported to this location on their first join

**Example:**
```
/quantum setfirstspawn
```

### 2. `/quantum setspawn`
Sets the regular spawn location where players can teleport using `/spawn`.

**Usage:** `/quantum setspawn`

**Permission:** `quantum.spawn.set` (default: op)

**Description:**
- Must be executed by a player (not from console)
- Sets the spawn point at the player's current location
- Players can return to this location using `/spawn`

**Example:**
```
/quantum setspawn
```

### 3. `/spawn`
Teleports the player to the spawn location.

**Usage:** `/spawn`

**Permission:** `quantum.spawn.use` (default: true)

**Description:**
- Teleports the player to the spawn location set by `/quantum setspawn`
- If spawn is not set, displays an error message
- Available to all players by default

**Example:**
```
/spawn
```

## Tab Completion

All three commands have tab completion enabled:
- `/quantum` shows `setspawn` and `setfirstspawn` in the suggestions
- `/spawn` has no arguments, so tab completion is empty

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `quantum.spawn.*` | All spawn permissions | op |
| `quantum.spawn.use` | Use `/spawn` command | true (all players) |
| `quantum.spawn.set` | Set spawn location | op |
| `quantum.spawn.setfirst` | Set first spawn location | op |

## Implementation Details

### Database
Spawn locations are stored in the `quantum_spawns` table with the following structure:
- `type`: Either 'spawn' or 'firstspawn'
- `world`: World name
- `x`, `y`, `z`: Coordinates
- `yaw`, `pitch`: View direction

### First Join Behavior
When a new player joins the server for the first time:
1. The `FirstJoinListener` detects it's their first join
2. If firstspawn is set, the player is automatically teleported there
3. If firstspawn is not set, the player spawns at the default world spawn

### Manager Classes
- **SpawnManager**: Handles spawn location storage and retrieval
- **Spawn**: Represents a spawn location data object
- **FirstJoinListener**: Listens for new player joins and teleports them to firstspawn

## Setup Instructions

1. Start your server with the updated plugin
2. Join as an operator
3. Go to the location where you want the first spawn to be
4. Execute `/quantum setfirstspawn`
5. Go to the location where you want the regular spawn to be
6. Execute `/quantum setspawn`
7. Players can now use `/spawn` to teleport to spawn
8. New players will automatically be teleported to firstspawn on their first join
