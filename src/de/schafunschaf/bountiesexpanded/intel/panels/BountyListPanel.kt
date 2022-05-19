package de.schafunschaf.bountiesexpanded.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.util.ui.Constants
import de.schafunschaf.bountiesexpanded.util.ui.plugins.BorderPlugin

class BountyListPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    private val bountyEntryWidth = width - 10f
    private val bountyEntryHeight = 80f

    fun draw(): CustomPanelAPI {
        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading("Faction Bounty List", Alignment.MID, 0f)

        val bountyListPanel = panel.createCustomPanel(width, height - Constants.sectionHeaderHeight, null)
        val bountyListUIElement = bountyListPanel.createUIElement(width, height - Constants.sectionHeaderHeight, true)

        for (i in 1..13) {
            drawBountyEntry(bountyListUIElement, i)
        }

        bountyListPanel.addUIElement(bountyListUIElement).setXAlignOffset(-5f)
        uiElement.addCustom(bountyListPanel, 3f)
        customPanel.addUIElement(uiElement)
        return customPanel
    }

    private fun drawBountyEntry(tooltip: TooltipMakerAPI, number: Int) {
        val color = if (number == 2)
            Misc.getHighlightColor()
        else
            Misc.getDarkPlayerColor()

        val bountyPanel = panel.createCustomPanel(bountyEntryWidth, bountyEntryHeight, BorderPlugin(1f, color))
        val bountyUIElement = bountyPanel.createUIElement(bountyEntryWidth, bountyEntryHeight, false)
        bountyUIElement.addSectionHeading("Bounty entry $number", Alignment.MID, 3f).position.setXAlignOffset(-5f)
        bountyUIElement.addPara("Bounty short description", 3f)
        bountyPanel.addUIElement(bountyUIElement).inTL(5f, 0f)
        tooltip.addCustom(bountyPanel, 6f)
        tooltip.addSpacer(3f)
    }

}