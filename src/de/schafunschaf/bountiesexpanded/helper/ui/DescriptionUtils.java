package de.schafunschaf.bountiesexpanded.helper.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class DescriptionUtils {
    public static void createShipListForIntel(TooltipMakerAPI info, float width, float padding, CampaignFleetAPI fleet, int maxShipsToDisplay, boolean showShipsRemaining) {
        Random random = new Random(fleet.getCommander().getNameString().hashCode() * 170000L);
        List<FleetMemberAPI> fleetMemberList;
        if (Settings.isDebugActive())
            fleetMemberList = FleetGenerator.createCompleteCopyForIntel(fleet);
        else
            fleetMemberList = FleetGenerator.createCopyForIntel(fleet, maxShipsToDisplay, random);

        int cols = 7;
        int rows = (int) Math.ceil(fleetMemberList.size() / (float) cols);
        float iconSize = width / cols;
        if (Settings.isDebugActive()) {
            int enemyFP = fleet.getFleetPoints();
            int playerFP = Global.getSector().getPlayerFleet().getFleetPoints();
            info.addSectionHeading("DEBUG INFO", Alignment.MID, padding);
            info.addPara("Enemy  FP -> " + enemyFP, padding, fleet.getFaction().getBaseUIColor(), String.valueOf(enemyFP));
            info.addPara("Player FP -> " + playerFP, padding, Misc.getHighlightColor(), String.valueOf(playerFP));
        }

        info.addShipList(cols, rows, iconSize, Color.BLACK, fleetMemberList, padding);

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
}