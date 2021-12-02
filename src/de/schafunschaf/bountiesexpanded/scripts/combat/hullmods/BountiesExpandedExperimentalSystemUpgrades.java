package de.schafunschaf.bountiesexpanded.scripts.combat.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu.SystemUpgrades.*;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedExperimentalSystemUpgrades extends BaseHullMod {
    public static final String ID = "bountiesExpanded_experimentalSystems";
    private static final Map<String, Map<UpgradeCategories, Map<UpgradeTypes, Float>>> upgradeMap = new HashMap<>();

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI ship = stats.getFleetMember();
        if (isNull(ship))
            return;

        if (!upgradeMap.containsKey(ship.getId()))
            upgradeMap.put(ship.getId(), new HashMap<UpgradeCategories, Map<UpgradeTypes, Float>>());

        Random random = new Random(ship.getId().hashCode());

        for (StatApplier category : getAllCategories(random))
            category.apply(stats, id, random);

//        WeightedRandomPicker<StatApplier> picker = new WeightedRandomPicker<>();

    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        generateDescriptions(tooltip, ship);
    }

    @Override
    public Color getBorderColor() {
        return new Color(255, 150, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(255, 150, 0);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
    }

    public static void addShipUpgrades(String fleetMemberID, Map<UpgradeCategories, Map<UpgradeTypes, Float>> upgrades) {
        Map<UpgradeCategories, Map<UpgradeTypes, Float>> shipUpgrades = upgradeMap.get(fleetMemberID);
        shipUpgrades.putAll(upgrades);
        upgradeMap.put(fleetMemberID, shipUpgrades);
    }

    public static Map<UpgradeCategories, Map<UpgradeTypes, Float>> getUpgradesForShip(String fleetMemberID) {
        return upgradeMap.get(fleetMemberID);
    }

    private void generateDescriptions(TooltipMakerAPI tooltip, ShipAPI ship) {
        Map<UpgradeCategories, Map<UpgradeTypes, Float>> fleetMemberUpgrades = upgradeMap.get(ship.getFleetMemberId());

        for (UpgradeCategories upgradeCategories : fleetMemberUpgrades.keySet()) {
            Map<UpgradeTypes, Float> upgradesInCategory = fleetMemberUpgrades.get(upgradeCategories);
            tooltip.addSectionHeading(upgradeCategories.name(), Alignment.MID, 3f);
            for (UpgradeTypes upgrade : upgradesInCategory.keySet()) {
                Float value = upgradesInCategory.get(upgrade);
                tooltip.addPara(upgrade.name() + " " + value, 0f);
            }
        }


//        for (UpgradeCategories upgradeType : upgradeList) {
//            int statValueOne = getRandomValue(upgradeType, 0, random);
//            int statValueTwo = getRandomValue(upgradeType, 1, random);
//            int statValueThree = getRandomValue(upgradeType, 2, random);
//            String valueStringOne = appendPercentageSign(upgradeType, statValueOne, 0);
//            String valueStringTwo = appendPercentageSign(upgradeType, statValueTwo, 1);
//            String valueStringThree = appendPercentageSign(upgradeType, statValueThree, 2);
//            Color titleColor = upgradeType.titleColor;
//
//            generateTooltip(tooltip, upgradeType, titleColor, 3f, valueStringOne, valueStringTwo, valueStringThree);
//        }
    }

//    private String appendPercentageSign(UpgradeCategories upgradeType, int statValueOne, int index) {
//        Integer fraction = upgradeType.fraction.get(index);
//        String appendedSign = fraction != 1 ? "" : "%";
//
//        return statValueOne + appendedSign;
//    }

    private void generateTooltip(TooltipMakerAPI tooltip, UpgradeCategories upgradeType, Color hlColor, float padding, String... values) {
        String bullet = "   • ";
//        tooltip.addPara(upgradeType.name, hlColor, padding);
//        tooltip.addPara(upgradeType.description, padding, Misc.getPositiveHighlightColor(), values);
    }
}
