package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu.SystemUpgrades.*;

public class SystemUpgradeEngines extends StatApplier {
    public SystemUpgradeEngines(Random random) {
        categoryType = UpgradeCategories.ENGINE;
        upgradeQuality = SystemUpgrades.UpgradeQuality.getRandomQuality(random);
        listOfUpgrades = SystemUpgrades.UpgradeCategories.getRandomUpgrades(categoryType, random);
        upgradeTypeMap = new HashMap<>();
        upgradeCategoriesMap = new HashMap<>();
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, Random random) {
        for (UpgradeTypes upgrade : listOfUpgrades) {
            float modValue = getModValue(random, upgradeQuality, upgrade);
            upgradeTypeMap.put(upgrade, modValue);

            switch (upgrade) {
                case MAX_SPEED:
                    stats.getMaxSpeed().modifyMult(id, 1f + modValue);
                    break;
                case ACCELERATION:
                    stats.getAcceleration().modifyMult(id, 1f + modValue);
                    stats.getDeceleration().modifyMult(id, 1f + modValue);
                    break;
                case TURN_RATE:
                    stats.getTurnAcceleration().modifyMult(id, 1f + modValue * 2);
                    break;
                case ZERO_FLUX_SPEED:
                    stats.getZeroFluxSpeedBoost().modifyFlat(id, 1f + modValue);
                    break;
                case BURN_LEVEL:
                    stats.getMaxBurnLevel().modifyFlat(id, 1f + modValue);
                    break;
                case FUEL_USE:
                    stats.getFuelUseMod().modifyMult(id, 1f - modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}
