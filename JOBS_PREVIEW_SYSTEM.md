# Job System - Preview & Reward System

**Inspiré de UniverseJobs** - Système de preview des actions et affichage amélioré des récompenses

## 🎯 Fonctionnalités principales

### 1. Preview des Actions en Temps Réel

Lorsque vous interagissez avec une structure, vous pouvez maintenant voir un aperçu des récompenses **avant** de récolter.

#### Comment utiliser:
- **Clic droit** sur une structure → Affiche la preview dans l'action bar
- **Clic gauche** sur une structure → Exécute l'action et donne les récompenses

#### Ce qui est affiché:
```
█ ⛏ Bûcheron » +10 XP │ +5.0$
```
- État de la structure (█ = WHOLE, ▓ = GOOD, ▒ = DAMAGED, ░ = STUMP)
- Nom du métier avec couleur
- XP à gagner
- Argent à gagner
- Indicateur de booster actif (✦)

#### Avec boosters actifs:
```
█ ⛏ Bûcheron » +15 XP ✦ │ +10.0$ ✦
```

#### Structure invalide:
```
⚠ Structure invalide pour votre métier
```

### 2. Preview Détaillée des Récompenses

Utilisez `/job rewards preview` pour voir un aperçu complet et formaté de vos prochaines récompenses.

#### Commandes:
```
/job rewards preview        # Affiche les 3 prochains niveaux avec récompenses
/job rewards preview 5      # Affiche les 5 prochains niveaux avec récompenses
/job rewards preview 10     # Affiche jusqu'à 10 niveaux
```

#### Affichage:
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

  ◆ Niveau 20 (1247 XP restants)
    • ✦ Booster Argent x2.0 - 2h

Utilisez /job rewards pour voir plus de récompenses.
```

### 3. Affichage Amélioré des Récompenses

La commande `/job rewards` a été améliorée avec:
- Icônes colorés pour chaque type de récompense
- Meilleur formatage et organisation
- Indicateurs pour les boosters "donjon uniquement"

```
════════════════════════════════════
✦ Prochaines Récompenses
════════════════════════════════════

▸ Niveau 5:
  • 💰 100$
  • ⚙ Commande spéciale

▸ Niveau 10:
  • 📦 magic_log x5 (Nexo)
  • ⚙ Action joueur

▸ Niveau 15:
  • ✦ Booster XP x1.5 (60 min) (Donjon)

Astuce: Utilisez /job rewards preview pour un aperçu détaillé!
```

## 📊 Types de Récompenses et Icônes

| Type | Icône | Description |
|------|-------|-------------|
| Argent | 💰 | Argent via Vault |
| Item Nexo | 📦 | Items custom Nexo |
| Item MythicMobs | ⚔ | Items MythicMobs |
| Booster XP | ✦ | Multiplicateur d'XP |
| Booster Argent | ✦ | Multiplicateur d'argent |
| Commande | ⚙ | Commande console/joueur |

## 🎨 Indicateurs Visuels

### États de Structure
- `█` **WHOLE** - Structure entière (100%)
- `▓` **GOOD** - Bon état (75%)
- `▒` **DAMAGED** - Abîmée (50%)
- `░` **STUMP** - Souche (25%)

### Boosters Actifs
- `✦` Indique qu'un booster est actif et s'applique
- Affiché après l'XP ou l'argent concerné

### Barres de Progression
```
[███████████████████████░░░░░░░░░░░] 77.3%
```
- Vert (█) = Progression complétée
- Gris foncé (█) = Progression restante
- Pourcentage affiché à la fin

## 🔧 Configuration

### jobs.yml

Ajout de nouveaux messages configurables:
```yaml
messages:
  # Messages pour le système de preview
  preview_hint: "&8[&7Clic droit pour aperçu&8]"
  preview_no_job: "&c⚠ Aucun métier sélectionné"
  preview_invalid_structure: "&c⚠ Structure invalide pour votre métier"
  preview_no_rewards: "&7Aucune récompense"
```

## 💡 Exemples d'Utilisation

### Scénario 1: Bûcheron débutant
```
Joueur: /job select lumberjack
Système: ✓ Vous avez sélectionné le métier: ⛏ Bûcheron

Joueur: *Clic droit sur un arbre*
Action Bar: █ ⛏ Bûcheron » +10 XP │ +5.0$

Joueur: *Clic gauche sur l'arbre*
Chat: +10 XP ⛏ Bûcheron | +5.0$

Joueur: /job rewards preview
Chat: [Affichage détaillé des 3 prochaines récompenses]
```

### Scénario 2: Avec boosters actifs
```
Joueur: *Active un booster XP x1.5*
Système: ✓ Booster activé: Booster d'XP (x1.5)

Joueur: *Clic droit sur un arbre*
Action Bar: █ ⛏ Bûcheron » +15 XP ✦ │ +5.0$

Joueur: *Clic gauche sur l'arbre*
Chat: +15 XP ⛏ Bûcheron | +5.0$
```

### Scénario 3: Structure invalide
```
Joueur: *Clic droit sur un minerai (job = lumberjack)*
Action Bar: ⚠ Structure invalide pour votre métier

Joueur: *Clic gauche sur le minerai*
Chat: Cette structure ne correspond pas à votre métier!
```

## 🚀 Comparaison avec UniverseJobs

| Fonctionnalité | UniverseJobs | Quantum (Nouveau) |
|----------------|--------------|-------------------|
| Preview d'actions | ✅ GUI customisable | ✅ Action bar avec icônes |
| Affichage récompenses | ✅ GUI avec items | ✅ Chat formaté avec emojis |
| Indicateurs boosters | ✅ Lore items | ✅ Symboles ✦ |
| Barre de progression | ✅ Items dans GUI | ✅ Barre visuelle ASCII |
| Calcul XP restante | ✅ | ✅ |
| Types de récompenses | ✅ Nombreux | ✅ 7 types supportés |

## 📝 Notes Techniques

### Classes ajoutées/modifiées:
- `ActionPreview.java` - Nouvelle classe pour la preview
- `JobManager.java` - Intégration ActionPreview
- `JobCommand.java` - Commandes améliorées
- `ToolListener.java` - Gestion clic droit/gauche

### Performance:
- Preview affichée instantanément (action bar)
- Calculs XP optimisés
- Pas de lag lors de l'affichage

### Compatibilité:
- Compatible avec le système existant
- Pas de breaking changes
- Rétrocompatible avec anciennes configurations

## 🐛 Dépannage

### La preview ne s'affiche pas
1. Vérifier que vous avez un métier sélectionné (`/job`)
2. Vérifier que la structure est valide pour votre métier
3. Essayer `/job info` pour voir les structures valides

### Les récompenses ne s'affichent pas
1. Vérifier que le métier a des récompenses configurées
2. Utiliser `/job rewards preview` au lieu de `/job rewards`
3. Vérifier la configuration dans `jobs.yml`

### Les boosters ne sont pas indiqués
1. Vérifier que le booster est actif (`/jobadmin info <joueur>`)
2. Vérifier si le booster est "dungeon_only" et que vous êtes dans un donjon
3. Attendre 60 secondes après activation du booster

## 📚 Ressources

- [JOBS_SYSTEM.md](JOBS_SYSTEM.md) - Documentation complète du système
- [UniverseJobs GitHub](https://github.com/UniverseStudiosMC/UniverseJobs) - Plugin d'inspiration
- Configuration example: `src/main/resources/jobs.yml`
