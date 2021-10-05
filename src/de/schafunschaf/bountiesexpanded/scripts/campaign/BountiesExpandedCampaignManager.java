package de.schafunschaf.bountiesexpanded.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;

import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class BountiesExpandedCampaignManager extends BaseCampaignEventListener implements EveryFrameScript {
    public BountiesExpandedCampaignManager() {
        super(true);
    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        HighValueBountyManager bountyManager = HighValueBountyManager.getInstance();
        HighValueBountyData bountyData = null;
        Set<String> flagshipVariantIds = bountyManager.getAllVariantIds();
        List<FleetEncounterContextPlugin.FleetMemberData> enemyCasualties = plugin.getWinnerData().getEnemyCasualties();

        if (isNullOrEmpty(enemyCasualties) || isNullOrEmpty(flagshipVariantIds)) {
            return;
        }

        for (FleetEncounterContextPlugin.FleetMemberData shipDestroyed : enemyCasualties) {
            String hullVariantId = shipDestroyed.getMember().getVariant().getHullVariantId();
            if (flagshipVariantIds.contains(hullVariantId)) {
                String bountyId = bountyManager.getBountyIdFromFlagshipVariantId(hullVariantId);
                bountyData = bountyManager.getBounty(bountyId);
            }
        }

        if (isNotNull(bountyData)) {
            if (!isNullOrEmpty(bountyData.specialItemRewards))
                for (String specialItemReward : bountyData.specialItemRewards)
                    loot.addSpecial(new SpecialItemData(specialItemReward, null), 1);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

    }
}
