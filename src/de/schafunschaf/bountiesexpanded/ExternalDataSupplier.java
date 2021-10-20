package de.schafunschaf.bountiesexpanded;

import com.fs.starfarer.api.Global;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.retrieval.RareFlagshipData;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static de.schafunschaf.bountiesexpanded.util.ParsingTools.parseJSONArray;

public class ExternalDataSupplier {
    public static final Logger log = Global.getLogger(ExternalDataSupplier.class);

    public static void loadSettings(String fileName) {
        try {
            JSONObject settings = Global.getSettings().loadJSON(fileName);
            Settings.SHEEP_DEBUG = settings.getBoolean("SHEEP_DEBUG");

            Settings.SKIRMISH_ACTIVE = settings.getBoolean("SKIRMISH_ACTIVE");
            Settings.SKIRMISH_SPAWN_CHANCE = settings.getDouble("SKIRMISH_SPAWN_CHANCE");
            Settings.SKIRMISH_MAX_BOUNTIES = settings.getInt("SKIRMISH_MAX_BOUNTIES");
            Settings.SKIRMISH_MIN_BOUNTIES = settings.getInt("SKIRMISH_MIN_BOUNTIES");
            Settings.SKIRMISH_MAX_DURATION = settings.getInt("SKIRMISH_MAX_DURATION");
            Settings.SKIRMISH_MIN_DURATION = settings.getInt("SKIRMISH_MIN_DURATION");

            Settings.ASSASSINATION_ACTIVE = settings.getBoolean("ASSASSINATION_ACTIVE");
            Settings.ASSASSINATION_SPAWN_CHANCE = settings.getDouble("ASSASSINATION_SPAWN_CHANCE");
            Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE = settings.getDouble("ASSASSINATION_MIN_TRAVEL_DISTANCE");
            Settings.ASSASSINATION_MAX_BOUNTIES = settings.getInt("ASSASSINATION_MAX_BOUNTIES");
            Settings.ASSASSINATION_MIN_BOUNTIES = settings.getInt("ASSASSINATION_MIN_BOUNTIES");

            Settings.HIGH_VALUE_BOUNTY_ACTIVE = settings.getBoolean("HIGH_VALUE_BOUNTY_ACTIVE");
            Settings.HIGH_VALUE_BOUNTY_MAX_BOUNTIES = settings.getInt("HIGH_VALUE_BOUNTY_MAX_BOUNTIES");
            Settings.HIGH_VALUE_BOUNTY_SPAWN_CHANCE = settings.getDouble("HIGH_VALUE_BOUNTY_SPAWN_CHANCE");
            Settings.HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS = settings.getDouble("HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS");
            Settings.HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS = settings.getDouble("HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS");
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load custom Settings! - " + exception.getMessage());
        }
    }

    public static void loadBlacklists(String fileName) {
        try {
            JSONObject blacklist = Global.getSettings().loadJSON(fileName);
            Blacklists.addFactions(parseJSONArray(blacklist.getJSONArray("blacklistedFactions")));
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load Blacklists! - " + exception.getMessage());
        }
    }

    public static void loadNameStringFiles(String fileName) {
        try {
            JSONObject fleetNames = Global.getSettings().loadJSON(fileName);
            NameStringCollection.setSuspiciousNames(parseJSONArray(fleetNames.getJSONArray("suspiciousNames")));
            NameStringCollection.setFleetActionTexts(parseJSONArray(fleetNames.getJSONArray("fleetActionTexts")));
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load FleetNames! - " + exception.getMessage());
        }
    }

    public static void loadHighValueBountyData(String fileName) {
        try {
            JSONArray uniqueBountyDataJSON = Global.getSettings().getMergedSpreadsheetDataForMod("bounty_id", fileName, "BountiesExpanded");

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

    public static Map<String, RareFlagshipData> loadRareFlagshipData(String fileName) {
        Map<String, RareFlagshipData> rareFlagshipDataMap = new HashMap<>();

        try {
            JSONArray uniqueBountyDataJSON = Global.getSettings().getMergedSpreadsheetDataForMod("bounty", fileName, "BountiesExpanded");

            for (int i = 0; i < uniqueBountyDataJSON.length(); ++i) {
                JSONObject row = uniqueBountyDataJSON.getJSONObject(i);
                if (row.has("bounty") && row.getString("bounty") != null && !row.getString("bounty").isEmpty()) {
                    String flagshipID = row.getString("bounty");
                    log.info("loading rare flagship " + flagshipID);

                    RareFlagshipData flagshipData = new RareFlagshipData(flagshipID,
                            row.getString("variant"),
                            row.getString("factions"),
                            (float) row.getDouble("weight"),
                            row.getString("source")
                    );
                    log.info("loaded rare flagship " + flagshipID);
                    rareFlagshipDataMap.put(flagshipID, flagshipData);
                } else {
                    log.info("hit empty line, rare flagship loading ended");
                }
            }
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load RareFlagshipData! - " + exception.getMessage());
        }

        return rareFlagshipDataMap;
    }
}
