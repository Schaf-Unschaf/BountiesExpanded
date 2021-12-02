package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;

public class RetrievalInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {
    public RetrievalInteractionDialogPlugin() {
        this(null);
    }

    public RetrievalInteractionDialogPlugin(FIDConfig params) {
        super(params);
        context = new GuaranteedShipRecoveryFleetEncounterContext();
    }
}