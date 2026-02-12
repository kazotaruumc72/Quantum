# BetterHud Integration pour Quantum

## Vue d'ensemble

Ce document décrit l'intégration de BetterHud dans le plugin Quantum, conformément à la demande d'inclure BetterHud avec les mêmes fonctionnalités et des optimisations supplémentaires dans un dossier séparé.

## Structure du Projet

### Dossier Séparé
```
Quantum/
├── betterhud/                           # Dossier séparé pour BetterHud (documentation & config standalone)
│   ├── pom.xml                          # Configuration Maven standalone
│   ├── README.md                        # Documentation complète en anglais
│   └── src/main/java/com/wynvers/quantum/betterhud/
│       ├── QuantumBetterHudManager.java
│       ├── QuantumCompassManager.java
│       ├── BetterHudListener.java
│       └── BetterHudUtil.java
│
└── src/main/java/com/wynvers/quantum/
    ├── betterhud/                       # Code intégré dans le plugin principal
    │   ├── QuantumBetterHudManager.java
    │   ├── QuantumCompassManager.java
    │   ├── BetterHudListener.java
    │   ├── BetterHudUtil.java
    │   └── README.md
    └── commands/
        └── HudDemoCommand.java          # Commande de démonstration
```

## Fonctionnalités Incluses

### 1. Toutes les Fonctionnalités de BetterHud
✅ **Système HUD côté serveur** - Pas besoin de mods clients
✅ **Génération automatique de resource packs**
✅ **Affichage d'images** (PNG, GIF, séquences)
✅ **Affichage de texte** avec formatage
✅ **Têtes de joueurs** personnalisées
✅ **Système d'animation** complet
✅ **Système de popups** avec variables
✅ **Système de boussole/waypoints** pour la navigation
✅ **Placeholders** avec expressions
✅ **Hot reload** sans redémarrage du serveur

### 2. Optimisations Ajoutées

#### a) Cache de Joueurs Optimisé
- **QuantumBetterHudManager** utilise un `ConcurrentHashMap` pour mettre en cache les instances `HudPlayer`
- Évite les appels répétés à l'API BetterHud
- Thread-safe pour les environnements multi-threadés
- Nettoyage automatique à la déconnexion des joueurs

#### b) Système de Cooldown pour les Popups
- Cooldown configurable (100ms par défaut) entre les affichages du même popup
- Évite le spam de popups aux joueurs
- Cache des timestamps des derniers affichages
- Protection contre les boucles infinies

#### c) Gestion Optimisée des Waypoints
- Cache local des waypoints actifs par joueur
- Suivi en mémoire pour éviter les requêtes répétées
- Méthodes de gestion en lot (clear all)
- Liste complète des waypoints disponible

#### d) Utilitaires de Performance
```java
// Formatage de nombres avec suffixes (K, M, B)
BetterHudUtil.formatNumber(1500000) // "1.5M"

// Barres de vie avec caractères Unicode
BetterHudUtil.getHealthBar(current, max, 10) // "████████░░"
BetterHudUtil.getColoredHealthBar(current, max, 10) // Coloré selon %

// Pourcentages formatés
BetterHudUtil.formatPercentage(75, 100) // "75%"

// Création rapide de variables
BetterHudUtil.createVariables("key1", "value1", "key2", "value2")
```

## Utilisation dans le Code

### Initialisation Automatique
Le système BetterHud est initialisé automatiquement au démarrage du plugin :

```java
// Dans Quantum.java - initializeNewSystems()
if (Bukkit.getPluginManager().getPlugin("BetterHud") != null) {
    this.betterHudManager = new QuantumBetterHudManager(this);
    this.compassManager = new QuantumCompassManager(betterHudManager, logger.getLogger());
    
    // Initialisation avec délai pour assurer le chargement complet
    Bukkit.getScheduler().runTaskLater(this, () -> {
        betterHudManager.initialize();
    }, 20L);
    
    // Enregistrement du listener de nettoyage
    getServer().getPluginManager().registerEvents(
        new BetterHudListener(betterHudManager, compassManager), 
        this
    );
    
    logger.success("✓ BetterHud Integration initialized!");
}
```

### Accès aux Managers
```java
// Depuis n'importe quelle classe avec accès au plugin
QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
QuantumCompassManager compassManager = plugin.getCompassManager();

// Vérifier la disponibilité
if (hudManager != null && hudManager.isAvailable()) {
    // Utiliser BetterHud
}
```

### Exemples d'Utilisation

#### 1. Afficher un Popup Simple
```java
hudManager.showPopup(player, "welcome_popup");
```

