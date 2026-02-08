package com.wynvers.quantum.mobs;

import java.util.Map;

/**
 * Configuration d'une compétence pour un mob.
 * Permet de définir des capacités spéciales pour les mobs spawned par les tours.
 */
public class MobSkillConfig {

    private String skillName;
    private String skillType;
    private int cooldown;
    private double range;
    private Map<String, Object> parameters;

    /**
     * Constructeur à partir d'une map de configuration.
     * 
     * @param config Map contenant les données de la compétence
     */
    public MobSkillConfig(Map<String, Object> config) {
        this.parameters = config;
        
        // Parsing des valeurs de base
        this.skillName = (String) config.getOrDefault("name", "unknown_skill");
        this.skillType = (String) config.getOrDefault("type", "passive");
        
        // Gestion du cooldown (peut être int ou double dans le YAML)
        Object cooldownObj = config.get("cooldown");
        if (cooldownObj instanceof Number) {
            this.cooldown = ((Number) cooldownObj).intValue();
        } else {
            this.cooldown = 0;
        }
        
        // Gestion de la portée (peut être int ou double dans le YAML)
        Object rangeObj = config.get("range");
        if (rangeObj instanceof Number) {
            this.range = ((Number) rangeObj).doubleValue();
        } else {
            this.range = 0.0;
        }
    }

    /**
     * Récupère le nom de la compétence.
     * 
     * @return Nom de la compétence
     */
    public String getSkillName() {
        return skillName;
    }

    /**
     * Récupère le type de la compétence (active, passive, etc.).
     * 
     * @return Type de compétence
     */
    public String getSkillType() {
        return skillType;
    }

    /**
     * Récupère le cooldown en secondes.
     * 
     * @return Cooldown de la compétence
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * Récupère la portée de la compétence.
     * 
     * @return Portée en blocs
     */
    public double getRange() {
        return range;
    }

    /**
     * Récupère un paramètre spécifique de la compétence.
     * 
     * @param key Clé du paramètre
     * @return Valeur du paramètre ou null si inexistant
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * Récupère un paramètre sous forme de String.
     * 
     * @param key Clé du paramètre
     * @param defaultValue Valeur par défaut si le paramètre n'existe pas
     * @return Valeur du paramètre ou la valeur par défaut
     */
    public String getParameterAsString(String key, String defaultValue) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Récupère un paramètre sous forme de int.
     * 
     * @param key Clé du paramètre
     * @param defaultValue Valeur par défaut si le paramètre n'existe pas
     * @return Valeur du paramètre ou la valeur par défaut
     */
    public int getParameterAsInt(String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Récupère un paramètre sous forme de double.
     * 
     * @param key Clé du paramètre
     * @param defaultValue Valeur par défaut si le paramètre n'existe pas
     * @return Valeur du paramètre ou la valeur par défaut
     */
    public double getParameterAsDouble(String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Récupère un paramètre sous forme de boolean.
     * 
     * @param key Clé du paramètre
     * @param defaultValue Valeur par défaut si le paramètre n'existe pas
     * @return Valeur du paramètre ou la valeur par défaut
     */
    public boolean getParameterAsBoolean(String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Récupère tous les paramètres de configuration.
     * 
     * @return Map des paramètres
     */
    public Map<String, Object> getAllParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "MobSkillConfig{" +
                "name='" + skillName + '\'' +
                ", type='" + skillType + '\'' +
                ", cooldown=" + cooldown +
                ", range=" + range +
                '}';
    }
}
