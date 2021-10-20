package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.retrieval;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

public class RareFlagshipData {
    public static final Logger log = Global.getLogger(RareFlagshipData.class);

    private final String flagshipID;
    private final String flagshipVariantID;
    private final String factionID;
    private final float weight;
    private final String intelDescription;

    public RareFlagshipData(String flagshipID, String flagshipVariantID, String factionID, float weight, String intelDescription) {
        this.flagshipID = flagshipID;
        this.flagshipVariantID = flagshipVariantID;
        this.factionID = factionID;
        this.weight = weight;
        this.intelDescription = intelDescription;
    }
}
