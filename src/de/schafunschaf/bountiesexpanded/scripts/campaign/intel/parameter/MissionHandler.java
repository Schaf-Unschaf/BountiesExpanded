package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.text.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.TriggeredMissionManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.awt.*;
import java.util.Collection;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval.RetrievalMissionEntity.RETRIEVAL_SHIP_KEY;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j
public class MissionHandler {
    @Getter
    @AllArgsConstructor
    public enum MissionType {
        ASSASSINATION(
                "Assassination", "assassination", "eliminate the fleet commander, %s",
                true, false),
        DESTRUCTION(
                "Destruction", "destruction", "destroy the flagship of %s",
                false, false),
        OBLITERATION(
                "Obliteration", "obliteration", "completely destroy the fleet of %s",
                true, false),
        //        INTIMIDATION(
//                "Intimidation", "intimidation", "force %s's fleet into retreat",
//                false, true),
        RETRIEVAL(
                "Retrieval", "retrieval", "retrieve and bring the ship back to %s",
                true, false);

        private final String missionTypeUCFirst;
        private final String missionType;
        private final String objectiveText;
        private final boolean flagshipRecoverable;
        private final boolean forceRetreat;

        public static MissionType getRandomMissionType() {
            MissionType[] types = MissionType.values();
            return types[new Random().nextInt(types.length)];
        }
    }

    private float chanceForConsequences;
    private MissionType missionType;

    public static MissionHandler createNewMissionGoal() {
        return createNewMissionGoal(null);
    }

    public static MissionHandler createNewMissionGoal(MissionType missionType) {
        if (isNull(missionType))
            missionType = MissionType.getRandomMissionType();

        float chanceForConsequences = 0f;
        if (MissionType.RETRIEVAL == missionType)
            chanceForConsequences = (float) new Random().nextInt(101) / 100;

        return new MissionHandler(chanceForConsequences, missionType);
    }

    public static boolean haveObjectivesBeenCompleted(BaseBountyIntel bountyIntel, CampaignFleetAPI fleet, BattleAPI battle) {
        MissionType missionType = bountyIntel.getMissionHandler().getMissionType();
        boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);

        if (MissionType.ASSASSINATION == missionType) {
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (MissionType.DESTRUCTION == missionType) {
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (MissionType.OBLITERATION == missionType) {
            boolean isFleetDestroyed = isNull(fleet) || fleet.isEmpty() || fleet.getNumShips() <= 0;

            return isPlayerInvolved && isFleetDestroyed;
        }

        if (MissionType.RETRIEVAL == missionType) {
            String retrievalShipId = (String) fleet.getMemoryWithoutUpdate().get(RETRIEVAL_SHIP_KEY);
            if (isNull(retrievalShipId)) return false;

            FleetMemberAPI retrievalShip = null;
            boolean wasFlagshipRecovered = false;

            for (FleetMemberAPI destroyedFleetMember : Misc.getSnapshotMembersLost(fleet)) {
                if (destroyedFleetMember.getId().equals(retrievalShipId)) {
                    retrievalShip = destroyedFleetMember;
                    break;
                }
            }

            if (isNull(retrievalShip)) return false;

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

            for (FleetMemberAPI playerFleetMember : playerFleet.getFleetData().getMembersListCopy()) {
                if (playerFleetMember == retrievalShip) {
                    wasFlagshipRecovered = true;
                    break;
                }
            }

            return wasFlagshipRecovered;
        }

        return false;
    }

    public void startRetrievalSecondStage(BaseBountyIntel bountyIntel, FleetMemberAPI retrievalShip) {
        MissionType missionType = bountyIntel.getMissionHandler().getMissionType();

        if (MissionType.RETRIEVAL == missionType) {
            TriggeredMissionManager missionManager = TriggeredMissionManager.getInstance();
            missionManager.createRetrievalMissionEvent(bountyIntel, retrievalShip);
        } else {
            String errorMessage = String.format("BountiesExpanded - Tried to start Retrieval Mission while MissionType was [%s]", missionType.missionType);
            log.warn(errorMessage);
        }
    }

    // TODO
    public void calculateRetrievalBaseReward(BountyEntity bountyEntity, Collection<FleetMemberAPI> shipList) {
        int baseReward = bountyEntity.getBaseReward();
        int sumOfShipValues = 0;

        for (FleetMemberAPI fleetMemberAPI : shipList)
            sumOfShipValues += fleetMemberAPI.getHullSpec().getBaseValue();
    }

    public void listShipsToRecover(Collection<FleetMemberAPI> shipList, TooltipMakerAPI info, float padding, Color color) {
        if (isNullOrEmpty(shipList))
            return;

        for (FleetMemberAPI fleetMemberAPI : shipList)
            info.addPara("%s", padding, color, DescriptionUtils.generateShipNameWithClass(fleetMemberAPI, false));
    }
}
