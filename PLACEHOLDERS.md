# 🏷️ Placeholders Quantum - Documentation Complète

> Tous les placeholders Quantum sont disponibles via [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## 🔧 Installation

1. Installer **PlaceholderAPI** sur votre serveur
2. Redémarrer le serveur
3. Les placeholders Quantum seront automatiquement enregistrés
4. Utiliser `/papi parse <joueur> %quantum_<placeholder>%` pour tester

---

## 🎯 Tous les Placeholders Disponibles

### 📦 **Storage (Stockage)**

#### Quantités d'items spécifiques

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_amt_nexo-<id>%` | Quantité d'un item Nexo spécifique | `64` |
| `%quantum_amt_minecraft-<material>%` | Quantité d'un item Minecraft | `128` |

**Exemples:**
- `%quantum_amt_nexo-custom_sword%` → Quantité d'épée custom Nexo
- `%quantum_amt_minecraft-diamond%` → Quantité de diamants
- `%quantum_amt_minecraft-oak_log%` → Quantité de bûches de chêne

#### Statistiques de stockage

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_storage_items%` | Nombre d'items différents stockés | `47` |
| `%quantum_storage_total%` | Nombre total d'items (quantité) | `2580` |

---

### 🏮 **Mode de Stockage**

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_mode%` | Mode actuel (brut) | `STORAGE`, `SELL`, `RECHERCHE` |
| `%quantum_mode_display%` | Mode actuel (formaté) | `§aSTOCKAGE`, `§6VENTE`, `§bRECHERCHE` |

---

### 💼 **Système de Métiers (Jobs System)**

#### Informations du métier actuel

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_job_name%` | Nom du métier actuel | `Bûcheron`, `Aucun` |
| `%quantum_job_level%` | Niveau du métier | `15` |
| `%quantum_job_exp%` | Expérience actuelle | `450` |
| `%quantum_job_exp_needed%` | Expérience nécessaire pour le prochain niveau | `1000` |
| `%quantum_job_exp_progress%` | Progression de l'expérience | `450/1000` |
| `%quantum_job_rank%` | Classement du joueur dans son métier | `3`, `N/A` |

#### Boosters actifs

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_job_booster_exp%` | Multiplicateur d'XP actif | `1.5`, `1.0` |
| `%quantum_job_booster_money%` | Multiplicateur d'argent actif | `2.0`, `1.0` |
| `%quantum_job_boosters_active%` | Nombre de boosters actifs | `2`, `0` |

**Notes sur les boosters:**
- Les boosters "dungeon_only" sont pris en compte uniquement si le joueur est dans un donjon
- Si aucun booster n'est actif, les multiplicateurs retournent `1.0`
- Les multiplicateurs sont formatés avec 1 décimale

#### Classements (Leaderboards)

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_job_top_<job>_<position>%` | Nom du joueur à la position N | `Kazotaruu`, `N/A` |
| `%quantum_job_top_<job>_<position>_level%` | Niveau du joueur à la position N | `50`, `0` |

**Exemples:**
- `%quantum_job_top_lumberjack_1%` → Nom du #1 en bûcheron
- `%quantum_job_top_miner_3%` → Nom du #3 en mineur
- `%quantum_job_top_lumberjack_1_level%` → Niveau du #1 en bûcheron
- `%quantum_job_top_miner_5_level%` → Niveau du #5 en mineur

**Notes:**
- Les classements sont calculés en temps réel depuis la base de données
- Le tri est fait par niveau décroissant, puis par XP décroissante
- Si la position demandée n'existe pas, retourne `N/A` pour le nom et `0` pour le niveau

---

### 🏯 **Système de Tours (Tower System)**

#### Progression globale

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_towers_completed%` | Nombre de tours terminées | `2` ou `Aucune tour configurée` |
| `%quantum_towers_total%` | Nombre total de tours | `4` |
| `%quantum_towers_percentage%` | Pourcentage global de complétion | `50.0` |
| `%quantum_total_floors_completed%` | Étages totaux terminés | `45/100` |

#### Tour actuelle (où se trouve le joueur)

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_tower_current%` | Nom de la tour actuelle | `Tour du Feu` |
| `%quantum_tower_floor%` | Numéro d'étage actuel | `5` |
| `%quantum_tower_progress%` | Progression dans la tour | `5/25` |
| `%quantum_tower_percentage%` | Pourcentage de progression | `20%` |
| `%quantum_tower_status%` | Statut actuel | `§aEn cours`, `§e§lBOSS D'ÉTAGE`, `§c§lBOSS FINAL` |
| `%quantum_tower_next_boss%` | Prochain boss | `Étage 10` ou `Aucun` |

#### Kills (monstres tués)

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_tower_kills_current%` | Kills sur l'étage actuel | `8` |
| `%quantum_tower_kills_required%` | Kills requis pour l'étage | `10` |
| `%quantum_tower_kills_progress%` | Progression des kills | `8/10` |

#### Tours spécifiques (par ID)

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_tower_<id>_progress%` | Progression d'une tour | `15/25` |
| `%quantum_tower_<id>_percentage%` | Pourcentage d'une tour | `60.0%` |
| `%quantum_tower_<id>_completed%` | Tour terminée ? | `true` ou `false` |

**Exemples:**
- `%quantum_tower_fire_progress%` → `15/25`
- `%quantum_tower_ice_percentage%` → `40.5%`
- `%quantum_tower_nature_completed%` → `false`
- `%quantum_tower_shadow_progress%` → `25/25`

---

### 🗡️ **Tracking de Kills (Zone)**

| Placeholder | Description | Exemple de résultat |
|------------|-------------|----------------------|
| `%quantum_killed_<mob>_<amount>%` | Vérifie si quota atteint | `true` ou `false` |

**Exemples:**
- `%quantum_killed_zombie_10%` → `true` si 10+ zombies tués
- `%quantum_killed_skeleton_5%` → `false` si moins de 5
- `%quantum_killed_wither_skeleton_20%` → Support des IDs avec underscore

---

### 📝 **Système d'Ordres (Orders)**

> Placeholders dynamiques pendant la création d'ordre

| Placeholder | Description | Valeur dynamique |
|------------|-------------|------------------|
| `%quantum_order_item_name%` | Nom de l'item | Variable |
| `%quantum_order_quantity%` | Quantité | Variable |
| `%quantum_order_price%` | Prix unitaire | Variable |
| `%quantum_order_total%` | Prix total | Variable |
| `%quantum_order_type%` | Type d'ordre | `ACHAT` ou `VENTE` |

---

## ⚙️ Utilisation dans les Configurations

### Dans `scoreboard.yml`
```yaml
lines:
  - "&6Tours Complétées:"
  - "  &f%quantum_towers_completed%/%quantum_towers_total%"
  - "  &7(%quantum_towers_percentage%%)"
```

### Dans les Menus (DeluxeMenus style)
```yaml
items:
  tower_progress:
    material: BEACON
    name: "&6Progression Tours"
    lore:
      - "&7Terminées: &f%quantum_towers_completed%/%quantum_towers_total%"
      - "&7Progression: &a%quantum_towers_percentage%%"
      - ""
      - "&e▶ Tour actuelle: &f%quantum_tower_current%"
      - "&7Étage: &f%quantum_tower_floor%"
```

### Avec d'autres plugins (TAB, FeatherBoard, etc.)
```yaml
# TAB Plugin
scoreboard:
  lines:
    - "&6&lSERVEUR"
    - ""
    - "&7Tours: &f%quantum_towers_completed%/%quantum_towers_total%"

# FeatherBoard
board:
  title: "&6&lQUANTUM"
  lines:
    - "&eTours: %quantum_towers_completed%/%quantum_towers_total%"
```

---

## 🔍 Tests des Placeholders

### Commande de test
```
/papi parse <joueur> %quantum_<placeholder>%
```

### Exemples de tests
```bash
/papi parse Kazotaruu %quantum_towers_completed%
/papi parse Kazotaruu %quantum_towers_percentage%
/papi parse Kazotaruu %quantum_tower_current%
/papi parse Kazotaruu %quantum_storage_items%
/papi parse Kazotaruu %quantum_amt_minecraft-diamond%
```

---

## 🚨 Notes Importantes

### Tours non configurées
Si aucune tour n'est configurée (pas de WorldGuard ou tours.yml vide) :
- `%quantum_towers_completed%` → `"Aucune tour configurée"`
- `%quantum_towers_total%` → `"0"`
- `%quantum_towers_percentage%` → `"0.0"`

### Format des pourcentages
- Les pourcentages sont formatés avec **1 décimale** : `50.5`, `33.3`, `100.0`
- Pas de symbole `%` inclus dans `%quantum_towers_percentage%` pour flexibilité
- Ajouter manuellement `%` dans votre config : `%quantum_towers_percentage%%`

### IDs des tours
Les IDs de tours doivent correspondre à ceux définis dans `zones.yml` :
- `fire` → Tour du Feu
- `ice` → Tour de Glace  
- `nature` → Tour de Nature
- `shadow` → Tour d'Ombre

---

## 🐛 Support & Issues

Si un placeholder ne fonctionne pas :
1. Vérifier que PlaceholderAPI est installé
2. Redémarrer le serveur après installation
3. Tester avec `/papi parse`
4. Vérifier la console pour les erreurs
5. Ouvrir une issue sur GitHub avec les logs

---

## 📚 Documentation Supplémentaire

- [PlaceholderAPI Wiki](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki)
- [Liste des placeholders externes](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders)
- [Guide d'intégration](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Hook-into-PlaceholderAPI)

---

**Version:** 2.0.0  
**Dernière mise à jour:** Février 2026  
**Auteur:** Wynvers / Kazotaruu_
