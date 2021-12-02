package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeSensor extends StatApplier {
    public SystemUpgradeSensor(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.SENSOR;
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
                case SENSOR_STRENGTH:
                    stats.getSensorStrength().modifyMult(id, 1f + modValue);
                    break;
                case SENSOR_PROFILE:
                    stats.getSensorProfile().modifyMult(id, 1f + modValue);
                    break;
                case SIGHT_RADIUS:
                    stats.getSightRadiusMod().modifyMult(id, 1f + modValue);
                    break;
                case AUTOFIRE_ACCURACY:
                    stats.getAutofireAimAccuracy().modifyMult(id, 1f + modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}