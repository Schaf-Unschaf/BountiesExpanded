package de.schafunschaf.bountiesexpanded.campaign.intel.buttons.factionlist

import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.DefaultButton
import de.schafunschaf.bountiesexpanded.campaign.intel.panels.MainPanel
import de.schafunschaf.bountiesexpanded.ids.BE_Colors.TRANSPARENT

class FactionButton(val faction: FactionAPI) : DefaultButton() {

    override fun buttonPressConfirmed(ui: IntelUIAPI) {
        MainPanel.selectedFaction = faction
        MainPanel.selectedBounty = null
    }

    override fun doesButtonHaveConfirmDialog(): Boolean {
        return false
    }

    override val name: String = ""

    override fun addButton(tooltip: TooltipMakerAPI, width: Float, height: Float): ButtonAPI? {
        val checkbox = tooltip.addAreaCheckbox("", this, TRANSPARENT, TRANSPARENT, TRANSPARENT, width, height, 0f)
        tooltip.addImage(faction.crest, width, -width)

        return checkbox
    }
}