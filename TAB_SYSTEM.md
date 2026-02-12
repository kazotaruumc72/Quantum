# Système TAB Complet avec Header/Footer

## Vue d'ensemble

Le système TAB de Quantum permet de personnaliser l'affichage de la liste des joueurs (TAB) avec des headers et footers différents en fonction des permissions des joueurs. Il utilise le format MiniMessage pour un formatage riche et des couleurs hexadécimales.

## Fonctionnalités

- ✅ Headers et footers personnalisables par groupe de permissions
- ✅ Support de 6 groupes prédéfinis (Elite, MVP+, MVP, VIP+, VIP, Default)
- ✅ Formatage MiniMessage avec couleurs hexadécimales et dégradés
- ✅ Placeholders dynamiques (Quantum + PlaceholderAPI + Serveur)
- ✅ Commandes en jeu pour éditer les headers/footers
- ✅ Rechargement à chaud de la configuration
- ✅ Rafraîchissement automatique des placeholders
- ✅ Système de priorité pour les permissions

## Configuration

### Fichier: `tab_config.yml`

Le fichier de configuration contient les sections suivantes:

#### Paramètres généraux
```yaml
settings:
  enabled: true                    # Activer/désactiver le système
  refresh-interval: 5               # Intervalle de rafraîchissement (secondes)
  priority-order:                   # Ordre de priorité des groupes
    - elite
    - mvp+
    - mvp
    - vip+
    - vip
    - default
```

#### Groupes de permissions

Chaque groupe est défini avec:
- `permission`: La permission requise (vide pour le groupe par défaut)
- `header`: Liste de lignes pour le header
- `footer`: Liste de lignes pour le footer

Exemple:
```yaml
groups:
  elite:
    permission: "quantum.tab.elite"
    header:
      - ""
      - "<gradient:#FFD700:#FFA500>╔═══════════════════════════════════╗</gradient>"
      - "<gradient:#FFD700:#FFA500>║         ⭐ QUANTUM ELITE ⭐      ║</gradient>"
      - "<gradient:#FFD700:#FFA500>╚═══════════════════════════════════╝</gradient>"
      - ""
      - "<#FFD700>Niveau:</> <#FFA500>%quantum_level%</>"
    footer:
      - ""
      - "<#FFD700>Joueurs:</> <#FFA500>%server_online%<#888888>/%server_max_players%</>"
      - ""
```

## Placeholders disponibles

### Placeholders Quantum
- `%quantum_level%` - Niveau du joueur
- `%quantum_job%` - Métier actuel du joueur
- `%quantum_job_level%` - Niveau du métier
- `%quantum_tower%` - Tour actuelle du joueur
- `%quantum_tower_floor%` - Étage actuel dans la tour

### Placeholders Serveur
- `%server_online%` - Nombre de joueurs en ligne
- `%server_max_players%` - Nombre maximum de joueurs
- `%server_tps%` - TPS du serveur

### Placeholders Joueur
- `%player_name%` - Nom du joueur
- `%player_displayname%` - Nom d'affichage du joueur
- `%player_ping%` - Ping du joueur

### PlaceholderAPI
Tous les placeholders de PlaceholderAPI sont supportés si le plugin est installé.

## Formatage MiniMessage

### Couleurs
```yaml
<red>texte rouge</>
<green>texte vert</>
<blue>texte bleu</>
<#FF5733>couleur hexadécimale</>
```

### Dégradés
```yaml
<gradient:#FFD700:#FFA500>Texte avec dégradé</gradient>
```

### Styles
```yaml
<bold>Texte en gras</bold>
<italic>Texte en italique</italic>
<underlined>Texte souligné</underlined>
<strikethrough>Texte barré</strikethrough>
```

## Commandes

### `/tabedit list`
Liste tous les groupes TAB disponibles avec leurs permissions.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit list
```

### `/tabedit reload`
Recharge la configuration TAB sans redémarrer le serveur.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit reload
```

### `/tabedit header <groupe>`
Affiche le header actuel d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit header elite
```

### `/tabedit footer <groupe>`
Affiche le footer actuel d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit footer vip
```

### `/tabedit header <groupe> add <texte>`
Ajoute une ligne au header d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit header elite add <gold>Nouvelle ligne!</gold>
```

### `/tabedit header <groupe> remove <numéro>`
Supprime une ligne du header d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit header elite remove 3
```

