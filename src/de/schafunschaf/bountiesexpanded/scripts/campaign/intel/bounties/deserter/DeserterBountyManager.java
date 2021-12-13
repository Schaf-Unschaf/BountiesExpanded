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
        return Settings.deserterBountyMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
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
        final DeserterBountyEntity deserterBountyEntity = EntityProvider.deserterBountyEntity();

        if (isNull(deserterBountyEntity))
            return null;

        final CampaignFleetAPI fleet = deserterBountyEntity.getFleet();
        SectorEntityToken startingPoint = deserterBountyEntity.getStartingPoint();
        final SectorEntityToken endingPoint = deserterBountyEntity.getEndingPoint();
        PersonAPI person = deserterBountyEntity.getTargetedPerson();
        Difficulty difficulty = deserterBountyEntity.getDifficulty();
        final String fleetTravelingActionText = String.format("leaving %s territory", fleet.getFaction().getDisplayName());

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, startingPoint);
        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleetMemory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        fleetMemory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, DESERTER_BOUNTY_FLEET_KEY);
        fleetMemory.set(DESERTER_BOUNTY_FLEET_KEY, deserterBountyEntity);

        fleet.clearAssignments();
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, endingPoint, 100f, fleetTravelingActionText, new Script() {
            public void run() {
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, endingPoint, deserterBountyEntity.getBountyIntel().getRemainingDuration());
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
            }
        });

        log.info(String.format("BountiesExpanded - Spawning Deserter Bounty: From %s | At %s | Travelling to%s, %s",
                deserterBountyEntity.getOfferingFaction().getDisplayName(), startingPoint.getName(), endingPoint.getName(), endingPoint.getStarSystem().getName()));
        log.info(String.format("Player-FP at creation: %d", Global.getSector().getPlayerFleet().getFleetPoints()));
        log.info(String.format("Enemy-FP at creation: %d", deserterBountyEntity.getFleet().getFleetPoints()));
        log.info(String.format("Difficulty: %s", difficulty.getShortDescription()));

        return new DeserterBountyIntel(deserterBountyEntity, fleet, person, startingPoint, endingPoint);
    }
}