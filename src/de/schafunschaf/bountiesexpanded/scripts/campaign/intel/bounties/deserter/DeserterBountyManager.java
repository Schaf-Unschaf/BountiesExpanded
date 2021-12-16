package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import lombok.extern.log4j.Log4j;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class DeserterBountyManager extends BaseEventManager {
    public static final String FLEET_NAME = "Deserter Fleet";
    public static final String KEY = "$bountiesExpanded_deserterBountyManager";
    public static final String DESERTER_BOUNTY_FLEET_KEY = "$bountiesExpanded_deserterBountyFleet";

    public DeserterBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static DeserterBountyManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (DeserterBountyManager) instance;
    }

    @Override
    protected int getMinConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.deserterBountyMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.deserterBountyMaxBounties;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.deserterBountyActive) {
            if (Settings.isDebugActive())
                return createDeserterBountyEvent();
            if (new Random().nextFloat() <= Settings.deserterBountySpawnChance) {
                return createDeserterBountyEvent();
            }
        }

        return null;
    }

    public DeserterBountyIntel createDeserterBountyEvent() {
        final DeserterBountyEntity bountyEntity = EntityProvider.deserterBountyEntity();

        if (isNull(bountyEntity))
            return null;

        final CampaignFleetAPI fleet = bountyEntity.getFleet();
        SectorEntityToken spawnLocation = bountyEntity.getSpawnLocation();
        final SectorEntityToken travelDestination = bountyEntity.getTravelDestination();
        PersonAPI person = bountyEntity.getTargetedPerson();
        Difficulty difficulty = bountyEntity.getDifficulty();
        final String fleetTravelingActionText = String.format("leaving %s territory", fleet.getFaction().getDisplayName());

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, spawnLocation);

        final DeserterBountyIntel bountyIntel = new DeserterBountyIntel(bountyEntity, fleet, person, spawnLocation, travelDestination);

        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleetMemory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        fleetMemory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, DESERTER_BOUNTY_FLEET_KEY);
        fleetMemory.set(DESERTER_BOUNTY_FLEET_KEY, bountyEntity);

        fleet.clearAssignments();
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, travelDestination, bountyIntel.getDuration(), fleetTravelingActionText, new Script() {
            public void run() {
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, travelDestination.getStarSystem().getStar(), bountyIntel.getRemainingDuration());
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
            }
        });

        log.info(String.format("BountiesExpanded - Spawning Deserter Bounty: From %s | At %s | Travelling to %s, %s",
                bountyEntity.getOfferingFaction().getDisplayName(), spawnLocation.getName(), travelDestination.getName(), travelDestination.getStarSystem().getName()));
        log.info(String.format("Player-FP at creation: %d", Global.getSector().getPlayerFleet().getFleetPoints()));
        log.info(String.format("Enemy-FP at creation: %d", bountyEntity.getFleet().getFleetPoints()));
        log.info(String.format("Difficulty: %s", difficulty.getShortDescription()));

        return bountyIntel;
    }
}