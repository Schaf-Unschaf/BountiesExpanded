package de.schafunschaf.bountiesexpanded.helper.market;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class MarketUtils {
    public static MarketAPI getBestMarketForQuality(FactionAPI faction) {
        float maxQuality = 0;
        MarketAPI market = null;
        for (MarketAPI factionMarket : Misc.getFactionMarkets(faction))
            if (factionMarket.getShipQualityFactor() > maxQuality) {
                market = factionMarket;
                maxQuality = market.getShipQualityFactor();
            }

        return market;
    }

    public static MarketAPI createFakeMarket(FactionAPI faction) {
        MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
        market.getStability().modifyFlat("fake", 10000.0F);
        market.setFactionId(faction.getId());
        SectorEntityToken token = Global.getSector().getHyperspace().createToken(0.0F, 0.0F);
        market.setPrimaryEntity(token);
        market.getStats().getDynamic().getMod("fleet_quality_mod").modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
        market.getStats().getDynamic().getMod("combat_fleet_size_mult").modifyFlat("fake", 1.0F);

        return market;
    }

    public static MarketAPI getRandomFactionMarket(FactionAPI faction) {
        if (isNull(faction))
            return null;

        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        List<MarketAPI> factionMarkets = Misc.getFactionMarkets(faction);
        picker.addAll(factionMarkets);

        return picker.isEmpty() ? null : picker.pick();
    }
}
