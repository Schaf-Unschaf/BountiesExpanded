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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

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
        boolean occurredInDestinationSystem = bountyEntity.getEndingPoint().getContainingLocation().equals(fleet.getContainingLocation());
        boolean occurredInHyperspace = fleet.isInHyperspace();
        float remainingDistance = Misc.getDistanceLY(fleet.getLocationInHyperspace(), destination.getLocationInHyperspace());

        if (isDone || isNotInvolved || isFlagshipAlive) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                result = new BountyResult(BountyResultType.END_OTHER, 0, 0);
                cleanUp(true);
                return;
            }
        }

        if (occurredInDestinationSystem)
            bonusPayment = 0;

        if (occurredInHyperspace)
            bonusPayment = FormattingTools.roundWholeNumber((int) (bonusPayment * (1 / travelDistance * remainingDistance)), 3);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getCargo().getCredits().add(payment + bonusPayment);
        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment);
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

        ArrowData arrow = new ArrowData(bountyEntity.getStartingPoint(), bountyEntity.getEndingPoint());
        arrow.color = getFactionForUIColors().getBaseUIColor();
        result.add(arrow);

        return result;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (fleet.isInHyperspace()) {
            if (Settings.isDebugActive())
                return Global.getSector().getHyperspace().createToken(fleet.getLocationInHyperspace().x, fleet.getLocationInHyperspace().y);
            return bountyEntity.getStartingPoint().getStarSystem().getHyperspaceAnchor();
        } else if (fleet.getContainingLocation() == bountyEntity.getEndingPoint().getContainingLocation()) {
            return bountyEntity.getEndingPoint().getStarSystem().getHyperspaceAnchor();
        }
        if (Settings.isDebugActive())
            return bountyEntity.getStartingPoint();
        else
            return null;
    }

    public Object getListInfo() {
        return getListInfoParam();
    }
}
