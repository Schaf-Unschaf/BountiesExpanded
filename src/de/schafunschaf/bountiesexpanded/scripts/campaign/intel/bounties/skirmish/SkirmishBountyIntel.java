package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;

public class SkirmishBountyIntel extends BaseBountyIntel {
    private final int maxFleetSizeForCompletion;
    private final SkirmishBountyEntity bountyEntity;
    private final int baseShipBounty;
    private final int payment;
    private int bonusPayment = 0;
    private int numBattles = 0;
    private float playerInvolvement = 0f;


    public SkirmishBountyIntel(SkirmishBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        this.maxFleetSizeForCompletion = bountyEntity.getFleet().getFleetSizeCount() - bountyEntity.getShipsToDestroy();
        this.bountyEntity = bountyEntity;
        this.baseShipBounty = bountyEntity.getBaseShipBounty();
        this.payment = bountyEntity.getBaseReward();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
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

        float playerInvolvedAverage = playerInvolvement / numBattles;

        if (playerInvolvedAverage <= 0) {
            result = new BountyResult(BountyResult.BountyResultType.END_OTHER, 0, 0, 0f, null);
            cleanUp(true);
            return;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add((payment + bonusPayment) * playerInvolvedAverage);
        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                bountyEntity.getOfferingFaction().getId());

        result = new BountyResult(BountyResult.BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment, playerInvolvedAverage, rep);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    @Override
    protected void cleanUp(boolean onlyIfImportant) {
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.BOUNTY_IDENTIFIER_KEY + bountyEntity.getHideout().getMarket().getName());
        super.cleanUp(onlyIfImportant);
    }

    @Override
    public void endImmediately() {
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.BOUNTY_IDENTIFIER_KEY + bountyEntity.getHideout().getMarket().getName());
        super.endImmediately();
    }
}
