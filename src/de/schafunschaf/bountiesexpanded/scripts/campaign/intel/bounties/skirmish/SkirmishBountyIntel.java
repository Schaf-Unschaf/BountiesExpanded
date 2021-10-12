package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;

import java.util.Random;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class SkirmishBountyIntel extends BaseBountyIntel {
    private final int maxFleetSizeForCompletion;
    private final SkirmishBountyEntity bountyEntity;
    private final int baseShipBounty;
    private int payment;
    private int bonusPayment = 0;
    private int numBattles = 0;
    private float playerInvolvement = 0f;


    public SkirmishBountyIntel(SkirmishBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        this.duration = new Random().nextInt((Settings.SKIRMISH_MAX_DURATION - Settings.SKIRMISH_MIN_DURATION) + 1) + Settings.SKIRMISH_MIN_DURATION;
        Misc.makeImportant(fleet, "pbe", duration + 20f);
        this.maxFleetSizeForCompletion = bountyEntity.getFleet().getFleetSizeCount() - bountyEntity.getShipsToDestroy();
        this.bountyEntity = bountyEntity;
        this.baseShipBounty = bountyEntity.getBaseShipBounty();
        this.payment = bountyEntity.getBaseReward();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        float maxRepGain = 25f;
        float neutralRepGain = 10f;
        float maxRewardDeduction = 50f;
        float maxRewardIncrease = 15f;

        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean hasLostEnoughShips = fleet.getFleetSizeCount() <= maxFleetSizeForCompletion;

        if (battle.isPlayerInvolved()) {
            numBattles++;
            playerInvolvement += battle.getPlayerInvolvementFraction();
        }

        if (isDone || isNotInvolved || !hasLostEnoughShips) {
            return;
        }

        for (CampaignFleetAPI otherFleet : battle.getSnapshotSideFor(fleet)) {
            float bounty = 0;
            for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
                float mult = Misc.getSizeNum(loss.getHullSpec().getHullSize());
                bounty += mult * baseShipBounty;
            }

            bonusPayment += (bounty * battle.getPlayerInvolvementFraction());
        }

        float playerInvolvedAverage = Math.round(playerInvolvement / numBattles);

        if (playerInvolvedAverage <= 0) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0f, null, 0f);
            cleanUp(true);
            return;
        }

        int paymentModifier;
        FactionAPI offeringFaction = bountyEntity.getOfferingFaction();
        float repToPlayer = offeringFaction.getRelToPlayer().getRel();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        CoreReputationPlugin.CustomRepImpact customRepImpact = new CoreReputationPlugin.CustomRepImpact();

        if (repToPlayer < 0f) {
            customRepImpact.delta = ((maxRepGain - neutralRepGain) * -repToPlayer + neutralRepGain) / 100;
            paymentModifier = -Math.round(100 + repToPlayer * maxRewardDeduction);
            payment *= (double) -paymentModifier / 100;
            bonusPayment *= (double) -paymentModifier / 100;
        } else {
            customRepImpact.delta = (neutralRepGain - repToPlayer * neutralRepGain) / 100;
            paymentModifier = Math.round(100 + repToPlayer * maxRewardIncrease);
            payment *= (double) paymentModifier / 100;
            bonusPayment *= (double) paymentModifier / 100;
        }

        playerFleet.getCargo().getCredits().add((payment + bonusPayment) * playerInvolvedAverage);
        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, customRepImpact, null, null, true, false),
                offeringFaction.getId());

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment, playerInvolvedAverage, rep, paymentModifier);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    @Override
    protected void advanceImpl(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        elapsedDays += days;

        if (elapsedDays >= duration && !isDone()) {
            boolean canEnd = isNull(fleet) || !fleet.isInCurrentLocation();
            if (canEnd) {
                result = new BountyResult(BountyResultType.END_TIME, 0, 0);
                cleanUp(true);
                return;
            }
        }

        if (isNull(fleet) || isNull(result)) {
            return;
        }

        if (fleet.getFleetSizeCount() <= maxFleetSizeForCompletion) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0);
            cleanUp(!fleet.isInCurrentLocation());
        }
    }

    @Override
    protected void cleanUp(boolean onlyIfImportant) {
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.BOUNTY_ACTIVE_AT_KEY + bountyEntity.getHideout().getMarket().getName());
        SkirmishBountyManager.getInstance().removeFactionFromActiveList(bountyEntity.getTargetedFaction().getId());
        super.cleanUp(onlyIfImportant);
    }

    @Override
    public void endImmediately() {
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.BOUNTY_ACTIVE_AT_KEY + bountyEntity.getHideout().getMarket().getName());
        SkirmishBountyManager.getInstance().removeFactionFromActiveList(bountyEntity.getTargetedFaction().getId());
        super.endImmediately();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> intelTags = super.getIntelTags(map);
        intelTags.add(bountyEntity.getOfferingFaction().getId());
        return intelTags;
    }
}
