package fr.robie.quantum.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu {
    
    private final String id;
    private String title;
    private int size;
    private String openCommand;
    
    // Animated title
    private boolean animatedTitle;
    private List<String> titleFrames;
    private int titleSpeed;
    
    // Items
    private final Map<String, MenuItem> items;
    
    public Menu(String id) {
        this.id = id;
        this.size = 54;
        this.items = new HashMap<>();
        this.titleFrames = new ArrayList<>();
        this.titleSpeed = 10;
    }
    
    // === GETTERS ===
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getOpenCommand() {
        return openCommand;
    }
    
    public boolean hasAnimatedTitle() {
        return animatedTitle;
    }
    
    public List<String> getTitleFrames() {
        return titleFrames;
    }
    
    public int getTitleSpeed() {
        return titleSpeed;
    }
    
    public Map<String, MenuItem> getItems() {
        return items;
    }
    
    public MenuItem getItem(String id) {
        return items.get(id);
    }
    
    // === SETTERS ===
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setSize(int size) {
        // Validate size (must be multiple of 9, between 9 and 54)
        if (size % 9 != 0 || size < 9 || size > 54) {
            this.size = 54;
        } else {
            this.size = size;
        }
    }
    
    public void setOpenCommand(String openCommand) {
        this.openCommand = openCommand;
    }
    
    public void setAnimatedTitle(boolean animatedTitle) {
        this.animatedTitle = animatedTitle;
    }
    
    public void setTitleFrames(List<String> titleFrames) {
        this.titleFrames = titleFrames;
    }
    
    public void setTitleSpeed(int titleSpeed) {
        this.titleSpeed = Math.max(1, titleSpeed);
    }
    
    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
    }
}
