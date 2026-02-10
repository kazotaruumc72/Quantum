# ModelEngine Health Bar Positioning Guide

## Problème / Problem

Lorsque des mobs utilisent des modèles ModelEngine, la barre de vie doit être positionnée au-dessus du modèle custom qui peut être plus grand ou plus petit que l'entité vanilla invisible qui le porte.

When mobs use ModelEngine models, the health bar must be positioned above the custom model which can be larger or smaller than the invisible vanilla entity that carries it.

## Solution

La solution implémentée utilise des **entités TextDisplay** (Minecraft 1.19.4+) pour afficher les barres de vie :
1. Une entité TextDisplay est créée au-dessus de chaque mob avec ModelEngine
2. L'offset vertical est configuré directement en coordonnées Y (en blocs)
3. La TextDisplay suit automatiquement le mob lorsqu'il se déplace
4. Un indicateur visuel (⚙ par défaut) identifie les mobs avec modèles ModelEngine

The implemented solution uses **TextDisplay entities** (Minecraft 1.19.4+) to display health bars:
1. A TextDisplay entity is created above each mob with ModelEngine
2. The vertical offset is configured directly in Y coordinates (in blocks)
3. The TextDisplay automatically follows the mob when it moves
4. A visual indicator (⚙ by default) identifies mobs with ModelEngine models

## Indicateur Visuel ModelEngine / ModelEngine Visual Indicator

Les mobs utilisant des modèles ModelEngine affichent automatiquement un symbole spécial (⚙ par défaut) au début de leur barre de vie, permettant de les identifier rapidement.

Mobs using ModelEngine models automatically display a special symbol (⚙ by default) at the beginning of their health bar, allowing them to be quickly identified.

### Personnalisation de l'Indicateur / Customizing the Indicator

Dans `mob_healthbar.yml`, section `symbols` :

```yaml
symbols:
  modelengine:
    # Symbole affiché pour les mobs avec ModelEngine
    # Symbol displayed for mobs with ModelEngine
    indicator: "⚙"
    # Couleur de l'indicateur (codes couleur Minecraft)
    # Indicator color (Minecraft color codes)
    color: "&7"
```

**Exemples de symboles / Symbol Examples:**
- `⚙` - Engrenage (par défaut / default)
- `⚡` - Éclair / Lightning
- `✦` - Étoile / Star
- `⬟` - Hexagone / Hexagon
- `●` - Cercle plein / Filled circle
- `◆` - Diamant / Diamond

## Configuration

### 1. Configuration Globale (Global Configuration)

Dans `mob_healthbar.yml`, un offset par défaut peut être défini :

```yaml
global:
  # Offset vertical par défaut pour les modèles ModelEngine (en blocs)
  # Utilisé pour positionner la healthbar au-dessus des modèles custom
  # Valeur positive = plus haut, 0.0 = au niveau de l'entité
  default_modelengine_offset: 0.5
```

### 2. Configuration Par Mob (Per-Mob Configuration)

Pour chaque mob custom dans `mob_healthbar.yml`, ajoutez le champ `modelengine_offset` :

```yaml
"Slime d'Eau":
  enabled: true
  name_color: "&b"
  bar_length: 20
  show_percentage: true
  show_numeric: false
  format: CLASSIC
  # Offset vertical pour les modèles ModelEngine (en blocs)
  # Positionne la healthbar au-dessus du modèle
  modelengine_offset: 1.0
  color_thresholds:
    75: "&b"
    50: "&3"
    25: "&9"
    0: "&1"
```

## Comment Ajuster l'Offset (How to Adjust the Offset)

1. **Déterminez la hauteur du modèle** : Regardez votre mob ModelEngine en jeu
2. **Commencez avec une valeur de base** : 
   - Petits mobs (< 1 bloc) : `0.5 - 1.0`
   - Mobs moyens (1-2 blocs) : `1.0 - 1.5`
   - Grands mobs (> 2 blocs) : `1.5 - 2.5`
3. **Testez et ajustez** : Rechargez la config avec `/quantum reload` et ajustez l'offset
4. **Ajustement fin** : Augmentez ou diminuez par incréments de 0.1-0.2 blocs

## Fonctionnement Technique (Technical Details)

### Utilisation des TextDisplay Entities

Depuis Minecraft 1.20.5+, les newlines (`\n`) ne fonctionnent plus dans les noms custom d'entités. Le système utilise donc des entités TextDisplay pour afficher les barres de vie :

```java
// Créer une TextDisplay au-dessus du mob
TextDisplay display = world.spawn(location.add(0, yOffset, 0), TextDisplay.class);
display.setBillboard(Display.Billboard.CENTER); // Toujours face au joueur
display.text(healthBarComponent); // Définir le texte
```

