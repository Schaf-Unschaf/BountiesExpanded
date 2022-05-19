package de.schafunschaf.bountiesexpanded.intel.panels

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.util.ui.Constants
import de.schafunschaf.bountiesexpanded.util.ui.plugins.BorderPlugin

class FactionListPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    private val flagElementSize = 60f
    private val flagSize = 50f

    fun draw(): CustomPanelAPI {

        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading("Factions", Alignment.MID, 0f)

        val flagListPanel = panel.createCustomPanel(width - 5, height - Constants.sectionHeaderHeight, null)
        val flagListUIElement = flagListPanel.createUIElement(width - 5, height - Constants.sectionHeaderHeight, true)
        flagListUIElement.addSpacer(0f).position.setXAlignOffset(0f)
        var isFirst = false
        Global.getSector().allFactions.forEach { faction ->
            if (!isFirst)
                flagListUIElement.addSpacer(3f).position.setXAlignOffset(0f)
            isFirst = true

            drawFlag(flagListUIElement, faction)
        }

        flagListPanel.addUIElement(flagListUIElement).inTL(0f, 3f)
        uiElement.addCustom(flagListPanel, 0f)
        customPanel.addUIElement(uiElement).inTL(0f, 0f)

        return customPanel
    }

    private fun drawFlag(tooltip: TooltipMakerAPI, faction: FactionAPI) {
        val flagPanel = panel.createCustomPanel(flagElementSize, flagElementSize, if (faction.id == "remnant") BorderPlugin(1f, Misc.getHighlightColor()) else null)
        val flagUIElement = flagPanel.createUIElement(flagElementSize, flagElementSize, false)
        flagUIElement.addImage(faction.crest, flagSize, 0f)
        flagUIElement.prev.position.setXAlignOffset(10f).setYAlignOffset(-5f)

        flagPanel.addUIElement(flagUIElement).inTL(-5f, 0f)
        tooltip.addCustom(flagPanel, 0f)
        tooltip.addSpacer(3f)
    }
}