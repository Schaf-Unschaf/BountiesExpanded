package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.HighValueBountyFleetEncounterContext;

public class HighValueBountyInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {
    public HighValueBountyInteractionDialogPlugin() {
        this(null);
    }

    public HighValueBountyInteractionDialogPlugin(FleetInteractionDialogPluginImpl.FIDConfig params) {
        super(params);
        context = new HighValueBountyFleetEncounterContext();
    }


}
