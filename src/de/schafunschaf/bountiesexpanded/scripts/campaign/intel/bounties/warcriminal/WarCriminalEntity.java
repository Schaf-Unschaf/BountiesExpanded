package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.ui.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionType;
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
    private static final String WAR_CRIMINAL_ICON = "bountiesExpanded_assassination";
    private final int baseReward;
    private final int level;
    private final Difficulty difficulty;
    private final FactionAPI targetedFaction;
    private final FactionAPI offeringFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken startingPoint;
    private final MissionType missionType;
    private final int shipsToDestroy;
    private final int maxFleetSizeForCompletion;
    private float targetRepBeforeBattle = 0;
    private WarCriminalIntel intel;

    public WarCriminalEntity(int baseReward, int level, float fractionToKill, Difficulty difficulty, FactionAPI targetedFaction, FactionAPI offeringFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken startingPoint, MissionType missionType) {
        this.baseReward = baseReward;
        this.level = level;
        this.difficulty = difficulty;
        this.targetedFaction = targetedFaction;
        this.offeringFaction = offeringFaction;
        this.fleet = fleet;
        this.person = person;
        this.startingPoint = startingPoint;
        this.missionType = missionType;
        this.shipsToDestroy = Math.max((int) (fleet.getFleetData().getMembersListCopy().size() * fractionToKill), 1);
        this.maxFleetSizeForCompletion = fleet.getNumShips() - shipsToDestroy;
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
                baseBountyIntel.addDays(info, "remaining", days, bulletColor);
            } else {
                info.addPara("Offered by: %s", initPad, getOfferingFaction().getBaseUIColor(), getOfferingFaction().getDisplayName());
                info.addPara("Target: %s", 0, getTargetedFaction().getBaseUIColor(), getPerson().getRank() + " " + getPerson().getNameString());
                if (!baseBountyIntel.isEnding()) {
                    info.addPara(" %s remaining", 0f, bulletColor,
                            highlightColor, days + singularOrPlural(days, " day"));
                }
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String payout = Misc.getDGSCredits(result.payment);

                    if (mode != ListInfoMode.IN_DESC) {
                        info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                        CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, getOfferingFaction(), null,
                                null, null, info, bulletColor, isUpdate, initPad);
                    }
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_TIME:
                case END_OTHER:
            }
        }
    }

    @Override
    public void createSmallDescription(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, float width, float height) {
        String briefingText = "BRIEFING TEXT GOES HERE";
        Color highlightColor = Misc.getHighlightColor();
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        if (isNull(result)) {
            DescriptionUtils.addFactionFlagsWithRep(info, width, opad, opad, getOfferingFaction(), getTargetedFaction());
            info.addSectionHeading("Briefing", getOfferingFaction().getBaseUIColor(), getOfferingFaction().getDarkUIColor(), Alignment.MID, opad);
            info.addPara(briefingText, opad);

            addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

            DescriptionUtils.generateHideoutDescription(info, baseBountyIntel, highlightColor);

            info.addSectionHeading("Fleet Intel", getOfferingFaction().getBaseUIColor(), getOfferingFaction().getDarkUIColor(), Alignment.MID, opad);
            DescriptionUtils.createShipListForIntel(info, width, opad, getFleet(), getFleet().getNumShips(), 3, false);
            info.addPara("Your tactical officer classifies this fleet as " + getDifficulty().getShortDescriptionAnOrA() + " %s encounter.",
                    opad, getDifficulty().getColor(), getDifficulty().getShortDescription());
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    float targetRepChange = result.targetRepAfterBattle - getTargetRepBeforeBattle();

                    DescriptionUtils.addFactionFlagsWithRepChange(info, width, opad, opad, getOfferingFaction(), result.rep.delta, getTargetedFaction(), targetRepChange);
                    info.addSectionHeading("Briefing", getOfferingFaction().getBaseUIColor(), getOfferingFaction().getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, opad);

                    info.addPara("You have successfully completed the mission.", opad);
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    info.addPara("This mission is no longer on offer.", opad);
            }
        }
    }

    @Override
    public SectorEntityToken getEndingPoint() {
        return null;
    }

    @Override
    public String getIcon() {
        return WAR_CRIMINAL_ICON;
    }

    @Override
    public String getTitle(BountyResult result) {
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    return "Mission completed";
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_OTHER:
                case END_TIME:
                    return "Mission failed";
            }
        }
        return String.format("Military Contract - %s", getMissionType().getMissionTypeUCFirst());
    }
}
