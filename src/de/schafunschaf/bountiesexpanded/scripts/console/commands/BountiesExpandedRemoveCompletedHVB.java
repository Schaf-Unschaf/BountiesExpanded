package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedRemoveCompletedHVB implements BaseCommand {
    public BountiesExpandedRemoveCompletedHVB() {
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

        if (manager.getCompletedBounties().contains(args)) {
            Console.showMessage("Removing '" + args + "' from completed HVBs.");
            manager.getCompletedBounties().remove(args);
            return CommandResult.SUCCESS;
        }

        Console.showMessage("HVB '" + args + "' was not in the completed HVB list.");
        return CommandResult.ERROR;
    }
}
