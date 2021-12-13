package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

@Log4j
public class NoShipRecoveryFleetEncounterContext extends FleetEncounterContext {
    public static String BOUNTIES_EXPANDED_NO_RECOVERY = "$bountiesExpanded_noRecovery";

    @SuppressWarnings("unchecked")
    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI
            winningFleet, CampaignFleetAPI otherFleet) {
        List<FleetMemberAPI> recoverableShips = super.getRecoverableShips(battle, winningFleet, otherFleet);

        MemoryAPI memory = otherFleet.getMemoryWithoutUpdate();
        Set<FleetMemberAPI> fleetMemberToRemove = (Set<FleetMemberAPI>) memory.get(BOUNTIES_EXPANDED_NO_RECOVERY);
        if (isNullOrEmpty(fleetMemberToRemove))
            return null;

        for (FleetMemberAPI fleetMember : fleetMemberToRemove) {
            recoverableShips.remove(fleetMember);
            getStoryRecoverableShips().remove(fleetMember);
        }

        return recoverableShips;
    }
}
