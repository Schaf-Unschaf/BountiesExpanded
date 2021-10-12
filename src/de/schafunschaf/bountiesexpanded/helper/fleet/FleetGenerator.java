package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

public class FleetGenerator {
    public static final Logger log = Global.getLogger(FleetGenerator.class);

    @Deprecated
    public static CampaignFleetAPI createAndSpawnFleet(float fleetPoints,
                                                       float qualityOverride,
                                                       MarketAPI fleetHomeMarket,
                                                       SectorEntityToken hideout,
                                                       PersonAPI fleetCaptain) {
        CampaignFleetAPI fleet = createCombatFleet(fleetPoints, qualityOverride, fleetHomeMarket, hideout, fleetCaptain);
        spawnFleet(fleet, hideout);
        return fleet;
    }

    public static CampaignFleetAPI createBountyFleetV2(float fleetPoints,
                                                       float qualityOverride,
                                                       MarketAPI fleetHomeMarket,
                                                       SectorEntityToken hideout,
                                                       PersonAPI fleetCaptain) {
        Random random = new Random();
        FactionAPI faction = fleetCaptain.getFaction();
        String factionID = faction.getId();
        String fleetName = fleetCaptain.getName().getLast() + "'s Fleet";
        float initialFP = fleetPoints * 0.7f;
        float remainingFP = fleetPoints - initialFP;

        FleetParamsV3 fleetParams = new FleetParamsV3(fleetHomeMarket,
                hideout.getLocationInHyperspace(),
                factionID, // factionID
                qualityOverride + 0.2f, // quality
                FleetTypes.PERSON_BOUNTY_FLEET, // fleetType
                initialFP, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        fleetParams.ignoreMarketFleetSizeMult = true;
        if (fleetPoints < 400)
            fleetParams.maxNumShips = 40;
        if (fleetPoints < 500)
            fleetParams.maxNumShips = 45;
        if (fleetPoints < 600)
            fleetParams.maxNumShips = 50;
        if (fleetPoints >= 600)
            fleetParams.maxNumShips = 65;

        FactionDoctrineAPI doctrine = faction.getDoctrine();

        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(factionID, FleetTypes.PERSON_BOUNTY_FLEET, fleetHomeMarket);
        CampaignFleetAPI mainForces = FleetFactoryV3.createFleet(fleetParams);
        FleetDataAPI fleetData = fleet.getFleetData();
        for (FleetMemberAPI fleetMember : mainForces.getFleetData().getMembersListCopy())
            fleetData.addFleetMember(fleetMember);

        // Support Forces to avoid Capital bloat
        FleetFactoryV3.addFleetPoints(fleet, random, remainingFP / 7 * doctrine.getWarships(), fleetParams, FleetFactoryV3.SizeFilterMode.NONE, ShipRoles.COMBAT_MEDIUM, ShipRoles.COMBAT_MEDIUM, ShipRoles.COMBAT_LARGE);
        FleetFactoryV3.addFleetPoints(fleet, random, remainingFP / 7 * doctrine.getCarriers(), fleetParams, FleetFactoryV3.SizeFilterMode.NONE, ShipRoles.CARRIER_SMALL, ShipRoles.CARRIER_MEDIUM, ShipRoles.CARRIER_LARGE);
        FleetFactoryV3.addFleetPoints(fleet, random, remainingFP / 7 * doctrine.getPhaseShips(), fleetParams, FleetFactoryV3.SizeFilterMode.NONE, ShipRoles.PHASE_MEDIUM, ShipRoles.PHASE_LARGE, ShipRoles.PHASE_CAPITAL);

        fleet.setCommander(fleetCaptain);
        if (isNotNull(fleet.getFlagship())) {
            FleetMemberAPI flagship = getShipWithHighestFP(fleetData.getMembersListCopy());
            flagship.setFlagship(true);
            fleet.getFlagship().setCaptain(fleetCaptain);
        }
        fleet.setFaction(factionID, true);
        fleet.setName(fleetName);
        FleetFactoryV3.addCommanderSkills(fleetCaptain, fleet, null);
        fleetData.sort();

        return fleet;
    }

    public static CampaignFleetAPI createCombatFleet(float fleetPoints,
                                                     float qualityOverride,
                                                     MarketAPI fleetHomeMarket,
                                                     SectorEntityToken hideout,
                                                     PersonAPI fleetCaptain) {
        String factionID = fleetCaptain.getFaction().getId();
        String fleetName = fleetCaptain.getName().getLast() + "'s Fleet";
        float freighterPoints = fleetPoints * 0.05f;
        float tankerPoints = fleetPoints * 0.05f;

        FleetParamsV3 fleetParams = new FleetParamsV3(fleetHomeMarket,
                hideout.getLocationInHyperspace(),
                factionID, // factionID
                qualityOverride + 0.2f, // quality
                FleetTypes.PERSON_BOUNTY_FLEET, // fleetType
                fleetPoints, // combatPts
                freighterPoints, // freighterPts
                tankerPoints, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        fleetParams.ignoreMarketFleetSizeMult = true;
        fleetParams.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(fleetParams);

        fleet.setCommander(fleetCaptain);
        if (isNotNull(fleet.getFlagship()))
            fleet.getFlagship().setCaptain(fleetCaptain);
        fleet.setFaction(factionID, true);
        fleet.setName(fleetName);
        FleetFactoryV3.addCommanderSkills(fleetCaptain, fleet, null);

        return fleet;
    }

    public static void spawnFleet(CampaignFleetAPI fleet, SectorEntityToken hideout) {
        log.info("BountiesExpanded: Spawning Fleet '" + fleet.getName() + "' at '" + hideout.getFullName() + "'");
        LocationAPI location = hideout.getContainingLocation();
        location.addEntity(fleet);
        fleet.setLocation(hideout.getLocation().x - 500, hideout.getLocation().y + 500);
        fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideout, 1000000f, null);
    }

    public static FleetMemberAPI getShipWithHighestFP(List<FleetMemberAPI> fleet) {
        FleetMemberAPI highestFP = null;
        for (FleetMemberAPI member : fleet) {
            if (isNull(highestFP)) highestFP = member;
            else if (member.getFleetPointCost() > highestFP.getFleetPointCost())
                highestFP = member;
        }
        return highestFP;
    }

    public static List<FleetMemberAPI> createCompleteCopyForIntel(CampaignFleetAPI fleet) {
        return createCopyForIntel(fleet, fleet.getNumShips(), null);
    }

    public static List<FleetMemberAPI> createCopyForIntel(CampaignFleetAPI fleet, int numOfShips, Random random) {
        List<FleetMemberAPI> copyList = new ArrayList<>();
        if (isNull(random)) {
            random = new Random(fleet.getCommander().getNameString().hashCode() * 170000L);
        }

        List<FleetMemberAPI> memberList = fleet.getFleetData().getMembersListCopy();
        boolean deflate = false;
        if (!fleet.isInflated()) {
            fleet.inflateIfNeeded();
            deflate = true;
        }

        if (!isNullOrEmpty(memberList)) {
            WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>();
            int shipsLeftToAdd = numOfShips;
            picker.setRandom(random);
            for (FleetMemberAPI member : memberList) {
                if (member.isFlagship()) {
                    copyList.add(createMemberCopy(member));
                    shipsLeftToAdd--;
                    continue;
                }

                if (member.isFighterWing()) continue;

                float weight = (float) member.getFleetPointCost();

                FleetMemberAPI copy = createMemberCopy(member);

                picker.add(copy, weight);
            }

            while (!picker.isEmpty() && shipsLeftToAdd > 0) {
                copyList.add(picker.pickAndRemove());
                shipsLeftToAdd--;
            }
        }

        if (deflate)
            fleet.deflate();

        return isNullOrEmpty(copyList) ? memberList : copyList;
    }

    private static FleetMemberAPI createMemberCopy(FleetMemberAPI member) {
        FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
        if (isNotNull(member.getCaptain()))
            copy.setCaptain(member.getCaptain());
        return copy;
    }
}
