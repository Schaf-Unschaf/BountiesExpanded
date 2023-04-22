package de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities.pirate

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.BaseBountyIntel
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.DeadOrAlive
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.Difficulty
import de.schafunschaf.bountiesexpanded.campaign.intel.bounties.entities.BaseBountyEntity
import de.schafunschaf.bountiesexpanded.ids.BE_Icons
import de.schafunschaf.bountiesexpanded.import.KILL_WORDS
import de.schafunschaf.bountiesexpanded.import.PIRATE_JOBS
import de.schafunschaf.bountiesexpanded.import.PIRATE_PERSONALITIES
import de.schafunschaf.bountiesexpanded.import.PIRATE_TITLES
import de.schafunschaf.bountiesexpanded.util.extensions.addSeparator
import de.schafunschaf.bountiesexpanded.util.extensions.addSprite
import kotlin.random.Random

class PirateBounty(
    override val title: String,
    override val icon: String,
    override val duration: Float,
    override val difficulty: Difficulty,
    override val deadOrAlive: DeadOrAlive,
    override val offeringFaction: FactionAPI,
    override val targetedFaction: FactionAPI,
    override val offeringPerson: PersonAPI,
    override val targetedPerson: PersonAPI,
    override val offeringMarket: MarketAPI,
    override val targetLocation: SectorEntityToken,
    override val creditReward: Int,
    override val itemRewards: ArrayList<CargoStackAPI>?,
    override var bountyIntel: BaseBountyIntel? = null,
    override var accepted: Boolean = false
) : BaseBountyEntity() {

    private val imageSize = 85f
    private val starSize = 15f
    private val badgeSize = 64f

    override fun createPreviewPosting(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addSpacer(0f).position.setXAlignOffset(-3f)
        info.setParaFont(Fonts.ORBITRON_24AABOLD)
        info.addPara("WANTED", 1f).setAlignment(Alignment.MID)
        info.addSpacer(0f).position.inTMid(45f)
        info.addSeparator(120f, 3f, Misc.getDarkPlayerColor(), 0f).setXAlignOffset(-60f)
        info.addSpacer(0f).position.inTMid(46f)
        info.addSeparator(150f, 1f, Misc.getDarkPlayerColor(), 0f).setXAlignOffset(-75f)
        info.addSpacer(0f).position.inTMid(50f).setXAlignOffset(-imageSize / 2)
        info.addSprite(
            Global.getSettings().getSprite(targetedPerson.portraitSprite),
            imageSize,
            imageSize,
            withScanlines = true,
            withBorder = true,
            borderSize = 1f,
            borderColor = Misc.getDarkPlayerColor(),
            padding = 3f
        )
        val badge = Global.getSettings().getSprite(BE_Icons.BOARD_BADGE)
        badge.alphaMult = 0.2f
        info.addSprite(
            badge,
            badgeSize,
            badgeSize,
            false,
            withBorder = false,
            borderSize = 0f,
            borderColor = null,
            padding = -badgeSize
        )
        info.prev.position.inTMid(170f).setXAlignOffset(40f)
        info.addSpacer(0f).position.inTMid(55f + imageSize)
        drawDifficultyStars(info)
        info.addSpacer(0f).position.inTL(0f, 55f + imageSize + starSize)
        info.setParaFont(Fonts.ORBITRON_16)
        val computeStringWidth = info.computeStringWidth(targetedPerson.nameString)
        info.addPara(info.shortenString(targetedPerson.nameString, width * 1.6f), targetedFaction.color, 3f)
            .setAlignment(Alignment.MID)
        info.setParaFontDefault()
        info.addPara(deadOrAlive.toString(), 6f).setAlignment(Alignment.MID)
        info.setParaFont(Fonts.ORBITRON_20AABOLD)
        info.addPara(Misc.getDGSCredits(creditReward.toFloat()), Misc.getHighlightColor(), 0f)
            .setAlignment(Alignment.MID)
        info.prev.position.inBL(0f, if (computeStringWidth > width * 0.95f) 21f else 2f)
    }

    override fun createDetailedPosting(info: TooltipMakerAPI, width: Float, height: Float) {
        val random = Random(targetedPerson.id.hashCode())
        val offeringFactionNameWithArticle = offeringFaction.displayNameWithArticle
        val marketName = offeringMarket.name
        val targetName = targetedPerson.nameString
        val pirateJob = PIRATE_JOBS.random(random)
        val piratePersonality = PIRATE_PERSONALITIES.random(random)
        val pirateTitle = createPirateTitle(marketName, random)
        val himOrHerself = targetedPerson.himOrHer + "self"
        val killWord = when (deadOrAlive) {
            DeadOrAlive.DEAD -> KILL_WORDS.random(random)
            DeadOrAlive.ALIVE -> "capture"
            DeadOrAlive.DEAD_OR_ALIVE -> KILL_WORDS.random(random) + " or capture"
        }
        val briefingText = "$targetName, also known as the $pirateTitle, has proven $himOrHerself as a big enough " +
                "annoyance for $offeringFactionNameWithArticle which can't be ignored any longer. A bounty was now offered for " +
                "the $killWord of that $piratePersonality $pirateJob."

        info.setParaFont(Fonts.INSIGNIA_LARGE)
        addFactionNameWithFlags(info, offeringFaction)
        info.prev.position.inTMid(20f)
        info.addSpacer(0f).position.inTL(5f, 50f)
        info.addPara(briefingText, 10f, Misc.getHighlightColor(), targetName, killWord)

        val heOrShe = targetedPerson.heOrShe.replaceFirstChar { it.uppercase() }
        val fake: SectorEntityToken = targetLocation.containingLocation.createToken(0f, 0f)
        fake.orbit = Global.getFactory().createCircularOrbit(targetLocation, 0f, 1000f, 100f)
        var loc = BreadcrumbSpecial.getLocatedString(fake)
        loc = loc.replace("orbiting".toRegex(), "hiding out near")
        loc = loc.replace("located in".toRegex(), "hiding out in")
        val star = targetLocation.starSystem.star
        val starType = BreadcrumbSpecial.getStarTypeName(star)
        val starColor = if (star != null && !star.spec.isBlackHole) BreadcrumbSpecial.getStarColorName(star) else ""
        if (targetLocation.tags.contains(Tags.SALVAGEABLE)) {
            val replace = targetLocation.customEntitySpec.aOrAn + " " + targetLocation.customEntitySpec.nameInText
            loc = loc.replace(replace, "an unidentified entity")
        }
        info.addPara(
            "$heOrShe is rumored to be $loc.",
            10f,
            Misc.getHighlightColor(),
            "trinary",
            "binary",
            "black",
            "hole",
            starType,
            starColor +
            " primary",
            "primary",
            "star",
            "system"
        )
    }

    private fun drawDifficultyStars(parentUIElement: TooltipMakerAPI) {
        val customPanelAPI =
            Global.getSettings().createCustom(imageSize, starSize, null)
        val uiElement = customPanelAPI.createUIElement(imageSize, starSize, false)

        var totalWidth = 0f
        var starAmount = 0
        while (starAmount < difficulty.ordinal + 1) {
            val sprite = Global.getSettings().getSprite(BE_Icons.STAR)
            sprite.color = difficulty.color
            uiElement.addSprite(
                sprite,
                starSize,
                starSize,
                withScanlines = false,
                withBorder = false,
                borderSize = 0f,
                borderColor = null,
                padding = if (starAmount > 0) -starSize else 0f
            )
            val width = if (starAmount > 0) starSize + 2f else 0f
            totalWidth += if (starAmount > 0) starSize + 2f else starSize
            uiElement.prev.position.setXAlignOffset(width)

            starAmount++
        }

        customPanelAPI.addUIElement(uiElement).setXAlignOffset(-totalWidth / 2 - 1f)
        parentUIElement.addCustom(customPanelAPI, 0f)
    }

    private fun createPirateTitle(marketName: String, random: Random): String {
        val title = PIRATE_TITLES.random(random)
        return if (random.nextBoolean()) "$title of $marketName" else "$marketName $title"
    }

    private fun addFactionNameWithFlags(info: TooltipMakerAPI, faction: FactionAPI) {
        val flagWidth = 30f
        val space = 10f
        val factionName = faction.displayName.replaceFirstChar(Char::uppercase)
        val text = "Official $factionName Bounty"
        val stringWidth = info.computeStringWidth(text)
        val sumWidth = flagWidth + space + stringWidth + space + flagWidth
        val customPanelAPI = Global.getSettings().createCustom(sumWidth, 30f, null)
        val uiElement = customPanelAPI.createUIElement(sumWidth, 30f, false)

        uiElement.setParaFont(Fonts.INSIGNIA_LARGE)
        uiElement.addImage(faction.logo, flagWidth, 0f)
        uiElement.prev.position.inTL(0f, 5f)
        uiElement.addPara(text, faction.color, 0f)
        uiElement.prev.position.inTL(flagWidth + space, 3f)
        uiElement.addImage(faction.logo, flagWidth, 0f)
        uiElement.prev.position.inTL(flagWidth + space + stringWidth + space, 5f)

        uiElement.addSeparator(sumWidth - 70f, 3f, faction.color, 0f).inTL(35f, 27f)
        uiElement.addSeparator(sumWidth + 20f, 1f, faction.color, 0f).inTL(-10f, 28f)

        customPanelAPI.addUIElement(uiElement).inTL(0f, 0f)
        info.addCustom(customPanelAPI, 0f)
    }
}