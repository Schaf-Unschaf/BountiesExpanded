package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.result;

abstract class BaseBountyResult {
    public enum BountyResultType {
        END_PLAYER_BOUNTY,
        END_PLAYER_NO_BOUNTY,
        END_PLAYER_NO_REWARD,
        END_OTHER,
        END_TIME,
    }

    public BountyResultType type;
    public int payment;

    public BaseBountyResult(BountyResultType type, int payment) {
        this.type = type;
        this.payment = payment;
    }
}
