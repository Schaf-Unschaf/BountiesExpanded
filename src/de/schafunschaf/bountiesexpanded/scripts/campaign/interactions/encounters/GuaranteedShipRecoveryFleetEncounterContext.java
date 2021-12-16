package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import lombok.extern.log4j.Log4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Log4j
public class GuaranteedShipRecoveryFleetEncounterContext extends FleetEncounterContext {
    public static String BOUNTIES_EXPANDED_GUARANTEED_RECOVERY = "$bountiesExpanded_guaranteeRecovery";
    List<FleetMemberAPI> recoverableShips;

    @SuppressWarnings("unchecked")
    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI
            winningFleet, CampaignFleetAPI otherFleet) {
        log.info("BountiesExpanded: Triggering GuaranteedShipRecoveryFleetEncounterContext");

        recoverableShips = super.getRecoverableShips(battle, winningFleet, otherFleet);
        List<FleetMemberData> enemyCasualties = getDataFor(winningFleet).getEnemyCasualties();
        Collection<FleetMemberAPI> shipsToRecover;

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet))
            return recoverableShips;

        if (otherFleet.getMemoryWithoutUpdate().contains(BOUNTIES_EXPANDED_GUARANTEED_RECOVERY))
            shipsToRecover = (Collection<FleetMemberAPI>) otherFleet.getMemoryWithoutUpdate().get(BOUNTIES_EXPANDED_GUARANTEED_RECOVERY);
        else
            return recoverableShips;

        // Check if at least one ship that should be guaranteed to be recoverable got destroyed
        if (!Collections.disjoint(enemyCasualties, shipsToRecover) && !Collections.disjoint(enemyCasualties, getStoryRecoverableShips()))
            return recoverableShips;

        for (FleetMemberAPI ship : shipsToRecover)
            if (!recoverableShips.contains(ship)) {
                if (!Settings.onlyRecoverWithSP) {
                    getStoryRecoverableShips().remove(ship);
                    prepareAndAddShipForRecovery(ship);
                } else if (!getStoryRecoverableShips().contains(ship))
                    prepareAndAddShipForRecovery(ship);
            }

        return recoverableShips;
    }

    private void prepareAndAddShipForRecovery(FleetMemberAPI ship) {
        DModManager.addDMods(ship, false, false, getWinner(), new Random(ship.getId().hashCode()));
        if (DModManager.getNumDMods(ship.getVariant()) > 0)
            DModManager.setDHull(ship.getVariant());

        float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
        float wingProb = Global.getSettings().getFloat("salvageWingProb");

        ship.setOwner(0);

        prepareShipForRecovery(ship, true, true, false, weaponProb, wingProb, getSalvageRandom());

        recoverableShips.add(ship);
    }
}
