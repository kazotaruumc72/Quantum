# Guide MiniMessage pour Quantum

## Vue d'ensemble

Quantum utilise le format **MiniMessage** pour tous les messages texte. C'est un format moderne, lisible et puissant qui remplace les anciens codes couleur (`&c`, `&a`, etc.).

## Pourquoi MiniMessage ?

✅ **Plus lisible** : `<red>Erreur</red>` au lieu de `&cErreur`
✅ **Plus puissant** : Gradients, hover, click actions
✅ **Plus sécurisé** : Pas d'injection de codes couleur par les joueurs
✅ **Plus moderne** : Standard actuel de Minecraft

## Couleurs de base

### Couleurs simples

```yaml
<black>Noir</black>
<dark_blue>Bleu foncé</dark_blue>
<dark_green>Vert foncé</dark_green>
<dark_aqua>Cyan foncé</dark_aqua>
<dark_red>Rouge foncé</dark_red>
<dark_purple>Violet foncé</dark_purple>
<gold>Or</gold>
<gray>Gris</gray>
<dark_gray>Gris foncé</dark_gray>
<blue>Bleu</blue>
<green>Vert</green>
<aqua>Cyan</aqua>
<red>Rouge</red>
<light_purple>Violet clair</light_purple>
<yellow>Jaune</yellow>
<white>Blanc</white>
```

### Couleurs hexadécimales

```yaml
<color:#FF5555>Rouge custom</color>
<#FF5555>Raccourci hex</color>
<#00FF00>Vert fluo</color>
```

## Formatage

### Styles de texte

```yaml
<bold>Gras</bold>
<italic>Italique</italic>
<underlined>Souligné</underlined>
<strikethrough>Barré</strikethrough>
<obfuscated>Aléatoire</obfuscated>
```

### Combinaisons

```yaml
<red><bold>Rouge et gras</bold></red>
<green><italic><underlined>Vert italique souligné</underlined></italic></green>
```

### Réinitialisation

```yaml
<red>Rouge <reset>normal <red>rouge encore
```

## Gradients

### Gradient simple

```yaml
<gradient:red:blue>Texte dégradé</gradient>
<gradient:gold:yellow:green>Gradient multiple</gradient>
```

### Gradient hex

```yaml
<gradient:#FF5555:#5555FF>Dégradé personnalisé</gradient>
<gradient:#FFD700:#FFA500:#FF4500>Dégradé or-orange-rouge</gradient>
```

### Gradient avec phases

```yaml
<gradient:red:gold:green:0.2>Dégradé avec phase</gradient>
```

## Hover & Click

### Hover (Info-bulle)

```yaml
<hover:show_text:'<green>Info complémentaire'>Survole-moi</hover>
<hover:show_text:'<red>Attention!<newline><gray>Ceci est un avertissement'>Texte multiligne</hover>
```

### Click (Actions)

```yaml
# Exécuter une commande
<click:run_command:'/help'>Clique pour l'aide</click>

# Suggérer une commande (insère dans le chat)
<click:suggest_command:'/msg '>Envoyer un message</click>

# Ouvrir une URL
<click:open_url:'https://example.com'>Ouvrir le site</click>

# Copier dans le presse-papier
<click:copy_to_clipboard:'Code promo'>Copier le code</click>
```

### Combinaison Hover + Click

```yaml
<hover:show_text:'<green>Cliquez pour ouvrir'><click:run_command:'/storage'>Storage</click></hover>
```

## Insertions spéciales

### Nouvelle ligne

```yaml
<newline>  # Saut de ligne
Ligne 1<newline>Ligne 2
```

### Insertion de texte

```yaml
<insertion:'Texte à insérer'>Shift+clic pour insérer</insertion>
```

## Exemples pratiques

### Messages de succès

```yaml
<green><bold>✓</bold> Opération réussie!
<green><bold>✓</bold> <gray>Vous avez reçu <yellow>64x Diamants
```

### Messages d'erreur

```yaml
<red><bold>✗</bold> Erreur: Permission requise
<red>Vous n'avez pas assez d'argent! <gray>Coût: <gold>1000$
```

### Messages d'information

```yaml
<yellow><bold>⚠</bold> Attention: Cette action est irréversible
<blue><bold>i</bold> <gray>Astuce: Utilisez /help pour plus d'infos
```

### Titres de section

```yaml
<gold><bold>■</bold> <yellow>Section Titre <gray>(Info)
<gradient:gold:yellow>======================</gradient>
```

### Messages interactifs

```yaml
<hover:show_text:'<green>Cliquer pour ouvrir le storage'><click:run_command:'/storage'><yellow>[Storage]</click></hover> <gray>Cliquez ici pour accéder
```

## Migration des anciens codes couleur

### Table de conversion

| Ancien (`&`) | Nouveau (MiniMessage) |
|--------------|----------------------|
| `&0` | `<black>` |
| `&1` | `<dark_blue>` |
| `&2` | `<dark_green>` |
| `&3` | `<dark_aqua>` |
| `&4` | `<dark_red>` |
| `&5` | `<dark_purple>` |
| `&6` | `<gold>` |
| `&7` | `<gray>` |
| `&8` | `<dark_gray>` |
| `&9` | `<blue>` |
| `&a` | `<green>` |
| `&b` | `<aqua>` |
| `&c` | `<red>` |
| `&d` | `<light_purple>` |
| `&e` | `<yellow>` |
| `&f` | `<white>` |
| `&l` | `<bold>` |
| `&m` | `<strikethrough>` |
| `&n` | `<underlined>` |
| `&o` | `<italic>` |
| `&k` | `<obfuscated>` |
| `&r` | `<reset>` |

