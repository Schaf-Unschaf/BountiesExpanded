package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import com.fs.starfarer.api.EveryFrameScript;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedSpawnHVB implements BaseCommand {
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

        Console.showMessage("attempting to spawn HVB with ID: " + args);
        EveryFrameScript bounty = manager.forceSpawn(args);
        if (isNotNull(bounty)) {
            Console.showMessage("it worked!");
            Console.showMessage("current active HVBs are: " + manager.getActiveBounties());
            return CommandResult.SUCCESS;

        }
        Console.showMessage("it didn't work!");
        return CommandResult.ERROR;
    }
}