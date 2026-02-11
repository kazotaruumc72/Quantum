# Jobs System Documentation

## Vue d'ensemble

Le système de métiers (Jobs) ajoute une progression de compétences complète au plugin Quantum. Les joueurs peuvent choisir un métier, gagner de l'expérience en interagissant avec des structures, monter en niveau et débloquer des récompenses.

**Améliorations récentes (inspirées de UniverseJobs):**
- ✨ **Système de preview des actions** - Aperçu des récompenses avant d'interagir avec une structure
- ✨ **Affichage amélioré des récompenses** - Preview détaillée avec icônes et formatage moderne
- ✨ **Indicateurs visuels** - Barres de progression et multiplicateurs de boosters

## Fonctionnalités principales

### 1. **Système de métiers**
- Chaque métier a un ID unique, un nom d'affichage et une description
- Niveau maximum configurable (par défaut: 100)
- Courbe d'XP identique au système de donjon (formule: `100 * 1.1^(level-1)`)
- Structures valides spécifiques à chaque métier

### 2. **Interaction avec les structures**
- **Clic droit** sur une structure: Affiche une preview des récompenses potentielles
- **Clic gauche** sur une structure: Exécute l'action et récolte les récompenses
- Chaque tap dégrade la structure d'un état:
  - WHOLE (entier) → GOOD (bon état) → DAMAGED (abîmé) → STUMP (souche)
- Récompenses différentes selon l'état de la structure:
  - WHOLE: 10 XP + 5$
  - GOOD: 7 XP + 3$
  - DAMAGED: 5 XP + 2$
  - STUMP: 2 XP + 1$

### 2.1 **Preview des actions (NOUVEAU)**
Lorsque le joueur effectue un clic droit sur une structure:
- Affichage dans l'action bar avec icônes colorés
- Indication de l'état de la structure
- XP et argent potentiels à gagner
- Indicateurs de boosters actifs (✦) si applicable
- Vérification de la validité de la structure pour le métier

Exemple d'affichage:
```
█ ⛏ Bûcheron » +15 XP ✦ │ +10.0$ ✦
```

### 3. **Système de récompenses**
Les récompenses sont automatiquement distribuées lorsque le joueur atteint un certain niveau. Types de récompenses supportés:

#### a) Commandes
- **console_command**: Exécute une commande console
  ```yaml
  type: "console_command"
  value: "give {player} minecraft:diamond 1"
  ```
- **player_command**: Exécute une commande en tant que joueur
  ```yaml
  type: "player_command"
  value: "say J'ai atteint le niveau 10!"
  ```

#### b) Argent
- **money**: Donne de l'argent via Vault
  ```yaml
  type: "money"
  value: "100"
  ```

#### c) Items
- **nexo_item**: Donne un item Nexo
  ```yaml
  type: "nexo_item"
  value: "magic_log"
  amount: 5
  ```
- **mythicmobs_item**: Donne un item MythicMobs
  ```yaml
  type: "mythicmobs_item"
  value: "EnchantedAxe"
  amount: 1
  ```

#### d) Boosters
- **exp_booster**: Multiplie l'XP gagnée
  ```yaml
  type: "exp_booster"
  value: "1.5"           # Multiplicateur (1.5x)
  duration: 3600         # Durée en secondes (1 heure)
  dungeon_only: true     # Actif uniquement en donjon
  ```
- **money_booster**: Multiplie l'argent gagné
  ```yaml
  type: "money_booster"
  value: "2.0"
  duration: 7200
  dungeon_only: false
  ```

### 4. **Boosters de donjon**
Les boosters peuvent être configurés pour fonctionner uniquement dans les donjons. Le système détecte automatiquement si le joueur est dans un donjon (via TowerManager) et applique les multiplicateurs en conséquence.

## Configuration

### jobs.yml

```yaml
jobs:
  lumberjack:
    id: "lumberjack"
    display_name: "&2⛏ Bûcheron"
    description:
      - "&7Coupez des arbres pour gagner"
      - "&7de l'argent et de l'expérience."
    icon: "minecraft:IRON_AXE"
    max_level: 100
    
    valid_structures:
      - "oak_tree_large"
      - "spruce_tree_snow"
      - "magic_tree"
    
    level_rewards:
      5:
        - type: "money"
          value: "100"
      10:
        - type: "nexo_item"
          value: "magic_log"
          amount: 5
      15:
        - type: "exp_booster"
          value: "1.5"
          duration: 3600
          dungeon_only: true
```

