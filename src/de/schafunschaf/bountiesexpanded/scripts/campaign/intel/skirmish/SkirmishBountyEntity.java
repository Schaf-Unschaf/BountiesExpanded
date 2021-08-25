package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.skirmish;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResult;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResultType;

import java.awt.*;
import java.util.List;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public class SkirmishBountyEntity implements BountyEntity {
    protected final int bountyCredits;
    protected final int level;
    protected final int baseShipBounty;

    protected final float fractionToKill;

    protected final FactionAPI offeringFaction;

    protected final FactionAPI targetedFaction;
    protected final CampaignFleetAPI fleet;
    protected final PersonAPI person;
    protected final SectorEntityToken hideout;

    public SkirmishBountyEntity(int bountyCredits, FactionAPI offeringFaction, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken hideout, float fractionToKill, int level) {
        this.bountyCredits = bountyCredits;
        this.fractionToKill = fractionToKill;
        this.offeringFaction = offeringFaction;
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.hideout = hideout;
        this.level = level;
        this.baseShipBounty = Math.round((int) (Settings.SKIRMISH_BASE_SHIP_BOUNTY * (1 - fractionToKill)) / 10f) * 10;
    }

    protected float getFractionToKill() {
        return fractionToKill;
    }

    public FactionAPI getOfferingFaction() {
        return offeringFaction;
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
    public SectorEntityToken getHideout() {
        return hideout;
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
    public int getLevel() {
        return level;
    }

    @Override
    public int getBountyCredits() {
        return bountyCredits;
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
                info.addPara("%s reward", initPad, bulletColor, highlightColor, Misc.getDGSCredits(bountyCredits));
                int days = Math.max((int) (duration - elapsedDays), 1);
                baseBountyIntel.addDays(info, "remaining", days, bulletColor);
            } else {
                info.addPara("Offered by: " + offeringFaction.getDisplayName(), initPad, bulletColor,
                        offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
                info.addPara("Target: " + targetedFaction.getDisplayName(), 0f, bulletColor,
                        targetedFaction.getBaseUIColor(), targetedFaction.getDisplayName());

                if (!baseBountyIntel.isEnding()) {
                    int days = (int) (duration - elapsedDays);
                    String daysStr = "days";
                    if (days <= 1) {
                        days = 1;
                        daysStr = "day";
                    }

                    info.addPara("%s reward, %s " + daysStr + " remaining", 0f, bulletColor,
                            highlightColor, Misc.getDGSCredits(bountyCredits), "" + days);
                }
            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    info.addPara("%s received", initPad, bulletColor, highlightColor, Misc.getDGSCredits(result.payment));
                    if (result.share < 1f)
                        info.addPara("%s participation", initPad, bulletColor, highlightColor, (int) (result.share * 100) + "%");
                    CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, 0f);
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
        String creditsPerSize = baseShipBounty + "/" + baseShipBounty * 2 + "/" + baseShipBounty * 3 + "/" + baseShipBounty * 5;

        info.addImages(width, 100, opad, 10f, offeringFaction.getLogo(), targetedFaction.getLogo());
        info.addPara("%s officials have offered a reward for beating-up a hostile %s fleet.", opad,
                factionColors,
                Misc.ucFirst(offeringFaction.getDisplayName()),
                targetedFaction.getDisplayNameWithArticleWithoutArticle());

        if (isNotNull(result)) {
            if (result.type == BountyResultType.END_PLAYER_BOUNTY) {
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

            List<FleetMemberAPI> fleetMemberList = fleet.getFleetData().getMembersListCopy();
            int shipsToDestroy = (int) (fleetMemberList.size() * fractionToKill);
            int cols = 7;
            int rows = (int) Math.ceil(fleetMemberList.size() / (float) cols);
            float iconSize = width / cols;
            String shipOrShips = shipsToDestroy > 1 ? "ships" : "ship";
            Color[] factionAndHighlightColors = {offeringFaction.getBaseUIColor(), highlightColor};
            info.addPara("Since this is an official military operation, %s transmitted a complete intel report.", 10f, offeringFaction.getBaseUIColor(), offeringFaction.getDisplayNameWithArticle());
            info.addShipList(cols, rows, iconSize, baseBountyIntel.getFactionForUIColors().getBaseUIColor(), fleetMemberList, 10f);
            info.addPara("To claim your bounty, %s demands the destruction of at least %s " + shipOrShips + ".", 10f, factionAndHighlightColors, offeringFaction.getDisplayNameWithArticle(), String.valueOf(shipsToDestroy));
            info.addPara("They will also pay an additional %s credits per kill as bonus on top of your reward.", 10f, highlightColor, creditsPerSize);
        }
    }
}