#### 2. Popup avec Variables
```java
Map<String, String> vars = BetterHudUtil.createVariables(
    "player_name", player.getName(),
    "coins", BetterHudUtil.formatNumber(playerCoins),
    "health", BetterHudUtil.formatPercentage(player.getHealth(), 20)
);
hudManager.showPopup(player, "stats_popup", vars);
```

#### 3. Ajouter un Waypoint
```java
Location targetLoc = new Location(world, 100, 64, 200);
compassManager.addWaypoint(player, "treasure", targetLoc);

// Avec icône personnalisée
compassManager.addWaypoint(player, "home", homeLocation, "house_icon");
```

#### 4. Gérer les Waypoints
```java
// Supprimer un waypoint
compassManager.removeWaypoint(player, "treasure");

// Effacer tous les waypoints
compassManager.clearWaypoints(player);

// Lister les waypoints actifs
Map<String, CompassPoint> waypoints = compassManager.getWaypoints(player);
```

## Commande de Démonstration

La commande `/huddemo` permet de tester toutes les fonctionnalités :

### Syntaxe
```
/huddemo popup <name> [key:value...]    - Afficher un popup
/huddemo waypoint add <name> [icon]     - Ajouter un waypoint
/huddemo waypoint remove <name>         - Supprimer un waypoint
/huddemo waypoint clear                 - Effacer tous les waypoints
/huddemo waypoint list                  - Lister les waypoints
/huddemo test                           - Popup de test avec variables
```

### Exemples
```bash
# Popup simple
/huddemo popup welcome

# Popup avec variables
/huddemo popup stats player:Notch coins:1500000

# Waypoint
/huddemo waypoint add tresor
/huddemo waypoint add maison house_icon
/huddemo waypoint list
/huddemo waypoint remove tresor
```

## Configuration Maven

### Dépendances Ajoutées
```xml
<!-- BetterHud Standard API -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterHud-standard-api</artifactId>
    <version>1.14.1</version>
    <scope>provided</scope>
</dependency>

<!-- BetterHud Bukkit API -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterHud-bukkit-api</artifactId>
    <version>1.14.1</version>
    <scope>provided</scope>
</dependency>

<!-- BetterCommand (requis par BetterHud) -->
<dependency>
    <groupId>io.github.toxicity188</groupId>
    <artifactId>BetterCommand</artifactId>
    <version>1.4.3</version>
    <scope>provided</scope>
</dependency>

<!-- Kotlin Standard Library (requis par BetterHud) -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>2.1.0</version>
    <scope>provided</scope>
</dependency>
```

### Maven Shade Plugin
Les dépendances Kotlin et BetterCommand sont relocalisées pour éviter les conflits :

```xml
<relocations>
    <!-- Relocate kotlin stdlib to avoid conflicts -->
    <relocation>
        <pattern>kotlin</pattern>
        <shadedPattern>com.wynvers.quantum.shaded.kotlin</shadedPattern>
    </relocation>
    <!-- Relocate BetterHud dependencies -->
    <relocation>
        <pattern>kr.toxicity.command</pattern>
        <shadedPattern>com.wynvers.quantum.shaded.bettercommand</shadedPattern>
    </relocation>
</relocations>
```

## Configuration plugin.yml

### Soft Dependency
```yaml
softdepend:
  - BetterHud
```

### Commande
```yaml
huddemo:
  description: BetterHud demo commands (popups and waypoints)
  usage: /huddemo <popup|waypoint|test> [args]
  aliases: [hud, betterhud]
  permission: quantum.betterhud.use
```

### Permissions
```yaml
quantum.betterhud.*:
  description: All BetterHud integration permissions
  default: true
  children:
    quantum.betterhud.use: true

quantum.betterhud.use:
  description: Use BetterHud demo commands
  default: true
```

## Architecture Technique

### Classes Principales

#### 1. QuantumBetterHudManager
- **Rôle** : Manager principal pour les HUDs et popups
- **Optimisations** :
  - Cache `ConcurrentHashMap<UUID, HudPlayer>` pour les joueurs
  - Système de cooldown pour les popups
  - Méthodes thread-safe
- **Méthodes clés** :
  - `initialize()` : Initialisation de l'API BetterHud
  - `showPopup(player, name, variables)` : Afficher un popup
  - `getHudPlayer(player)` : Obtenir un HudPlayer (avec cache)
  - `removePlayer(player)` : Nettoyage à la déconnexion

#### 2. QuantumCompassManager
- **Rôle** : Gestion des waypoints/boussole
- **Optimisations** :
  - Cache local des waypoints actifs
  - Suivi des points de boussole par joueur
- **Méthodes clés** :
  - `addWaypoint(player, name, location, icon)` : Ajouter un waypoint
  - `removeWaypoint(player, name)` : Supprimer un waypoint
  - `clearWaypoints(player)` : Effacer tous les waypoints
  - `getWaypoints(player)` : Liste des waypoints actifs

