package com.wynvers.quantum.tools;

import com.nexomc.nexo.api.NexoItems;
import com.wynvers.quantum.Quantum;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * Gestionnaire des structures pour la compétence One-shot de la hache
 */
public class StructureManager {
    
    private final Quantum plugin;
    private final Map<String, Structure> structures;
    private YamlConfiguration config;
    
    public StructureManager(Quantum plugin) {
        this.plugin = plugin;
        this.structures = new HashMap<>();
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "structures.yml");
        if (!configFile.exists()) {
            plugin.saveResource("structures.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadStructures();
        plugin.getLogger().info("✓ Structure system loaded! (" + structures.size() + " structure types)");
    }
    
    private void loadStructures() {
        structures.clear();
        
        ConfigurationSection structuresSection = config.getConfigurationSection("structures");
        if (structuresSection == null) return;
        
        for (String structureId : structuresSection.getKeys(false)) {
            ConfigurationSection section = structuresSection.getConfigurationSection(structureId);
            if (section == null) continue;
            
            String displayName = section.getString("display_name", structureId);
            Structure structure = new Structure(structureId, displayName);
            
            // Charger les différents états
            for (StructureState state : StructureState.values()) {
                ConfigurationSection stateSection = section.getConfigurationSection(state.name().toLowerCase());
                if (stateSection != null) {
                    List<String> blockStrings = stateSection.getStringList("blocks");
                    List<StructureBlock> blocks = new ArrayList<>();
                    
                    for (String blockString : blockStrings) {
                        String[] parts = blockString.split(":");
                        if (parts.length < 2) continue;
                        
                        String[] coords = parts[0].split(",");
                        if (coords.length != 3) continue;
                        
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);
                        
                        String type = parts[1];
                        String id = parts.length > 2 ? parts[2] : null;
                        
                        blocks.add(new StructureBlock(x, y, z, type, id));
                    }
                    
                    structure.setState(state, blocks);
                }
            }
            
            structures.put(structureId, structure);
        }
    }
    
    public void reload() {
        loadConfig();
    }
    
    /**
     * Casse une structure complète (whole → stump)
     */
    public void breakStructure(Location baseLocation, String structureId, StructureState fromState) {
        Structure structure = structures.get(structureId);
        if (structure == null) return;
        
        // Retirer tous les blocs de l'état actuel
        List<StructureBlock> currentBlocks = structure.getState(fromState);
        if (currentBlocks != null) {
            for (StructureBlock block : currentBlocks) {
                Location loc = baseLocation.clone().add(block.getX(), block.getY(), block.getZ());
                loc.getBlock().setType(Material.AIR);
            }
        }
        
        // Placer l'état "stump"
        List<StructureBlock> stumpBlocks = structure.getState(StructureState.STUMP);
        if (stumpBlocks != null) {
            for (StructureBlock block : stumpBlocks) {
                Location loc = baseLocation.clone().add(block.getX(), block.getY(), block.getZ());
                placeBlock(loc, block);
            }
        }
    }
    
    /**
     * Place un bloc (Minecraft ou Nexo)
     */
    private void placeBlock(Location location, StructureBlock block) {
        if ("minecraft".equalsIgnoreCase(block.getType())) {
            Material material = Material.matchMaterial(block.getId());
            if (material != null) {
                location.getBlock().setType(material);
            } else {
                plugin.getLogger().warning("Material '" + block.getId() + "' not found in structure configuration");
            }
        } else if ("nexo".equalsIgnoreCase(block.getType())) {
            // TODO: Utiliser NexoBlocks pour placer un bloc Nexo
            // Pour l'instant, placer un bloc normal
            plugin.getLogger().warning("Nexo blocks not yet supported in structures");
        }
    }
    
