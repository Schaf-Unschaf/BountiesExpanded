package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.Getter;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.helper.MiscBountyUtils.getUpdatedRep;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;

@Getter
public class DeserterBountyIntel extends BaseBountyIntel {
    private final DeserterBountyEntity deserterBountyEntity;
    private final int payment;

    public DeserterBountyIntel(DeserterBountyEntity deserterBountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken spawnLocation, SectorEntityToken travelDestination) {
        super(BountyType.DESERTER, deserterBountyEntity, deserterBountyEntity.getMissionHandler(), campaignFleetAPI, personAPI, spawnLocation, travelDestination);
        this.deserterBountyEntity = deserterBountyEntity;
        this.payment = deserterBountyEntity.getBaseReward();
        this.duration = new Random().nextInt(Settings.deserterBountyMaxDuration - Settings.deserterBountyMinDuration) + Settings.deserterBountyMinDuration;
        deserterBountyEntity.setBountyIntel(this);
        Misc.makeImportant(fleet, "pbe");
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isNotComplete = !MissionHandler.haveObjectivesBeenCompleted(this, fleet, battle);

        if (isDone || isNotInvolved || isNotComplete)
            return;

        float targetRepAfterBattle = getUpdatedRep(deserterBountyEntity.getTargetedFaction());

        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);

        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.COMBAT_HELP_CRITICAL, null, null, null, true, false),
                deserterBountyEntity.getOfferingFaction().getId());

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep, targetRepAfterBattle);
        SharedData.getData().getPersonBountyEventData().reportSuccess();

        cleanUp(false);
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return bountyEntity.getTargetedFaction();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (Settings.isDebugActive())
            return fleet.getContainingLocation().createToken(fleet.getLocation().x, fleet.getLocation().y);

        Constellation c = travelDestination.getConstellation();
        SectorEntityToken entity = null;
        if (c != null && map != null) {
            entity = map.getConstellationLabelEntity(c);
        }

        if (entity == null) {
            entity = travelDestination;
        }

        return entity;
    }
}
