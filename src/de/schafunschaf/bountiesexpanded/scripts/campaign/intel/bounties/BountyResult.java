package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;

public class BountyResult {
    public BountyResultType type;
    public int payment;
    public int bonus;
    public Float share;
    public ReputationActionResponsePlugin.ReputationAdjustmentResult rep;

    public BountyResult(BountyResultType type, int payment, int bonus, Float share, ReputationActionResponsePlugin.ReputationAdjustmentResult rep) {
        this.type = type;
        this.payment = payment;
        this.bonus = bonus;
        this.share = share;
        this.rep = rep;
    }

    public enum BountyResultType {
        END_PLAYER_BOUNTY,
        END_PLAYER_NO_BOUNTY,
        END_PLAYER_NO_REWARD,
        END_OTHER,
        END_TIME,
    }
}
