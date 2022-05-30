package de.schafunschaf.bountiesexpanded.campaign.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.panels.MainPanel
import java.awt.Color

class BountyBoardIntel : BaseIntel() {

    companion object {

        @JvmStatic
        fun getInstance(): BountyBoardIntel {
            val intelManager = Global.getSector().intelManager

            return if (intelManager.hasIntelOfClass(BountyBoardIntel::class.java))
                intelManager.getIntel(BountyBoardIntel::class.java).first() as BountyBoardIntel
            else
                BountyBoardIntel()
        }
    }

    override fun canTurnImportantOff(): Boolean {
        return false
    }

    override fun getSortTier(): IntelInfoPlugin.IntelSortTier {
        return IntelInfoPlugin.IntelSortTier.TIER_0
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags = HashSet<String>()
        tags.add(Tags.INTEL_BOUNTY)

        return tags
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: ListInfoMode) {
        info.addPara(name, getTitleColor(ListInfoMode.INTEL), 0f)
    }

    override fun getName(): String {
        return "Sector Bounty Board"
    }

    override fun hasSmallDescription(): Boolean {
        return false
    }

    override fun hasLargeDescription(): Boolean {
        return true
    }

    override fun createLargeDescription(panel: CustomPanelAPI, width: Float, height: Float) {
        MainPanel(panel, width, height).drawPanels()
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "bountiesExpanded_intel_icon")
    }

    override fun getTitleColor(mode: ListInfoMode): Color {
        return Misc.getHighlightColor()
    }
}