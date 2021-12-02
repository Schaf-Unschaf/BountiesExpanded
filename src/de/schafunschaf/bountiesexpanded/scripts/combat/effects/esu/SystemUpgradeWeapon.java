package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeWeapon extends StatApplier {
    public SystemUpgradeWeapon(Random random) {
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
                case WEAPON_TURN_RATE:
                    stats.getWeaponTurnRateBonus().modifyMult(id, 1f + modValue);
                    stats.getBeamWeaponTurnRateBonus().modifyMult(id, 1f + modValue);
                    break;
                case WEAPON_RECOIL:
                    stats.getMaxRecoilMult().modifyMult(id, 1f - modValue);
                    stats.getRecoilPerShotMult().modifyMult(id, 1f - modValue);
                    stats.getRecoilDecayMult().modifyMult(id, 1f - modValue);
                    break;
                case BALLISTIC_WEAPON_RANGE:
                    stats.getBallisticWeaponRangeBonus().modifyMult(id, 1f + modValue);
                    break;
                case ENERGY_WEAPON_RANGE:
                    stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f + modValue);
                    break;
                case BALLISTIC_ROF:
                    stats.getBallisticRoFMult().modifyMult(id, 1f + modValue);
                    break;
                case ENERGY_ROF:
                    stats.getEnergyRoFMult().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_RANGE:
                    stats.getMissileWeaponRangeBonus().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_HEALTH:
                    stats.getMissileHealthBonus().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_GUIDANCE:
                    stats.getMissileGuidance().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_RELOAD:
                    stats.getMissileRoFMult().modifyMult(id, 1f + modValue);
                    break;
                case ECCM_CHANCE:
                    stats.getEccmChance().modifyFlat(id, 1f + modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}