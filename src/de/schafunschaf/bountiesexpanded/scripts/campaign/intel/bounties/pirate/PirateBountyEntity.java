package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
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
import static de.schafunschaf.bountiesexpanded.util.FormattingTools.singularOrPlural;

@Getter
@Setter
public class PirateBountyEntity implements BountyEntity {
    private final String pirateBountyIcon = "bountiesExpanded_placeholder";
    private final int baseReward;
    private final int level;
    private final float fleetQuality;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction = Global.getSector().getFaction(Factions.PIRATES);
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI targetedPerson;
    private final SectorEntityToken startingPoint;
    private final SectorEntityToken endingPoint = null;
    private final MissionHandler missionHandler;
    private float targetRepBeforeBattle;
    private PirateBountyIntel bountyIntel;

    public PirateBountyEntity(int baseReward, int level, float fleetQuality, Difficulty difficulty, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI targetedPerson, SectorEntityToken startingPoint, MissionHandler missionHandler) {
        this.baseReward = baseReward;
        this.level = level;
        this.fleetQuality = fleetQuality;
        this.difficulty = difficulty;
        this.offeringFaction = offeringFaction;
        this.fleet = fleet;
        this.targetedPerson = targetedPerson;
        this.startingPoint = startingPoint;
        this.missionHandler = missionHandler;
    }

    @Override
    public BaseEventManager getBountyManager() {
        return PirateBountyManager.getInstance();
    }

    @Override
    public PersonAPI getOfferingPerson() {
        return null;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = mode == ListInfoMode.IN_DESC ? 10f : 3f;
        float bulletPadding = mode == ListInfoMode.IN_DESC ? 3f : 0f;
        BountyResult result = baseBountyIntel.getResult();
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);
        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        baseBountyIntel.bullet(info);

        if (isNull(result)) {
            info.addPara("Reward: %s", initPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
            info.addPara("Target: %s", bulletPadding, bulletColor, targetedFaction.getBaseUIColor(), targetedPerson.getNameString());
            info.addPara("Time left: %s", bulletPadding, bulletColor, highlightColor, days + singularOrPlural(days, " day"));
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
        String objectiveText = missionHandler.getMissionType().getObjectiveText();
        String briefingText = String.format("A large sum has been put on the head of %s, wanted for %s countless crimes in %s space.\n\n" +
                        "To collect this bounty, we need to %s.",
                targetedPerson.getNameString(), getTargetedPerson().getHisOrHer(), offeringFaction.getDisplayName(), objectiveText);
        Color highlightColor = Misc.getHighlightColor();
        Color offeringFactionColor = offeringFaction.getBaseUIColor();
        Color targetedFactionColor = targetedFaction.getBaseUIColor();
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        if (isNull(result)) {
            TooltipAPIUtils.addCustomImagesWithSingleRepBar(info, width, opad, 10f,
                    targetedPerson.getPortraitSprite(),
                    targetedFaction.getLogo(), targetedFaction.getRelToPlayer().getRel());
            info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad, new Color[]{targetedFactionColor, offeringFactionColor}, targetedPerson.getNameString(), offeringFaction.getDisplayNameWithArticle());

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            DescriptionUtils.generateHideoutDescription(info, baseBountyIntel, highlightColor);
            DescriptionUtils.generateFancyFleetDescription(info, opad, fleet, targetedPerson);

            info.addSectionHeading("Fleet Intel", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, isRetrievalMission ? 0f : opad);
            DescriptionUtils.generateShipListForIntel(info, width, opad, fleet, 14, 2, true);
            DescriptionUtils.addDifficultyText(info, opad, difficulty);
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    float targetRepChange = result.targetRepAfterBattle - targetRepBeforeBattle;
                    TooltipAPIUtils.addFactionFlagsWithRepChange(info, width, opad, 10f,
                            offeringFaction, result.rep.delta,
                            targetedFaction, targetRepChange);
                    info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    TooltipAPIUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);
                    info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("This mission is no longer on offer.", opad);
                    break;
            }
        }
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
                    return "Pirate Bounty - Completed";
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    return "Pirate Bounty - Failed";
            }
        }
        return String.format("Pirate Bounty - %s", missionHandler.getMissionType().getMissionTypeUCFirst());
    }
}
