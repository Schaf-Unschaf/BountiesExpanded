package de.schafunschaf.bountiesexpanded.helper.faction;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class MiscFactionUtils {
    public static int getTotalPopulation(FactionAPI factionAPI) {
        if (isNull(factionAPI))
            return 0;
        int population = 0;
        List<MarketAPI> factionMarkets = Misc.getFactionMarkets(factionAPI);

        for (MarketAPI market : factionMarkets) population += market.getSize();

        return population;
    }

    public static boolean canFactionOfferBounties(FactionAPI factionAPI) {
        if (isNull(factionAPI))
            return false;

        int totalPopulation = getTotalPopulation(factionAPI);

        return totalPopulation > 0;
    }
}