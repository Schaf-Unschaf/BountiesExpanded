package de.schafunschaf.bountiesexpanded.helper;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyManager;
import data.scripts.VayraModPlugin;
import data.scripts.campaign.intel.VayraPersonBountyIntel;
import data.scripts.campaign.intel.VayraPersonBountyManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter.DeserterBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate.PirateBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.TriggeredMissionManager;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class ModInitHelper {
    public static void prepareForUpdate() {
        Settings.prepareUpdate = true;

        uninstallManager(SkirmishBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        uninstallManager(AssassinationBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        uninstallManager(WarCriminalManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(WarCriminalManager.KEY);
        uninstallManager(PirateBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(PirateBountyManager.KEY);
        uninstallManager(DeserterBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(DeserterBountyManager.KEY);
        uninstallManager(TriggeredMissionManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(TriggeredMissionManager.KEY);

        uninstallPlugins();
        Settings.prepareUpdate = false;
    }

    public static void uninstallManager(BaseEventManager bountyManager) {
        if (isNull(bountyManager))
            return;
        for (EveryFrameScript script : bountyManager.getActive()) {
            ((BaseBountyIntel) script).endImmediately();
            Global.getSector().removeScript(script);
        }
        Global.getSector().removeScript(bountyManager);
    }

    private static void uninstallPlugins() {
        Global.getSector().unregisterPlugin(BountiesExpandedCampaignPlugin.PLUGIN_ID);
        removeCampaignManager();
    }

    private static void removeCampaignManager() {
        if (Global.getSector().hasScript(BountiesExpandedCampaignManager.class)) {
            for (EveryFrameScript script : Global.getSector().getScripts())
                if (script instanceof BountiesExpandedCampaignManager) {
                    Global.getSector().removeScript(script);
                    break;
                }
        }
    }

    public static void initManagerAndPlugins() {
        if (Settings.skirmishActive) addSkirmishManager();
        else {
            uninstallManager(SkirmishBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        }

        if (Settings.assassinationActive) addAssassinationManager();
        else {
            uninstallManager(AssassinationBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        }

        if (Settings.warCriminalActive) addWarCriminalManager();
        else {
            uninstallManager(WarCriminalManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(WarCriminalManager.KEY);
        }

        if (Settings.pirateBountyActive) addPirateBountyManager();
        else {
            uninstallManager(PirateBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(PirateBountyManager.KEY);
        }

        if (Settings.deserterBountyActive) addDeserterBountyManager();
        else {
            uninstallManager(DeserterBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(DeserterBountyManager.KEY);
        }

        if (Settings.triggeredEventsActive) addTriggeredMissionManager();
        else {
            uninstallManager(TriggeredMissionManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(TriggeredMissionManager.KEY);
        }

        disableVanillaAndModdedBounties();

        RareFlagshipManager.loadRareFlagshipData();
        addCampaignPlugins();
    }

    private static void disableVanillaAndModdedBounties() {
        if (Settings.disableVanillaBounties)
            removeVanillaBountyManager();

        if (Settings.disableVayraBounties && Global.getSettings().getModManager().isModEnabled("vayrasector"))
            removeVayraBountyManager();
    }

    private static void addSkirmishManager() {
        if (!Global.getSector().hasScript(SkirmishBountyManager.class)) {
            Global.getSector().addScript(new SkirmishBountyManager());
            log.info("BountiesExpanded: SkirmishBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing SkirmishBountyManager");
        }
    }

    private static void addAssassinationManager() {
        if (!Global.getSector().hasScript(AssassinationBountyManager.class)) {
            Global.getSector().addScript(new AssassinationBountyManager());
            log.info("BountiesExpanded: AssassinationBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing AssassinationBountyManager");
        }
    }

    private static void addWarCriminalManager() {
        if (!Global.getSector().hasScript(WarCriminalManager.class)) {
            Global.getSector().addScript(new WarCriminalManager());
            log.info("BountiesExpanded: WarCriminalManager added");
        } else {
            log.info("BountiesExpanded: Found existing WarCriminalManager");
        }
    }

    private static void addPirateBountyManager() {
        if (!Global.getSector().hasScript(PirateBountyManager.class)) {
            Global.getSector().addScript(new PirateBountyManager());
            log.info("BountiesExpanded: PirateBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing PirateBountyManager");
        }
    }

    private static void addDeserterBountyManager() {
        if (!Global.getSector().hasScript(DeserterBountyManager.class)) {
            Global.getSector().addScript(new DeserterBountyManager());
            log.info("BountiesExpanded: DeserterBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing DeserterBountyManager");
        }
    }

    private static void addTriggeredMissionManager() {
        if (!Global.getSector().hasScript(TriggeredMissionManager.class)) {
            Global.getSector().addScript(new TriggeredMissionManager());
            log.info("BountiesExpanded: TriggeredMissionManager added");
        } else {
            log.info("BountiesExpanded: Found existing TriggeredMissionManager");
        }
    }

    private static void addCampaignPlugins() {
        Global.getSector().registerPlugin(new BountiesExpandedCampaignPlugin());

        if (!Global.getSector().hasScript(BountiesExpandedCampaignManager.class)) {
            Global.getSector().addScript(new BountiesExpandedCampaignManager());
            log.info("BountiesExpanded: BountiesExpandedCampaignManager added");
        } else {
            log.info("BountiesExpanded: Found existing BountiesExpandedCampaignManager");
        }
    }

    private static void removeVanillaBountyManager() {
        PersonBountyManager personBountyManager = PersonBountyManager.getInstance();
        if (ComparisonTools.isNull(personBountyManager))
            return;

        List<EveryFrameScript> activeBounties = personBountyManager.getActive();
        for (EveryFrameScript bounty : activeBounties) {
            ((PersonBountyIntel) bounty).endImmediately();
            Global.getSector().removeScript(bounty);
        }
        Global.getSector().removeScript(personBountyManager);
    }

    private static void removeVayraBountyManager() {
        VayraModPlugin.PIRATE_BOUNTY_MODE = VayraModPlugin.PirateMode.NEVER;
        BountyEventData.getParticipatingFactions().remove(Factions.PIRATES);
        VayraPersonBountyManager personBountyManager = VayraPersonBountyManager.getInstance();
        if (ComparisonTools.isNull(personBountyManager))
            return;

        List<EveryFrameScript> activeBounties = personBountyManager.getActive();
        for (EveryFrameScript bounty : activeBounties) {
            ((VayraPersonBountyIntel) bounty).endImmediately();
            Global.getSector().removeScript(bounty);
        }
        Global.getSector().removeScript(personBountyManager);
    }
}