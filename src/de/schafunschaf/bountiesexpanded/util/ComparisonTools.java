package de.schafunschaf.bountiesexpanded.util;

import java.util.Collection;
import java.util.Map;

public class ComparisonTools {
    public static boolean containsAny(final Collection<?> collection1, final Collection<?> collection2) {
        if (isNull(collection1) || isNull(collection2))
            return false;
        for (final Object value : collection1) {
            if (collection2.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        if (isNotNull(collection))
            return collection.isEmpty();
        return true;
    }

    public static boolean isNullOrEmpty(Map<?, ?> collection) {
        if (isNotNull(collection))
            return collection.isEmpty();
        return true;
    }
}
