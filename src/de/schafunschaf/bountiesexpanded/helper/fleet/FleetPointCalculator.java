package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.Global;

public class FleetPointCalculator {
    public static float getPlayerBasedFP(float modifier) {
        float fleetPoints = Global.getSector().getPlayerFleet().getFleetPoints() * modifier;
        fleetPoints *= 1f + (float) Math.random() * 0.15f;
        return fleetPoints;
    }

    public static float vanillaCalculation(int level) {
        float fleetPoints = (5 + level * 5) * 5f;
        fleetPoints *= 0.75f + (float) Math.random() * 0.25f;

        if (level >= 7) {
            fleetPoints += 20f;
        }
        if (level >= 8) {
            fleetPoints += 30f;
        }
        if (level >= 9) {
            fleetPoints += 50f;
        }
        if (level >= 10) {
            fleetPoints += 50f;
        }

        return fleetPoints;
    }
}
