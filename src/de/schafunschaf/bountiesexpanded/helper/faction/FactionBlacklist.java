package de.schafunschaf.bountiesexpanded.helper.faction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FactionBlacklist {
    private static final Set<String> defaultBlacklist = new HashSet<>();

    public static void addFaction(String faction) {
        defaultBlacklist.add(faction);
    }

    public static void addFactions(Collection<String> factionCollection) {
        defaultBlacklist.addAll(factionCollection);
    }

    public static Set<String> getDefaultBlacklist() {
        return new HashSet<>(defaultBlacklist);
    }
}