### Récompenses d'actions

```yaml
action_rewards:
  structure_tap:
    whole:
      exp: 10
      money: 5
    good:
      exp: 7
      money: 3
    damaged:
      exp: 5
      money: 2
    stump:
      exp: 2
      money: 1
```

## Commandes

### Commandes joueur

#### `/job` - Afficher vos informations de métier
Affiche votre métier actuel, niveau, XP et progression.

#### `/job select <métier>` - Choisir un métier
Sélectionne un métier. Vous ne pouvez avoir qu'un seul métier actif à la fois.

Exemple: `/job select lumberjack`

#### `/job list` - Lister tous les métiers
Affiche tous les métiers disponibles avec leurs descriptions.

#### `/job info [métier]` - Informations sur un métier
Affiche les détails d'un métier spécifique (structures valides, niveau max, etc.)

Exemple: `/job info miner`

#### `/job rewards` - Voir les prochaines récompenses
Affiche les récompenses des 10 prochains niveaux avec un formatage amélioré.

**Nouvelle fonctionnalité:**
- Affichage avec icônes colorés (💰, 📦, ⚔, ✦)
- Indication "Donjon" pour les boosters dungeon_only
- Présentation organisée par niveau

#### `/job rewards preview [niveaux]` - Preview détaillé des récompenses (NOUVEAU)
Affiche un aperçu détaillé et formaté des prochaines récompenses.
- **niveaux**: Nombre de niveaux à afficher (1-10, défaut: 3)

Affiche:
- Barre de progression visuelle
- XP totale nécessaire pour chaque niveau
- Récompenses avec descriptions détaillées et icônes
- Indicateurs pour les boosters dungeon_only

Exemple: 
```
/job rewards preview
/job rewards preview 5
```

### Commandes admin

#### `/jobadmin set <joueur> <métier>` - Définir le métier d'un joueur
Force un métier pour un joueur.

Exemple: `/jobadmin set Notch lumberjack`

#### `/jobadmin addexp <joueur> <quantité>` - Ajouter de l'XP
Ajoute de l'XP au métier d'un joueur.

Exemple: `/jobadmin addexp Notch 100`

#### `/jobadmin setlevel <joueur> <niveau>` - Définir le niveau
Change le niveau du métier d'un joueur.

Exemple: `/jobadmin setlevel Notch 50`

#### `/jobadmin reset <joueur>` - Réinitialiser le métier
Supprime le métier actif d'un joueur.

#### `/jobadmin info <joueur>` - Voir les infos d'un joueur
Affiche les informations de métier d'un joueur.

#### `/jobadmin reload` - Recharger la configuration
Recharge le fichier jobs.yml sans redémarrer le serveur.

## Permissions

### Joueur
- `quantum.job.use` - Utiliser le système de métiers (défaut: true)

### Admin
- `quantum.job.admin` - Accès aux commandes admin (défaut: op)
- `quantum.job.*` - Toutes les permissions de métiers

## Intégration avec les systèmes existants

### StructureManager
Le système de métiers s'intègre avec le StructureManager pour:
- Détecter les structures lors d'un clic
- Dégrader l'état des structures
- Appliquer les récompenses appropriées

### TowerManager / Donjons
- Détection automatique si le joueur est dans un donjon
- Application des boosters "dungeon_only"
- Les boosters d'XP/argent de donjon sont indiqués dans le lore

### Vault (Économie)
- Distribution d'argent via les récompenses
- Multiplicateurs de money_booster

### Database
Les données de métiers sont stockées dans la table `quantum_player_jobs`:
```sql
CREATE TABLE quantum_player_jobs (
    uuid VARCHAR(36) PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0
)
```

## Exemples de métiers

### Bûcheron (Lumberjack)
- Structures: Arbres (oak_tree_large, spruce_tree_snow, magic_tree)
- Récompenses: Items Nexo, boosters d'XP, argent

### Mineur (Miner)
- Structures: Dépôts de minerais (stone_deposit, iron_deposit, gold_deposit)
- Récompenses: Items compressés, boosters, ingots

## Placement des récompenses en serpent

Les récompenses sont organisées automatiquement lors du chargement de la configuration. Le système lit les `level_rewards` dans l'ordre et les stocke pour distribution automatique lorsque le joueur atteint le niveau correspondant.

## Messages personnalisables

Tous les messages sont configurables dans `jobs.yml`:

