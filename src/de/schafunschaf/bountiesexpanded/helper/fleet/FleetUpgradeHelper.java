package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import de.schafunschaf.bountiesexpanded.helper.ship.HullModUtils;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;

import java.util.Random;

public class FleetUpgradeHelper {
    public static void upgradeRandomShips(CampaignFleetAPI fleet, int numSMods, float probability, boolean excludeFlagship, Random random) {
        if (ComparisonTools.isNull(random))
            random = new Random();

        FleetDataAPI fleetData = fleet.getFleetData();
        for (FleetMemberAPI fleetMember : fleetData.getMembersListCopy()) {
            if (excludeFlagship && fleetMember.isFlagship())
                continue;

            upgradeShip(numSMods, probability, random, fleetMember);
        }
    }

    private static void upgradeShip(int numSMods, float probability, Random random, FleetMemberAPI fleetMember) {
        ShipVariantAPI shipVariant = fleetMember.getVariant();
        for (int i = 0; i < numSMods; i++)
            if (random.nextFloat() <= probability)
                shipVariant.addPermaMod(HullModUtils.getRandomFreeSMod(shipVariant, random), true);

        fleetMember.setVariant(shipVariant, true, true);
    }
}
