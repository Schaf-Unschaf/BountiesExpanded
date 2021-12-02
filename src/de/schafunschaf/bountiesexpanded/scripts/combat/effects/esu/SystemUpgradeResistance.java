package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.util.HashMap;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu.SystemUpgrades.*;

public class SystemUpgradeResistance extends StatApplier {
    public SystemUpgradeResistance(Random random) {
        categoryType = UpgradeCategories.RESISTANCE;
        upgradeQuality = UpgradeQuality.getRandomQuality(random);
        listOfUpgrades = UpgradeCategories.getRandomUpgrades(categoryType, random);
        upgradeTypeMap = new HashMap<>();
        upgradeCategoriesMap = new HashMap<>();
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, Random random) {
        for (UpgradeTypes upgrade : listOfUpgrades) {
            float modValue = getModValue(random, upgradeQuality, upgrade);
            upgradeTypeMap.put(upgrade, modValue);

            switch (upgrade) {
                case ENERGY_ARMOR_DAMAGE_TAKEN:
                    stats.getEnergyDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case KINETIC_ARMOR_DAMAGE_TAKEN:
                    stats.getKineticArmorDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case EXPLOSIVE_ARMOR_DAMAGE_TAKEN:
                    stats.getHighExplosiveDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case FRAGMENTATION_ARMOR_DAMAGE_TAKEN:
                    stats.getFragmentationDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case ENERGY_SHIELD_DAMAGE_TAKEN:
                    stats.getEnergyShieldDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case KINETIC_SHIELD_DAMAGE_TAKEN:
                    stats.getKineticShieldDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case EXPLOSIVE_SHIELD_DAMAGE_TAKEN:
                    stats.getHighExplosiveShieldDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case FRAGMENTATION_SHIELD_DAMAGE_TAKEN:
                    stats.getFragmentationShieldDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
                case EMP_DAMAGE_TAKEN:
                    stats.getEmpDamageTakenMult().modifyMult(id, 1f - modValue);
                    break;
            }
        }

        saveUpgrades(stats);
    }
}
