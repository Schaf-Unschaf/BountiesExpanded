package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.util.List;

public class HighValueBountyFleetEncounterContext extends FleetEncounterContext {
    public static final Logger log = Global.getLogger(HighValueBountyFleetEncounterContext.class);

    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI
            winningFleet, CampaignFleetAPI otherFleet) {
        log.info("BountiesExpanded: Triggering HighValueBountyFleetEncounterContext");

        List<FleetMemberAPI> recoverableShips = super.getRecoverableShips(battle, winningFleet, otherFleet);
        List<FleetMemberAPI> storyRecoverableShips = getStoryRecoverableShips();

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet))
            return recoverableShips;

        String bountyId = (String) otherFleet.getMemoryWithoutUpdate().get("$bountiesExpanded_highValueBounty");

        for (FleetMemberAPI recoverableShip : recoverableShips)
            if (recoverableShip.getVariant().getHullVariantId().equals(bountyId))
                return recoverableShips;

        for (FleetMemberAPI recoverableShip : storyRecoverableShips)
            if (recoverableShip.getVariant().getHullVariantId().equals(bountyId))
                return recoverableShips;

        HighValueBountyData bountyData = HighValueBountyManager.getInstance().getBounty(bountyId);
        ShipVariantAPI variant = Global.getSettings().getVariant(bountyData.flagshipVariantId);
        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);

        float dp = fleetMember.getBaseDeployCost();
        int num = (int) (dp / 5f);
        if (num < 4) num = 4;

        DModManager.addDMods(fleetMember, true, num, null);
        if (DModManager.getNumDMods(variant) > 0)
            DModManager.setDHull(variant);

        float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
        float wingProb = Global.getSettings().getFloat("salvageWingProb");

        prepareShipForRecovery(fleetMember, false, true, true, weaponProb, wingProb, getSalvageRandom());

        recoverableShips.add(fleetMember);

        return recoverableShips;
    }
}
