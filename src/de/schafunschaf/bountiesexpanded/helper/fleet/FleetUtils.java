package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class FleetUtils {
    public static Set<CampaignFleetAPI> getAllFleets() {
        Set<CampaignFleetAPI> allFleets = new HashSet<>(Global.getSector().getHyperspace().getFleets());
        for (LocationAPI locationAPI : Global.getSector().getAllLocations())
            allFleets.addAll(locationAPI.getFleets());

        allFleets.remove(null);

        return allFleets;
    }

    public static Set<CampaignFleetAPI> findFleetWithMemKey(String memKey) {
        return findFleetWithMemKey(memKey, getAllFleets());
    }

    public static Set<CampaignFleetAPI> findFleetWithMemKey(String memKey, Collection<CampaignFleetAPI> campaignFleetAPIList) {
        Set<CampaignFleetAPI> returnSet = new HashSet<>();
        for (CampaignFleetAPI campaignFleetAPI : campaignFleetAPIList)
            if (campaignFleetAPI.getMemoryWithoutUpdate().contains(memKey))
                returnSet.add(campaignFleetAPI);

        return returnSet;
    }

    public static FleetMemberAPI getShipWithHighestFP(List<FleetMemberAPI> fleet) {
        FleetMemberAPI highestFP = null;
        for (FleetMemberAPI member : fleet) {
            if (isNull(highestFP)) highestFP = member;
            else if (member.getFleetPointCost() > highestFP.getFleetPointCost())
                highestFP = member;
        }
        return highestFP;
    }
}
