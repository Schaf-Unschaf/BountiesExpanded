package de.schafunschaf.bountiesexpanded.helper.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class SModUpgradeHelper {
    public static void upgradeShip(FleetMemberAPI fleetMember) {
        ShipVariantAPI shipVariant = fleetMember.getVariant().clone();
        replaceAndEnhanceHullMods(shipVariant);
        fleetMember.setVariant(shipVariant, true, true);
    }

    public static void replaceAndEnhanceHullMods(ShipVariantAPI shipVariant) {
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

        String id1 = "";
        String id2 = "";
        int opCost1 = 0;
        int opCost2 = 0;
        for (Map.Entry<String, Integer> hullModSet : hullModsWithOP.entrySet()) {
            if (opCost1 < hullModSet.getValue()) {
                id2 = id1;
                opCost2 = opCost1;
                id1 = hullModSet.getKey();
                opCost1 = hullModSet.getValue();
            } else if (opCost2 < hullModSet.getValue()) {
                id2 = hullModSet.getKey();
                opCost2 = hullModSet.getValue();
            }
        }

        if (id1.isEmpty())
            if (!hasSafetyOverrides && Misc.getSizeNum(shipVariant.getHullSize()) >= 3f && !hasModBuiltIn(shipVariant, HullMods.INTEGRATED_TARGETING_UNIT))
                id1 = HullMods.INTEGRATED_TARGETING_UNIT;
            else if (!hasModBuiltIn(shipVariant, HullMods.HARDENED_SHIELDS))
                id1 = HullMods.HARDENED_SHIELDS;
            else if (!hasModBuiltIn(shipVariant, HullMods.HEAVYARMOR))
                id1 = HullMods.HEAVYARMOR;
            else
                id1 = getRandomFreeHullMod(shipVariant);

        shipVariant.addPermaMod(id1, true);

        if (id2.isEmpty())
            if (!isNull(id1) && !id1.equals(HullMods.INTEGRATED_TARGETING_UNIT) && !hasSafetyOverrides && Misc.getSizeNum(shipVariant.getHullSize()) >= 3f && !hasModBuiltIn(shipVariant, HullMods.INTEGRATED_TARGETING_UNIT))
                id2 = HullMods.INTEGRATED_TARGETING_UNIT;
            else if (!isNull(id1) && !id1.equals(HullMods.HARDENED_SHIELDS) && !hasModBuiltIn(shipVariant, HullMods.HARDENED_SHIELDS))
                id2 = HullMods.HARDENED_SHIELDS;
            else if (!hasModBuiltIn(shipVariant, HullMods.HEAVYARMOR))
                id2 = HullMods.HEAVYARMOR;
            else
                id2 = getRandomFreeHullMod(shipVariant);

        shipVariant.addPermaMod(id2, true);

        if (!hasModBuiltIn(shipVariant, HullMods.REINFORCEDHULL) && !isNull(id1) && !id1.equals(HullMods.REINFORCEDHULL) || !isNull(id2) && id2.equals(HullMods.REINFORCEDHULL))
            shipVariant.addPermaMod(HullMods.REINFORCEDHULL, true);
        else
            shipVariant.addPermaMod(getRandomFreeHullMod(shipVariant), true);

        if (hasSafetyOverrides)
            shipVariant.addMod(HullMods.HARDENED_SUBSYSTEMS);
    }

    public static boolean hasModBuiltIn(ShipVariantAPI shipVariant, String hullModId) {
        return shipVariant.getPermaMods().contains(hullModId) || shipVariant.getHullSpec().getBuiltInMods().contains(hullModId);
    }

    public static String getRandomFreeHullMod(ShipVariantAPI shipVariant) {
        List<String> hullMods = new ArrayList<>(Arrays.asList(HullMods.ARMOREDWEAPONS, HullMods.AUTOREPAIR, HullMods.AUXILIARY_THRUSTERS, HullMods.BLAST_DOORS, HullMods.ECM, HullMods.FLUX_COIL, HullMods.FLUX_DISTRIBUTOR, HullMods.HARDENED_SHIELDS, HullMods.HEAVYARMOR, HullMods.INSULATEDENGINE, HullMods.POINTDEFENSEAI, HullMods.REINFORCEDHULL, HullMods.SOLAR_SHIELDING, HullMods.STABILIZEDSHIELDEMITTER));
        Collections.shuffle(hullMods);
        for (String hullMod : hullMods) {
            if (!hasModBuiltIn(shipVariant, hullMod)) {
                return hullMod;
            }
        }
        return null;
    }
}
