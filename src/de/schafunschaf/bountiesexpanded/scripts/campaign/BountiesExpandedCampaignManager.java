package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;

import java.util.Collection;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class BountiesExpandedCampaignManager extends BaseCampaignEventListener implements EveryFrameScript {
    public BountiesExpandedCampaignManager() {
        super(true);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        BattleAPI battle = result.getBattle();
        if (!battle.isPlayerInvolved()) return;

        List<CampaignFleetAPI> nonPlayerSide = battle.getNonPlayerSide();
        if (isNullOrEmpty(nonPlayerSide)) return;

        removeShipwrecksMarkedForRecovery(nonPlayerSide);
    }

    @SuppressWarnings("unchecked")
    private void removeShipwrecksMarkedForRecovery(Collection<CampaignFleetAPI> enemyFleets) {
        for (CampaignFleetAPI enemyFleet : enemyFleets)
            if (enemyFleet.getMemoryWithoutUpdate().contains(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY)) {
                Collection<FleetMemberAPI> fleetMembers = (Collection<FleetMemberAPI>) enemyFleet.getMemoryWithoutUpdate().get(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY);
                LocationAPI containingLocation = enemyFleet.getContainingLocation();
                List<SectorEntityToken> salvageableEntities = containingLocation.getEntitiesWithTag(Tags.SALVAGEABLE);

                for (SectorEntityToken salvageableEntity : salvageableEntities)
                    if (salvageableEntity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
                        ShipRecoverySpecial.PerShipData shipData = ((DerelictShipEntityPlugin) salvageableEntity.getCustomPlugin()).getData().ship;
                        for (FleetMemberAPI fleetMember : fleetMembers)
                            if (fleetMember.getId().equals(shipData.fleetMemberId))
                                salvageableEntity.setExpired(true);
                    }

            }
    }
}
