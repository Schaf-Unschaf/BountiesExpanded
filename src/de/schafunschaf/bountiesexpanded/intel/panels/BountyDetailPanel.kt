package de.schafunschaf.bountiesexpanded.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import de.schafunschaf.bountiesexpanded.intel.buttons.ReloadButton

class BountyDetailPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    fun draw(): CustomPanelAPI {
        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading("description bla bla", Alignment.MID, 0f)
        uiElement.addPara("Detailed long description and all that fluff", 3f)
        ReloadButton().addButton(uiElement, 50f, 20f)

        customPanel.addUIElement(uiElement)
        return customPanel
    }

}