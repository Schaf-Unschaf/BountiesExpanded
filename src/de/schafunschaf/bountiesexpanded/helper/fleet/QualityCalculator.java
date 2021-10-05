package de.schafunschaf.bountiesexpanded.helper.fleet;

public class QualityCalculator {
    public static float vanillaCalculation(int level) {
        float quality = (float) level / 10f;
        if (quality > 1) quality = 1;
        return quality;
    }
}
