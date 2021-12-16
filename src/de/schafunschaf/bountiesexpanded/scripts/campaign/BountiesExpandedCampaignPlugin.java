package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.NoShipRecoveryFleetEncounterContext;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins.NoRecoveryInteractionDialogPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins.RetrievalInteractionDialogPlugin;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedCampaignPlugin extends BaseCampaignPlugin {
    public static final String PLUGIN_ID = "BountiesExpandedCampaignPlugin";

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        MemoryAPI memory = interactionTarget.getMemoryWithoutUpdate();
        String fleetIdentifierKey = (String) memory.get(EntityProvider.FLEET_IDENTIFIER_KEY);

        if (isNotNull(fleetIdentifierKey)) {
            setTargetRepBeforeBattle((BountyEntity) interactionTarget.getMemoryWithoutUpdate().get(fleetIdentifierKey));
        }

        // Plugin for guaranteed Ship-Recovery
        if (interactionTarget.getMemoryWithoutUpdate().contains(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY)) {
            InteractionDialogPlugin interactionDialogPlugin = new RetrievalInteractionDialogPlugin();
            return new PluginPick<>(interactionDialogPlugin, PickPriority.MOD_SPECIFIC);
        }

        // Plugin for no Ship-Recovery (Destruction and Obliteration missions)
        if (interactionTarget.getMemoryWithoutUpdate().contains(NoShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_NO_RECOVERY)) {
            InteractionDialogPlugin interactionDialogPlugin = new NoRecoveryInteractionDialogPlugin();
            return new PluginPick<>(interactionDialogPlugin, PickPriority.MOD_SPECIFIC);
        }

        return null;
    }

    private void setTargetRepBeforeBattle(BountyEntity bountyEntity) {
        if (isNull(bountyEntity))
            return;

        ReputationChangeTracker repChangeTracker = SharedData.getData().getPlayerActivityTracker().getRepChangeTracker();
        float lastValue = repChangeTracker.getDataFor(bountyEntity.getTargetedFaction().getId()).getLastValue();
        bountyEntity.setTargetRepBeforeBattle(lastValue);
    }
}
