package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.result;

public class AssassinationBountyResult extends BaseBountyResult {
    public int bonus;

    public AssassinationBountyResult(BountyResultType type, int payment, int bonus) {
        super(type, payment);
        this.bonus = bonus;
    }
}
