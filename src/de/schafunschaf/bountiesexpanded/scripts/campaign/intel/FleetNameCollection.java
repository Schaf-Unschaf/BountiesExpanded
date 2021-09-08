package de.schafunschaf.bountiesexpanded.scripts.campaign.intel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class FleetNameCollection {
    private static final List<String> assassinationNames = new ArrayList<>();

    public static List<String> getAssassinationNames() {
        return assassinationNames;
    }

    public static String getRandomName() {
        return assassinationNames.get(new Random().nextInt(assassinationNames.size()));
    }

    public static void setAssassinationNames(Collection<String> assassinationNames) {
        FleetNameCollection.assassinationNames.addAll(assassinationNames);
    }
}