```yaml
messages:
  job_selected: "&a✓ Vous avez sélectionné le métier: {job_name}"
  job_level_up: "&6✦ Niveau supérieur! &7Vous êtes maintenant {job_name} &fniveau {level}&7!"
  job_reward_received: "&a✓ Récompense débloquée: {reward}"
  structure_tapped: "&7+{exp} XP {job_name} &7| +{money}$"
  booster_activated: "&a✓ Booster activé: {booster_name} &7(x{multiplier})"
  booster_expired: "&c✗ Votre booster {booster_name} a expiré!"
```

## Système de Preview des Actions (NOUVEAU)

Le système de preview, inspiré de UniverseJobs, permet aux joueurs de voir exactement ce qu'ils vont gagner avant d'effectuer une action.

### Fonctionnalités

#### Preview instantanée (Action Bar)
- **Activation**: Clic droit sur une structure
- **Affichage**: Dans l'action bar du joueur
- **Informations affichées**:
  - Icône de l'état de la structure (█ ▓ ▒ ░)
  - Nom du métier avec couleur
  - XP et argent potentiels
  - Indicateurs de boosters actifs (✦)
  - Validation de la structure pour le métier

#### Preview détaillée (Chat)
- **Activation**: `/job rewards preview [niveaux]`
- **Affichage**: Dans le chat avec formatage avancé
- **Informations affichées**:
  - Niveau et progression actuels
  - Barre de progression visuelle
  - Prochaines récompenses par niveau
  - XP totale nécessaire pour chaque récompense
  - Descriptions détaillées avec icônes

### Exemples visuels

#### Preview d'action (Action Bar):
```
█ ⛏ Bûcheron » +10 XP │ +5.0$
▓ ⛏ Bûcheron » +7 XP │ +3.0$
▒ ⛏ Bûcheron » +15 XP ✦ │ +10.0$ ✦  (avec boosters)
⚠ Structure invalide pour votre métier
```

#### Preview détaillée (Chat):
```
╔═══════════════════════════════════════╗
║  Aperçu des Récompenses              ║
╚═══════════════════════════════════════╝

Métier: ⛏ Bûcheron
Niveau: 5/100
XP: 85/110
[███████████████████████░░░░░░░░░░░] 77.3%

▸ Prochaines récompenses:

  ◆ Niveau 10 (135 XP restants)
    • 💰 100$ d'argent
    • 📦 magic_log x5 (Item Nexo)

  ◆ Niveau 15 (589 XP restants)
    • ✦ Booster XP x1.5 - 1h (Donjon uniquement)
```

### Indicateurs visuels

- **États de structure**: 
  - █ WHOLE (entier)
  - ▓ GOOD (bon état)
  - ▒ DAMAGED (abîmé)
  - ░ STUMP (souche)

- **Types de récompenses**:
  - 💰 Argent
  - 📦 Items Nexo
  - ⚔ Items MythicMobs
  - ✦ Boosters (XP/Argent)
  - ⚙ Commandes spéciales

- **Multiplicateurs actifs**:
  - ✦ Indique qu'un booster est actif

## Notes techniques

### Courbe d'XP
La formule utilisée est la même que le système de donjon:
```java
requiredExp = (int) Math.floor(100 * Math.pow(1.1, level - 1))
```

### Gestion des boosters
- Les boosters sont vérifiés toutes les 60 secondes
- Expiration automatique avec notification au joueur
- Support de plusieurs boosters actifs simultanément
- Multiplicateurs cumulatifs

### Performance
- Chargement des données en async lors du join
- Sauvegarde en async lors du quit
- Cache en mémoire pour accès rapide
- Tâche périodique pour vérifier l'expiration des boosters

## Dépannage

### Le joueur ne gagne pas d'XP
1. Vérifier qu'il a un métier sélectionné (`/job`)
2. Vérifier que la structure est valide pour son métier
3. Vérifier les logs pour les erreurs de base de données

### Les récompenses ne sont pas données
1. Vérifier la syntaxe dans jobs.yml
2. Pour les items Nexo/MythicMobs, vérifier que les plugins sont chargés
3. Pour l'argent, vérifier que Vault est installé

### Les boosters ne fonctionnent pas
1. Vérifier si le booster est "dungeon_only" et si le joueur est bien dans un donjon
2. Vérifier l'expiration du booster
3. Utiliser `/jobadmin info <joueur>` pour voir les boosters actifs
