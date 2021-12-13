package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.ship.SModUpgradeHelper;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import lombok.extern.log4j.Log4j;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class HVBRevengeManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_hvbRevengeManager";
    public static final String HVB_REVENGE_FLEET_KEY = "$bountiesExpanded_hvbRevengeFleet";

    public HVBRevengeManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static HVBRevengeManager getInstance() {
        return (HVBRevengeManager) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }

    @Override
    protected int getMinConcurrent() {
        return 0;
    }

    @Override
    protected int getMaxConcurrent() {
        return 1;
    }

    @Override
    protected EveryFrameScript createEvent() {
        return null;
    }

    public EveryFrameScript forceSpawn() {
        log.info("attempting to force-spawn HVB Revenge Fleet");
        HVBRevengeEntity HVBRevengeEntity = EntityProvider.hvbRevengeEntity();
        if (isNull(HVBRevengeEntity)) {
            log.info("failed to force-spawn HVB Revenge Fleet");
            return null;
        }

        HVBRevengeIntel HVBRevengeIntel = createHVBRevengeEvent();

        Global.getSector().addScript(HVBRevengeIntel);

        return HVBRevengeIntel;
    }

    private HVBRevengeIntel createHVBRevengeEvent() {
            HVBRevengeEntity hvbRevengeEntity = EntityProvider.hvbRevengeEntity();

        if (isNull(hvbRevengeEntity)) {
            log.warn("BountiesExpanded: Failed to create HighValueBountyEntity");
            return null;
        }

        CampaignFleetAPI fleet = hvbRevengeEntity.getFleet();
        SectorEntityToken hideout = hvbRevengeEntity.getStartingPoint();
        String randomActionText = NameStringCollection.getFleetActionText();

        fleet.setNoFactionInName(true);
        fleet.setName("Sheep loves vanilla ice cream");

        for (FleetMemberAPI fleetMemberAPI : fleet.getFleetData().getMembersListCopy()) {
            if (fleetMemberAPI.isFlagship())
                continue;

            PersonAPI shipCaptain = OfficerManagerEvent.createOfficer(hvbRevengeEntity.getTargetedFaction(), 14, OfficerManagerEvent.SkillPickPreference.ANY, true, fleet, true, true, 10, null);
            shipCaptain.setName(new FullName("Omega", "Sheep", FullName.Gender.MALE));
            shipCaptain.setPortraitSprite(Global.getSector().getFaction(Factions.OMEGA).getPortraits(FullName.Gender.MALE).pick());
            shipCaptain.setPersonality(Personalities.AGGRESSIVE);
            shipCaptain.setRankId(Ranks.CLONE);
            fleetMemberAPI.setCaptain(shipCaptain);
        }

        FleetGenerator.spawnFleet(fleet, hideout);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(HVB_REVENGE_FLEET_KEY, hvbRevengeEntity);
        memory.set(MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER, true);
        memory.set(MemFlags.MEMORY_KEY_PIRATE, true);
        memory.set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        memory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);

        fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideout, 100000f, randomActionText, null);
        upgradeShips(fleet);

        HVBRevengeIntel bountyIntel = new HVBRevengeIntel(hvbRevengeEntity, fleet, hvbRevengeEntity.getTargetedPerson(), hideout, null);

        log.info("BountiesExpanded: Creating HighValueBountyRevengeEvent");
        addActive(bountyIntel);

        return bountyIntel;
    }

    public void upgradeShips(CampaignFleetAPI bountyFleet) {
        if (isNull(bountyFleet))
            return;

        Random random = new Random(bountyFleet.getId().hashCode() * 1337L);

        for (FleetMemberAPI fleetMember : bountyFleet.getFleetData().getMembersListCopy()) {
            SModUpgradeHelper.upgradeShip(fleetMember, 5, random);
            SModUpgradeHelper.addMinorUpgrades(fleetMember, random);
        }
    }
}
