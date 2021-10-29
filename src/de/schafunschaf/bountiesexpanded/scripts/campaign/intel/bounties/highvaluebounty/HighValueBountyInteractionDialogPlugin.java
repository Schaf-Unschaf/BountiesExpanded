package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

public class HighValueBountyInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {
    public HighValueBountyInteractionDialogPlugin() {
        this(null);
    }

    public HighValueBountyInteractionDialogPlugin(FleetInteractionDialogPluginImpl.FIDConfig params) {
        super(params);
        context = new HighValueBountyFleetEncounterContext();
    }


}
