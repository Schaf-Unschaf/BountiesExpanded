package de.schafunschaf.bountiesexpanded.helper.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class DescriptionUtils {
    public static void createShipListForIntel(TooltipMakerAPI info, float width, float padding, CampaignFleetAPI fleet, int maxShipsToDisplay, int maxRows, boolean showShipsRemaining) {
        Random random = new Random(fleet.getCommander().getNameString().hashCode() * 170000L);
        List<FleetMemberAPI> fleetMemberList;
        if (Settings.isDebugActive()) {
            maxRows = 0;
            fleetMemberList = FleetGenerator.createCompleteCopyForIntel(fleet);
        } else
            fleetMemberList = FleetGenerator.createCopyForIntel(fleet, maxShipsToDisplay, random);

        fleetMemberList = orderListBySize(fleetMemberList);

        int cols = 7;
        int rows = (int) Math.ceil(fleetMemberList.size() / (float) cols);
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

        info.addShipList(cols, rows, iconSize, fleet.getFaction().getBaseUIColor(), fleetMemberList, padding);

        if (showShipsRemaining && !Settings.isDebugActive()) {
            int num = fleet.getNumShips() - maxShipsToDisplay;
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

    public static List<FleetMemberAPI> orderListBySize(@NotNull List<FleetMemberAPI> fleetMemberList) {
        if (isNullOrEmpty(fleetMemberList))
            return new ArrayList<>();

        FleetMemberAPI flagship = null;
        List<FleetMemberAPI> sortedList = new ArrayList<>();
        List<FleetMemberAPI> frigateList = new ArrayList<>();
        List<FleetMemberAPI> destroyerList = new ArrayList<>();
        List<FleetMemberAPI> cruiserList = new ArrayList<>();
        List<FleetMemberAPI> capitalList = new ArrayList<>();

        for (FleetMemberAPI fleetMemberAPI : fleetMemberList) {
            if (fleetMemberAPI.isFlagship()) {
                flagship = fleetMemberAPI;
                continue;
            }
            switch (fleetMemberAPI.getHullSpec().getHullSize()) {
                case FRIGATE:
                    frigateList.add(fleetMemberAPI);
                    break;
                case DESTROYER:
                    destroyerList.add(fleetMemberAPI);
                    break;
                case CRUISER:
                    cruiserList.add(fleetMemberAPI);
                    break;
                case CAPITAL_SHIP:
                    capitalList.add(fleetMemberAPI);
                    break;
            }
        }
        sortedList.add(flagship);
        sortedList.addAll(capitalList);
        sortedList.addAll(cruiserList);
        sortedList.addAll(destroyerList);
        sortedList.addAll(frigateList);
        sortedList.remove(null);

        return sortedList;
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

        int fleetSize = fleet.getNumShips();
        FleetMemberAPI flagship = fleet.getFlagship();
        PersonAPI commander = fleet.getCommander();
        String shipType = flagship.getHullSpec().getHullNameWithDashClass() + " " + flagship.getHullSpec().getDesignation().toLowerCase();
        String hisOrHer = person.getHisOrHer();
        String fleetDesc;

        if (fleetSize <= 10) fleetDesc = "small fleet";
        else if (fleetSize <= 20) fleetDesc = "medium-sized fleet";
        else if (fleetSize <= 30) fleetDesc = "large fleet";
        else if (fleetSize <= 40) fleetDesc = "very large fleet";
        else if (fleetSize <= 50) fleetDesc = "gigantic fleet";
        else fleetDesc = "freaking armada";

        info.addPara("%s is in command of a %s and personally commands the %s, a %s, as %s flagship.", padding, commander.getFaction().getBaseUIColor(),
                commander.getFaction().getRank(commander.getRankId()) + " " + person.getName().getFullName(), fleetDesc, flagship.getShipName(), shipType, hisOrHer);
    }

    public static void addRepMessage(TooltipMakerAPI info, float width, float pad, FactionAPI faction, ReputationActionResponsePlugin.ReputationAdjustmentResult rep, boolean showRepBar) {
        if (isNull(info) || isNull(faction) || isNull(rep))
            return;

        String factionName = faction.getDisplayName();
        float delta = rep.delta;
        int deltaInt = Math.round(Math.abs(delta) * 100f);
        FactionAPI player = Global.getSector().getPlayerFaction();
        int repInt = RepLevel.getRepInt(player.getRelationship(faction.getId()));
        RepLevel repLevel = player.getRelationshipLevel(faction.getId());
        Color factionColor = faction.getBaseUIColor();
        Color deltaColor = Misc.getPositiveHighlightColor();
        Color relationColor = faction.getRelColor(player.getId());

        String deltaString = "improved by " + deltaInt;
        String standing = "" + repInt + "/100" + " (" + repLevel.getDisplayName().toLowerCase() + ")";

        if (delta < 0) {
            deltaColor = Misc.getNegativeHighlightColor();
            deltaString = "reduced by " + deltaInt;
        } else if (delta == 0) {
            deltaString = "not affected";
            deltaColor = Misc.getTextColor();
        }

        Color[] highlightColors = {factionColor, deltaColor, relationColor};

        if (showRepBar) {
            info.addPara("Relationship with %s %s", pad, highlightColors, factionName, deltaString, standing);
            info.addRelationshipBar(faction, width, 3f);
        } else
            info.addPara("Relationship with %s %s, currently at %s", pad, highlightColors, factionName, deltaString, standing);
    }
}