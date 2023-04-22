package de.schafunschaf.bountiesexpanded.campaign.intel.panels

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.ReloadButton

class BountyDetailPanel(private val panel: CustomPanelAPI, val width: Float, val height: Float) {

    fun draw(): CustomPanelAPI {
        val customPanel = panel.createCustomPanel(width, height, null)
        val uiElement = customPanel.createUIElement(width, height, true)

        val bounty = MainPanel.selectedBounty
        uiElement.addSectionHeading(
            if (bounty == null) "No Bounty selected" else "${bounty.title} - ${bounty.targetedPerson.nameString}",
            Alignment.MID,
            0f
        )

        bounty?.createDetailedPosting(uiElement, width, height)

        uiElement.addSectionHeading("", Alignment.MID, 0f).position.setSize(width, 1f).inBL(0f, 0f)
        ReloadButton().addButton(uiElement, 50f, 20f)?.position?.setYAlignOffset(-22f)

        customPanel.addUIElement(uiElement)
        return customPanel
    }
}