package de.schafunschaf.bountiesexpanded.campaign.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.buttons.IntelButton

open class BaseIntel : BaseIntelPlugin() {

    override fun doesButtonHaveConfirmDialog(buttonId: Any): Boolean {
        return if (buttonId is IntelButton) {
            buttonId.doesButtonHaveConfirmDialog()
        } else false
    }

    override fun createConfirmationPrompt(buttonId: Any, prompt: TooltipMakerAPI) {
        if (buttonId is IntelButton) {
            buttonId.createConfirmationPrompt(prompt)
        }
    }

    override fun getConfirmText(buttonId: Any): String {
        return if (buttonId is IntelButton) {
            buttonId.confirmText
        } else "Confirm"
    }

    override fun getCancelText(buttonId: Any): String {
        return if (buttonId is IntelButton) {
            buttonId.cancelText
        } else "Cancel"
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI) {
        if (buttonId is IntelButton) {
            buttonId.buttonPressConfirmed(ui)
        }
        Global.getSector().playerFleet.fleetData.setSyncNeeded()
        Global.getSector().playerFleet.fleetData.syncMemberLists()
        ui.updateUIForItem(this)
    }

    override fun buttonPressCancelled(buttonId: Any, ui: IntelUIAPI) {
        if (buttonId is IntelButton) {
            buttonId.buttonPressCancelled(ui)
        }
        Global.getSector().playerFleet.fleetData.setSyncNeeded()
        Global.getSector().playerFleet.fleetData.syncMemberLists()
        ui.updateUIForItem(this)
    }
}