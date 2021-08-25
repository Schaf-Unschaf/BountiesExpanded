package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty;

import java.awt.*;

public final class Difficulty {

    public static final Difficulty LOW = new Difficulty(
            "an easy encounter", 0.5f, Color.GREEN
    );
    public static final Difficulty MEDIUM = new Difficulty(
            "medium difficulty", 1f, Color.YELLOW
    );
    public static final Difficulty HARD = new Difficulty(
            "hard difficulty", 1.5f, Color.ORANGE
    );
    public static final Difficulty EXTREME = new Difficulty(
            "extreme difficulty", 2f, Color.RED
    );
    public static final Difficulty BOSS = new Difficulty(
            "boss difficulty", 3f, Color.MAGENTA
    );

    private final String shortDescription;
    private final float modifier;
    private final Color color;

    private static final Difficulty[] VALUES = {
            LOW, MEDIUM, HARD, EXTREME, BOSS
    };

    private Difficulty(String shortDescription, float modifier, Color color) {
        this.modifier = modifier;
        this.shortDescription = shortDescription;
        this.color = color;
    }

    public static Difficulty[] values() {
        return VALUES.clone();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public float getModifier() {
        return modifier;
    }

    public Color getColor() {
        return color;
    }
}
