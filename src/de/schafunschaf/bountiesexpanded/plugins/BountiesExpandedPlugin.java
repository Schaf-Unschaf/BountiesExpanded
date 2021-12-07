package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.scripts.VayraModPlugin;
import data.scripts.campaign.intel.VayraUniqueBountyIntel;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.ModUpdateHelper;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUtils;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.ExternalDataSupplier.*;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class BountiesExpandedPlugin extends BaseModPlugin {
    public static final String DEFAULT_BLACKLIST_FILE = "data/config/bountiesExpanded/default_blacklist.json";
    public static final String SETTINGS_FILE = "bounties_expanded_settings.ini";
    public static final String NAME_STRINGS_FILE = "data/config/bountiesExpanded/name_strings.json";
    public static final String VAYRA_UNIQUE_BOUNTIES_FILE = "data/config/vayraBounties/unique_bounty_data.csv";
    public static final String RARE_FLAGSHIPS_FILE = "data/config/vayraBounties/rare_flagships.csv";

    @Override
    public void onApplicationLoad() {
        initBountiesExpanded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Settings.prepareUpdate) {
            ModUpdateHelper.prepareForUpdate();
            return;
        }

        setBlacklists();

        ModUpdateHelper.initManagerAndPlugins();

        reloadSMods();

        if (Settings.highValueBountyActive && Global.getSettings().getModManager().isModEnabled("vayrasector"))
            removeVayraUniqueBounties();

        if (Settings.sheepDebug) printDebugInfo();
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

    private void reloadSMods() {
        reloadSkirmishSMods();
        reloadHVBSMods();
        reloadAssassinationSMods();
        reloadHVBRevengeSMods();
        reloadWarCriminalSMods();
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

    private void reloadWarCriminalSMods() {
        WarCriminalManager bountyManager = WarCriminalManager.getInstance();
        if (isNull(bountyManager))
            return;

        Set<CampaignFleetAPI> warCriminalFleets = FleetUtils.findFleetWithMemKey(WarCriminalManager.WAR_CRIMINAL_BOUNTY_FLEET_KEY);
        for (CampaignFleetAPI warCriminalFleet : warCriminalFleets) {
            bountyManager.upgradeShips(warCriminalFleet);
        }
    }

    private void initBountiesExpanded() {
        loadSettings(SETTINGS_FILE);
        loadBlacklists(DEFAULT_BLACKLIST_FILE);
        loadNameStringFiles(NAME_STRINGS_FILE);

        loadHighValueBountyData(VAYRA_UNIQUE_BOUNTIES_FILE);
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
