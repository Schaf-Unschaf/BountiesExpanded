package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeLogistic extends StatApplier {
    public SystemUpgradeLogistic(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.LOGISTIC;
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
                case CR_PEAK:
                    stats.getPeakCRDuration().modifyMult(id, 1f + modValue);
                    break;
                case CR_MAX:
                    stats.getMaxCombatReadiness().modifyFlat(id, 1f + modValue);
                    break;
                case CR_LOSS:
                    stats.getCRLossPerSecondPercent().modifyMult(id, 1f - modValue);
                    break;
                case CR_DEPLOYMENT:
                    stats.getCRPerDeploymentPercent().modifyMult(id, 1f - modValue);
                    break;
                case WEAPON_MALFUNCTION:
                    stats.getWeaponMalfunctionChance().modifyMult(id, 1f - modValue);
                    break;
                case ENGINE_MALFUNCTION:
                    stats.getEngineMalfunctionChance().modifyMult(id, 1f - modValue);
                    break;
                case SHIELD_MALFUNCTION:
                    stats.getShieldMalfunctionChance().modifyMult(id, 1f - modValue);
                    break;
                case CRITICAL_MALFUNCTION:
                    stats.getCriticalMalfunctionChance().modifyMult(id, 1f - modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}
