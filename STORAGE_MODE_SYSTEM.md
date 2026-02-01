# 🔄 Système de Changement de Mode Storage/Sell

## ✅ Statut : **ENTIÈREMENT FONCTIONNEL**

Tous les composants sont déjà implémentés et connectés. Aucune modification nécessaire !

---

## 📦 Composants

### 1. **StorageMode.java**
Gère les modes du storage (STORAGE / SELL)

```java
// Modes disponibles
enum Mode {
    STORAGE("&aStorage"),
    SELL("&eVente")
}

// Méthodes principales
StorageMode.setMode(player, Mode.STORAGE)  // Définir un mode
StorageMode.getMode(player)                 // Récupérer le mode actuel
StorageMode.toggleMode(player)              // Basculer entre modes
StorageMode.getModeDisplay(player)          // Obtenir le nom formaté
```

**Emplacement:** `src/main/java/com/wynvers/quantum/storage/StorageMode.java`

---

### 2. **PlaceholderManager.java**
Parse les placeholders internes de Quantum

```java
// Parse automatiquement %mode% dans les textes
parse(player, "&7Mode: %mode%")  
// → "&7Mode: &aStorage" ou "&7Mode: &eVente"
```

**Fonctionnalités:**
- Remplace `%mode%` par le mode actuel du joueur
- Applique les codes couleur (`&a`, `&e`)
- Compatible avec PlaceholderAPI

**Emplacement:** `src/main/java/com/wynvers/quantum/managers/PlaceholderManager.java`

---

### 3. **Menu.java - méthode open()**
Parse le titre du menu avec les placeholders

```java
public void open(Player player, Quantum plugin) {
    // Parse le titre avec placeholders (dont %mode%)
    String parsedTitle = plugin.getPlaceholderManager().parse(player, title);
    
    Inventory inventory = Bukkit.createInventory(null, size, parsedTitle);
    populateInventory(inventory, player);
    player.openInventory(inventory);
}
```

**Emplacement:** `src/main/java/com/wynvers/quantum/menu/Menu.java` (ligne ~163)

---

### 4. **MenuItem.java - méthode executeActions()**
Gère le changement de mode lors du clic

```java
public void executeActions(Player player, Quantum plugin, ClickType clickType) {
    // Détection du bouton QUANTUM_CHANGE_MODE
    if (buttonType == ButtonType.QUANTUM_CHANGE_MODE) {
        if (targetMode != null) {
            // Définir le mode spécifique (STORAGE ou SELL)
            StorageMode.Mode mode = StorageMode.Mode.valueOf(targetMode.toUpperCase());
            StorageMode.setMode(player, mode);
            player.sendMessage("§aMode changé en: §e" + mode.getDisplayName());
        } else {
            // Toggle si pas de mode spécifié
            StorageMode.toggleMode(player);
            player.sendMessage("§aMode changé en: §e" + StorageMode.getModeDisplay(player));
        }
        
        // Rafraîchir le menu
        player.closeInventory();
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Menu storageMenu = plugin.getMenuManager().getMenu("storage");
            if (storageMenu != null) {
                storageMenu.open(player, plugin);
            }
        }, 2L);
        
        return;
    }
    
    // ... reste des actions
}
```

**Emplacement:** `src/main/java/com/wynvers/quantum/menu/MenuItem.java` (ligne ~239)

---

## 🎯 Configuration storage.yml

Le fichier est déjà correctement configuré :

```yaml
menu_title: '&8» &fMode: %mode%'  # %mode% sera remplacé dynamiquement

items:
  mode_storage:
    slots: [0]
    material: LIME_WOOL
    display_name: '&aMode: Storage'
    button_type: QUANTUM_CHANGE_MODE
    target_mode: STORAGE  # Fixe le mode à STORAGE
    left_click_actions:
      - '[close]'

  mode_sell:
    slots: [8]
    material: GOLD_BLOCK
    display_name: '&eMode: Vente'
    button_type: QUANTUM_CHANGE_MODE
    target_mode: SELL  # Fixe le mode à SELL
    left_click_actions:
      - '[close]'
```

