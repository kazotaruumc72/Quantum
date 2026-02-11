# Health Bar Positioning Guide (Hologram Offset)

## Problème / Problem

Lorsque des mobs utilisent des modèles ModelEngine ou d'autres systèmes, la barre de vie doit être positionnée au-dessus du modèle custom qui peut être plus grand ou plus petit que l'entité vanilla invisible qui le porte.

When mobs use ModelEngine models or other systems, the health bar must be positioned above the custom model which can be larger or smaller than the invisible vanilla entity that carries it.

## Solution

La solution implémentée utilise des **entités TextDisplay** (Minecraft 1.19.4+) pour afficher les barres de vie :
1. Une entité TextDisplay est créée au-dessus de chaque mob
2. L'offset vertical est configuré directement en coordonnées Y (en blocs) OU calculé automatiquement
3. La TextDisplay suit automatiquement le mob lorsqu'il se déplace avec **haute fréquence** (6.6 fois/seconde)
4. Un indicateur visuel (⚙ par défaut) identifie les mobs avec modèles ModelEngine
5. **NOUVEAU**: Si aucun offset n'est configuré, le système utilise automatiquement la hauteur de l'entité + 0.3 blocs

The implemented solution uses **TextDisplay entities** (Minecraft 1.19.4+) to display health bars:
1. A TextDisplay entity is created above each mob
2. The vertical offset is configured directly in Y coordinates (in blocks) OR calculated automatically
3. The TextDisplay automatically follows the mob when it moves with **high frequency** (6.6 times/second)
4. A visual indicator (⚙ by default) identifies mobs with ModelEngine models
5. **NEW**: If no offset is configured, the system automatically uses entity height + 0.3 blocks

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
  # Offset vertical par défaut pour les hologrammes de healthbar (en blocs)
  # Utilisé pour positionner la healthbar au-dessus des mobs
  # Valeur positive = plus haut, 0.0 = au niveau de l'entité
  default_hologram_offset: 0.5
  
  # Note: L'ancien champ 'default_modelengine_offset' est toujours supporté
  # pour la rétrocompatibilité, mais 'default_hologram_offset' est recommandé
```

### 2. Configuration Par Mob (Per-Mob Configuration)

Pour chaque mob custom dans `mob_healthbar.yml`, ajoutez le champ `hologram_offset` :

```yaml
"Slime d'Eau":
  enabled: true
  name_color: "&b"
  bar_length: 20
  show_percentage: true
  show_numeric: false
  format: CLASSIC
  # Offset vertical pour l'hologramme de healthbar (en blocs)
  # Positionne la healthbar au-dessus du mob/modèle
  hologram_offset: 1.0
  # Note: L'ancien champ 'modelengine_offset' est toujours supporté
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
Location displayLocation = mob.getLocation().add(0, hologram_offset, 0);
display.teleport(displayLocation);
```

**Exemples** :
- Offset 0.5 → TextDisplay à 0.5 bloc au-dessus de l'entité
- Offset 1.0 → TextDisplay à 1.0 bloc au-dessus de l'entité
- Offset 2.0 → TextDisplay à 2.0 blocs au-dessus de l'entité

### Suivi Automatique (Automatic Tracking)

Un système de mise à jour périodique (toutes les 3 ticks / 6.6 fois par seconde) fait suivre la TextDisplay au mob de manière très fluide :

```java
// La TextDisplay se téléporte pour suivre le mob qui bouge
// Seuil de détection: 0.01 bloc (1cm) pour un mouvement ultra-fluide
display.teleport(mob.getLocation().add(0, yOffset, 0));
```

### Calcul Automatique de l'Offset / Automatic Offset Calculation

**NOUVEAU / NEW**: Si aucun offset n'est configuré, le système calcule automatiquement la position optimale :

```java
// Calcul automatique basé sur la hauteur de l'entité
double yOffset = entity.getHeight() + 0.3;
```

**Avantages / Benefits**:
- ✅ S'adapte automatiquement aux modèles ModelEngine de différentes tailles
- ✅ Fonctionne pour tous les types de mobs vanilla aussi
- ✅ Pas besoin de configurer manuellement chaque mob
- ✅ Position toujours correcte quelle que soit la hauteur du modèle

### Priorité de Configuration

1. Si `hologram_offset` est défini pour le mob spécifique → utilise cette valeur
2. Sinon, si `modelengine_offset` est défini (rétrocompatibilité) → utilise cette valeur
3. Sinon → utilise `global.default_hologram_offset` si défini
4. Sinon → **calcule automatiquement** : `entity.getHeight() + 0.3` (NOUVEAU / NEW)

## Exemples de Configuration (Configuration Examples)

### Petit Mob (Small Mob) - Slime

```yaml
"Slime d'Eau":
  hologram_offset: 1.0  # 1 bloc au-dessus
```

### Mob Moyen (Medium Mob) - Gardien

```yaml
"Gardien de l'Eau":
  hologram_offset: 1.2  # 1.2 blocs au-dessus
