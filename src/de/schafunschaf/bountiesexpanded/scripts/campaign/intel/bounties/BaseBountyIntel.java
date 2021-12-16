package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.Getter;

import java.awt.*;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
public abstract class BaseBountyIntel extends BaseIntelPlugin implements FleetEventListener {
    protected final Difficulty difficulty;
    protected final CampaignFleetAPI fleet;
    protected final BountyEntity bountyEntity;
    protected final PersonAPI person;
    protected final SectorEntityToken spawnLocation;
    protected final SectorEntityToken travelDestination;
    protected final MissionHandler missionHandler;
    protected int maxFleetSizeForCompletion;
    protected float duration;
    protected float elapsedDays = 0f;
    protected BountyResult result;
    protected BountyType bountyType;

    public BaseBountyIntel(BountyType bountyType, BountyEntity bountyEntity, MissionHandler missionHandler, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken spawnLocation, SectorEntityToken travelDestination) {
        this.bountyType = bountyType;
        this.bountyEntity = bountyEntity;
        this.missionHandler = missionHandler;
        this.fleet = campaignFleetAPI;
        this.spawnLocation = spawnLocation;
        this.travelDestination = travelDestination;
        this.person = personAPI;
        this.duration = 100f;
        this.difficulty = bountyEntity.getDifficulty();

        fleet.addEventListener(this);
        Global.getSector().getIntelManager().queueIntel(this);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(bountyEntity.getTitle(result), getTitleColor(mode), 0f);
        bountyEntity.addBulletPoints(this, info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        bountyEntity.createSmallDescription(this, info, width, height);
        if (Settings.isDebugActive() || isNotNull(result))
            addDeleteButton(info, width);
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return bountyEntity.getOfferingFaction();
    }

    @Override
    public String getIcon() {
        return bountyEntity.getIcon();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        tags.add(fleet.getFaction().getId());

        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return spawnLocation;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return bountyEntity.getTitle(result);
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone() || isNotNull(result)) {
            return;
        }

        if (this.fleet == fleet) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0);
            cleanUp(true);
        }
    }

    @Override
    public void addDays(TooltipMakerAPI info, String after, float days, Color c, float pad) {
        super.addDays(info, after, days, c, pad);
    }

    @Override
    public void bullet(TooltipMakerAPI info) {
        super.bullet(info);
    }

    @Override
    public void unindent(TooltipMakerAPI info) {
        super.unindent(info);
    }

    @Override
    public void indent(TooltipMakerAPI info) {
        super.indent(info);
    }

    @Override
    public Color getBulletColorForMode(ListInfoMode mode) {
        return super.getBulletColorForMode(mode);
    }

    @Override
    protected void advanceImpl(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        elapsedDays += days;

        if (elapsedDays >= duration && !isDone()) {
            boolean canEnd = isNull(fleet) || !fleet.isInCurrentLocation();
            if (canEnd) {
                result = new BountyResult(BountyResultType.END_TIME, 0, 0, 0);
                cleanUp(true);
                return;
            }
        }

        if (isNull(fleet)) {
            return;
        }

        if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0);
            cleanUp(!fleet.isInCurrentLocation());
        }

    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
        cleanUpFleetAndEndIfNecessary();
    }

    public float getRemainingDuration() {
        return duration - elapsedDays;
    }

    protected void cleanUp(boolean onlyIfImportant) {
        sendUpdateIfPlayerHasIntel(result, onlyIfImportant);
        cleanUpFleetAndEndIfNecessary();
    }

    protected void cleanUpFleetAndEndIfNecessary() {
        if (isNotNull(fleet)) {
            Misc.makeUnimportant(fleet, "pbe");
            fleet.clearAssignments();

            if (!Settings.prepareUpdate && isNotNull(spawnLocation)) {
                SectorEntityToken despawnLocation = Misc.findNearestPlanetTo(fleet, false, false);
                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, despawnLocation, 30f, new Script() {
                    @Override
                    public void run() {
                        fleet.despawn();
                    }
                });
            } else {
                fleet.despawn();
            }
        }

        if (!isEnding() && !isEnded()) {
            endAfterDelay();
        }
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_4;
    }

    @Override
    protected String getName() {
        return bountyType.name();
    }
}