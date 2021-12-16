package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.NoShipRecoveryFleetEncounterContext;

public class NoRecoveryInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {
    public NoRecoveryInteractionDialogPlugin() {
        this(null);
    }

    public NoRecoveryInteractionDialogPlugin(FIDConfig params) {
        super(params);
        context = new NoShipRecoveryFleetEncounterContext();
    }
}