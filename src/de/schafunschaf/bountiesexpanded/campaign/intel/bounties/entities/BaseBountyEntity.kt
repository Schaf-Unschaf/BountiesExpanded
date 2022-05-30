package de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty

abstract class BaseBountyEntity : PostedBounty {

    override fun createPreview(info: TooltipMakerAPI, width: Float, height: Float) {
        val imageSize = 64f
        val outerMargin = 2f
        val padding = 3f

        info.addSpacer(0f).position.setXAlignOffset(-10 + outerMargin)
        info.addImage(targetedPerson.portraitSprite, imageSize, padding)
        info.addImage(offeringFaction.logo, 32f, 20f, -20f)
        info.textWidthOverride = width - imageSize - outerMargin * 2
        info.addPara(
            "${offeringFaction.displayNameWithArticleWithoutArticle} VS ${targetedFaction.displayNameWithArticleWithoutArticle}",
            -imageSize - padding
        ).position.setXAlignOffset(imageSize + outerMargin * 2)
        info.addPara("At: ${constellation.name}", padding)
        info.addPara("Reward: $creditReward", padding)
    }

    override fun acceptBounty() {
        val bountyManager = Global.getSector().memoryWithoutUpdate.get(BountyManager.memoryID) as BountyManager
        bountyManager.postBounty(this, constellation.systems.random().planets.random())
        accepted = true
    }

    override fun abortBounty(force: Boolean) {

    }

    override fun endBounty(successful: Boolean) {

    }

    override fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }

}