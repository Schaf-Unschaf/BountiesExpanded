package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedSpawnSkirmishBounty implements BaseCommand {
    public BountiesExpandedSpawnSkirmishBounty() {
    }

    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        SkirmishBountyManager manager = SkirmishBountyManager.getInstance();
        if (isNull(manager)) {
            Console.showMessage("the SkirmishBountyManager instance is missing!");
            return CommandResult.ERROR;
        }

        Console.showMessage("attempting to spawn SkirmishBounty...");
        SkirmishBountyIntel skirmishBountyEvent = manager.createSkirmishBountyEvent();
        if (isNotNull(skirmishBountyEvent)) {
            manager.addActive(skirmishBountyEvent);
            Console.showMessage("it worked!");
            Console.showMessage("Spawned SkirmishBounty at " + skirmishBountyEvent.getHideout().getName());
            return CommandResult.SUCCESS;
        }

        Console.showMessage("it didn't work!");
        return CommandResult.ERROR;
    }
}
