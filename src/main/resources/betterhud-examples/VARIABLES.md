# ================================================================
# Variables BetterHud - Référence Complète pour Quantum
# ================================================================
# Ce fichier liste toutes les variables disponibles pour les
# configurations BetterHud dans le contexte du plugin Quantum
# ================================================================

# VARIABLES JOUEUR (Natives Minecraft)
# =====================================

${player_name}              # Nom du joueur
${player_display_name}      # Nom d'affichage (avec couleurs/préfixes)
${player_uuid}              # UUID du joueur

# Stats de base
${health}                   # Vie actuelle (ex: 15.5)
${max_health}              # Vie maximale (ex: 20.0)
${health_percentage}       # Vie en pourcentage (ex: 75)
${food_level}              # Niveau de nourriture (0-20)
${saturation}              # Saturation (nourriture cachée)
${air}                     # Air disponible (sous l'eau)
${max_air}                 # Air maximum
${level}                   # Niveau d'expérience
${exp}                     # Points d'expérience actuels
${exp_to_next}            # EXP nécessaire pour level suivant

# Position et monde
${x}                       # Coordonnée X (arrondie)
${y}                       # Coordonnée Y
${z}                       # Coordonnée Z
${exact_x}                 # Coordonnée X précise (décimales)
${exact_y}                 # Coordonnée Y précise
${exact_z}                 # Coordonnée Z précise
${world_name}              # Nom du monde actuel
${biome}                   # Biome actuel
${light_level}             # Niveau de lumière (0-15)

# Direction et orientation
${yaw}                     # Angle horizontal (0-360)
${pitch}                   # Angle vertical (-90 à 90)
${direction}               # Direction cardinale (N, S, E, W, NE, etc.)
${cardinal}                # Direction simple (Nord, Sud, Est, Ouest)

# Gamemode et état
${gamemode}                # Mode de jeu (SURVIVAL, CREATIVE, etc.)
${is_flying}               # true/false si le joueur vole
${is_sneaking}            # true/false si le joueur est accroupi
${is_sprinting}           # true/false si le joueur court
${is_blocking}            # true/false si bloque avec bouclier
${is_gliding}             # true/false si vol avec élytres
${is_sleeping}            # true/false si dort
${is_swimming}            # true/false si nage

# Combat
${armor_value}             # Points d'armure (0-20)
${armor_toughness}        # Solidité d'armure
${attack_damage}          # Dégâts d'attaque
${attack_speed}           # Vitesse d'attaque


# VARIABLES ÉCONOMIE (via Vault)
# ================================

${money}                   # Argent du joueur (formaté)
${money_raw}              # Argent brut (nombre)
${money_formatted}        # Argent formaté avec symbole ($)
${balance}                # Alias de ${money}
${eco_balance}            # Balance économique


# VARIABLES QUANTUM - SYSTÈME DE STOCKAGE
# =========================================

${storage_items}           # Nombre d'items dans le stockage
${storage_capacity}        # Capacité maximale du stockage
${storage_used_slots}     # Slots utilisés
${storage_free_slots}     # Slots libres
${storage_percentage}      # Pourcentage d'utilisation
${storage_status}          # Statut (ex: "Presque plein", "Disponible")

# Dernières actions
${storage_last_item}       # Dernier item ajouté/retiré
${storage_last_amount}     # Quantité du dernier item
${storage_last_action}     # Dernière action (ADD/REMOVE)


# VARIABLES QUANTUM - SYSTÈME DE MÉTIERS
# ========================================

${job_name}                # Nom du métier actuel
${job_level}               # Niveau du métier
${job_exp}                 # EXP du métier actuel
${job_exp_next}           # EXP pour level suivant
${job_exp_total}          # EXP total du métier
${job_percentage}          # Progression en % vers level suivant
${job_rank}               # Rang dans le métier (Novice, Expert, etc.)

# Stats de métier
${job_total_actions}       # Actions totales effectuées
${job_earnings_total}      # Argent total gagné avec ce job
${job_earnings_session}    # Argent gagné cette session


# VARIABLES QUANTUM - SYSTÈME DE TOUR
# =====================================

