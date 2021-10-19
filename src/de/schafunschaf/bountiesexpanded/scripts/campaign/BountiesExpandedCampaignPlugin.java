package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyInteractionDialogPlugin;

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
        if (interactionTarget.getMemoryWithoutUpdate().contains("$bountiesExpanded_highValueBounty")) {
            InteractionDialogPlugin interactionDialogPlugin = new HighValueBountyInteractionDialogPlugin();
            return new PluginPick<>(interactionDialogPlugin, PickPriority.MOD_SPECIFIC);
        }
        return null;
    }
}