Since Minecraft 1.20.5+, newlines (`\n`) no longer work in custom entity names. The system therefore uses TextDisplay entities to display health bars:

### Application de l'Offset

L'offset est appliqué directement comme coordonnée Y lors du positionnement de la TextDisplay :

```java
// L'offset est en blocs (1.0 = 1 bloc au-dessus de l'entité)
Location displayLocation = mob.getLocation().add(0, modelengine_offset, 0);
display.teleport(displayLocation);
```

**Exemples** :
- Offset 0.5 → TextDisplay à 0.5 bloc au-dessus de l'entité
- Offset 1.0 → TextDisplay à 1.0 bloc au-dessus de l'entité
- Offset 2.0 → TextDisplay à 2.0 blocs au-dessus de l'entité

### Suivi Automatique (Automatic Tracking)

Un système de mise à jour périodique (toutes les 5 ticks / 4 fois par seconde) fait suivre la TextDisplay au mob :

```java
// La TextDisplay se téléporte pour suivre le mob qui bouge
display.teleport(mob.getLocation().add(0, yOffset, 0));
```

### Priorité de Configuration

1. Si `modelengine_offset` est défini pour le mob spécifique → utilise cette valeur
2. Sinon → utilise `global.default_modelengine_offset`
3. Si aucun offset défini → `0.5` (offset par défaut)

## Exemples de Configuration (Configuration Examples)

### Petit Mob (Small Mob) - Slime

```yaml
"Slime d'Eau":
  modelengine_offset: 1.0  # 1 bloc au-dessus
```

### Mob Moyen (Medium Mob) - Gardien

```yaml
"Gardien de l'Eau":
  modelengine_offset: 1.2  # 1.2 blocs au-dessus
```

### Grand Mob (Large Mob) - Serviteur

```yaml
"Serviteur d'Eau":
  modelengine_offset: 1.5  # 1.5 blocs au-dessus
```

### Boss (Boss)

```yaml
"⚔ Chevalier de l'Eau ⚔":
  modelengine_offset: 2.0  # 2 blocs au-dessus
```

## Commandes (Commands)

- `/quantum reload` - Recharge la configuration (y compris les offsets)
- `/quantum healthbar <mode>` - Change le mode d'affichage de la healthbar

## Notes Importantes (Important Notes)

- L'offset ne s'applique QUE si ModelEngine est installé ET l'entité a un modèle
- Les entités vanilla (sans ModelEngine) utilisent un offset par défaut de 0.5 bloc
- L'offset est en blocs Minecraft (1.0 = 1 bloc de hauteur)
- Les valeurs sont toujours positives (la healthbar est au-dessus du mob)
- La healthbar suit automatiquement le mob lorsqu'il se déplace
- Les TextDisplay entities sont automatiquement nettoyées quand le mob meurt

## Dépannage (Troubleshooting)

### La healthbar est trop haute
→ Diminuez la valeur de `modelengine_offset` (ex: de 1.5 à 1.2)

### La healthbar est trop basse
→ Augmentez la valeur de `modelengine_offset` (ex: de 1.0 à 1.3)

### La healthbar ne change pas après modification
→ Utilisez `/quantum reload` pour recharger la configuration

### L'offset ne s'applique pas
→ Vérifiez que :
  1. ModelEngine est installé et chargé
  2. Le mob a bien un modèle ModelEngine appliqué
  3. Le nom du mob dans `mob_healthbar.yml` correspond EXACTEMENT au nom configuré dans `towers.yml`

### La healthbar ne suit pas le mob
→ C'est normal, elle se met à jour toutes les 5 ticks (0.25 secondes). Si le problème persiste, redémarrez le serveur.

## Différences avec l'Ancienne Implémentation

### Avant (Minecraft 1.19 et antérieurs)
- Utilisait des newlines (`\n`) dans setCustomName()
- L'offset était approximatif et dépendait du client
- Ne fonctionnait plus à partir de Minecraft 1.20.5+

### Maintenant (Minecraft 1.21+)
- Utilise des entités TextDisplay
- L'offset est précis et en coordonnées réelles
- Compatible avec Minecraft 1.19.4+ et futures versions
- Meilleur suivi du mouvement des mobs
- Nettoyage automatique des affichages

## Fichiers Modifiés (Modified Files)

- `src/main/java/com/wynvers/quantum/healthbar/HealthBarManager.java` - Utilisation de TextDisplay
- `src/main/java/com/wynvers/quantum/healthbar/HealthBarListener.java` - Cleanup des TextDisplay
- `src/main/java/com/wynvers/quantum/Quantum.java` - Shutdown du HealthBarManager
- `src/main/resources/mob_healthbar.yml` - Configuration des offsets
