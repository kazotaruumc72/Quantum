/**
 * API client for Quantum Menu Builder
 * Handles all communication with the plugin's REST API
 */

const API_BASE = '/api/menus';

class MenuAPI {
    /**
     * Get all menus
     */
    static async getMenus() {
        try {
            const response = await fetch(API_BASE);
            if (!response.ok) throw new Error('Failed to fetch menus');
            return await response.json();
        } catch (error) {
            console.error('Error fetching menus:', error);
            throw error;
        }
    }

    /**
     * Get a specific menu by ID
     */
    static async getMenu(menuId) {
        try {
            const response = await fetch(`${API_BASE}/${menuId}`);
            if (!response.ok) throw new Error(`Failed to fetch menu: ${menuId}`);
            return await response.json();
        } catch (error) {
            console.error(`Error fetching menu ${menuId}:`, error);
            throw error;
        }
    }

    /**
     * Create a new menu
     */
    static async createMenu(menuData) {
        try {
            const response = await fetch(API_BASE, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(menuData)
            });
            if (!response.ok) throw new Error('Failed to create menu');
            return await response.json();
        } catch (error) {
            console.error('Error creating menu:', error);
            throw error;
        }
    }

    /**
     * Update an existing menu
     */
    static async updateMenu(menuId, menuData) {
        try {
            const response = await fetch(`${API_BASE}/${menuId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(menuData)
            });
            if (!response.ok) throw new Error(`Failed to update menu: ${menuId}`);
            return await response.json();
        } catch (error) {
            console.error(`Error updating menu ${menuId}:`, error);
            throw error;
        }
    }

    /**
     * Delete a menu
     */
    static async deleteMenu(menuId) {
        try {
            const response = await fetch(`${API_BASE}/${menuId}`, {
                method: 'DELETE'
            });
            if (!response.ok) throw new Error(`Failed to delete menu: ${menuId}`);
            return await response.json();
        } catch (error) {
            console.error(`Error deleting menu ${menuId}:`, error);
            throw error;
        }
    }

    /**
     * Reload all menus on the server
     */
    static async reloadMenus() {
        try {
            const response = await fetch(`${API_BASE}/reload`, {
                method: 'POST'
            });
            if (!response.ok) throw new Error('Failed to reload menus');
            return await response.json();
        } catch (error) {
            console.error('Error reloading menus:', error);
            throw error;
        }
    }
}

/**
 * Convert menu data between API format and YAML format
 */
class MenuConverter {
    /**
     * Convert YAML-style menu data to UI-friendly format
     */
    static fromYAML(yamlData) {
        return {
            id: yamlData.id || '',
            title: yamlData.menu_title || yamlData.title || '',
            size: yamlData.size || 54,
            openCommand: yamlData.open_command || '',
            items: this.convertItems(yamlData.items || {})
        };
    }

    /**
     * Convert UI format back to YAML format
     */
    static toYAML(uiData) {
        return {
            menu_title: uiData.title,
            size: uiData.size,
            open_command: uiData.openCommand,
            items: this.convertItemsToYAML(uiData.items)
        };
    }

    /**
     * Convert YAML items to UI format
     */
    static convertItems(yamlItems) {
        const items = [];
        for (const [itemId, itemData] of Object.entries(yamlItems)) {
            items.push({
                id: itemId,
                slots: this.parseSlots(itemData.slot, itemData.slots),
                material: itemData.material || '',
                nexoItem: itemData.nexo_item || '',
                displayName: itemData.display_name || '',
                lore: itemData.lore || [],
                buttonType: itemData.button_type || '',
                type: itemData.type || '',
                leftClickActions: itemData.left_click_actions || [],
                rightClickActions: itemData.right_click_actions || [],
                glow: itemData.glow || false,
                customModelData: itemData.custom_model_data || 0
            });
        }
        return items;
    }

    /**
     * Convert UI items to YAML format
     */
    static convertItemsToYAML(items) {
        const yamlItems = {};
        for (const item of items) {
            const itemData = {};

            // Add slots
            if (item.slots && item.slots.length > 0) {
                if (item.slots.length === 1) {
                    itemData.slot = item.slots[0];
                } else {
                    itemData.slots = item.slots;
                }
            }

            // Add material
            if (item.material) itemData.material = item.material;
            if (item.nexoItem) itemData.nexo_item = item.nexoItem;

            // Add display properties
            if (item.displayName) itemData.display_name = item.displayName;
            if (item.lore && item.lore.length > 0) itemData.lore = item.lore;

            // Add button properties
            if (item.buttonType) itemData.button_type = item.buttonType;
            if (item.type) itemData.type = item.type;

            // Add actions
            if (item.leftClickActions && item.leftClickActions.length > 0) {
                itemData.left_click_actions = item.leftClickActions;
            }
            if (item.rightClickActions && item.rightClickActions.length > 0) {
                itemData.right_click_actions = item.rightClickActions;
            }

            // Add other properties
            if (item.glow) itemData.glow = true;
            if (item.customModelData) itemData.custom_model_data = item.customModelData;

            yamlItems[item.id] = itemData;
        }
        return yamlItems;
    }

    /**
     * Parse slots from YAML format
     */
    static parseSlots(singleSlot, slotArray) {
        const slots = [];

        if (singleSlot !== undefined && singleSlot !== null) {
            slots.push(singleSlot);
        }

        if (slotArray && Array.isArray(slotArray)) {
            for (const slotDef of slotArray) {
                if (typeof slotDef === 'number') {
                    slots.push(slotDef);
                } else if (typeof slotDef === 'string') {
                    // Parse ranges like "9-17"
                    if (slotDef.includes('-')) {
                        const [start, end] = slotDef.split('-').map(s => parseInt(s.trim()));
                        for (let i = start; i <= end; i++) {
                            slots.push(i);
                        }
                    } else {
                        slots.push(parseInt(slotDef));
                    }
                }
            }
        }

        return slots;
    }
}
