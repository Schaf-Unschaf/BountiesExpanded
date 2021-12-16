package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.market.MarketUtils;
import de.schafunschaf.bountiesexpanded.helper.mission.MissionTextUtils;
import de.schafunschaf.bountiesexpanded.helper.text.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.helper.ui.TooltipAPIUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.NameStringCollection;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import de.schafunschaf.bountiesexpanded.util.CollectionUtils;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

import static com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.FormattingTools.singularOrPlural;

@Getter
@Setter
public class WarCriminalEntity implements BountyEntity {
    private final String warCriminalIcon;
    private final int baseReward;
    private final int level;
    private final float fleetQuality;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction;
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI targetedPerson;
    private final SectorEntityToken spawnLocation;
    private final SectorEntityToken dropOffLocation;
    private final MissionHandler missionHandler;
    private PersonAPI offeringPerson;
    private FleetMemberAPI retrievalTargetShip;
    private float targetRepBeforeBattle;
    private WarCriminalIntel bountyIntel;

    private final String killWord = (String) CollectionUtils.getRandomEntry(NameStringCollection.killWords);
    private final String crimeReason = (String) CollectionUtils.getRandomEntry(NameStringCollection.crimeReasons);
    private final String crimeType = (String) CollectionUtils.getRandomEntry(NameStringCollection.crimeTypes);
    private final String crimeVictim = (String) CollectionUtils.getRandomEntry(NameStringCollection.crimeVictims);
    private String crimeMarketName = "[REDACTED]";

