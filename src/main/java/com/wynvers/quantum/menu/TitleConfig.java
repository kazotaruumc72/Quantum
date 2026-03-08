package com.wynvers.quantum.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a title configuration with support for SIMPLY and CONDITIONNAL types
 */
public class TitleConfig {

    private TitleType titleType;
    private String simpleTitle;
    private boolean titleAnimation;
    private int delayAnimation; // in seconds
    private List<AnimationFrame> animations;
    private List<ConditionalTitle> conditions;

    public TitleConfig() {
        this.titleType = TitleType.SIMPLY;
        this.titleAnimation = false;
        this.delayAnimation = 2;
        this.animations = new ArrayList<>();
        this.conditions = new ArrayList<>();
    }

    // === GETTERS ===

    public TitleType getTitleType() {
        return titleType;
    }

    public String getSimpleTitle() {
        return simpleTitle;
    }

    public boolean isTitleAnimation() {
        return titleAnimation;
    }

    public int getDelayAnimation() {
        return delayAnimation;
    }

    public List<AnimationFrame> getAnimations() {
        return animations;
    }

    public List<ConditionalTitle> getConditions() {
        return conditions;
    }

    // === SETTERS ===

    public void setTitleType(TitleType titleType) {
        this.titleType = titleType;
    }

    public void setSimpleTitle(String simpleTitle) {
        this.simpleTitle = simpleTitle;
    }

    public void setTitleAnimation(boolean titleAnimation) {
        this.titleAnimation = titleAnimation;
    }

    public void setDelayAnimation(int delayAnimation) {
        this.delayAnimation = Math.max(1, delayAnimation);
    }

    public void setAnimations(List<AnimationFrame> animations) {
        this.animations = animations;
    }

    public void setConditions(List<ConditionalTitle> conditions) {
        this.conditions = conditions;
    }

    public void addAnimation(AnimationFrame frame) {
        this.animations.add(frame);
    }

    public void addCondition(ConditionalTitle condition) {
        this.conditions.add(condition);
    }

    // === NESTED CLASSES ===

    /**
     * Represents an animation frame
     */
    public static class AnimationFrame {
        private String frame;

        public AnimationFrame(String frame) {
            this.frame = frame;
        }

        public String getFrame() {
            return frame;
        }

        public void setFrame(String frame) {
            this.frame = frame;
        }
    }

    /**
     * Represents a conditional title with requirements
     */
    public static class ConditionalTitle {
        private String id;
        private List<Requirement> requirements;
        private String title;

        public ConditionalTitle(String id) {
            this.id = id;
            this.requirements = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public List<Requirement> getRequirements() {
            return requirements;
        }

        public void addRequirement(Requirement requirement) {
            this.requirements.add(requirement);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    /**
     * Title type enum
     */
    public enum TitleType {
        SIMPLY,
        CONDITIONNAL
    }
}
