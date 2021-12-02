package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.mission.MissionTextUtils;
import de.schafunschaf.bountiesexpanded.helper.text.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.helper.ui.TooltipAPIUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

import static com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
@Setter
public class WarCriminalEntity implements BountyEntity {
    private final String warCriminalIcon = "bountiesExpanded_placeholder";
    private final int baseReward;
    private final int level;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction;
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI targetedPerson;
    private final SectorEntityToken startingPoint;
    private final SectorEntityToken endingPoint;
    private final MissionHandler missionHandler;
    private PersonAPI offeringPerson;
    private FleetMemberAPI retrievalTargetShip;
    private float targetRepBeforeBattle;
    private WarCriminalIntel bountyIntel;

    public WarCriminalEntity(int baseReward, int level, Difficulty difficulty, FactionAPI targetedFaction, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI targetedPerson, SectorEntityToken startingPoint, SectorEntityToken endingPoint, MissionHandler missionHandler) {
        this.baseReward = baseReward;
        this.level = level;
        this.difficulty = difficulty;
        this.targetedFaction = targetedFaction;
        this.offeringFaction = offeringFaction;
        this.fleet = fleet;
        this.targetedPerson = targetedPerson;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.missionHandler = missionHandler;
        this.offeringPerson = offeringFaction.createRandomPerson();
    }

    @Override
    public BaseEventManager getBountyManager() {
        return WarCriminalManager.getInstance();
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        BountyResult result = baseBountyIntel.getResult();
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);
        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        baseBountyIntel.bullet(info);

        if (isNull(result)) {
            if (mode == ListInfoMode.IN_DESC) {
                info.addPara("%s reward on completion", initPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
            } else {
                info.addPara("Offered by: %s", initPad, offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
                info.addPara("Target: %s", 0, targetedFaction.getBaseUIColor(), targetedPerson.getNameString());
            }
            baseBountyIntel.addDays(info, "remaining", days, bulletColor, 0f);
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String payout = Misc.getDGSCredits(result.payment);

                    if (mode != ListInfoMode.IN_DESC) {
                        info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                        CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                                null, null, info, bulletColor, isUpdate, 0f);
                    }
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
        boolean isRetrievalMission = MissionHandler.MissionType.RETRIEVAL.equals(missionHandler.getMissionType());
        PersonAPI personForBriefingText = isRetrievalMission ? offeringPerson : targetedPerson;
        String objectiveText = String.format(missionHandler.getMissionType().getObjectiveText(), personForBriefingText.getNameString());
        String briefingText = String.format("A large sum has been put on the head of %s, wanted for %s countless war crimes against %s.\n\n" +
                        "To collect this bounty, we need to %s.",
                targetedPerson.getNameString(), getTargetedPerson().getHisOrHer(), offeringFaction.getDisplayNameWithArticle(), objectiveText);
        Color highlightColor = Misc.getHighlightColor();
        Color offeringFactionColor = offeringFaction.getBaseUIColor();
        Color targetedFactionColor = targetedFaction.getBaseUIColor();
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        if (isNull(result)) {
            TooltipAPIUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);
            info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad, new Color[]{targetedFactionColor, offeringFactionColor, personForBriefingText.getFaction().getBaseUIColor()}, targetedPerson.getNameString(), offeringFaction.getDisplayNameWithArticle(), personForBriefingText.getNameString());

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            DescriptionUtils.generateHideoutDescription(info, baseBountyIntel, highlightColor);

            if (isRetrievalMission) {
                info.addSectionHeading("Target Intel", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                MissionTextUtils.generateRetrievalTargetSection(info, width, opad, retrievalTargetShip, targetedFaction);
            }

            info.addSectionHeading("Fleet Intel", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, isRetrievalMission ? 0f : opad);
            DescriptionUtils.generateShipListForIntel(info, width, opad, fleet, fleet.getNumShips(), 3, false);
            DescriptionUtils.generateFancyFleetDescription(info, opad, fleet, targetedPerson);
            DescriptionUtils.addDifficultyText(info, opad, difficulty);
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    float targetRepChange = result.targetRepAfterBattle - targetRepBeforeBattle;

                    TooltipAPIUtils.addFactionFlagsWithRepChange(info, width, opad, opad, offeringFaction, result.rep.delta, targetedFaction, targetRepChange);
                    info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("You have successfully completed the mission.\n\n" +
                                    "Bring the %s back to %s in the %s and hand it over to %s for an additional reward.",
                            opad,
                            new Color[]{highlightColor, offeringFactionColor, offeringFactionColor, offeringFactionColor},
                            retrievalTargetShip.getShipName(),
                            endingPoint.getName(),
                            endingPoint.getStarSystem().getName(),
                            offeringPerson.getNameString());
                    MissionTextUtils.generateRetrievalConsequencesText(info, opad, retrievalTargetShip, offeringFaction, offeringPerson, missionHandler.getChanceForConsequences());
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    TooltipAPIUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);
                    info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("This mission is no longer on offer.\n\n" +
                            "The wanted ship is either destroyed or someone else recovered it.", opad);
                    break;
            }
        }
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", warCriminalIcon);
    }

    @Override
    public String getTitle(BountyResult result) {
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    return "War Criminal Hunt - Completed";
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    return "War Criminal Hunt - Failed";
            }
        }
        return String.format("War Criminal Hunt - %s", missionHandler.getMissionType().getMissionTypeUCFirst());
    }
}
