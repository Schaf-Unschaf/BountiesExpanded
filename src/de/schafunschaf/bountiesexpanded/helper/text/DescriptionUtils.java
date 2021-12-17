package de.schafunschaf.bountiesexpanded.helper.text;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUtils;
import de.schafunschaf.bountiesexpanded.helper.location.LocationUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

public class DescriptionUtils {
    public static final float DEFAULT_IMAGE_HEIGHT = 100f;

    public static void generateShipListForIntel(TooltipMakerAPI info, float width, float padding, CampaignFleetAPI fleet, int maxShipsToDisplay, int maxRows, boolean showShipsRemaining) {
        Random random = new Random(fleet.getCommander().getNameString().hashCode() * 170000L);
        List<FleetMemberAPI> fleetMemberList;
        if (Settings.isDebugActive()) {
            fleetMemberList = FleetGenerator.createCompleteCopyForIntel(fleet);
        } else
            fleetMemberList = FleetGenerator.createCopyForIntel(fleet, maxShipsToDisplay, random);

        generateShipListForIntel(info, width, padding, fleetMemberList, maxShipsToDisplay, maxRows, showShipsRemaining, random);
    }

    public static void generateShipListForIntel(TooltipMakerAPI info, float width, float padding, List<FleetMemberAPI> shipList, int maxShipsToDisplay, int maxRows, boolean showShipsRemaining, Random random) {
        if (isNullOrEmpty(shipList))
            return;

        if (isNull(random))
            random = new Random(shipList.get(0).getId().hashCode());

        if (Settings.isDebugActive())
            maxRows = 0;

        CampaignFleetAPI fleet = shipList.get(0).getFleetData().getFleet();

        shipList = FleetUtils.orderListBySize(shipList);

        int cols = 7;
        int rows = (int) Math.ceil(shipList.size() / (float) cols);
        if (maxRows > 0 && rows > maxRows)
            rows = maxRows;

        float iconSize = width / cols;
        if (Settings.isDebugActive()) {
            int enemyFP = fleet.getFleetPoints();
            int playerFP = Global.getSector().getPlayerFleet().getFleetPoints();
            info.addSectionHeading("DEBUG INFO", Alignment.MID, padding);
            info.addPara("Enemy  FP -> " + enemyFP, padding, fleet.getFaction().getBaseUIColor(), String.valueOf(enemyFP));
            info.addPara("Player FP -> " + playerFP, padding, Misc.getHighlightColor(), String.valueOf(playerFP));
        }

        info.addShipList(cols, rows, iconSize, fleet.getFaction().getBaseUIColor(), shipList, padding);

        if (showShipsRemaining && !Settings.isDebugActive()) {
            int num = shipList.size() - maxShipsToDisplay;
            num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));

            if (num < 5) num = 0;
            else if (num < 10) num = 5;
            else if (num < 20) num = 10;
            else num = 20;

