# Guide d'Installation Rapide - BetterHud pour Quantum

## 📋 Prérequis

- Serveur Minecraft 1.21.11 (Paper/Spigot)
- Plugin Quantum 1.0.1+
- Plugin BetterHud 1.14.1+
- Plugin PlaceholderAPI (recommandé)
- Plugin Vault (pour l'économie)

## 🚀 Installation en 5 Minutes

### Étape 1 : Télécharger les Plugins

1. **BetterHud** : [Télécharger sur SpigotMC](https://www.spigotmc.org/resources/betterhud.105121/)
2. **PlaceholderAPI** : [Télécharger sur SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/)
3. **Vault** : [Télécharger sur SpigotMC](https://www.spigotmc.org/resources/vault.34315/)

Placez les fichiers `.jar` dans le dossier `plugins/` de votre serveur.

### Étape 2 : Premier Démarrage

1. Démarrez le serveur une première fois
2. Arrêtez le serveur (les plugins créent leurs dossiers)
3. Vous devriez maintenant avoir :
   ```
   plugins/
   ├── Quantum/
   ├── BetterHud/
   ├── PlaceholderAPI/
   └── Vault/
   ```

### Étape 3 : Installer les Configurations Quantum

Copiez les fichiers de configuration depuis `Quantum/src/main/resources/betterhud-examples/` :

```bash
# Depuis le dossier du serveur
cd plugins/

# Copier la configuration principale
cp Quantum/betterhud-examples/config.yml BetterHud/config.yml

# Créer les dossiers nécessaires
mkdir -p BetterHud/popups
mkdir -p BetterHud/huds
mkdir -p BetterHud/compass

# Copier les configurations
cp Quantum/betterhud-examples/popups.yml BetterHud/popups/quantum_popups.yml
cp Quantum/betterhud-examples/huds.yml BetterHud/huds/quantum_huds.yml
cp Quantum/betterhud-examples/compass.yml BetterHud/compass/quantum_compass.yml
```

**Ou manuellement** :
1. Ouvrez `plugins/Quantum/betterhud-examples/`
2. Copiez chaque fichier vers sa destination (voir README.md)

### Étape 4 : Installer les Placeholders

Si vous utilisez PlaceholderAPI :

```
/papi ecloud download Player
/papi ecloud download Server
/papi ecloud download Statistic
/papi ecloud download Vault
/papi reload
```

### Étape 5 : Redémarrer et Tester

1. Redémarrez le serveur complètement
2. Connectez-vous en jeu
3. Testez l'intégration :
   ```
   /huddemo popup test_popup
   /huddemo waypoint add test
   /huddemo test
   ```

## ✅ Vérification de l'Installation

### Vérifier que BetterHud est chargé

Dans la console du serveur, vous devriez voir :
```
[Quantum] ✓ BetterHud Integration initialized! (Optimized HUD & Compass)
[Quantum] ✓ BetterHud Demo Command registered
```

### Vérifier les permissions

Donnez-vous la permission pour tester :
```
/lp user VOTRE_PSEUDO permission set quantum.betterhud.use true
```

Ou ajoutez dans `permissions.yml` :
```yaml
default:
  default: true
  permissions:
    quantum.betterhud.use: true
```

### Vérifier les HUDs

1. Connectez-vous en jeu
2. Vous devriez voir le HUD principal s'afficher automatiquement
3. Testez les commandes :
   ```
   /huddemo popup success message:"Test réussi!"
   /huddemo waypoint add ma_base
   /huddemo waypoint list
   ```

## 🎯 Première Personnalisation

### Changer les Couleurs du HUD Principal

Éditez `plugins/BetterHud/huds/quantum_huds.yml` :

```yaml
quantum_main:
  layouts:
    default:
      player_info:
        text:
          - "<gradient:#FF0000:#00FF00><bold>QUANTUM</bold></gradient>"  # Changez ces couleurs
```

### Activer/Désactiver des Popups

Éditez `plugins/BetterHud/config.yml` :

```yaml
quantum-integration:
  auto-popups:
    level-up: true          # Popup lors d'un level up
    job-levelup: true       # Popup lors d'un job level up
    storage-change: false   # Désactiver les notifications de stockage
    money-change: true      # Popup lors de transactions
```

### Changer la Position du HUD

Éditez les coordonnées `x` et `y` :

```yaml
player_info:
  type: text
  x: 10      # Distance depuis le bord gauche (négatif = droite)
  y: 10      # Distance depuis le haut (négatif = bas)
  align: LEFT  # LEFT, CENTER, RIGHT
```

## 🔧 Résolution de Problèmes Courants

### Problème : "BetterHud not found"

**Solution** :
1. Vérifiez que BetterHud.jar est dans `plugins/`
2. Redémarrez le serveur
3. Vérifiez la version : BetterHud 1.14.1+ requis

### Problème : Les popups ne s'affichent pas

**Solution** :
1. Vérifiez les permissions : `/lp user PSEUDO permission set quantum.betterhud.use true`
2. Testez avec : `/huddemo popup test_popup`
3. Vérifiez les logs : `plugins/BetterHud/logs/latest.log`
4. Vérifiez que le fichier popup existe : `plugins/BetterHud/popups/quantum_popups.yml`

### Problème : Variables non remplacées (affichage ${variable})

**Solution** :
1. Installez PlaceholderAPI
2. Installez les expansions nécessaires (voir Étape 4)
3. Utilisez `/papi parse PSEUDO %player_name%` pour tester
4. Redémarrez le serveur après installation

### Problème : Erreur YAML "mapping values are not allowed here"

**Solution** :
1. Vérifiez l'indentation (espaces uniquement, pas de tabulations)
2. Vérifiez les deux-points `:` (espace après)
3. Utilisez un validateur YAML : https://www.yamllint.com/
4. Comparez avec les exemples fournis

### Problème : Performance (lag)

**Solution** :
Éditez `plugins/BetterHud/config.yml` :
```yaml
performance:
  update-interval: 4        # Augmentez (moins de mises à jour)
  cache-player-data: true
  async-updates: true

settings:
  update-interval: 4        # Au lieu de 2
```

## 📱 Commandes Utiles

### Commandes Admin

```bash
/betterhud reload              # Recharger les configurations
/betterhud debug               # Activer le mode debug
/betterhud version             # Voir la version
```

### Commandes Joueur (avec permission)

```bash
/huddemo popup <nom>           # Afficher un popup
/huddemo waypoint add <nom>    # Ajouter un waypoint
/huddemo waypoint remove <nom> # Retirer un waypoint
/huddemo waypoint clear        # Effacer tous les waypoints
/huddemo waypoint list         # Lister les waypoints
/huddemo test                  # Popup de test
```

### Commandes Quantum

```bash
/quantum reload                # Recharger Quantum (BetterHud aussi)
/storage                       # Ouvrir le stockage (avec HUD spécial)
/job                          # Système de métiers (avec HUD job)
```

## 🎨 Exemples de Personnalisation

### Exemple 1 : Popup Personnalisé Simple

Ajoutez dans `plugins/BetterHud/popups/quantum_popups.yml` :

```yaml
mon_popup:
  layouts:
    default:
      location: CENTER
      y: 0
      text:
        - "<gold><bold>Mon Message</bold></gold>"
        - "<white>Texte personnalisé ici"
  animation:
    fade-in: 10
    stay: 60
    fade-out: 10
```

Utilisez en jeu : `/huddemo popup mon_popup`

### Exemple 2 : HUD Minimaliste

Créez un nouveau fichier `plugins/BetterHud/huds/mon_hud.yml` :

```yaml
mon_hud_simple:
  settings:
    group:
      - default
  layouts:
    default:
      info:
        type: text
        x: 10
        y: 10
        text:
          - "<white>❤ ${health} | <gold>$ ${money} | <yellow>Lv.${player_level}"
```

### Exemple 3 : Waypoint Automatique

Dans votre code Java :

```java
// Créer un waypoint lors d'une quête
QuantumCompassManager compass = plugin.getCompassManager();
Location questLoc = new Location(world, 100, 64, 200);
compass.addWaypoint(player, "Objectif Quête", questLoc);

// Le joueur verra le waypoint dans son HUD compass
```

## 📚 Ressources Supplémentaires

- **Documentation Complète** : [BETTERHUD_INTEGRATION.md](../BETTERHUD_INTEGRATION.md)
- **Configuration Détaillée** : [README.md](README.md)
- **Wiki BetterHud** : https://github.com/toxicity188/BetterHud/wiki
- **Support Discord Quantum** : [Lien Discord si disponible]

## 🎓 Prochaines Étapes

1. ✅ Installation terminée
2. 📖 Lisez le README.md pour la personnalisation avancée
3. 🎨 Personnalisez les couleurs et positions selon vos préférences
4. 🔌 Créez vos propres popups pour vos systèmes customs
5. 📊 Ajoutez des placeholders personnalisés si nécessaire
6. 🧪 Testez avec vos joueurs et ajustez

## 💡 Conseils Pro

1. **Sauvegardez** vos configurations avant de modifier
2. **Testez** sur un serveur de test d'abord
3. **Utilisez** `/betterhud reload` au lieu de redémarrer le serveur
4. **Documentez** vos modifications personnalisées
5. **Partagez** vos créations avec la communauté

## 🆘 Besoin d'Aide ?

Si vous rencontrez des problèmes :

1. Consultez ce guide d'installation
2. Vérifiez les logs : `plugins/BetterHud/logs/` et `logs/latest.log`
3. Testez les commandes de base
4. Créez une issue sur GitHub avec :
   - Version de Minecraft
   - Version de Quantum
   - Version de BetterHud
   - Message d'erreur complet
   - Configuration concernée

Bonne chance ! 🚀
