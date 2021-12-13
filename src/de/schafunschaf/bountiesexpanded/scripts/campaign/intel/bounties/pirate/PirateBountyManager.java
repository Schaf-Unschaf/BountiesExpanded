package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
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
public class PirateBountyManager extends BaseEventManager {
    public static final String FLEET_NAME = "Pirate Fleet";
    public static final String FLEET_ACTION_TEXT = "doing shady stuff";
    public static final String KEY = "$bountiesExpanded_pirateBountyManager";
    public static final String PIRATE_BOUNTY_FLEET_KEY = "$bountiesExpanded_pirateBountyFleet";

    public PirateBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static PirateBountyManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (PirateBountyManager) instance;
    }

    @Override
    protected int getMinConcurrent() {
        return Settings.pirateBountyMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.pirateBountyMaxBounties;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.pirateBountyActive) {
            if (Settings.isDebugActive())
                return createPirateBountyEvent();
            if (new Random().nextFloat() <= Settings.pirateBountySpawnChance) {
                return createPirateBountyEvent();
            }
        }

        return null;
    }

    public PirateBountyIntel createPirateBountyEvent() {
        PirateBountyEntity pirateBountyEntity = EntityProvider.pirateBountyEntity();

        if (isNull(pirateBountyEntity))
            return null;

        CampaignFleetAPI fleet = pirateBountyEntity.getFleet();
        SectorEntityToken startingPoint = pirateBountyEntity.getStartingPoint();
        PersonAPI person = pirateBountyEntity.getTargetedPerson();
        Difficulty difficulty = pirateBountyEntity.getDifficulty();

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, startingPoint);
        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleet.getCurrentAssignment().setActionText(FLEET_ACTION_TEXT);
        fleet.setTransponderOn(true);
        fleetMemory.set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, PIRATE_BOUNTY_FLEET_KEY);
        fleetMemory.set(PIRATE_BOUNTY_FLEET_KEY, pirateBountyEntity);

        log.info("BountiesExpanded - Spawning Pirate Bounty: By "
                + pirateBountyEntity.getOfferingFaction().getDisplayName() + " | Against "
                + pirateBountyEntity.getTargetedFaction().getDisplayName() + " | At "
                + startingPoint.getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + pirateBountyEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + difficulty.getShortDescription());

        return new PirateBountyIntel(pirateBountyEntity, fleet, person, startingPoint, null);
    }
}