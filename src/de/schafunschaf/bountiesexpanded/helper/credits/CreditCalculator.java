package de.schafunschaf.bountiesexpanded.helper.credits;

import com.fs.starfarer.api.Global;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.level.LevelPicker;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

public class CreditCalculator {
    public static int vanillaCalculation(float multiplier) {
        int bountyLevel = LevelPicker.pickLevel(0);
        return calculate(bountyLevel, multiplier);
    }

    public static int vanillaCalculation(int level, float multiplier) {
        return calculate(level, multiplier);
    }

    public static int getRewardByFP(float fleetPoints, float multiplier) {
        return FormattingTools.roundWholeNumber((int) (Settings.BASE_REWARD_PER_FP * fleetPoints * multiplier), 3);
    }

    private static int calculate(int level, float multiplier) {
        float base = Global.getSettings().getFloat("basePersonBounty");
        float perLevel = Global.getSettings().getFloat("personBountyPerLevel");
        float random = perLevel * (int) (Math.random() * 15) / 15f;

        return FormattingTools.roundWholeNumber((int) ((base + perLevel * level + random) * multiplier), 3);
    }
}