#### 3. BetterHudListener
- **Rôle** : Gestion des événements
- **Fonctions** :
  - Nettoyage automatique à la déconnexion
  - Libération de la mémoire des caches

#### 4. BetterHudUtil
- **Rôle** : Utilitaires et méthodes d'aide
- **Fonctions** :
  - Formatage de nombres (K, M, B)
  - Création rapide de maps de variables
  - Génération de barres de vie
  - Calcul de pourcentages
  - Troncature de texte

## Cas d'Utilisation

### 1. Système de Niveau
```java
// Afficher un popup de level up
Map<String, String> vars = BetterHudUtil.createVariables(
    "level", String.valueOf(newLevel),
    "exp_required", BetterHudUtil.formatNumber(expRequired)
);
hudManager.showPopup(player, "level_up", vars);
```

### 2. Système de Quêtes
```java
// Ajouter un waypoint pour une quête
Location questLocation = quest.getTargetLocation();
compassManager.addWaypoint(player, "quest_" + quest.getId(), questLocation, "quest_marker");

// Supprimer à la fin de la quête
compassManager.removeWaypoint(player, "quest_" + quest.getId());
```

### 3. Système de Commerce
```java
// Popup de confirmation de vente
Map<String, String> vars = BetterHudUtil.createVariables(
    "item", itemName,
    "quantity", String.valueOf(quantity),
    "price", BetterHudUtil.formatNumber(totalPrice)
);
hudManager.showPopup(player, "sale_confirmation", vars);
```

### 4. Système de Combat
```java
// Afficher les stats de boss avec barre de vie
String healthBar = BetterHudUtil.getColoredHealthBar(
    boss.getHealth(),
    boss.getMaxHealth(),
    20
);

Map<String, String> vars = BetterHudUtil.singleVariable("boss_health", healthBar);
hudManager.showPopup(player, "boss_stats", vars);
```

## Avantages de l'Intégration

### 1. Performance
- ✅ Cache intelligent évite les appels API répétés
- ✅ Cooldown système prévient le spam
- ✅ Thread-safe pour haute concurrence
- ✅ Nettoyage automatique de la mémoire

### 2. Facilité d'Utilisation
- ✅ API simplifiée avec méthodes utilitaires
- ✅ Détection automatique de BetterHud
- ✅ Fallback gracieux si BetterHud absent
- ✅ Exemples de code complets

### 3. Maintenance
- ✅ Code séparé dans package dédié
- ✅ Documentation complète
- ✅ Versioning clair des dépendances
- ✅ Tests via commande de démonstration

### 4. Extensibilité
- ✅ Facile d'ajouter de nouvelles fonctionnalités
- ✅ Classes découplées et modulaires
- ✅ Getters publics pour accès externe
- ✅ Interface claire et cohérente

## Prérequis

### Serveur
- **Minecraft** : 1.21.11
- **Java** : 21+
- **Paper/Spigot** : 1.21.11+

### Plugins Requis
- **BetterHud** : 1.14.2+ (optionnel, détecté automatiquement)

### Plugins Compatibles
- PlaceholderAPI (pour variables étendues)
- WorldGuard (pour zones de waypoints)

## Installation

1. Installer BetterHud sur le serveur
2. Placer Quantum.jar dans `/plugins`
3. Démarrer le serveur
4. Le système BetterHud sera initialisé automatiquement

### Vérification
```
[Quantum] ✓ BetterHud Integration initialized! (Optimized HUD & Compass)
[Quantum] ✓ BetterHud Demo Command registered
```

## Dépannage

### BetterHud non détecté
- Vérifier que BetterHud.jar est dans `/plugins`
- Vérifier la version de BetterHud (1.14.2+)
- Vérifier les logs de démarrage

### Popups ne s'affichent pas
- Vérifier que le popup existe dans la config BetterHud
- Vérifier le cooldown (100ms entre affichages)
- Vérifier les variables passées

### Waypoints non visibles
- S'assurer que le HUD de boussole est configuré dans BetterHud
- Vérifier les permissions du joueur
- Vérifier les coordonnées du waypoint

## Ressources

- [BetterHud GitHub](https://github.com/toxicity188/BetterHud)
- [BetterHud Documentation](https://deepwiki.com/toxicity188/BetterHud)
- [API Usage Guide](https://deepwiki.com/toxicity188/BetterHud/12.1-using-the-api)

## Support

Pour toute question ou problème :
1. Consulter la documentation BetterHud
2. Vérifier les logs du serveur
3. Tester avec `/huddemo test`
4. Consulter le README.md dans `betterhud/` pour plus de détails

## Licence

L'intégration BetterHud suit la même licence que le plugin Quantum. BetterHud lui-même est sous licence MIT.
