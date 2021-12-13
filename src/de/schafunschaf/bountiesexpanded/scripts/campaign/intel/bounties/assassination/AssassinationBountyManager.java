package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUpgradeHelper;
import de.schafunschaf.bountiesexpanded.helper.ship.SModUpgradeHelper;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import lombok.extern.log4j.Log4j;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class AssassinationBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_assassinationBountyManager";
    public static final String ASSASSINATION_BOUNTY_FLEET_KEY = "$bountiesExpanded_assassinationBountyFleet";

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
        return Settings.assassinationMinBounties;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.assassinationMaxBounties;
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.assassinationActive) {
            if (Settings.isDebugActive())
                return createAssassinationBountyEvent();
            if (new Random().nextFloat() <= Settings.assassinationSpawnChance)
                return createAssassinationBountyEvent();
        }

        return null;
    }

    public AssassinationBountyIntel createAssassinationBountyEvent() {
        log.info("BountiesExpanded: creating new Assassination Bounty");
        final AssassinationBountyEntity assassinationBountyEntity = EntityProvider.assassinationBountyEntity();
        if (isNull(assassinationBountyEntity)) {
            log.warn("BountiesExpanded: failed to create Assassination Bounty");
            return null;
        }
        final CampaignFleetAPI bountyFleet = assassinationBountyEntity.getFleet();
        bountyFleet.setNoFactionInName(true);
        bountyFleet.setName(NameStringCollection.getSuspiciousName());
        FleetGenerator.spawnFleet(bountyFleet, assassinationBountyEntity.getStartingPoint());
        bountyFleet.setTransponderOn(false);
        bountyFleet.getAI().clearAssignments();
        final MemoryAPI fleetMemory = bountyFleet.getMemoryWithoutUpdate();
        bountyFleet.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, assassinationBountyEntity.getStartingPoint(), 5f, "Resupplying Fleet", new Script() {
            public void run() {
                bountyFleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                        Misc.findNearestJumpPoint(assassinationBountyEntity.getStartingPoint()),
                        15f,
                        "Travelling to Jump-Point",
                        new Script() {
                            public void run() {
                                bountyFleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                                        assassinationBountyEntity.getEndingPoint().getStarSystem().getHyperspaceAnchor(),
                                        45f,
                                        "Travelling to " + assassinationBountyEntity.getEndingPoint().getStarSystem().getName(),
                                        new Script() {
                                            public void run() {
                                                bountyFleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION,
                                                        assassinationBountyEntity.getEndingPoint(),
                                                        15f,
                                                        "Travelling to " + assassinationBountyEntity.getEndingPoint().getName(),
                                                        new Script() {
                                                            public void run() {
                                                                bountyFleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                                                                        assassinationBountyEntity.getEndingPoint(),
                                                                        2f,
                                                                        "Preparing to dock at " + assassinationBountyEntity.getEndingPoint().getName(),
                                                                        new Script() {
                                                                            public void run() {
                                                                                bountyFleet.despawn();
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
        fleetMemory.set(EntityProvider.FLEET_IDENTIFIER_KEY, ASSASSINATION_BOUNTY_FLEET_KEY);
        fleetMemory.set(ASSASSINATION_BOUNTY_FLEET_KEY, assassinationBountyEntity);

        log.info("BountiesExpanded - Spawning Assassination Bounty: "
                + assassinationBountyEntity.getFleet().getName() + " | "
                + assassinationBountyEntity.getStartingPoint().getName() + " -> "
                + assassinationBountyEntity.getEndingPoint().getName());
        log.info("Player-FP at creation: " + Global.getSector().getPlayerFleet().getFleetPoints());
        log.info("Enemy-FP at creation: " + assassinationBountyEntity.getFleet().getFleetPoints());
        log.info("Difficulty: " + assassinationBountyEntity.getDifficulty().getShortDescription());

        upgradeShips(bountyFleet);

        return new AssassinationBountyIntel(assassinationBountyEntity, assassinationBountyEntity.getFleet(), assassinationBountyEntity.getTargetedPerson(), assassinationBountyEntity.getStartingPoint(), assassinationBountyEntity.getEndingPoint());
    }

    public void upgradeShips(CampaignFleetAPI bountyFleet) {
        if (isNull(bountyFleet))
            return;

        Random random = new Random(bountyFleet.getId().hashCode() * 1337L);
        int modValue = ((AssassinationBountyEntity) bountyFleet.getMemoryWithoutUpdate().get(AssassinationBountyManager.ASSASSINATION_BOUNTY_FLEET_KEY)).getDifficulty().getFlatModifier();
        FleetMemberAPI flagship = bountyFleet.getFlagship();
        if (isNull(flagship))
            return;

        if (flagship.getVariant().getSMods().isEmpty()) {
            SModUpgradeHelper.upgradeShip(flagship, 2, random);
            SModUpgradeHelper.addMinorUpgrades(flagship, random);
        }

        FleetUpgradeHelper.upgradeRandomShips(bountyFleet, modValue, modValue * 0.1f, true, random);
    }
}
