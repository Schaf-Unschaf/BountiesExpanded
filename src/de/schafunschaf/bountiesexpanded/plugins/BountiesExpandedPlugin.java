package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import static de.schafunschaf.bountylib.campaign.helper.util.ParsingTools.parseJSONArray;


public class BountiesExpandedPlugin extends BaseModPlugin {
    public static final String DEFAULT_BLACKLIST_FILE = "data/config/bountiesExpanded/defaultBlacklist.json";
    public static final String SETTINGS_FILE = "BountiesExpandedSettings.json";
    public static Logger log = Global.getLogger(BountiesExpandedPlugin.class);

    @Override
    public void onApplicationLoad() {
        initBountiesExpanded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Settings.UNINSTALL) {
            uninstallMod();
            return;
        }

        setBlacklists();

        initializeManager();

        if (Settings.SHEEP_DEBUG) printDebugInfo();
    }

    private void initializeManager() {
        addSkirmishManager();
        addAssassinationManager();
    }

    private void initBountiesExpanded() {
        try {
            loadSettings();
            loadBlacklists();
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load Settings! - " + exception.getMessage());
        }
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

    private void loadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);
        Settings.SHEEP_DEBUG = settings.getBoolean("SHEEP_DEBUG");
        Settings.UNINSTALL = settings.getBoolean("UNINSTALL");

        Settings.SKIRMISH_ACTIVE = settings.getBoolean("SKIRMISH_ACTIVE");
        Settings.SKIRMISH_SPAWN_CHANCE = settings.getDouble("SKIRMISH_SPAWN_CHANCE");
        Settings.SKIRMISH_MAX_BOUNTIES = settings.getInt("SKIRMISH_MAX_BOUNTIES");
        Settings.SKIRMISH_MIN_BOUNTIES = settings.getInt("SKIRMISH_MIN_BOUNTIES");
        Settings.SKIRMISH_MAX_DURATION = settings.getInt("SKIRMISH_MAX_DURATION");
        Settings.SKIRMISH_MIN_DURATION = settings.getInt("SKIRMISH_MIN_DURATION");

//        Settings.ASSASSINATION_ACTIVE = settings.getBoolean("ASSASSINATION_ACTIVE");
        Settings.ASSASSINATION_SPAWN_CHANCE = settings.getDouble("ASSASSINATION_SPAWN_CHANCE");
        Settings.ASSASSINATION_MAX_BOUNTIES = settings.getInt("ASSASSINATION_MAX_BOUNTIES");
        Settings.ASSASSINATION_MIN_BOUNTIES = settings.getInt("ASSASSINATION_MIN_BOUNTIES");
        Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE = settings.getDouble("ASSASSINATION_MIN_TRAVEL_DISTANCE");
        Settings.ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER = settings.getDouble("ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER");
    }

    private void loadBlacklists() throws IOException, JSONException {
        JSONObject blacklist = Global.getSettings().loadJSON(DEFAULT_BLACKLIST_FILE);
        Blacklists.addFactions(parseJSONArray(blacklist.getJSONArray("blacklistedFactions")));
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
    }

    private void uninstallMod() {
        uninstallManager(SkirmishBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        uninstallManager(AssassinationBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
    }

    private void uninstallManager(BaseEventManager bountyManager) {
        for (EveryFrameScript script : bountyManager.getActive()) {
            ((BaseBountyIntel) script).endImmediately();
            Global.getSector().removeScript(script);
        }
        Global.getSector().removeScript(bountyManager);
    }
}
