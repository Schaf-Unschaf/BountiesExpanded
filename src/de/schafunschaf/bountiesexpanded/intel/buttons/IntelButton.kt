package de.schafunschaf.bountiesexpanded.intel.buttons

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

interface IntelButton {

    fun buttonPressCancelled(ui: IntelUIAPI)
    fun buttonPressConfirmed(ui: IntelUIAPI)
    fun createConfirmationPrompt(tooltip: TooltipMakerAPI)
    fun doesButtonHaveConfirmDialog(): Boolean
    val confirmText: String
    val cancelText: String
    val name: String
    val shortcut: Int
    fun addButton(tooltip: TooltipMakerAPI, width: Float, height: Float): ButtonAPI?
    fun addTooltip(tooltip: TooltipMakerAPI)
}