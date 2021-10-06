package de.schafunschaf.bountiesexpanded.helper.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

public class SModUpgradeHelper {
    public static void upgradeShip(FleetMemberAPI fleetMember, int numSMods) {
        if (numSMods <= 0)
            return;

        ShipVariantAPI shipVariant = fleetMember.getVariant().clone();
        for (int i = 0; i < numSMods; i++)
            upgradeHullMod(shipVariant);

        fleetMember.setVariant(shipVariant, true, true);
    }

    public static void upgradeHullMod(ShipVariantAPI shipVariant) {
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
                id = getRandomFreeHullMod(shipVariant);

        shipVariant.addPermaMod(id, true);
    }

    public static void addMinorUpgrades(FleetMemberAPI fleetMember) {
        ShipVariantAPI shipVariant = fleetMember.getVariant().clone();

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
            shipVariant.addPermaMod(getRandomFreeHullMod(shipVariant), true);

        if (hasSafetyOverrides && !shipVariant.hasHullMod(HullMods.HARDENED_SUBSYSTEMS))
            shipVariant.addMod(HullMods.HARDENED_SUBSYSTEMS);

        fleetMember.setVariant(shipVariant, true, true);
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
