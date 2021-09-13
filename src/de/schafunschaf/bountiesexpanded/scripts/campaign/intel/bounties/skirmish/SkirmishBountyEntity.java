package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountylib.campaign.helper.fleet.FleetGenerator;

import java.awt.*;
import java.util.List;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;
import static de.schafunschaf.bountylib.campaign.helper.util.FormattingTools.singularOrPlural;

public class SkirmishBountyEntity implements BountyEntity {
    private final int baseReward;
    private final int level;
    private final int maxPayout;
    private final Difficulty difficulty;
    private final int baseShipBounty;
    private final float fractionToKill;
    private final int shipsToDestroy;
    private final FactionAPI offeringFaction;
    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken hideout;
    String[] creditsPerSize;

    public SkirmishBountyEntity(int baseReward, FactionAPI offeringFaction, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken hideout, float fractionToKill, Difficulty difficulty, int level) {
        this.baseReward = baseReward;
        this.fractionToKill = fractionToKill;
        this.offeringFaction = offeringFaction;
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.hideout = hideout;
        this.difficulty = difficulty;
        this.level = level;
        this.baseShipBounty = Math.round((int) (Settings.SKIRMISH_BASE_SHIP_BOUNTY * (1 - fractionToKill)) / 10f) * 10;
        this.shipsToDestroy = Math.max((int) (fleet.getFleetData().getMembersListCopy().size() * fractionToKill), 1);
        this.maxPayout = calculateMaxPayout();
        this.creditsPerSize = new String[]{Misc.getDGSCredits(baseShipBounty * Misc.getSizeNum(ShipAPI.HullSize.FRIGATE)),
                Misc.getDGSCredits(baseShipBounty * Misc.getSizeNum(ShipAPI.HullSize.DESTROYER)),
                Misc.getDGSCredits(baseShipBounty * Misc.getSizeNum(ShipAPI.HullSize.CRUISER)),
                Misc.getDGSCredits(baseShipBounty * Misc.getSizeNum(ShipAPI.HullSize.CAPITAL_SHIP))};
    }

