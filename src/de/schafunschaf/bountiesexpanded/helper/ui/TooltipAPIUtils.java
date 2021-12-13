package de.schafunschaf.bountiesexpanded.helper.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

import static de.schafunschaf.bountiesexpanded.helper.text.DescriptionUtils.DEFAULT_IMAGE_HEIGHT;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class TooltipAPIUtils {
    public static void addRepMessage(TooltipMakerAPI info, float width, float pad, FactionAPI faction, ReputationActionResponsePlugin.ReputationAdjustmentResult repChange) {
        if (isNull(info) || isNull(faction) || isNull(repChange))
            return;

        String factionName = faction.getDisplayName();
        float delta = repChange.delta;
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
        info.addPara("Relationship with %s %s, currently at %s", pad, highlightColors, factionName, deltaString, standing);
    }

    public static void addFactionRepLastChangedBox(TooltipMakerAPI info, float width, float padding, FactionAPI faction) {
        if (isNull(info) || isNull(faction))
            return;

        float innerPadding = 5f;
        float flagPadding = 10f;
        float sizeDiv = 6f;
        float flagWidth = 410f / sizeDiv;
        float flagHeight = 256f / sizeDiv;

        info.addImage(faction.getLogo(), flagWidth, flagHeight, padding);
        info.getPrev().getPosition().setXAlignOffset(innerPadding);

        info.addPara("%s", 0f, faction.getBaseUIColor(), faction.getDisplayName());
        info.getPrev().getPosition().setXAlignOffset(flagWidth + flagPadding);
        info.getPrev().getPosition().setYAlignOffset(flagHeight);

        info.addRelationshipBar(faction, width - flagWidth - flagPadding - innerPadding * 2, 8f);
        info.addSpacer(0f);
        info.getPrev().getPosition().setXAlignOffset(-(flagWidth + flagPadding + innerPadding));
    }

    public static void addFactionRepLastChangedBox(TooltipMakerAPI info, float width, float pad, FactionAPI faction, float repChange) {
        if (isNull(info) || isNull(faction))
            return;

        float innerPadding = 5f;
        float flagPadding = 10f;
        float sizeDiv = 8f;
        float flagWidth = 410f / sizeDiv;
        float flagHeight = 256f / sizeDiv;

        String factionName = faction.getDisplayName();
        int deltaInt = Math.round(Math.abs(repChange) * 100f);
        FactionAPI player = Global.getSector().getPlayerFaction();
        Color factionColor = faction.getBaseUIColor();
        Color deltaColor = Misc.getPositiveHighlightColor();
        Color relationColor = faction.getRelColor(player.getId());

        String deltaString = "improved by " + deltaInt;

        if (repChange < 0) {
            deltaColor = Misc.getNegativeHighlightColor();
            deltaString = "reduced by " + deltaInt;
        } else if (repChange == 0) {
            deltaString = "not affected";
            deltaColor = Misc.getTextColor();
        }

        Color[] highlightColors = {deltaColor, relationColor};

        info.addImage(faction.getLogo(), flagWidth, flagHeight, pad);
        info.getPrev().getPosition().setXAlignOffset(innerPadding);

        info.addPara("%s", 0f, factionColor, factionName);
        info.getPrev().getPosition().setXAlignOffset(flagWidth + flagPadding);
        info.getPrev().getPosition().setYAlignOffset(flagHeight);

        info.addPara("Relationship %s", 3f, highlightColors, deltaString);
        info.addSpacer(0f);
        info.getPrev().getPosition().setXAlignOffset(-(flagWidth + flagPadding + innerPadding));
    }

    public static void addRepBarWithChange(TooltipMakerAPI info, float width, float pad, float repBarValue, float repChange) {
        int deltaInt = Math.round(Math.abs(repChange) * 100f);
        Color deltaColor = Misc.getPositiveHighlightColor();
        String deltaString = "improved by " + deltaInt;

        if (repChange < 0) {
            deltaColor = Misc.getNegativeHighlightColor();
            deltaString = "reduced by " + deltaInt;
        } else if (repChange == 0) {
            deltaString = "not affected";
            deltaColor = Misc.getTextColor();
        }

        float textPad = 8f;

        info.addRelationshipBar(repBarValue, width, pad);
        float barWidth = info.getPrev().getPosition().getWidth();
        info.addPara(deltaString, textPad, deltaColor, deltaString);
        info.getPrev().getPosition().setXAlignOffset(barWidth / 3 + pad);

        info.addSpacer(0f);
        info.getPrev().getPosition().setXAlignOffset(-(barWidth / 3 + pad));
    }

    public static void addFactionFlagsWithRep(TooltipMakerAPI info, float width, float pad, float itemPad, FactionAPI leftFaction, FactionAPI rightFaction) {
        addCustomImagesWithRepBar(info, width, pad, itemPad, leftFaction, leftFaction.getLogo(), rightFaction, rightFaction.getLogo());
    }

    public static void addFactionFlagsWithRepChange(TooltipMakerAPI info, float width, float pad, float itemPad, FactionAPI leftFaction, float leftRepChange, FactionAPI rightFaction, float rightRepChange) {
        addCustomImagesWithRepChange(info, width, pad, itemPad, leftFaction, leftFaction.getLogo(), leftRepChange, rightFaction, rightFaction.getLogo(), rightRepChange);
    }

    public static void addPersonWithFactionRepBar(TooltipMakerAPI info, float width, float pad, float itemPad, PersonAPI person) {
        addCustomImagesWithSingleRepBar(info, width, pad, itemPad, person.getPortraitSprite(), person.getFaction().getLogo(), person.getFaction().getRelToPlayer().getRel());
    }

    public static void addPersonWithFactionRepBarAndChange(TooltipMakerAPI info, float width, float pad, float itemPad, PersonAPI person, float repChange) {
        addCustomImagesWithSingleRepBarAndChange(info, width, pad, itemPad, person.getPortraitSprite(), person.getFaction().getLogo(), person.getFaction().getRelToPlayer().getRel(), repChange);
    }

    public static void addCustomImagesWithSingleRepBar(TooltipMakerAPI info, float width, float pad, float itemPad, String leftImage, String rightImage, float repBarValue) {
        info.addImages(width, DEFAULT_IMAGE_HEIGHT, pad, itemPad, leftImage, rightImage);
        info.addRelationshipBar(repBarValue, width, pad);
    }

    public static void addCustomImagesWithSingleRepBarAndChange(TooltipMakerAPI info, float width, float pad, float itemPad, String leftImage, String rightImage, float repBarValue, float repChange) {
        info.addImages(width, DEFAULT_IMAGE_HEIGHT, pad, itemPad, leftImage, rightImage);
        addRepBarWithChange(info, width, pad, repBarValue, repChange);
    }

    public static void addCustomImagesWithRepBar(TooltipMakerAPI info, float width, float pad, float itemPad, FactionAPI leftFaction, String leftImage, FactionAPI rightFaction, String rightImage) {
        info.addImages(width, DEFAULT_IMAGE_HEIGHT, pad, itemPad, leftImage, rightImage);

        info.addRelationshipBar(leftFaction, width / 2 - itemPad, 5f);

        info.addRelationshipBar(rightFaction, width / 2 - itemPad + 2f, 5f);
        info.getPrev().getPosition().setXAlignOffset(width / 2 + itemPad - 2f);
        info.getPrev().getPosition().setYAlignOffset(info.getPrev().getPosition().getHeight());

        info.addSpacer(0f);
        info.getPrev().getPosition().setXAlignOffset(-(width / 2 + itemPad - 2f));
        info.getPrev().getPosition().setYAlignOffset(-info.getPrev().getPosition().getHeight());
    }

    public static void addCustomImagesWithRepChange(TooltipMakerAPI info, float width, float pad, float itemPad, FactionAPI leftFaction, String leftImage, float leftRepChange, FactionAPI rightFaction, String rightImage, float rightRepChange) {
        float[] repDelta = {leftRepChange, rightRepChange};
        int[] deltaInt = {Math.round(Math.abs(leftRepChange) * 100f), Math.round(Math.abs(rightRepChange) * 100f)};
        Color[] deltaColor = {Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor()};
        String[] deltaString = {"improved by " + deltaInt[0], "improved by " + deltaInt[1]};

        for (int i = 0; i < repDelta.length; i++) {
            if (repDelta[i] < 0) {
                deltaColor[i] = Misc.getNegativeHighlightColor();
                deltaString[i] = "reduced by " + deltaInt[i];
            } else if (repDelta[i] == 0) {
                deltaString[i] = "not affected";
                deltaColor[i] = Misc.getTextColor();
            }
        }

        float textPad = 8f;

        info.addImages(width, DEFAULT_IMAGE_HEIGHT, pad, itemPad, leftImage, rightImage);

        info.addRelationshipBar(leftFaction, width / 2 - itemPad, 5f);
        float leftBarHeight = info.getPrev().getPosition().getHeight();
        float leftBarWidth = info.getPrev().getPosition().getWidth();
        info.addPara(deltaString[0], textPad, deltaColor[0], deltaString[0]);
        float leftTextHeight = info.getPrev().getPosition().getHeight();
        info.getPrev().getPosition().setXAlignOffset(leftBarWidth / 7);

        info.addRelationshipBar(rightFaction, width / 2 - itemPad + 2f, 5f);
        float rightBarWidth = info.getPrev().getPosition().getWidth();
        info.getPrev().getPosition().setXAlignOffset(-(leftBarWidth / 7) + width / 2 + itemPad - 2f);
        info.getPrev().getPosition().setYAlignOffset(leftBarHeight + leftTextHeight + textPad);
        info.addPara(deltaString[1], textPad, deltaColor[1], deltaString[1]);
        info.getPrev().getPosition().setXAlignOffset(rightBarWidth / 6);

        info.addSpacer(0f);
        info.getPrev().getPosition().setXAlignOffset(-(rightBarWidth / 6) - width / 2 - itemPad + 2f);
    }
}
