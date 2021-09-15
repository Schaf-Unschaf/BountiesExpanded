package de.schafunschaf.bountiesexpanded.helper.level;

import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;

import java.util.Random;

public class LevelPicker {
    public static int pickLevel(int minLevel, int maxLevel) {
        int bound = Math.max(maxLevel - minLevel, 0) + 1;
        return new Random().nextInt(bound);
    }

    public static int pickLevel(int variation) {
        int level = BountyEventData.getSharedData().getLevel();
        float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 2f);
        timeFactor = Math.max(timeFactor, 0);
        timeFactor = Math.min(timeFactor, 1);
        level += Math.round(3 * timeFactor);
        level = Math.min(level, 10);
        level = (level - variation) + new Random().nextInt(variation * 2 + 1);
        return Math.max(level, 0);
    }
}
