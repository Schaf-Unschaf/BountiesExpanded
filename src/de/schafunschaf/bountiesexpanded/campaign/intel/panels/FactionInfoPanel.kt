package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.helper.ProgressBar
import de.schafunschaf.bountiesexpanded.util.ui.plugins.BorderPlugin

class FactionInfoPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    fun draw(): CustomPanelAPI {
        val selectedFaction = MainPanel.selectedFaction
        val headerText =
            if (selectedFaction == null) "No Faction Selected" else "${selectedFaction.displayNameWithArticleWithoutArticle} Info"

        val customPanel = panel.createCustomPanel(width, height, BorderPlugin(1f, Misc.getHighlightColor()))
        val uiElement = customPanel.createUIElement(width, height, false)

        uiElement.addSectionHeading(headerText, Alignment.MID, 0f)

        if (selectedFaction != null) uiElement.addRelationshipBar(selectedFaction, 100f, 3f)
        ProgressBar.addBarLTR(uiElement, "", null, null, 100f, 20f, 1f, 1f, 25f, 3f, null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBasePlayerColor())

        customPanel.addUIElement(uiElement).inTL(0f, 0f)
        return customPanel
    }

}