package de.schafunschaf.bountiesexpanded.campaign.intel.buttons

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities.PirateBounty

class ReloadButton : DefaultButton() {

    override fun buttonPressConfirmed(ui: IntelUIAPI) {
        val postedBounties = BountyManager.getInstance().postedBounties
        postedBounties.clear()

        for (i in 1..20) {
            val pirateBounty = PirateBounty()
            postedBounties.add(pirateBounty)
        }
    }

    override fun doesButtonHaveConfirmDialog(): Boolean {
        return false
    }

    override val name: String
        get() = "Reload"

    override fun addButton(tooltip: TooltipMakerAPI, width: Float, height: Float): ButtonAPI? {
        return tooltip.addButton(name, this, width, height, 0f)
    }
}