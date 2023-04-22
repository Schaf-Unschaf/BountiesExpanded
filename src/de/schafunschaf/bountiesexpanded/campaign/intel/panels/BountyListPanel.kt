package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.bountylist.PostedBountyButton
import de.schafunschaf.bountiesexpanded.util.ui.SECTION_HEADER_HEIGHT
import de.schafunschaf.bountiesexpanded.util.ui.plugins.BorderPlugin

class BountyListPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    private val bountyEntryWidth = width / 2 - 10f
    private val bountyEntryHeight = 250f

    fun draw(): CustomPanelAPI {
        val selectedFaction = MainPanel.selectedFaction
        val factionName = if (selectedFaction == null) "Empty" else selectedFaction.displayNameWithArticleWithoutArticle

        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading("$factionName Bounty List", Alignment.MID, 0f)

        val bountyListPanel = panel.createCustomPanel(width, height - SECTION_HEADER_HEIGHT, null)
        val bountyListUIElement = bountyListPanel.createUIElement(width, height - SECTION_HEADER_HEIGHT, true)

        BountyManager.getInstance().postedBounties.filter { it.offeringFaction == selectedFaction }
            .forEach { drawBountyEntry(bountyListUIElement, it) }

        bountyListPanel.addUIElement(bountyListUIElement).setXAlignOffset(-5f)
        uiElement.addCustom(bountyListPanel, 3f)
        customPanel.addUIElement(uiElement)
        return customPanel
    }

    private var isFirst: Boolean = true
    private var prevElement: UIComponentAPI? = null
    private fun drawBountyEntry(tooltip: TooltipMakerAPI, postedBounty: PostedBounty) {
        val isSelected = MainPanel.selectedBounty == postedBounty
        val borderColor = if (isSelected) Misc.getTextColor() else Misc.getDarkPlayerColor()

        val bountyPanel = panel.createCustomPanel(
            bountyEntryWidth,
            bountyEntryHeight,
            BorderPlugin(1f, borderColor)
        )
        val bountyUIElement = bountyPanel.createUIElement(bountyEntryWidth, bountyEntryHeight, false)

        PostedBountyButton(postedBounty).addButton(bountyUIElement, bountyEntryWidth, bountyEntryHeight)
        bountyUIElement.addSectionHeading(
            postedBounty.title,
            if (isSelected) Misc.getHighlightColor() else Misc.getTextColor(),
            Misc.getDarkPlayerColor(),
            Alignment.MID,
            -bountyEntryHeight
        ).position.setXAlignOffset(-10f)

        bountyUIElement.addImage(postedBounty.targetedFaction.logo, 24f, -16f)
        bountyUIElement.prev.position.setXAlignOffset(-8f)
        bountyUIElement.addImage(postedBounty.targetedFaction.logo, 24f, -15f)
        bountyUIElement.prev.position.setXAlignOffset(bountyEntryWidth - 28f)

        bountyUIElement.addSpacer(1f).position.setXAlignOffset(-bountyEntryWidth + 8f + 28f)
        postedBounty.createPreviewPosting(
            bountyUIElement,
            bountyEntryWidth,
            bountyEntryHeight - SECTION_HEADER_HEIGHT
        )

        bountyPanel.addUIElement(bountyUIElement).inTL(5f, 0f)
        if (isFirst) {
            tooltip.addCustom(
                bountyPanel,
                6f
            ).position.setXAlignOffset(if (prevElement == null) 5f else -bountyEntryWidth - 9f)
        } else {
            prevElement = tooltip.prev
            tooltip.addCustom(bountyPanel, -bountyEntryHeight).position.setXAlignOffset(bountyEntryWidth + 8f)
            tooltip.addSpacer(3f)
        }
        isFirst = !isFirst
    }

}