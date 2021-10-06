package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import de.schafunschaf.bountiesexpanded.helper.ship.SModUpgradeHelper;

import java.util.Random;

public class FleetUpgradeHelper {
    public static CampaignFleetAPI upgradeRandomShips(CampaignFleetAPI fleet, int numSMods, float probability) {
        CampaignFleetAPI upgradedFleet = Global.getFactory().createEmptyFleet(fleet.getFaction().getId(), fleet.getName(), true);
        for (FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy()) {
            ShipVariantAPI shipVariant = fleetMember.getVariant().clone();
            for (int i = 0; i < numSMods; i++)
                if (new Random().nextFloat() <= probability)
                    shipVariant.addPermaMod(SModUpgradeHelper.getRandomFreeHullMod(shipVariant), true);

            fleetMember.setVariant(shipVariant, true, true);
            upgradedFleet.getFleetData().addFleetMember(fleetMember);
        }
        return upgradedFleet;
    }
}
