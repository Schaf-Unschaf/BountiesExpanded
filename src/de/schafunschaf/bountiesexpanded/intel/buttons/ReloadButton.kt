package de.schafunschaf.bountiesexpanded.intel.buttons

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.intel.BountyBoardIntel

class ReloadButton : DefaultButton() {

    override fun buttonPressConfirmed(ui: IntelUIAPI) {
        ui.updateUIForItem(BountyBoardIntel.getInstance())
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