            if (num > 1) {
                info.addPara("The intel assessment notes the fleet may contain upwards of %s other ships" +
                        " of lesser significance.", padding, Misc.getHighlightColor(), "" + num);
            } else {
                info.addPara("The intel assessment notes the fleet may contain several other ships" +
                        " of lesser significance.", padding);
            }
        }
    }

    public static void addDifficultyText(TooltipMakerAPI info, float padding, Difficulty difficulty) {
        info.addPara("Your tactical officer classifies this fleet as " + difficulty.getShortDescriptionAnOrA() + " %s encounter.",
                padding, difficulty.getColor(), difficulty.getShortDescription());
    }


    private static void generateFancyCommanderDescription(TooltipMakerAPI info, float padding, CampaignFleetAPI fleet, PersonAPI person) {
        if (isNull(person))
            return;
        if (isNull(person.getStats()))
            return;

        PersonAPI commander = fleet.getCommander();
        String heOrShe = person.getHeOrShe();
        String levelDesc;
        String skillDesc;

        int personLevel = person.getStats().getLevel();
        if (personLevel <= 4) levelDesc = "an unremarkable officer";
        else if (personLevel <= 7) levelDesc = "a capable officer";
        else if (personLevel <= 11) levelDesc = "a highly capable officer";
        else levelDesc = "an exceptionally capable officer";

        List<MutableCharacterStatsAPI.SkillLevelAPI> knownSkills = commander.getStats().getSkillsCopy();
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();

        for (MutableCharacterStatsAPI.SkillLevelAPI skill : knownSkills) {
            String skillName = skill.getSkill().getId();
            switch (skillName) {
                case Skills.WEAPON_DRILLS:
                    picker.add("a great number of illegal weapon modifications");
                    break;
                case Skills.AUXILIARY_SUPPORT:
                    picker.add("armed-to-the-teeth support ships");
                    break;
                case Skills.COORDINATED_MANEUVERS:
                    picker.add("a high effectiveness in coordinating the maneuvers of ships during combat");
                    break;
                case Skills.WOLFPACK_TACTICS:
                    picker.add("highly coordinated frigate attacks");
                    break;
                case Skills.CREW_TRAINING:
                    picker.add("a very courageous crew");
                    break;
                case Skills.CARRIER_GROUP:
                    picker.add("a noteworthy level of skill in running carrier operations");
                    break;
                case Skills.OFFICER_TRAINING:
                    picker.add("having extremely skilled subordinates");
                    break;
                case Skills.OFFICER_MANAGEMENT:
                    picker.add("having a high number of skilled subordinates");
                    break;
                case Skills.NAVIGATION:
                    picker.add("having highly skilled navigators");
                    break;
                case Skills.SENSORS:
                    picker.add("having overclocked sensory equipment");
                    break;
                case Skills.ELECTRONIC_WARFARE:
                    picker.add("being proficient in electronic warfare");
                    break;
                case Skills.FIGHTER_UPLINK:
                    picker.add("removing engine-safety-protocols from fighters");
                    break;
                case Skills.FLUX_REGULATION:
                    picker.add("using overclocked flux coils");
                    break;
                case Skills.PHASE_CORPS:
                    picker.add("using experimental phase coils");
                    break;
                case Skills.FIELD_REPAIRS:
                    picker.add("having highly skilled mechanics");
                    break;
                case Skills.DERELICT_CONTINGENT:
                    picker.add("using military-grade duct tape");
                    break;
            }
        }

        Random random = new Random(person.getId().hashCode() * 1337L);
        picker.setRandom(random);

        skillDesc = picker.isEmpty() ? "nothing, really" : picker.pick();
        if (levelDesc.contains("unremarkable"))
            levelDesc = "an otherwise unremarkable officer";

        info.addPara("%s is %s known for %s.", padding, commander.getFaction().getBaseUIColor(), Misc.ucFirst(heOrShe), levelDesc, skillDesc);
    }

    public static void generateFancyFleetDescription(TooltipMakerAPI info, float padding, CampaignFleetAPI fleet, PersonAPI person) {
        if (isNull(person))
            return;

        boolean isRareShip = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY);
        int fleetSize = fleet.getNumShips();
        FleetMemberAPI flagship = fleet.getFlagship();
        PersonAPI commander = fleet.getCommander();
        String shipName = flagship.getShipName();
        String shipClass = flagship.getHullSpec().getHullNameWithDashClass();
        String aOrAn = FormattingTools.aOrAn(shipClass);
        String rareString = isRareShip ? "a rare" : aOrAn;
        String shipDesignation = flagship.getHullSpec().getDesignation().toLowerCase();
        String hisOrHer = person.getHisOrHer();
        String fleetDesc;
        String outputText;

        if (fleetSize <= 4) fleetDesc = "few ships";
        else if (fleetSize <= 10) fleetDesc = "small fleet";
        else if (fleetSize <= 20) fleetDesc = "medium-sized fleet";
        else if (fleetSize <= 32) fleetDesc = "large fleet";
        else if (fleetSize <= 44) fleetDesc = "very large fleet";
        else if (fleetSize <= 56) fleetDesc = "gigantic fleet";
        else fleetDesc = "freaking armada";

        Color[] highlightColors = new Color[]{commander.getFaction().getBaseUIColor(), Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getHighlightColor()};
        String[] highlights = new String[]{commander.getFaction().getRank(commander.getRankId()) + " " + person.getName().getFullName(), fleetDesc, shipName, shipClass, shipDesignation};

        outputText = String.format("%s is in command of a %s and personally commands the %s, " + rareString + " %s %s, as " + hisOrHer + " flagship.", (Object[]) highlights);

        info.addPara(outputText, padding, highlightColors, highlights);
    }

    public static String generateShipNameWithClass(FleetMemberAPI ship, boolean isRareShip) {
        if (isNull(ship))
            return "NO SHIP FOR NAME AND CLASS";

        String rareString = isRareShip ? "rare " : "";
        String shipName = ship.getShipName();
        String shipClass = ship.getHullSpec().getHullNameWithDashClass();
        String aOrAn = FormattingTools.aOrAn(shipClass);
        String shipDesignation = ship.getHullSpec().getDesignation().toLowerCase();
        String shipType = String.format("%s%s %s", rareString, shipClass, shipDesignation);

        return String.format("%s, %s %s", shipName, aOrAn, shipType);
    }

    public static String generateShipClassWithDesignation(FleetMemberAPI ship, boolean isRareShip) {
        if (isNull(ship))
            return "NO SHIP FOR NAME AND CLASS";

        String rareString = isRareShip ? "rare " : "";
        String shipClass = ship.getHullSpec().getHullNameWithDashClass();
        String aOrAn = isRareShip ? "a" : FormattingTools.aOrAn(shipClass);
        String shipDesignation = ship.getHullSpec().getDesignation().toLowerCase();
        String shipType = String.format("%s%s %s", rareString, shipClass, shipDesignation);

        return String.format("%s %s", aOrAn, shipType); // a (rare) Wolf-Class frigate
    }

    public static void generateHideoutDescription(TooltipMakerAPI info, BaseBountyIntel baseBountyIntel, Color highlightColor) {
        String isOrWas = isNull(baseBountyIntel.getFleet().getAI().getCurrentAssignmentType()) ? "was last seen " : "is ";
        SectorEntityToken hideout = baseBountyIntel.getSpawnLocation();
        info.addPara(
                "The fleet " + isOrWas + "near " + hideout.getName() + " in the "
                        + hideout.getStarSystem().getName() + ".",
                10f, highlightColor, hideout.getName(), hideout.getStarSystem().getName());
    }

    public static void generateFakeHideoutDescription(TooltipMakerAPI info, BaseBountyIntel baseBountyIntel, float padding) {
        String heOrShe = FormattingTools.capitalizeFirst(baseBountyIntel.getPerson().getHeOrShe());
        SectorEntityToken spawnLocation = baseBountyIntel.getSpawnLocation();
        SectorEntityToken fakeLocation = spawnLocation.getContainingLocation().createToken(0.0F, 0.0F);

        fakeLocation.setOrbit(Global.getFactory().createCircularOrbit(spawnLocation, 0.0F, 1000.0F, 100.0F));
        String loc = BreadcrumbSpecial.getLocatedString(fakeLocation);
        loc = loc.replaceAll("orbiting", "hiding out near");
        loc = loc.replaceAll("located in", "hiding out in");

        info.addPara(heOrShe + " is rumored to be " + loc + ".", padding);
    }

    public static void generatePatrolDescription(TooltipMakerAPI info, BaseBountyIntel baseBountyIntel, SectorEntityToken location, float padding, boolean isRealLocation) {
        String heOrShe = FormattingTools.capitalizeFirst(baseBountyIntel.getPerson().getHeOrShe());
        CampaignFleetAPI fleet = baseBountyIntel.getFleet();
        SectorEntityToken fakeLocation = location.getContainingLocation().createToken(fleet.getCurrentAssignment().getTarget().getLocation());

        fakeLocation.setOrbit(Global.getFactory().createCircularOrbit(location, 0.0F, 1000.0F, 100.0F));
        String terrainString = BreadcrumbSpecial.getTerrainString(fleet);
        String loc;
        if (isNotNull(terrainString)) {
            String systemDescription = BreadcrumbSpecial.getLocationDescription(fleet, isRealLocation);
            loc = String.format("%s is rumored to be currently flying through %s in %s.", heOrShe, terrainString, systemDescription);
            info.addPara(loc, padding);
        } else {
            loc = BreadcrumbSpecial.getLocatedString(LocationUtils.getNearestLocation(fleet));
            loc = loc.replaceAll("orbiting", "patrolling near");
            loc = loc.replaceAll("located in", "hiding out in");

            info.addPara(heOrShe + " is rumored to be " + loc + ".", padding);
        }
    }

    public static void generateFakeTravelDescription(TooltipMakerAPI info, BaseBountyIntel baseBountyIntel, float padding) {
        String heOrShe = FormattingTools.capitalizeFirst(baseBountyIntel.getPerson().getHeOrShe());
        SectorEntityToken travelDestination = baseBountyIntel.getTravelDestination();
        SectorEntityToken fakeLocation = travelDestination.getContainingLocation().createToken(0.0F, 0.0F);

        fakeLocation.setOrbit(Global.getFactory().createCircularOrbit(travelDestination, 0.0F, 1000.0F, 100.0F));
        String obfuscatedLocation = BreadcrumbSpecial.getLocationDescription(travelDestination, false);
        String travelDescription = String.format("%s is currently traveling to %s.", heOrShe, obfuscatedLocation);

        info.addPara(travelDescription, padding);
    }
}