    public WarCriminalEntity(int baseReward, int level, float fleetQuality, Difficulty difficulty, FactionAPI targetedFaction, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI targetedPerson, SectorEntityToken spawnLocation, SectorEntityToken dropOffLocation, MissionHandler missionHandler) {
        this.baseReward = baseReward;
        this.level = level;
        this.fleetQuality = fleetQuality;
        this.difficulty = difficulty;
        this.targetedFaction = targetedFaction;
        this.offeringFaction = offeringFaction;
        this.fleet = fleet;
        this.targetedPerson = targetedPerson;
        this.spawnLocation = spawnLocation;
        this.dropOffLocation = dropOffLocation;
        this.missionHandler = missionHandler;
        this.offeringPerson = offeringFaction.createRandomPerson();
        this.warCriminalIcon = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY) ? "bountiesExpanded_warcriminal_silly" : "bountiesExpanded_warcriminal";
        MarketAPI randomFactionMarket = MarketUtils.getRandomFactionMarket(offeringFaction);
        if (isNotNull(randomFactionMarket))
            this.crimeMarketName = randomFactionMarket.getName();
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
        float bulletPadding = mode == ListInfoMode.IN_DESC ? 3f : 0f;
        BountyResult result = baseBountyIntel.getResult();
        float duration = baseBountyIntel.getDuration();
        float elapsedDays = baseBountyIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);
        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        baseBountyIntel.bullet(info);

        if (isNull(result)) {
            info.addPara("Target: %s", initPad, bulletColor, targetedFaction.getBaseUIColor(), targetedPerson.getNameString());
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
        boolean hasRareFlagship = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY);
        String targetRankAndName = targetedPerson.getRank() + " " + targetedPerson.getNameString();
        String hisOrHerOffering = offeringPerson.getHisOrHer();
        String hisOrHerTargeted = targetedPerson.getHisOrHer();
        String heOrSheTargeted = targetedPerson.getHeOrShe();
        String offeringFactionUCFirst = Misc.ucFirst(offeringFaction.getDisplayNameWithArticle());
        String shipClassAndDesignation = DescriptionUtils.generateShipClassWithDesignation(retrievalTargetShip, hasRareFlagship);
        String crimeReasonString = buildCrimeReasonString();
        Color highlightColor = Misc.getHighlightColor();
        Color offeringFactionColor = offeringFaction.getBaseUIColor();
        Color targetedFactionColor = targetedFaction.getBaseUIColor();
        String briefingText = isRetrievalMission
                ? String.format("%s has posted a reward for the %s of %s. %s is accused for stealing %s from a private collector.\n\n" +
                        "The mission contractor, %s, requires you to be especially careful with the recovery of %s ship. Any lasting damage done will reduce the payout.",
                offeringFactionUCFirst, killWord, targetRankAndName, Misc.ucFirst(heOrSheTargeted), shipClassAndDesignation, offeringPerson.getNameString(), hisOrHerOffering)
                : String.format("%s has posted a bounty for the %s of %s after multiple crimes against military and civilian personnel.\n\n" +
                        "The latest entry on the ever-growing list of misconducts was %s %s.",
                offeringFactionUCFirst, killWord, targetRankAndName, hisOrHerTargeted, crimeReasonString);
        Color[] highlightColors = isRetrievalMission
                ? new Color[]{offeringFactionColor, targetedFactionColor, highlightColor, offeringFactionColor}
                : new Color[]{offeringFactionColor, targetedFactionColor, offeringFactionColor};
        String[] highlightStrings = isRetrievalMission
                ? new String[]{offeringFactionUCFirst, targetRankAndName, shipClassAndDesignation, offeringPerson.getNameString()}
                : new String[]{offeringFactionUCFirst, targetRankAndName, crimeReasonString};

        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        if (isNull(result)) {
            TooltipAPIUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);
            info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad, highlightColors, highlightStrings);

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            DescriptionUtils.generateHideoutDescription(info, baseBountyIntel, highlightColor);
            DescriptionUtils.generateFancyFleetDescription(info, opad, fleet, targetedPerson);

            if (isRetrievalMission) {
                info.addSectionHeading("Target Intel", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                MissionTextUtils.generateRetrievalTargetSection(info, width, opad, retrievalTargetShip, targetedFaction);
            }

            info.addSectionHeading("Fleet Intel", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, isRetrievalMission ? 0f : opad);
            DescriptionUtils.generateShipListForIntel(info, width, opad, fleet, fleet.getNumShips(), 3, false);
            DescriptionUtils.addDifficultyText(info, opad, difficulty);
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    float targetRepChange = result.targetRepAfterBattle - targetRepBeforeBattle;

                    TooltipAPIUtils.addFactionFlagsWithRepChange(info, width, opad, opad, offeringFaction, result.rep.delta, targetedFaction, targetRepChange);
                    info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("You have successfully completed the mission.\n\n" +
                                    "Bring the %s back to %s in the %s and hand it over to %s for an additional reward.",
                            opad,
                            new Color[]{highlightColor, offeringFactionColor, offeringFactionColor, offeringFactionColor},
                            retrievalTargetShip.getShipName(),
                            dropOffLocation.getName(),
                            dropOffLocation.getStarSystem().getName(),
                            offeringPerson.getNameString());
                    MissionTextUtils.generateRetrievalConsequencesText(info, opad, retrievalTargetShip, offeringFaction, offeringPerson, missionHandler.getChanceForConsequences());
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    TooltipAPIUtils.addFactionFlagsWithRep(info, width, opad, opad, offeringFaction, targetedFaction);
                    info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara("This mission is no longer on offer.\n\n" +
                            "The wanted ship is either destroyed or someone else recovered it.", opad);
                    break;
            }
        }
    }

    @Override
    public SectorEntityToken getTravelDestination() {
        return dropOffLocation;
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

    private String buildCrimeReasonString() {
        if (isNull(crimeReason) || isNull(crimeType) || isNull(crimeVictim))
            return "ERROR BUILDING CRIME REASON";

        String returnString = crimeReason;

        returnString = returnString.replace("$faction", offeringFaction.getDisplayName());
        returnString = returnString.replace("$market", crimeMarketName);
        returnString = returnString.replace("$crime", crimeType);
        returnString = returnString.replace("$victim", crimeVictim);
        returnString = returnString.replace("$aOrAnFaction", FormattingTools.aOrAn(offeringFaction.getDisplayName()) + " " + offeringFaction.getDisplayName());

        return returnString;
    }
}
