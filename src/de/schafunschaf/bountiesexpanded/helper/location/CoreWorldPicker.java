package de.schafunschaf.bountiesexpanded.helper.location;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.Settings;

import java.util.ArrayList;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

public class CoreWorldPicker {
    public static SectorEntityToken pickRandomHideout(List<MarketAPI> marketList) {
        List<MarketAPI> filteredMarketList = new ArrayList<>();
        List<MarketAPI> allMarkets;
        if (isNull(marketList))
            allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        else if (marketList.isEmpty())
            return null;
        else
            allMarkets = marketList;

        for (MarketAPI market : allMarkets) {
            if (Settings.IGNORE_PLAYER_MARKETS && market.isPlayerOwned())
                continue;
            if (!market.isHidden() && !market.isInHyperspace())
                filteredMarketList.add(market);
        }
        return pickHideout(filteredMarketList);
    }

    public static SectorEntityToken pickRandomHideout() {
        return pickRandomHideout(null);
    }

    public static SectorEntityToken pickSafeHideout(FactionAPI faction, List<MarketAPI> marketList) {
        List<MarketAPI> filteredSafeMarketList = new ArrayList<>();
        List<MarketAPI> factionMarketList;
        for (FactionAPI checkedFaction : Global.getSector().getAllFactions()) {
            if (!faction.isHostileTo(checkedFaction)) {
                factionMarketList = Misc.getFactionMarkets(checkedFaction);
                if (isNotNull(marketList)) {
                    factionMarketList.retainAll(marketList);
                }

                for (MarketAPI market : factionMarketList) {
                    if (Settings.IGNORE_PLAYER_MARKETS && market.isPlayerOwned())
                        continue;
                    if (!market.isHidden() && !market.isInHyperspace())
                        filteredSafeMarketList.add(market);
                }
            }
        }
        return pickHideout(filteredSafeMarketList);
    }

    public static SectorEntityToken pickSafeHideout(FactionAPI faction) {
        return pickSafeHideout(faction, null);
    }

    public static SectorEntityToken pickFactionHideout(FactionAPI faction, List<MarketAPI> marketList) {
        List<MarketAPI> filteredFactionMarketList = new ArrayList<>();
        List<MarketAPI> factionMarketList;
        if (isNull(marketList))
            factionMarketList = Misc.getFactionMarkets(faction);
        else if (marketList.isEmpty())
            return null;
        else
            factionMarketList = marketList;

        for (MarketAPI market : factionMarketList) {
            if (Settings.IGNORE_PLAYER_MARKETS && market.isPlayerOwned())
                continue;
            if (!market.isHidden() && !market.isInHyperspace())
                filteredFactionMarketList.add(market);
        }
        return pickHideout(filteredFactionMarketList);
    }

    public static SectorEntityToken pickFactionHideout(FactionAPI faction) {
        return pickFactionHideout(faction, null);
    }

    public static List<MarketAPI> getDistantMarkets(float minDistance, SectorEntityToken sectorEntityToken) {
        if (isNull(sectorEntityToken))
            return null;
        List<MarketAPI> marketList = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (Misc.getDistanceLY(sectorEntityToken.getStarSystem().getLocation(), system.getLocation()) >= minDistance) {
                for (SectorEntityToken systemEntity : system.getAllEntities()) {
                    marketList.add(systemEntity.getMarket());
                }
            }
        }
        return marketList;
    }

    private static SectorEntityToken pickHideout(List<MarketAPI> marketList) {
        if (isNullOrEmpty(marketList))
            return null;
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        picker.addAll(marketList);
        return picker.pick().getPrimaryEntity();
    }
}