${tower_floor}             # Étage actuel de la tour
${tower_max_floor}        # Étage maximum atteint
${tower_monsters_killed}   # Monstres tués cet étage
${tower_monsters_total}    # Monstres totaux cet étage
${tower_monsters_remaining} # Monstres restants
${tower_progress}          # Progression en % de l'étage
${tower_time}             # Temps passé cet étage
${tower_deaths}           # Morts dans la tour
${tower_completion}       # % de complétion totale de la tour


# VARIABLES QUANTUM - SYSTÈME D'ORDRES
# ======================================

${active_orders}           # Nombre d'ordres actifs
${pending_orders}          # Ordres en attente
${completed_orders_total}  # Ordres complétés (total)
${orders_value_total}      # Valeur totale des ordres actifs
${last_order_item}        # Item du dernier ordre créé
${last_order_time}        # Temps depuis dernier ordre
${orders_earnings}        # Gains via ordres (total)


# VARIABLES QUANTUM - SYSTÈME DE ZONES
# ======================================

${zone_name}               # Nom de la zone actuelle
${zone_description}        # Description de la zone
${zone_type}              # Type de zone (PVP, SAFE, etc.)
${zone_pvp_enabled}       # true/false si PVP activé
${zone_monsters_enabled}   # true/false si monstres spawn
${zone_entry_time}        # Temps depuis entrée dans la zone
${zone_owner}             # Propriétaire de la zone (si applicable)


# VARIABLES QUANTUM - SYSTÈME PVP
# =================================

${pvp_kills}              # Kills PvP total
${pvp_deaths}             # Morts PvP total
${pvp_kd_ratio}           # Ratio K/D
${pvp_streak}             # Série de kills actuelle
${pvp_best_streak}        # Meilleure série de kills
${pvp_last_kill}          # Dernier joueur tué
${pvp_last_death}         # Tué par (dernier)


# VARIABLES QUANTUM - SYSTÈME DE DONJONS
# ========================================

${dungeon_name}            # Nom du donjon actuel
${dungeon_level}           # Niveau du donjon
${dungeon_difficulty}      # Difficulté (Easy, Normal, Hard, etc.)
${dungeon_time}           # Temps passé dans le donjon
${dungeon_monsters_killed} # Monstres tués
${dungeon_monsters_total}  # Total de monstres
${dungeon_deaths}         # Morts dans le donjon
${dungeon_party_size}     # Taille du groupe
${dungeon_objectives_completed} # Objectifs complétés
${dungeon_objectives_total}     # Total objectifs


# VARIABLES QUANTUM - BOSS
# ==========================

${boss_name}              # Nom du boss
${boss_health}            # Vie actuelle du boss
${boss_max_health}        # Vie max du boss
${boss_health_percentage} # % de vie du boss
${boss_phase}             # Phase du combat (si applicable)
${boss_nearby}            # true/false si boss à proximité


# VARIABLES QUANTUM - SYSTÈME DE GROUPE
# =======================================

${party_leader}           # Leader du groupe
${party_size}             # Nombre de membres
${party_member_1}         # Nom du membre 1
${party_member_2}         # Nom du membre 2
${party_member_3}         # Nom du membre 3
${member_1_health}        # Vie du membre 1
${member_2_health}        # Vie du membre 2
${member_3_health}        # Vie du membre 3
${member_1_distance}      # Distance au membre 1
${member_1_direction}     # Direction vers membre 1
${in_party}               # true/false si dans un groupe


# VARIABLES QUANTUM - SYSTÈME DE QUÊTES
# =======================================

${quest_name}             # Nom de la quête active
${quest_description}      # Description de la quête
${quest_objective}        # Objectif actuel
${quest_progress}         # Progression (ex: 5/10)
${quest_percentage}       # Progression en %
${quest_reward_exp}       # Récompense EXP
${quest_reward_money}     # Récompense argent
${quest_time_remaining}   # Temps restant (si limité)
${quest_distance}         # Distance à l'objectif
${quest_direction}        # Direction vers objectif
${has_active_quest}       # true/false


# VARIABLES QUANTUM - WAYPOINTS
# ===============================

${waypoint_name}          # Nom du waypoint
${waypoint_distance}      # Distance au waypoint
${waypoint_direction}     # Direction vers waypoint
${waypoint_description}   # Description du waypoint
${waypoint_x}             # Coord X du waypoint
${waypoint_y}             # Coord Y
${waypoint_z}             # Coord Z
${waypoint_world}         # Monde du waypoint

