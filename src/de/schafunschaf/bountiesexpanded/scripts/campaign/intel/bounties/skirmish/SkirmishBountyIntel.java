package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

import java.util.*;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize;
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
    private Map<HullSize, int[]> destroyedShips;

    public SkirmishBountyIntel(SkirmishBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        this.duration = new Random().nextInt((Settings.SKIRMISH_MAX_DURATION - Settings.SKIRMISH_MIN_DURATION) + 1) + Settings.SKIRMISH_MIN_DURATION;
        Misc.makeImportant(fleet, "pbe", duration + 20f);
        this.maxFleetSizeForCompletion = bountyEntity.getMaxFleetSizeForCompletion();
        this.bountyEntity = bountyEntity;
        this.baseShipBounty = bountyEntity.getBaseShipBounty();
        this.payment = bountyEntity.getBaseReward();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isNull(destroyedShips)) {
            final int[] destroyedData = {0, 0, 0}; // Num destroyed, payment per ship, payment sum
            destroyedShips = new HashMap<>();
            destroyedShips.put(HullSize.FRIGATE, destroyedData);
            destroyedShips.put(HullSize.DESTROYER, Arrays.copyOf(destroyedData, 3));
            destroyedShips.put(HullSize.CRUISER, Arrays.copyOf(destroyedData, 3));
            destroyedShips.put(HullSize.CAPITAL_SHIP, Arrays.copyOf(destroyedData, 3));
        }

        float maxRepGain = 25f;
        float neutralRepGain = 10f;
        float maxRewardDeduction = 50f;
        float maxRewardIncrease = 15f;

        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean hasLostEnoughShips = fleet.getNumShips() <= maxFleetSizeForCompletion;

        if (battle.isPlayerInvolved()) {
            numBattles++;
            playerInvolvement += battle.getPlayerInvolvementFraction();

            for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(fleet))
                increaseShipByOne(loss, destroyedShips);
        }

        if (isDone || isNotInvolved || !hasLostEnoughShips)
            return;

        playerInvolvement = Math.round((playerInvolvement / numBattles) * 100) / 100;
        if (playerInvolvement <= 0) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0f, null, 0, 0f, null);
            cleanUp(true);
            return;
        }

        int paymentModifier;
        FactionAPI offeringFaction = bountyEntity.getOfferingFaction();
        float repToPlayer = offeringFaction.getRelToPlayer().getRel();
        float lastRepChange = new ReputationChangeTracker().getDataFor(bountyEntity.getTargetedFaction().getId()).getLastValue();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        CoreReputationPlugin.CustomRepImpact customRepImpact = new CoreReputationPlugin.CustomRepImpact();

        if (repToPlayer < 0f) {
            customRepImpact.delta = ((maxRepGain - neutralRepGain) * -repToPlayer + neutralRepGain) / 100;
            paymentModifier = -Math.round(100 + repToPlayer * maxRewardDeduction);
            payment *= (float) -paymentModifier * playerInvolvement / 100;
            calculateBonusPayment(paymentModifier);
        } else if (repToPlayer > 0f) {
            customRepImpact.delta = (neutralRepGain - repToPlayer * neutralRepGain) / 100;
            paymentModifier = Math.round(100 + repToPlayer * maxRewardIncrease);
            payment *= (float) paymentModifier * playerInvolvement / 100;
            calculateBonusPayment(paymentModifier);
        } else {
            customRepImpact.delta = neutralRepGain / 100;
            paymentModifier = 100;
        }

        payment = FormattingTools.roundWholeNumber(payment, 2);

        playerFleet.getCargo().getCredits().add(payment + bonusPayment);

        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, customRepImpact, null, null, true, false),
                offeringFaction.getId());
        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment, playerInvolvement, rep, lastRepChange, paymentModifier, destroyedShips);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    private void increaseShipByOne(FleetMemberAPI fleetMember, Map<HullSize, int[]> destroyedShips) {
        ShipAPI.HullSize hullSize = fleetMember.getHullSpec().getHullSize();
        int[] destroyedShipsData = destroyedShips.get(hullSize);
        if (destroyedShips.containsKey(hullSize)) {
            destroyedShipsData[0]++;
            destroyedShips.put(hullSize, destroyedShipsData);
        }
    }

    private void calculateBonusPayment(int paymentModifier) {
        if (paymentModifier < 0)
            paymentModifier = -paymentModifier;

        for (Map.Entry<HullSize, int[]> entry : destroyedShips.entrySet()) {
            int bonusPerSize = 0;
            int payoutPerSize = 0;
            int numFleetMembers = bountyEntity.getNumFleetMembers().get(entry.getKey());
            float mult = Misc.getSizeNum(entry.getKey());
            int[] destroyedShipsData = entry.getValue();
            bonusPerSize += baseShipBounty * mult * destroyedShipsData[0];
            payoutPerSize += bonusPerSize * paymentModifier * playerInvolvement / 100;
            payoutPerSize = FormattingTools.roundWholeNumber(payoutPerSize, 2);
            destroyedShipsData[1] = payoutPerSize;
            destroyedShipsData[2] = (int) (numFleetMembers * baseShipBounty * mult);
            bonusPayment += payoutPerSize;
        }
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

        if (isNull(fleet) || isNull(result))
            return;

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
