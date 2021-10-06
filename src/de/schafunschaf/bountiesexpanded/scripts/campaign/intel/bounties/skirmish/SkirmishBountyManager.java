package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
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
    public static final String BOUNTY_IDENTIFIER_KEY = "$bountiesExpanded_skirmishBountyActive_";
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
            if (Global.getSector().getMemoryWithoutUpdate().contains(BOUNTY_IDENTIFIER_KEY + skirmishBountyEntityAttempt.getStartingPoint().getMarket().getName())) {
                maxSpawningAttempts--;
                continue;
            }
            isValidBounty = true;
            skirmishBountyEntity = skirmishBountyEntityAttempt;
        }

        if (!isValidBounty)
            return null;

        CampaignFleetAPI fleet = skirmishBountyEntity.getFleet();
        FleetGenerator.spawnFleet(fleet, skirmishBountyEntity.getHideout());
        fleet.setName(FLEET_NAME);
        fleet.getCurrentAssignment().setActionText(FLEET_ACTION_TEXT);
        fleet.setTransponderOn(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

        Global.getSector().getMemoryWithoutUpdate().set(BOUNTY_IDENTIFIER_KEY + skirmishBountyEntity.getHideout().getMarket().getName(), null);

        log.info("BountiesExpanded - Spawning Skirmish Bounty: By "
                + skirmishBountyEntity.getOfferingFaction().getDisplayName() + " | Against "
                + skirmishBountyEntity.getTargetedFaction().getDisplayName() + " | At "
                + skirmishBountyEntity.getHideout().getName());

        addFactionToActiveBountyList(skirmishBountyEntity.getTargetedFaction().getId());

        return new SkirmishBountyIntel(skirmishBountyEntity, skirmishBountyEntity.getFleet(), skirmishBountyEntity.getPerson(), skirmishBountyEntity.getHideout());
    }

    public void addFactionToActiveBountyList(String factionId) {
        activeFactionBountyList.add(factionId);
    }

    public void removeFactionFromActiveList(String factionId) {
        activeFactionBountyList.remove(factionId);
    }
}
