package de.schafunschaf.bountiesexpanded.helper.intel;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BountyEventData {
    public static PersonBountyEventData getSharedData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    public static List<String> getParticipatingFactions() {
        Set<String> participatingFactions = new LinkedHashSet<>(getSharedData().getParticipatingFactions());
        participatingFactions.remove(null);
        return new ArrayList<>(participatingFactions);
    }

    public static boolean isParticipating(MarketAPI market) {
        return getSharedData().isParticipating(market.getFactionId());
    }
}
