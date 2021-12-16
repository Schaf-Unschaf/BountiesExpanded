package de.schafunschaf.bountiesexpanded.helper.fleet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

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

    public static List<FleetMemberAPI> orderListBySize(@NotNull List<FleetMemberAPI> fleetMemberList) {
        if (isNullOrEmpty(fleetMemberList))
            return new ArrayList<>();

        FleetMemberAPI flagship = null;
        List<FleetMemberAPI> sortedList = new ArrayList<>();
        List<FleetMemberAPI> frigateList = new ArrayList<>();
        List<FleetMemberAPI> destroyerList = new ArrayList<>();
        List<FleetMemberAPI> cruiserList = new ArrayList<>();
        List<FleetMemberAPI> capitalList = new ArrayList<>();

        for (FleetMemberAPI fleetMemberAPI : fleetMemberList) {
            if (fleetMemberAPI.isFlagship()) {
                flagship = fleetMemberAPI;
                continue;
            }
            switch (fleetMemberAPI.getHullSpec().getHullSize()) {
                case FRIGATE:
                    frigateList.add(fleetMemberAPI);
                    break;
                case DESTROYER:
                    destroyerList.add(fleetMemberAPI);
                    break;
                case CRUISER:
                    cruiserList.add(fleetMemberAPI);
                    break;
                case CAPITAL_SHIP:
                    capitalList.add(fleetMemberAPI);
                    break;
            }
        }
        sortedList.add(flagship);
        sortedList.addAll(capitalList);
        sortedList.addAll(cruiserList);
        sortedList.addAll(destroyerList);
        sortedList.addAll(frigateList);
        sortedList.remove(null);

        return sortedList;
    }
}
