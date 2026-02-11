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
- **✨ NEW: Filtres et recherche** - Recherchez et filtrez vos items par nom, type, quantité
- **✨ NEW: Pagination** - Navigation fluide entre plusieurs pages de storage

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
- **✨ NEW: Pagination des ordres** - Navigation entre plusieurs pages (21 ordres par page)
- **✨ NEW: Historique des transactions** - Consultation complète de toutes vos transactions
- **✨ NEW: Statistiques de trading** - Analysez vos performances commerciales

### 📊 Système d'Historique et Statistiques

- **📝 Historique des transactions**:
  - Enregistrement automatique de toutes les transactions
  - Filtrage par type (achats/ventes)
  - Affichage détaillé (acheteur, vendeur, item, quantité, prix, date)
  - Consultation illimitée dans le temps

- **📊 Statistiques de trading**:
  - Statistiques globales (total achats/ventes, profit net)
  - Statistiques par période (aujourd'hui, semaine, mois)
  - Top items les plus échangés
  - Top partenaires commerciaux
  - Prix moyens d'achat/vente par item

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

### 💼 Système de Jobs (Métiers)

- **✨ NEW: Preview des Actions** - Aperçu des récompenses avant interaction (inspiré de UniverseJobs)
- **✨ NEW: Affichage amélioré** - Récompenses avec icônes et couleurs
- **Progression de métiers** - Système de niveaux et XP
- **Récompenses automatiques** - Items, argent, boosters par niveau
- **Interaction avec structures** - Tapez des structures pour gagner XP
- **Boosters** - Multiplicateurs d'XP et d'argent
- **Preview détaillée** - Commande `/job rewards preview` avec barre de progression
- **Action Bar** - Preview instantanée sur clic droit
- **Support dungeon** - Boosters spécifiques aux donjons

📚 **Documentation complète**: 
- [JOBS_SYSTEM.md](JOBS_SYSTEM.md) - Documentation complète
- [JOBS_PREVIEW_SYSTEM.md](JOBS_PREVIEW_SYSTEM.md) - Guide du système de preview

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
├── transactions.yml        # ✨ NEW: Historique des transactions
├── menus/                  # Dossier des menus
│   ├── storage.yml         # Menu du storage (3 modes)
│   ├── orders_cultures.yml # Menu ordres cultures
│   ├── orders_minerais.yml # Menu ordres minerais
│   ├── orders_autre.yml    # Menu ordres autres
│   ├── order_confirm.yml   # Menu confirmation transaction
│   ├── history.yml         # ✨ NEW: Menu historique
│   └── statistics.yml      # ✨ NEW: Menu statistiques
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
- ✅ **NEW: Rechercher des items** par nom
- ✅ **NEW: Filtrer** par type (Nexo/Minecraft)
- ✅ **NEW: Trier** par quantité ou ordre alphabétique
- ✅ **NEW: Naviguer** entre plusieurs pages

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
- **NEW: Recherche et filtres** disponibles
- **NEW: Pagination automatique** (28 items par page)

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

### ✨ Nouvelles Fonctionnalités de Filtrage

#### **Recherche d'Items**

1. Ouvrir `/storage`
2. Cliquer sur le bouton **RECHERCHE** (🔍)
3. Taper le nom de l'item dans le chat
4. Les résultats s'affichent instantanément

**Exemple:**
```
Recherche: "diamond"
Résultats: Diamond, Diamond Sword, Diamond Pickaxe, etc.
```

#### **Filtres par Type**

Bouton **FILTRE TYPE**:
- **Tous** - Afficher tous les items
- **Nexo uniquement** - Items custom Nexo seulement
- **Minecraft uniquement** - Items vanilla seulement

#### **Modes de Tri**

Bouton **TRI**:
- **Récent** - Ordre d'ajout (par défaut)
- **Quantité (↓)** - Plus grande quantité d'abord
- **Quantité (↑)** - Plus petite quantité d'abord
- **Alphabétique** - Ordre A-Z

#### **Réinitialiser**

Bouton **RÉINITIALISER** - Efface tous les filtres actifs

---

## 📦 Système d'Ordres

### Vue d'ensemble

Le système d'ordres permet aux joueurs d'acheter et vendre des items entre eux avec une économie intégrée.

### ✨ Pagination des Ordres

Lorsqu'il y a plus de 21 ordres dans une catégorie:

- **Navigation automatique** - Boutons Précédent/Suivant
- **21 ordres par page** - 3 rangées de 7 items
- **Indicateur de page** - "Page X/Y" en temps réel
- **Tri automatique** - Par date (plus récents en premier)

**Boutons de navigation:**
- **◀ Précédent** - Page précédente (slot 48)
- **Suivant ▶** - Page suivante (slot 50)

---

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
8. **Transaction enregistrée dans l'historique**

**Exemple:**
```
Joueur A cherche: 64 Diamonds à 10$/unité
Coût total: 640$
→ 640$ retirés immédiatement
→ Ordre créé dans "Ordres - Minerais"
→ Transaction enregistrée
```

---

#### **2. Acceptation d'un Ordre (Vendeur)**

```
/quantum orders <catégorie> → Clic sur ordre → VENDRE
```

**Via menu orders:**
1. Ouvrir `/quantum orders cultures` (ou minerais/autre)
2. **Naviguer entre les pages** si nécessaire
3. Voir tous les ordres disponibles
4. Cliquer sur un ordre
5. Menu de confirmation s'ouvre
6. Vérifier les détails (quantité, prix, votre stock)
7. Cliquer sur **VENDRE** (lime dye)
8. **Transaction enregistrée automatiquement**

**Via mode VENTE:**
1. Ouvrir `/storage`
2. Cliquer sur bouton **MODE VENTE**
3. Shift-clic sur item dans votre inventaire
4. Choisir l'ordre parmi les disponibles
5. Transaction automatique
6. **Transaction enregistrée automatiquement**

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
   - 📝 **Transaction enregistrée dans transactions.yml**
   - 📊 **Statistiques mises à jour**

**Exemple de transaction:**
```
[AVANT]
Acheteur A: 640$ retirés (lors création ordre)
Vendeur B: 64 Diamonds en storage

[TRANSACTION]
→ 640$ transférés à Vendeur B
→ 64 Diamonds retirés du storage de B
→ 64 Diamonds ajoutés à l'inventaire de A
→ Transaction enregistrée: ID, date, prix, items

[📦 APRÈS]
Acheteur A: Reçoit 64 Diamonds
Vendeur B: Reçoit 640$
Ordre: Supprimé
Historique: Transaction #1234 enregistrée
```

---

## 📝 Historique des Transactions

### Accéder à l'Historique

```bash
/quantum history              # Ouvrir l'historique complet
/quantum history buy          # Filtrer: achats uniquement
/quantum history sell         # Filtrer: ventes uniquement
```

### Informations Affichées

Chaque transaction affiche:
- **Date et heure** - Timestamp précis
- **Type** - Achat ou Vente
- **Partenaire** - Nom de l'autre joueur
- **Item** - Nom et quantité
- **Prix unitaire** - Prix par item
- **Prix total** - Coût total de la transaction
- **Rôle** - Votre rôle (acheteur/vendeur)

### Navigation

- **Pagination automatique** - 21 transactions par page
- **Filtres disponibles**:
  - **TOUT** - Toutes les transactions
  - **ACHATS** - Vos achats uniquement
  - **VENTES** - Vos ventes uniquement
- **Tri chronologique** - Plus récentes en premier

### Exemple d'Affichage

```
────────────────────────
✨ HISTORIQUE DES TRANSACTIONS
────────────────────────

📘 #1 - ACHAT
  Date: 2026-02-03 10:30:15
  Vendeur: Steve
  Item: 64x Diamond
  Prix: 10.00$/u (640.00$ total)
  
📗 #2 - VENTE
  Date: 2026-02-03 09:15:42
  Acheteur: Notch
  Item: 32x Iron Ingot
  Prix: 2.50$/u (80.00$ total)
  
Page 1/3 - Total: 52 transactions
```

---

## 📊 Statistiques de Trading

### Accéder aux Statistiques

```bash
/quantum stats                # Statistiques globales
/quantum stats today          # Statistiques du jour
/quantum stats week           # Statistiques de la semaine
/quantum stats month          # Statistiques du mois
```

### Statistiques Disponibles

#### **🌐 Statistiques Globales**

```
────────────────────────
📊 STATISTIQUES DE TRADING
────────────────────────

💰 GLOBAL:
  Achats: 15,640.00$ (124 transactions)
  Ventes: 28,920.00$ (186 transactions)
  Profit net: +13,280.00$
  
🗓️ AUJOURD'HUI:
  Transactions: 8
  Profit net: +420.00$
  
🏆 TOP ITEMS:
  1. Diamond (2,048x)
  2. Iron Ingot (1,536x)
  3. Gold Ingot (892x)
  
🤝 TOP PARTENAIRES:
  1. Steve (45 transactions)
  2. Notch (32 transactions)
  3. Herobrine (28 transactions)
```

#### **📈 Statistiques Par Période**

- **Aujourd'hui** - Transactions du jour
- **Cette Semaine** - 7 derniers jours
- **Ce Mois** - 30 derniers jours

Chaque période affiche:
- Nombre de transactions
- Total acheté
- Total vendu
- Profit net

#### **👑 Classements**

**Items les plus échangés:**
- Top 10 items par quantité totale
- Détail achats vs ventes

**Partenaires commerciaux:**
- Top 10 joueurs par nombre de transactions
- Volume total échangé

**Prix moyens:**
- Prix moyen d'achat par item
- Prix moyen de vente par item
- Marge bénéficiaire

### Menu Statistiques (GUI)

Menu interactif avec:
- **Vue d'ensemble** - Résumé global
- **Périodes** - Boutons pour changer de période
- **Détails** - Items cliquables pour détails
- **Graphiques** - Visualisation des tendances

---

## 💻 Commandes

### Commandes Joueur

#### `/storage` (Aliases: `/store`, `/st`)
```bash
/storage              # Ouvrir le storage GUI
```

Au sein du GUI, utilisez les boutons pour:
- Changer de mode (VIEW/RECHERCHE/VENTE)
- **NEW: Rechercher** des items
- **NEW: Filtrer** par type
- **NEW: Trier** les items
- Créer des ordres (mode RECHERCHE)
- Vendre des items (mode VENTE)

#### `/quantum orders <catégorie>`
```bash
/quantum orders cultures      # Ordres de cultures
/quantum orders minerais      # Ordres de minerais
/quantum orders autre         # Autres ordres
```

#### **✨ NEW: Commandes Historique & Statistiques**

```bash
# Historique
/quantum history              # Historique complet
/quantum history buy          # Achats uniquement
/quantum history sell         # Ventes uniquement

# Statistiques
/quantum stats                # Statistiques globales
/quantum stats today          # Stats du jour
/quantum stats week           # Stats de la semaine
/quantum stats month          # Stats du mois
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
/quantum history <player>   # Voir l'historique d'un joueur
/quantum stats <player>     # Voir les stats d'un joueur
```

---

## 🔑 Permissions

### Permissions Storage

```yaml
quantum.admin              # Accès admin complet (GUI interactif + commandes)
quantum.storage.use        # Ouvrir /storage (lecture seule) - DEFAULT
quantum.storage.transfer   # Commande /qstorage transfer (admin-only)
quantum.storage.remove     # Commande /qstorage remove (admin-only)
quantum.storage.filter     # Utiliser les filtres de storage - DEFAULT
```

### Permissions Ordres

```yaml
quantum.orders.use         # Utiliser le système d'ordres - DEFAULT
quantum.orders.create      # Créer des ordres (mode RECHERCHE) - DEFAULT
quantum.orders.sell        # Vendre aux ordres (mode VENTE) - DEFAULT
quantum.orders.admin       # Supprimer n'importe quel ordre (shift+clic gauche)
```

### **✨ NEW: Permissions Historique & Statistiques**

```yaml
quantum.history.view       # Consulter son historique - DEFAULT
quantum.history.others     # Voir l'historique des autres (admin)
quantum.stats.view         # Consulter ses statistiques - DEFAULT
quantum.stats.others       # Voir les stats des autres (admin)
```

### Permissions Menus

```yaml
quantum.menu.open          # Ouvrir les menus - DEFAULT
quantum.menu.admin         # Ouvrir menus d'autres joueurs (admin-only)
```

**Résumé:**
- **Joueurs normaux:** `/storage` (view + modes + filtres), créer/accepter ordres, historique, stats
- **Admins:** Gestion storage + suppression ordres + menus admin + historique/stats autres
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

#### **✨ NEW: Placeholders Statistiques**

```
# Statistiques globales
%quantum_stats_total_buy%          # Total acheté
%quantum_stats_total_sell%         # Total vendu
%quantum_stats_net_profit%         # Profit net
%quantum_stats_transaction_count%  # Nombre de transactions

# Statistiques période
%quantum_stats_today_profit%       # Profit du jour
%quantum_stats_week_profit%        # Profit de la semaine
%quantum_stats_month_profit%       # Profit du mois

# Items
%quantum_stats_most_sold_item%     # Item le plus vendu
%quantum_stats_most_bought_item%   # Item le plus acheté
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
      - '&7Total sold: &a%quantum_stats_total_sell%$'
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
import com.wynvers.quantum.storage.StorageFilterHandler;
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
        
        // NEW: Utiliser les filtres
        StorageFilterHandler filterHandler = quantum.getStorageFilterHandler();
        filterHandler.setSearchQuery(player, "diamond");
        List<StorageFilterHandler.StorageEntry> filtered = filterHandler.applyFilters(storage, player);
        
        // Sauvegarder
        storage.save(quantum);
    }
}
```

#### **✨ NEW: Historique & Statistiques API**

```java
import com.wynvers.quantum.transactions.TransactionHistoryManager;
import com.wynvers.quantum.statistics.TradingStatisticsManager;

public class ExampleHistoryStatsAPI {
    
    public void useHistoryAndStats(Player player) {
        Quantum quantum = Quantum.getInstance();
        
        // Historique
        TransactionHistoryManager historyManager = quantum.getTransactionHistoryManager();
        
        // Obtenir les transactions
        List<Transaction> allTransactions = historyManager.getPlayerHistory(player, null, 0);
        List<Transaction> buyTransactions = historyManager.getPlayerHistory(player, "BUY", 10);
        
        // Statistiques basiques
        double totalBuy = historyManager.getTotalBuyAmount(player);
        double totalSell = historyManager.getTotalSellAmount(player);
        double netProfit = historyManager.getNetProfit(player);
        
        // Statistiques avancées
        TradingStatisticsManager statsManager = quantum.getTradingStatisticsManager();
        
        // Stats globales
        PlayerStatistics globalStats = statsManager.getGlobalStatistics(player);
        player.sendMessage("Net profit: " + globalStats.netProfit + "$");
        
        // Stats par période
        PlayerStatistics todayStats = statsManager.getPeriodStatistics(player, TimePeriod.TODAY);
        PlayerStatistics weekStats = statsManager.getPeriodStatistics(player, TimePeriod.WEEK);
        
        // Top items
        List<ItemStatistic> topItems = statsManager.getMostTradedItems(player, 5);
        for (ItemStatistic item : topItems) {
            player.sendMessage(item.getFormattedName() + ": " + item.totalQuantity + "x");
        }
        
        // Top partenaires
        List<PartnerStatistic> topPartners = statsManager.getTopTradingPartners(player, 5);
        
        // Générer un résumé
        List<String> summary = statsManager.generateStatisticsSummary(player);
        summary.forEach(player::sendMessage);
    }
}
```

#### Orders API

```java
import com.wynvers.quantum.orders.OrderManager;
import com.wynvers.quantum.orders.Order;
import com.wynvers.quantum.orders.OrderPaginationHandler;

public class ExampleOrdersAPI {
    
    public void manageOrders(Player player) {
        Quantum quantum = Quantum.getInstance();
        OrderManager orderManager = quantum.getOrderManager();
        
        // Obtenir tous les ordres d'une catégorie
        List<Order> orders = orderManager.getOrdersByCategory("cultures");
        
        // NEW: Utiliser la pagination
        OrderPaginationHandler paginationHandler = quantum.getOrderPaginationHandler();
        int currentPage = paginationHandler.getCurrentPage(player, "cultures");
        int totalPages = paginationHandler.getTotalPages("cultures");
        List<String> ordersForPage = paginationHandler.getOrdersForPage("cultures", currentPage);
        
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
    
    // NEW: Events historique
    @EventHandler
    public void onTransactionRecord(TransactionRecordEvent event) {
        Transaction transaction = event.getTransaction();
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
2. Chercher les lignes avec `[Quantum]` ou `[ORDERS]` ou `[STORAGE]` ou `[TRANSACTIONS]`
3. Les logs incluent:
   - Actions de storage (add/remove)
   - Création d'ordres
   - Transactions
   - Erreurs de matching items
   - Enregistrements d'historique

**Logs exemple:**
```
[Quantum] [STORAGE] Added 64x DIAMOND to Notch's storage
[Quantum] [ORDERS] Order created: cultures-1234567890 by Steve
[Quantum] [TRANSACTION] Successful: Buyer=Steve, Seller=Notch, Item=DIAMOND, Qty=64, Price=640.0
[Quantum] [TRANSACTIONS] Recorded transaction #1234
```

---

## 📋 Licence

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
- [x] **✨ Système de pages pour menus ordres**
- [x] **✨ Filtres et recherche d'items dans storage**
- [x] **✨ Historique des transactions**
- [x] **✨ Statistiques de trading**

### 📅 Prévu
- [ ] Backup automatique du storage
- [ ] Interface web de gestion
- [ ] Support MythicMobs items
- [ ] Système de taxes sur transactions
- [ ] Market automatique (ordres bot)
- [ ] Notifications in-game pour ordres complétés
- [ ] Multi-monnaie support
- [ ] Intégration Discord (notifications)
- [ ] Graphiques de tendances (prix historiques)
- [ ] Système d'alertes (prix cibles)

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
