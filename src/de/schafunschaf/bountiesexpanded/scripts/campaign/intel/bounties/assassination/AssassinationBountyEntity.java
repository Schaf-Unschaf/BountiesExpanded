package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.helper.ui.DescriptionUtils.createShipListForIntel;
import static de.schafunschaf.bountiesexpanded.helper.ui.DescriptionUtils.generateFancyFleetDescription;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.FormattingTools.aOrAn;
import static de.schafunschaf.bountiesexpanded.util.FormattingTools.singularOrPlural;

public class AssassinationBountyEntity implements BountyEntity {
    private AssassinationBountyIntel intel;
    private final int baseReward;
    private final int bonusReward;
    private final int level;
    private final Difficulty difficulty;

    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken startingPoint;
    private final SectorEntityToken endingPoint;

    private final int obfuscatedFleetSize;

    public static final Object ENTERED_HYPERSPACE = new Object();
    private static final String ASSASSINATION_ICON = "bountiesExpanded_assassination";

    public AssassinationBountyEntity(int baseReward, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken startingPoint, SectorEntityToken endingPoint, Difficulty difficulty, int level) {
        this.baseReward = (int) ((Math.round(baseReward * Settings.ASSASSINATION_BASE_REWARD_MULTIPLIER) / 1000) * 1000);
        this.bonusReward = (int) ((Math.round(baseReward * Settings.ASSASSINATION_BONUS_REWARD_MULTIPLIER) / 1000) * 1000);
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.difficulty = difficulty;
        this.level = level;

        this.obfuscatedFleetSize = Math.max(fleet.getNumShips() - 4 + new Random().nextInt(9), 1);
    }

    @Override
    public FactionAPI getOfferingFaction() {
        return null;
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
        return startingPoint;
    }

    public SectorEntityToken getEndingPoint() {
        return endingPoint;
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", ASSASSINATION_ICON);
    }

