package de.schafunschaf.bountiesexpanded.scripts.combat.effects;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu.SystemUpgrades;
import de.schafunschaf.bountiesexpanded.scripts.combat.hullmods.BountiesExpandedExperimentalSystemUpgrades;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class StatApplier {
    protected SystemUpgrades.UpgradeCategories categoryType;
    protected SystemUpgrades.UpgradeQuality upgradeQuality;
    protected List<SystemUpgrades.UpgradeTypes> listOfUpgrades;
    protected Map<SystemUpgrades.UpgradeTypes, Float> upgradeTypeMap;
    protected Map<SystemUpgrades.UpgradeCategories, Map<SystemUpgrades.UpgradeTypes, Float>> upgradeCategoriesMap;

    public abstract void apply(MutableShipStatsAPI stats, String id, Random random);

    protected float getModValue(Random random, SystemUpgrades.UpgradeQuality upgradeQuality, SystemUpgrades.UpgradeTypes upgrade) {
        boolean isNegativeStat = random.nextFloat() <= upgradeQuality.getChanceForNegativeStats();
        float modValue = random.nextInt((int) (upgrade.getMaxValue() * 100)) + 1;
        modValue = isNegativeStat ? -modValue / 100 : modValue * upgradeQuality.getQualityModifier() / 100;
        return modValue;
    }

    protected void saveUpgrades(MutableShipStatsAPI stats) {
        upgradeCategoriesMap.put(categoryType, upgradeTypeMap);
        BountiesExpandedExperimentalSystemUpgrades.addShipUpgrades(stats.getFleetMember().getId(), upgradeCategoriesMap);
    }
}
