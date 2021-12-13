package de.schafunschaf.bountiesexpanded;

import com.fs.starfarer.api.impl.campaign.DebugFlags;

public class Settings {
    public static boolean sheepDebug = false;
    public static boolean prepareUpdate = false;

    public static boolean ignorePlayerMarkets = true;

    public static int baseRewardPerFP = 250;

    public static boolean skirmishActive = true;
    public static double skirmishSpawnChance = 0.5;
    public static int skirmishMinBounties = 0;
    public static int skirmishMaxBounties = 2;
    public static int skirmishMinDuration = 30;
    public static int skirmishMaxDuration = 60;
    public static int skirmishBaseShipBounty = 7500;

    public static boolean assassinationActive = true;
    public static double assassinationSpawnChance = 0.1;
    public static int assassinationMinBounties = 0;
    public static int assassinationMaxBounties = 1;
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

    public static boolean onlyRecoverWithSP = false;

    public static boolean pirateBountyActive = true;
    public static boolean disableVayraBounties = true;
    public static double pirateBountySpawnChance = 0.7;
    public static int pirateBountyMinBounties = 1;
    public static int pirateBountyMaxBounties = 4;
    public static int pirateBountyMinDuration = 60;
    public static int pirateBountyMaxDuration = 90;

    public static boolean deserterBountyActive = true;
    public static double deserterBountySpawnChance = 0.3;
    public static int deserterBountyMinBounties = 0;
    public static int deserterBountyMaxBounties = 2;
    public static int deserterBountyMinDuration = 60;
    public static int deserterBountyMaxDuration = 90;

    public static boolean isDebugActive() {
        return DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.sheepDebug;
    }
}
