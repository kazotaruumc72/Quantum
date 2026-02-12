# Configuration BetterHud pour Quantum

Ce dossier contient des exemples de configuration pour l'intégration BetterHud avec le plugin Quantum.

## 📁 Fichiers de Configuration

### `config.yml`
Configuration principale de BetterHud avec les paramètres optimisés pour Quantum.

**Installation** : Copiez dans `plugins/BetterHud/config.yml`

### `popups.yml`
Définit tous les popups (notifications) utilisés par Quantum :
- Notifications de niveau (level up, job level up)
- Notifications de stockage (ajout/retrait d'items)
- Notifications d'ordres (création, complétion)
- Notifications de tour (progression)
- Notifications de zone (entrée/sortie)
- Notifications d'économie (argent reçu/dépensé)
- Notifications génériques (succès, erreur, avertissement)

**Installation** : Copiez dans `plugins/BetterHud/popups/quantum_popups.yml`

### `huds.yml`
Définit les différents HUDs affichés selon le contexte :
- `quantum_main` : HUD principal avec infos joueur, stats, économie
- `quantum_storage` : HUD du système de stockage
- `quantum_job` : Progression du métier actuel
- `quantum_tower` : Informations de la tour
- `quantum_boss` : Barre de vie du boss
- `quantum_orders` : Statut des ordres actifs
- `quantum_pvp` : Stats PvP dans les zones de combat
- `quantum_zone` : Informations de zone
- `quantum_party` : Informations du groupe
- `quantum_dungeon` : HUD de donjon
- `quantum_scoreboard` : Scoreboard complet
- `quantum_minimal` : HUD minimaliste

**Installation** : Copiez dans `plugins/BetterHud/huds/quantum_huds.yml`

### `compass.yml`
Définit les waypoints et marqueurs de compass :
- Boussole directionnelle
- Waypoints personnalisés
- Marqueurs de quêtes
- Marqueurs de tour, donjon, shop
- Marqueur de maison (home)
- Marqueur de point de mort
- Marqueurs de membres du groupe
- Marqueurs de PNJ proches
- Indicateurs de frontière de zone

**Installation** : Copiez dans `plugins/BetterHud/compass/quantum_compass.yml`

## 🚀 Installation Rapide

1. **Installez BetterHud** sur votre serveur Minecraft 1.21.11

2. **Copiez les fichiers de configuration** :
   ```bash
   cp config.yml plugins/BetterHud/config.yml
   cp popups.yml plugins/BetterHud/popups/quantum_popups.yml
   cp huds.yml plugins/BetterHud/huds/quantum_huds.yml
   cp compass.yml plugins/BetterHud/compass/quantum_compass.yml
   ```

3. **Redémarrez le serveur** ou utilisez `/betterhud reload`

4. **Testez avec Quantum** :
   ```
   /huddemo popup test_popup
   /huddemo waypoint add test
   ```

## ⚙️ Personnalisation

### Modifier les Couleurs

Les configurations utilisent le format MiniMessage de BetterHud :
- `<gradient:#COLOR1:#COLOR2>text</gradient>` : Dégradé de couleurs
- `<white>`, `<yellow>`, `<gold>`, `<green>`, etc. : Couleurs simples
- `<bold>`, `<italic>`, `<underlined>` : Formatage du texte

**Exemple** :
```yaml
text:
  - "<gradient:#FF0000:#00FF00><bold>Mon Texte</bold></gradient>"
```

### Modifier les Positions

Utilisez les coordonnées `x` et `y` pour positionner les éléments :
- `x: 0, y: 0` = Centre de l'écran
- `x: 10, y: 10` = Haut gauche (10px de chaque côté)
- `x: -10, y: -10` = Bas droite (10px de chaque côté)

Utilisez `align: CENTER`, `align: LEFT`, ou `align: RIGHT` pour l'alignement.

### Modifier les Durées

Pour les popups, ajustez les durées d'animation (en ticks, 20 ticks = 1 seconde) :
```yaml
animation:
  fade-in: 10    # Durée d'apparition (0.5s)
  stay: 60       # Durée d'affichage (3s)
  fade-out: 10   # Durée de disparition (0.5s)
```

## 🔧 PlaceholderAPI

Pour utiliser les placeholders Quantum, installez **PlaceholderAPI** et les expansions nécessaires :

```
/papi ecloud download Player
/papi ecloud download Server
/papi ecloud download Statistic
/papi ecloud download Vault
/papi reload
```

### Placeholders Quantum Personnalisés

Si vous développez des placeholders personnalisés pour Quantum, enregistrez-les dans la classe principale :

```java
// Dans Quantum.java
if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    new QuantumPlaceholders(this).register();
}
```

## 📊 Variables Disponibles

### Variables Joueur
- `${player_name}` : Nom du joueur
- `${player_level}` : Niveau du joueur
- `${health}`, `${max_health}` : Santé
- `${food_level}` : Niveau de nourriture

### Variables Quantum
- `${money}` : Argent du joueur (via Vault)
- `${job_name}`, `${job_level}` : Métier actuel
- `${storage_items}`, `${storage_capacity}` : Stockage
- `${tower_floor}` : Étage de la tour
- `${active_orders}` : Nombre d'ordres actifs

### Variables de Position
- `${x}`, `${y}`, `${z}` : Coordonnées
- `${world_name}` : Nom du monde
- `${direction}` : Direction (N, S, E, W)
- `${biome}` : Biome actuel

## 🎨 Groupes de HUD

Créez des profils différents pour différents types de joueurs :

```yaml
hud-groups:
  default:
    - quantum_main
    - quantum_job
  
  minimal:
    - quantum_minimal
  
  hardcore:
    - quantum_main
    - quantum_pvp
    - quantum_boss
```

Assignez les joueurs aux groupes via permissions : `betterhud.group.default`

## 🔊 Sons

Tous les popups peuvent avoir des sons personnalisés :

```yaml
sound:
  key: "minecraft:entity.experience_orb.pickup"
  volume: 1.0
  pitch: 1.0
```

Consultez la [liste des sons Minecraft](https://minecraft.fandom.com/wiki/Sounds.json) pour les clés disponibles.

## 🐛 Dépannage

### Les popups ne s'affichent pas
1. Vérifiez que BetterHud est installé et chargé
2. Vérifiez les permissions : `quantum.betterhud.use`
3. Testez avec `/huddemo popup test_popup`
4. Vérifiez les logs : `plugins/BetterHud/logs/`

### Les HUDs ne se mettent pas à jour
1. Vérifiez `update-interval` dans `config.yml`
2. Vérifiez que PlaceholderAPI est installé pour les placeholders
3. Utilisez `/betterhud reload` après modification

### Les couleurs ne s'affichent pas
1. Assurez-vous d'utiliser le format MiniMessage (`<color>`) et non legacy (`&`)
2. Si vous utilisez legacy, activez `use-legacy-colors: true` dans config.yml

### Problèmes de performance
1. Augmentez `update-interval` (valeurs plus élevées = moins de mises à jour)
2. Activez `cache-player-data: true`
3. Activez `async-updates: true`
4. Réduisez le nombre de HUDs actifs simultanément

## 📚 Ressources

- [Wiki BetterHud](https://github.com/toxicity188/BetterHud/wiki)
- [Documentation Quantum](../BETTERHUD_INTEGRATION.md)
- [MiniMessage Format](https://docs.advntr.dev/minimessage/format.html)
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)

## 💡 Exemples d'Intégration

### Afficher un popup lors d'un level up

```java
QuantumBetterHudManager hudManager = plugin.getBetterHudManager();
Map<String, String> vars = BetterHudUtil.createVariables(
    "level", String.valueOf(newLevel),
    "player", player.getName()
);
hudManager.showPopup(player, "level_up", vars);
```

### Ajouter un waypoint personnalisé

```java
QuantumCompassManager compassManager = plugin.getCompassManager();
Location targetLocation = new Location(world, x, y, z);
compassManager.addWaypoint(player, "ma_quete", targetLocation);
```

### Mettre à jour le HUD

```java
hudManager.updateHud(player, null, UpdateEvent.EMPTY);
```

## 📝 Notes

- Toutes les configurations utilisent le format YAML
- Les commentaires (lignes commençant par `#`) sont ignorés
- Respectez l'indentation (espaces, pas de tabulations)
- Les couleurs utilisent le format hexadécimal : `#RRGGBB`
- Les coordonnées sont en pixels relatifs à la position de référence

## 🆕 Mises à Jour

Ces configurations sont compatibles avec :
- **BetterHud** : 1.14.1+
- **Minecraft** : 1.21.11
- **Quantum** : 1.0.1+

Vérifiez régulièrement les mises à jour de BetterHud pour de nouvelles fonctionnalités.

## 📧 Support

Pour toute question ou problème :
1. Consultez d'abord ce README
2. Vérifiez la documentation de Quantum
3. Créez une issue sur GitHub
4. Contactez l'équipe de développement
