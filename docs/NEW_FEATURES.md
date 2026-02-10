# Quantum - Nouvelles Fonctionnalités

## 🪑 Système de Furniture Nexo

Le système de furniture permet aux meubles Nexo de réapparaître automatiquement après avoir été cassés, avec la possibilité de faire apparaître d'autres meubles alternatifs et de donner des drops.

### Configuration (`furniture.yml`)

```yaml
furniture:
  fruit_tree:
    nexo_id: "fruit_tree"
    respawn_time: 300  # 5 minutes en secondes
    alternative_furniture:
      - furniture_id: "fruit_tree_bare"
        chance: 30.0  # 30% de chance d'apparaître à la place
    drops:
      - nexo_id: "apple_golden"
        min_amount: 1
        max_amount: 3
        chance: 10.0  # 10% de chance
      - minecraft: "APPLE"
        min_amount: 1
        max_amount: 2
        chance: 90.0
```

### Fonctionnalités

- ✅ Réapparition automatique des meubles après un délai configurable
- ✅ Système de meubles alternatifs avec pourcentage de chance
- ✅ Drops configurables (items Nexo ou Minecraft)
- ✅ Pourcentage de chance pour chaque drop
- ✅ Quantités min/max pour chaque drop

---

## 🌾 Système de Cultures Personnalisées

Un système de cultures qui ne nécessite pas le plugin CustomCrops, uniquement Nexo. Les cultures poussent avec des animations (différents modèles) et peuvent être récoltées avec n'importe quelle houe.

### Configuration (`custom_crops.yml`)

```yaml
crops:
  magic_wheat:
    display_name: "&eBlé Magique"
    nexo_id_base: "magic_wheat_seed"
    growth_stages:
      - stage: 1
        nexo_id: "magic_wheat_stage1"
        duration: 120  # 2 minutes
      - stage: 2
        nexo_id: "magic_wheat_stage2"
        duration: 180  # 3 minutes
      # ... jusqu'au stade mature
    harvest_commands:
      - "give {player} diamond 1"
      - "playsound minecraft:entity.player.levelup player {player}"
    drops:
      - nexo_id: "magic_wheat"
        min_amount: 2
        max_amount: 4
        chance: 100.0
```

### Fonctionnalités

- ✅ Croissance automatique avec animations (changement de modèle)
- ✅ Récolte avec n'importe quel type de houe (bois, pierre, fer, diamant, netherite)
- ✅ Exécution de commandes lors de la récolte
- ✅ Système de drops avec pourcentages
- ✅ Stades de croissance configurables

---

## 🔨 Outils Améliorables (Pioche, Hache, Houe)

Trois outils améliorables avec des compétences spéciales, montant jusqu'au niveau 10.

### Configuration (`tools.yml`)

#### Pioche - Double Extraction
- **Niveau 1-3**: Multiplicateur x2 sur les drops de furniture
- **Niveau 4-6**: Multiplicateur x3
- **Niveau 7-10**: Multiplicateur x4

#### Hache - One-shot
- **Fonction**: Coupe une structure entière d'un coup (whole → stump)
- **Taux d'activation**:
  - Niveau 1-3: 1/500 (0.2%)
  - Niveau 4-6: 1/400 (0.25%)
  - Niveau 7-10: 1/300 (0.33%)
- **Coût**: 5000$ par activation

#### Houe - Rare Loot
- **Fonction**: Donne des loots rares lors de la récolte
- **Chances**:
  - Niveau 1-3: 5% de chance, 1 drop max
  - Niveau 4-6: 7.5% de chance, 2 drops max
  - Niveau 7-10: 10% de chance, 3 drops max

### Structures (`structures.yml`)

Les structures définissent des arbres ou constructions qui peuvent être coupés avec la compétence One-shot de la hache.

```yaml
structures:
  oak_tree_large:
    display_name: "&2Grand Chêne"
    whole:  # État complet
      blocks:
        - "0,0,0:minecraft:OAK_LOG"
        - "0,1,0:minecraft:OAK_LOG"
        # ...
    good:  # Bon état
      blocks: # ...
    damaged:  # Abîmé
      blocks: # ...
    stump:  # Souche
      blocks:
        - "0,0,0:minecraft:OAK_LOG"
```

