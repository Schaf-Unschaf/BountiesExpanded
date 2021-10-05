package de.schafunschaf.bountiesexpanded.helper.location;

import com.fs.starfarer.api.impl.campaign.ids.Tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TagCollection {
    private static final String[] vanillaBountySystemTagArray = {
            Tags.THEME_MISC_SKIP,
            Tags.THEME_MISC,
            Tags.THEME_REMNANT_NO_FLEETS,
            Tags.THEME_RUINS,
            Tags.THEME_REMNANT_DESTROYED,
            Tags.THEME_CORE_UNPOPULATED
    };

    private static final String[] remnantSystemTagArray = {
            Tags.THEME_REMNANT,
            Tags.THEME_REMNANT_MAIN,
            Tags.THEME_REMNANT_SECONDARY,
            Tags.THEME_REMNANT_RESURGENT
    };

    private static final String[] derelictSystemTagArray = {
            Tags.THEME_DERELICT,
            Tags.THEME_DERELICT_CRYOSLEEPER,
            Tags.THEME_DERELICT_MOTHERSHIP,
            Tags.THEME_DERELICT_SURVEY_SHIP,
            Tags.THEME_DERELICT_PROBES
    };

    public static final Set<String> DERELICT_SYSTEM_TAGS = new HashSet<>(Arrays.asList(derelictSystemTagArray));
    public static final Set<String> REMNANT_SYSTEM_TAGS = new HashSet<>(Arrays.asList(remnantSystemTagArray));
    public static final Set<String> VANILLA_BOUNTY_SYSTEM_TAGS = new HashSet<>(Arrays.asList(vanillaBountySystemTagArray));
}
