package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
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
        if (Settings.ASSASSINATION_ACTIVE && new Random().nextFloat() <= Settings.ASSASSINATION_SPAWN_CHANCE) {
            final AssassinationBountyEntity assassinationBountyEntity = EntityProvider.assassinationBountyEntity();
            if (isNull(assassinationBountyEntity))
                return null;
            final CampaignFleetAPI fleet = assassinationBountyEntity.getFleet();
            String fleetTypeName = "Assassination Fleet";
            fleet.setName(fleetTypeName);
            fleet.setTransponderOn(false);
            fleet.getAI().clearAssignments();
            fleet.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, assassinationBountyEntity.getStartingPoint(), 5f, "Resupplying fleet", new Script() {
                public void run() {
                    fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                            Misc.findNearestJumpPoint(assassinationBountyEntity.getStartingPoint()),
                            30f,
                            "Travelling to Jump-Point",
                            new Script() {
                                public void run() {
                                    fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                                            assassinationBountyEntity.getEndingPoint(),
                                            100f,
                                            "Travelling to " + assassinationBountyEntity.getEndingPoint().getStarSystem(),
                                            null);
                                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SMUGGLER, true);
                                }
                            });
                }
            });
            fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

            return new AssassinationBountyIntel(assassinationBountyEntity, assassinationBountyEntity.getFleet(), assassinationBountyEntity.getPerson(), assassinationBountyEntity.getStartingPoint());
        }

        return null;
    }
}
