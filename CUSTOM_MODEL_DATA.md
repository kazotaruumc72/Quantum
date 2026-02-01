# Custom Model Data Reference

## Menu des Catégories d'Ordres

Voici les custom model data utilisés pour les icônes du menu principal `/rechercher`.

### Plage 2000-2099 : Catégories du Menu

| CMD  | Material            | Catégorie | Description                     |
|------|---------------------|-----------|----------------------------------|
| 2001 | WHEAT               | Cultures  | 🌾 Cultures et graines          |
| 2002 | DIAMOND             | Loots     | 💎 Objets de loots et drops      |
| 2003 | GOLDEN_APPLE        | Items     | 📦 Items spéciaux                |
| 2004 | POTION              | Potions   | 🧪 Potions et élixirs            |
| 2005 | DIAMOND_CHESTPLATE  | Armures   | 🛡️ Pièces d'armures             |
| 2006 | DIAMOND_PICKAXE     | Outils    | ⚒️ Outils et équipements         |

---

## Plage 1000-1999 : Items du Menu Potions

### Potions Positives (1000-1099)

| CMD  | Type              | Description                   |
|------|-------------------|-------------------------------|
| 1001 | Potion de Vitesse | Potion de vitesse améliorée  |
| 1002 | Potion de Force   | Potion de force concentrée   |
| 1003 | Potion de Régén  | Potion de régénération       |

### Potions Négatives (1100-1199)

| CMD  | Type              | Description                   |
|------|-------------------|-------------------------------|
| 1100 | Potion de Poison  | Potion empoisonnée            |
| 1101 | Potion de Faiblesse | Potion affaiblissante       |

### Élixirs Spéciaux (1200-1299)

| CMD  | Type              | Description                   |
|------|-------------------|-------------------------------|
| 1200 | Élixir Magique    | Élixir spécial rare            |
| 1201 | Élixir de Mana    | Restaure la mana              |

---

## Structure Resource Pack

### Exemple pour les catégories

**Fichier** : `assets/minecraft/models/item/wheat.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/wheat"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 2001
      },
      "model": "item/quantum/categories/cultures"
    }
  ]
}
```

**Fichier** : `assets/minecraft/models/item/quantum/categories/cultures.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/quantum/categories/cultures"
  }
}
```

**Texture** : `assets/minecraft/textures/item/quantum/categories/cultures.png`

---

### Exemple pour les potions

**Fichier** : `assets/minecraft/models/item/potion.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/potion_overlay",
    "layer1": "item/potion"
  },
  "overrides": [
    {
      "predicate": {"custom_model_data": 1001},
      "model": "item/quantum/potions/speed"
    },
    {
      "predicate": {"custom_model_data": 1002},
      "model": "item/quantum/potions/strength"
    },
    {
      "predicate": {"custom_model_data": 1003},
      "model": "item/quantum/potions/regen"
    },
    {
      "predicate": {"custom_model_data": 2004},
      "model": "item/quantum/categories/potions"
    }
  ]
}
```

---

## Bonnes Pratiques

### Organisation des CMD

- **1000-1999** : Items des sous-menus (potions, armures, etc.)
- **2000-2999** : Icônes de catégories et menus principaux
- **3000-3999** : Items customs du serveur
- **4000-4999** : Cosmetics et décorations

### Nommage des fichiers

```
assets/minecraft/
├── models/item/quantum/
│   ├── categories/      # Icônes des catégories
│   │   ├── cultures.json
│   │   ├── loots.json
│   │   └── ...
│   ├── potions/        # Items du menu potions
│   │   ├── speed.json
│   │   ├── strength.json
│   │   └── ...
│   └── items/          # Autres items customs
└── textures/item/quantum/
    ├── categories/
    │   ├── cultures.png
    │   ├── loots.png
    │   └── ...
    └── potions/
        ├── speed.png
        ├── strength.png
        └── ...
```

---

## Commandes de Test

Pour tester tes custom models en jeu :

```bash
# Donner un item avec custom model data
/give @s wheat{CustomModelData:2001}
/give @s diamond{CustomModelData:2002}
/give @s golden_apple{CustomModelData:2003}
/give @s potion{CustomModelData:2004}
/give @s diamond_chestplate{CustomModelData:2005}
/give @s diamond_pickaxe{CustomModelData:2006}

# Tester les potions du sous-menu
/give @s potion{CustomModelData:1001}
/give @s potion{CustomModelData:1002}
/give @s potion{CustomModelData:1003}
```

---

## Notes

- Les custom model data ne modifient que l'apparence visuelle
- Ils sont compatibles avec tous les plugins
- Nécessite un resource pack côté client
- Alternative : Utiliser Nexo pour des items vraiment customs
