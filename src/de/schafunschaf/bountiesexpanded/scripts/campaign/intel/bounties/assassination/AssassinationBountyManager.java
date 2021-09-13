package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.FleetNameCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import org.apache.log4j.Logger;

import java.util.Random;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public class AssassinationBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_assassinationBountyManager";
    public static Logger log = Global.getLogger(AssassinationBountyManager.class);

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
    protected float getIntervalRateMult() {
        return super.getIntervalRateMult();
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.ASSASSINATION_ACTIVE) {
            if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.SHEEP_DEBUG)
                return createAssassinationBountyEvent();
            if (new Random().nextFloat() <= Settings.ASSASSINATION_SPAWN_CHANCE)
                return createAssassinationBountyEvent();
        }

        return null;
    }

    private AssassinationBountyIntel createAssassinationBountyEvent() {
        final AssassinationBountyEntity assassinationBountyEntity = EntityProvider.assassinationBountyEntity();
        if (isNull(assassinationBountyEntity))
            return null;
        final CampaignFleetAPI fleet = assassinationBountyEntity.getFleet();
        fleet.setNoFactionInName(true);
        fleet.setName(FleetNameCollection.getRandomName());
        fleet.setTransponderOn(false);
        fleet.getAI().clearAssignments();
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
                                                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SMUGGLER, false);
                                            }
                                        });
                                fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, false);
                                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SMUGGLER, true);
                            }
                        });
            }
        });
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

        log.info("BountiesExpanded - Spawning Assassination Bounty: "
                + assassinationBountyEntity.getFleet().getName() + " | "
                + assassinationBountyEntity.getStartingPoint().getName() + " -> "
                + assassinationBountyEntity.getEndingPoint().getName());

        return new AssassinationBountyIntel(assassinationBountyEntity, assassinationBountyEntity.getFleet(), assassinationBountyEntity.getPerson(), assassinationBountyEntity.getStartingPoint());
    }
}
