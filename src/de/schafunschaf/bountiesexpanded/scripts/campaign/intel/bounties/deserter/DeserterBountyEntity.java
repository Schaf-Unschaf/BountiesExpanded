package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter;

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
import de.schafunschaf.bountiesexpanded.helper.text.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.helper.ui.TooltipAPIUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
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
public class DeserterBountyEntity implements BountyEntity {
    private String deserterBountyIcon;
    private String deserterBountyFlag;
    private final int baseReward;
    private final int level;
    private final float fleetQuality;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction;
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI targetedPerson;
    private final FleetMemberAPI flagship;
    private final SectorEntityToken spawnLocation;
    private final SectorEntityToken travelDestination;
    private final MissionHandler missionHandler;
    private float targetRepBeforeBattle;
    private DeserterBountyIntel bountyIntel;

    public DeserterBountyEntity(int baseReward, int level, float fleetQuality, Difficulty difficulty, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI targetedPerson, SectorEntityToken spawnLocation, SectorEntityToken travelDestination, MissionHandler missionHandler) {
        this.baseReward = baseReward;
        this.level = level;
        this.fleetQuality = fleetQuality;
        this.difficulty = difficulty;
        this.targetedFaction = offeringFaction;
        this.offeringFaction = offeringFaction;
        this.fleet = fleet;
        this.targetedPerson = targetedPerson;
        this.flagship = fleet.getFleetData().getMemberWithCaptain(targetedPerson);
        this.spawnLocation = spawnLocation;
        this.travelDestination = travelDestination;
        this.missionHandler = missionHandler;
        this.deserterBountyIcon = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY) ? "bountiesExpanded_deserter_crest_silly" : "bountiesExpanded_deserter_crest";
        this.deserterBountyFlag = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY) ? "bountiesExpanded_deserter_flag_silly" : "bountiesExpanded_deserter_flag";
    }

    @Override
    public BaseEventManager getBountyManager() {
        return DeserterBountyManager.getInstance();
    }

    @Override
    public PersonAPI getOfferingPerson() {
        return null;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        float bulletPadding = mode == ListInfoMode.IN_DESC ? 3f : 0f;
        BountyResult result = baseBountyIntel.getResult();
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);
        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        baseBountyIntel.bullet(info);

        if (isNull(result)) {
            info.addPara("Offered by: %s", initPad, bulletColor, offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
            info.addPara("Reward: %s", bulletPadding, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
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
        String hisOrHer = getTargetedPerson().getHisOrHer();
        String briefingText = String.format("A large sum has been put on the head of %s, wanted dead for %s recent theft of military equipment and betrayal of %s.\n\n" +
                        "To claim this bounty, we need to end %s life by destroying the %s.",
                targetedPerson.getNameString(), hisOrHer, offeringFaction.getDisplayNameWithArticle(), hisOrHer, flagship.getShipName());
        Color factionColor = baseBountyIntel.getFactionForUIColors().getBaseUIColor();
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;
        int maxShipsOnIntel = 14;
        boolean showShipsRemaining = fleet.getNumShips() > maxShipsOnIntel;

        if (isNull(result)) {
            TooltipAPIUtils.addCustomImagesWithSingleRepBar(info, width, opad, 10f,
                    targetedPerson.getPortraitSprite(),
                    Global.getSettings().getSpriteName("intel", deserterBountyFlag), offeringFaction.getRelToPlayer().getRel());
            info.addSectionHeading("Briefing", factionColor, baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad, factionColor, targetedPerson.getNameString(), offeringFaction.getDisplayNameWithArticle(), flagship.getShipName());

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            if (fleet.getContainingLocation() == travelDestination.getContainingLocation())
                DescriptionUtils.generatePatrolDescription(info, baseBountyIntel, opad, false);
            else
                DescriptionUtils.generateFakeTravelDescription(info, baseBountyIntel, opad);

            DescriptionUtils.generateFancyFleetDescription(info, opad, fleet, targetedPerson);

            info.addSectionHeading("Fleet Intel", factionColor, baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, isRetrievalMission ? 0f : opad);
            DescriptionUtils.generateShipListForIntel(info, width, opad, fleet, maxShipsOnIntel, 2, showShipsRemaining);
            DescriptionUtils.addDifficultyText(info, opad, difficulty);
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String debriefingText = "Mission completed. You brought %s to justice.";

                    TooltipAPIUtils.addCustomImagesWithSingleRepBarAndChange(info, width, opad, 10f,
                            offeringFaction.getLogo(),
                            Global.getSettings().getSpriteName("intel", deserterBountyFlag), offeringFaction.getRelToPlayer().getRel(), result.rep.delta);
                    info.addSectionHeading("Briefing", factionColor, baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara(debriefingText, opad, factionColor, targetedPerson.getRank() + " " + targetedPerson.getNameString());
                    baseBountyIntel.bullet(info);
                    info.addPara("%s Credits received", opad, Misc.getHighlightColor(), Misc.getDGSCredits(result.payment));
                    baseBountyIntel.unindent(info);
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    TooltipAPIUtils.addCustomImagesWithSingleRepBar(info, width, opad, 10f,
                            targetedPerson.getPortraitSprite(),
                            Global.getSettings().getSpriteName("intel", deserterBountyFlag), offeringFaction.getRelToPlayer().getRel());
                    info.addSectionHeading("Briefing", factionColor, baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("This mission is no longer on offer.", opad);
                    break;
            }
        }
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", deserterBountyIcon);
    }

    @Override
    public String getTitle(BountyResult result) {
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    return "Deserter Bounty - Completed";
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    return "Deserter Bounty - Failed";
            }
        }
        return String.format("Deserter Bounty - %s", targetedPerson.getNameString());
    }
}
