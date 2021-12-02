package de.schafunschaf.bountiesexpanded.helper.ship;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.VariantSource;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;
import de.schafunschaf.bountiesexpanded.scripts.combat.hullmods.BountiesExpandedExperimentalSystemUpgrades;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class ShipUtils {
    public static void markMemberForRecovery(FleetMemberAPI fleetMemberToRecover) {
        if (isNull(fleetMemberToRecover)) return;

        List<FleetMemberAPI> singleShipCollection = new ArrayList<>();
        singleShipCollection.add(fleetMemberToRecover);
        markMembersForRecovery(singleShipCollection);
    }

    public static void markMembersForRecovery(Collection<FleetMemberAPI> fleetMembersToRecover) {
        if (isNull(fleetMembersToRecover)) return;
        fleetMembersToRecover.remove(null);
        if (fleetMembersToRecover.isEmpty()) return;

        MemoryAPI memoryAPI = fleetMembersToRecover.iterator().next().getFleetData().getFleet().getMemoryWithoutUpdate();
        memoryAPI.set(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY, fleetMembersToRecover);
    }

    public static void generateShipWithESU() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        ShipVariantAPI variant = playerFleet.getFlagship().getVariant();
        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
        if (variant.isStockVariant() || variant.getSource() != VariantSource.REFIT) {
            variant = variant.clone();
            variant.setOriginalVariant(null);
            variant.setSource(VariantSource.REFIT);
            fleetMember.setVariant(variant, false, false);
        }

        variant.addPermaMod(BountiesExpandedExperimentalSystemUpgrades.ID);
        playerFleet.getFleetData().addFleetMember(fleetMember);
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
}
