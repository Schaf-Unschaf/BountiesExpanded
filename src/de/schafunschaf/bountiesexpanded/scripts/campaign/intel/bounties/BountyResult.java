package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;

public class BountyResult {
    public final BountyResultType type;
    public final int payment;
    public final int bonus;
    public final Float share;
    public final ReputationActionResponsePlugin.ReputationAdjustmentResult rep;

    public BountyResult(BountyResultType type, int payment, int bonus, Float share, ReputationActionResponsePlugin.ReputationAdjustmentResult rep) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.share = share;
        this.rep = rep;
    }
}
