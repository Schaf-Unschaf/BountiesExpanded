package de.schafunschaf.bountiesexpanded.scripts.console.commands;

import de.schafunschaf.bountiesexpanded.helper.ModUpdateHelper;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class BountiesExpandedPrepareUpdate implements BaseCommand {
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        ModUpdateHelper.prepareForUpdate();
        Console.showMessage("Preparing Mod for update...");
        Console.showMessage("Please wait 2 days, save game, quit and update the Mod");
        return CommandResult.SUCCESS;
    }
}