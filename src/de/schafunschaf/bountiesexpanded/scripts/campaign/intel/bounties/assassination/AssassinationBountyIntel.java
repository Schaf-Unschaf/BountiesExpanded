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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyType;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.helper.MiscBountyUtils.getUpdatedRep;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
public class AssassinationBountyIntel extends BaseBountyIntel {
    private final AssassinationBountyEntity assassinationBountyEntity;
    private final int payment;
    private final SectorEntityToken destination;

    private final float travelDistance;
    private int bonusPayment;

    public AssassinationBountyIntel(AssassinationBountyEntity assassinationBountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken spawnLocation, SectorEntityToken travelDestination) {
        super(BountyType.ASSASSINATION, assassinationBountyEntity, assassinationBountyEntity.getMissionHandler(), campaignFleetAPI, personAPI, spawnLocation, travelDestination);
        Misc.makeImportant(fleet, "pbe");
        this.assassinationBountyEntity = assassinationBountyEntity;
        this.payment = assassinationBountyEntity.getBaseReward();
        this.bonusPayment = assassinationBountyEntity.getBonusReward();
        this.destination = assassinationBountyEntity.getTravelDestination();
        this.travelDistance = Misc.getDistanceLY(assassinationBountyEntity.getSpawnLocation().getStarSystem().getHyperspaceAnchor(), assassinationBountyEntity.getTravelDestination().getStarSystem().getHyperspaceAnchor());
        assassinationBountyEntity.setIntel(this);
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isFlagshipAlive = isNotNull(fleet.getFlagship()) && fleet.getFlagship().getCaptain() == person;
        boolean occurredInDestinationSystem = assassinationBountyEntity.getTravelDestination().getContainingLocation().equals(fleet.getContainingLocation());
        boolean occurredInHyperspace = fleet.isInHyperspace();
        float remainingDistance = Misc.getDistanceLY(fleet.getLocationInHyperspace(), destination.getLocationInHyperspace());
        float targetRepAfterBattle = getUpdatedRep(assassinationBountyEntity.getTargetedFaction());

        if (isDone || isNotInvolved || isFlagshipAlive) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                result = new BountyResult(BountyResultType.END_OTHER, 0, 0, targetRepAfterBattle);
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

        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, bonusPayment, targetRepAfterBattle);
        SharedData.getData().getPersonBountyEventData().reportSuccess();
        cleanUp(false);
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return bountyEntity.getTargetedFaction();
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        if (!fleet.isInHyperspace()) return null;

        List<ArrowData> result = new ArrayList<>();
        ArrowData arrow;

        if (Settings.isDebugActive() && fleet.isInHyperspace())
            arrow = new ArrowData(fleet, assassinationBountyEntity.getTravelDestination());
        else
            arrow = new ArrowData(assassinationBountyEntity.getSpawnLocation(), assassinationBountyEntity.getTravelDestination());

        arrow.color = getFactionForUIColors().getBaseUIColor();
        result.add(arrow);

        return result;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (fleet.isInHyperspace()) {
            if (Settings.isDebugActive())
                return Global.getSector().getHyperspace().createToken(fleet.getLocationInHyperspace().x, fleet.getLocationInHyperspace().y);
            return assassinationBountyEntity.getSpawnLocation().getStarSystem().getHyperspaceAnchor();
        } else if (fleet.getContainingLocation() == assassinationBountyEntity.getTravelDestination().getContainingLocation()) {
            return assassinationBountyEntity.getTravelDestination().getStarSystem().getHyperspaceAnchor();
        }
        if (Settings.isDebugActive())
            return assassinationBountyEntity.getSpawnLocation();
        else
            return null;
    }

    public Object getListInfo() {
        return getListInfoParam();
    }
}
