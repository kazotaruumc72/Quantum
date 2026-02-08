# Dossier pour les schematics utilisés par les skills de mobs

Place tes fichiers .schem ou .schematic ici.

Exemple :
- ice_spike_1.schem
- ice_spike_2.schem
- fire_trap.schem

Ces schematics peuvent être utilisés dans towers.yml :

```yaml
skills:
  - id: iceberg
    interval: 30
    schematic: "ice_spike_1"  # nom sans extension
```

Pour créer un schematic avec WorldEdit/FAWE :
1. //wand
2. Sélectionne ta structure
3. //copy
4. //schem save nom_du_schematic
5. Copie le fichier .schem ici depuis plugins/WorldEdit/schematics/ ou plugins/FastAsyncWorldEdit/schematics/
