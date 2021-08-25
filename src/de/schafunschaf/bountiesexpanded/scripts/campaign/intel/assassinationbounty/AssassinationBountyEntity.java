package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.assassinationbounty;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResult;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResultType;

import java.awt.*;
import java.util.List;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public class AssassinationBountyEntity implements BountyEntity {
    private String activity;
    private final int bountyCredits;
    private final FactionAPI offeringFaction;
    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken hideout;

    public AssassinationBountyEntity(int bountyCredits, FactionAPI offeringFaction, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken hideout) {
        this.bountyCredits = bountyCredits;
        this.offeringFaction = offeringFaction;
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.hideout = hideout;
        this.activity = fleet.getAI().getCurrentAssignmentType().getDescription().toLowerCase();
        this.activity = this.activity.replaceAll("system", "around");
    }

    @Override
    public FactionAPI getOfferingFaction() {
        return offeringFaction;
    }

    @Override
    public CampaignFleetAPI getFleet() {
        return null;
    }

    @Override
    public PersonAPI getPerson() {
        return null;
    }

    @Override
    public SectorEntityToken getHideout() {
        return null;
    }

    @Override
    public String getIcon() {
        return offeringFaction.getLogo();
    }

    @Override
    public String getTitle(BountyResult result) {
        return null;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getBountyCredits() {
        return 0;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        float pad = 3f;
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
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;

        info.addImage(getIcon(), width, 128, opad);

        String has = offeringFaction.getDisplayNameHasOrHave();
        Color[] colors = {offeringFaction.getColor(), targetedFaction.getColor()};

        info.addPara("%s officials are offering a bounty for the disruption of a hostile %s military operation.", opad,
                colors,
                Misc.ucFirst(offeringFaction.getDisplayName()),
                targetedFaction.getDisplayNameWithArticleWithoutArticle());

        if (isNotNull(result)) {
            if (result.type == BountyResultType.END_PLAYER_BOUNTY) {
                info.addPara("You have successfully disrupted the military operation.", opad);
            } else {
                info.addPara("This bounty is no longer on offer.", opad);
            }
        }

        addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);
        if (isNull(result)) {
            String isOrWas = isNull(fleet.getAI().getCurrentAssignmentType()) ? "was last seen " : "is ";
            info.addPara(
                    "The fleet " + isOrWas + activity + " " + hideout.getName() + " in the "
                            + hideout.getStarSystem().getName() + ".",
                    10f, Misc.getHighlightColor(), hideout.getName(), hideout.getStarSystem().getName());

            List<FleetMemberAPI> fleetMemberList = fleet.getFleetData().getMembersListCopy();
            int cols = 7;
            int rows = fleetMemberList.size() / cols + 1;
            float iconSize = width / cols;
            info.addPara("Since this is an official military operation it was easy to get our hands on their fleet composition.", 10f);
            info.addShipList(cols, rows, iconSize, baseBountyIntel.getFactionForUIColors().getBaseUIColor(), fleetMemberList, 10f);
        }
    }
}