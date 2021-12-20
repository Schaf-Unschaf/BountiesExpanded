package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval.RetrievalMissionEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval.RetrievalMissionIntel;
import de.schafunschaf.bountiesexpanded.util.ShipPaymentPair;
import lombok.extern.log4j.Log4j;

import java.util.Random;

@Log4j
public class TriggeredMissionManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_triggeredMissionManager";

    public TriggeredMissionManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static TriggeredMissionManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (TriggeredMissionManager) instance;
    }

    @Override
    protected int getMinConcurrent() {
        return 0;
    }

    @Override
    protected int getMaxConcurrent() {
        return 0;
    }

    @Override
    protected EveryFrameScript createEvent() {
        return null;
    }

    /**
     * Starts a new mission for returning a single recovered ship
     * @param bountyIntel BountyIntel that triggered the mission
     * @param retrievalShip The ship you have to return
     */
    public void createRetrievalMissionEvent(BaseBountyIntel bountyIntel, FleetMemberAPI retrievalShip, int remainingPayment) {
        float baseValue = retrievalShip.getHullSpec().getBaseValue();
        float mult = (float) (100 - new Random(retrievalShip.getId().hashCode()).nextInt(26)) / 100;
        int recoveryValue = (int) (baseValue * mult);
        ShipPaymentPair<FleetMemberAPI, Integer> shipWithPayment = new ShipPaymentPair<>(retrievalShip, recoveryValue);

        RetrievalMissionEntity retrievalMissionEntity = new RetrievalMissionEntity(bountyIntel.getBountyEntity(), shipWithPayment, remainingPayment);
        RetrievalMissionIntel retrievalMissionIntel = new RetrievalMissionIntel(retrievalMissionEntity);

        Global.getSector().addScript(retrievalMissionIntel);
        Global.getSector().getIntelManager().addIntel(retrievalMissionIntel);

        addActive(retrievalMissionIntel); // needs to be added since it's not triggered by createEvent();

        log.info("BountiesExpanded - Spawning new Retrieval Mission");
    }
}
