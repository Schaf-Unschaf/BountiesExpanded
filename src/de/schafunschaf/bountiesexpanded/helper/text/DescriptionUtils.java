package de.schafunschaf.bountiesexpanded.helper.text;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

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

        boolean hasRareFlagship = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY);
        int fleetSize = fleet.getNumShips();
        FleetMemberAPI flagship = fleet.getFlagship();
        PersonAPI commander = fleet.getCommander();
        String shipNameWithClass = generateShipNameWithClass(flagship, hasRareFlagship);
        String hisOrHer = person.getHisOrHer();
        String fleetDesc;
        String outputText;
        String[] highlights;

        if (fleetSize <= 8) fleetDesc = "small fleet";
        else if (fleetSize <= 16) fleetDesc = "medium-sized fleet";
        else if (fleetSize <= 24) fleetDesc = "large fleet";
        else if (fleetSize <= 32) fleetDesc = "very large fleet";
        else if (fleetSize <= 40) fleetDesc = "gigantic fleet";
        else fleetDesc = "freaking armada";

        highlights = new String[]{commander.getFaction().getRank(commander.getRankId()) + " " + person.getName().getFullName(), fleetDesc, shipNameWithClass};
        outputText = String.format("%s is in command of a %s and personally commands the %s, as " + hisOrHer + " flagship.", (Object[]) highlights);

        info.addPara(outputText, padding, commander.getFaction().getBaseUIColor(), highlights);
    }

    public static String generateShipNameWithClass(FleetMemberAPI ship, boolean isRareShip) {
        String rareString = isRareShip ? "rare " : "";
        String shipName = ship.getShipName();
        String shipClass = ship.getHullSpec().getHullNameWithDashClass();
        String aOrAn = FormattingTools.aOrAn(shipClass);
        String shipDesignation = ship.getHullSpec().getDesignation().toLowerCase();
        String shipType = String.format("%s%s %s", rareString, shipClass, shipDesignation);

        return String.format("%s, %s %s", shipName, aOrAn, shipType);
    }

    public static void generateHideoutDescription(TooltipMakerAPI info, BaseBountyIntel baseBountyIntel, Color highlightColor) {
        String isOrWas = isNull(baseBountyIntel.getFleet().getAI().getCurrentAssignmentType()) ? "was last seen " : "is ";
        SectorEntityToken hideout = baseBountyIntel.getStartingPoint();
        info.addPara(
                "The fleet " + isOrWas + "near " + hideout.getName() + " in the "
                        + hideout.getStarSystem().getName() + ".",
                10f, highlightColor, hideout.getName(), hideout.getStarSystem().getName());
    }
}