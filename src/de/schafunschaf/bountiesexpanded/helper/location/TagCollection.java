package de.schafunschaf.bountiesexpanded.helper.location;

import com.fs.starfarer.api.impl.campaign.ids.Tags;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;

import java.util.*;

public class TagCollection {
    private static final String[] vanillaBountySystemTagArray = {
            Tags.THEME_MISC_SKIP,
            Tags.THEME_MISC,
            Tags.THEME_REMNANT_NO_FLEETS,
            Tags.THEME_RUINS,
            Tags.THEME_REMNANT_DESTROYED,
            Tags.THEME_CORE_UNPOPULATED
    };
    public static final Set<String> VANILLA_BOUNTY_SYSTEM_TAGS = new HashSet<>(Arrays.asList(vanillaBountySystemTagArray));
    private static final String[] remnantSystemTagArray = {
            Tags.THEME_REMNANT,
            Tags.THEME_REMNANT_MAIN,
            Tags.THEME_REMNANT_SECONDARY,
            Tags.THEME_REMNANT_RESURGENT
    };
    public static final Set<String> REMNANT_SYSTEM_TAGS = new HashSet<>(Arrays.asList(remnantSystemTagArray));
    private static final String[] derelictSystemTagArray = {
            Tags.THEME_DERELICT,
            Tags.THEME_DERELICT_CRYOSLEEPER,
            Tags.THEME_DERELICT_MOTHERSHIP,
            Tags.THEME_DERELICT_SURVEY_SHIP,
            Tags.THEME_DERELICT_PROBES
    };
    public static final Set<String> DERELICT_SYSTEM_TAGS = new HashSet<>(Arrays.asList(derelictSystemTagArray));

    public static Map<String, Integer> getDefaultTagMap(Set<String> tagCollection) {
        if (ComparisonTools.isNullOrEmpty(tagCollection))
            return null;

        HashMap<String, Integer> tagMap = new HashMap<>();
        for (String tagName : tagCollection)
            tagMap.put(tagName, 1);

        return tagMap;
    }
}
