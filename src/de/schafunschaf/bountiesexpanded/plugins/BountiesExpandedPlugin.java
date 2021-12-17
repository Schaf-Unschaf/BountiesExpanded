package de.schafunschaf.bountiesexpanded.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.ModInitHelper;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.helper.ship.HullModUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter.DeserterBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter.DeserterBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate.PirateBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate.PirateBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import lombok.extern.log4j.Log4j;

import java.util.Set;

import static de.schafunschaf.bountiesexpanded.ExternalDataSupplier.*;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class BountiesExpandedPlugin extends BaseModPlugin {
    public static final String DEFAULT_BLACKLIST_FILE = "data/config/bountiesExpanded/default_blacklist.json";
    public static final String SETTINGS_FILE = "bounties_expanded_settings.ini";
    public static final String NAME_STRINGS_FILE = "data/config/bountiesExpanded/name_strings.json";
    public static final String RARE_FLAGSHIPS_FILE = "data/config/vayraBounties/rare_flagships.csv";

    @Override
    public void onApplicationLoad() {
        initBountiesExpanded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Settings.prepareUpdate) {
            ModInitHelper.prepareForUpdate();
            return;
        }

        setBlacklists();

        ModInitHelper.initManagerAndPlugins();

        reloadHullMods();

        if (Settings.sheepDebug) printDebugInfo();

        if (newGame)
            spawnInitialBounties();
    }

    private void spawnInitialBounties() {
        if (Settings.pirateBountyActive) {
            PirateBountyManager pirateBountyManager = PirateBountyManager.getInstance();
            if (isNull(pirateBountyManager))
                return;

            pirateBountyManager.spawnInitialBounties();
        }
    }

    private void reloadHullMods() {
        reloadSkirmishMods();
        reloadAssassinationMods();
        reloadWarCriminalMods();
        reloadPirateBountyMods();
        reloadDeserterBountyMods();
    }

    private void reloadSkirmishMods() {
        SkirmishBountyManager bountyManager = SkirmishBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        for (EveryFrameScript everyFrameScript : bountyManager.getActive()) {
            SkirmishBountyIntel bountyIntel = (SkirmishBountyIntel) everyFrameScript;
            CampaignFleetAPI fleet = bountyIntel.getFleet();
            float fleetQuality = ((BountyEntity) fleet.getMemoryWithoutUpdate().get(SkirmishBountyManager.SKIRMISH_BOUNTY_FLEET_KEY)).getFleetQuality();
            HullModUtils.addDMods(fleet, fleetQuality);
            bountyManager.upgradeShips(fleet);
        }
    }

    private void reloadAssassinationMods() {
        AssassinationBountyManager bountyManager = AssassinationBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        for (EveryFrameScript everyFrameScript : bountyManager.getActive()) {
            AssassinationBountyIntel bountyIntel = (AssassinationBountyIntel) everyFrameScript;
            CampaignFleetAPI fleet = bountyIntel.getFleet();
            float fleetQuality = ((BountyEntity) fleet.getMemoryWithoutUpdate().get(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY)).getFleetQuality();
            HullModUtils.addDMods(fleet, fleetQuality);
            bountyManager.upgradeShips(fleet);
        }
    }

    private void reloadWarCriminalMods() {
        WarCriminalManager bountyManager = WarCriminalManager.getInstance();
        if (isNull(bountyManager))
            return;

        for (EveryFrameScript everyFrameScript : bountyManager.getActive()) {
            WarCriminalIntel bountyIntel = (WarCriminalIntel) everyFrameScript;
            CampaignFleetAPI fleet = bountyIntel.getFleet();
            float fleetQuality = ((BountyEntity) fleet.getMemoryWithoutUpdate().get(WarCriminalManager.WAR_CRIMINAL_BOUNTY_FLEET_KEY)).getFleetQuality();
            HullModUtils.addDMods(fleet, fleetQuality);
            bountyManager.upgradeShips(fleet);
        }
    }

    private void reloadPirateBountyMods() {
        PirateBountyManager bountyManager = PirateBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        for (EveryFrameScript everyFrameScript : bountyManager.getActive()) {
            PirateBountyIntel bountyIntel = (PirateBountyIntel) everyFrameScript;
            CampaignFleetAPI fleet = bountyIntel.getFleet();
            float fleetQuality = ((BountyEntity) fleet.getMemoryWithoutUpdate().get(PirateBountyManager.PIRATE_BOUNTY_FLEET_KEY)).getFleetQuality();
            HullModUtils.addDMods(fleet, fleetQuality);
        }
    }

    private void reloadDeserterBountyMods() {
        DeserterBountyManager bountyManager = DeserterBountyManager.getInstance();
        if (isNull(bountyManager))
            return;

        for (EveryFrameScript everyFrameScript : bountyManager.getActive()) {
            DeserterBountyIntel bountyIntel = (DeserterBountyIntel) everyFrameScript;
            CampaignFleetAPI fleet = bountyIntel.getFleet();
            float fleetQuality = ((BountyEntity) fleet.getMemoryWithoutUpdate().get(DeserterBountyManager.DESERTER_BOUNTY_FLEET_KEY)).getFleetQuality();
            HullModUtils.addDMods(fleet, fleetQuality);
        }
    }

    private void initBountiesExpanded() {
        loadSettings(SETTINGS_FILE);
        loadBlacklists(DEFAULT_BLACKLIST_FILE);
        loadNameStringFiles(NAME_STRINGS_FILE);
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
}
