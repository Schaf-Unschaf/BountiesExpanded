package de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BaseBountyIntel
import de.schafunschaf.bountiesexpanded.ids.BE_Icons
import kotlin.random.Random

class PirateBounty(override var accepted: Boolean = false) : BaseBountyEntity() {

    override val title: String = "Pirate Hunt"
    override val icon: String = BE_Icons.PIRATE_SILLY
    override val difficulty: Int = Random.nextInt(1, 5)
    override val duration: Int = 60
    override val offeringFaction: FactionAPI = Global.getSector().allFactions.random()
    override val targetedFaction: FactionAPI = Global.getSector().getFaction(Factions.PIRATES)
    override val offeringPerson: PersonAPI = offeringFaction.createRandomPerson()
    override val targetedPerson: PersonAPI =
        Global.getFactory().createOfficerData(targetedFaction.createRandomPerson()).person
    override val constellation: Constellation =
        Global.getSector().allLocations.filter { loc -> loc.isInConstellation }.random().constellation
    override val creditReward: Int = Random.nextInt(10_000, 100_000)
    override val itemRewards: ArrayList<CargoStackAPI>? = null
    override var bountyIntel: BaseBountyIntel? = null
}