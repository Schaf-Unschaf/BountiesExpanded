package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import java.util.Map;

import static com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import static com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class BountyResult {
    public final BountyResultType type;
    public final int payment;
    public int bonus = 0;
    public Float share = 0f;
    public ReputationAdjustmentResult rep = null;
    public float targetRepAfterBattle = 0;
    public float rewardAdjustment = 0;
    public Map<HullSize, int[]> destroyedShips = null;

    // Skirmish Result
    public BountyResult(BountyResultType type, int payment, int bonus, Float share, ReputationAdjustmentResult rep, float targetRepAfterBattle, float rewardAdjustment, Map<HullSize, int[]> destroyedShips) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.share = share;
        this.rep = rep;
        this.targetRepAfterBattle = targetRepAfterBattle;
        this.rewardAdjustment = rewardAdjustment;
        this.destroyedShips = destroyedShips;
    }

    // Assassination Result
    public BountyResult(BountyResultType type, int payment, int bonus, float targetRepAfterBattle) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.targetRepAfterBattle = targetRepAfterBattle;
    }

    // HVB Result
    public BountyResult(BountyResultType type, int payment, ReputationAdjustmentResult rep) {
        this.type = type;
        this.payment = payment;
        this.rep = rep;
    }

    // War Criminal Result
    public BountyResult(BountyResultType type, int payment, ReputationAdjustmentResult rep, float targetRepAfterBattle) {
        this.type = type;
        this.payment = payment;
        this.rep = rep;
        this.targetRepAfterBattle = targetRepAfterBattle;
    }
}
