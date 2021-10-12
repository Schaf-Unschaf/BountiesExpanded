package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import org.apache.log4j.Logger;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class AssassinationBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_assassinationBountyManager";
    public static final String ASSASSINATION_BOUNTY_FLEET_KEY = "$bountiesExpanded_assassinationBountyFleet";
    public static final Logger log = Global.getLogger(AssassinationBountyManager.class);

    public AssassinationBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static AssassinationBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (AssassinationBountyManager) test;
    }

    @Override
    protected int getMinConcurrent() {
        return Settings.ASSASSINATION_MIN_BOUNTIES;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.ASSASSINATION_MAX_BOUNTIES;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.ASSASSINATION_ACTIVE) {
            if (Settings.isDebugActive())
                return createAssassinationBountyEvent();
            if (new Random().nextFloat() <= Settings.ASSASSINATION_SPAWN_CHANCE)
                return createAssassinationBountyEvent();
        }

        return null;
    }

    public AssassinationBountyIntel createAssassinationBountyEvent() {
        final AssassinationBountyEntity assassinationBountyEntity = EntityProvider.assassinationBountyEntity();
        if (isNull(assassinationBountyEntity))
            return null;
        final CampaignFleetAPI fleet = assassinationBountyEntity.getFleet();
        fleet.setNoFactionInName(true);
        fleet.setName(NameStringCollection.getSuspiciousName());
        FleetGenerator.spawnFleet(fleet, assassinationBountyEntity.getStartingPoint());
        fleet.setTransponderOn(false);
        fleet.getAI().clearAssignments();
        final MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        fleet.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, assassinationBountyEntity.getStartingPoint(), 5f, "Resupplying fleet", new Script() {
            public void run() {
                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                        Misc.findNearestJumpPoint(assassinationBountyEntity.getStartingPoint()),
                        15f,
                        "Travelling to Jump-Point",
                        new Script() {
                            public void run() {
                                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                                        assassinationBountyEntity.getEndingPoint().getStarSystem().getHyperspaceAnchor(),
                                        45f,
                                        "Travelling to " + assassinationBountyEntity.getEndingPoint().getStarSystem().getName(),
                                        new Script() {
                                            public void run() {
                                                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                                                        assassinationBountyEntity.getEndingPoint(),
                                                        15f,
                                                        "Travelling to " + assassinationBountyEntity.getEndingPoint().getName(),
                                                        new Script() {
                                                            public void run() {
                                                                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                                                                        assassinationBountyEntity.getEndingPoint(),
                                                                        2f,
                                                                        "Preparing to dock at " + assassinationBountyEntity.getEndingPoint().getName(),
                                                                        new Script() {
                                                                            public void run() {
                                                                                fleet.despawn();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                fleetMemory.set(MemFlags.MEMORY_KEY_SMUGGLER, false);
                                            }
                                        });
                                fleetMemory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, false);
                                fleetMemory.set(MemFlags.MEMORY_KEY_SMUGGLER, true);
                                assassinationBountyEntity.reportEnteredHyperspace();
                            }
                        });
            }
        });
        fleetMemory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        fleetMemory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        fleetMemory.set(ASSASSINATION_BOUNTY_FLEET_KEY, assassinationBountyEntity);

        log.info("BountiesExpanded - Spawning Assassination Bounty: "
                + assassinationBountyEntity.getFleet().getName() + " | "
                + assassinationBountyEntity.getStartingPoint().getName() + " -> "
                + assassinationBountyEntity.getEndingPoint().getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + assassinationBountyEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + assassinationBountyEntity.getDifficulty().getShortDescription());

        return new AssassinationBountyIntel(assassinationBountyEntity, assassinationBountyEntity.getFleet(), assassinationBountyEntity.getPerson(), assassinationBountyEntity.getStartingPoint());
    }
}
