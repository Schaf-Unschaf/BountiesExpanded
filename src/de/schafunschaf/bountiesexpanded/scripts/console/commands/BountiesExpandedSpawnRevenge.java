package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import com.fs.starfarer.api.EveryFrameScript;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedSpawnRevenge implements BaseCommand {
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        HVBRevengeManager manager = HVBRevengeManager.getInstance();
        if (isNull(manager)) {
            Console.showMessage("the HVBRevengeManager instance is missing!");
            return CommandResult.ERROR;
        }

        Console.showMessage("attempting to spawn HVB with ID: " + args);
        EveryFrameScript bounty = manager.forceSpawn();
        if (isNotNull(bounty)) {
            Console.showMessage("it worked!");
            return CommandResult.SUCCESS;

        }
        Console.showMessage("it didn't work!");
        return CommandResult.ERROR;
    }
}
