package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.ui.TooltipMakerAPI

interface PostedBounty {

    val title: String
    val icon: String
    val duration: Float
    val difficulty: Difficulty
    val deadOrAlive: DeadOrAlive
    val offeringFaction: FactionAPI
    val targetedFaction: FactionAPI
    val offeringPerson: PersonAPI
    val targetedPerson: PersonAPI
    val offeringMarket: MarketAPI
    val targetLocation: SectorEntityToken
    val creditReward: Int
    val itemRewards: ArrayList<CargoStackAPI>?
    var bountyIntel: BaseBountyIntel?
    var accepted: Boolean

    fun createPreviewPosting(info: TooltipMakerAPI, width: Float, height: Float)
    fun createDetailedPosting(info: TooltipMakerAPI, width: Float, height: Float)
    fun acceptBounty()
    fun abortBounty(force: Boolean)
    fun endBounty(successful: Boolean)
    fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?)
}