    /**
     * Détecte une structure à une position donnée et retourne son ID et son état
     * @param location Position du bloc cliqué
     * @return Un tableau [structureId, state] ou null si aucune structure détectée
     */
    public String[] detectStructure(Location location) {
        // Pour chaque structure connue
        for (Structure structure : structures.values()) {
            // Pour chaque état possible
            for (StructureState state : StructureState.values()) {
                List<StructureBlock> blocks = structure.getState(state);
                if (blocks == null || blocks.isEmpty()) continue;
                
                // Tester si la structure correspond à cette position
                // On teste chaque bloc comme potentiel point de base
                for (StructureBlock baseBlock : blocks) {
                    Location baseLocation = location.clone().subtract(baseBlock.getX(), baseBlock.getY(), baseBlock.getZ());
                    
                    if (matchesStructure(baseLocation, blocks)) {
                        return new String[] { structure.getId(), state.name() };
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Vérifie si une structure correspond aux blocs à une position donnée
     */
    private boolean matchesStructure(Location baseLocation, List<StructureBlock> blocks) {
        for (StructureBlock block : blocks) {
            Location loc = baseLocation.clone().add(block.getX(), block.getY(), block.getZ());
            
            if ("minecraft".equalsIgnoreCase(block.getType())) {
                Material material = Material.matchMaterial(block.getId());
                if (material == null || loc.getBlock().getType() != material) {
                    return false;
                }
            }
            // TODO: Vérifier les blocs Nexo
        }
        
        return true;
    }
    
    /**
     * Dégrade l'état d'une structure (whole -> good -> damaged -> stump)
     * @param location Position de la structure
     * @param structureId ID de la structure
     * @param currentState État actuel
     * @return Le nouvel état ou null si aucun changement
     */
    public StructureState degradeStructure(Location location, String structureId, StructureState currentState) {
        Structure structure = structures.get(structureId);
        if (structure == null) return null;
        
        StructureState newState = getNextState(currentState);
        if (newState == null) return null;
        
        // Retirer les blocs de l'état actuel
        List<StructureBlock> currentBlocks = structure.getState(currentState);
        if (currentBlocks != null) {
            for (StructureBlock block : currentBlocks) {
                Location loc = location.clone().add(block.getX(), block.getY(), block.getZ());
                loc.getBlock().setType(Material.AIR);
            }
        }
        
        // Placer les blocs du nouvel état
        List<StructureBlock> newBlocks = structure.getState(newState);
        if (newBlocks != null) {
            for (StructureBlock block : newBlocks) {
                Location loc = location.clone().add(block.getX(), block.getY(), block.getZ());
                placeBlock(loc, block);
            }
        }
        
        return newState;
    }
    
    /**
     * Retourne l'état suivant dans la dégradation
     */
    private StructureState getNextState(StructureState current) {
        return switch (current) {
            case WHOLE -> StructureState.GOOD;
            case GOOD -> StructureState.DAMAGED;
            case DAMAGED -> StructureState.STUMP;
            case STUMP -> null;  // Pas de dégradation possible
        };
    }
    
    /**
     * Récupère une structure par son ID
     */
    public Structure getStructure(String structureId) {
        return structures.get(structureId);
    }
    
    /**
     * Récupère toutes les structures
     */
    public Collection<Structure> getAllStructures() {
        return structures.values();
    }
    
    /**
     * Classe représentant une structure
     */
    public static class Structure {
        private final String id;
        private final String displayName;
        private final Map<StructureState, List<StructureBlock>> states;
        
        public Structure(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
            this.states = new EnumMap<>(StructureState.class);
        }
        
        public void setState(StructureState state, List<StructureBlock> blocks) {
            states.put(state, blocks);
        }
        
        public List<StructureBlock> getState(StructureState state) {
            return states.get(state);
        }
        
        public String getId() {
            return id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Classe représentant un bloc dans une structure
     */
    public static class StructureBlock {
        private final int x, y, z;
        private final String type; // "minecraft" ou "nexo"
        private final String id;   // Material name ou Nexo ID
        
        public StructureBlock(int x, int y, int z, String type, String id) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.id = id;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public String getType() { return type; }
        public String getId() { return id; }
    }
    
    /**
     * États possibles d'une structure
     */
    public enum StructureState {
        WHOLE,    // Entier
        GOOD,     // Bon état
        DAMAGED,  // Abîmé
        STUMP     // Souche
    }
}
