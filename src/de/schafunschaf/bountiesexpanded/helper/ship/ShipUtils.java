package de.schafunschaf.bountiesexpanded.helper.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.NoShipRecoveryFleetEncounterContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class ShipUtils {
    public static void markMemberForRecovery(FleetMemberAPI fleetMemberToRecover) {
        if (isNull(fleetMemberToRecover)) return;

        List<FleetMemberAPI> singleShipCollection = new ArrayList<>();
        singleShipCollection.add(fleetMemberToRecover);
        markShipsForRecovery(singleShipCollection);
    }

    public static void markShipsForRecovery(Collection<FleetMemberAPI> shipsToRecover) {
        if (isNull(shipsToRecover)) return;
        shipsToRecover.remove(null);
        if (shipsToRecover.isEmpty()) return;

        MemoryAPI memoryAPI = shipsToRecover.iterator().next().getFleetData().getFleet().getMemoryWithoutUpdate();
        memoryAPI.set(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY, shipsToRecover);
    }

    public static void markShipsAsUnrecoverable(@NotNull FleetMemberAPI... shipsWithoutRecovery) {
        List<FleetMemberAPI> fleetMembers = Arrays.asList(shipsWithoutRecovery);

        CampaignFleetAPI fleet = fleetMembers.get(0).getFleetData().getFleet();
        fleet.getMemoryWithoutUpdate().set(NoShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_NO_RECOVERY, shipsWithoutRecovery);
    }

    public static FleetMemberAPI findMemberForStats(MutableShipStatsAPI stats) {
        if (isNotNull(stats.getFleetMember()))
            return stats.getFleetMember();

        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if (isNotNull(ship.getFleetMember()))
                return ship.getFleetMember();
        }

        return searchFleetForStats(Global.getSector().getPlayerFleet(), stats);
    }

    private static FleetMemberAPI searchFleetForStats(CampaignFleetAPI fleet, MutableShipStatsAPI stats) {
        if (isNull(fleet))
            return null;

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isFighterWing())
                continue;

            if (member.getStats() == stats)
                return member;
            else if (isNotNull(stats.getEntity()) && member.getStats().getEntity() == stats.getEntity())
                return member;
            else if (isNotNull(stats.getFleetMember()) && (stats.getFleetMember() == member || member.getStats().getFleetMember() == stats.getFleetMember()))
                return member;
            else if (isNotNull(stats.getVariant()) && member.getVariant() == stats.getVariant())
                return member;
            else if (isNotNull(member.getVariant().getStatsForOpCosts()))
                if (member.getVariant().getStatsForOpCosts() == stats)
                    return member;
                else if (isNotNull(stats.getEntity()) && member.getVariant().getStatsForOpCosts().getEntity() == stats.getEntity())
                    return member;
                else if (isNotNull(stats.getFleetMember()) && member.getVariant().getStatsForOpCosts().getFleetMember() == stats.getFleetMember())
                    return member;
                else if (isNotNull(stats.getVariant()) && member.getVariant().getStatsForOpCosts().getVariant() == stats.getVariant())
                    return member;

            ShipVariantAPI shipVariant = member.getVariant();
            for (String moduleVariantId : shipVariant.getStationModules().keySet()) {
                ShipVariantAPI moduleVariant = shipVariant.getModuleVariant(moduleVariantId);

                if (isNotNull(moduleVariant.getStatsForOpCosts())) if (moduleVariant.getStatsForOpCosts() == stats)
                    return member;
                else if (isNotNull(stats.getEntity()) && stats.getEntity() == moduleVariant.getStatsForOpCosts().getEntity())
                    return member;
                else if (isNotNull(stats.getFleetMember()) && moduleVariant.getStatsForOpCosts().getFleetMember() == stats.getFleetMember())
                    return member;
            }
        }

        return null;
    }

    public static void upgradeShip(FleetMemberAPI fleetMember, int numSMods, Random random) {
        if (!fleetMember.getVariant().getSMods().isEmpty())
            return;

        if (numSMods <= 0)
            return;

        if (isNull(random))
            random = new Random();

        ShipVariantAPI shipVariant = fleetMember.getVariant();
        for (int i = 0; i < numSMods; i++)
            HullModUtils.upgradeHullMod(shipVariant, random);

        fleetMember.setVariant(shipVariant, true, true);
    }

    public static void addMinorUpgrades(FleetMemberAPI fleetMember, Random random) {
        if (isNull(random))
            random = new Random();
        ShipVariantAPI shipVariant = fleetMember.getVariant();

        boolean hasSafetyOverrides = false;

        for (String hullModId : shipVariant.getHullMods()) {
            if (hullModId.equals(HullMods.SAFETYOVERRIDES)) {
                hasSafetyOverrides = true;
                break;
            }
        }

        if (!HullModUtils.hasModBuiltIn(shipVariant, HullMods.REINFORCEDHULL))
            shipVariant.addPermaMod(HullMods.REINFORCEDHULL, true);
        else
            shipVariant.addPermaMod(HullModUtils.getRandomFreeSMod(shipVariant, random), true);

        if (hasSafetyOverrides && !shipVariant.hasHullMod(HullMods.HARDENED_SUBSYSTEMS))
            shipVariant.addMod(HullMods.HARDENED_SUBSYSTEMS);

        fleetMember.setVariant(shipVariant, true, true);
    }
}
