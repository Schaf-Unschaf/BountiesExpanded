package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.market.MarketUtils;
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
    private final String pirateBountyIcon;
    private final int baseReward;
    private final int level;
    private final float fleetQuality;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction;
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI targetedPerson;
    private final SectorEntityToken spawnLocation;
    private final SectorEntityToken travelDestination = null;
    private final MissionHandler missionHandler;
    private float targetRepBeforeBattle;
    private PirateBountyIntel bountyIntel;

    private final String pirateJob = (String) CollectionUtils.getRandomEntry(NameStringCollection.pirateJobs);
    private final String piratePersonality = (String) CollectionUtils.getRandomEntry(NameStringCollection.piratePersonalities);
    private final String killWord = (String) CollectionUtils.getRandomEntry(NameStringCollection.killWords);
    private final String pirateTitle;

    public PirateBountyEntity(int baseReward, int level, float fleetQuality, Difficulty difficulty, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI targetedPerson, SectorEntityToken spawnLocation, MissionHandler missionHandler) {
        this.baseReward = baseReward;
        this.level = level;
        this.fleetQuality = fleetQuality;
        this.difficulty = difficulty;
        this.offeringFaction = offeringFaction;
        this.targetedFaction = Global.getSector().getFaction(Factions.PIRATES);
        this.fleet = fleet;
        this.targetedPerson = targetedPerson;
        this.spawnLocation = spawnLocation;
        this.missionHandler = missionHandler;
        this.pirateBountyIcon = fleet.getMemoryWithoutUpdate().contains(RareFlagshipManager.RARE_FLAGSHIP_KEY) ? "bountiesExpanded_pirate_silly" : "bountiesExpanded_pirate";
        String title = (String) CollectionUtils.getRandomEntry(NameStringCollection.pirateTitles);
        String market;
        MarketAPI factionMarket = MarketUtils.getRandomFactionMarket(offeringFaction);

        if (isNull(factionMarket))
            market = "NullPointerException";
        else
            market = factionMarket.getName();

        this.pirateTitle = Math.random() <= 0.5f
                ? String.format("%s of %s", title, market)
                : String.format("%s %s", market, title);
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
        String targetName = targetedPerson.getNameString();
        String himOrHerself = targetedPerson.getHimOrHer() + "self";
        String offeringFactionName = offeringFaction.getDisplayNameWithArticle();
        String briefingText = String.format("%s, also known as the %s, has proven %s as a big enough annoyance for %s which can't be ignored any longer.\n\n" +
                        "A bounty was now offered for the %s of that %s %s.",
                targetName, pirateTitle, himOrHerself, offeringFactionName,
                killWord, piratePersonality, pirateJob);
        Color highlightColor = Misc.getHighlightColor();
        Color offeringFactionColor = offeringFaction.getBaseUIColor();
        Color targetedFactionColor = targetedFaction.getBaseUIColor();
        Color[] highlightColors = new Color[]{targetedFactionColor, targetedFactionColor, offeringFactionColor};
        String[] highlightStrings = new String[]{targetName, pirateTitle, offeringFactionName};

        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;
        int maxShipsOnIntel = 14;
        boolean showShipsRemaining = fleet.getNumShips() > maxShipsOnIntel;

        if (isNull(result)) {
            TooltipAPIUtils.addCustomImagesWithSingleRepBar(info, width, opad, 10f,
                    targetedPerson.getPortraitSprite(),
                    targetedFaction.getLogo(), targetedFaction.getRelToPlayer().getRel());
            info.addSectionHeading("Briefing", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad, highlightColors, highlightStrings);

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            DescriptionUtils.generateFakeHideoutDescription(info, baseBountyIntel, opad);
            DescriptionUtils.generateFancyFleetDescription(info, opad, fleet, targetedPerson);

            info.addSectionHeading("Fleet Intel", baseBountyIntel.getFactionForUIColors().getBaseUIColor(), baseBountyIntel.getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            DescriptionUtils.generateShipListForIntel(info, width, opad, fleet, maxShipsOnIntel, 2, showShipsRemaining);
            DescriptionUtils.generateThreatDescription(info, fleet, opad);
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
        return Global.getSettings().getSpriteName("intel", pirateBountyIcon);
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
        return String.format("Pirate Bounty - %s", targetedPerson.getNameString());
    }
}