    private int calculateMaxPayout() {
        int maxPayout = 0;
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy())
            maxPayout += Misc.getSizeNum(ship.getHullSpec().getHullSize()) * baseShipBounty;
        return maxPayout + baseReward;
    }

    @Override
    public FactionAPI getOfferingFaction() {
        return offeringFaction;
    }

    @Override
    public FactionAPI getTargetedFaction() {
        return targetedFaction;
    }

    @Override
    public CampaignFleetAPI getFleet() {
        return fleet;
    }

    @Override
    public PersonAPI getPerson() {
        return person;
    }

    @Override
    public SectorEntityToken getStartingPoint() {
        return hideout;
    }

    @Override
    public SectorEntityToken getEndingPoint() {
        return null;
    }

    @Override
    public String getIcon() {
        return targetedFaction.getCrest();
    }

    @Override
    public String getTitle(BountyResult result) {
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                    return "Bounty Completed";
                case END_OTHER:
                case END_TIME:
                    return "Bounty Ended";
            }
        }
        return "Military Bounty - Skirmish";
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int getBaseReward() {
        return baseReward;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();

        BountyResult result = baseBountyIntel.getResult();
        baseBountyIntel.bullet(info);

        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        if (result == null) {
            if (mode == ListInfoMode.IN_DESC) {
                info.addPara("%s reward on completion", initPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
                info.addPara("%s maximum reward", 0f, bulletColor, highlightColor, Misc.getDGSCredits(maxPayout));
                int days = Math.max((int) (duration - elapsedDays), 1);
                baseBountyIntel.addDays(info, "remaining", days, bulletColor);
            } else {
                info.addPara("Offered by: " + offeringFaction.getDisplayName(), initPad, bulletColor,
                        offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
                info.addPara("Target: " + targetedFaction.getDisplayName(), 0f, bulletColor,
                        targetedFaction.getBaseUIColor(), targetedFaction.getDisplayName());

                if (!baseBountyIntel.isEnding()) {
                    int days = Math.max((int) (duration - elapsedDays), 1);

                    info.addPara("%s reward, %s day" + singularOrPlural(days) + " remaining", 0f, bulletColor,
                            highlightColor, Misc.getDGSCredits(maxPayout), "" + days);
                }
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String payout = Misc.getDGSCredits(Math.round((result.payment + result.bonus) * result.share));
                    String basePayout = Misc.getDGSCredits(result.payment);
                    String bonusPayout = Misc.getDGSCredits(result.bonus);
                    info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                    if (result.share < 1f)
                        info.addPara("(%s Base + %s Bonus) * %s Participation", initPad, bulletColor, highlightColor, basePayout, bonusPayout, (int) (result.share * 100) + "%");
                    else
                        info.addPara("%s Base + %s Bonus", initPad, bulletColor, highlightColor, basePayout, bonusPayout);
                    CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, initPad);
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                    CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, 0f);
                    break;
                case END_TIME:
                case END_OTHER:
                    break;
            }
        }

        baseBountyIntel.unindent(info);
    }

    @Override
    public void createSmallDescription(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, float width, float height) {
        Color highlightColor = Misc.getHighlightColor();
        Color[] factionColors = {offeringFaction.getColor(), targetedFaction.getColor()};
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;


        info.addImages(width, 100, opad, opad, offeringFaction.getLogo(), targetedFaction.getLogo());
        info.addPara("%s officials have offered a reward for thinning out a hostile %s fleet.", opad,
                factionColors,
                Misc.ucFirst(offeringFaction.getDisplayName()),
                targetedFaction.getDisplayNameWithArticleWithoutArticle());

        if (isNotNull(result)) {
            if (result.type == BountyResult.BountyResultType.END_PLAYER_BOUNTY) {
                info.addPara("You have successfully completed the mission.", opad);
            } else {
                info.addPara("This mission is no longer on offer.", opad);
            }
        }

        addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

        if (isNull(result)) {
            String isOrWas = isNull(fleet.getAI().getCurrentAssignmentType()) ? "was last seen " : "is ";
            info.addPara(
                    "The fleet " + isOrWas + "near " + hideout.getName() + " in the "
                            + hideout.getStarSystem().getName() + ".",
                    10f, highlightColor, hideout.getName(), hideout.getStarSystem().getName());

            List<FleetMemberAPI> fleetMemberList = FleetGenerator.createCompleteCopyForIntel(fleet);
            int cols = 7;
            int rows = (int) Math.ceil(fleetMemberList.size() / (float) cols);
            float iconSize = width / cols;
            Color[] factionAndHighlightColors = {offeringFaction.getBaseUIColor(), highlightColor};
            info.addPara("Since this is an official military operation, %s transmitted a complete intel report.", opad, offeringFaction.getBaseUIColor(), offeringFaction.getDisplayNameWithArticle());
            info.addShipList(cols, rows, iconSize, baseBountyIntel.getFactionForUIColors().getBaseUIColor(), fleetMemberList, opad);
            info.addPara("To claim your bounty, %s demands the destruction of at least %s ship" + singularOrPlural(shipsToDestroy) + ".", opad, factionAndHighlightColors, offeringFaction.getDisplayNameWithArticle(), String.valueOf(shipsToDestroy));
            info.addPara("They will also pay an additional %s / %s / %s / %s credits per kill as bonus on top of your reward.", opad, highlightColor, creditsPerSize);
            info.addPara("Your tactical officer classifies this fleet as " + difficulty.getShortDescriptionAnOrA() + " %s encounter.", opad, difficulty.getColor(), difficulty.getShortDescription());

            if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO || Settings.SHEEP_DEBUG)
                info.addPara("FLEET LEVEL: " + level, opad * 2);
        }
    }

    public String[] getCreditsPerSize() {
        return creditsPerSize;
    }

    public float getFractionToKill() {
        return fractionToKill;
    }

    public int getMaxPayout() {
        return maxPayout;
    }

    public int getBaseShipBounty() {
        return baseShipBounty;
    }

    public int getShipsToDestroy() {
        return shipsToDestroy;
    }

    public SectorEntityToken getHideout() {
        return hideout;
    }
}