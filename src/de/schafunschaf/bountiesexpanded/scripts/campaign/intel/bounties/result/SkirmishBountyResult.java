package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.result;

import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;

public class SkirmishBountyResult extends BaseBountyResult {
    public Float share;
    public ReputationActionResponsePlugin.ReputationAdjustmentResult rep;

    public SkirmishBountyResult(BountyResultType type, int payment, float share, ReputationActionResponsePlugin.ReputationAdjustmentResult rep) {
        super(type, payment);
        this.share = share;
        this.rep = rep;
    }
}
