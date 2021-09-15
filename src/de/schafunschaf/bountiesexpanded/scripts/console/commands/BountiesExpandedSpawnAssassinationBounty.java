package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class BountiesExpandedSpawnAssassinationBounty implements BaseCommand {
    public BountiesExpandedSpawnAssassinationBounty() {
    }

    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        } else {
            AssassinationBountyManager manager = AssassinationBountyManager.getInstance();
            if (isNull(manager)) {
                Console.showMessage("the AssassinationBountyManager instance is missing!");
                return CommandResult.ERROR;
            } else {
                Console.showMessage("attempting to spawn AssassinationBounty...");
                AssassinationBountyIntel assassinationBountyEvent = manager.createAssassinationBountyEvent();
                if (isNotNull(assassinationBountyEvent)) {
                    Console.showMessage("it worked!");
                    Console.showMessage("Spawned AssassinationBounty at " + assassinationBountyEvent.getHideout().getName());
                    return CommandResult.SUCCESS;
                } else {
                    Console.showMessage("it didn't work!");
                    return CommandResult.ERROR;
                }
            }
        }
    }
}