### Commandes

```bash
/tool upgrade               # Améliorer l'outil en main
/tool info                  # Voir les infos de l'outil
/tool give <type> <niveau>  # Obtenir un outil (admin)
```

### Permissions

- `quantum.tool.use` - Utiliser les outils
- `quantum.tool.upgrade` - Améliorer les outils
- `quantum.tool.give` - Donner des outils (admin)

---

## ⚔️ Arme de Donjon

Une arme améliorable qui ne peut être utilisée **que dans les donjons** (zones WorldGuard configurées).

### Configuration (`dungeon_weapon.yml`)

```yaml
weapon:
  max_level: 10
  nexo_ids:
    1: "dungeon_sword_level1"
    2: "dungeon_sword_level2"
    # ... jusqu'au niveau 10
  
  attributes:
    level_1_to_3:
      attack_damage: 8.0
      attack_speed: 1.6
    level_4_to_6:
      attack_damage: 12.0
      attack_speed: 1.8
    level_7_to_10:
      attack_damage: 16.0
      attack_speed: 2.0

dungeon_regions:
  - "dungeon_1"
  - "dungeon_2"
  - "tower_1"
  # ... liste des régions WorldGuard considérées comme donjons
```

### Fonctionnalités

- ✅ Utilisable uniquement dans les zones de donjon configurées
- ✅ Message de titre affiché si utilisée hors donjon
- ✅ Annulation automatique des dégâts hors donjon
- ✅ Système d'amélioration jusqu'au niveau 10
- ✅ Enchantements qui évoluent avec le niveau

### Commandes

```bash
/weapon upgrade  # Améliorer l'arme en main
/weapon info     # Voir les infos de l'arme
/weapon give     # Obtenir une arme de donjon (admin)
```

### Permissions

- `quantum.weapon.use` - Utiliser l'arme de donjon
- `quantum.weapon.upgrade` - Améliorer l'arme
- `quantum.weapon.give` - Donner l'arme (admin)

---

## 📋 Récapitulatif des Fichiers de Configuration

| Fichier | Description |
|---------|-------------|
| `furniture.yml` | Configuration des meubles réapparaissants |
| `custom_crops.yml` | Configuration des cultures personnalisées |
| `tools.yml` | Configuration des outils améliorables |
| `structures.yml` | Définition des structures pour la hache |
| `dungeon_weapon.yml` | Configuration de l'arme de donjon |

---

## 🎮 Guide d'Utilisation

### Pour les Joueurs

1. **Furniture**: Cassez les meubles Nexo pour obtenir des drops. Ils réapparaîtront automatiquement.

2. **Cultures**: 
   - Plantez les graines de culture
   - Attendez qu'elles poussent (changement de modèle automatique)
   - Récoltez avec une houe quand elles sont matures

3. **Outils**:
   - Obtenez des outils Quantum auprès des admins
   - Améliorez-les avec `/tool upgrade` (coût en argent)
   - Utilisez leurs compétences spéciales

4. **Arme de Donjon**:
   - Obtenez l'arme auprès des admins
   - Utilisez-la **uniquement** dans les donjons
   - Améliorez-la pour plus de puissance

### Pour les Admins

1. Configurez les fichiers YAML selon vos besoins
2. Créez les items Nexo correspondants dans votre pack Nexo
3. Donnez les outils/armes aux joueurs avec les commandes
4. Configurez les régions WorldGuard pour les donjons

---

## 🔧 Dépendances

- **Nexo** (requis) - Pour les items et meubles personnalisés
- **WorldGuard** (optionnel) - Pour les zones de donjon
- **Vault** (optionnel) - Pour le système d'économie (améliorations)

---

## ⚠️ Notes Importantes

1. Les items Nexo doivent être créés dans votre pack Nexo avec les IDs correspondants
2. Les structures sont basées sur les coordonnées relatives au bloc de base
3. Le système de crops vérifie toutes les 30 secondes pour la croissance
4. Les meubles alternatifs sont choisis aléatoirement selon les pourcentages
5. L'arme de donjon vérifie la région WorldGuard à chaque attaque

---

## 📝 Exemple de Configuration Complète

Voir les fichiers de configuration fournis dans `/src/main/resources/` pour des exemples complets et fonctionnels.
