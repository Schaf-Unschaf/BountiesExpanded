package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;

import java.util.ArrayList;
import java.util.List;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public class AssassinationBountyIntel extends BaseBountyIntel {
    private final int payment;
    private final SectorEntityToken destination;

    private final float travelDistance;
    private int bonusPayment = 0;

    public AssassinationBountyIntel(AssassinationBountyEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, campaignFleetAPI, personAPI, sectorEntityToken);
        this.payment = bountyEntity.getBaseReward();
        this.destination = bountyEntity.getEndingPoint();
        this.travelDistance = Misc.getDistanceLY(bountyEntity.getStartingPoint().getStarSystem().getHyperspaceAnchor(), bountyEntity.getEndingPoint().getStarSystem().getHyperspaceAnchor());
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
            bonusPayment = Math.round((int) (payment * (Settings.ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER / travelDistance * remainingDistance)) / 1000) * 1000;
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
        if (fleet.isInHyperspace())
            return entity.getStartingPoint().getStarSystem().getHyperspaceAnchor();
        else if (fleet.getContainingLocation() == entity.getEndingPoint().getContainingLocation())
            return entity.getEndingPoint().getStarSystem().getHyperspaceAnchor();
        if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.SHEEP_DEBUG)
            return entity.getStartingPoint();
        else
            return null;
    }
}
