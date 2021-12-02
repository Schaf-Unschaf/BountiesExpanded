package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

public class SystemUpgradeProjectile extends StatApplier {
    public SystemUpgradeProjectile(Random random) {
        categoryType = SystemUpgrades.UpgradeCategories.PROJECTILE;
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
                case BALLISTIC_DAMAGE:
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1f + modValue);
                    break;
                case ENERGY_DAMAGE:
                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + modValue);
                    break;
                case BEAM_DAMAGE:
                    stats.getBeamWeaponDamageMult().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_DAMAGE:
                    stats.getMissileWeaponDamageMult().modifyMult(id, 1f + modValue);
                    break;
                case HIT_STRENGTH:
                    stats.getHitStrengthBonus().modifyMult(id, 1f + modValue);
                    break;
                case SHIELD_DAMAGE:
                    stats.getShieldDamageTakenMult().modifyMult(id, 1f + modValue);
                    break;
                case ENERGY_FLUX_COST:
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - modValue);
                    break;
                case BALLISTIC_FLUX_COST:
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - modValue);
                    break;
                case PROJECTILE_SPEED:
                    stats.getProjectileSpeedMult().modifyMult(id, 1f + modValue);
                    break;
                case MISSILE_SPEED:
                    stats.getMissileMaxSpeedBonus().modifyMult(id, 1f + modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}
