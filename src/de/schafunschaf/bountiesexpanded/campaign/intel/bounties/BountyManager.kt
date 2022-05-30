package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken

class BountyManager {

    companion object {

        @JvmStatic
        val memoryID = "\$_BE_bountyManager"

        @JvmStatic
        fun getInstance(): BountyManager {
            var instance = Global.getSector().memoryWithoutUpdate.get(memoryID)

            if (instance == null) {
                instance = BountyManager()
                Global.getSector().memoryWithoutUpdate.set(memoryID, instance)

            }

            return instance as BountyManager
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
    }

    fun getPostedBy(offeringFaction: FactionAPI): List<PostedBounty> {
        return postedBounties.filter { it.offeringFaction == offeringFaction }.toCollection(ArrayList())
    }
}