package de.schafunschaf.bountiesexpanded.helper;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

public class MiscBountyUtils {
    public static float getUpdatedRep(FactionAPI faction) {
        ReputationChangeTracker repChangeTracker = SharedData.getData().getPlayerActivityTracker().getRepChangeTracker();
        repChangeTracker.advance(0);
        return repChangeTracker.getDataFor(faction.getId()).getLastValue();
    }
}