### Exemples de migration

```yaml
# Avant
"&a&l✓ &aSuccès!"

# Après
"<green><bold>✓</bold> Succès!"

# Avant
"&7Vous avez reçu &e64x &fDiamants"

# Après
"<gray>Vous avez reçu <yellow>64x</yellow> <white>Diamants"

# Avant
"&6&l■ &eMenu Principal"

# Après
"<gold><bold>■</bold> <yellow>Menu Principal"
```

## Utilisation dans Quantum

### Dans messages.yml

```yaml
prefix: "<gray>[</gray><gold>Quantum</gold><gray>]</gray> "

storage:
  opened: "<green><bold>✓</bold> Storage ouvert"
  item-added: "<green><bold>✓</bold> Ajouté <yellow>{amount}x</yellow> <white>{item}</white> au storage!"
```

### Dans les menus (storage.yml, sell.yml)

```yaml
menu_title: '<white>Quantum Storage <gray>| <yellow>Mode: %mode_simple%'

items:
  mode_toggle:
    display_name: '<green><bold>⚡</bold> Mode: %mode_simple%'
    lore:
      - '<gray>Mode actuel: <yellow>%mode_simple%'
      - ' '
      - '<yellow>► Cliquez pour changer'
```

## Placeholders disponibles

### Placeholders Quantum

```yaml
%mode%              # Mode complet avec préfixe: "Mode: Stockage" ou "Mode: Vente"
%mode_simple%       # Mode court: "Stockage" ou "Vente"
{player}            # Nom du joueur
{amount}            # Quantité
{item}              # Nom de l'item
{price}             # Prix unitaire
{total_price}       # Prix total
{menu}              # Nom du menu
```

### Combinaison avec PlaceholderAPI

Si PlaceholderAPI est installé, vous pouvez utiliser ses placeholders :

```yaml
"<gray>Joueur: <yellow>%player_name%"
"<gray>Argent: <gold>%vault_eco_balance_formatted%"
```

## Bonnes pratiques

### 1. Fermer les balises

✅ **Correct :**
```yaml
"<green>Texte vert</green>"
```

❌ **Incorrect :**
```yaml
"<green>Texte vert"  # Pas fermé
```

### 2. Ne pas imbriquer les couleurs

✅ **Correct :**
```yaml
"<green>Vert</green> <red>Rouge</red>"
```

❌ **Incorrect :**
```yaml
"<green>Vert <red>Rouge</green></red>"  # Imbrication incorrecte
```

### 3. Utiliser reset pour réinitialiser

```yaml
"<red><bold>Important</bold></red> <reset>Texte normal"
```

### 4. Échapper les guillemets dans hover/click

```yaml
<hover:show_text:'Don\'t forget!'>
```

### 5. Tester vos messages

Utilisez `/quantum reload` après modification pour tester immédiatement.

## Débogage

### Problèmes courants

**Problème** : Le texte s'affiche brut avec les balises
**Solution** : Vérifier que MiniMessage est activé dans le MessageManager

**Problème** : Les couleurs ne s'affichent pas
**Solution** : Vérifier la syntaxe des balises (pas d'espaces)

**Problème** : Le gradient ne fonctionne pas
**Solution** : Vérifier que le texte dans `<gradient>` est assez long

**Problème** : Le hover ne s'affiche pas
**Solution** : Vérifier l'échappement des guillemets

## Ressources

- [Documentation officielle MiniMessage](https://docs.advntr.dev/minimessage/format.html)
- [Web UI MiniMessage](https://webui.advntr.dev/) - Tester vos messages en ligne
- [Discord Adventure](https://discord.gg/MMfhJ8F) - Support communautaire

## Exemples complets

### Message de vente

```yaml
sell:
  success: |
    <green><bold>✓</bold> Vente réussie !
    <gray>Vous avez vendu <yellow>{amount}x {item}
    <gray>Vous avez reçu <gold>{total_price}
```

### Message d'aide interactif

```yaml
help:
  storage: |
    <gold><bold>■</bold> <yellow>Commandes Storage
    <hover:show_text:'<green>Cliquer pour ouvrir'><click:run_command:'/storage'><yellow>/storage</click></hover> <gray>- Ouvrir le storage
    <hover:show_text:'<green>Transférer l\'item en main'><click:suggest_command:'/qstorage transfer hand '><yellow>/qstorage transfer hand</click></hover> <gray>- Transférer
```

### Message de mode avec gradient

```yaml
mode:
  changed: "<gradient:green:gold>Mode changé:</gradient> <yellow>%mode_simple%"
```

### Titre avec emojis

```yaml
title:
  storage_full: |
    <red><bold>⚠ STORAGE PLEIN ⚠</bold>
    <gray>Vendez ou retirez des items pour libérer de l'espace
```

---

**Note** : Tous les messages de Quantum ont été migrés vers MiniMessage. Les anciens codes couleur (`&c`, `&a`, etc.) ne sont plus supportés dans les fichiers de configuration.
