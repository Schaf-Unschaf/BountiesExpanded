package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import data.scripts.VayraModPlugin;
import data.scripts.campaign.intel.VayraUniqueBountyIntel;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.BountiesExpandedCampaignPlugin;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ParsingTools.parseJSONArray;


public class BountiesExpandedPlugin extends BaseModPlugin {
    public static final String DEFAULT_BLACKLIST_FILE = "data/config/bountiesExpanded/default_blacklist.json";
    public static final String SETTINGS_FILE = "bounties_expanded_settings.ini";
    public static final String NAME_STRINGS_FILE = "data/config/bountiesExpanded/name_strings.json";
    public static final String VAYRA_UNIQUE_BOUNTIES_FILE = "data/config/vayraBounties/unique_bounty_data.csv";
    public static final Logger log = Global.getLogger(BountiesExpandedPlugin.class);

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

            for (EveryFrameScript activeBounty : vayraUniqueBountyManager.getActive())
                ((VayraUniqueBountyIntel) activeBounty).endImmediately();

            Global.getSector().removeScript(vayraUniqueBountyManager);
        }
    }

    private void initManagerAndPlugins() {
        if (Settings.SKIRMISH_ACTIVE) addSkirmishManager();
        if (Settings.ASSASSINATION_ACTIVE) addAssassinationManager();
        if (Settings.HIGH_VALUE_BOUNTY_ACTIVE) addHighValueBountyManager();
        addCampaignPlugins();
    }

    private void initBountiesExpanded() {
        loadSettings();
        loadBlacklists();
        loadNameStringFiles();

        loadHighValueBountyData();
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

    private void addCampaignPlugins() {
        Global.getSector().registerPlugin(new BountiesExpandedCampaignPlugin());

        if (!Global.getSector().hasScript(BountiesExpandedCampaignManager.class)) {
            Global.getSector().addScript(new BountiesExpandedCampaignManager());
            log.info("BountiesExpanded: BountiesExpandedCampaignPlugin added");
        } else {
            log.info("BountiesExpanded: Found existing BountiesExpandedCampaignPlugin");
        }
    }

    private void printCompletedBounties() {
        HighValueBountyManager bountyManager = HighValueBountyManager.getInstance();
        log.info("BountiesExpanded: List of completed HVBs:");
        for (String completedBounty : bountyManager.getCompletedBounties()) {
            log.info(completedBounty);
        }
    }


    private void loadSettings() {
        try {
            JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);
            Settings.SHEEP_DEBUG = settings.getBoolean("SHEEP_DEBUG");

            Settings.SKIRMISH_ACTIVE = settings.getBoolean("SKIRMISH_ACTIVE");
            Settings.SKIRMISH_SPAWN_CHANCE = settings.getDouble("SKIRMISH_SPAWN_CHANCE");
            Settings.SKIRMISH_MAX_BOUNTIES = settings.getInt("SKIRMISH_MAX_BOUNTIES");
            Settings.SKIRMISH_MIN_BOUNTIES = settings.getInt("SKIRMISH_MIN_BOUNTIES");
            Settings.SKIRMISH_MAX_DURATION = settings.getInt("SKIRMISH_MAX_DURATION");
            Settings.SKIRMISH_MIN_DURATION = settings.getInt("SKIRMISH_MIN_DURATION");

            Settings.ASSASSINATION_ACTIVE = settings.getBoolean("ASSASSINATION_ACTIVE");
            Settings.ASSASSINATION_SPAWN_CHANCE = settings.getDouble("ASSASSINATION_SPAWN_CHANCE");
            Settings.ASSASSINATION_MAX_BOUNTIES = settings.getInt("ASSASSINATION_MAX_BOUNTIES");
            Settings.ASSASSINATION_MIN_BOUNTIES = settings.getInt("ASSASSINATION_MIN_BOUNTIES");
            Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE = settings.getDouble("ASSASSINATION_MIN_TRAVEL_DISTANCE");
            Settings.ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER = settings.getDouble("ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER");

            Settings.HIGH_VALUE_BOUNTY_ACTIVE = settings.getBoolean("HIGH_VALUE_BOUNTY_ACTIVE");
            Settings.HIGH_VALUE_BOUNTY_MAX_BOUNTIES = settings.getInt("HIGH_VALUE_BOUNTY_SPAWN_CHANCE");
            Settings.HIGH_VALUE_BOUNTY_SPAWN_CHANCE = settings.getDouble("HIGH_VALUE_BOUNTY_SPAWN_CHANCE");
            Settings.HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS = settings.getDouble("HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS");
            Settings.HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS = settings.getDouble("HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS");
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load custom Settings! - " + exception.getMessage());
        }
    }

    private void loadBlacklists() {
        try {
            JSONObject blacklist = Global.getSettings().loadJSON(DEFAULT_BLACKLIST_FILE);
            Blacklists.addFactions(parseJSONArray(blacklist.getJSONArray("blacklistedFactions")));
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load Blacklists! - " + exception.getMessage());
        }
    }

    private void loadNameStringFiles() {
        try {
            JSONObject fleetNames = Global.getSettings().loadJSON(NAME_STRINGS_FILE);
            NameStringCollection.setSuspiciousNames(parseJSONArray(fleetNames.getJSONArray("suspiciousNames")));
            NameStringCollection.setFleetActionTexts(parseJSONArray(fleetNames.getJSONArray("fleetActionTexts")));
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load FleetNames! - " + exception.getMessage());
        }
    }

    public static void loadHighValueBountyData() {
        try {
            JSONArray uniqueBountyDataJSON = Global.getSettings().getMergedSpreadsheetDataForMod("bounty_id", VAYRA_UNIQUE_BOUNTIES_FILE, "BountiesExpanded");

            for (int i = 0; i < uniqueBountyDataJSON.length(); ++i) {
                JSONObject row = uniqueBountyDataJSON.getJSONObject(i);
                if (row.has("bounty_id") && row.getString("bounty_id") != null && !row.getString("bounty_id").isEmpty()) {
                    String bountyId = row.getString("bounty_id");
                    log.info("loading unique bounty " + bountyId);
                    String fleetListString = row.optString("fleetVariantIds");
                    List<String> fleetList = null;
                    if (fleetListString != null) {
                        fleetList = new ArrayList<>(Arrays.asList(fleetListString.split("\\s*(,\\s*)+")));
                        if (fleetList.isEmpty() || (fleetList.get(0)).isEmpty()) {
                            fleetList = null;
                        }
                    }

                    String itemListString = row.optString("specialItemRewards");
                    List<String> itemList = null;
                    if (itemListString != null) {
                        itemList = new ArrayList<>(Arrays.asList(itemListString.split("\\s*(,\\s*)+")));
                        if (itemList.isEmpty() || (itemList.get(0)).isEmpty()) {
                            itemList = null;
                        }
                    }

                    String prerequisiteBountiesString = row.optString("neverSpawnUnlessBountiesCompleted");
                    List<String> prerequisiteBountiesList = null;
                    if (prerequisiteBountiesString != null) {
                        prerequisiteBountiesList = new ArrayList<>(Arrays.asList(prerequisiteBountiesString.split("\\s*(,\\s*)+")));
                        if (prerequisiteBountiesList.isEmpty() || (prerequisiteBountiesList.get(0)).isEmpty()) {
                            prerequisiteBountiesList = null;
                        }
                    }

                    HighValueBountyData bountyData = new HighValueBountyData(bountyId,
                            row.getInt("level"),
                            row.getString("rank"),
                            row.getString("firstName"),
                            row.getString("lastName"),
                            row.optString("captainPersonality", "aggressive"),
                            row.getString("fleetName"),
                            row.getString("flagshipName"),
                            row.getString("gender"),
                            row.getString("faction"),
                            row.getString("portrait"),
                            row.getString("greetingText"),
                            row.getBoolean("suppressIntel"),
                            row.getString("postedByFaction"),
                            row.getInt("creditReward"),
                            (float) row.getInt("repReward") / 100.0F,
                            row.getString("intelText"),
                            row.getString("flagshipVariantId"),
                            fleetList,
                            row.getInt("minimumFleetFP"),
                            (float) row.getDouble("playerFPScalingFactor"),
                            (float) row.optDouble("chanceToAutoRecover", 1.0D),
                            itemList,
                            prerequisiteBountiesList,
                            row.getBoolean("neverSpawnWhenFactionHostile"),
                            row.getBoolean("neverSpawnWhenFactionNonHostile"),
                            row.getInt("neverSpawnBeforeCycle"),
                            row.getInt("neverSpawnBeforeLevel"),
                            row.getInt("neverSpawnBeforeFleetPoints")
                    );
                    HighValueBountyManager.highValueBountyData.put(bountyId, bountyData);
                    log.info("loaded unique bounty id " + bountyId);
                } else {
                    log.info("hit empty line, unique bounty loading ended");
                }
            }
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load HighValueBountyData! - " + exception.getMessage());
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

    public static void prepareForUpdate() {
        Settings.PREPARE_UPDATE = true;
        HighValueBountyManager.getInstance().saveCompletedBountyData();

        uninstallManager(SkirmishBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(SkirmishBountyManager.KEY);
        uninstallManager(AssassinationBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(AssassinationBountyManager.KEY);
        uninstallManager(HighValueBountyManager.getInstance());
        Global.getSector().getMemoryWithoutUpdate().unset(HighValueBountyManager.KEY);

        uninstallPlugins();
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
        Global.getSector().unregisterPlugin("BountiesExpandedCampaignPlugin");
    }
}
