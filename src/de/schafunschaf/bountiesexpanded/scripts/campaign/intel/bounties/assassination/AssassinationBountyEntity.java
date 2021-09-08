package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;
import static de.schafunschaf.bountylib.campaign.helper.util.FormattingTools.dayOrDays;

public class AssassinationBountyEntity implements BountyEntity {
    private final int baseReward;
    private final Difficulty difficulty;

    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken startingPoint;
    private final SectorEntityToken endingPoint;

    private final int obfuscatedFleetSize;
    private final List<FleetMemberAPI> flagshipAsList;

    public AssassinationBountyEntity(int baseReward, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken startingPoint, SectorEntityToken endingPoint, Difficulty difficulty) {
        this.baseReward = baseReward;
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.difficulty = difficulty;

        this.obfuscatedFleetSize = Math.max(fleet.getNumShips() - 7 + new Random().nextInt(15), 1);
        this.flagshipAsList = getFlagshipCopy();
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
        return person.getPortraitSprite();
    }

    @Override
    public String getTitle(BountyResult result) {
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

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        int remainingTravelTime = Math.round(RouteLocationCalculator.getTravelDays(fleet, endingPoint));
        BountyResult result = baseBountyIntel.getResult();
        baseBountyIntel.bullet(info);

        if (result == null) {
            if (mode == ListInfoMode.IN_DESC) {
                boolean isFleetInHyperspace = fleet.getContainingLocation().isHyperspace();
                float travelPad = isFleetInHyperspace ? 10f : 0f;
                info.addPara("Target: %s", initPad, bulletColor,
                        targetedFaction.getBaseUIColor(), person.getNameString());
                info.addPara("Current Activity: %s", 0f, bulletColor,
                        highlightColor, fleet.getCurrentAssignment().getActionText());

                if (isFleetInHyperspace) {
                    info.addPara("Origin: %s", travelPad, bulletColor,
                            highlightColor, startingPoint.getStarSystem().getName());
                    info.addPara("Destination: %s", 0f, bulletColor,
                            highlightColor, endingPoint.getName());
                    info.addPara("Estimated Travel Time: %s " + dayOrDays(remainingTravelTime), 0f, bulletColor, highlightColor, String.valueOf(remainingTravelTime));
                }

                info.addPara("Reward: %s", travelPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
            } else {
                info.addPara("Target: %s", initPad, bulletColor,
                        targetedFaction.getBaseUIColor(), person.getNameString());

                if (!baseBountyIntel.isEnding()) {
                    info.addPara("Reward: %s", 0f, bulletColor,
                            highlightColor, Misc.getDGSCredits(baseReward));
                }

                String currentLocation;
                if (fleet.isInHyperspace()) currentLocation = "Hyperspace";
                else if (fleet.getContainingLocation() == endingPoint.getContainingLocation())
                    currentLocation = endingPoint.getStarSystem().getName();
                else
                    currentLocation = "Unknown";

                info.addPara("Location: %s", 0f, bulletColor, highlightColor, currentLocation);
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    info.addPara("%s received", 10f, bulletColor, highlightColor,
                            Misc.getDGSCredits(result.payment + result.bonus));
                    info.addPara("Base: %s + Bonus: %s", 3f, bulletColor, highlightColor,
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
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        info.addImages(width, 128, opad, opad, person.getPortraitSprite(), targetedFaction.getCrest());

        if (isNull(result)) {
            String voidNet = "VoidNet™";
            String messageTopic = "##### HIGH PRIORITY MESSAGE #####";
            info.addPara("Our communications officer decrypted the following message sent over '%s':", opad, highlightColor, voidNet);
            info.addPara("     %s", opad, highlightColor, messageTopic);
            info.addPara("Requesting immediate assassination of specified target. Window of opportunity is short. Generous reward offered. Additional payment available if killed in Hyperspace.", opad / 2);

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            int cols = 1;
            int rows = 1;
            float iconSize = width / 3;
            info.addPara("The message had an intel file containing the targets ship attached.", opad);
            info.addShipList(cols, rows, iconSize, baseBountyIntel.getFactionForUIColors().getBaseUIColor(), flagshipAsList, opad);
            info.addPara("Intercepted communications suggest that " + person.getHisOrHer() + " escort contains roughly %s additional ships.", opad, highlightColor, String.valueOf(obfuscatedFleetSize));
            info.addPara("Your tactical officer classifies this fleet as " + difficulty.getShortDescriptionAnOrA() + " %s encounter.", opad, difficulty.getColor(), difficulty.getShortDescription());
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

            FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());

            copyList.add(copy);
        }
        if (deflate)
            fleet.deflate();
        return copyList;
    }
}