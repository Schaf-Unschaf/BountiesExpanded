package de.schafunschaf.bountiesexpanded.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import de.schafunschaf.bountiesexpanded.campaign.intel.BountyBoardIntel
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BountyManager
import de.schafunschaf.bountiesexpanded.campaign.listeners.BE_CampaignListener

class BountiesExpandedPlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        initBountiesExpanded()
    }

    override fun beforeGameSave() {
        removeIntel()
    }

    override fun afterGameSave() {
        initIntel()
    }

    override fun onGameLoad(newGame: Boolean) {
        initIntel()
        initManager()
        initListener()
    }

    private fun initIntel() {
        val bountyBoardIntel = BountyBoardIntel.getInstance()
        bountyBoardIntel.isNew = false
        Global.getSector().intelManager.addIntel(bountyBoardIntel)
    }

    private fun initManager() {
        BountyManager.getInstance()
    }

    private fun initListener() {
        BE_CampaignListener.init()
    }

    private fun removeIntel() {
        val bountyBoardIntel = BountyBoardIntel.getInstance()
        Global.getSector().intelManager.removeIntel(bountyBoardIntel)
    }

    private fun initBountiesExpanded() {}
}