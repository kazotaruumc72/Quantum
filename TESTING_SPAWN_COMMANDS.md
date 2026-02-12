# Testing Guide for Spawn Commands

## Prerequisites
- Quantum plugin installed on a Minecraft server (Paper/Spigot 1.21+)
- MySQL database configured
- Operator permissions for testing

## Testing Steps

### 1. Test `/quantum setspawn`

**Steps:**
1. Join the server as an operator
2. Go to a location you want as spawn
3. Run: `/quantum setspawn`
4. Verify you see: `✓ Spawn location set at: X, Y, Z`

**Expected Result:**
- Command succeeds
- Location is saved to database
- Coordinates displayed in chat

**Error Cases to Test:**
- Run as non-op player → Should see permission error
- Run from console → Should see "command can only be used by players"

---

### 2. Test `/quantum setfirstspawn`

**Steps:**
1. Join the server as an operator
2. Go to a different location from spawn
3. Run: `/quantum setfirstspawn`
4. Verify you see: `✓ First spawn location set at: X, Y, Z`

**Expected Result:**
- Command succeeds
- Location is saved to database
- Coordinates displayed in chat

**Error Cases to Test:**
- Run as non-op player → Should see permission error
- Run from console → Should see "command can only be used by players"

---

### 3. Test `/spawn` Teleportation

**Steps:**
1. After setting spawn with `/quantum setspawn`
2. Move to a different location
3. Run: `/spawn`
4. Verify you are teleported to the spawn location

**Expected Result:**
- Player teleports to spawn
- Message: "Teleported to spawn."

**Error Cases to Test:**
- Run without setting spawn first → "Spawn location is not set."
- Remove permission and try → Permission error

---

### 4. Test First Join Teleportation

**Steps:**
1. Set firstspawn with `/quantum setfirstspawn`
2. Create a new player account (or use a player who has never joined)
3. Join the server with that account
4. Verify the player spawns at the firstspawn location

**Expected Result:**
- New player automatically teleported to firstspawn
- Existing players not affected (spawn at normal location)

**Note:** The `hasPlayedBefore()` method is used to detect first join.

---

### 5. Test Tab Completion

**Steps:**
1. Type `/quantum ` and press TAB
2. Verify `setspawn` and `setfirstspawn` appear in suggestions
3. Type `/quantum sets` and press TAB
4. Verify it completes to `setspawn`
5. Type `/spawn` and press TAB
6. Verify no suggestions (command has no arguments)

**Expected Result:**
- Tab completion works for `/quantum` subcommands
- `/spawn` has empty tab completion

---

### 6. Test Permission System

**Permission Tests:**

| Permission | Command | Expected Behavior |
|------------|---------|-------------------|
| `quantum.spawn.use` | `/spawn` | All players can teleport |
| `quantum.spawn.set` | `/quantum setspawn` | Only ops can set |
| `quantum.spawn.setfirst` | `/quantum setfirstspawn` | Only ops can set |
| `quantum.spawn.*` | All spawn commands | Grants all permissions |

**Steps:**
1. Give a player `quantum.spawn.use` only
2. They should be able to `/spawn` but not set spawn locations
3. Give a player `quantum.spawn.set`
4. They should be able to set spawn but not firstspawn (unless they also have `quantum.spawn.setfirst`)

---

### 7. Test Database Persistence

**Steps:**
1. Set both spawn and firstspawn
2. Stop the server
3. Restart the server
4. Use `/spawn` command
5. Join with a new player

**Expected Result:**
- Spawn locations persist across restarts
- `/spawn` still works
- New players still teleport to firstspawn

**Database Check:**
```sql
SELECT * FROM quantum_spawns;
```
Should show 2 rows: one for 'spawn' and one for 'firstspawn'

---

### 8. Test Edge Cases

**World Changes:**
1. Set spawn in one world
2. Delete that world or make it unavailable
3. Try to `/spawn`
4. Expected: Should handle gracefully (null check in `toLocation()`)

**Invalid Data:**
1. Manually corrupt database entry (set invalid world name)
2. Try to `/spawn`
3. Expected: Should return null and show error

**Multiple Sets:**
1. Set spawn at location A
2. Set spawn at location B (update)
3. `/spawn` should go to location B
4. Database should have only 1 spawn row (UPDATE not INSERT)

---

## Verification Checklist

- [ ] `/quantum setspawn` sets spawn correctly
- [ ] `/quantum setfirstspawn` sets firstspawn correctly
- [ ] `/spawn` teleports to spawn
- [ ] New players teleport to firstspawn
- [ ] Existing players not affected
- [ ] Tab completion works
- [ ] Permissions work correctly
- [ ] Database persists data
- [ ] Server restart preserves spawns
- [ ] Error messages are clear
- [ ] Console commands blocked appropriately

## Common Issues

**Issue:** "SpawnManager not loaded!"
- **Solution:** Check server logs, ensure database connection is working

**Issue:** Spawn not persisting
- **Solution:** Check database table exists and has write permissions

**Issue:** New players not teleporting to firstspawn
- **Solution:** Verify firstspawn is set and FirstJoinListener is registered

**Issue:** Permission denied
- **Solution:** Check player has correct permission node via LuckPerms or permission plugin
