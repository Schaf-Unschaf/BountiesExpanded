package de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities.pirate

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BaseBountyIntel
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.DeadOrAlive
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.Difficulty
import de.schafunschaf.bountiesexpanded.helper.LocationEntityProvider
import de.schafunschaf.bountiesexpanded.ids.BE_Icons
import de.schafunschaf.bountiesexpanded.util.MathUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

object PirateBountyProvider {

    private val validEntityTags = arrayListOf(
        Tags.GATE,
        Tags.COMM_RELAY,
        Tags.NAV_BUOY,
        Tags.SENSOR_ARRAY,
        Tags.JUMP_POINT,
        Tags.SALVAGEABLE,
        Tags.DEBRIS_FIELD,
        Tags.WRECK,
        Tags.PLANET,
        Tags.GAS_GIANT,
        Tags.TERRAIN,
    )

    private val forbiddenEntityTags = arrayListOf(
        Tags.ACCRETION_DISK
    )

    private val forbiddenSystemTags = arrayListOf(
        Tags.THEME_REMNANT,
        Tags.THEME_REMNANT_MAIN,
        Tags.THEME_REMNANT_RESURGENT,
        Tags.THEME_CORE_POPULATED,
        Tags.THEME_UNSAFE,
        Tags.THEME_HIDDEN
    )

    fun createBounty(): PirateBounty? {
        val title = "Criminal Hunt"
        val icon = BE_Icons.PIRATE_SILLY
        val difficulty = Difficulty.values().random()
        val deadOrAlive = DeadOrAlive.values().random()
        val duration = 60f
        val offeringFaction: FactionAPI? =
            Global.getSector().getFaction(SharedData.getData().personBountyEventData.participatingFactions.random())
        val targetedFaction: FactionAPI = arrayOf(
            Global.getSector().getFaction(Factions.PIRATES),
            Global.getSector().getFaction(Factions.LUDDIC_PATH)
        ).random()
        val offeringPerson: PersonAPI? = offeringFaction?.createRandomPerson()
        val targetedPerson: PersonAPI? =
            Global.getFactory().createOfficerData(targetedFaction.createRandomPerson()).person
        val offeringMarket: MarketAPI? = Misc.getFactionMarkets(offeringFaction).random()
        val targetLocation: SectorEntityToken? = LocationEntityProvider()
            .forbiddenSystems(forbiddenSystemTags)
            .allowedEntities(validEntityTags)
            .forbiddenEntities(forbiddenEntityTags)
            .withinMinRange(10f)
            .withinMaxRange(30f)
            .pickEntity()
        val credits = Random.nextInt(10_000, 200_000)
        val creditReward = MathUtils.roundWholeNumber(credits, max(abs(credits).toString().length - 3, 1))
        val itemRewards: ArrayList<CargoStackAPI>? = null
        val bountyIntel: BaseBountyIntel? = null

        if (offeringFaction == null || offeringPerson == null || targetedPerson == null || offeringMarket == null || targetLocation == null)
            return null

        return PirateBounty(
            title,
            icon,
            duration,
            difficulty,
            deadOrAlive,
            offeringFaction,
            targetedFaction,
            offeringPerson,
            targetedPerson,
            offeringMarket,
            targetLocation,
            creditReward,
            itemRewards,
            bountyIntel
        )
    }
}