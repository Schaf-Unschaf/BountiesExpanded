package de.schafunschaf.bountiesexpanded.helper

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.util.MathUtils
import java.awt.Color

object ProgressBar {

    fun addBarLTR(
        tooltip: TooltipMakerAPI, text: String, textAlign: Alignment?, font: String?,
        width: Float, height: Float, borderSize: Float, borderMargin: Float, progress: Float, pad: Float,
        textColor: Color?, borderColor: Color, bgColor: Color, barColor: Color
    ): UIComponentAPI {
        var textAlign = textAlign
        var textColor = textColor
        if (textAlign == null) {
            textAlign = Alignment.MID
        }
        if (textColor == null) {
            textColor = Misc.getTextColor()
        }
        val customPanel = Global.getSettings().createCustom(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)
        if (font != null && font.isNotEmpty()) {
            uiElement.setParaFont(font)
        }
        val maxBarWidth = width - borderSize * 2 - borderMargin * 2
        val progressBarWidth = MathUtils.clamp(maxBarWidth / 100 * progress, 0f, maxBarWidth)
        val barText = tooltip.shortenString(text, maxBarWidth)

        // background
        uiElement.addSectionHeading("", Color.BLACK, bgColor, Alignment.MID, 0f).position
            .setSize(width, height)
            .inTL(0f, 0f)
        // bar
        uiElement.addSectionHeading("", Color.BLACK, barColor, Alignment.MID, 0f).position
            .setSize(progressBarWidth, height - borderSize * 2 - borderMargin * 2)
            .inTL(borderMargin + borderSize, borderMargin + borderSize)
        // text
        uiElement.addPara(barText, textColor, 0f).setAlignment(textAlign)
        uiElement.prev.position.setSize(maxBarWidth, height - borderSize * 2 - borderMargin * 2)
            .inTL(borderMargin + borderSize, borderMargin + borderSize)
        // top border
        uiElement.addSectionHeading("", Color.BLACK, borderColor, Alignment.MID, 0f).position
            .setSize(width, borderSize)
            .inTL(0f, 0f)
        // left border
        uiElement.addSectionHeading("", Color.BLACK, borderColor, Alignment.MID, 0f).position
            .setSize(borderSize, height - borderSize * 2)
            .inTL(0f, borderSize)
        // right border
        uiElement.addSectionHeading("", Color.BLACK, borderColor, Alignment.MID, 0f).position
            .setSize(borderSize, height - borderSize * 2)
            .inTL(width - borderSize, borderSize)
        // bot border
        uiElement.addSectionHeading("", Color.BLACK, borderColor, Alignment.MID, 0f).position
            .setSize(width, borderSize)
            .inTL(0f, height - borderSize)
        customPanel.addUIElement(uiElement).inTL(0f, 0f)
        tooltip.addCustom(customPanel, pad)
        return customPanel
    }
}