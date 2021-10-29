package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import java.util.Set;

public class RareFlagshipData {
    private final String flagshipID;
    private final String flagshipVariantID;
    private final Set<String> factionIDs;
    private final float weight;

    public RareFlagshipData(String flagshipID, String flagshipVariantID, Set<String> factionIDs, float weight) {
        this.flagshipID = flagshipID;
        this.flagshipVariantID = flagshipVariantID;
        this.factionIDs = factionIDs;
        this.weight = weight;
    }

    public String getFlagshipID() {
        return flagshipID;
    }

    public String getFlagshipVariantID() {
        return flagshipVariantID;
    }

    public Set<String> getFactionIDs() {
        return factionIDs;
    }

    public float getWeight() {
        return weight;
    }
}

