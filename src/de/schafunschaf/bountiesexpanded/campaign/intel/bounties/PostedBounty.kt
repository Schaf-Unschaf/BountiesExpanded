package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.ui.TooltipMakerAPI

interface PostedBounty {

    val title: String
    val icon: String
    val duration: Int
    val difficulty: Int
    val offeringFaction: FactionAPI
    val targetedFaction: FactionAPI
    val offeringPerson: PersonAPI
    val targetedPerson: PersonAPI
    val constellation: Constellation
    val creditReward: Int
    val itemRewards: ArrayList<CargoStackAPI>?
    var bountyIntel: BaseBountyIntel?
    var accepted: Boolean

    fun createPreview(info: TooltipMakerAPI, width: Float, height: Float)
    fun acceptBounty()
    fun abortBounty(force: Boolean)
    fun endBounty(successful: Boolean)
    fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?)
}