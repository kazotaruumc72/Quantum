/**
 * Quantum Menu Builder - Main Application
 */

class MenuBuilder {
    constructor() {
        this.currentMenu = null;
        this.menus = [];
        this.selectedSlot = null;
        this.init();
    }

    async init() {
        this.setupEventListeners();
        await this.loadMenus();
    }

    setupEventListeners() {
        // New menu buttons
        document.getElementById('new-menu-btn').addEventListener('click', () => this.createNewMenu());
        document.getElementById('welcome-new-menu-btn').addEventListener('click', () => this.createNewMenu());

        // Save/Delete buttons
        document.getElementById('save-menu-btn').addEventListener('click', () => this.saveCurrentMenu());
        document.getElementById('delete-menu-btn').addEventListener('click', () => this.deleteCurrentMenu());

        // Menu size change
        document.getElementById('menu-size').addEventListener('change', (e) => {
            if (this.currentMenu) {
                this.currentMenu.size = parseInt(e.target.value);
                this.renderInventoryGrid();
            }
        });

        // Modal close buttons
        document.querySelectorAll('.close, .close-modal').forEach(el => {
            el.addEventListener('click', () => this.closeModal());
        });

        // Item modal buttons
        document.getElementById('save-item-btn').addEventListener('click', () => this.saveItem());
        document.getElementById('remove-item-btn').addEventListener('click', () => this.removeItem());

        // Close modal when clicking outside
        window.addEventListener('click', (e) => {
            const modal = document.getElementById('item-modal');
            if (e.target === modal) {
                this.closeModal();
            }
        });
    }

    async loadMenus() {
        try {
            const menusData = await MenuAPI.getMenus();
            this.menus = menusData.map(m => MenuConverter.fromYAML(m));
            this.renderMenuList();
        } catch (error) {
            console.error('Failed to load menus:', error);
            this.showError('Failed to load menus. Please check if the server is running.');
        }
    }

    renderMenuList() {
        const menuList = document.getElementById('menu-list');
        menuList.innerHTML = '';

        if (this.menus.length === 0) {
            menuList.innerHTML = '<p style="padding: 1rem; color: var(--text-muted);">No menus found. Create one to get started!</p>';
            return;
        }

        this.menus.forEach(menu => {
            const menuItem = document.createElement('div');
            menuItem.className = 'menu-item';
            if (this.currentMenu && this.currentMenu.id === menu.id) {
                menuItem.classList.add('active');
            }

            menuItem.innerHTML = `
                <h3>${menu.title || menu.id}</h3>
                <p>${menu.size} slots · ${menu.openCommand ? `/${menu.openCommand}` : 'No command'}</p>
            `;

            menuItem.addEventListener('click', () => this.selectMenu(menu));
            menuList.appendChild(menuItem);
        });
    }

    selectMenu(menu) {
        this.currentMenu = menu;
        this.renderMenuList();
        this.showMenuEditor();
        this.loadMenuIntoEditor();
    }

    showMenuEditor() {
        document.getElementById('welcome-screen').classList.add('hidden');
        document.getElementById('menu-editor').classList.add('active');
    }

    hideMenuEditor() {
        document.getElementById('welcome-screen').classList.remove('hidden');
        document.getElementById('menu-editor').classList.remove('active');
    }

    loadMenuIntoEditor() {
        document.getElementById('menu-title').value = this.currentMenu.title || '';
        document.getElementById('menu-size').value = this.currentMenu.size;
        document.getElementById('open-command').value = this.currentMenu.openCommand || '';

        this.renderInventoryGrid();
    }

    renderInventoryGrid() {
        const grid = document.getElementById('inventory-grid');
        grid.innerHTML = '';
        grid.style.gridTemplateColumns = 'repeat(9, 1fr)';

        const size = this.currentMenu ? this.currentMenu.size : 54;

        for (let i = 0; i < size; i++) {
            const slot = document.createElement('div');
            slot.className = 'inventory-slot';
            slot.dataset.slot = i;

            // Check if this slot has an item
            const item = this.getItemAtSlot(i);
            if (item) {
                slot.classList.add('has-item');
                const materialDisplay = item.nexoItem || item.material || '?';
                slot.innerHTML = `
                    <span class="slot-number">${i}</span>
                    <span class="slot-material">${materialDisplay}</span>
                `;
            } else {
                slot.innerHTML = `<span class="slot-number">${i}</span>`;
            }

            slot.addEventListener('click', () => this.selectSlot(i));
            grid.appendChild(slot);
        }
    }

    selectSlot(slotIndex) {
        this.selectedSlot = slotIndex;

        // Update visual selection
        document.querySelectorAll('.inventory-slot').forEach(s => s.classList.remove('selected'));
        document.querySelector(`.inventory-slot[data-slot="${slotIndex}"]`).classList.add('selected');

        // Open item editor
        this.openItemEditor(slotIndex);
    }

    getItemAtSlot(slotIndex) {
        if (!this.currentMenu || !this.currentMenu.items) return null;
        return this.currentMenu.items.find(item => item.slots && item.slots.includes(slotIndex));
    }

