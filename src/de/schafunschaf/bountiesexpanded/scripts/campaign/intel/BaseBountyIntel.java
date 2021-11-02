package de.schafunschaf.bountiesexpanded.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionType;
import lombok.Getter;

import java.awt.*;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
public abstract class BaseBountyIntel extends BaseIntelPlugin implements FleetEventListener {
    protected final Difficulty difficulty;
    protected final CampaignFleetAPI fleet;
    protected final BountyEntity entity;
    protected final PersonAPI person;
    protected final SectorEntityToken hideout;
    protected final MissionType missionType;
    protected int maxFleetSizeForCompletion;
    protected float duration;
    protected float elapsedDays = 0f;
    protected BountyResult result;

    public BaseBountyIntel(BountyEntity bountyEntity, MissionType missionType, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        this.entity = bountyEntity;
        this.missionType = missionType;
        this.fleet = campaignFleetAPI;
        this.hideout = sectorEntityToken;
        this.person = personAPI;
        this.duration = 100f;
        this.difficulty = entity.getDifficulty();
        this.maxFleetSizeForCompletion = bountyEntity.getMaxFleetSizeForCompletion();

        fleet.addEventListener(this);
        Global.getSector().getIntelManager().queueIntel(this);
    }

    @Deprecated
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(entity.getTitle(result), getTitleColor(mode), 0f);
        entity.addBulletPoints(this, info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        entity.createSmallDescription(this, info, width, height);
        if (Settings.isDebugActive() || isNotNull(result))
            addDeleteButton(info, width);
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return entity.getOfferingFaction();
    }

    @Override
    public String getIcon() {
        return entity.getIcon();
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
        return hideout;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return entity.getTitle(result);
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

    public void addDays(TooltipMakerAPI info, String after, float days, Color c) {
        super.addDays(info, after, days, c);
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

    protected void cleanUp(boolean onlyIfImportant) {
        sendUpdateIfPlayerHasIntel(result, onlyIfImportant);
        cleanUpFleetAndEndIfNecessary();
    }

    protected void cleanUpFleetAndEndIfNecessary() {
        if (isNotNull(fleet)) {
            Misc.makeUnimportant(fleet, "pbe");
            fleet.clearAssignments();

            if (!Settings.PREPARE_UPDATE && hideout != null) {
                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, hideout, 1000000f, null);
            } else {
                fleet.despawn();
            }
        }

        if (!isEnding() && !isEnded()) {
            endAfterDelay();
        }
    }
}