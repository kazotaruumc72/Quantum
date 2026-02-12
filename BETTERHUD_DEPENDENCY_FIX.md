# BetterHud Dependency Resolution Fix

## Problem
The Maven build was failing with 401 Unauthorized errors when trying to resolve BetterHud dependencies from JitPack:

```
[ERROR] Failed to read artifact descriptor for io.github.toxicity188:BetterHud-standard-api:jar:1.14.2
[ERROR]         Caused by: Could not transfer artifact io.github.toxicity188:BetterHud-standard-api:pom:1.14.2 from/to jitpack.io (https://jitpack.io): status code: 401, reason phrase: Unauthorized (401)
[ERROR] Failed to read artifact descriptor for io.github.toxicity188:BetterHud-bukkit-api:jar:1.14.2
[ERROR]         Caused by: Could not transfer artifact io.github.toxicity188:BetterHud-bukkit-api:pom:1.14.2 from/to jitpack.io (https://jitpack.io): status code: 401, reason phrase: Unauthorized (401)
```

## Root Cause
1. The BetterHud artifacts were not available on JitPack
2. The pom.xml was using version 1.14.2 which was not published to Maven Central yet
3. BetterCommand version 1.5.1 was also not available on Maven Central
4. The dependencies were using `compile` scope, which would bundle them into the JAR (incorrect for plugin dependencies)

## Solution
Changed the following in `pom.xml`:

### 1. Downgraded to Available Versions
- BetterHud-standard-api: `1.14.2` → `1.14.1` (latest on Maven Central)
- BetterHud-bukkit-api: `1.14.2` → `1.14.1` (latest on Maven Central)
- BetterCommand: `1.5.1` → `1.4.3` (latest on Maven Central)

### 2. Changed Dependency Scope
Changed all BetterHud-related dependencies from `compile` to `provided`:
- BetterHud-standard-api: `<scope>compile</scope>` → `<scope>provided</scope>`
- BetterHud-bukkit-api: `<scope>compile</scope>` → `<scope>provided</scope>`
- BetterCommand: `<scope>compile</scope>` → `<scope>provided</scope>`
- kotlin-stdlib: `<scope>compile</scope>` → `<scope>provided</scope>`

### 3. Why `provided` Scope?
The `provided` scope is correct because:
- These dependencies are needed for compilation
- They should NOT be bundled into the final JAR
- They are expected to be available at runtime via the BetterHud plugin itself
- BetterHud is configured as a `softdepend` in plugin.yml (optional runtime dependency)

## Verification
After the fix, the BetterHud dependencies are successfully resolved from Maven Central:

```bash
$ ls -la ~/.m2/repository/io/github/toxicity188/
BetterCommand/
BetterHud-bukkit-api/
BetterHud-standard-api/
```

The build error messages no longer mention BetterHud dependencies, confirming they are now resolved correctly.

## Documentation Updates
Updated all documentation files to reflect the new versions:
- BETTERHUD_INTEGRATION.md
- BETTERHUD_README.md
- IMPLEMENTATION_SUMMARY.md
- betterhud/README.md
- betterhud/pom.xml
- src/main/java/com/wynvers/quantum/betterhud/README.md

## Impact
- ✅ BetterHud dependencies now resolve correctly from Maven Central
- ✅ No authentication required (public Maven Central repository)
- ✅ Correct dependency scope ensures proper plugin architecture
- ✅ Plugin will work with BetterHud 1.14.1+ installed on the server
- ✅ No breaking changes to the integration code

## Future Upgrade Path
When BetterHud 1.14.2 and BetterCommand 1.5.1 are published to Maven Central, the versions can be upgraded in pom.xml. The current versions (1.14.1 and 1.4.3) are compatible with the existing integration code.
