package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.skirmish;

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
import de.schafunschaf.bountylib.campaign.intel.BountyEventData;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;

public class SkirmishBountyIntel extends BaseBountyIntel {
    private final int maxFleetSizeForCompletion;
    private final SkirmishBountyEntity bountyEntity;
    private int payment = 0;
    private final int baseShipBounty;
    private int numBattles = 0;
    private float playerInvolvement = 0f;

    public SkirmishBountyIntel(SkirmishBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        maxFleetSizeForCompletion = (int) (bountyEntity.getFleet().getFleetSizeCount() * bountyEntity.getFractionToKill());
        this.bountyEntity = bountyEntity;
        this.baseShipBounty = bountyEntity.baseShipBounty;
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

        payment += bountyEntity.getBountyCredits();

        for (CampaignFleetAPI otherFleet : battle.getSnapshotSideFor(fleet)) {
            float bounty = 0;
            for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
                float mult = Misc.getSizeNum(loss.getHullSpec().getHullSize());
                bounty += mult * baseShipBounty;
            }

            payment += (bounty * battle.getPlayerInvolvementFraction());
        }

        if (payment <= 0) {
            result = new BountyEventData.BountyResult(BountyEventData.BountyResultType.END_OTHER, 0, 0, null);
            cleanUp(true);
            return;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add(payment);
        float playerInvolvedAverage = playerInvolvement / numBattles;
        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                bountyEntity.offeringFaction.getId());

        result = new BountyEventData.BountyResult(BountyEventData.BountyResultType.END_PLAYER_BOUNTY, payment, playerInvolvedAverage, rep);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }
}
