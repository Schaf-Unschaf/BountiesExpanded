package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedShowHVBInfo implements BaseCommand {
    public BountiesExpandedShowHVBInfo() {
    }

    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        } else {
            HighValueBountyManager manager = HighValueBountyManager.getInstance();
            if (isNull(manager)) {
                Console.showMessage("the HighValueBountyManager instance is missing!");
                return CommandResult.ERROR;
            } else {
                Console.showMessage("List of all available HVBs: " + manager.getBountiesList());
                Console.showMessage("List of active HVBs: " + manager.getActiveBounties());
                Console.showMessage("List of completed HVBs: " + manager.getCompletedBounties());
                return CommandResult.SUCCESS;
            }
        }
    }

}
