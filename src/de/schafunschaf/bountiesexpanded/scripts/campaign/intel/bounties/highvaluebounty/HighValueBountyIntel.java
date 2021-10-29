package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class HighValueBountyIntel extends BaseBountyIntel {
    private final HighValueBountyEntity bountyEntity;
    private final int payment;

    public HighValueBountyIntel(HighValueBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, bountyEntity.getMissionType(), campaignFleetAPI, personAPI, sectorEntityToken);
        this.bountyEntity = bountyEntity;
        this.payment = bountyEntity.getBaseReward();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isFlagshipAlive = fleet.getFlagship() == bountyEntity.flagship;

        if (isDone || isNotInvolved || isFlagshipAlive) {
            return;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add(payment);

        ReputationActionResponsePlugin.ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                bountyEntity.getOfferingFaction().getId());

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        HighValueBountyManager.getInstance().markBountyAsCompleted(bountyEntity.getBountyId());
        cleanUp(false);
    }

    @Override
    protected void advanceImpl(float amount) {
        if (isNull(fleet)) {
            return;
        }

        if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0);
            cleanUp(!fleet.isInCurrentLocation());
        }
    }

    @Override
    protected void cleanUp(boolean onlyIfImportant) {
        super.cleanUp(onlyIfImportant);
        HighValueBountyManager.getInstance().removeBountyFromActiveList(bountyEntity.getBountyId());
    }

    @Override
    protected void cleanUpFleetAndEndIfNecessary() {
        super.cleanUpFleetAndEndIfNecessary();
        HighValueBountyManager.getInstance().removeBountyFromActiveList(bountyEntity.getBountyId());
    }

    public HighValueBountyEntity getEntity() {
        return bountyEntity;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (Settings.isDebugActive())
            return super.getMapLocation(map);

        Constellation c = hideout.getConstellation();
        SectorEntityToken entity = null;
        if (c != null && map != null) {
            entity = map.getConstellationLabelEntity(c);
        }

        if (entity == null) {
            entity = hideout;
        }

        return entity;
    }
}
