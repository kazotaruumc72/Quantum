# ModelEngine Health Bar Positioning Guide

## Problème / Problem

Lorsque des mobs utilisent des modèles ModelEngine, la barre de vie apparaît à la mauvaise hauteur car le modèle visuel peut être plus grand ou plus petit que l'entité vanilla invisible qui le porte.

When mobs use ModelEngine models, the health bar appears at the wrong height because the visual model can be larger or smaller than the invisible vanilla entity that carries it.

## Solution

La solution implémentée ajoute deux fonctionnalités principales :
1. Un système d'offset vertical configurable pour positionner correctement les barres de vie au-dessus des modèles ModelEngine
2. Un indicateur visuel sur la healthbar pour identifier facilement les mobs avec modèles ModelEngine

The implemented solution adds two main features:
1. A configurable vertical offset system to correctly position health bars above ModelEngine models
2. A visual indicator on the healthbar to easily identify mobs with ModelEngine models

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
  # Valeur négative = plus bas, positive = plus haut
  # 0.0 = désactivé (utilise la position native de l'entité)
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
  # Ajuste la hauteur d'affichage de la healthbar
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
4. **Conversion** : Chaque ligne de texte (newline) ajoute environ 0.3 blocs de hauteur verticale. Le calcul est déterministe (offset / 0.3 = nombre de lignes), mais le rendu visuel peut varier selon les paramètres du client. Exemples :
   - Offset 0.3 blocs → 1 ligne vide
   - Offset 0.9 blocs → 3 lignes vides
   - Offset 1.5 blocs → 5 lignes vides

## Fonctionnement Technique (Technical Details)

### Détection ModelEngine

Le système détecte automatiquement si une entité utilise un modèle ModelEngine :

```java
boolean hasModelEngine = ModelEngineAPI.getModeledEntity(entity.getUniqueId()) != null;
```

### Application de l'Offset

L'offset est appliqué en ajoutant des lignes vides (`\n`) au-dessus du nom du mob. Le calcul utilise une constante de conversion (VERTICAL_SPACING_PER_NEWLINE = 0.3) qui représente l'espacement vertical théorique ajouté par chaque newline :

```java
// Chaque '\n' ajoute théoriquement 0.3 blocs de hauteur verticale
int numLines = (int) Math.round(offset / VERTICAL_SPACING_PER_NEWLINE);
String offsetNewlines = "\n".repeat(Math.max(0, numLines));
String newName = offsetNewlines + originalName + "\n" + healthBar;
```

**Exemples de conversion** (calcul déterministe) :
- Offset 0.3 blocs → 1 ligne vide (0.3 / 0.3 = 1)
- Offset 0.9 blocs → 3 lignes vides (0.9 / 0.3 = 3)
- Offset 1.5 blocs → 5 lignes vides (1.5 / 0.3 = 5)

**Note** : Le calcul du nombre de lignes est déterministe, mais l'espacement visuel réel peut varier selon la résolution et les paramètres d'affichage du client Minecraft. La constante 0.3 est basée sur des observations empiriques et fonctionne bien dans la plupart des cas. Si la healthbar n'est pas parfaitement positionnée, ajustez l'offset par incréments de 0.3.

### Priorité de Configuration

1. Si `modelengine_offset` est défini pour le mob spécifique → utilise cette valeur
2. Sinon → utilise `global.default_modelengine_offset`
3. Si aucun offset défini → `0.0` (pas d'offset)

## Exemples de Configuration (Configuration Examples)

### Petit Mob (Small Mob) - Slime

```yaml
"Slime d'Eau":
  modelengine_offset: 1.0  # Un peu au-dessus
```

### Mob Moyen (Medium Mob) - Gardien

```yaml
"Gardien de l'Eau":
  modelengine_offset: 1.2  # Hauteur moyenne
```

### Grand Mob (Large Mob) - Serviteur

```yaml
"Serviteur d'Eau":
  modelengine_offset: 1.5  # Plus haut
```

### Boss (Boss)

```yaml
"⚔ Chevalier de l'Eau ⚔":
  modelengine_offset: 2.0  # Très haut
```

## Commandes (Commands)

- `/quantum reload` - Recharge la configuration (y compris les offsets)
- `/quantum healthbar <mode>` - Change le mode d'affichage de la healthbar

## Notes Importantes (Important Notes)

- L'offset ne s'applique QUE si ModelEngine est installé ET l'entité a un modèle
- Les entités vanilla (sans ModelEngine) ne sont pas affectées
- L'offset est en blocs Minecraft (1.0 = 1 bloc de hauteur)
- Des valeurs négatives déplaceront la healthbar vers le bas
- La valeur `0.0` désactive complètement l'offset

## Dépannage (Troubleshooting)

### La healthbar est trop haute
→ Diminuez la valeur de `modelengine_offset` (ex: de 1.5 à 1.0)

### La healthbar est trop basse
→ Augmentez la valeur de `modelengine_offset` (ex: de 1.0 à 1.5)

### La healthbar ne change pas après modification
→ Utilisez `/quantum reload` pour recharger la configuration

### L'offset ne s'applique pas
→ Vérifiez que :
  1. ModelEngine est installé et chargé
  2. Le mob a bien un modèle ModelEngine appliqué
  3. Le nom du mob dans `mob_healthbar.yml` correspond EXACTEMENT au `display_name` dans `towers.yml`

## Fichiers Modifiés (Modified Files)

- `src/main/java/com/wynvers/quantum/healthbar/HealthBarManager.java` - Logique d'offset
- `src/main/java/com/wynvers/quantum/towers/TowerSpawnerConfig.java` - Support d'offset
- `src/main/resources/mob_healthbar.yml` - Configuration des offsets
- `src/main/resources/towers.yml` - Documentation
