package de.schafunschaf.bountiesexpanded.helper.mission;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class MissionTextUtils {
    public static void generateRetrievalTargetSection(TooltipMakerAPI info, float width, float opad, FleetMemberAPI retrievalTargetShip, FactionAPI targetedFaction) {
        generateRetrievalTargetSection(info, width, opad, Collections.singletonList(retrievalTargetShip), 1, 4, targetedFaction);
    }

    public static void generateRetrievalTargetSection(TooltipMakerAPI info, float width, float opad, List<FleetMemberAPI> shipList, int maxRows, int cols, FactionAPI targetedFaction) {
        int rows = (int) Math.ceil(shipList.size() / (float) cols);
        if (maxRows > 0 && rows > maxRows)
            rows = maxRows;

        float iconSize = width / cols;

        info.addShipList(cols, rows, iconSize, targetedFaction.getBaseUIColor(), shipList, opad);
    }

    public static void generateRetrievalConsequencesText(TooltipMakerAPI info, float padding, FleetMemberAPI ship, FactionAPI faction, PersonAPI person, float chanceForConsequences) {
        Color highlightColor = Misc.getHighlightColor();
        Color factionColor = faction.getBaseUIColor();
        Color[] possibleConsequencesColors = new Color[]{highlightColor, Misc.getTextColor(), factionColor, factionColor};
        String[] possibleConsequencesHighlights = {ship.getShipName(), getRevengeChanceText(chanceForConsequences), person.getNameString(), person.getFaction().getDisplayNameWithArticle()};
        String possibleConsequencesText = String.format("If you decide to keep the %s, %s %s will put YOUR head on the next bounty and also hurt your relations with %s.",
                (Object[]) possibleConsequencesHighlights);

        info.addPara(possibleConsequencesText, padding, possibleConsequencesColors, possibleConsequencesHighlights);
    }

    private static String getRevengeChanceText(float chanceToAnger) {
        if (chanceToAnger >= 0.75f)
            return "it is nearly guaranteed";
        if (chanceToAnger >= 0.50f)
            return "it is most likely";
        if (chanceToAnger >= 0.25f)
            return "there's a reasonable chance";

        return "there's a small chance";
    }
}
