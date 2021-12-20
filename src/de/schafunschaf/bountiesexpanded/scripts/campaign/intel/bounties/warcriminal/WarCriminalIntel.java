package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyType;
import lombok.Getter;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.helper.MiscBountyUtils.getUpdatedRep;
import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.MissionType;
import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.haveObjectivesBeenCompleted;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;

@Getter
public class WarCriminalIntel extends BaseBountyIntel {
    private final WarCriminalEntity warCriminalEntity;
    private final int payment;

    public WarCriminalIntel(WarCriminalEntity warCriminalEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken spawnLocation, SectorEntityToken travelDestination) {
        super(BountyType.WAR_CRIMINAL, warCriminalEntity, warCriminalEntity.getMissionHandler(), campaignFleetAPI, personAPI, spawnLocation, travelDestination);
        this.warCriminalEntity = warCriminalEntity;
        this.payment = warCriminalEntity.getBaseReward();
        this.duration = new Random().nextInt(Settings.warCriminalMaxDuration - Settings.warCriminalMinDuration) + Settings.warCriminalMinDuration;
        warCriminalEntity.setBountyIntel(this);
        Misc.makeImportant(fleet, "pbe");
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isNotComplete = !haveObjectivesBeenCompleted(this, fleet, battle);

        if (isDone || isNotInvolved || isNotComplete)
            return;

        float targetRepAfterBattle = getUpdatedRep(warCriminalEntity.getTargetedFaction());
        boolean isRetrievalMission = MissionType.RETRIEVAL == missionHandler.getMissionType();
        int totalPayout = payment;
        int remainingPayment = 0;
        if (isRetrievalMission) {
            totalPayout = Math.round(payment * 0.7f);
            remainingPayment = payment - totalPayout;
        }

        Global.getSector().getPlayerFleet().getCargo().getCredits().add(totalPayout);

        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                warCriminalEntity.getOfferingFaction().getId());

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, totalPayout, rep, targetRepAfterBattle);
        SharedData.getData().getPersonBountyEventData().reportSuccess();

        if (isRetrievalMission)
            if (Settings.retrievalEventActive)
                missionHandler.startRetrievalSecondStage(warCriminalEntity.getBountyIntel(), warCriminalEntity.getRetrievalTargetShip(), remainingPayment);

        cleanUp(false);
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (Settings.isDebugActive())
            return fleet.getContainingLocation().createToken(fleet.getLocation().x, fleet.getLocation().y);

        return spawnLocation.getStarSystem().getHyperspaceAnchor();
    }
}
