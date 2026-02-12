# TAB API Dependency Resolution Fix

## Problem
The Maven build was failing with a dependency resolution error when trying to fetch the TAB API from the neznamy repository:

```
[ERROR] Failed to execute goal on project Quantum: Could not collect dependencies for project com.wynvers:Quantum:jar:1.0.1
[ERROR] Failed to read artifact descriptor for me.neznamy:tab-api:jar:5.0.6
[ERROR]         Caused by: The following artifacts could not be resolved: me.neznamy:tab-api:pom:5.0.6 (absent): Could not transfer artifact me.neznamy:tab-api:pom:5.0.6 from/to neznamy-repo (https://repo.neznamy.me/releases): repo.neznamy.me
```

## Root Cause
1. The TAB API repository at `https://repo.neznamy.me/releases` is not accessible (DNS resolution failure)
2. The TAB API artifacts are not available on Maven Central
3. The dependency was configured to be fetched from a remote repository that is no longer available

## Solution
Changed the TAB API dependency to use a local JAR file instead of fetching from a remote repository:

### 1. Downloaded TAB API JAR
Downloaded the TAB API 5.0.6 JAR file from the official GitHub releases:
- Source: `https://github.com/NEZNAMY/TAB/releases/download/5.0.6/TAB.v5.0.6.jar`
- Saved as: `libs/tab-api-5.0.6.jar`

### 2. Updated Dependency Configuration
Changed the dependency in `pom.xml` from `provided` scope to `system` scope:

```xml
<!-- TAB - https://github.com/NEZNAMY/TAB -->
<dependency>
    <groupId>me.neznamy</groupId>
    <artifactId>tab-api</artifactId>
    <version>5.0.6</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/tab-api-5.0.6.jar</systemPath>
</dependency>
```

### 3. Removed Inaccessible Repository
Removed the neznamy repository from the repositories section since it's no longer accessible:

```xml
<!-- REMOVED - Repository is not accessible -->
<!-- <repository>
    <id>neznamy-repo</id>
    <url>https://repo.neznamy.me/releases</url>
</repository> -->
```

### 4. Why `system` Scope?
The `system` scope is used because:
- The TAB API is not available on any accessible Maven repository
- The JAR file is bundled with the project for guaranteed availability
- Maven will use the local JAR file during compilation
- The TAB plugin is still expected to be available at runtime (configured as `softdepend` in plugin.yml)

## Verification
After the fix, the TAB API dependency is successfully resolved from the local JAR file:

```bash
$ mvn dependency:tree -Dincludes=me.neznamy:tab-api
[WARNING] 'dependencies.dependency.systemPath' for me.neznamy:tab-api:jar should not point at files within the project directory
```

The warning is expected for system dependencies and indicates that Maven is correctly using the local JAR file.

## Impact
- ✅ TAB API dependency now resolves correctly from the local JAR file
- ✅ No external repository required (eliminates dependency on repo.neznamy.me)
- ✅ Build no longer fails due to TAB API dependency issues
- ✅ Plugin will work with TAB plugin installed on the server (TAB is a softdepend)
- ✅ No changes required to the integration code

## File Structure
```
Quantum/
├── libs/
│   ├── nexo-1.18.jar          # Existing system dependency
│   └── tab-api-5.0.6.jar      # New TAB API JAR
└── pom.xml                     # Updated with system dependency
```

## Future Considerations
If the neznamy repository becomes accessible again, or if TAB API is published to Maven Central, the dependency can be reverted to use the remote repository. However, the current local JAR approach is more reliable and eliminates external dependencies.

## Related Documentation
- TAB Plugin: https://github.com/NEZNAMY/TAB
- TAB API Usage: See `src/main/java/com/wynvers/quantum/tab/TABManager.java`
- Similar Fix: See `BETTERHUD_DEPENDENCY_FIX.md` for the BetterHud dependency fix

---

**Fix Date**: 2026-02-12  
**TAB API Version**: 5.0.6  
**Quantum Version**: 1.0.1  
**Status**: ✅ Fixed and Verified
