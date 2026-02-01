package com.wynvers.quantum.utils;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.inventory.ItemStack;

/**
 * Utilitaires pour manipuler les items Minecraft et Nexo
 */
public class ItemUtils {

    /**
     * Récupère un item Nexo par son ID
     * @param nexoId L'ID de l'item Nexo
     * @return L'ItemStack Nexo ou null si non trouvé
     */
    public static ItemStack getNexoItem(String nexoId) {
        try {
            return NexoItems.itemFromId(nexoId).build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie si un item est un item Nexo
     * @param itemStack L'item à vérifier
     * @return true si c'est un item Nexo
     */
    public static boolean isNexoItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        try {
            return NexoItems.exists(itemStack);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Récupère l'ID d'un item Nexo
     * @param itemStack L'item Nexo
     * @return L'ID de l'item ou null si ce n'est pas un item Nexo
     */
    public static String getNexoItemId(ItemStack itemStack) {
        if (!isNexoItem(itemStack)) return null;
        try {
            return NexoItems.idFromItem(itemStack);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtient l'identifiant complet d'un item (minecraft:id ou nexo:id)
     * @param itemStack L'item à identifier
     * @return L'identifiant complet
     */
    public static String getItemId(ItemStack itemStack) {
        if (itemStack == null) return null;
        
        // Vérifier si c'est un item Nexo
        String nexoId = getNexoItemId(itemStack);
        if (nexoId != null) {
            return "nexo:" + nexoId;
        }
        
        // Sinon c'est un item Minecraft
        return "minecraft:" + itemStack.getType().name().toLowerCase();
    }
}
