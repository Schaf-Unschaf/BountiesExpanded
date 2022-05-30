package de.schafunschaf.bountiesexpanded.campaign.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager

class BE_CampaignListener(permaRegister: Boolean) : BaseCampaignEventListener(permaRegister) {

    companion object {

        @JvmStatic
        fun init() {
            if (!Global.getSector().listenerManager.hasListenerOfClass(BE_CampaignListener::class.java))
                Global.getSector().listenerManager.addListener(BE_CampaignListener(false), true)
        }
    }

    override fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        val bountyManager = Global.getSector().memoryWithoutUpdate.get(BountyManager.memoryID) as BountyManager

        bountyManager.activeBounties.forEach { bounty -> bounty.reportBattleOccurred(primaryWinner, battle) }
    }
}