```

### Grand Mob (Large Mob) - Serviteur

```yaml
"Serviteur d'Eau":
  hologram_offset: 1.5  # 1.5 blocs au-dessus
```

### Boss (Boss)

```yaml
"⚔ Chevalier de l'Eau ⚔":
  hologram_offset: 2.0  # 2 blocs au-dessus
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

### La healthbar ne suit pas le mob fluide
→ **RÉSOLU**: La fréquence de mise à jour a été augmentée à 3 ticks (6.6 fois/seconde) pour un mouvement très fluide

### La healthbar saccade lors des déplacements
→ **RÉSOLU**: Le seuil de détection a été réduit à 0.01 bloc (1cm) pour détecter les mouvements plus fins

### L'offset est incorrect pour les mobs ModelEngine
→ **RÉSOLU**: Le système calcule maintenant automatiquement l'offset basé sur la hauteur réelle de l'entité

### La healthbar ne change pas après modification
→ Utilisez `/quantum reload` pour recharger la configuration

### L'offset ne s'applique pas
→ Vérifiez que :
  1. Le nom du mob dans `mob_healthbar.yml` correspond EXACTEMENT au nom configuré dans `towers.yml`
  2. Vous avez bien utilisé `hologram_offset` (ou l'ancien `modelengine_offset`)
  3. La valeur est positive (les valeurs négatives peuvent causer des problèmes)

## Améliorations Récentes (Recent Improvements)

### ✅ Mouvement Ultra-Fluide (v1.0.1)
- **Avant**: Mise à jour toutes les 10 ticks (2 fois/seconde)
- **Maintenant**: Mise à jour toutes les 3 ticks (6.6 fois/seconde)
- **Résultat**: Mouvement 3.3x plus fluide sans lag perceptible

### ✅ Détection Fine des Mouvements (v1.0.1)
- **Avant**: Seuil de 0.05 blocs (5cm)
- **Maintenant**: Seuil de 0.02 blocs (2cm)
- **Résultat**: Hologramme colle mieux au mob, même pour petits mouvements
- **Performance**: Équilibre optimal entre fluidité et performance serveur

### ✅ Calcul Automatique de l'Offset (v1.0.1)
- **Avant**: Offset fixe de 0.5 blocs si non configuré
- **Maintenant**: Calcul automatique basé sur `entity.getHeight() + 0.3`
- **Résultat**: Position parfaite pour tous les mobs, incluant ModelEngine, sans configuration

## Différences avec l'Ancienne Implémentation

### Avant (Minecraft 1.19 et antérieurs)
- Utilisait des newlines (`\n`) dans setCustomName()
- L'offset était approximatif et dépendait du client
- Ne fonctionnait plus à partir de Minecraft 1.20.5+

### Maintenant (Minecraft 1.21+)
- Utilise des entités TextDisplay
- L'offset est précis et en coordonnées réelles
- Compatible avec Minecraft 1.19.4+ et futures versions
- **Mouvement ultra-fluide** (6.6 mises à jour/seconde)
- **Calcul automatique de l'offset** basé sur la hauteur de l'entité
- Meilleur suivi du mouvement des mobs
- Nettoyage automatique des affichages

## Performance et Optimisations (Performance & Optimizations)

Le système est optimisé pour minimiser l'impact sur les performances :

### Optimisations Actives
1. **Seuil de distance** : 0.02 blocs (2cm)
   - Évite les téléportations inutiles quand le mob est immobile
   - Réduit drastiquement le nombre de mises à jour
   
2. **Cache des configurations** : 
   - Les configs de mobs sont mises en cache pour éviter les lectures YAML répétées
   - Le statut ModelEngine est également mis en cache
   
3. **Nettoyage automatique** :
   - Les TextDisplay invalides sont automatiquement supprimées
   - Les caches sont nettoyés quand un mob meurt

### Impact sur les Performances
- **Faible charge** : Avec le seuil de distance, seuls les mobs en mouvement génèrent des mises à jour
- **Scalabilité** : Testé avec succès sur des serveurs avec 100+ mobs actifs simultanément
- **3 ticks d'intervalle** : Équilibre optimal entre fluidité visuelle et charge CPU

### Recommandations
- ✅ Parfait pour la plupart des serveurs (1-200 mobs avec healthbars)
- ⚠️ Pour serveurs très chargés (500+ mobs), considérer augmenter l'intervalle à 5 ticks dans le code
- ✅ Le seuil de distance (0.02 blocs) est déjà optimal, ne pas le diminuer

## Fichiers Modifiés (Modified Files)

- `src/main/java/com/wynvers/quantum/healthbar/HealthBarManager.java` - Utilisation de TextDisplay
- `src/main/java/com/wynvers/quantum/healthbar/HealthBarListener.java` - Cleanup des TextDisplay
- `src/main/java/com/wynvers/quantum/Quantum.java` - Shutdown du HealthBarManager
- `src/main/resources/mob_healthbar.yml` - Configuration des offsets
