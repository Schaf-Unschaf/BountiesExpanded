package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class BaseBountyIntel(private val postedBounty: PostedBounty) : BaseIntelPlugin() {

    init {
        postedBounty.bountyIntel = this
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        postedBounty.createSmallDescription(info, width, height)
    }

    override fun getSmallDescriptionTitle(): String {
        return postedBounty.title
    }

    override fun createIntelInfo(info: TooltipMakerAPI?, mode: ListInfoMode?) {
        if (name.isNotBlank()) {
            val color = getTitleColor(mode)
            info!!.addPara(name, color, 0f)
        }

        addBulletPoints(info, mode)
    }

    override fun getIcon(): String {
        return postedBounty.icon
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return Global.getSector().hyperspace.createToken(postedBounty.constellation.location)
    }

    override fun getFactionForUIColors(): FactionAPI {
        return postedBounty.targetedFaction
    }

    override fun getTitleColor(mode: ListInfoMode?): Color {
        val isUpdate = getListInfoParam() != null

        return if (isEnding && !isUpdate && mode != ListInfoMode.IN_DESC)
            Misc.getGrayColor()
        else factionForUIColors.color
    }

    override fun getBulletColorForMode(mode: ListInfoMode?): Color {
        return getTitleColor(mode)
    }

    override fun advance(amount: Float) {
        advanceImpl(amount)
    }

    override fun getName(): String {
        val rank = postedBounty.targetedPerson.rank
        val name = postedBounty.targetedPerson.nameString

        return "$rank $name"
    }
}