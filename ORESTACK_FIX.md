# Orestack Compilation and Dependency Fix

## Problem

The project was experiencing build failures when using the Orestack API:

### Initial Compilation Error
```
[ERROR] /C:/Users/Utilisateur/Desktop/Quantum/src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java:[30,30] cannot access io.github.pigaut.voxel.event.PlayerEvent
  class file for io.github.pigaut.voxel.event.PlayerEvent not found
[ERROR] /C:/Users/Utilisateur/Desktop/Quantum/src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java:[43,30] cannot find symbol
  symbol:   method getPlayer()
  location: variable event of type io.github.pigaut.orestack.api.event.GeneratorMineEvent
```

### Dependency Resolution Error
```
[ERROR] Failed to execute goal on project Quantum: Could not collect dependencies for project com.wynvers:Quantum:jar:1.0.1
[ERROR] Failed to read artifact descriptor for io.github.pigaut.voxel:VoxelSpigot:jar:1.4.1
[ERROR] Caused by: Could not transfer artifact io.github.pigaut.voxel:VoxelSpigot:pom:1.4.1 from/to jitpack.io (https://jitpack.io): status code: 401, reason phrase: Unauthorized (401)
```

## Root Cause

The Orestack API (version 1.1) events `GeneratorMineEvent` and `GeneratorHarvestEvent` extend `io.github.pigaut.voxel.event.PlayerEvent` from the VoxelSpigot library.

The OrestackAPI pom.xml declares VoxelSpigot (groupId: `io.github.pigaut.voxel`, version: 1.4.1) as a `provided` dependency. However:

1. Maven does not automatically resolve `provided` transitive dependencies during compilation
2. VoxelSpigot is **not available** in Maven Central despite the groupId suggesting it should be
3. VoxelSpigot cannot be downloaded from JitPack (returns 401 Unauthorized)
4. The artifact is not available in any accessible Maven repository

## Solution

Since VoxelSpigot is not available through Maven repositories but is required for compilation, we:

1. **Excluded VoxelSpigot** from the OrestackAPI dependency to prevent Maven from attempting to download it:

```xml
<!-- Orestack API - https://github.com/pigaut/Orestack -->
<dependency>
    <groupId>io.github.pigaut.orestack.api</groupId>
    <artifactId>OrestackAPI</artifactId>
    <version>1.1</version>
    <scope>provided</scope>
    <exclusions>
        <!-- Exclude VoxelSpigot as it's not available in Maven repositories -->
        <exclusion>
            <groupId>io.github.pigaut.voxel</groupId>
            <artifactId>VoxelSpigot</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

2. **Created a stub class** for `io.github.pigaut.voxel.event.PlayerEvent` to satisfy compilation requirements:

Location: `src/main/java/io/github/pigaut/voxel/event/PlayerEvent.java`

This stub provides the minimal interface needed for compilation. At runtime, the actual implementation will be provided by the Orestack plugin.

## Dependency Chain

```
Quantum Project
  └─ OrestackAPI 1.1 (provided)
      └─ VoxelSpigot 1.4.1 (excluded) ← Stub provided in source code
```

## Repository Availability

- ✅ `io.github.pigaut.orestack.api:OrestackAPI:1.1` - Available on Maven Central
- ❌ `io.github.pigaut.voxel:VoxelSpigot:1.4.1` - NOT available in any Maven repository

## Files Modified

- `pom.xml`: Added exclusion for VoxelSpigot dependency in OrestackAPI
- `src/main/java/io/github/pigaut/voxel/event/PlayerEvent.java`: Created stub class for compilation
- `src/main/java/com/wynvers/quantum/jobs/OrestackJobListener.java`: No changes needed (code was correct)

## Verification

To verify the fix works:

```bash
mvn clean compile
```

The VoxelSpigot dependency error should no longer appear. The build will succeed if all other dependencies are accessible.

## Runtime Behavior

At runtime, the actual VoxelSpigot classes will be provided by the Orestack plugin when it loads. The stub class in the source code will be replaced by the plugin's implementation, allowing the Orestack integration to work correctly.

## Important Note

This solution assumes that:
1. The Orestack plugin is installed on the server
2. The Orestack plugin provides the VoxelSpigot classes at runtime
3. The stub class signature matches the actual VoxelSpigot PlayerEvent class

If the Orestack plugin is not available at runtime, the OrestackJobListener will not function, but the plugin will still load successfully.
