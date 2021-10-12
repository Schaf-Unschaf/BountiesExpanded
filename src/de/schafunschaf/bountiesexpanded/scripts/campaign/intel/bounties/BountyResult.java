package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import static com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;

public class BountyResult {
    public final BountyResultType type;
    public final int payment;
    public int bonus = 0;
    public Float share = 0f;
    public ReputationAdjustmentResult rep = null;
    public float rewardAdjustment = 0;

    // Skirmish Result
    public BountyResult(BountyResultType type, int payment, int bonus, Float share, ReputationAdjustmentResult rep, float rewardAdjustment) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.share = share;
        this.rep = rep;
        this.rewardAdjustment = rewardAdjustment;
    }

    // Assassination Result
    public BountyResult(BountyResultType type, int payment, int bonus) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
    }

    // HVB Result
    public BountyResult(BountyResultType type, int payment, ReputationAdjustmentResult rep) {
        this.type = type;
        this.payment = payment;
        this.rep = rep;
    }
}
