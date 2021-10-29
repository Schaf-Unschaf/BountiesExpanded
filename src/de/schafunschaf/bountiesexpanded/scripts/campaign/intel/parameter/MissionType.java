package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;

import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class MissionType {
    public static final MissionType ASSASSINATION = new MissionType(
            "Assassination", "assassination", "eliminate the fleet commander",
            "TODO",
            true, true, false, false);

    public static final MissionType DESTRUCTION = new MissionType(
            "Destruction", "destruction", "destroy the flagship",
            "TODO",
            true, false, false, false);

    public static final MissionType OBLITERATION = new MissionType(
            "Obliteration", "obliteration", "destroy the targeted fleet",
            "TODO",
            true, true, true, false);

    public static final MissionType INTIMIDATION = new MissionType(
            "Intimidation", "intimidation", "force the fleet into retreat",
            "TODO",
            false, false, false, true);

    private static final MissionType[] VALUES = {
            ASSASSINATION, DESTRUCTION, OBLITERATION, INTIMIDATION
    };

    private final String missionTypeUCFirst;
    private final String missionType;
    private final String missionGoalShortDescription;
    private final String missionGoalLongDescription;
    private final boolean eliminateCaptain;
    private final boolean flagshipRecoverable;
    private final boolean destroyFleet;
    private final boolean forceRetreat;
    private final boolean[] missionGoals;
    private final String missionMemoryKey;

    private MissionType(String missionTypeUCFirst, String missionType, String missionGoalShortDescription, String missionGoalLongDescription,
                        boolean eliminateCaptain, boolean flagshipRecoverable, boolean destroyFleet, boolean forceRetreat) {
        this.missionTypeUCFirst = missionTypeUCFirst;
        this.missionType = missionType;
        this.missionGoalShortDescription = missionGoalShortDescription;
        this.missionGoalLongDescription = missionGoalLongDescription;
        this.eliminateCaptain = eliminateCaptain;
        this.flagshipRecoverable = flagshipRecoverable;
        this.destroyFleet = destroyFleet;
        this.forceRetreat = forceRetreat;
        this.missionGoals = new boolean[]{eliminateCaptain, flagshipRecoverable, destroyFleet, forceRetreat};
        this.missionMemoryKey = "$bountiesExpanded_missionType_" + missionType;
    }

    public static boolean areGoalsCompleted(BaseBountyIntel bountyIntel, CampaignFleetAPI fleet, BattleAPI battle) {
        MissionType missionType = bountyIntel.getMissionType();

        if (missionType == ASSASSINATION) {
            boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (missionType == DESTRUCTION) {
            boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);
            boolean isFlagshipDestroyed = isNull(fleet.getFlagship()) || fleet.getFlagship().getCaptain() != bountyIntel.getPerson();

            return isPlayerInvolved && isFlagshipDestroyed;
        }

        if (missionType == OBLITERATION) {
            boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);
            boolean isFleetDestroyed = isNull(fleet) || fleet.isEmpty() || fleet.getNumShips() <= 0;

            return isPlayerInvolved && isFleetDestroyed;
        }
        if (missionType == INTIMIDATION) {
            boolean isPlayerInvolved = battle.isPlayerInvolved() && battle.isInvolved(fleet) && !battle.onPlayerSide(fleet);
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

    public String getMissionTypeUCFirst() {
        return missionTypeUCFirst;
    }

    public String getMissionType() {
        return missionType;
    }

    public String getMissionGoalShortDescription() {
        return missionGoalShortDescription;
    }

    public String getMissionGoalLongDescription() {
        return missionGoalLongDescription;
    }

    public boolean isEliminateCaptain() {
        return eliminateCaptain;
    }

    public boolean isFlagshipRecoverable() {
        return flagshipRecoverable;
    }

    public boolean isDestroyFleet() {
        return destroyFleet;
    }

    public boolean isForceRetreat() {
        return forceRetreat;
    }

    public boolean[] getMissionGoals() {
        return missionGoals;
    }

    public String getMissionMemoryKey() {
        return missionMemoryKey;
    }
}
