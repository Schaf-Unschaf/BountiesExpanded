package de.schafunschaf.bountiesexpanded;

import java.util.Set;

public class Blacklists {
    private static Set<String> SKIRMISH_BOUNTY_BLACKLIST;

    public static Set<String> getSkirmishBountyBlacklist() {
        return SKIRMISH_BOUNTY_BLACKLIST;
    }

    public static void setSkirmishBountyBlacklist(Set<String> skirmishBountyBlacklist) {
        SKIRMISH_BOUNTY_BLACKLIST = skirmishBountyBlacklist;
    }
}
