package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class BountiesExpandedRemoveFaultyBounties implements BaseCommand {
    public BountiesExpandedRemoveFaultyBounties() {
    }

    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        int totalFleetsRemoved = 0;
        totalFleetsRemoved += clearFleetsAtLocation(Global.getSector().getHyperspace());
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            totalFleetsRemoved += clearFleetsAtLocation(location);
        }

        Console.showMessage("Removed " + totalFleetsRemoved + " faulty Skirmish-Fleets");
        return CommandResult.SUCCESS;
    }

    private int clearFleetsAtLocation(LocationAPI location) {
        if (isNull(location))
            return 0;

        List<CampaignFleetAPI> fleetsToRemove = new ArrayList<>();
        List<CampaignFleetAPI> locationFleets = location.getFleets();
        if (isNullOrEmpty(locationFleets))
            return 0;

        for (CampaignFleetAPI fleet : locationFleets) {
            FleetAssignmentDataAPI currentAssignment = fleet.getCurrentAssignment();
            if (isNull(currentAssignment))
                continue;

            String actionText = currentAssignment.getActionText();
            if (isNull(actionText))
                continue;

            String fleetName = fleet.getName();
            if (isNull(fleetName))
                continue;

            if (actionText.equals(SkirmishBountyManager.FLEET_ACTION_TEXT) && !fleetName.equals(SkirmishBountyManager.FLEET_NAME))
                fleetsToRemove.add(fleet);
        }

        if (!isNullOrEmpty(fleetsToRemove)) {
            for (int i = 0; i < fleetsToRemove.size(); i++) {
                CampaignFleetAPI fleet = fleetsToRemove.get(i);
                fleet.despawn();
            }
        }

        return fleetsToRemove.size();
    }
}
