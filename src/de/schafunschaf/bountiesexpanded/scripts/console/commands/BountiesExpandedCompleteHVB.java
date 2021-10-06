package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedCompleteHVB implements BaseCommand {
    public BountiesExpandedCompleteHVB() {
    }

    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        HighValueBountyManager manager = HighValueBountyManager.getInstance();
        if (isNull(manager)) {
            Console.showMessage("the HighValueBountyManager instance is missing!");
            return CommandResult.ERROR;
        }

        if (isNull(manager.getBounty(args))) {
            Console.showMessage("HVB '" + args + "' does not exist!");
            return CommandResult.ERROR;
        }

        if (manager.getCompletedBounties().contains(args)) {
            Console.showMessage("HVB '" + args + "' is already completed.");
            return CommandResult.ERROR;
        }

        manager.markBountyAsCompleted(args);
        Console.showMessage("HVB '" + args + "' is now marked as completed.");
        return CommandResult.SUCCESS;
    }
}
