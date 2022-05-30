package de.schafunschaf.bountiesexpanded.campaign.intel.buttons.bountylist

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.DefaultButton
import de.schafunschaf.bountiesexpanded.campaign.intel.panels.MainPanel
import de.schafunschaf.bountiesexpanded.ids.BE_Colors.TRANSPARENT

class PostedBountyButton(val postedBounty: PostedBounty) : DefaultButton() {

    override fun buttonPressConfirmed(ui: IntelUIAPI) {
        MainPanel.selectedBounty = postedBounty
    }

    override fun doesButtonHaveConfirmDialog(): Boolean {
        return false
    }

    override val name: String = ""

    override fun addButton(tooltip: TooltipMakerAPI, width: Float, height: Float): ButtonAPI? {
        return tooltip.addAreaCheckbox("", this, TRANSPARENT, TRANSPARENT, TRANSPARENT, width, height, 0f)
    }
}