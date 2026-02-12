# TAB Plugin Integration for Minecraft 1.21.11

## Overview
This project integrates the TAB plugin (https://github.com/NEZNAMY/TAB/) for enhanced player tablist, nametags, and scoreboard features.

## Version
- **TAB API Version**: 5.5.0
- **Minecraft Version**: 1.21.11
- **Paper API Version**: 1.21.11-R0.1-SNAPSHOT

## Changes Made

### 1. Updated TAB API Dependency
- **Previous**: TAB API 5.0.6 (system dependency with local JAR)
- **Current**: TAB API 5.5.0 (Maven dependency from JitPack)

### 2. Dependency Configuration
The TAB API is now loaded from JitPack repository:

```xml
<dependency>
    <groupId>com.github.NEZNAMY</groupId>
    <artifactId>TAB-API</artifactId>
    <version>5.5.0</version>
    <scope>provided</scope>
</dependency>
```

### 3. Benefits of TAB API 5.5.0
- ✅ Official support for Minecraft 1.21.11
- ✅ Support for relational conditions
- ✅ Improved performance with packet listeners
- ✅ Enhanced MiniMessage support
- ✅ Support for less than 80 players / 4 columns in Layout
- ✅ Fixed various bugs from previous versions

## Building the Project

### Prerequisites
1. Java 21 or higher
2. Maven 3.6 or higher
3. Internet connection (for downloading dependencies)

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Build the plugin JAR
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

## TAB Plugin Installation

### Server Setup
1. Download TAB plugin v5.5.0 from:
   - GitHub: https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
   - SpigotMC: https://www.spigotmc.org/resources/57806/
   - Modrinth: https://modrinth.com/plugin/tab-was-taken

2. Place `TAB-v5.5.0.jar` in your server's `plugins/` folder

3. Place the compiled `Quantum-1.0.1.jar` in your server's `plugins/` folder

4. Start your server

## Custom Placeholders
The Quantum plugin registers the following custom placeholders with TAB:

- `%quantum_level%` - Player's Quantum level
- `%quantum_job%` - Player's current job
- `%quantum_job_level%` - Player's job level
- `%quantum_tower%` - Player's current tower
- `%quantum_tower_floor%` - Player's tower floor progress

These placeholders can be used in TAB's configuration files for:
- Tablist names
- Nametags
- Scoreboard
- Header/Footer

## Example TAB Configuration

```yaml
tablist-name-formatting:
  enabled: true
  prefix: "&7[Lvl %quantum_level%] "
  suffix: " &8| &e%quantum_job%"
```

## Troubleshooting

### TAB Integration Not Working
1. Ensure TAB plugin is installed and running
2. Check console logs for TAB initialization messages
3. Verify TAB plugin version is 5.5.0 or compatible

### Build Failures
1. Ensure you have internet connection for Maven dependencies
2. Clear Maven cache: `rm -rf ~/.m2/repository/com/github/NEZNAMY`
3. Try force update: `mvn clean install -U`

## References
- TAB Plugin: https://github.com/NEZNAMY/TAB/
- TAB Wiki: https://github.com/NEZNAMY/TAB/wiki
- TAB API Documentation: https://github.com/NEZNAMY/TAB/wiki/Developer-API
- Release Notes (5.5.0): https://github.com/NEZNAMY/TAB/releases/tag/5.5.0
