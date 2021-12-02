package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.shared.ReputationChangeTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters.GuaranteedShipRecoveryFleetEncounterContext;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins.HighValueBountyInteractionDialogPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.plugins.RetrievalInteractionDialogPlugin;

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

        // Plugin for guaranteed Ship-Recovery
        if (interactionTarget.getMemoryWithoutUpdate().contains(GuaranteedShipRecoveryFleetEncounterContext.BOUNTIES_EXPANDED_GUARANTEED_RECOVERY)) {
            InteractionDialogPlugin interactionDialogPlugin = new RetrievalInteractionDialogPlugin();
            return new PluginPick<>(interactionDialogPlugin, PickPriority.MOD_SPECIFIC);
        }

        if (interactionTarget.getMemoryWithoutUpdate().contains(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY)) {
            SkirmishBountyEntity skirmishBountyEntity = (SkirmishBountyEntity) interactionTarget.getMemoryWithoutUpdate().get(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY);
            setTargetRepBeforeBattle(skirmishBountyEntity);
            return null;
        }

        if (interactionTarget.getMemoryWithoutUpdate().contains(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY)) {
            AssassinationBountyEntity assassinationBountyEntity = (AssassinationBountyEntity) interactionTarget.getMemoryWithoutUpdate().get(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY);
            setTargetRepBeforeBattle(assassinationBountyEntity);
            return null;
        }

        if (interactionTarget.getMemoryWithoutUpdate().contains(WarCriminalManager.WAR_CRIMINAL_BOUNTY_FLEET_KEY)) {
            WarCriminalEntity warCriminalEntity = (WarCriminalEntity) interactionTarget.getMemoryWithoutUpdate().get(WarCriminalManager.WAR_CRIMINAL_BOUNTY_FLEET_KEY);
            setTargetRepBeforeBattle(warCriminalEntity);
        }

        return null;
    }

    private void setTargetRepBeforeBattle(BountyEntity bountyEntity) {
        ReputationChangeTracker repChangeTracker = SharedData.getData().getPlayerActivityTracker().getRepChangeTracker();
        float lastValue = repChangeTracker.getDataFor(bountyEntity.getTargetedFaction().getId()).getLastValue();
        bountyEntity.setTargetRepBeforeBattle(lastValue);
    }
}
