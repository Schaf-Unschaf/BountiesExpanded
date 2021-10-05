package de.schafunschaf.bountiesexpanded.helper.credits;

import com.fs.starfarer.api.Global;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.level.LevelPicker;

public class CreditCalculator {
    public static int vanillaCalculation(float multiplier) {
        int bountyLevel = LevelPicker.pickLevel(0);
        return calculate(bountyLevel, multiplier);
    }

    public static int vanillaCalculation(int level, float multiplier) {
        return calculate(level, multiplier);
    }

    public static int getRewardByFP(float fleetPoints, float multiplier) {
        return Math.round((int) (Settings.BASE_REWARD_PER_FP * fleetPoints * multiplier / 1000)) * 1000;
    }

    private static int calculate(int level, float multiplier) {
        float base = Global.getSettings().getFloat("basePersonBounty");
        float perLevel = Global.getSettings().getFloat("personBountyPerLevel");
        float random = perLevel * (int) (Math.random() * 15) / 15f;

        return Math.round((int) ((base + perLevel * level + random) * multiplier) / 1000) * 1000;
    }
}