### `/tabedit header <groupe> set <numéro> <texte>`
Modifie une ligne du header d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit header elite set 2 <gradient:#FFD700:#FFA500>Nouveau texte</gradient>
```

### `/tabedit header <groupe> clear`
Efface tout le header d'un groupe.

**Permission**: `quantum.tab.edit`

**Exemple**:
```
/tabedit header elite clear
```

## Permissions

### Permissions administratives
- `quantum.tab.*` - Toutes les permissions TAB
- `quantum.tab.edit` - Éditer les headers et footers
- `quantum.tab.reload` - Recharger la configuration

### Permissions de groupe (pour les joueurs)
- `quantum.tab.elite` - Header/footer Elite
- `quantum.tab.mvp+` - Header/footer MVP+
- `quantum.tab.mvp` - Header/footer MVP
- `quantum.tab.vip+` - Header/footer VIP+
- `quantum.tab.vip` - Header/footer VIP
- (Aucune permission requise pour le groupe default)

### Configuration LuckPerms

Pour donner une permission à un groupe:
```
/lp group vip permission set quantum.tab.vip true
/lp group mvp permission set quantum.tab.mvp true
/lp group elite permission set quantum.tab.elite true
```

Pour donner une permission à un joueur:
```
/lp user <joueur> permission set quantum.tab.vip true
```

## Système de priorité

Le système vérifie les permissions dans l'ordre défini dans `priority-order`. Le premier groupe dont le joueur a la permission sera utilisé.

**Ordre par défaut**:
1. Elite (plus haute priorité)
2. MVP+
3. MVP
4. VIP+
5. VIP
6. Default (aucune permission requise)

Exemple: Si un joueur a les permissions `quantum.tab.vip` ET `quantum.tab.mvp`, il recevra le header/footer MVP car il est plus haut dans la priorité.

## Fonctionnement technique

### Initialisation
1. Au démarrage du plugin, TABManager charge `tab_config.yml`
2. Les placeholders Quantum et serveur sont enregistrés auprès de TAB API
3. Un task de rafraîchissement automatique est démarré

### Affichage pour les joueurs
1. Quand un joueur se connecte, TABListener est déclenché
2. Après un délai d'1 seconde (pour charger les données), le système détermine le groupe approprié
3. Les placeholders sont remplacés par leurs valeurs
4. Le header et footer sont envoyés au joueur

### Rafraîchissement
Un task s'exécute toutes les N secondes (défini par `refresh-interval`) pour mettre à jour les placeholders dynamiques (TPS, joueurs en ligne, etc.).

## Exemple de configuration complète

```yaml
settings:
  enabled: true
  refresh-interval: 5
  priority-order:
    - elite
    - mvp+
    - mvp
    - vip+
    - vip
    - default

groups:
  elite:
    permission: "quantum.tab.elite"
    header:
      - ""
      - "<gradient:#FFD700:#FFA500>╔═══════════════════════════════════╗</gradient>"
      - "<gradient:#FFD700:#FFA500>║         ⭐ QUANTUM ELITE ⭐      ║</gradient>"
      - "<gradient:#FFD700:#FFA500>╚═══════════════════════════════════╝</gradient>"
      - ""
      - "<#FFD700>Niveau:</> <#FFA500>%quantum_level%</>"
      - "<#FFD700>Métier:</> <#FFA500>%quantum_job% <#888888>(Niv. %quantum_job_level%)</>"
      - ""
    footer:
      - ""
      - "<#888888>───────────────────────────────────────</>"
      - "<#FFD700>Tour:</> <#FFA500>%quantum_tower% <#888888>- Étage %quantum_tower_floor%</>"
      - "<#FFD700>Joueurs:</> <#FFA500>%server_online%<#888888>/%server_max_players%</> <#888888>│</> <#FFD700>TPS:</> <#FFA500>%server_tps%</>"
      - "<#888888>───────────────────────────────────────</>"
      - ""
  
  default:
    permission: ""
    header:
      - ""
      - "<gradient:#32b8c6:#1d6880>╔═══════════════════════════════════╗</gradient>"
      - "<gradient:#32b8c6:#1d6880>║          QUANTUM SERVER           ║</gradient>"
      - "<gradient:#32b8c6:#1d6880>╚═══════════════════════════════════╝</gradient>"
      - ""
    footer:
      - ""
      - "<#32b8c6>Joueurs:</> <#1d6880>%server_online%<#888888>/%server_max_players%</>"
      - ""
```

## Dépannage

### Les headers/footers ne s'affichent pas
1. Vérifiez que le plugin TAB est installé et activé
2. Vérifiez les logs du serveur pour les erreurs TAB
3. Vérifiez que `tab_config.yml` est bien présent dans le dossier du plugin
4. Utilisez `/tabedit reload` pour recharger la configuration

### Les couleurs ne s'affichent pas correctement
1. Assurez-vous d'utiliser la syntaxe MiniMessage correcte
2. Les couleurs hexadécimales nécessitent Minecraft 1.16+
3. Testez avec des couleurs nommées simples d'abord: `<red>`, `<blue>`, etc.

### Les placeholders ne fonctionnent pas
1. Vérifiez que le placeholder existe (voir la liste ci-dessus)
2. Pour les placeholders PlaceholderAPI, assurez-vous que PAPI est installé
3. Utilisez `/tabedit reload` après avoir modifié des placeholders

### Les permissions ne fonctionnent pas
1. Vérifiez que LuckPerms est installé et configuré
2. Utilisez `/lp user <joueur> permission check quantum.tab.<groupe>` pour vérifier les permissions
3. Vérifiez l'ordre de priorité dans `tab_config.yml`

## Support et développement

Pour signaler un bug ou demander une fonctionnalité:
1. Ouvrez une issue sur le repository GitHub
2. Incluez vos logs et votre configuration
3. Décrivez précisément le problème rencontré

## Intégration avec d'autres plugins

### TAB Plugin
Version requise: 5.5.0 ou supérieur
Le système s'intègre avec TAB pour utiliser son API de placeholders et de mise à jour.

### PlaceholderAPI
Si installé, tous les placeholders PAPI sont automatiquement disponibles dans les headers et footers.

### LuckPerms
Utilisé pour gérer les permissions de groupe. Compatible avec d'autres plugins de permissions via Vault.

## Notes de version

### v1.0.1
- ✅ Système TAB complet avec headers/footers
- ✅ Support de 6 groupes de permissions
- ✅ Commandes d'édition en jeu
- ✅ Placeholders Quantum, serveur et PlaceholderAPI
- ✅ Formatage MiniMessage avec dégradés
- ✅ Rafraîchissement automatique
- ✅ Configuration YAML complète
