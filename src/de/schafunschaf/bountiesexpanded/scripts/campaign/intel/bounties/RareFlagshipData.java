package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import lombok.Getter;

import java.util.Set;

@Getter
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
}

