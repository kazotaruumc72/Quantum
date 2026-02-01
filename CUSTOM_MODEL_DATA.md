# Custom Model Data - Guide d'Utilisation

## Vue d'ensemble

Quantum supporte les **custom model data** pour afficher des textures personnalisées dans les menus.
Cela permet d'utiliser des resource packs pour créer des interfaces visuelles uniques.

## Configuration dans les menus

### Syntaxe de base

```yaml
items:
  mon_item:
    slots: [10]
    material: PAPER  # Material de base
    amount: 1        # IMPORTANT: Toujours mettre 1 pour custom model data
    custom_model_data: 1001  # ID du modèle personnalisé
    display_name: '&eMon Item Custom'
    lore:
      - '&7Ceci est un item avec texture custom'
```

### Règles importantes

1. **Amount = 1** : Toujours utiliser `amount: 1` pour les items avec custom model data
   - Les textures custom ne s'affichent correctement qu'avec 1 item
   - Ne jamais mettre `amount: 64` ou autre valeur

2. **Material de base** : Choisir un material compatible
   - `PAPER` : Le plus courant, très flexible
   - `STICK` : Alternative populaire
   - `CARROT_ON_A_STICK` : Utilisé pour les outils custom
   - Tout material vanilla peut être utilisé

3. **Custom Model Data ID** : Doit correspondre au resource pack
   - Coordonner les IDs avec ton resource pack
   - Documenter les IDs utilisés pour éviter les conflits

## Exemples d'utilisation

### Menu Storage

```yaml
# Mode de stockage avec icône custom
mode_toggle:
  slots: [4]
  material: PAPER
  amount: 1
  custom_model_data: 2001  # Icône "Storage Mode"
  display_name: '&e&lMode: %mode%'
  type: quantum_change_mode
```

### Menu Sell

```yaml
# Bouton vendre tout avec icône custom
sell_all:
  slots: [22]
  material: PAPER
  amount: 1
  custom_model_data: 3001  # Icône "Sell All"
  display_name: '&6&l★ Vendre tout'
  button_type: QUANTUM_CHANGE_AMOUNT
  amount: 999999

# Bouton confirmer avec icône custom
confirm_sell:
  slots: [23]
  material: PAPER
  amount: 1
  custom_model_data: 3002  # Icône "Confirm"
  display_name: '&a&l✓ Vendre'
  button_type: QUANTUM_SELL
```

### Boutons de contrôle

```yaml
# Boutons +/- avec icônes custom
remove_10:
  slots: [11]
  material: PAPER
  amount: 1
  custom_model_data: 4001  # Icône "-10"
  display_name: '&c&l-10'
  button_type: QUANTUM_CHANGE_AMOUNT
  amount: -10

add_10:
  slots: [15]
  material: PAPER
  amount: 1
  custom_model_data: 4002  # Icône "+10"
  display_name: '&a&l+10'
  button_type: QUANTUM_CHANGE_AMOUNT
  amount: 10
```

### Bordures décoratives

```yaml
border:
  material: PAPER
  amount: 1
  custom_model_data: 5000  # Texture de bordure custom
  display_name: ' '
  slots: [0, 1, 2, 3, 5, 6, 7, 8]
```

## Intégration avec Nexo

Si tu utilises des items Nexo, tu peux combiner avec custom model data :

```yaml
nexo_item_custom:
  slots: [10]
  nexo_item: mon_item_nexo  # Item Nexo
  custom_model_data: 6001   # Custom model data additionnel (optionnel)
  display_name: '&eItem Nexo Custom'
```

## Items spéciaux

### quantum_sell_item

⚠️ **Note importante** : Les items `quantum_sell_item` utilisent l'item réel du joueur.
- Le `custom_model_data` de la config est **ignoré**
- L'item affiché provient de `SellSession.getItemToSell()`
- Si l'item du joueur a un custom model data, il sera préservé

```yaml
item_to_sell:
  slots: [4]
  material: PAPER  # Placeholder (sera remplacé)
  amount: 1        # Placeholder (sera remplacé)
  type: quantum_sell_item
  display_name: '&e%item_name%'  # Sera appliqué à l'item réel
  lore:
    - '&7Quantité: %quantity%'
```

### quantum_storage

Les items du storage préservent automatiquement leur custom model data :

```yaml
storage_slots:
  slots: [9-44]
  type: quantum_storage
  lore_append:
    - ' '
    - '&7Clic gauche: Retirer 1'
    - '&7Shift + Clic gauche: Retirer tout'
```

## Checklist de migration

Pour convertir tes menus existants vers custom model data :

- [ ] Identifier tous les items à personnaliser
- [ ] Choisir les materials de base (PAPER recommandé)
- [ ] Définir les custom model data IDs
- [ ] Mettre `amount: 1` sur tous les items custom
- [ ] Créer/mettre à jour le resource pack
- [ ] Tester chaque menu individuellement
- [ ] Documenter les IDs utilisés

## Gestion des IDs

### Convention recommandée

- `1000-1999` : Icons généraux (menus, navigation)
- `2000-2999` : Storage system
- `3000-3999` : Sell system
- `4000-4999` : Contrôles (+/-/confirm/cancel)
- `5000-5999` : Décorations (bordures, backgrounds)
- `6000-6999` : Items custom spéciaux

### Exemple de mapping

```yaml
# IDs Storage (2000-2999)
2001: storage_icon
2002: storage_mode_sell
2003: storage_mode_storage

# IDs Sell (3000-3999)
3001: sell_all_icon
3002: confirm_sell_icon
3003: cancel_icon

# IDs Contrôles (4000-4999)
4001: minus_10
4002: plus_10
4003: minus_1
4004: plus_1

# IDs Bordures (5000-5999)
5000: border_default
5001: border_top
5002: border_bottom
```

## Compatibilité

✅ **Compatible avec** :
- Minecraft 1.14+
- Nexo items
- PlaceholderAPI
- Tous les button types Quantum

⚠️ **Limitations** :
- Nécessite un resource pack côté client
- Les joueurs sans resource pack verront le material de base
- `amount: 1` requis (pas de stacks visuels)

## Ressources

- [Minecraft Wiki - Custom Model Data](https://minecraft.wiki/w/Custom_model_data)
- [Resource Pack Tutorial](https://minecraft.wiki/w/Tutorials/Creating_a_resource_pack)
- [Nexo Documentation](https://docs.nexomc.com/)

## Support

Pour toute question ou problème avec les custom model data :
1. Vérifier que `amount: 1` est présent
2. Confirmer que l'ID existe dans le resource pack
3. Tester avec le material vanilla de base
4. Vérifier les logs pour erreurs de chargement
