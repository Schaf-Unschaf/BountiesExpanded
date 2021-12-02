package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.Getter;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.helper.MiscBountyUtils.getUpdatedRep;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;

@Getter
public class WarCriminalIntel extends BaseBountyIntel {
    private final WarCriminalEntity warCriminalEntity;
    private final int payment;

    public WarCriminalIntel(WarCriminalEntity warCriminalEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(warCriminalEntity, warCriminalEntity.getMissionHandler(), campaignFleetAPI, personAPI, sectorEntityToken);
        this.warCriminalEntity = warCriminalEntity;
        this.payment = warCriminalEntity.getBaseReward();
        this.duration = new Random().nextInt((Settings.warCriminalMaxDuration - Settings.warCriminalMinDuration) + 1) + Settings.warCriminalMinDuration;
        warCriminalEntity.setBountyIntel(this);
        Misc.makeImportant(fleet, "warCriminalBounty", duration);
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isNotComplete = !MissionHandler.haveObjectivesBeenCompleted(this, fleet, battle);

        if (isDone || isNotInvolved || isNotComplete)
            return;

        float targetRepAfterBattle = getUpdatedRep(warCriminalEntity.getTargetedFaction());

        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);

        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                warCriminalEntity.getOfferingFaction().getId());

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep, targetRepAfterBattle);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        if (Settings.retrievalEventActive)
            missionHandler.startRetrievalSecondStage(warCriminalEntity.getBountyIntel(), warCriminalEntity.getRetrievalTargetShip());

        cleanUp(false);
    }
}
