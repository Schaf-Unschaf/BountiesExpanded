package de.schafunschaf.bountiesexpanded.helper.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;

import java.util.*;

public class SModUpgradeHelper {
    public static void upgradeShip(FleetMemberAPI fleetMember, int numSMods, Random random) {
        if (!fleetMember.getVariant().getSMods().isEmpty())
            return;

        if (numSMods <= 0)
            return;

        if (ComparisonTools.isNull(random))
            random = new Random();

        ShipVariantAPI shipVariant = fleetMember.getVariant();
        for (int i = 0; i < numSMods; i++)
            upgradeHullMod(shipVariant, random);

        fleetMember.setVariant(shipVariant, true, true);
    }

    public static void upgradeHullMod(ShipVariantAPI shipVariant, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();
        Map<String, Integer> hullModsWithOP = new HashMap<>();
        boolean hasSafetyOverrides = false;

        for (String hullModId : shipVariant.getHullMods()) {
            if (hullModId.equals(HullMods.SAFETYOVERRIDES))
                hasSafetyOverrides = true;

            HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(hullModId);
            if (hullMod.hasTag(Tags.HULLMOD_NO_BUILD_IN) || hullMod.isHidden() || hasModBuiltIn(shipVariant, hullModId))
                continue;

            switch (shipVariant.getHullSpec().getHullSize()) {
                case FRIGATE:
                    hullModsWithOP.put(hullModId, hullMod.getFrigateCost());
                    break;
                case DESTROYER:
                    hullModsWithOP.put(hullModId, hullMod.getDestroyerCost());
                    break;
                case CRUISER:
                    hullModsWithOP.put(hullModId, hullMod.getCruiserCost());
                    break;
                case CAPITAL_SHIP:
                    hullModsWithOP.put(hullModId, hullMod.getCapitalCost());
                    break;
            }
        }

        String id = "";
        int opCost = 0;
        for (Map.Entry<String, Integer> hullModSet : hullModsWithOP.entrySet()) {
            if (opCost < hullModSet.getValue()) {
                id = hullModSet.getKey();
                opCost = hullModSet.getValue();
            }
        }

        if (id.isEmpty())
            if (!hasSafetyOverrides && Misc.getSizeNum(shipVariant.getHullSize()) >= 3f && !hasModBuiltIn(shipVariant, HullMods.INTEGRATED_TARGETING_UNIT))
                id = HullMods.INTEGRATED_TARGETING_UNIT;
            else if (!hasModBuiltIn(shipVariant, HullMods.HARDENED_SHIELDS))
                id = HullMods.HARDENED_SHIELDS;
            else if (!hasModBuiltIn(shipVariant, HullMods.HEAVYARMOR))
                id = HullMods.HEAVYARMOR;
            else
                id = getRandomFreeSMod(shipVariant, random);

        shipVariant.addPermaMod(id, true);
    }

    public static void addMinorUpgrades(FleetMemberAPI fleetMember, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();
        ShipVariantAPI shipVariant = fleetMember.getVariant();

        boolean hasSafetyOverrides = false;

        for (String hullModId : shipVariant.getHullMods()) {
            if (hullModId.equals(HullMods.SAFETYOVERRIDES)) {
                hasSafetyOverrides = true;
                break;
            }
        }

        if (!hasModBuiltIn(shipVariant, HullMods.REINFORCEDHULL))
            shipVariant.addPermaMod(HullMods.REINFORCEDHULL, true);
        else
            shipVariant.addPermaMod(getRandomFreeSMod(shipVariant, random), true);

        if (hasSafetyOverrides && !shipVariant.hasHullMod(HullMods.HARDENED_SUBSYSTEMS))
            shipVariant.addMod(HullMods.HARDENED_SUBSYSTEMS);

        fleetMember.setVariant(shipVariant, true, true);
    }

    public static void addRandomSMods(FleetMemberAPI fleetMember, int numSMods, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();
        ShipVariantAPI shipVariant = fleetMember.getVariant();
        int preUpgradeSModsAmount = shipVariant.getSMods().size();

        do
            shipVariant.addPermaMod(getRandomFreeHullMod(shipVariant, random), true);
        while ((shipVariant.getSMods().size() - preUpgradeSModsAmount) < numSMods);

        fleetMember.setVariant(shipVariant, true, true);
    }

    public static boolean hasModBuiltIn(ShipVariantAPI shipVariant, String hullModId) {
        return shipVariant.getPermaMods().contains(hullModId) || shipVariant.getHullSpec().getBuiltInMods().contains(hullModId);
    }

