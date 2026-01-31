# Quantum - Advanced Virtual Storage & Dynamic GUI Builder

> 🚀 Plugin Minecraft Spigot/Paper pour un système de stockage virtuel illimité avec constructeur de GUI dynamique

## 📋 Table des Matières

- [Fonctionnalités](#-fonctionnalités)
- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Commandes](#-commandes)
- [Permissions](#-permissions)
- [Menus](#-menus)
- [API Développeur](#-api-développeur)
- [Support](#-support)

## ✨ Fonctionnalités

### 🎒 Système de Stockage Virtuel

- **Capacité illimitée** - Stockez autant d'items que vous voulez
- **Support Nexo** - Compatible avec les items custom Nexo
- **Items Vanilla** - Support complet des items Minecraft
- **Syntaxe explicite** - `nexo:id` et `minecraft:id` pour éviter les conflits
- **Base de données** - Sauvegarde MySQL ou SQLite
- **GUI Dynamique** - Menu storage qui se met à jour automatiquement

### 🎨 Constructeur de GUI

- **Fichiers YAML** - Configuration facile des menus
- **Titres animés** - Animations frame par frame
- **Custom Model Data** - Support des modèles custom via resource packs
- **Effet Glow** - Ajoutez un effet lumineux aux items
- **Hide Flags** - Masquez les tooltips indésirables
- **Items Nexo** - Intégration complète avec Nexo
- **Actions** - Système d'actions au clic (commandes, messages, sons, etc.)
- **Requirements** - Conditions d'affichage et de clic
- **PlaceholderAPI** - Support des placeholders

### 🛠️ Fonctionnalités Avancées

- **Tab Completion** - Autocomplétion intelligente
- **Console Commands** - Gestion des joueurs depuis la console
- **Multi-langue** - Système de messages personnalisables
- **Hotreload** - Rechargement sans redémarrage
- **Protection GUI** - Items non déplaçables automatiquement

## 📦 Installation

### Prérequis

- **Serveur**: Spigot, Paper, Purpur ou Folia 1.16+
- **Java**: 11 ou supérieur
- **Optionnel**: 
  - [Nexo](https://nexomc.com/) pour les items custom
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) pour les placeholders

### Installation

1. Téléchargez `Quantum.jar`
2. Placez-le dans le dossier `plugins/`
3. Redémarrez le serveur
4. Les fichiers de configuration seront créés automatiquement

### Structure des Fichiers

```
plugins/Quantum/
├── config.yml              # Configuration principale
├── menus/                  # Dossier des menus
│   ├── example.yml         # Menu d'exemple simple
│   ├── example_advanced.yml # Menu avec toutes les features
│   └── storage.yml         # Menu du storage
└── messages/               # Dossier des messages
    ├── messages_en.yml     # Messages anglais
    └── messages_fr.yml     # Messages français
```

## ⚙️ Configuration

### config.yml

```yaml
database:
  type: sqlite  # mysql ou sqlite
  host: localhost
  port: 3306
  database: quantum
  username: root
  password: ''

language: fr  # en ou fr
```

### Configuration des Menus

Voir [MENU_GUIDE.md](MENU_GUIDE.md) pour le guide complet de création de menus.

**Exemple simple:**

```yaml
menu_title: '&6&lMon Menu'
size: 27
open_command: monmenu

items:
  custom_item:
    slot: 13
    material: DIAMOND_SWORD
    custom_model_data: 1001  # ID du modèle custom
    display_name: '&b&lÉpée Légendaire'
    lore:
      - '&7Une épée puissante!'
    glow: true  # Effet lumineux
    hide_flags:
      - HIDE_ENCHANTS
      - HIDE_ATTRIBUTES
    left_click:
      actions:
        - '[message] &aVous avez cliqué!'
        - '[sound] ENTITY_PLAYER_LEVELUP:1.0:1.0'
```

## 💻 Commandes

### Commandes Joueur

#### `/qstorage` (Aliases: `/qs`, `/quantumstorage`)

**Transfer (Transférer vers le storage):**
```
/qstorage transfer hand              # Item dans la main
/qstorage transfer hand 32           # 32 items de la main
/qstorage transfer all               # Tout l'inventaire
/qstorage transfer diamond 64        # 64 diamants (auto-détection)
/qstorage transfer nexo:custom_sword 10     # 10 épées Nexo
/qstorage transfer minecraft:diamond 64     # 64 diamants vanilla
```

**Remove (Retirer du storage):**
```
/qstorage remove diamond 32          # 32 diamants
/qstorage remove nexo:custom_sword 5        # 5 épées Nexo
/qstorage remove minecraft:emerald 16       # 16 émeraudes vanilla
```

### Commandes Console

**Transfer:**
```
/qstorage transfer minecraft:diamond 64 Notch
/qstorage transfer nexo:custom_item 10 Steve
```

**Remove:**
```
/qstorage remove minecraft:diamond 64 Notch
/qstorage remove nexo:custom_item 10 Steve
```

### Commandes Admin

```
/quantum reload              # Recharger la configuration
/storage <player>           # Ouvrir le storage d'un joueur
/menu <menu> [player]       # Ouvrir un menu
```

## 🔑 Permissions

```yaml
quantum.admin              # Accès admin complet
quantum.reload             # Recharger le plugin
quantum.storage.use        # Utiliser le storage
quantum.storage.transfer   # Transférer des items
quantum.storage.remove     # Retirer des items
quantum.menu.open          # Ouvrir les menus
quantum.menu.admin         # Ouvrir menus d'autres joueurs
```

## 📚 Menus

### Features Disponibles

#### Custom Model Data
```yaml
items:
  custom_item:
    slot: 10
    material: DIAMOND_SWORD
    custom_model_data: 1001  # Votre ID de modèle
```

#### Effet Glow
```yaml
items:
  glowing_item:
    slot: 11
    material: DIAMOND
    glow: true  # Ajoute l'effet lumineux
```

#### Hide Flags (Tooltips Custom)
```yaml
items:
  clean_item:
    slot: 12
    material: POTION
    hide_flags:
      - HIDE_POTION_EFFECTS  # Masquer effets potion
      - HIDE_ATTRIBUTES      # Masquer attributs
      - HIDE_ENCHANTS        # Masquer enchantements
      - HIDE_UNBREAKABLE     # Masquer incassable
      - HIDE_DESTROYS        # Masquer "Peut détruire"
      - HIDE_PLACED_ON       # Masquer "Peut être placé"
      - HIDE_DYE             # Masquer couleur armure
```

#### Items Nexo
```yaml
items:
  nexo_item:
    slot: 13
    nexo_item: your_nexo_item_id  # ID Nexo
    glow: true  # Peut ajouter glow sur items Nexo
```

#### Titres Animés
```yaml
animated_title:
  enabled: true
  speed: 10  # Ticks entre chaque frame
  frames:
    - '&6&l>> &e&lStorage &6&l<<'
    - '&e&l>> &6&lStorage &e&l<<'
```

#### Actions
```yaml
left_click:
  actions:
    - '[message] &aMessage au joueur'
    - '[console] give %player% diamond 1'
    - '[player] say Hello'
    - '[sound] ENTITY_PLAYER_LEVELUP:1.0:1.0'
    - '[close]'
    - '[menu] autre_menu'
```

#### Requirements
```yaml
view_requirements:
  - 'permission: quantum.vip'
  - 'money >= 1000'
  - 'level >= 10'

click_requirements:
  - 'item: DIAMOND 10'  # Besoin de 10 diamants
  - 'permission: quantum.admin'
```

## 👨‍💻 API Développeur

### Ajouter Quantum comme dépendance

**Maven:**
```xml
<dependency>
    <groupId>com.wynvers</groupId>
    <artifactId>quantum</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```gradle
compileOnly 'com.wynvers:quantum:1.0.0'
```

### Utiliser l'API

```java
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Example {
    
    public void exampleUsage(Player player) {
        Quantum quantum = Quantum.getInstance();
        
        // Accéder au storage d'un joueur
        PlayerStorage storage = quantum.getStorageManager().getStorage(player);
        
        // Ajouter des items
        storage.addItem(Material.DIAMOND, 64);
        storage.addNexoItem("custom_sword", 10);
        
        // Vérifier et retirer
        if (storage.hasItem(Material.DIAMOND, 32)) {
            storage.removeItem(Material.DIAMOND, 32);
        }
        
        // Sauvegarder
        storage.save(quantum);
        
        // Ouvrir un menu
        quantum.getMenuManager().getMenu("storage").open(player);
    }
}
```

### Events

```java
@EventHandler
public void onStorageUpdate(StorageUpdateEvent event) {
    Player player = event.getPlayer();
    PlayerStorage storage = event.getStorage();
    // Faire quelque chose...
}

@EventHandler
public void onMenuOpen(MenuOpenEvent event) {
    Player player = event.getPlayer();
    Menu menu = event.getMenu();
    // Faire quelque chose...
}
```

## 📖 Documentation Complète

- [COMMANDS.md](COMMANDS.md) - Documentation complète des commandes
- [MENU_GUIDE.md](MENU_GUIDE.md) - Guide de création de menus
- [API.md](API.md) - Documentation API pour développeurs

## 🐛 Support

### Rapporter un Bug

Ouvrez une [issue sur GitHub](https://github.com/kazotaruumc72/Quantum/issues) avec:
- Version de Quantum
- Version du serveur
- Plugins installés
- Logs d'erreur
- Steps pour reproduire

### Demande de Feature

Utilisez les [GitHub Discussions](https://github.com/kazotaruumc72/Quantum/discussions) pour:
- Proposer de nouvelles features
- Discuter d'améliorations
- Partager vos créations

## 📜 Licence

© 2026 Wynvers Studios - Tous droits réservés

Développé par [Kazotaruu_](https://github.com/kazotaruumc72)

## 🌟 Features à Venir

- [ ] System de pages pour le storage GUI
- [ ] Filtres et recherche d'items
- [ ] Backup automatique du storage
- [ ] Interface web de gestion
- [ ] Support de MythicMobs items
- [ ] Intégration economy (vente d'items)
- [ ] Statistiques de stockage

---

**Merci d'utiliser Quantum !** ⚡
