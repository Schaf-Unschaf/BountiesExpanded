package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

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
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUpgradeHelper;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import lombok.extern.log4j.Log4j;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class SkirmishBountyManager extends BaseEventManager {
    public static final String FLEET_NAME = "Skirmisher Fleet";
    public static final String FLEET_ACTION_TEXT = "practicing military maneuvers";
    public static final String KEY = "$bountiesExpanded_skirmishBountyManager";
    public static final String SKIRMISH_BOUNTY_FLEET_KEY = "$bountiesExpanded_skirmishBountyFleet";
    private final Set<String> bountiesActiveForFaction = new HashSet<>();
    private final Set<String> bountiesActiveAtEntity = new HashSet<>();

    public SkirmishBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static SkirmishBountyManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (SkirmishBountyManager) instance;
    }

    @Override
    protected int getMinConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.skirmishMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.skirmishMaxBounties;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.skirmishActive) {
            if (Settings.isDebugActive())
                return createSkirmishBountyEvent();
            if (new Random().nextFloat() <= Settings.skirmishSpawnChance) {
                return createSkirmishBountyEvent();
            }
        }

        return null;
    }

    public SkirmishBountyIntel createSkirmishBountyEvent() {
        SkirmishBountyEntity skirmishBountyEntity = EntityProvider.skirmishBountyEntity();

        if (isNull(skirmishBountyEntity))
            return null;
        if (hasActiveBounty(skirmishBountyEntity))
            return null;

        CampaignFleetAPI fleet = skirmishBountyEntity.getFleet();
        SectorEntityToken spawnLocation = skirmishBountyEntity.getSpawnLocation();
        PersonAPI person = skirmishBountyEntity.getTargetedPerson();
        Difficulty difficulty = skirmishBountyEntity.getDifficulty();

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, spawnLocation);
        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleet.getCurrentAssignment().setActionText(FLEET_ACTION_TEXT);
        fleet.setTransponderOn(true);
        fleetMemory.set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
        fleetMemory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, SKIRMISH_BOUNTY_FLEET_KEY);
        fleetMemory.set(SKIRMISH_BOUNTY_FLEET_KEY, skirmishBountyEntity);

        log.info("BountiesExpanded - Spawning Skirmish Bounty: By "
                + skirmishBountyEntity.getOfferingFaction().getDisplayName() + " | Against "
                + skirmishBountyEntity.getTargetedFaction().getDisplayName() + " | At "
                + spawnLocation.getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + skirmishBountyEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + difficulty.getShortDescription());

        registerBounty(skirmishBountyEntity);

        upgradeShips(fleet);

        return new SkirmishBountyIntel(skirmishBountyEntity, fleet, person, spawnLocation, null);
    }

    public void upgradeShips(CampaignFleetAPI bountyFleet) {
        if (isNull(bountyFleet))
            return;

        Random random = new Random(bountyFleet.getId().hashCode() * 1337L);
        int modValue = ((SkirmishBountyEntity) bountyFleet.getMemoryWithoutUpdate().get(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY)).getDifficulty().getFlatModifier();
        FleetUpgradeHelper.upgradeRandomShips(bountyFleet, modValue, modValue * 0.1f, false, random);
    }

    public void registerBounty(SkirmishBountyEntity bountyEntity) {
        bountiesActiveForFaction.add(bountyEntity.getTargetedFaction().getId());
        bountiesActiveAtEntity.add(bountyEntity.getSpawnLocationID());
    }

    public void unregisterBounty(SkirmishBountyEntity bountyEntity) {
        bountiesActiveForFaction.remove(bountyEntity.getTargetedFaction().getId());
        bountiesActiveAtEntity.remove(bountyEntity.getSpawnLocationID());
    }

    public boolean hasActiveBounty(SkirmishBountyEntity bountyEntity) {
        return bountiesActiveForFaction.contains(bountyEntity.getTargetedFaction().getId())
                || bountiesActiveAtEntity.contains(bountyEntity.getSpawnLocationID());
    }
}