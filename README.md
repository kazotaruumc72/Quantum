# Quantum - Advanced Virtual Storage & Order Trading System

> 🚀 Plugin Minecraft Spigot/Paper pour stockage virtuel illimité avec système d'ordres d'achat et GUI dynamiques

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.16+-green.svg)
![Java](https://img.shields.io/badge/java-11+-orange.svg)

---

## 📋 Table des Matières

- [Fonctionnalités](#-fonctionnalités)
- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Système de Storage](#-système-de-storage)
- [Système d'Ordres](#-système-dordres)
- [Commandes](#-commandes)
- [Permissions](#-permissions)
- [PlaceholderAPI](#-placeholderapi)
- [Menus Dynamiques](#-menus-dynamiques)
- [API Développeur](#-api-développeur)
- [Support](#-support)

---

## ✨ Fonctionnalités

### 🎒 Système de Stockage Virtuel

- **Capacité illimitée** - Stockez autant d'items que vous voulez
- **GUI Read-Only** - Joueurs visualisent uniquement, admins gèrent
- **Gestion Admin/Console** - Seuls admins et console contrôlent les items
- **Support Nexo** - Compatible avec les items custom Nexo
- **Items Vanilla** - Support complet des items Minecraft
- **Syntaxe explicite** - `nexo:id` et `minecraft:id` pour éviter les conflits
- **Base de données** - Sauvegarde MySQL ou SQLite
- **GUI Dynamique** - Menu storage avec placeholders temps réel
- **PlaceholderAPI** - Placeholders pour afficher les quantités stockées

### 📦 Système d'Ordres d'Achat

- **Créer des ordres** - Les joueurs commandent des items aux autres
- **3 Modes Storage**:
  - **MODE VIEW**: Voir le contenu (par défaut)
  - **MODE RECHERCHE**: Créer des ordres d'achat depuis le storage
  - **MODE VENTE**: Vendre des items depuis l'inventaire
- **Catégories d'ordres** - Organisation par type d'items (cultures, minerais, autre, etc.)
- **Système de prix** - Prix configurables avec économie Vault
- **Transaction sécurisée**:
  - Argent retiré à la création de l'ordre
  - Items transférés automatiquement du storage vendeur → inventaire acheteur
  - Argent transféré automatiquement à la vente
  - Suppression automatique de l'ordre après transaction
- **Menu de confirmation** - Avant toute transaction
- **Gestion des ordres**:
  - Shift + Clic Gauche (Admin): Supprimer n'importe quel ordre
  - Shift + Clic Droit (Propriétaire): Supprimer son propre ordre

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
- **Système de cache** - Optimisation des performances

---

## 📦 Installation

### Prérequis

- **Serveur**: Spigot, Paper, Purpur ou Folia 1.16+
- **Java**: 11 ou supérieur
- **Requis**: 
  - [Vault](https://www.spigotmc.org/resources/vault.34315/) - Pour l'économie
- **Optionnel**: 
  - [Nexo](https://nexomc.com/) - Pour les items custom
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Pour les placeholders

### Installation

1. **Téléchargez** `Quantum.jar`
2. **Installez** Vault et votre plugin d'économie (EssentialsX, etc.)
3. **Placez** Quantum.jar dans le dossier `plugins/`
4. **Redémarrez** le serveur
5. **Configuration** automatique créée dans `plugins/Quantum/`

### Structure des Fichiers

```
plugins/Quantum/
├── config.yml              # Configuration principale
├── orders.yml              # Ordres d'achat en cours
├── menus/                  # Dossier des menus
│   ├── storage.yml         # Menu du storage (3 modes)
│   ├── orders_cultures.yml # Menu ordres cultures
│   ├── orders_minerais.yml # Menu ordres minerais
│   ├── orders_autre.yml    # Menu ordres autres
│   └── order_confirm.yml   # Menu confirmation transaction
└── messages/               # Dossier des messages
    ├── messages_en.yml     # Messages anglais
    └── messages_fr.yml     # Messages français
```

---

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

---

## 💾 Système de Storage

### Architecture du Storage

#### **👥 Joueurs (View-Only)**

**Commande:**
```
/storage    # Ouvrir le GUI (lecture seule par défaut)
```

**Peuvent:**
- ✅ Voir tous les items stockés
- ✅ Voir les quantités en temps réel
- ✅ Consulter via placeholders
- ✅ Changer de mode (VIEW/RECHERCHE/VENTE) avec permission

**Ne peuvent PAS:**
- ❌ Déposer des items (sauf admins)
- ❌ Retirer directement des items (sauf admins)
- ❌ Utiliser `/qstorage` (admin-only)

---

#### **🔑 Admins (`quantum.admin`)**

**Accès complet via commandes:**

```bash
# Déposer
/qstorage transfer hand
/qstorage transfer all
/qstorage transfer diamond 64
/qstorage transfer nexo:custom_sword 10

# Retirer
/qstorage remove diamond 32
/qstorage remove nexo:custom_sword 5
```

**Accès complet via GUI interactif:**
- ✅ Drag & drop pour déposer
- ✅ Clic gauche: retirer 1 stack (64 max)
- ✅ Clic droit: retirer 1 item
- ✅ Shift + Clic: retirer TOUT
- ✅ Shift-click depuis inventaire: déposer

---

#### **🖥️ Console**

**Gestion complète avec ciblage joueur:**

```bash
# Ajouter au storage d'un joueur
qstorage transfer minecraft:diamond 64 Notch
qstorage transfer nexo:ruby 10 Steve

# Retirer du storage d'un joueur
qstorage remove minecraft:diamond 32 Notch
qstorage remove nexo:custom_item 5 Steve
```

**Cas d'usage:**
- Récompenses automatiques
- Systèmes de shop
- Quêtes avec récompenses
- Scripts administratifs

---

### Modes du Storage

Le menu storage possède **3 modes** accessibles via bouton:

#### **MODE VIEW** (Défaut)
- Affichage du contenu du storage
- Lecture seule pour joueurs
- Gestion complète pour admins

#### **MODE RECHERCHE**
- Créer des ordres d'achat depuis le storage
- Cliquer sur un item → configurer prix et quantité
- Argent retiré immédiatement à la création
- Ordre publié dans la catégorie correspondante

#### **MODE VENTE**
- Vendre des items de son inventaire aux ordres existants
- Shift-click sur item inventaire → menu de sélection d'ordre
- Transaction automatique si validation

---

## 📦 Système d'Ordres

### Vue d'ensemble

Le système d'ordres permet aux joueurs d'acheter et vendre des items entre eux avec une économie intégrée.

### Workflow Complet

#### **1. Création d'un Ordre (Acheteur)**

```
/storage → MODE RECHERCHE → Clic sur item → Configurer
```

1. Ouvrir `/storage`
2. Cliquer sur bouton **MODE RECHERCHE**
3. Cliquer sur l'item recherché
4. Entrer la **quantité** dans le chat
5. Entrer le **prix par unité** dans le chat
6. **Argent retiré immédiatement** du compte
7. Ordre publié dans la catégorie appropriée

**Exemple:**
```
Joueur A cherche: 64 Diamonds à 10$/unité
Coût total: 640$
→ 640$ retirés immédiatement
→ Ordre créé dans "Ordres - Minerais"
```

---

#### **2. Acceptation d'un Ordre (Vendeur)**

```
/quantum orders <catégorie> → Clic sur ordre → VENDRE
```

**Via menu orders:**
1. Ouvrir `/quantum orders cultures` (ou minerais/autre)
2. Voir tous les ordres disponibles
3. Cliquer sur un ordre
4. Menu de confirmation s'ouvre
5. Vérifier les détails (quantité, prix, votre stock)
6. Cliquer sur **VENDRE** (lime dye)

**Via mode VENTE:**
1. Ouvrir `/storage`
2. Cliquer sur bouton **MODE VENTE**
3. Shift-clic sur item dans votre inventaire
4. Choisir l'ordre parmi les disponibles
5. Transaction automatique

---

#### **3. Transaction Automatique**

Lors de la vente:

1. **Vérifications**:
   - ✅ Vendeur a assez d'items en storage
   - ✅ Items matchent exactement (type + custom model data)
   - ✅ Vendeur ≠ Acheteur (pas d'auto-vente)

2. **Transferts**:
   - 💰 Argent: Acheteur → Vendeur
   - 📦 Items: Storage Vendeur → Inventaire Acheteur (si online) ou Storage (si offline)

3. **Finalisation**:
   - 🗑️ Ordre supprimé de `orders.yml`
   - 📨 Notifications envoyées aux deux joueurs
   - 📝 Transaction loggée dans la console

**Exemple de transaction:**
```
[AVANT]
Acheteur A: 640$ retirés (lors création ordre)
Vendeur B: 64 Diamonds en storage

[TRANSACTION]
→ 640$ transférés à Vendeur B
→ 64 Diamonds retirés du storage de B
→ 64 Diamonds ajoutés à l'inventaire de A

[APRÈS]
Acheteur A: Reçoit 64 Diamonds
Vendeur B: Reçoit 640$
Ordre: Supprimé
```

---

#### **4. Gestion des Ordres**

**Supprimer un ordre:**

- **Shift + Clic Gauche** (Admin): Supprimer n'importe quel ordre
- **Shift + Clic Droit** (Propriétaire): Supprimer son propre ordre

**Note**: L'argent n'est **PAS remboursé** lors de la suppression. Prévoir un système de remboursement admin si nécessaire.

---

### Catégories d'Ordres

Les ordres sont organisés par catégories:

- **Cultures** (`/quantum orders cultures`) - Blé, carottes, pommes de terre, etc.
- **Minerais** (`/quantum orders minerais`) - Diamants, fer, or, etc.
- **Autre** (`/quantum orders autre`) - Autres items

**Configuration:** Voir `menus/orders_*.yml` pour personnaliser.

---

### Menu de Confirmation (order_confirm)

Avant chaque transaction, un menu apparaît:

```
┌─────────────────────────────┐
│ [VENDRE] [ITEM] [REFUSER]  │
└─────────────────────────────┘
```

**Slot 1 - VENDRE (LIME_DYE)**
- Clic → Exécute la transaction
- Affiche: Argent à recevoir, items requis

**Slot 2 - ITEM (Display)**
- Affichage de l'item concerné
- Lore: Détails complets de la transaction

**Slot 3 - REFUSER (RED_DYE)**
- Clic → Retour au menu de catégorie
- Annule la transaction

---

### Sécurité

- ✅ **Argent retiré à la création** (pas de fraude)
- ✅ **Vérifications doubles** (avant et pendant transaction)
- ✅ **Rollback automatique** en cas d'échec
- ✅ **Prevention auto-vente** (seller != buyer)
- ✅ **Matching exact items** (custom model data inclus)
- ✅ **Logs détaillés** dans la console

---

## 💻 Commandes

### Commandes Joueur

#### `/storage` (Aliases: `/store`, `/st`)
```bash
/storage              # Ouvrir le storage GUI
```

Au sein du GUI, utilisez les boutons pour:
- Changer de mode (VIEW/RECHERCHE/VENTE)
- Créer des ordres (mode RECHERCHE)
- Vendre des items (mode VENTE)

#### `/quantum orders <catégorie>`
```bash
/quantum orders cultures      # Ordres de cultures
/quantum orders minerais      # Ordres de minerais
/quantum orders autre         # Autres ordres
```

---

### Commandes Admin

#### `/qstorage` (Aliases: `/qs`, `/quantumstorage`)

**Transfer (Ajouter au storage):**
```bash
# Pour vous-même
/qstorage transfer hand              # Item dans la main
/qstorage transfer hand 32           # 32 items de la main
/qstorage transfer all               # Tout l'inventaire
/qstorage transfer diamond 64        # 64 diamants (auto-détection)
/qstorage transfer nexo:custom_sword 10     # 10 épées Nexo
/qstorage transfer minecraft:diamond 64     # 64 diamants vanilla

# Pour un joueur (console)
/qstorage transfer minecraft:diamond 64 Notch
/qstorage transfer nexo:ruby 10 Steve
```

**Remove (Retirer du storage):**
```bash
# Pour vous-même
/qstorage remove diamond 32          # 32 diamants
/qstorage remove nexo:custom_sword 5        # 5 épées Nexo
/qstorage remove minecraft:emerald 16       # 16 émeraudes vanilla

# Pour un joueur (console)
/qstorage remove minecraft:diamond 64 Notch
/qstorage remove nexo:custom_item 10 Steve
```

#### Autres Commandes Admin

```bash
/quantum reload              # Recharger la configuration
/storage <player>           # Ouvrir le storage d'un joueur
/menu <menu> [player]       # Ouvrir un menu custom
```

---

## 🔑 Permissions

### Permissions Storage

```yaml
quantum.admin              # Accès admin complet (GUI interactif + commandes)
quantum.storage.use        # Ouvrir /storage (lecture seule) - DEFAULT
quantum.storage.transfer   # Commande /qstorage transfer (admin-only)
quantum.storage.remove     # Commande /qstorage remove (admin-only)
```

### Permissions Ordres

```yaml
quantum.orders.use         # Utiliser le système d'ordres - DEFAULT
quantum.orders.create      # Créer des ordres (mode RECHERCHE) - DEFAULT
quantum.orders.sell        # Vendre aux ordres (mode VENTE) - DEFAULT
quantum.orders.admin       # Supprimer n'importe quel ordre (shift+clic gauche)
```

### Permissions Menus

```yaml
quantum.menu.open          # Ouvrir les menus - DEFAULT
quantum.menu.admin         # Ouvrir menus d'autres joueurs (admin-only)
```

**Résumé:**
- **Joueurs normaux:** `/storage` (view + modes), créer/accepter ordres
- **Admins:** Gestion storage + suppression ordres + menus admin
- **Console:** Accès complet avec ciblage joueur

---

## 📊 PlaceholderAPI

### Installation

1. Installez [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
2. Redémarrez le serveur
3. Quantum détectera automatiquement PlaceholderAPI

### Placeholders Disponibles

#### **Items Nexo**
```
%quantum_amt_nexo-<item_id>%
```

**Exemples:**
```
%quantum_amt_nexo-custom_sword%
%quantum_amt_nexo-ruby%
%quantum_amt_nexo-magic_wand%
```

#### **Items Minecraft**
```
%quantum_amt_minecraft-<material>%
```

**Exemples:**
```
%quantum_amt_minecraft-diamond%
%quantum_amt_minecraft-iron_ingot%
%quantum_amt_minecraft-gold_block%
```

#### **Auto-détection (sans préfixe)**
```
%quantum_amt_custom_sword%    → Cherche Nexo d'abord, puis Minecraft
%quantum_amt_diamond%         → Cherche Nexo d'abord, puis Minecraft
```

### Utilisation dans les Menus

```yaml
items:
  diamond_display:
    material: DIAMOND
    display_name: '&b&lDiamond Storage'
    lore:
      - '&7Amount in storage: &f%quantum_amt_minecraft-diamond%'
      - '&7'
      - '&7Click to create an order'
    slots: [10]
```

**Utilisation dans d'autres plugins:**

- **Scoreboards** (via Scoreboard plugins)
- **Chat** (via Chat plugins)
- **Holograms** (via Hologram plugins)
- **NPCs** (via Citizens/NPCs plugins)

---

## 🎨 Menus Dynamiques

### Features Disponibles

#### Custom Model Data
```yaml
items:
  custom_item:
    slot: 10
    material: DIAMOND_SWORD
    custom_model_data: 1001  # ID du modèle custom
```

#### Effet Glow
```yaml
items:
  glowing_item:
    slot: 11
    material: DIAMOND
    glow: true  # Ajoute l'effet lumineux
```

#### Hide Flags
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
```

**Flags disponibles:**
- `HIDE_ENCHANTS`, `HIDE_ATTRIBUTES`, `HIDE_UNBREAKABLE`
- `HIDE_DESTROYS`, `HIDE_PLACED_ON`, `HIDE_POTION_EFFECTS`, `HIDE_DYE`

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
  speed: 10  # Ticks entre frames
  frames:
    - '&6&l>> &e&lStorage &6&l<<'
    - '&e&l>> &6&lStorage &e&l<<'
```

#### Actions au Clic
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

**Types d'actions:**
- `[message]` - Message au joueur
- `[console]` - Commande console
- `[player]` - Commande joueur
- `[sound]` - Son (format: `SOUND:volume:pitch`)
- `[close]` - Ferme le menu
- `[menu]` - Ouvre un autre menu

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

### Exemple Complet

```yaml
menu_title: '&6&lCustom Shop'
size: 27
open_command: shop

animated_title:
  enabled: true
  speed: 10
  frames:
    - '&6&l>> &e&lShop &6&l<<'
    - '&e&l>> &6&lShop &e&l<<'

items:
  # Item custom avec modèle
  premium_sword:
    slot: 11
    material: DIAMOND_SWORD
    custom_model_data: 1001
    display_name: '&b&lPremium Sword'
    lore:
      - '&7A legendary weapon!'
      - '&7Price: &e1000 coins'
    glow: true
    hide_flags:
      - HIDE_ENCHANTS
      - HIDE_ATTRIBUTES
    click_requirements:
      - 'money >= 1000'
    left_click:
      actions:
        - '[console] eco take %player% 1000'
        - '[console] give %player% diamond_sword{CustomModelData:1001} 1'
        - '[message] &aPurchased Premium Sword!'
        - '[sound] ENTITY_PLAYER_LEVELUP:1.0:1.0'
  
  # Item Nexo
  magic_staff:
    slot: 13
    nexo_item: magic_staff
    display_name: '&5&lMagic Staff'
    lore:
      - '&7Powerful magical weapon'
      - '&7Price: &e5000 coins'
    glow: true
    click_requirements:
      - 'money >= 5000'
    left_click:
      actions:
        - '[console] eco take %player% 5000'
        - '[console] nexo give %player% magic_staff 1'
        - '[message] &aPurchased Magic Staff!'
  
  # Bouton fermer
  close:
    slot: 22
    material: BARRIER
    display_name: '&c&lClose'
    left_click:
      actions:
        - '[close]'
```

---

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

#### Storage API

```java
import com.wynvers.quantum.Quantum;
import com.wynvers.quantum.storage.PlayerStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ExampleStorageAPI {
    
    public void manageStorage(Player player) {
        Quantum quantum = Quantum.getInstance();
        PlayerStorage storage = quantum.getStorageManager().getStorage(player);
        
        // Ajouter des items
        storage.addItem(Material.DIAMOND, 64);
        storage.addNexoItem("custom_sword", 10);
        
        // Vérifier et retirer
        if (storage.hasItem(Material.DIAMOND, 32)) {
            storage.removeItem(Material.DIAMOND, 32);
        }
        
        // Obtenir la quantité
        int diamonds = storage.getItemAmount(Material.DIAMOND);
        player.sendMessage("You have " + diamonds + " diamonds");
        
        // Sauvegarder
        storage.save(quantum);
    }
}
```

#### Orders API

```java
import com.wynvers.quantum.orders.OrderManager;
import com.wynvers.quantum.orders.Order;

public class ExampleOrdersAPI {
    
    public void manageOrders(Player player) {
        Quantum quantum = Quantum.getInstance();
        OrderManager orderManager = quantum.getOrderManager();
        
        // Obtenir tous les ordres d'une catégorie
        List<Order> orders = orderManager.getOrdersByCategory("cultures");
        
        // Créer un ordre programmatiquement
        orderManager.createOrder(
            player,
            "minecraft:diamond",
            64,
            10.0,  // Prix par unité
            "minerais"
        );
        
        // Supprimer un ordre
        orderManager.deleteOrder("cultures", "order_id_1234");
    }
}
```

#### Menu API

```java
import com.wynvers.quantum.menu.Menu;

public class ExampleMenuAPI {
    
    public void openMenu(Player player) {
        Quantum quantum = Quantum.getInstance();
        
        // Ouvrir un menu
        Menu menu = quantum.getMenuManager().getMenu("storage");
        if (menu != null) {
            menu.open(player, quantum);
        }
        
        // Créer un menu programmatiquement
        Menu customMenu = new Menu("custom_menu", "&6My Menu", 27);
        customMenu.open(player, quantum);
    }
}
```

### Events

```java
import com.wynvers.quantum.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuantumListener implements Listener {
    
    @EventHandler
    public void onStorageUpdate(StorageUpdateEvent event) {
        Player player = event.getPlayer();
        PlayerStorage storage = event.getStorage();
        // Faire quelque chose...
    }
    
    @EventHandler
    public void onOrderCreate(OrderCreateEvent event) {
        Player player = event.getPlayer();
        Order order = event.getOrder();
        // Faire quelque chose...
    }
    
    @EventHandler
    public void onOrderTransaction(OrderTransactionEvent event) {
        Player buyer = event.getBuyer();
        Player seller = event.getSeller();
        Order order = event.getOrder();
        // Faire quelque chose...
    }
    
    @EventHandler
    public void onMenuOpen(MenuOpenEvent event) {
        Player player = event.getPlayer();
        Menu menu = event.getMenu();
        // Faire quelque chose...
    }
}
```

---

## 🐛 Support

### Rapporter un Bug

Ouvrez une [issue sur GitHub](https://github.com/kazotaruumc72/Quantum/issues) avec:
- Version de Quantum
- Version du serveur (Spigot/Paper/Purpur)
- Plugins installés (surtout Vault, Nexo, PlaceholderAPI)
- Logs d'erreur complets
- Steps pour reproduire le bug

### Demande de Feature

Utilisez les [GitHub Discussions](https://github.com/kazotaruumc72/Quantum/discussions) pour:
- Proposer de nouvelles features
- Discuter d'améliorations
- Partager vos créations (menus custom, etc.)
- Poser des questions

### Debugging

**Activer les logs détaillés:**

1. Vérifier les logs dans `logs/latest.log`
2. Chercher les lignes avec `[Quantum]` ou `[ORDERS]` ou `[STORAGE]`
3. Les logs incluent:
   - Actions de storage (add/remove)
   - Création d'ordres
   - Transactions
   - Erreurs de matching items

**Logs exemple:**
```
[Quantum] [STORAGE] Added 64x DIAMOND to Notch's storage
[Quantum] [ORDERS] Order created: cultures-1234567890 by Steve
[Quantum] [TRANSACTION] Successful: Buyer=Steve, Seller=Notch, Item=DIAMOND, Qty=64, Price=640.0
```

---

## 📜 Licence

© 2026 Wynvers Studios - Tous droits réservés

Développé par [Kazotaruu_](https://github.com/kazotaruumc72)

---

## 🌟 Roadmap

### ✅ Implémenté
- [x] Storage virtuel illimité
- [x] GUI read-only pour joueurs
- [x] Système d'ordres d'achat/vente
- [x] 3 modes storage (VIEW/RECHERCHE/VENTE)
- [x] Transaction sécurisée avec économie
- [x] Support Nexo et vanilla items
- [x] PlaceholderAPI integration
- [x] Menus dynamiques YAML
- [x] Custom model data support
- [x] Titres animés
- [x] Console commands avec ciblage
- [x] Tab completion intelligent
- [x] Gestion des ordres (suppression)
- [x] Menu de confirmation transactions
- [x] Système de cache optimisé

### 🚧 En Développement
- [ ] Système de pages pour menus ordres
- [ ] Filtres et recherche d'items dans storage
- [ ] Historique des transactions
- [ ] Statistiques de trading

### 📅 Prévu
- [ ] Backup automatique du storage
- [ ] Interface web de gestion
- [ ] Support MythicMobs items
- [ ] Système de taxes sur transactions
- [ ] Market automatique (ordres bot)
- [ ] Notifications in-game pour ordres complétés
- [ ] Multi-monnaie support
- [ ] Intégration Discord (notifications)

---

## 🙏 Remerciements

Merci aux plugins qui ont inspiré Quantum:
- **zMenu** - Pour le système de menus YAML
- **ChestShop** - Pour l'inspiration du système d'échange
- **AuctionHouse** - Pour l'inspiration du système d'ordres

Merci aux technologies utilisées:
- **Spigot/Paper** - API Minecraft
- **Vault** - Économie
- **Nexo** - Items custom
- **PlaceholderAPI** - Placeholders

---

**Merci d'utiliser Quantum !** ⚡

*Pour toute question, contactez-nous sur GitHub ou Discord.*