    public static boolean hasWeaponRangeMod(ShipVariantAPI shipVariant, String hullModId) {
        Set<String> permaMods = shipVariant.getPermaMods();
        List<String> builtInMods = shipVariant.getHullSpec().getBuiltInMods();
        if (hullModId.equals("advancedcore") && permaMods.contains(HullMods.INTEGRATED_TARGETING_UNIT))
            return true;
        if (hullModId.equals("advancedcore") && builtInMods.contains(HullMods.INTEGRATED_TARGETING_UNIT))
            return true;
        if (hullModId.equals(HullMods.INTEGRATED_TARGETING_UNIT) && permaMods.contains("advancedcore"))
            return true;
        if (hullModId.equals(HullMods.INTEGRATED_TARGETING_UNIT) && builtInMods.contains("advancedcore"))
            return true;

        return false;
    }

    public static String getRandomFreeSMod(ShipVariantAPI shipVariant, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();
        List<String> hullMods = new ArrayList<>(Arrays.asList(HullMods.ARMOREDWEAPONS,
                HullMods.ADVANCEDOPTICS,
                HullMods.ACCELERATED_SHIELDS,
                HullMods.ARMOREDWEAPONS,
                HullMods.AUTOREPAIR,
                HullMods.AUXILIARY_THRUSTERS,
                HullMods.BLAST_DOORS,
                HullMods.ECCM,
                HullMods.ECM,
                HullMods.FLUXBREAKERS,
                HullMods.FLUX_COIL,
                HullMods.FLUX_DISTRIBUTOR,
                HullMods.HARDENED_SUBSYSTEMS,
                HullMods.HARDENED_SHIELDS,
                HullMods.HEAVYARMOR,
                HullMods.INSULATEDENGINE,
                HullMods.POINTDEFENSEAI,
                HullMods.REINFORCEDHULL,
                HullMods.SOLAR_SHIELDING,
                HullMods.STABILIZEDSHIELDEMITTER,
                HullMods.TURRETGYROS,
                HullMods.UNSTABLE_INJECTOR));

        removeInvalidMods(shipVariant, hullMods);

        for (int i = 0; i < hullMods.size(); i++) {
            String selectedHullMod = hullMods.get(random.nextInt(hullMods.size()));
            if (!hasModBuiltIn(shipVariant, selectedHullMod))
                return selectedHullMod;
        }
        return null;
    }

    private static void removeInvalidMods(ShipVariantAPI shipVariant, List<String> hullMods) {
        if (shipVariant.getHullSpec().isPhase()) {
            hullMods.remove(HullMods.HARDENED_SHIELDS);
            hullMods.remove(HullMods.ACCELERATED_SHIELDS);
            hullMods.remove(HullMods.EXTENDED_SHIELDS);
            hullMods.remove(HullMods.FRONT_SHIELD_CONVERSION);
            hullMods.remove(HullMods.OMNI_SHIELD_CONVERSION);
            hullMods.remove(HullMods.STABILIZEDSHIELDEMITTER);
        }

    }

    public static String getRandomFreeHullMod(ShipVariantAPI shipVariant, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();
        List<String> hullMods = new ArrayList<>(Arrays.asList(
                HullMods.OMNI_SHIELD_CONVERSION,
                "advancedcore",
                HullMods.ADVANCEDOPTICS,
                HullMods.ACCELERATED_SHIELDS,
                HullMods.ARMOREDWEAPONS,
                HullMods.AUTOREPAIR,
                HullMods.AUXILIARY_THRUSTERS,
                HullMods.BLAST_DOORS,
                HullMods.ECCM,
                HullMods.ECM,
                HullMods.EFFICIENCY_OVERHAUL,
                HullMods.EXTENDED_SHIELDS,
                HullMods.FLUXBREAKERS,
                HullMods.FLUX_COIL,
                HullMods.FLUX_DISTRIBUTOR,
                HullMods.FRONT_SHIELD_CONVERSION,
                HullMods.HARDENED_SUBSYSTEMS,
                HullMods.HARDENED_SHIELDS,
                HullMods.HEAVYARMOR,
                "high_scatter_amp",
                HullMods.INSULATEDENGINE,
                HullMods.MAGAZINES,
                "missile_reload",
                HullMods.MISSLERACKS,
                HullMods.NAV_RELAY,
                HullMods.OPERATIONS_CENTER,
                HullMods.POINTDEFENSEAI,
                HullMods.REINFORCEDHULL,
                HullMods.SAFETYOVERRIDES,
                HullMods.SOLAR_SHIELDING,
                HullMods.STABILIZEDSHIELDEMITTER,
                HullMods.SURVEYING_EQUIPMENT,
                HullMods.INTEGRATED_TARGETING_UNIT,
                HullMods.TURRETGYROS,
                HullMods.UNSTABLE_INJECTOR
        ));

        removeInvalidMods(shipVariant, hullMods);

        for (int i = 0; i < hullMods.size(); i++) {
            String selectedHullMod = hullMods.get(random.nextInt(hullMods.size()));
            if (!hasModBuiltIn(shipVariant, selectedHullMod) && !hasWeaponRangeMod(shipVariant, selectedHullMod))
                return selectedHullMod;
        }
        return null;
    }

}
