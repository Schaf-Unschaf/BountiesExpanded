package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUpgradeHelper;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class SkirmishBountyManager extends BaseEventManager {
    private final Set<String> activeFactionBountyList = new HashSet<>();
    public static final String FLEET_NAME = "Skirmisher Fleet";
    public static final String FLEET_ACTION_TEXT = "practicing military maneuvers";
    public static final String KEY = "$bountiesExpanded_skirmishBountyManager";
    public static final String BOUNTY_ACTIVE_AT_KEY = "$bountiesExpanded_skirmishBountyActive_";
    public static final String SKIRMISH_BOUNTY_FLEET_KEY = "$bountiesExpanded_skirmishBountyFleet";
    public static final Logger log = Global.getLogger(SkirmishBountyManager.class);

    public SkirmishBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static SkirmishBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (SkirmishBountyManager) test;
    }

    @Override
    protected int getMinConcurrent() {
        return Settings.SKIRMISH_MIN_BOUNTIES;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.SKIRMISH_MAX_BOUNTIES;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.SKIRMISH_ACTIVE) {
            if (Settings.isDebugActive())
                return createSkirmishBountyEvent();
            if (new Random().nextFloat() <= Settings.SKIRMISH_SPAWN_CHANCE) {
                return createSkirmishBountyEvent();
            }
        }

        return null;
    }

    public SkirmishBountyIntel createSkirmishBountyEvent() {
        SkirmishBountyEntity skirmishBountyEntity = null;
        boolean isValidBounty = false;
        int maxSpawningAttempts = 5;

        while (!isValidBounty && maxSpawningAttempts > 0) {
            SkirmishBountyEntity skirmishBountyEntityAttempt = EntityProvider.skirmishBountyEntity();
            if (isNull(skirmishBountyEntityAttempt)) {
                maxSpawningAttempts--;
                continue;
            }
            if (activeFactionBountyList.contains(skirmishBountyEntityAttempt.getTargetedFaction().getId())) {
                maxSpawningAttempts--;
                continue;
            }
            if (Global.getSector().getMemoryWithoutUpdate().contains(BOUNTY_ACTIVE_AT_KEY + skirmishBountyEntityAttempt.getHideout().getMarket().getName())) {
                maxSpawningAttempts--;
                continue;
            }
            isValidBounty = true;
            skirmishBountyEntity = skirmishBountyEntityAttempt;
        }

        if (!isValidBounty)
            return null;

        CampaignFleetAPI fleet = skirmishBountyEntity.getFleet();
        SectorEntityToken hideout = skirmishBountyEntity.getHideout();
        PersonAPI person = skirmishBountyEntity.getPerson();
        Difficulty difficulty = skirmishBountyEntity.getDifficulty();

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, hideout);
        fleet.getCurrentAssignment().setActionText(FLEET_ACTION_TEXT);
        fleet.setTransponderOn(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
        fleet.getMemoryWithoutUpdate().set(SKIRMISH_BOUNTY_FLEET_KEY, skirmishBountyEntity);

        Global.getSector().getMemoryWithoutUpdate().set(BOUNTY_ACTIVE_AT_KEY + hideout.getMarket().getName(), null);

        log.info("BountiesExpanded - Spawning Skirmish Bounty: By "
                + skirmishBountyEntity.getOfferingFaction().getDisplayName() + " | Against "
                + skirmishBountyEntity.getTargetedFaction().getDisplayName() + " | At "
                + hideout.getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + skirmishBountyEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + difficulty.getShortDescription());

        addFactionToActiveBountyList(skirmishBountyEntity.getTargetedFaction().getId());

        upgradeShips(fleet);

        return new SkirmishBountyIntel(skirmishBountyEntity, fleet, person, hideout);
    }

    public void upgradeShips(CampaignFleetAPI bountyFleet) {
        Random random = new Random(bountyFleet.getId().hashCode() * 1337L);
        int modValue = ((SkirmishBountyEntity) bountyFleet.getMemoryWithoutUpdate().get(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY)).getDifficulty().getLevelAdjustment();
        FleetUpgradeHelper.upgradeRandomShips(bountyFleet, modValue, modValue * 0.1f, false, random);
    }

    public void addFactionToActiveBountyList(String factionId) {
        activeFactionBountyList.add(factionId);
    }

    public void removeFactionFromActiveList(String factionId) {
        activeFactionBountyList.remove(factionId);
    }
}
