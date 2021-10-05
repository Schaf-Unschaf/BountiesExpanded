package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;

import java.util.ArrayList;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class AssassinationBountyIntel extends BaseBountyIntel {
    private final AssassinationBountyEntity bountyEntity;
    private final int payment;
    private final SectorEntityToken destination;

    private final float travelDistance;
    private int bonusPayment;

    public AssassinationBountyIntel(AssassinationBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        Misc.makeImportant(fleet, "pbe", 100f);
        this.bountyEntity = bountyEntity;
        this.payment = bountyEntity.getBaseReward();
        this.bonusPayment = bountyEntity.getBonusReward();
        this.destination = bountyEntity.getEndingPoint();
        this.travelDistance = Misc.getDistanceLY(bountyEntity.getStartingPoint().getStarSystem().getHyperspaceAnchor(), bountyEntity.getEndingPoint().getStarSystem().getHyperspaceAnchor());
        bountyEntity.setIntel(this);
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isFlagshipAlive = isNotNull(fleet.getFlagship()) && fleet.getFlagship().getCaptain() == person;
        boolean occurredInHyperspace = fleet.isInHyperspace();
        float remainingDistance = Misc.getDistanceLY(fleet.getLocationInHyperspace(), destination.getLocationInHyperspace());

        if (isDone || isNotInvolved || isFlagshipAlive) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                result = new BountyResult(BountyResult.BountyResultType.END_OTHER, 0, 0, 0f, null);
                cleanUp(true);
                return;
            }
        }

        if (occurredInHyperspace)
            bonusPayment = (Math.round((int) (bonusPayment * (Settings.ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER / travelDistance * remainingDistance)) / 1000) * 1000);
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add(payment + bonusPayment);
        result = new BountyResult(BountyResult.BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment, 0f, null);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return entity.getTargetedFaction();
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        if (!fleet.isInHyperspace()) return null;

        List<ArrowData> result = new ArrayList<>();

        ArrowData arrow = new ArrowData(entity.getStartingPoint(), entity.getEndingPoint());
        arrow.color = getFactionForUIColors().getBaseUIColor();
        result.add(arrow);

        return result;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (fleet.isInHyperspace()) {
            if (Settings.isDebugActive())
                return Global.getSector().getHyperspace().createToken(fleet.getLocationInHyperspace().x, fleet.getLocationInHyperspace().y);
            return entity.getStartingPoint().getStarSystem().getHyperspaceAnchor();
        } else if (fleet.getContainingLocation() == entity.getEndingPoint().getContainingLocation()) {
            return entity.getEndingPoint().getStarSystem().getHyperspaceAnchor();
        }
        if (Settings.isDebugActive())
            return entity.getStartingPoint();
        else
            return null;
    }

    public Object getListInfo() {
        return getListInfoParam();
    }
}
