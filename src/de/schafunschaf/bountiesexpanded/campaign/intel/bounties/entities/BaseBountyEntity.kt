package de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.PostedBounty

abstract class BaseBountyEntity : PostedBounty {

    override fun createDetailedPosting(info: TooltipMakerAPI, width: Float, height: Float) {

    }

    override fun acceptBounty() {
        val bountyManager = Global.getSector().memoryWithoutUpdate.get(BountyManager.memoryID) as BountyManager
        bountyManager.postBounty(this, targetLocation)
        accepted = true
    }

    override fun abortBounty(force: Boolean) {

    }

    override fun endBounty(successful: Boolean) {

    }

    override fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }
}