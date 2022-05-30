package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.bountylist.PostedBountyButton
import de.schafunschaf.bountiesexpanded.util.ui.Constants
import de.schafunschaf.bountiesexpanded.util.ui.plugins.BorderPlugin

class BountyListPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    private val bountyEntryWidth = width - 10f
    private val bountyEntryHeight = 90f

    fun draw(): CustomPanelAPI {
        val selectedFaction = MainPanel.selectedFaction
        val factionName = if (selectedFaction == null) "Empty" else selectedFaction.displayNameWithArticleWithoutArticle

        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading("$factionName Bounty List", Alignment.MID, 0f)

        val bountyListPanel = panel.createCustomPanel(width, height - Constants.sectionHeaderHeight, null)
        val bountyListUIElement = bountyListPanel.createUIElement(width, height - Constants.sectionHeaderHeight, true)

        BountyManager.getInstance().postedBounties.filter { it.offeringFaction == selectedFaction }
            .forEach { drawBountyEntry(bountyListUIElement, it) }

        bountyListPanel.addUIElement(bountyListUIElement).setXAlignOffset(-5f)
        uiElement.addCustom(bountyListPanel, 3f)
        customPanel.addUIElement(uiElement)
        return customPanel
    }

    private fun drawBountyEntry(tooltip: TooltipMakerAPI, postedBounty: PostedBounty) {
        val color =
            if (MainPanel.selectedBounty == postedBounty) Misc.getHighlightColor() else Misc.getDarkPlayerColor()

        val bountyPanel = panel.createCustomPanel(bountyEntryWidth, bountyEntryHeight, BorderPlugin(1f, color))
        val bountyUIElement = bountyPanel.createUIElement(bountyEntryWidth, bountyEntryHeight, false)

        PostedBountyButton(postedBounty).addButton(bountyUIElement, bountyEntryWidth, bountyEntryHeight)
        bountyUIElement.addSectionHeading(
            postedBounty.title,
            Alignment.MID,
            -bountyEntryHeight
        ).position.setXAlignOffset(-10f)
        postedBounty.createPreview(
            bountyUIElement,
            bountyEntryWidth,
            bountyEntryHeight - Constants.sectionHeaderHeight
        )
        bountyPanel.addUIElement(bountyUIElement).inTL(5f, 0f)
        tooltip.addCustom(bountyPanel, 6f)
        tooltip.addSpacer(3f)
    }

}