package de.schafunschaf.bountiesexpanded.scripts.campaign.intel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class NameStringCollection {
    private static final List<String> suspiciousNames = new ArrayList<>();
    private static final List<String> fleetActionTexts = new ArrayList<>();

    public static List<String> getSuspiciousNames() {
        return suspiciousNames;
    }

    public static String getSuspiciousName() {
        return suspiciousNames.get(new Random().nextInt(suspiciousNames.size()));
    }

    public static void setSuspiciousNames(Collection<String> suspiciousNames) {
        NameStringCollection.suspiciousNames.addAll(suspiciousNames);
    }

    public static List<String> getfleetActionTexts() {
        return fleetActionTexts;
    }

    public static String getFleetActionText() {
        return fleetActionTexts.get(new Random().nextInt(fleetActionTexts.size()));
    }

    public static void setFleetActionTexts(Collection<String> fleetActionTexts) {
        NameStringCollection.fleetActionTexts.addAll(fleetActionTexts);
    }
}
