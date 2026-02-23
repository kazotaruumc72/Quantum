# Orestack Compilation Fix

## Problem

The project was experiencing compilation errors when using the Orestack API:

```
[ERROR] /C:/Users/Utilisateur/Desktop/Quantum/src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java:[30,30] cannot access io.github.pigaut.voxel.event.PlayerEvent
  class file for io.github.pigaut.voxel.event.PlayerEvent not found
[ERROR] /C:/Users/Utilisateur/Desktop/Quantum/src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java:[43,30] cannot find symbol
  symbol:   method getPlayer()
  location: variable event of type io.github.pigaut.orestack.api.event.GeneratorMineEvent
```

## Root Cause

The Orestack API (version 1.1) events `GeneratorMineEvent` and `GeneratorHarvestEvent` extend `io.github.pigaut.voxel.event.PlayerEvent` from the VoxelSpigot library.

While the OrestackAPI pom.xml declares VoxelSpigot as a `provided` dependency, Maven does not automatically resolve `provided` transitive dependencies during compilation. This means that even though Orestack API depends on VoxelSpigot, our project needs to explicitly declare it as a dependency to compile successfully.

## Solution

Added the VoxelSpigot dependency to the project's pom.xml:

```xml
<!-- VoxelSpigot - Required by Orestack API for PlayerEvent -->
<dependency>
    <groupId>io.github.pigaut.voxel</groupId>
    <artifactId>VoxelSpigot</artifactId>
    <version>1.4.1</version>
    <scope>provided</scope>
</dependency>
```

## Dependency Chain

```
Quantum Project
  └─ OrestackAPI 1.1 (provided)
      └─ VoxelSpigot 1.4.1 (provided) ← Must be explicitly declared in Quantum
```

## Maven Central Availability

Both dependencies are published to Maven Central:
- `io.github.pigaut.orestack.api:OrestackAPI:1.1`
- `io.github.pigaut.voxel:VoxelSpigot:1.4.1`

## Files Modified

- `pom.xml`: Added VoxelSpigot dependency
- `src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java`: No changes needed (code was correct)

## Verification

To verify the fix works:

```bash
mvn clean compile
```

The compilation should now succeed without errors related to PlayerEvent or getPlayer().
