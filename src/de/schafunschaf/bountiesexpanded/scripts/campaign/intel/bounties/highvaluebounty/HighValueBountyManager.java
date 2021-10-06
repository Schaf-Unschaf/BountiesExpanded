package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.IntervalUtil;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import org.apache.log4j.Logger;

import java.util.*;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class HighValueBountyManager extends BaseEventManager {
    private final IntervalUtil spawnTimer = new IntervalUtil((float) Settings.HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS, (float) Settings.HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS);
    public static final String KEY = "$bountiesExpanded_highValueBountyManager";
    private final String completedBountyDataKey = "$bountiesExpanded_completedBountyData";
    public static final Map<String, HighValueBountyData> highValueBountyData = new HashMap<>();
    private final Set<String> highValueBountyDataActive = new HashSet<>();
    private final Set<String> highValueBountyDataCompleted = new HashSet<>();
    public static final Logger log = Global.getLogger(HighValueBountyManager.class);

    public HighValueBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        loadCompletedBountyData();
    }

    public static HighValueBountyManager getInstance() {
        return (HighValueBountyManager) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }

    public boolean isBountyActive(String bountyId) {
        return highValueBountyDataActive.contains(bountyId);
    }

    public boolean isBountyCompleted(String bountyId) {
        return highValueBountyDataCompleted.contains(bountyId);
    }

    public HighValueBountyData getBounty(String bountyId) {
        return highValueBountyData.get(bountyId);
    }

    public Set<String> getBountiesList() {
        return new HashSet<>(highValueBountyData.keySet());
    }

    public Set<String> getCompletedBounties() {
        return highValueBountyDataCompleted;
    }

    public Set<String> getActiveBounties() {
        return highValueBountyDataActive;
    }

    public Set<String> getAllVariantIds() {
        Set<String> flagshipVariantIds = new HashSet<>();
        for (Map.Entry<String, HighValueBountyData> bountyData : highValueBountyData.entrySet())
            flagshipVariantIds.add(bountyData.getValue().flagshipVariantId);
        return flagshipVariantIds;
    }

    public String getBountyIdFromFlagshipVariantId(String flagshipVariantId) {
        for (String bountyId : getBountiesList()) {
            if (getBounty(bountyId).flagshipVariantId.equals(flagshipVariantId))
                return bountyId;
        }
        return null;
    }

    public void markBountyAsActive(String bountyId) {
        highValueBountyDataActive.add(bountyId);
    }

    public void markBountyAsCompleted(String bountyId) {
        highValueBountyDataCompleted.add(bountyId);
    }

    @Override
    protected int getMinConcurrent() {
        return 0;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.HIGH_VALUE_BOUNTY_MAX_BOUNTIES;
    }

    @Override
    public void advance(float amount) {
        if (Settings.HIGH_VALUE_BOUNTY_ACTIVE) {
            spawnTimer.advance(Global.getSector().getClock().convertToDays(amount));

            if (spawnTimer.intervalElapsed()) {
                if (getActiveBounties().size() < getMaxConcurrent()) {
                    log.info("BountiesExpanded: HVB spawn slot is available [" + getActiveBounties().size() + "/" + getMaxConcurrent() + "]");
                    if (Settings.HIGH_VALUE_BOUNTY_SPAWN_CHANCE <= new Random().nextFloat())
                        createEvent();
                } else
                    log.info("BountiesExpanded: No available HVB slot! [" + getActiveBounties().size() + "/" + getMaxConcurrent() + "]");
            }
        }
    }

    @Override
    protected EveryFrameScript createEvent() {
        return createHighValueBountyEvent(null);
    }

    public EveryFrameScript forceSpawn(String bountyId) {
        log.info("attempting to force-spawn HVB: " + bountyId);
        HighValueBountyEntity highValueBountyEntity = EntityProvider.highValueBountyEntity(bountyId);
        if (isNull(highValueBountyEntity)) {
            log.info("failed to force-spawn HVB: " + bountyId);
            return null;
        }

        HighValueBountyIntel highValueBountyIntel = createHighValueBountyEvent(highValueBountyEntity);

        if (isNull(highValueBountyIntel) || highValueBountyIntel.isDone()) {
            log.info("failed to force-spawn HVB: " + bountyId);
            return null;
        }

        Global.getSector().addScript(highValueBountyIntel);
        log.info("currently active HVBs: " + getActiveBounties());

        return highValueBountyIntel;
    }

    private HighValueBountyIntel createHighValueBountyEvent(HighValueBountyEntity highValueBountyEntity) {
        if (isNull(highValueBountyEntity))
            highValueBountyEntity = EntityProvider.highValueBountyEntity();

        if (isNull(highValueBountyEntity)) {
            log.warn("BountiesExpanded: Failed to create HighValueBountyEntity");
            return null;
        }

        CampaignFleetAPI fleet = highValueBountyEntity.getFleet();
        SectorEntityToken hideout = highValueBountyEntity.getHideout();
        String bountyId = highValueBountyEntity.getBountyId();
        HighValueBountyData bountyData = getBounty(bountyId);
        String randomActionText = NameStringCollection.getFleetActionText();

        FleetGenerator.spawnFleet(fleet, hideout);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set("$bountiesExpanded_highValueBounty", bountyId);
        memory.set("$bountiesExpanded_highValueBountyGreeting", bountyData.greetingText);
        memory.set(MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER, true);
        memory.set(MemFlags.MEMORY_KEY_PIRATE, true);
        memory.set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        memory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);

        fleet.setNoFactionInName(true);
        fleet.setName(bountyData.fleetName);
        fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideout, 100000f, randomActionText, null);

        HighValueBountyIntel bountyIntel = new HighValueBountyIntel(highValueBountyEntity, fleet, highValueBountyEntity.getPerson(), hideout);

        log.info("BountiesExpanded: Creating HighValueBountyEvent");
        markBountyAsActive(bountyId);
        addActive(bountyIntel);

        return bountyIntel;
    }

    @SuppressWarnings("unchecked")
    public void loadCompletedBountyData() {
        Object object = Global.getSector().getMemoryWithoutUpdate().get(completedBountyDataKey);
        if (object instanceof Set<?> && !isNullOrEmpty((Collection<?>) object)) {
            getCompletedBounties().addAll((Collection<? extends String>) object);
        }
    }

    public void saveCompletedBountyData() {
        Global.getSector().getMemoryWithoutUpdate().set(completedBountyDataKey, getCompletedBounties());
    }
}
