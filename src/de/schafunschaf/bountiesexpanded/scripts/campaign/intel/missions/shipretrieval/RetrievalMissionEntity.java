package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.helper.mission.MissionTextUtils;
import de.schafunschaf.bountiesexpanded.helper.ui.TooltipAPIUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.MissionEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.BEBaseMissionIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.BEMissionResult;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;
import de.schafunschaf.bountiesexpanded.util.ShipPaymentPair;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

import static com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;

/**
 * Retrieval Mission
 * This mission gets triggered after completing a 'Retrieval'-Type bounty.
 */
@Getter
@Setter
public class RetrievalMissionEntity implements MissionEntity {
    public static final String RETRIEVAL_DELIVERY_TARGET = "bountiesExpanded_retrievalDeliveryTarget";
    public static final String RETRIEVAL_CONTRACTOR_KEY = "$bountiesExpanded_retrievalContractor";
    public static final String RETRIEVAL_SHIP_KEY = "$bountiesExpanded_retrievalShip";

    private RetrievalMissionIntel missionIntel;
    private final String retrievalMissionIcon = "bountiesExpanded_retrieval";
    private final FactionAPI offeringFaction;
    private final FactionAPI targetedFaction;
    private final PersonAPI missionContact;
    private final String icon = getIconSprite();
    private final int baseReward;
    private final float chanceForConsequences;
    private final MarketAPI missionMarket;
    private final ShipPaymentPair<FleetMemberAPI, Integer> shipWithPayment;

    public RetrievalMissionEntity(BountyEntity bountyEntity, ShipPaymentPair<FleetMemberAPI, Integer> shipWithPayment) {
        this.offeringFaction = bountyEntity.getOfferingFaction();
        this.targetedFaction = bountyEntity.getTargetedFaction();
        this.missionContact = bountyEntity.getOfferingPerson();
        this.baseReward = bountyEntity.getBaseReward();
        this.chanceForConsequences = bountyEntity.getMissionHandler().getChanceForConsequences();
        this.missionMarket = bountyEntity.getTravelDestination().getMarket();
        this.shipWithPayment = shipWithPayment;
    }

    @Override
    public String getTitle(BEMissionResult result) {
        if (isNotNull(result)) {
            switch (result.getMissionState()) {
                case POSTED:
                case ACCEPTED:
                    return String.format("Retrieval Mission - Visit %s", missionContact.getNameString());
                case COMPLETED:
                    return "Retrieval Mission - Completed";
                case FAILED:
                    return "Retrieval Mission - Failed";
                case ABANDONED:
                case CANCELLED:
                    return "Retrieval Mission - Cancelled";
                default:
                    return "Retrieval Mission - Wrong MissionState";
            }
        }
        return "Retrieval Mission - Start";
    }

    @Override
    public void addBulletPoints(BEBaseMissionIntel missionIntel, TooltipMakerAPI info, ListInfoMode mode) {
        FleetMemberAPI ship = shipWithPayment.ship;
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = this.missionIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;
        float bulletPadding = mode == ListInfoMode.IN_DESC ? 3f : 0f;
        float duration = this.missionIntel.getDuration();
        float elapsedDays = this.missionIntel.getElapsedDays();
        int days = Math.max((int) (duration - elapsedDays), 1);
        String payoutForShip = Misc.getDGSCredits(FormattingTools.roundWholeNumber(getPayoutForShip(), 2));
        String numDMods = String.valueOf(DModManager.getNumDMods(ship.getVariant()));
        boolean isUpdate = missionIntel.getListInfoParam() != null;
        BEMissionResult missionResult = missionIntel.result;

        missionIntel.bullet(info);

        switch (missionResult.getMissionState()) {
            case POSTED:
            case ACCEPTED:
                info.addPara("Dock at %s, %s", initPad, bulletColor, missionMarket.getTextColorForFactionOrPlanet(),
                        missionMarket.getName(), missionMarket.getStarSystem().getName());
                info.addPara("Contact %s", bulletPadding, bulletColor, missionMarket.getTextColorForFactionOrPlanet(),
                        missionContact.getNameString());
                if (mode == ListInfoMode.IN_DESC)
                    info.addPara("%s reward for ship in current condition (%s D-Mods)", bulletPadding, bulletColor, highlightColor, payoutForShip, numDMods);

                missionIntel.addDays(info, "remaining", days, bulletColor, bulletPadding);
                break;
            case COMPLETED:
                String payout = Misc.getDGSCredits(missionResult.getPayment());

                if (mode != ListInfoMode.IN_DESC) {
                    info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                    CoreReputationPlugin.addAdjustmentMessage(missionResult.getOfferingFactionRepChange().delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, bulletPadding);
                }
                break;
            case CANCELLED:
            case FAILED:
            case ABANDONED:
                if (mode != ListInfoMode.IN_DESC) {
                    CoreReputationPlugin.addAdjustmentMessage(missionResult.getOfferingFactionRepChange().delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, initPad);
                }
                break;
        }

        missionIntel.unindent(info);
    }

