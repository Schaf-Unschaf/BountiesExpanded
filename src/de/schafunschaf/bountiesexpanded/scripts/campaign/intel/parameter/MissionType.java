package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import lombok.Getter;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
public class MissionType {
    public static final MissionType ASSASSINATION = new MissionType(
            "Assassination", "assassination", "eliminate the fleet commander",
            "TODO",
            true, false);

    public static final MissionType DESTRUCTION = new MissionType(
            "Destruction", "destruction", "destroy the flagship",
            "TODO",
            true, false);

    public static final MissionType OBLITERATION = new MissionType(
            "Obliteration", "obliteration", "destroy the targeted fleet",
            "TODO",
            true, false);

    public static final MissionType SKIRMISH = new MissionType(
            "Skirmish", "skirmish", "thin out or destroy the targeted fleet",
            "TODO",
            false, false);

    public static final MissionType INTIMIDATION = new MissionType(
            "Intimidation", "intimidation", "force the fleet into retreat",
            "TODO",
            false, true);

    private static final MissionType[] VALUES = {
            ASSASSINATION, DESTRUCTION, OBLITERATION, INTIMIDATION, SKIRMISH
    };

    private final String missionTypeUCFirst;
    private final String missionType;
    private final String missionGoalShortDescription;
    private final String missionGoalLongDescription;
    private final boolean flagshipRecoverable;
    private final boolean forceRetreat;
    private final String missionMemoryKey;

    private MissionType(String missionTypeUCFirst, String missionType, String missionGoalShortDescription,
                        String missionGoalLongDescription, boolean flagshipRecoverable, boolean forceRetreat) {
        this.missionTypeUCFirst = missionTypeUCFirst;
        this.missionType = missionType;
        this.missionGoalShortDescription = missionGoalShortDescription;
        this.missionGoalLongDescription = missionGoalLongDescription;
        this.flagshipRecoverable = flagshipRecoverable;
        this.forceRetreat = forceRetreat;
        this.missionMemoryKey = "$bountiesExpanded_missionType_" + missionType;
    }

    public static boolean haveObjectivesBeenCompleted(BaseBountyIntel bountyIntel, CampaignFleetAPI fleet, BattleAPI battle) {
        MissionType missionType = bountyIntel.getMissionType();
        boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);

        if (missionType == ASSASSINATION) {
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (missionType == DESTRUCTION) {
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (missionType == OBLITERATION) {
            boolean isFleetDestroyed = isNull(fleet) || fleet.isEmpty() || fleet.getNumShips() <= 0;

            return isPlayerInvolved && isFleetDestroyed;
        }

        if (missionType == SKIRMISH) {
            int maxFleetSizeForCompletion = bountyIntel.getMaxFleetSizeForCompletion();

            return fleet.getNumShips() <= maxFleetSizeForCompletion;
        }

        if (missionType == INTIMIDATION) {
            boolean isFlagshipAlive = isNotNull(fleet.getFlagship()) && fleet.getFlagship().getCaptain() == bountyIntel.getPerson();

        }

        return false;
    }

    public static MissionType[] values() {
        return VALUES.clone();
    }

    public static MissionType getRandomMissionType() {
        return values()[new Random().nextInt(values().length)];
    }
}
