package de.schafunschaf.bountiesexpanded;

import com.fs.starfarer.api.impl.campaign.DebugFlags;

public class Settings {
    public static boolean SHEEP_DEBUG = false;
    public static boolean PREPARE_UPDATE = false;

    public static final boolean IGNORE_PLAYER_MARKETS = true;

    public static final int BASE_REWARD_PER_FP = 250;

    public static boolean SKIRMISH_ACTIVE = true;
    public static double SKIRMISH_SPAWN_CHANCE = 0.5;
    public static int SKIRMISH_MIN_BOUNTIES = 1;
    public static int SKIRMISH_MAX_BOUNTIES = 4;
    public static int SKIRMISH_MIN_DURATION = 30;
    public static int SKIRMISH_MAX_DURATION = 60;
    public static final int SKIRMISH_BASE_SHIP_BOUNTY = 7500;

    public static boolean ASSASSINATION_ACTIVE = true;
    public static double ASSASSINATION_SPAWN_CHANCE = 0.1;
    public static int ASSASSINATION_MIN_BOUNTIES = 0;
    public static int ASSASSINATION_MAX_BOUNTIES = 2;
    public static double ASSASSINATION_MIN_TRAVEL_DISTANCE = 5;
    public static double ASSASSINATION_MAX_DISTANCE_BONUS_MULTIPLIER = 1;
    public static final double ASSASSINATION_BASE_REWARD_MULTIPLIER = 0.5;
    public static final double ASSASSINATION_BONUS_REWARD_MULTIPLIER = 3;

    public static boolean HIGH_VALUE_BOUNTY_ACTIVE = true;
    public static int HIGH_VALUE_BOUNTY_MAX_BOUNTIES = 3;
    public static double HIGH_VALUE_BOUNTY_SPAWN_CHANCE = 0.3;
    public static double HIGH_VALUE_BOUNTY_MIN_TIME_BETWEEN_SPAWNS = 14;
    public static double HIGH_VALUE_BOUNTY_MAX_TIME_BETWEEN_SPAWNS = 28;

    public static boolean isDebugActive() {
        return DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.SHEEP_DEBUG;
    }
}
