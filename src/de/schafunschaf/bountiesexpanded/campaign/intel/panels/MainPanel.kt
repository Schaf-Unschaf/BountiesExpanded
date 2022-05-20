package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.CustomPanelAPI

class MainPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    private val headerHeight = 100f
    private val remainingHeight = height - headerHeight
    private val padding = 5f
    private val factionListPanelWidth = 70f
    private val bountyListPanelWidth = 250f - padding
    private val detailPanelWidth = width - factionListPanelWidth - bountyListPanelWidth - padding * 2

    fun drawPanels() {
        val headerPanel = HeaderPanel(panel, width, headerHeight).draw()
        val factionListPanel = FactionListPanel(panel, factionListPanelWidth, remainingHeight).draw()
        val bountyListPanel = BountyListPanel(panel, bountyListPanelWidth, remainingHeight).draw()
        val bountyDetailPanel = BountyDetailPanel(panel, detailPanelWidth, remainingHeight).draw()

        val uiElement = panel.createUIElement(width, headerHeight, false)

        uiElement.addCustom(headerPanel, 0f)
        uiElement.addCustom(factionListPanel, 0f)
        uiElement.addCustom(bountyListPanel, 0f).position.rightOfTop(factionListPanel, padding)
        uiElement.addCustom(bountyDetailPanel, 0f).position.rightOfTop(bountyListPanel, padding)

        panel.addUIElement(uiElement).inTL(-5f, 0f)
    }
}