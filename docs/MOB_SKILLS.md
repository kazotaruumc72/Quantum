# Mob Skills System - Guide d'utilisation

Système d'affichage de titles/subtitles quand un mob utilise un skill.

## Configuration

Les skills sont configurés dans `mob_skills.yml` :

```yaml
enabled: true
display_radius: 20  # Distance en blocs

title_timing:
  fade_in: 10
  stay: 40
  fade_out: 10

skills:
  summon:
    title: "&d&l✦ INVOCATION"
    subtitle: "&7Des alliés apparaissent!"
  
  healing:
    title: "&a&l+ RÉGÉNÉRATION"
    subtitle: "&7Le mob se soigne!"
  # ... etc
```

## Utilisation dans le code

### 1. Récupérer le manager

```java
MobSkillManager skillManager = plugin.getMobSkillManager();
```

### 2. Afficher un skill à un joueur spécifique

```java
// Quand un mob utilise "fireball" sur un joueur
skillManager.showSkill(player, "fire");
```

### 3. Afficher à tous les joueurs proches d'un mob

```java
// Quand un mob (Entity) utilise un skill
Entity mob = ...; // ton mob
skillManager.showSkillToNearby(mob, "healing");

// Ou avec une Location
Location mobLocation = mob.getLocation();
skillManager.showSkillToNearby(mobLocation, "thunder");
```

### 4. Afficher à tous les joueurs dans une tour

```java
// Pour un skill global dans une tour
skillManager.showSkillToTower("tower_1", "explosion");
```

## Exemples d'intégration

### Dans TowerSpawnerManager (spawners de mobs)

Quand un mob spawn avec des skills définis dans `towers.yml`, tu peux déclencher l'affichage du skill comme ça :

```java
// Exemple : mob utilise le skill "healing" toutes les 30 secondes
public void triggerMobSkill(Entity mob, String skillId) {
    MobSkillManager skillManager = plugin.getMobSkillManager();
    
    // Afficher le title à tous les joueurs proches
    skillManager.showSkillToNearby(mob, skillId);
    
    // Ensuite exécuter l'effet du skill (soins, dégâts, etc.)
    executeSkillEffect(mob, skillId);
}
```

### Dans un Listener de combat

```java
@EventHandler
public void onMobUseSkill(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof LivingEntity) {
        LivingEntity mob = (LivingEntity) event.getDamager();
        
        // Vérifier si le mob a un skill "fire" à déclencher
        if (shouldTriggerFireSkill(mob)) {
            // Afficher le title
            plugin.getMobSkillManager().showSkillToNearby(mob, "fire");
            
            // Appliquer l'effet de feu
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                player.setFireTicks(100); // 5 secondes
            }
        }
    }
}
```

### Dans un système de skill cyclique (BukkitRunnable)

```java
public void startSkillCycle(Entity mob, String skillId, int intervalTicks) {
    new BukkitRunnable() {
        @Override
        public void run() {
            if (!mob.isValid() || mob.isDead()) {
                cancel();
                return;
            }
            
            // Afficher le skill
            plugin.getMobSkillManager().showSkillToNearby(mob, skillId);
            
            // Exécuter l'effet du skill
            applySkillEffect(mob, skillId);
        }
    }.runTaskTimer(plugin, 0L, intervalTicks);
}
```

## Skills disponibles par défaut

| Skill ID      | Titre                     | Description                          |
|---------------|---------------------------|--------------------------------------|
| `summon`      | ✦ INVOCATION             | Invoque d'autres mobs                |
| `healing`     | + RÉGÉNÉRATION          | Le mob se soigne                     |
| `fire`        | 🔥 FEU                  | Met le joueur en feu                 |
| `fireprison`  | 🔥 PRISON DE FLAMMES   | Piège de feu autour du joueur       |
| `iceberg`     | ❄ ICEBERG               | Pic de glace surgit                  |
| `ice`         | ❄ GEL                   | Gèle le joueur                       |
| `thunder`     | ⚡ ÉCLAIR               | Foudre sur le joueur                 |
| `poisonous`   | ☠ POISON                | Empoisonne le joueur                 |
| `wither`      | ☠ WITHER                | Effet wither                         |
| `explosion`   | 💥 EXPLOSION           | Explosion autour du mob              |
| `charge`      | ➡ CHARGE                | Le mob charge                        |
| `teleport`    | ✨ TÉLÉPORTATION       | Le mob se téléporte               |
| `shield`      | 🛡 BOUCLIER            | Le mob se protège                   |
| `rage`        | 🔥 RAGE                 | Le mob entre en furie                |

## Ajouter des skills personnalisés

Dans `mob_skills.yml`, ajoute simplement :

```yaml
skills:
  mon_skill_custom:
    title: "&b&lMON SKILL"
    subtitle: "&7Description du skill"
```

Puis dans ton code :

```java
skillManager.showSkill(player, "mon_skill_custom");
```

## Vérifier si un skill existe

```java
if (skillManager.hasSkill("fireball")) {
    skillManager.showSkill(player, "fireball");
}
```

## Recharger la configuration

```java
skillManager.reload();
```

Ou via la commande :
```
/quantum reload
```

## Notes importantes

- Les skills n'exécutent **pas automatiquement** d'effets, ils affichent seulement le title/subtitle
- Tu dois implémenter la logique d'exécution du skill toi-même (dégâts, effets, spawns, etc.)
- Le `display_radius` détermine la portée d'affichage du title (en blocs)
- Les codes couleur Minecraft (`&c`, `&l`, etc.) sont supportés
- Les emojis Unicode sont supportés (🔥, ❄, ⚡, etc.)
