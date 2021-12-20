package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUpgradeHelper;
import de.schafunschaf.bountiesexpanded.helper.location.LocationUtils;
import de.schafunschaf.bountiesexpanded.helper.ship.HullModUtils;
import de.schafunschaf.bountiesexpanded.helper.ship.ShipUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.extern.log4j.Log4j;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval.RetrievalMissionEntity.RETRIEVAL_SHIP_KEY;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class WarCriminalManager extends BaseEventManager implements BaseBountyManager {
    public static final String FLEET_NAME = "War Criminal Fleet";
    public static final String KEY = "$bountiesExpanded_warCriminalManagerBountyManager";
    public static final String WAR_CRIMINAL_BOUNTY_FLEET_KEY = "$bountiesExpanded_warCriminalBountyFleet";

    public WarCriminalManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static WarCriminalManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (WarCriminalManager) instance;
    }

    @Override
    public String getFleetKey() {
        return WAR_CRIMINAL_BOUNTY_FLEET_KEY;
    }

    @Override
    protected int getMinConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.warCriminalMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
        if (Settings.isDebugActive())
            return 5;

        return Settings.warCriminalMaxBounties;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.warCriminalActive) {
            if (Settings.isDebugActive())
                return createWarCriminalBountyEvent();
            if (new Random().nextFloat() <= Settings.warCriminalSpawnChance) {
                return createWarCriminalBountyEvent();
            }
        }

        return null;
    }

    public WarCriminalIntel createWarCriminalBountyEvent() {
        WarCriminalEntity warCriminalEntity = EntityProvider.warCriminalEntity();

        if (isNull(warCriminalEntity))
            return null;

        final CampaignFleetAPI fleet = warCriminalEntity.getFleet();
        SectorEntityToken spawnLocation = warCriminalEntity.getSpawnLocation();
        PersonAPI person = warCriminalEntity.getTargetedPerson();
        Difficulty difficulty = warCriminalEntity.getDifficulty();

        fleet.setName(FLEET_NAME);
        FleetGenerator.spawnFleet(fleet, spawnLocation);

        WarCriminalIntel warCriminalIntel = new WarCriminalIntel(warCriminalEntity, fleet, person, spawnLocation, warCriminalEntity.getDropOffLocation());

        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, WAR_CRIMINAL_BOUNTY_FLEET_KEY);
        fleetMemory.set(WAR_CRIMINAL_BOUNTY_FLEET_KEY, warCriminalEntity);

        fleet.clearAssignments();
        fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, spawnLocation.getStarSystem().getStar(), warCriminalIntel.getDuration(), new Script() {
            @Override
            public void run() {
                if (fleet.isInCurrentLocation())
                    fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, LocationUtils.getNearestLocation(fleet), 30f);
                else
                    fleet.despawn();
            }
        });
        fleet.setTransponderOn(true);

        FleetMemberAPI flagship = fleet.getFlagship();
        switch (warCriminalEntity.getMissionHandler().getMissionType()) {
            case RETRIEVAL:
                warCriminalEntity.setRetrievalTargetShip(flagship);
                fleetMemory.set(RETRIEVAL_SHIP_KEY, flagship.getId());
                ShipUtils.markMemberForRecovery(flagship);
                break;
            case OBLITERATION:
                fleetMemory.set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                break;
        }

        log.info("BountiesExpanded - Spawning War Criminal Bounty: By "
                + warCriminalEntity.getOfferingFaction().getDisplayName() + " | Against "
                + warCriminalEntity.getTargetedFaction().getDisplayName() + " | At "
                + spawnLocation.getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + warCriminalEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + difficulty.getShortDescription());

        upgradeShips(fleet);

        return warCriminalIntel;
    }

    public void upgradeShips(CampaignFleetAPI bountyFleet) {
        if (isNull(bountyFleet))
            return;

        Random random = new Random(bountyFleet.getId().hashCode() * 1337L);
        WarCriminalEntity warCriminalEntity = (WarCriminalEntity) bountyFleet.getMemoryWithoutUpdate().get(WarCriminalManager.WAR_CRIMINAL_BOUNTY_FLEET_KEY);
        int modValue = warCriminalEntity.getDifficulty().getFlatModifier();
        boolean isRareShip = bountyFleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY);
        int numSMods = isRareShip ? 3 : 2;
        FleetMemberAPI flagship = bountyFleet.getFlagship();
        if (isNull(flagship))
            return;

        if (warCriminalEntity.getMissionHandler().getMissionType().equals(MissionHandler.MissionType.RETRIEVAL))
            HullModUtils.addRandomSMods(flagship, numSMods, random);

        if (flagship.getVariant().getSMods().isEmpty())
            ShipUtils.upgradeShip(flagship, numSMods, random);

        ShipUtils.addMinorUpgrades(flagship, random);

        flagship.updateStats();

        FleetUpgradeHelper.upgradeRandomShips(bountyFleet, modValue, modValue * 0.1f, true, random);
    }
}
