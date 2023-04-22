package de.schafunschaf.bountiesexpanded.import

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Factions
import de.schafunschaf.bountiesexpanded.data.ships.RARE_FLAGSHIPS
import de.schafunschaf.bountiesexpanded.data.ships.RareFlagshipData
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

fun loadNameStringFiles(fileName: String?) {
    val log = Global.getLogger(Any::class.java)
    try {
        val fleetNames = Global.getSettings().loadJSON(fileName)
        SUSPICIOUS_NAMES.addAll(parseJSONArray(fleetNames.getJSONArray("suspiciousNames")))
        FLEET_ACTION_TEXTS.addAll(parseJSONArray(fleetNames.getJSONArray("fleetActionTexts")))
        PIRATE_JOBS.addAll(parseJSONArray(fleetNames.getJSONArray("pirateJobs")))
        PIRATE_PERSONALITIES.addAll(parseJSONArray(fleetNames.getJSONArray("piratePersonalities")))
        PIRATE_TITLES.addAll(parseJSONArray(fleetNames.getJSONArray("pirateTitles")))
        KILL_WORDS.addAll(parseJSONArray(fleetNames.getJSONArray("killWords")))
        CRIME_REASONS.addAll(parseJSONArray(fleetNames.getJSONArray("crimeReasons")))
        CRIME_TYPES.addAll(parseJSONArray(fleetNames.getJSONArray("crimeTypes")))
        CRIME_VICTIMS.addAll(parseJSONArray(fleetNames.getJSONArray("crimeVictims")))
        PIRATE_FLEET_NAMES.addAll(parseJSONArray(fleetNames.getJSONArray("pirateFleetNames")))
    } catch (exception: IOException) {
        log.error("BountiesExpanded - Failed to load FleetNames! - " + exception.message)
    } catch (exception: JSONException) {
        log.error("BountiesExpanded - Failed to load FleetNames! - " + exception.message)
    }
}

fun loadRareFlagshipData(fileName: String?) {
    val log = Global.getLogger(Any::class.java)
    val rareFlagshipDataMap: MutableMap<String, RareFlagshipData> = HashMap()
    try {
        val uniqueBountyDataJSON =
            Global.getSettings().getMergedSpreadsheetDataForMod("bounty", fileName, "BountiesExpanded")
        for (i in 0 until uniqueBountyDataJSON.length()) {
            val row = uniqueBountyDataJSON.getJSONObject(i)
            if (row.has("bounty") && row.getString("bounty") != null && row.getString("bounty").isNotEmpty()) {
                val flagshipID = row.getString("bounty")
                log.info("loading rare flagship $flagshipID")
                val factionStrings = row.optString("factions")
                val factionList: MutableSet<String> = HashSet()
                if (factionStrings != null) {
                    val faction = factionStrings.split("\\s*(,\\s*)+".toRegex()).toTypedArray()
                    for (x in faction.indices) faction[x] = faction[x].replace("persean_league", Factions.PERSEAN)
                    factionList.addAll(listOf(*faction))
                }
                val flagshipData = RareFlagshipData(
                    flagshipID,
                    row.getString("variant"),
                    factionList,
                    row.getDouble("weight").toFloat()
                )
                log.info("loaded rare flagship $flagshipID")
                rareFlagshipDataMap[flagshipID] = flagshipData
            } else {
                log.info("hit empty line, rare flagship loading ended")
            }
        }
    } catch (exception: IOException) {
        log.error("BountiesExpanded - Failed to load RareFlagshipData! - " + exception.message)
    } catch (exception: JSONException) {
        log.error("BountiesExpanded - Failed to load RareFlagshipData! - " + exception.message)
    }

    RARE_FLAGSHIPS.putAll(rareFlagshipDataMap)
}

@Throws(JSONException::class)
fun parseJSONArray(jsonArray: JSONArray): Collection<String> {
    val parsedStrings: MutableCollection<String> = ArrayList()
    val arraySize = jsonArray.length()
    for (i in 0 until arraySize) parsedStrings.add(jsonArray[i].toString())
    return parsedStrings
}