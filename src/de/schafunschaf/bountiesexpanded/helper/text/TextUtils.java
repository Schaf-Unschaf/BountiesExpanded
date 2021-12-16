package de.schafunschaf.bountiesexpanded.helper.text;

import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.Collection;


public class TextUtils {

     // Credits to Vayra's Sector
    public static String getAlliterativeFleetName(String name, Collection<String> nameCollection) {
        String first = name.substring(0, 1).toLowerCase();
        WeightedRandomPicker<String> alliterative = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> backup = new WeightedRandomPicker<>();

        for (String collectionName : nameCollection) {
            if (collectionName.substring(0, 1).equals(first)) {
                alliterative.add(collectionName);
            } else {
                backup.add(collectionName);
            }
        }

        if (!alliterative.isEmpty()) {
            return alliterative.pick();
        } else {
            return backup.pick();
        }
    }
}
