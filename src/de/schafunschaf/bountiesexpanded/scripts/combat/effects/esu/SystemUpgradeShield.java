package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeShield extends StatApplier {
    public SystemUpgradeShield(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.SHIELD;
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
                case SHIELD_UPKEEP:
                    stats.getShieldUpkeepMult().modifyMult(id, 1f - modValue);
                    break;
                case SHIELD_ABSORPTION:
                    stats.getShieldAbsorptionMult().modifyFlat(id, -modValue);
                    break;
                case SHIELD_TURN_RATE:
                    stats.getShieldTurnRateMult().modifyMult(id, 1f + modValue);
                    break;
                case SHIELD_UNFOLD:
                    stats.getShieldUnfoldRateMult().modifyMult(id, 1f + modValue);
                    break;
                case SHIELD_ARC:
                    stats.getShieldArcBonus().modifyMult(id, 1f + modValue);
                    break;

            }
        }

        saveUpgrades(stats);
    }
}
