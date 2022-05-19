package de.schafunschaf.bountiesexpanded.intel.buttons

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class DefaultButton : IntelButton {

    override fun buttonPressCancelled(ui: IntelUIAPI) {}
    override fun buttonPressConfirmed(ui: IntelUIAPI) {}
    override fun createConfirmationPrompt(tooltip: TooltipMakerAPI) {}

    override fun doesButtonHaveConfirmDialog(): Boolean {
        return false
    }

    override val confirmText: String = ""
    override val cancelText: String = ""
    override val name: String = ""
    override val shortcut: Int = 0

    override fun addButton(tooltip: TooltipMakerAPI, width: Float, height: Float): ButtonAPI? {
        return null
    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
    }
}