# 📝 Order Transaction System - Implementation Guide

## ✅ Files créés

### 1. Menu de confirmation (`order_confirm.yml`)
- Layout: Hopper 5 slots
- Slot 1: Bouton VENDRE (LIME_DYE)
- Slot 2: Item avec détails
- Slot 3: Bouton REFUSER (RED_DYE)
- Placeholders: buyer, quantity, prices, seller stock

### 2. ButtonType.java
Ajout de 3 nouveaux types:
- `QUANTUM_ORDER_CONFIRM_DISPLAY` - Affichage de l'item
- `QUANTUM_CONFIRM_ORDER_SELL` - Bouton VENDRE
- `QUANTUM_CANCEL_ORDER_CONFIRM` - Bouton REFUSER

### 3. OrderTransaction.java
Logique de transaction:
- Vérification: items en stock, argent acheteur
- Transfert: argent (buyer → seller), items (seller storage → buyer inventory)
- Suppression de l'ordre de orders.yml
- Messages de confirmation
- Rollback en cas d'échec

### 4. OrderButtonHandler.java
Gestionnaire de clics:
- `handleOrderClick()` - Ouvre order_confirm avec détails
- `handleConfirmSell()` - Exécute la transaction
- `handleCancelConfirm()` - Retour au menu catégorie
- Cache des données d'ordre par joueur

---

## 🔧 Intégrations nécessaires

### 1. MenuListener.java

**A. Initialiser OrderButtonHandler**
```java
private final OrderButtonHandler orderButtonHandler;

public MenuListener(Quantum plugin) {
    this.plugin = plugin;
    this.orderButtonHandler = new OrderButtonHandler(plugin);
}
```

**B. Détecter les clics sur ordres dans orders_* menus**

Dans `onInventoryClick()`, après la détection du button_type:

```java
// Si le menu est un menu orders_* et l'item a le tag quantum_order_id
if (view.getTitle().contains("Ordres -")) {
    ItemMeta meta = clickedItem.getItemMeta();
    if (meta != null) {
        NamespacedKey orderIdKey = new NamespacedKey(plugin, "quantum_order_id");
        String orderId = meta.getPersistentDataContainer().get(orderIdKey, PersistentDataType.STRING);
        
        if (orderId != null) {
            // Extraire la catégorie depuis le titre du menu
            // "Ordres - Cultures" -> "cultures"
            String title = view.getTitle();
            String category = title.substring(title.indexOf("-") + 2).toLowerCase();
            
            event.setCancelled(true);
            orderButtonHandler.handleOrderClick(player, category, orderId);
            return;
        }
    }
}
```

**C. Gérer les nouveaux button types**

Dans le switch du button_type:

```java
case QUANTUM_CONFIRM_ORDER_SELL:
    event.setCancelled(true);
    orderButtonHandler.handleConfirmSell(player);
    break;
    
case QUANTUM_CANCEL_ORDER_CONFIRM:
    event.setCancelled(true);
    orderButtonHandler.handleCancelConfirm(player);
    break;
```

**D. Nettoyer le cache à la déconnexion**

Dans `onPlayerQuit()` ou créer un listener:
```java
@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    orderButtonHandler.clearCache(event.getPlayer());
}
```

---

### 2. OrderCreationManager.java

**Retirer l'argent lors de la création de l'ordre**

Dans `finalizeOrder()`, après avoir créé l'ordre dans orders.yml:

```java
// Retirer l'argent du joueur
Economy economy = plugin.getVaultManager().getEconomy();
if (economy != null) {
    double totalPrice = session.getTotalPrice();
    
    if (economy.has(player, totalPrice)) {
        economy.withdrawPlayer(player, totalPrice);
        player.sendMessage("§8[§6Quantum§8] §c-" + String.format("%.2f", totalPrice) + "$");
    } else {
        // Ne devrait pas arriver car vérifié avant
        player.sendMessage("§c⚠ Vous n'avez plus assez d'argent!");
        // Supprimer l'ordre créé
        ordersConfig.set(orderPath, null);
        ordersConfig.save(ordersFile);
        return;
    }
}
```

---

### 3. Quantum.java (Main Plugin)

**Ajouter OrderButtonHandler au MenuListener**

Si MenuListener est instancié dans Quantum.java:

```java
// Dans onEnable()
MenuListener menuListener = new MenuListener(this);
getServer().getPluginManager().registerEvents(menuListener, this);
```

---

## 📦 Ordre de déploiement

1. **Compile** le plugin
2. **Reload** le serveur
3. **Test 1** : Créer un ordre via /quantum storage (mode RECHERCHE)
   - Vérifier que l'argent est retiré à la création
4. **Test 2** : Ouvrir /quantum orders cultures
   - Cliquer sur un ordre
   - Vérifier que order_confirm s'ouvre
5. **Test 3** : Dans order_confirm
   - Cliquer sur VENDRE
   - Vérifier transaction (argent + items)
   - Vérifier suppression de l'ordre
6. **Test 4** : Cliquer sur REFUSER
   - Vérifier retour au menu catégorie

---

## 💡 Notes techniques

### PDC Tags utilisés
- `quantum_order_id` - ID unique de l'ordre dans orders.yml
- `quantum_item_id` - ID de l'item (nexo:xxx ou minecraft:xxx)

### Flow complet
1. **Création d'ordre**:
   - Joueur A clique sur item en mode RECHERCHE
   - Configure quantité + prix
   - **ARGENT RETIRÉ ICI**
   - Ordre sauvegardé dans orders.yml

2. **Acceptation d'ordre**:
   - Joueur B ouvre /quantum orders [catégorie]
   - Clique sur ordre
   - order_confirm s'ouvre
   - Clique VENDRE
   - Transaction exécutée:
     * Argent: A → B
     * Items: B storage → A inventory
   - Ordre supprimé
   - Notifications envoyées

### Sécurité
- Vérifications double (avant ordre_confirm ET avant transaction)
- Transactions atomiques (all or nothing)
- Rollback automatique en cas d'échec
- Prevention auto-vente (seller != buyer)

### Gestion offline
Si l'acheteur est offline lors de la transaction:
- Transaction exécutée quand même
- Items doivent être stockés temporairement
- **TODO**: Implémenter système de récupération

---

## ✅ Checklist d'intégration

- [ ] MenuListener.java : OrderButtonHandler initialisé
- [ ] MenuListener.java : Détection clics orders_* avec quantum_order_id
- [ ] MenuListener.java : Switch cases QUANTUM_CONFIRM_ORDER_SELL / QUANTUM_CANCEL_ORDER_CONFIRM
- [ ] MenuListener.java : clearCache() dans PlayerQuitEvent
- [ ] OrderCreationManager.java : Retrait argent dans finalizeOrder()
- [ ] Compilation réussie
- [ ] Tests fonctionnels passés
- [ ] Messages de confirmation corrects
- [ ] Transactions sécurisées
