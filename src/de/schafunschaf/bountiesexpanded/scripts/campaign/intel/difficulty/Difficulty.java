package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty;

import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;

public final class Difficulty {

    public static final Difficulty EASY = new Difficulty(
            "easy", "an", 1f, 0, Color.GREEN
    );
    public static final Difficulty MEDIUM = new Difficulty(
            "fair", "a", 1.15f, 1, Color.CYAN
    );
    public static final Difficulty CHALLENGING = new Difficulty(
            "challenging", "a", 1.25f, 2, Color.ORANGE
    );
    public static final Difficulty HARD = new Difficulty(
            "difficult", "a", 1.4f, 3, Color.RED
    );
    public static final Difficulty BOSS = new Difficulty(
            "BOSS", "a", 1.6f, 4, Color.MAGENTA
    );
    private static final Difficulty[] VALUES = {
            EASY, MEDIUM, HARD, CHALLENGING, BOSS
    };
    private final String shortDescription;
    private final String shortDescriptionAnOrA;
    private final float modifier;
    private final int levelAdjustment;
    private final Color color;

    private Difficulty(String shortDescription, String shortDescriptionAnOrA, float modifier, int levelAdjustment, Color color) {
        this.shortDescriptionAnOrA = shortDescriptionAnOrA;
        this.shortDescription = shortDescription;
        this.modifier = modifier;
        this.levelAdjustment = levelAdjustment;
        this.color = color;
    }

    public static Difficulty[] values() {
        return VALUES.clone();
    }

    public static Difficulty randomDifficulty() {
        WeightedRandomPicker<Difficulty> picker = new WeightedRandomPicker<>();
        picker.add(EASY, 1);
        picker.add(MEDIUM, 2);
        picker.add(CHALLENGING, 2);
        picker.add(HARD, 1);
        return picker.pick();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getShortDescriptionAnOrA() {
        return shortDescriptionAnOrA;
    }

    public float getModifier() {
        return modifier;
    }

    public int getLevelAdjustment() {
        return levelAdjustment;
    }

    public Color getColor() {
        return color;
    }
}
