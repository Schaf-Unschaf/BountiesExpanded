package de.schafunschaf.bountiesexpanded.helper;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.TriggeredMissionManager;
import lombok.extern.log4j.Log4j;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class ModUpdateHelper {
    public static void prepareForUpdate() {
        Settings.prepareUpdate = true;
        HighValueBountyManager.getInstance().saveCompletedBountyData();

        uninstallManager(SkirmishBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        uninstallManager(AssassinationBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        uninstallManager(HighValueBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(HighValueBountyManager.KEY);
        uninstallManager(HVBRevengeManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(HVBRevengeManager.KEY);
        uninstallManager(WarCriminalManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(WarCriminalManager.KEY);
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

        if (Settings.highValueBountyActive) addHighValueBountyManager();
        else {
            HighValueBountyManager highValueBountyManager = HighValueBountyManager.getInstance();
            if (isNotNull(highValueBountyManager))
                highValueBountyManager.saveCompletedBountyData();

            uninstallManager(highValueBountyManager);
            Global.getSector().getMemoryWithoutUpdate().unset(HighValueBountyManager.KEY);
        }

        if (Settings.highValueBountyRevengeActive) addHVBRevengeManager();
        else {
            uninstallManager(HVBRevengeManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(HVBRevengeManager.KEY);
        }

        if (Settings.warCriminalActive) addWarCriminalManager();
        else {
            uninstallManager(WarCriminalManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(WarCriminalManager.KEY);
        }

        if (Settings.triggeredEventsActive) addTriggeredMissionManager();
        else {
            uninstallManager(TriggeredMissionManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(TriggeredMissionManager.KEY);
        }

        RareFlagshipManager.loadRareFlagshipData();
        addCampaignPlugins();
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

    private static void addHighValueBountyManager() {
        if (!Global.getSector().hasScript(HighValueBountyManager.class)) {
            Global.getSector().addScript(new HighValueBountyManager());
            log.info("BountiesExpanded: HighValueBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing HighValueBountyManager");
            printCompletedBounties();
        }
    }

    private static void addHVBRevengeManager() {
        if (!Global.getSector().hasScript(HVBRevengeManager.class)) {
            Global.getSector().addScript(new HVBRevengeManager());
            log.info("BountiesExpanded: HVBRevengeManager added");
        } else {
            log.info("BountiesExpanded: Found existing HVBRevengeManager");
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

    private static void printCompletedBounties() {
        HighValueBountyManager bountyManager = HighValueBountyManager.getInstance();
        log.info("BountiesExpanded: List of completed HVBs:");
        for (String completedBounty : bountyManager.getCompletedBounties()) {
            log.info(completedBounty);
        }
    }
}