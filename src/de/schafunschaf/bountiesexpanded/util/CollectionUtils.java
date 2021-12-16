package de.schafunschaf.bountiesexpanded.util;

import java.util.List;
import java.util.Random;

public class CollectionUtils {
    public static Object getRandomEntry(List<?> collection) {
        if (ComparisonTools.isNullOrEmpty(collection))
            return null;

        return collection.get(new Random().nextInt(collection.size()));
    }
}
