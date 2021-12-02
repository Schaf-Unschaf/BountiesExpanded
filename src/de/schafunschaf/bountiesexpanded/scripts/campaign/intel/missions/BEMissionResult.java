package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions;

import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;

@Getter
@Setter
@NoArgsConstructor
public class BEMissionResult {
    BaseMissionIntel.MissionState missionState;
    private int payment;
    private ReputationAdjustmentResult offeringFactionRepChange;
}