    openItemEditor(slotIndex) {
        const item = this.getItemAtSlot(slotIndex);
        const modal = document.getElementById('item-modal');

        if (item) {
            // Editing existing item
            document.getElementById('item-slots').value = item.slots.join(', ');
            document.getElementById('item-material').value = item.material || '';
            document.getElementById('item-nexo').value = item.nexoItem || '';
            document.getElementById('item-display-name').value = item.displayName || '';
            document.getElementById('item-lore').value = (item.lore || []).join('\n');
            document.getElementById('item-button-type').value = item.buttonType || '';
            document.getElementById('item-left-actions').value = (item.leftClickActions || []).join('\n');
            document.getElementById('item-right-actions').value = (item.rightClickActions || []).join('\n');
            document.getElementById('item-glow').checked = item.glow || false;
            document.getElementById('item-custom-model-data').value = item.customModelData || 0;
            document.getElementById('item-type').value = item.type || '';
        } else {
            // Creating new item
            document.getElementById('item-slots').value = slotIndex;
            document.getElementById('item-material').value = '';
            document.getElementById('item-nexo').value = '';
            document.getElementById('item-display-name').value = '';
            document.getElementById('item-lore').value = '';
            document.getElementById('item-button-type').value = '';
            document.getElementById('item-left-actions').value = '';
            document.getElementById('item-right-actions').value = '';
            document.getElementById('item-glow').checked = false;
            document.getElementById('item-custom-model-data').value = 0;
            document.getElementById('item-type').value = '';
        }

        modal.classList.add('active');
    }

    closeModal() {
        document.getElementById('item-modal').classList.remove('active');
    }

    parseSlots(slotsString) {
        const slots = [];
        const parts = slotsString.split(',');

        for (const part of parts) {
            const trimmed = part.trim();
            if (trimmed.includes('-')) {
                // Range: 0-8
                const [start, end] = trimmed.split('-').map(s => parseInt(s.trim()));
                for (let i = start; i <= end; i++) {
                    slots.push(i);
                }
            } else {
                // Single slot
                const slot = parseInt(trimmed);
                if (!isNaN(slot)) {
                    slots.push(slot);
                }
            }
        }

        return slots;
    }

    saveItem() {
        if (!this.currentMenu) return;

        const slotsString = document.getElementById('item-slots').value;
        const slots = this.parseSlots(slotsString);

        if (slots.length === 0) {
            alert('Please specify at least one slot');
            return;
        }

        const itemData = {
            id: `item_${Date.now()}`,
            slots: slots,
            material: document.getElementById('item-material').value,
            nexoItem: document.getElementById('item-nexo').value,
            displayName: document.getElementById('item-display-name').value,
            lore: document.getElementById('item-lore').value.split('\n').filter(l => l.trim()),
            buttonType: document.getElementById('item-button-type').value,
            type: document.getElementById('item-type').value,
            leftClickActions: document.getElementById('item-left-actions').value.split('\n').filter(l => l.trim()),
            rightClickActions: document.getElementById('item-right-actions').value.split('\n').filter(l => l.trim()),
            glow: document.getElementById('item-glow').checked,
            customModelData: parseInt(document.getElementById('item-custom-model-data').value) || 0
        };

        // Remove existing items at these slots
        if (!this.currentMenu.items) {
            this.currentMenu.items = [];
        }
        this.currentMenu.items = this.currentMenu.items.filter(item => {
            return !item.slots.some(slot => slots.includes(slot));
        });

        // Add new item
        this.currentMenu.items.push(itemData);

        this.renderInventoryGrid();
        this.closeModal();
    }

    removeItem() {
        if (!this.currentMenu || this.selectedSlot === null) return;

        const item = this.getItemAtSlot(this.selectedSlot);
        if (!item) return;

        this.currentMenu.items = this.currentMenu.items.filter(i => i !== item);
        this.renderInventoryGrid();
        this.closeModal();
    }

    createNewMenu() {
        const menuId = prompt('Enter a unique ID for the new menu (e.g., my_menu):');
        if (!menuId) return;

        // Check if ID already exists
        if (this.menus.find(m => m.id === menuId)) {
            alert('A menu with this ID already exists!');
            return;
        }

        const newMenu = {
            id: menuId,
            title: '&fNew Menu',
            size: 54,
            openCommand: menuId,
            items: []
        };

        this.menus.push(newMenu);
        this.selectMenu(newMenu);
        this.renderMenuList();
    }

    async saveCurrentMenu() {
        if (!this.currentMenu) return;

        try {
            // Get updated values from form
            this.currentMenu.title = document.getElementById('menu-title').value;
            this.currentMenu.openCommand = document.getElementById('open-command').value;

            // Convert to YAML format
            const yamlData = MenuConverter.toYAML(this.currentMenu);

            // Save via API
            const existingMenu = this.menus.find(m => m.id === this.currentMenu.id);
            if (existingMenu && existingMenu !== this.currentMenu) {
                await MenuAPI.updateMenu(this.currentMenu.id, yamlData);
            } else {
                await MenuAPI.createMenu({ id: this.currentMenu.id, ...yamlData });
            }

            await this.loadMenus();
            this.showSuccess('Menu saved successfully!');
        } catch (error) {
            console.error('Failed to save menu:', error);
            this.showError('Failed to save menu: ' + error.message);
        }
    }

    async deleteCurrentMenu() {
        if (!this.currentMenu) return;

        if (!confirm(`Are you sure you want to delete the menu "${this.currentMenu.title}"?`)) {
            return;
        }

        try {
            await MenuAPI.deleteMenu(this.currentMenu.id);
            this.menus = this.menus.filter(m => m.id !== this.currentMenu.id);
            this.currentMenu = null;
            this.renderMenuList();
            this.hideMenuEditor();
            this.showSuccess('Menu deleted successfully!');
        } catch (error) {
            console.error('Failed to delete menu:', error);
            this.showError('Failed to delete menu: ' + error.message);
        }
    }

    showSuccess(message) {
        alert('✓ ' + message);
    }

    showError(message) {
        alert('✗ ' + message);
    }
}

// Initialize the application when the DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.menuBuilder = new MenuBuilder();
});
