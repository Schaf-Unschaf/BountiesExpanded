package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.factionlist.FactionButton
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
        BountyManager.getInstance().postedBounties.map(PostedBounty::offeringFaction).distinct()
            .forEach {
                if (!isFirst)
                    flagListUIElement.addSpacer(3f).position.setXAlignOffset(0f)
                isFirst = true

                drawFlag(flagListUIElement, it)
            }

        flagListPanel.addUIElement(flagListUIElement).inTL(0f, 3f)
        uiElement.addCustom(flagListPanel, 0f)
        customPanel.addUIElement(uiElement).inTL(0f, 0f)

        return customPanel
    }

    private fun drawFlag(tooltip: TooltipMakerAPI, faction: FactionAPI) {
        val flagPanel = panel.createCustomPanel(
            flagElementSize,
            flagElementSize,
            if (faction == MainPanel.selectedFaction) BorderPlugin(1f, Misc.getHighlightColor()) else null
        )
        val flagUIElement = flagPanel.createUIElement(flagElementSize, flagElementSize, false)

        flagUIElement.addSpacer(0f).position.setXAlignOffset(10f).setYAlignOffset(-5f)
        FactionButton(faction).addButton(flagUIElement, flagSize, flagSize)

        flagPanel.addUIElement(flagUIElement).inTL(-5f, 0f)
        tooltip.addCustom(flagPanel, 0f)
        tooltip.addSpacer(3f)
    }
}