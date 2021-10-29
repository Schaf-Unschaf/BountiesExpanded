package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;

public class WarCriminalIntel extends BaseBountyIntel {
    private final WarCriminalEntity bountyEntity;
    private final int payment;
    private int numBattles = 0;
    private float playerInvolvement = 0f;

    public WarCriminalIntel(WarCriminalEntity bountyEntity, CampaignFleetAPI campaignFleetAPI, PersonAPI personAPI, SectorEntityToken sectorEntityToken) {
        super(bountyEntity, bountyEntity.getMissionType(), campaignFleetAPI, personAPI, sectorEntityToken);
        this.bountyEntity = bountyEntity;
        this.payment = bountyEntity.getBaseReward();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        boolean isDone = isDone() || isNotNull(result);
        boolean isNotInvolved = !battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet);
        boolean isFlagshipAlive = true;

        if (battle.isPlayerInvolved()) {
            numBattles++;
            playerInvolvement += battle.getPlayerInvolvementFraction();
        }

        if (isDone || isNotInvolved)
            return;

        playerInvolvement = Math.round((playerInvolvement / numBattles) * 100);
        if (playerInvolvement <= 0) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, 0, 0f, null, 0, 0f, null);
            cleanUp(true);

        }
    }
}
