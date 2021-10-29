package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import data.scripts.VayraModPlugin;
import data.scripts.campaign.intel.VayraUniqueBountyIntel;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUtils;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.ExternalDataSupplier.*;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;


public class BountiesExpandedPlugin extends BaseModPlugin {
    public static final String DEFAULT_BLACKLIST_FILE = "data/config/bountiesExpanded/default_blacklist.json";
    public static final String SETTINGS_FILE = "bounties_expanded_settings.ini";
    public static final String NAME_STRINGS_FILE = "data/config/bountiesExpanded/name_strings.json";
    public static final String VAYRA_UNIQUE_BOUNTIES_FILE = "data/config/vayraBounties/unique_bounty_data.csv";
    public static final String RARE_FLAGSHIPS_FILE = "data/config/vayraBounties/rare_flagships.csv";
    public static final Logger log = Global.getLogger(BountiesExpandedPlugin.class);

    public static void prepareForUpdate() {
        Settings.PREPARE_UPDATE = true;
        HighValueBountyManager.getInstance().saveCompletedBountyData();

        uninstallManager(SkirmishBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        uninstallManager(AssassinationBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        uninstallManager(HighValueBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(HighValueBountyManager.KEY);
        uninstallManager(HVBRevengeManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(HVBRevengeManager.KEY);

        uninstallPlugins();
        Settings.PREPARE_UPDATE = false;
    }

    private static void uninstallManager(BaseEventManager bountyManager) {
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

    @Override
    public void onApplicationLoad() {
        initBountiesExpanded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Settings.PREPARE_UPDATE) {
            prepareForUpdate();
            return;
        }

        setBlacklists();

        initManagerAndPlugins();

        reloadSMods();

        if (Settings.HIGH_VALUE_BOUNTY_ACTIVE && Global.getSettings().getModManager().isModEnabled("vayrasector"))
            removeVayraUniqueBounties();

        if (Settings.SHEEP_DEBUG) printDebugInfo();
    }

    private void removeVayraUniqueBounties() {
        VayraModPlugin.UNIQUE_BOUNTIES = false;
        VayraUniqueBountyManager vayraUniqueBountyManager = VayraUniqueBountyManager.getInstance();
        if (isNotNull(vayraUniqueBountyManager)) {
            for (String bountyId : vayraUniqueBountyManager.getSpentBountiesList()) {
                if (bountyId.equals("unique spentBounties was null, adding empty list"))
                    continue;
                HighValueBountyManager.getInstance().markBountyAsCompleted(bountyId);
            }

            List<EveryFrameScript> activeBounties = vayraUniqueBountyManager.getActive();
            for (int i = 0; i < activeBounties.size(); i++) {
                EveryFrameScript activeBounty = activeBounties.get(i);
                ((VayraUniqueBountyIntel) activeBounty).endImmediately();
            }

            Global.getSector().removeScript(vayraUniqueBountyManager);
        }
    }

    private void initManagerAndPlugins() {
        if (Settings.SKIRMISH_ACTIVE) addSkirmishManager();
        else {
            uninstallManager(SkirmishBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        }

        if (Settings.ASSASSINATION_ACTIVE) addAssassinationManager();
        else {
            uninstallManager(AssassinationBountyManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        }

        if (Settings.HIGH_VALUE_BOUNTY_ACTIVE) addHighValueBountyManager();
        else {
            HighValueBountyManager highValueBountyManager = HighValueBountyManager.getInstance();
            if (isNotNull(highValueBountyManager))
                highValueBountyManager.saveCompletedBountyData();

            uninstallManager(highValueBountyManager);
            Global.getSector().getMemoryWithoutUpdate().unset(HighValueBountyManager.KEY);
        }

        if (Settings.HIGH_VALUE_BOUNTY_ACTIVE) addHVBRevengeManager();
        else {
            uninstallManager(HVBRevengeManager.getInstance());
            Global.getSector().getMemoryWithoutUpdate().unset(HVBRevengeManager.KEY);
        }

        RareFlagshipManager.loadRareFlagshipData();
        addCampaignPlugins();
    }

    private void reloadSMods() {
        reloadSkirmishSMods();
        reloadHVBSMods();
        reloadAssassinationSMods();
        reloadHVBRevengeSMods();
    }

    private void reloadSkirmishSMods() {
        SkirmishBountyManager bountyManager = SkirmishBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        Set<CampaignFleetAPI> skirmishFleets = FleetUtils.findFleetWithMemKey(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY);
        for (CampaignFleetAPI skirmishFleet : skirmishFleets) {
            bountyManager.upgradeShips(skirmishFleet);
        }
    }

    private void reloadHVBSMods() {
        HighValueBountyManager bountyManager = HighValueBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        Set<CampaignFleetAPI> highValueBountyFleets = FleetUtils.findFleetWithMemKey(HighValueBountyManager.HIGH_VALUE_BOUNTY_FLEET_KEY);
        for (CampaignFleetAPI hvbFleet : highValueBountyFleets) {
            bountyManager.upgradeShips(hvbFleet);
        }
    }

    private void reloadAssassinationSMods() {
        AssassinationBountyManager bountyManager = AssassinationBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        Set<CampaignFleetAPI> assassinationFleets = FleetUtils.findFleetWithMemKey(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY);
        for (CampaignFleetAPI assassinationFleet : assassinationFleets) {
            bountyManager.upgradeShips(assassinationFleet);
        }
    }

    private void reloadHVBRevengeSMods() {
        HVBRevengeManager bountyManager = HVBRevengeManager.getInstance();
        if (isNull(bountyManager))
            return;

        Set<CampaignFleetAPI> hvbRevengeFleets = FleetUtils.findFleetWithMemKey(HVBRevengeManager.HVB_REVENGE_FLEET_KEY);
        for (CampaignFleetAPI hvbRevengeFleet : hvbRevengeFleets) {
            bountyManager.upgradeShips(hvbRevengeFleet);
        }
    }

    private void initBountiesExpanded() {
        loadSettings(SETTINGS_FILE);
        loadBlacklists(DEFAULT_BLACKLIST_FILE);
        loadNameStringFiles(NAME_STRINGS_FILE);

        loadHighValueBountyData(VAYRA_UNIQUE_BOUNTIES_FILE);
    }

    private void addSkirmishManager() {
        if (!Global.getSector().hasScript(SkirmishBountyManager.class)) {
            Global.getSector().addScript(new SkirmishBountyManager());
            log.info("BountiesExpanded: SkirmishBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing SkirmishBountyManager");
        }
    }

    private void addAssassinationManager() {
        if (!Global.getSector().hasScript(AssassinationBountyManager.class)) {
            Global.getSector().addScript(new AssassinationBountyManager());
            log.info("BountiesExpanded: AssassinationBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing AssassinationBountyManager");
        }
    }

    private void addHighValueBountyManager() {
        if (!Global.getSector().hasScript(HighValueBountyManager.class)) {
            Global.getSector().addScript(new HighValueBountyManager());
            log.info("BountiesExpanded: HighValueBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing HighValueBountyManager");
            printCompletedBounties();
        }
    }

    private void addHVBRevengeManager() {
        if (!Global.getSector().hasScript(HVBRevengeManager.class)) {
            Global.getSector().addScript(new HVBRevengeManager());
            log.info("BountiesExpanded: HVBRevengeManager added");
        } else {
            log.info("BountiesExpanded: Found existing HVBRevengeManager");
        }
    }

    private void addCampaignPlugins() {
        Global.getSector().registerPlugin(new BountiesExpandedCampaignPlugin());

        if (!Global.getSector().hasScript(BountiesExpandedCampaignManager.class)) {
            Global.getSector().addScript(new BountiesExpandedCampaignManager());
            log.info("BountiesExpanded: BountiesExpandedCampaignManager added");
        } else {
            log.info("BountiesExpanded: Found existing BountiesExpandedCampaignManager");
        }
    }

    private void printCompletedBounties() {
        HighValueBountyManager bountyManager = HighValueBountyManager.getInstance();
        log.info("BountiesExpanded: List of completed HVBs:");
        for (String completedBounty : bountyManager.getCompletedBounties()) {
            log.info(completedBounty);
        }
    }

    private void setBlacklists() {
        Set<String> factionBountyBlacklist = Blacklists.getDefaultBlacklist();
        factionBountyBlacklist.add(Factions.PIRATES);
        factionBountyBlacklist.add(Factions.LUDDIC_PATH);
        Blacklists.setSkirmishBountyBlacklist(factionBountyBlacklist);
    }

    private void printDebugInfo() {
        log.info("###### BountiesExpanded - Blacklisted Factions - DEFAULT ######");
        for (String faction : Blacklists.getDefaultBlacklist()) {
            log.info(faction);
        }
        log.info("###### BountiesExpanded - Blacklisted Factions - SKIRMISH ######");
        for (String faction : Blacklists.getSkirmishBountyBlacklist()) {
            log.info(faction);
        }
        log.info("###### BountiesExpanded - Participating Factions ######");
        for (String faction : BountyEventData.getSharedData().getParticipatingFactions()) {
            log.info(faction);
        }
        log.info("###### BountiesExpanded - HighValueBounties ######");
        for (String bounty : HighValueBountyManager.getInstance().getBountiesList()) {
            log.info(bounty);
        }
    }
}
