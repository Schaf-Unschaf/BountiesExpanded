package de.schafunschaf.bountiesexpanded;

import com.fs.starfarer.api.impl.campaign.DebugFlags;

public class Settings {
    public static boolean sheepDebug = false;
    public static boolean prepareUpdate = false;

    public static boolean ignorePlayerMarkets = true;
    public static boolean shipsCanBeRecoveredWithSP = false;

    public static int baseRewardPerFP = 250;

    public static boolean skirmishActive = true;
    public static double skirmishSpawnChance = 0.5;
    public static int skirmishMinBounties = 1;
    public static int skirmishMaxBounties = 4;
    public static int skirmishMinDuration = 30;
    public static int skirmishMaxDuration = 60;
    public static int skirmishBaseShipBounty = 7500;

    public static boolean assassinationActive = true;
    public static double assassinationSpawnChance = 0.1;
    public static int assassinationMinBounties = 0;
    public static int assassinationMaxBounties = 2;
    public static double assassinationMinTravelDistance = 5;
    public static double assassinationBaseRewardMultiplier = 1;
    public static double assassinationBonusRewardMultiplier = 4;

    public static boolean highValueBountyActive = true;
    public static int highValueBountyMaxBounties = 3;
    public static double highValueBountySpawnChance = 0.3;
    public static double highValueBountyMinTimeBetweenSpawns = 14;
    public static double highValueBountyMaxTimeBetweenSpawns = 28;
    public static boolean highValueBountyRevengeActive = false;

    public static boolean warCriminalActive = true;
    public static double warCriminalSpawnChance = 0.2;
    public static int warCriminalMinBounties = 0;
    public static int warCriminalMaxBounties = 2;
    public static int warCriminalMinDuration = 30;
    public static int warCriminalMaxDuration = 60;

    public static boolean triggeredEventsActive = true;

    public static boolean retrievalEventActive = true;
    public static int retrievalEventDuration = 30;

    public static boolean isDebugActive() {
        return DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.sheepDebug;
    }
}
