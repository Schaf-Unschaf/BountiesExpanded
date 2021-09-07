package de.schafunschaf.bountiesexpanded;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Blacklists {
    private static final Set<String> defaultBlacklist = new HashSet<>();
    private static Set<String> SKIRMISH_BOUNTY_BLACKLIST;

    public static Set<String> getDefaultBlacklist() {
        return new HashSet<>(defaultBlacklist);
    }

    public static void addFaction(String faction) {
        defaultBlacklist.add(faction);
    }

    public static void addFactions(Collection<String> factionCollection) {
        defaultBlacklist.addAll(factionCollection);
    }

    public static Set<String> getSkirmishBountyBlacklist() {
        return SKIRMISH_BOUNTY_BLACKLIST;
    }

    public static void setSkirmishBountyBlacklist(Set<String> skirmishBountyBlacklist) {
        SKIRMISH_BOUNTY_BLACKLIST = skirmishBountyBlacklist;
    }
}
