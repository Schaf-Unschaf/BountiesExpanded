package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyInteractionDialogPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;

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
        if (interactionTarget.getMemoryWithoutUpdate().contains(HighValueBountyManager.HIGH_VALUE_BOUNTY_FLEET_KEY)) {
            InteractionDialogPlugin interactionDialogPlugin = new HighValueBountyInteractionDialogPlugin();
            return new PluginPick<>(interactionDialogPlugin, PickPriority.MOD_SPECIFIC);
        }
        if (interactionTarget.getMemoryWithoutUpdate().contains(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY)) {
            SkirmishBountyEntity skirmishBountyEntity = (SkirmishBountyEntity) interactionTarget.getMemoryWithoutUpdate().get(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY);
            ReputationChangeTracker repChangeTracker = SharedData.getData().getPlayerActivityTracker().getRepChangeTracker();
            float lastValue = repChangeTracker.getDataFor(skirmishBountyEntity.getTargetedFaction().getId()).getLastValue();
            skirmishBountyEntity.setTargetRepBeforeBattle(lastValue);
        }
        if (interactionTarget.getMemoryWithoutUpdate().contains(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY)) {
            AssassinationBountyEntity assassinationBountyEntity = (AssassinationBountyEntity) interactionTarget.getMemoryWithoutUpdate().get(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY);
            ReputationChangeTracker repChangeTracker = SharedData.getData().getPlayerActivityTracker().getRepChangeTracker();
            float lastValue = repChangeTracker.getDataFor(assassinationBountyEntity.getTargetedFaction().getId()).getLastValue();
            assassinationBountyEntity.setTargetRepBeforeBattle(lastValue);
        }
        return null;
    }
}
