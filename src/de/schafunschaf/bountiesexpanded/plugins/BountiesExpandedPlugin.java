package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountylib.campaign.helper.faction.FactionBlacklist;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

public class BountiesExpandedPlugin extends BaseModPlugin {
    public static Logger log = Global.getLogger(BountiesExpandedPlugin.class);
    String SETTINGS_FILE = "BountiesExpanded.json";

    @Override
    public void onApplicationLoad() {
        initBountiesExpanded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        setBlacklists();
        if (!Global.getSector().hasScript(SkirmishBountyManager.class)) {
            Global.getSector().addScript(new SkirmishBountyManager());
            log.info("BountiesExpanded: SkirmishBountyManager added");
        } else {
            log.info("BountiesExpanded: Found existing SkirmishBountyManager");
        }
    }

    private void initBountiesExpanded() {
        try {
            loadSettings();
        } catch (IOException | JSONException exception) {
            log.error("BountiesExpanded - Failed to load Settings! - " + exception.getMessage());
        }
    }

    private void loadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);
        Settings.SHEEP_DEBUG = settings.getBoolean("SHEEP_DEBUG");
        Settings.SKIRMISH_SPAWN_CHANCE = settings.getDouble("SKIRMISH_SPAWN_CHANCE");
        Settings.SKIRMISH_MAX_BOUNTIES = settings.getInt("SKIRMISH_MAX_BOUNTIES");
        Settings.SKIRMISH_MIN_BOUNTIES = settings.getInt("SKIRMISH_MIN_BOUNTIES");
        Settings.SKIRMISH_MAX_DURATION = settings.getInt("SKIRMISH_MAX_DURATION");
        Settings.SKIRMISH_MIN_DURATION = settings.getInt("SKIRMISH_MIN_DURATION");
    }

    private void setBlacklists() {
        Set<String> factionBountyBlacklist = FactionBlacklist.getDefaultBlacklist();
        factionBountyBlacklist.add(Factions.PIRATES);
        factionBountyBlacklist.add(Factions.LUDDIC_PATH);
        Blacklists.setSkirmishBountyBlacklist(factionBountyBlacklist);
    }

}
