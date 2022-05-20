package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken

class BountyManager {

    companion object {

        @JvmStatic
        val memoryID = "\$_BE_bountyManager"

        @JvmStatic
        fun init() {
            if (!Global.getSector().memoryWithoutUpdate.contains(memoryID))
                Global.getSector().memoryWithoutUpdate.set(memoryID, BountyManager())
        }
    }

    val postedBounties = ArrayList<PostedBounty>()
    val activeBounties = ArrayList<PostedBounty>()

    fun makeBountyActive(bounty: PostedBounty) {
        postedBounties.remove(bounty)
        activeBounties.add(bounty)
    }

    fun postBounty(bounty: PostedBounty, location: SectorEntityToken) {
        postedBounties.add(bounty)

        location.containingLocation.constellation
    }
}