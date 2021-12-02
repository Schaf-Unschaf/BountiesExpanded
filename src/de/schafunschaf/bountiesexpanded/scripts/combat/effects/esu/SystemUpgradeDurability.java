package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeDurability extends StatApplier {
    public SystemUpgradeDurability(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.DURABILITY;
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
                case ARMOR_BONUS:
                    stats.getArmorBonus().modifyMult(id, 1f + modValue);
                    break;
                case HULL_BONUS:
                    stats.getHullBonus().modifyMult(id, 1f + modValue);
                    break;
                case ENGINE_HEALTH:
                    stats.getEngineHealthBonus().modifyMult(id, 1f + modValue);
                    break;
                case WEAPON_HEALTH:
                    stats.getWeaponHealthBonus().modifyMult(id, 1f + modValue);
                    break;
                case ENGINE_REPAIR:
                    stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - modValue);
                    break;
                case WEAPON_REPAIR:
                    stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}