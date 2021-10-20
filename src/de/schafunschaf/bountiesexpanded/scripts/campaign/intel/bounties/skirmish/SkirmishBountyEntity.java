package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.ui.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.FormattingTools.singularOrPlural;

public class SkirmishBountyEntity implements BountyEntity {
    private float targetRepBeforeBattle = 0;
    private static final String SKIRMISH_ICON = "bountiesExpanded_skirmish";
    private final int baseReward;
    private final int level;
    private final int maxPayout;
    private final Difficulty difficulty;
    private final int baseShipBounty;
    private final float fractionToKill;
    private final int shipsToDestroy;
    private final int maxFleetSizeForCompletion;
    private final FactionAPI offeringFaction;
    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken hideout;
    private final String[] creditsPerSize;
    private final Map<HullSize, Integer> numFleetMembers;

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
        this.baseShipBounty = (int) FormattingTools.roundWholeNumber((Settings.SKIRMISH_BASE_SHIP_BOUNTY * (1 - fractionToKill)), 1);
        this.shipsToDestroy = Math.max((int) (fleet.getFleetData().getMembersListCopy().size() * fractionToKill), 1);
        this.maxFleetSizeForCompletion = fleet.getNumShips() - shipsToDestroy;
        this.maxPayout = calculateMaxPayout();
        this.creditsPerSize = new String[]{
                Misc.getDGSCredits(FormattingTools.roundWholeNumber(baseShipBounty * Misc.getSizeNum(HullSize.FRIGATE), 2)),
                Misc.getDGSCredits(FormattingTools.roundWholeNumber(baseShipBounty * Misc.getSizeNum(HullSize.DESTROYER), 2)),
                Misc.getDGSCredits(FormattingTools.roundWholeNumber(baseShipBounty * Misc.getSizeNum(HullSize.CRUISER), 2)),
                Misc.getDGSCredits(FormattingTools.roundWholeNumber(baseShipBounty * Misc.getSizeNum(HullSize.CAPITAL_SHIP), 2))};
        numFleetMembers = new HashMap<>();
        initHullSizeMap(numFleetMembers);
        for (FleetMemberAPI fleetMember : fleet.getFleetData().getMembersListCopy())
            increaseShipByOne(fleetMember, numFleetMembers);
    }

    private int calculateMaxBonus() {
        int maxBonus = 0;
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy())
            maxBonus += Misc.getSizeNum(ship.getHullSpec().getHullSize()) * baseShipBounty;
        return FormattingTools.roundWholeNumber(maxBonus, 2);
    }

    private int calculateMaxPayout() {
        return calculateMaxBonus() + baseReward;
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
        return Global.getSettings().getSpriteName("intel", SKIRMISH_ICON);
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

    public float getTargetRepBeforeBattle() {
        return targetRepBeforeBattle;
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

    public int getMaxFleetSizeForCompletion() {
        return maxFleetSizeForCompletion;
    }

    public Map<HullSize, Integer> getNumFleetMembers() {
        return numFleetMembers;
    }

    public SectorEntityToken getHideout() {
        return hideout;
    }

    public void setTargetRepBeforeBattle(float targetRepBeforeBattle) {
        this.targetRepBeforeBattle = targetRepBeforeBattle;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);

        BountyResult result = baseBountyIntel.getResult();
        baseBountyIntel.bullet(info);

        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        if (result == null) {
            if (mode == ListInfoMode.IN_DESC) {
                info.addPara("%s reward on completion", initPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
                info.addPara("%s maximum reward", 0f, bulletColor, highlightColor, Misc.getDGSCredits(maxPayout));
                baseBountyIntel.addDays(info, "remaining", days, bulletColor);
            } else {
                info.addPara("Offered by: " + offeringFaction.getDisplayName(), initPad, bulletColor,
                        offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
                info.addPara("Target: " + targetedFaction.getDisplayName(), 0f, bulletColor,
                        targetedFaction.getBaseUIColor(), targetedFaction.getDisplayName());
                if (!baseBountyIntel.isEnding()) {
                    info.addPara("%s reward, %s day" + singularOrPlural(days) + " remaining", 0f, bulletColor,
                            highlightColor, Misc.getDGSCredits(maxPayout), "" + days);
                }
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String payout = Misc.getDGSCredits(result.payment + result.bonus);

                    if (mode != ListInfoMode.IN_DESC) {
                        info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                        CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                                null, null, info, bulletColor, isUpdate, initPad);
                    }
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
        String isOrWas = isNull(fleet.getAI().getCurrentAssignmentType()) ? "was last seen " : "is ";
        Color highlightColor = Misc.getHighlightColor();
        Color[] factionColors = {offeringFaction.getColor(), targetedFaction.getColor()};
        Color[] factionAndHighlightColors = {offeringFaction.getBaseUIColor(), highlightColor, highlightColor};
        BountyResult result = baseBountyIntel.getResult();
        int shipsLeftToDestroy = fleet.getNumShips() - maxFleetSizeForCompletion;
        float opad = 10f;

        if (isNull(result)) { // Bounty not completed
            DescriptionUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);

            info.addSectionHeading("Briefing", offeringFaction.getBaseUIColor(), offeringFaction.getDarkUIColor(), Alignment.MID, opad);
            info.addPara("%s officials have offered a reward for thinning out a hostile %s fleet.", opad,
                    factionColors,
                    Misc.ucFirst(offeringFaction.getDisplayName()),
                    targetedFaction.getDisplayNameWithArticleWithoutArticle());

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            info.addPara(
                    "The fleet " + isOrWas + "near " + hideout.getName() + " in the "
                            + hideout.getStarSystem().getName() + ".",
                    10f, highlightColor, hideout.getName(), hideout.getStarSystem().getName());

            if (shipsLeftToDestroy < shipsToDestroy)
                info.addPara("To claim your bounty, %s demands the destruction of at least %s ship" + singularOrPlural(shipsToDestroy)
                                + " (%s ship" + singularOrPlural(shipsLeftToDestroy) + " left).",
                        opad, factionAndHighlightColors,
                        offeringFaction.getDisplayNameWithArticle(), String.valueOf(shipsToDestroy), String.valueOf(shipsLeftToDestroy));
            else
                info.addPara("To claim your bounty, %s demands the destruction of at least %s ship" + singularOrPlural(shipsToDestroy) + ".",
                        opad, factionAndHighlightColors,
                        offeringFaction.getDisplayNameWithArticle(), String.valueOf(shipsToDestroy));
            info.addPara("They will also pay an additional %s / %s / %s / %s credits per kill as bonus on top of your reward.",
                    opad, highlightColor, creditsPerSize);

            info.addSectionHeading("Fleet Intel", offeringFaction.getBaseUIColor(), offeringFaction.getDarkUIColor(), Alignment.MID, opad);

            info.addPara("Since this is an official military operation, %s transmitted a complete intel report.",
                    opad, offeringFaction.getBaseUIColor(), offeringFaction.getDisplayNameWithArticle());
            DescriptionUtils.createShipListForIntel(info, width, opad, fleet, fleet.getNumShips(), 3, false);
            info.addPara("Your tactical officer classifies this fleet as " + difficulty.getShortDescriptionAnOrA() + " %s encounter.",
                    opad, difficulty.getColor(), difficulty.getShortDescription());
        } else { // Bounty completed
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    float rewardAdjustment = result.rewardAdjustment;
                    float targetRepChange = result.targetRepAfterBattle - targetRepBeforeBattle;
                    String increasedDecreased;
                    Color rewardColor;
                    int percent;
                    String percentage;

                    DescriptionUtils.addFactionFlagsWithRepChange(info, width, opad, opad, offeringFaction, result.rep.delta, targetedFaction, targetRepChange);

                    info.addSectionHeading("Briefing", offeringFaction.getBaseUIColor(), offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara("%s officials have offered a reward for thinning out a hostile %s fleet.", opad,
                            factionColors,
                            Misc.ucFirst(offeringFaction.getDisplayName()),
                            targetedFaction.getDisplayNameWithArticleWithoutArticle());

                    info.addPara("You have successfully completed the mission.", opad);

                    if (rewardAdjustment > 0) {
                        increasedDecreased = "increased";
                        rewardColor = Misc.getPositiveHighlightColor();
                        percent = (int) rewardAdjustment - 100;
                    } else {
                        increasedDecreased = "decreased";
                        rewardColor = Misc.getNegativeHighlightColor();
                        percent = 100 + (int) rewardAdjustment;
                    }
                    percentage = percent + "%";

                    info.addSectionHeading("Skirmish Summary", offeringFaction.getBaseUIColor(), offeringFaction.getDarkUIColor(), Alignment.MID, opad);

                    if (result.share < 1f)
                        info.addPara("Payout reduced due to participation in battle by %s", opad, Misc.getNegativeHighlightColor(), (int) (result.share * 100) + "%");

                    if (percent != 0)
                        info.addPara("Payout %s by %s due to faction standing", opad, rewardColor, increasedDecreased, percentage);

                    addDestroyedShipStats(info, width, result);
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    info.addPara("This mission is no longer on offer.", opad);
            }
        }
    }

    private void initHullSizeMap(Map<ShipAPI.HullSize, Integer> shipMap) {
        shipMap.put(ShipAPI.HullSize.FRIGATE, 0);
        shipMap.put(ShipAPI.HullSize.DESTROYER, 0);
        shipMap.put(ShipAPI.HullSize.CRUISER, 0);
        shipMap.put(ShipAPI.HullSize.CAPITAL_SHIP, 0);
    }

    private void increaseShipByOne(FleetMemberAPI fleetMember, Map<ShipAPI.HullSize, Integer> sizeMap) {
        ShipAPI.HullSize hullSize = fleetMember.getHullSpec().getHullSize();
        if (sizeMap.containsKey(hullSize))
            sizeMap.put(hullSize, sizeMap.get(hullSize) + 1);
        else
            sizeMap.put(hullSize, 1);
    }

    private void addDestroyedShipStats(TooltipMakerAPI info, float width, BountyResult result) {
        List<List<Object>> shipBonusRowList = new ArrayList<>();
        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color grayColor = Misc.getGrayColor();
        float cellWidth = (width - 10) / 10;
        int totalShips = 0;
        int totalDestroyed = 0;
        int totalMaxBonus = 0;

        for (HullSize hullSize : HullSize.values()) {
            if (hullSize.equals(HullSize.DEFAULT) || hullSize.equals(HullSize.FIGHTER))
                continue;

            int numInitialShips = numFleetMembers.get(hullSize);
            if (numInitialShips <= 0)
                continue;

            int[] destroyedShipData = result.destroyedShips.get(hullSize);
            int numDestroyedShips = destroyedShipData[0];

            totalShips += numInitialShips;
            totalDestroyed += numDestroyedShips;

            List<Object> column = new ArrayList<>();
            String shipType;
            switch (hullSize) {
                case FRIGATE:
                    shipType = "- Frigates ";
                    break;
                case DESTROYER:
                    shipType = "- Destroyers ";
                    break;
                case CRUISER:
                    shipType = "- Cruisers ";
                    break;
                case CAPITAL_SHIP:
                    shipType = "- Capitals ";
                    break;
                default:
                    shipType = "- Sheeps ";
            }

            Color numDestroyedColor = numDestroyedShips == numInitialShips ? textColor : grayColor;
            // Ship-Type (Destroyed / Total)
            column.add(Alignment.LMID);
            column.add(numDestroyedColor);
            column.add(shipType + "(" + numDestroyedShips + "/" + numInitialShips + ")");
            // Bonus
            int bonus = destroyedShipData[1];
            column.add(Alignment.RMID);
            column.add(highlightColor);
            column.add(Misc.getDGSCredits(bonus));
            // Max Bonus
            int maxBonus = destroyedShipData[2];
            totalMaxBonus += maxBonus;
            column.add(Alignment.RMID);
            column.add(grayColor);
            column.add(Misc.getDGSCredits(FormattingTools.roundWholeNumber(maxBonus, 2)));

            shipBonusRowList.add(column);
        }

        // Skirmish Payout Summary
        info.beginTable(getOfferingFaction(), 20,
                "", cellWidth * 5f,
                "Received", cellWidth * 2.5f,
                "Offered", cellWidth * 2.5f);

        Color numDestroyedColor = totalDestroyed == totalShips ? textColor : grayColor;

        List<Object> basePayoutRow = new ArrayList<>();
        // Name
        basePayoutRow.add(Alignment.LMID);
        basePayoutRow.add(textColor);
        basePayoutRow.add("Base payment");
        // Base Received
        basePayoutRow.add(Alignment.RMID);
        basePayoutRow.add(highlightColor);
        basePayoutRow.add(Misc.getDGSCredits(result.payment));
        // Max Received
        basePayoutRow.add(Alignment.RMID);
        basePayoutRow.add(grayColor);
        basePayoutRow.add(Misc.getDGSCredits(baseReward));

        List<Object> bonusPayoutRow = new ArrayList<>();
        // Name
        bonusPayoutRow.add(Alignment.LMID);
        bonusPayoutRow.add(numDestroyedColor);
        bonusPayoutRow.add("Bonus (" + totalDestroyed + "/" + totalShips + ")");
        // Base Received
        bonusPayoutRow.add(Alignment.RMID);
        bonusPayoutRow.add(highlightColor);
        bonusPayoutRow.add(Misc.getDGSCredits(result.bonus));
        // Max Received
        bonusPayoutRow.add(Alignment.RMID);
        bonusPayoutRow.add(grayColor);
        bonusPayoutRow.add(Misc.getDGSCredits(FormattingTools.roundWholeNumber(totalMaxBonus, 2)));

        List<Object> totalPayoutRow = new ArrayList<>();
        // Name
        totalPayoutRow.add(Alignment.LMID);
        totalPayoutRow.add(textColor);
        totalPayoutRow.add("Total earnings");
        // Total Received
        totalPayoutRow.add(Alignment.RMID);
        totalPayoutRow.add(highlightColor);
        totalPayoutRow.add(Misc.getDGSCredits(result.payment + result.bonus));
        // Max Total
        totalPayoutRow.add(Alignment.RMID);
        totalPayoutRow.add(grayColor);
        totalPayoutRow.add(Misc.getDGSCredits(maxPayout));

        info.addRow(basePayoutRow.toArray());
        info.addRow(bonusPayoutRow.toArray());

        for (List<Object> columns : shipBonusRowList)
            info.addRow(columns.toArray());

        info.addRow("");
        info.addRow(totalPayoutRow.toArray());

        info.addTable("", 0, 10);
    }
}