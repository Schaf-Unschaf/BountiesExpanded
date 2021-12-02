package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeFlux extends StatApplier {
    public SystemUpgradeFlux(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.FLUX;
        upgradeQuality = SystemUpgrades.UpgradeQuality.getRandomQuality(random);
        listOfUpgrades = SystemUpgrades.UpgradeCategories.getRandomUpgrades(categoryType, random);
        upgradeTypeMap = new HashMap<>();
        upgradeCategoriesMap = new HashMap<>();
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, Random random) {
        for (SystemUpgrades.UpgradeTypes upgrade : listOfUpgrades) {
            float modValue = getModValue(random, upgradeQuality, upgrade);
            upgradeTypeMap.put(upgrade, modValue);

            switch (upgrade) {
                case FLUX_CAPACITY:
                    stats.getFluxCapacity().modifyMult(id, 1f + modValue);
                    break;
                case FLUX_DISSIPATION:
                    stats.getFluxDissipation().modifyMult(id, 1f + modValue);
                    break;
                case HARD_FLUX_DISSIPATION:
                    stats.getHardFluxDissipationFraction().modifyFlat(id, 1f + modValue);
                    break;
                case VENT_RATE:
                    stats.getVentRateMult().modifyMult(id, 1f + modValue);
                    break;
                case ZERO_FLUX_LIMIT:
                    stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 1f + modValue);
                    break;
                case OVERLOAD_DURATION:
                    stats.getOverloadTimeMod().modifyMult(id, 1f - modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}