    @Override
    public String getTitle(BountyResult result) {
        if (intel.getListInfo() == ENTERED_HYPERSPACE)
            return "Assassination - Target entered Hyperspace";
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    return "Assassination successful";
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    return "Assassination failed";
            }
        }
        return "Private Contract - Assassination";
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int getBaseReward() {
        return baseReward;
    }

    public int getBonusReward() {
        return bonusReward;
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
        int remainingTravelTime = Math.round(RouteLocationCalculator.getTravelDays(fleet, endingPoint));
        BountyResult result = baseBountyIntel.getResult();
        baseBountyIntel.bullet(info);

        if (intel.getListInfo() == ENTERED_HYPERSPACE) {
            info.addPara("Target: %s", initPad, bulletColor,
                    targetedFaction.getBaseUIColor(), person.getNameString());
            info.addPara("Origin: %s", 0f, bulletColor,
                    highlightColor, startingPoint.getStarSystem().getName());
            info.addPara("Destination: %s", 0f, bulletColor,
                    highlightColor, endingPoint.getStarSystem().getName());
            return;
        }

        if (result == null) {
            String currentLocation;

            if (fleet.isInHyperspace())
                currentLocation = "Hyperspace";
            else if (fleet.getContainingLocation() == endingPoint.getContainingLocation())
                currentLocation = endingPoint.getStarSystem().getName();
            else {
                if (getDifficulty() == Difficulty.EASY) {
                    String factionName = startingPoint.getFaction().getDisplayNameWithArticleWithoutArticle();
                    currentLocation = "Near " + aOrAn(factionName) + " " + factionName + " controlled world";
                } else if (getDifficulty() == Difficulty.MEDIUM) {
                    String factionName = person.getFaction().getDisplayNameWithArticle();
                    currentLocation = "Near a world not at war with " + factionName;
                } else
                    currentLocation = "Unknown";
            }

            if (mode == ListInfoMode.IN_DESC) {
                boolean isFleetInHyperspace = (fleet.isInHyperspace() || fleet.isInHyperspaceTransition());
                float travelPad = isFleetInHyperspace ? 10f : 3f;
                info.addPara("Target: %s", initPad, bulletColor,
                        targetedFaction.getBaseUIColor(), person.getNameString());
                info.addPara("Reward: %s", 3f, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
                info.addPara("Max Bonus: %s", 3f, bulletColor, highlightColor, Misc.getDGSCredits(bonusReward));
                info.addPara("Location: %s", 3f, bulletColor, highlightColor, currentLocation);
                info.addPara("Current Activity: %s", 3f, bulletColor,
                        highlightColor, fleet.getCurrentAssignment().getActionText());

                if (isFleetInHyperspace) {
                    info.addPara("Origin: %s", travelPad, bulletColor,
                            highlightColor, startingPoint.getStarSystem().getName());
                    info.addPara("Destination: %s", 3f, bulletColor,
                            highlightColor, endingPoint.getName());
                    info.addPara("Estimated Travel Time: %s day" + singularOrPlural(remainingTravelTime), 0f, bulletColor, highlightColor, String.valueOf(remainingTravelTime));
                }
            } else {
                info.addPara("Target: %s", initPad, bulletColor,
                        targetedFaction.getBaseUIColor(), person.getNameString());

                if (!baseBountyIntel.isEnding()) {
                    info.addPara("Reward: %s", 0f, bulletColor,
                            highlightColor, Misc.getDGSCredits(baseReward + bonusReward));
                }

                info.addPara("Location: %s", 0f, bulletColor, highlightColor, currentLocation);
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    if (mode == ListInfoMode.IN_DESC)
                        info.addPara("Max reward was: %s", initPad, bulletColor, highlightColor,
                                Misc.getDGSCredits(baseReward + bonusReward));

                    info.addPara("%s received", initPad, bulletColor, highlightColor,
                            Misc.getDGSCredits(result.payment + result.bonus));
                    if (result.bonus > 0)
                        info.addPara("Base: %s + Bonus: %s", 0f, bulletColor, highlightColor,
                                Misc.getDGSCredits(result.payment),
                                Misc.getDGSCredits(result.bonus));
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
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
        List<FleetMemberAPI> flagshipCopy = getFlagshipCopy();
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        info.addImages(width, 128, opad, opad, person.getPortraitSprite(), targetedFaction.getCrest());

        if (isNull(result)) {
            String voidNet = "VoidNet™";
            info.addPara("Our communications officer decrypted the following message sent over '%s':", opad, highlightColor, voidNet);
            info.addSectionHeading("######  HIGH PRIORITY MESSAGE  ######", Misc.getNegativeHighlightColor(), Color.DARK_GRAY, Alignment.MID, opad);
            info.addPara("Requesting immediate assassination of specified target. Window of opportunity is short. Generous reward offered. Additional payment available if killed in Hyperspace. Hidden transponder smuggled successfully on board. Will transmit more information once triggered in hyperspace.", opad);

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            info.addSectionHeading("Fleet Intel", targetedFaction.getBaseUIColor(), targetedFaction.getDarkUIColor(), Alignment.MID, opad);

            int cols = 1;
            int rows = 1;
            float iconSize = width / 3;
            info.addPara("The message had an intel file containing the targets ship attached.", opad);
            if (!Settings.isDebugActive())
                info.addShipList(cols, rows, iconSize, Color.BLACK, flagshipCopy, opad);
            generateFancyFleetDescription(info, opad, fleet, person);
            info.addPara("Intercepted communications suggest that " + person.getHisOrHer() + " escort contains roughly %s additional ship" + singularOrPlural(obfuscatedFleetSize) + ".", opad, highlightColor, String.valueOf(obfuscatedFleetSize));
            info.addPara("Your tactical officer classifies this fleet as " + difficulty.getShortDescriptionAnOrA() + " %s encounter.", opad, difficulty.getColor(), difficulty.getShortDescription());

            if (Settings.isDebugActive()) {
                createShipListForIntel(info, width, opad, fleet, fleet.getNumShips(), 1, false);
                info.addPara("SPAWN LOCATION: " + startingPoint.getName(), 0f);
                info.addPara("DESTINATION: " + endingPoint.getName(), 0f);
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    info.addPara("Target elimination confirmed. Credits received.", opad);
                    info.addPara("%s Credits got transferred (Base: %s + Bonus: %s)", 10f, highlightColor,
                            Misc.getDGSCredits(result.payment + result.bonus),
                            Misc.getDGSCredits(result.payment),
                            Misc.getDGSCredits(result.bonus));
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                    info.addPara("The target was eliminated by someone else.", opad);
                    break;
                case END_TIME:
                case END_OTHER:
                    info.addPara("We lost track of the target. Mission failed.", opad);
                    break;
            }
        }
    }

    public void reportEnteredHyperspace() {
        intel.sendUpdateIfPlayerHasIntel(ENTERED_HYPERSPACE, true);
    }

    public AssassinationBountyIntel getIntel() {
        return intel;
    }

    public void setIntel(AssassinationBountyIntel intel) {
        this.intel = intel;
    }

    private List<FleetMemberAPI> getFlagshipCopy() {
        List<FleetMemberAPI> copyList = new ArrayList<>();

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        boolean deflate = false;
        if (!fleet.isInflated()) {
            fleet.inflateIfNeeded();
            deflate = true;
        }
        for (FleetMemberAPI member : members) {
            if (!member.isFlagship())
                continue;

            copyList.add(member);
        }
        if (deflate)
            fleet.deflate();
        return copyList;
    }
}