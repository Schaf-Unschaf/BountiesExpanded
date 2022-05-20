package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI

class HeaderPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    fun draw(): CustomPanelAPI {
        val customPanel = panel.createCustomPanel(width, height, null)
        val headerElement = customPanel.createUIElement(width, height, false)

        headerElement.addSectionHeading("Sector Bounty Board", Alignment.MID, 0f)
        headerElement.addPara("Welcome to the Sector Bounty Board!", 3f)

        customPanel.addUIElement(headerElement).inTL(0f, 0f)
        return customPanel
    }

}