package de.schafunschaf.bountiesexpanded.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResult;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResultType;

import java.awt.*;
import java.util.Random;
import java.util.Set;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public abstract class BaseBountyIntel extends BaseIntelPlugin implements FleetEventListener {

    protected final float duration;
    protected final Difficulty difficulty;
    protected final CampaignFleetAPI fleet;
    protected final BountyEntity entity;
    protected final PersonAPI person;
    protected final SectorEntityToken hideout;
    protected float elapsedDays = 0f;
    protected BountyResult result;

    public BaseBountyIntel(BountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        this.entity = bountyEntity;
        this.fleet = campaignFleetAPI;
        this.hideout = sectorEntityToken;
        this.person = personAPI;
        this.duration = new Random().nextInt((Settings.SKIRMISH_MAX_DURATION - Settings.SKIRMISH_MIN_DURATION) + 1) + Settings.SKIRMISH_MIN_DURATION;
        this.difficulty = entity.getDifficulty();

        fleet.addEventListener(this);
        Misc.makeImportant(fleet, "pbe", duration + 20f);
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
        if (Settings.SHEEP_DEBUG)
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
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isFlagshipAlive = isNotNull(fleet.getFlagship()) && fleet.getFlagship().getCaptain() == person;

        if (isDone || isNotInvolved || isFlagshipAlive) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                result = new BountyResult(BountyResultType.END_OTHER, 0, 0, null);
                cleanUp(true);
                return;
            }
        }

        int payment = (int) (entity.getBaseReward() * battle.getPlayerInvolvementFraction());
        if (payment <= 0) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, null);
            cleanUp(true);
            return;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add(payment);
        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, 0, null);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone() || isNotNull(result)) {
            return;
        }

        if (this.fleet == fleet) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, null);
            cleanUp(true);
        }
    }

    public float getElapsedDays() {
        return elapsedDays;
    }

    public float getDuration() {
        return duration;
    }

    public BountyResult getResult() {
        return result;
    }

    public void addDays(TooltipMakerAPI info, String after, float days, Color c) {
        super.addDays(info, after, days, c);
    }

    public void bullet(TooltipMakerAPI info) {
        super.bullet(info);
    }

    public Color getBulletColorForMode(ListInfoMode mode) {
        return super.getBulletColorForMode(mode);
    }

    public void unindent(TooltipMakerAPI info) {
        super.unindent(info);
    }

    @Override
    protected void advanceImpl(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        elapsedDays += days;

        if (elapsedDays >= duration && !isDone()) {
            boolean canEnd = isNull(fleet) || !fleet.isInCurrentLocation();
            if (canEnd) {
                result = new BountyResult(BountyResultType.END_TIME, 0, 0, null);
                cleanUp(true);
                return;
            }
        }

        if (isNull(fleet)) {
            return;
        }

        if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, null);
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

    private void cleanUpFleetAndEndIfNecessary() {
        if (isNotNull(fleet)) {
            Misc.makeUnimportant(fleet, "pbe");
            fleet.clearAssignments();

//            if (hideout != null) {
//                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, hideout, 1000000f, null);
//            } else {
//                fleet.despawn();
//            }

            fleet.despawn();
        }

        if (!isEnding() && !isEnded()) {
            endAfterDelay();
        }
    }
}