---

## 🔄 Flux d'Exécution

```
1. Joueur exécute /storage
   ↓
2. Menu.open() est appelé
   ↓
3. PlaceholderManager.parse() remplace %mode% dans le titre
   ↓
4. Menu s'affiche avec "Mode: Storage" ou "Mode: Vente"
   ↓
5. Joueur clique sur bouton STORAGE ou SELL
   ↓
6. MenuItem.executeActions() détecte QUANTUM_CHANGE_MODE
   ↓
7. StorageMode.setMode() change le mode du joueur
   ↓
8. Menu se ferme puis se réouvre (2 ticks plus tard)
   ↓
9. Le titre est re-parsé avec le nouveau mode
   ↓
10. Joueur voit le titre mis à jour !
```

---

## 🧪 Tests

### Test 1: Vérifier le placeholder
```java
String title = "&7Mode: %mode%";
String parsed = plugin.getPlaceholderManager().parse(player, title);
// Devrait afficher: "&7Mode: &aStorage" (par défaut)
```

### Test 2: Changer le mode
```java
StorageMode.setMode(player, StorageMode.Mode.SELL);
String display = StorageMode.getModeDisplay(player);
// Devrait retourner: "&eVente"
```

### Test 3: Toggle
```java
StorageMode.toggleMode(player); // STORAGE → SELL
StorageMode.toggleMode(player); // SELL → STORAGE
```

---

## ⚙️ Points Techniques

### Pourquoi fermer/réouvrir le menu ?

Bukkit ne supporte pas la modification dynamique du titre d'un inventaire. La seule solution est de :
1. Fermer l'inventaire
2. Recréer un nouvel inventaire avec le nouveau titre
3. Réouvrir

Le délai de 2 ticks (`runTaskLater(..., 2L)`) permet :
- D'éviter les conflits de fermeture
- De donner le temps au serveur de traiter l'événement
- D'assurer une transition fluide

### Stockage en mémoire

Les modes sont stockés dans une `HashMap<UUID, Mode>` en mémoire :
- **Avantage:** Rapide, pas d'I/O
- **Inconvénient:** Perdu au redémarrage
- **Solution future:** Sauvegarder dans la base de données si nécessaire

---

## 🐛 Dépannage

### Le titre ne change pas
✅ Vérifier que PlaceholderManager est initialisé dans `Quantum.onEnable()`
✅ Vérifier les logs pour erreurs de parsing
✅ Tester manuellement: `/quantum debug mode <player>`

### Le bouton ne fonctionne pas
✅ Vérifier que `button_type: QUANTUM_CHANGE_MODE` est bien défini
✅ Vérifier que `target_mode: STORAGE` ou `SELL` est présent
✅ Vérifier les logs pour exceptions

### Le menu ne se rafraîchit pas
✅ Vérifier que MenuManager.getMenu("storage") retourne bien le menu
✅ Augmenter le délai si nécessaire (2L → 5L)
✅ Vérifier qu'il n'y a pas d'autres plugins qui interfèrent

---

## 🎨 Personnalisation

### Ajouter un mode custom
```java
// Dans StorageMode.java
enum Mode {
    STORAGE("&aStorage"),
    SELL("&eVente"),
    CRAFT("&bCraft")  // Nouveau mode
}
```

### Changer les couleurs
```yaml
# Dans storage.yml
menu_title: '&8» &fMode: %mode% &8«'  # Ajouter des décorations
```

### Ajouter une animation
```yaml
animated_title: true
title_frames:
  - '&8» &fMode: %mode%'
  - '&8» &7Mode: %mode%'
  - '&8» &fMode: %mode%'
title_speed: 10
```

---

## 📝 Notes

- Le système est **complètement fonctionnel** en l'état
- Tous les composants sont **déjà implémentés**
- La configuration storage.yml est **correcte**
- Aucune modification nécessaire pour le faire fonctionner

**Auteur:** Wynvers Studios  
**Date:** Février 2026  
**Version:** 1.0.0