    @Override
    public void createSmallDescription(BEBaseMissionIntel missionIntel, TooltipMakerAPI info, float width, float height) {
        FleetMemberAPI ship = shipWithPayment.ship;
        String[] briefingHighlights = {ship.getShipName(), missionContact.getNameString(), missionMarket.getName(), missionMarket.getStarSystem().getName()};
        String himOrHer = missionContact.getHimOrHer();
        String briefingText = String.format("You have successfully recovered the %s.\n\n" +
                        "The mission's contractor, %s, has contacted you after hearing the news and demands that you visit " + himOrHer + " on %s in the %s.\n\n" +
                        "Once docked, contact " + himOrHer + " and transfer the ship for an additional reward, depending on how much damage the hull has taken.",
                (Object[]) briefingHighlights);

        Color highlightColor = Misc.getHighlightColor();
        Color offeringFactionColor = offeringFaction.getBaseUIColor();
        BEMissionResult missionResult = missionIntel.getResult();
        float opad = 10f;

        if (isNotNull(missionResult)) {
            switch (missionResult.getMissionState()) {
                case POSTED:
                case ACCEPTED:
                    Color[] briefingColors = new Color[]{highlightColor, offeringFactionColor, offeringFactionColor, offeringFactionColor};

                    TooltipAPIUtils.addPersonWithFactionRepBar(info, width, opad, opad, missionContact);
                    info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, opad, briefingColors, briefingHighlights);

                    addBulletPoints(missionIntel, info, ListInfoMode.IN_DESC);

                    MissionTextUtils.generateRetrievalConsequencesText(info, opad, ship, offeringFaction, missionContact, chanceForConsequences);
                    break;
                case COMPLETED:
                    String[] completedHighlights = {ship.getShipName(), missionContact.getNameString(), missionContact.getFaction().getDisplayNameWithArticle()};
                    String completedText = String.format("You have successfully completed the mission.\n\n" +
                            "As promised, you delivered the %s to %s and avoided any diplomatic incidents with %s.", (Object[]) completedHighlights);
                    Color[] completedColors = new Color[]{highlightColor, offeringFactionColor, offeringFactionColor};

                    TooltipAPIUtils.addPersonWithFactionRepBarAndChange(info, width, opad, opad, missionContact, missionResult.getOfferingFactionRepChange().delta);
                    info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara(completedText, opad, completedColors, completedHighlights);
                    break;
                case CANCELLED:
                case FAILED:
                case ABANDONED:
                    String[] failedHighlights = {ship.getShipName(), missionContact.getNameString()};
                    String failedText = String.format("Mission failed.\n\n" +
                            "You decided to keep the %s for yourself or selling it, potentially upsetting %s...", (Object[]) failedHighlights);
                    Color[] failedColors = new Color[]{highlightColor, offeringFactionColor};

                    TooltipAPIUtils.addPersonWithFactionRepBarAndChange(info, width, opad, opad, missionContact, missionResult.getOfferingFactionRepChange().delta);
                    info.addSectionHeading("Briefing", offeringFactionColor, offeringFaction.getDarkUIColor(), Alignment.MID, opad);
                    info.addPara(briefingText, Misc.getGrayColor(), opad);

                    info.addPara(failedText, opad, failedColors, failedHighlights);
                    break;
            }
        }
    }

    private String getIconSprite() {
        return Global.getSettings().getSpriteName("intel", retrievalMissionIcon);
    }

    public int getPayoutForShip() {
        int shipValue = shipWithPayment.getPayment();
        int numDMods = DModManager.getNumDMods(shipWithPayment.getShip().getVariant());

        for (int i = 0; i < numDMods; i++)
            shipValue *= 0.7f;

        return shipValue;
    }
}