# Waypoints multiples
${waypoint_1_name}        # Waypoint 1
${waypoint_1_distance}    # Distance waypoint 1
${waypoint_1_direction}   # Direction waypoint 1
# ... (jusqu'à waypoint_10)


# VARIABLES QUANTUM - HOME
# ==========================

${home_name}              # Nom du home principal
${home_distance}          # Distance au home
${home_direction}         # Direction vers home
${home_x}, ${home_y}, ${home_z} # Coordonnées home
${has_home}               # true/false
${total_homes}            # Nombre de homes définis


# VARIABLES QUANTUM - POINT DE MORT
# ===================================

${death_x}, ${death_y}, ${death_z} # Coordonnées de mort
${death_world}            # Monde de la mort
${death_distance}         # Distance au point de mort
${death_direction}        # Direction vers point de mort
${death_time}             # Temps depuis la mort
${death_marker_time}      # Temps avant expiration du marqueur
${recent_death}           # true/false


# VARIABLES SYSTÈME ET SERVEUR
# ==============================

${online_players}         # Joueurs en ligne
${max_players}            # Joueurs maximum
${tps}                    # TPS du serveur
${server_name}            # Nom du serveur
${time}                   # Heure actuelle (monde)
${date}                   # Date actuelle (réelle)

# Temps de jeu
${playtime}               # Temps de jeu total
${playtime_session}       # Temps cette session
${playtime_formatted}     # Temps formaté (1h 30m)


# VARIABLES UTILITAIRES
# ======================

${empty}                  # Chaîne vide
${newline}               # Saut de ligne
${space}                 # Espace


# FORMATS DE NOMBRE
# ==================
# Utilisez avec BetterHudUtil.formatNumber()

${number}                 # Nombre brut
${number_formatted}       # Formaté (1,234)
${number_short}          # Court (1.2K, 5.3M)
${number_percentage}     # En pourcentage


# FORMATS DE TEMPS
# =================

${time_short}            # Format court (1h 30m)
${time_long}             # Format long (1 hour 30 minutes)
${time_seconds}          # Secondes uniquement
${time_ticks}            # Ticks (pour Minecraft)


# COULEURS ET FORMATAGE
# =======================
# Utilisez la syntaxe MiniMessage

<white>                  # Blanc
<black>                  # Noir
<gray>                   # Gris
<dark_gray>             # Gris foncé
<red>                    # Rouge
<dark_red>              # Rouge foncé
<green>                  # Vert
<dark_green>            # Vert foncé
<blue>                   # Bleu
<dark_blue>             # Bleu foncé
<yellow>                 # Jaune
<gold>                   # Or/Orange
<aqua>                   # Cyan/Aqua
<dark_aqua>             # Cyan foncé
<light_purple>          # Violet clair
<dark_purple>           # Violet foncé

# Dégradés
<gradient:#START:#END>texte</gradient>

# Formatage
<bold>                   # Gras
<italic>                 # Italique
<underlined>            # Souligné
<strikethrough>         # Barré
<obfuscated>            # Obfusqué (animation)

# Reset
<reset>                  # Reset tout formatage


# EXEMPLES D'UTILISATION
# =======================

# Afficher argent formaté
"<green>Money: <gold>${money_formatted}"

# Barre de santé
"<red>❤ ${health}/${max_health} <gray>(${health_percentage}%)"

# Position
"<gray>Pos: <white>${x}, ${y}, ${z}"

# Progression job
"<white>${job_name} <gold>Lv.${job_level} <green>(${job_percentage}%)"

# Waypoint avec distance
"<yellow>📍 ${waypoint_name} <gray>- ${waypoint_distance}m ${waypoint_direction}"

# Groupe avec membres
"<gradient:#9B59B6:#3498DB>Party [${party_size}/4]</gradient>"

# Boss avec vie
"<red>${boss_name} ❤ ${boss_health}/${boss_max_health}"

# Tower progress
"<white>Floor ${tower_floor} <green>${tower_progress}%"


# NOTES
# ======
# - Toutes les variables sont sensibles à la casse
# - Les variables non trouvées affichent ${variable} tel quel
# - Utilisez PlaceholderAPI pour variables custom additionnelles
# - Certaines variables nécessitent des contextes spécifiques
# - Les booléens (true/false) peuvent être utilisés avec conditions
