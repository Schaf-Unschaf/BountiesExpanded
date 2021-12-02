package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.shipretrieval;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.util.FormattingTools;
import de.schafunschaf.bountiesexpanded.util.ShipPaymentPair;

import java.util.List;
import java.util.Map;

public class RetrievalMissionIntel extends BEBaseMissionIntel {
    public static String RETRIEVAL_IMPORTANT_REASON = "bountiesExpanded_retrievalDelivery";
    public static String RETRIEVAL_CONTACT_KEY = "$bountiesExpanded_retrievalContact";

    private final RetrievalMissionEntity missionEntity;

    public RetrievalMissionIntel(RetrievalMissionEntity missionEntity) {
        super(missionEntity);
        this.missionEntity = missionEntity;
        setImportant(true);

        missionEntity.setMissionIntel(this);
    }

    @Override
    public void reportMadeVisibleToPlayer() {
        missionAccepted();
    }

    @Override
    public void missionAccepted() {
        market.addPerson(contact);
        market.getCommDirectory().addPerson(contact);
        contact.getMemoryWithoutUpdate().set(RETRIEVAL_CONTACT_KEY, this);

        Misc.makeImportant(contact, RETRIEVAL_IMPORTANT_REASON, duration);
        Misc.makeImportant(market.getPrimaryEntity(), RETRIEVAL_IMPORTANT_REASON, duration);

        result.setMissionState(MissionState.ACCEPTED);
    }

    @Override
    public void advanceMission(float amount) {

    }

    @Override
    public void endMission() {
        Misc.makeUnimportant(contact, RETRIEVAL_IMPORTANT_REASON);
        Misc.makeUnimportant(market.getPrimaryEntity(), RETRIEVAL_IMPORTANT_REASON);
        contact.getMemoryWithoutUpdate().unset(RETRIEVAL_CONTACT_KEY);
        market.getCommDirectory().removePerson(contact);
        market.removePerson(contact);
        endAfterDelay();
    }

    @Override
    protected MissionResult createTimeRanOutFailedResult() {
        triggerMissionStatusUpdate(MissionState.FAILED, null);
        return null;
    }

    @Override
    protected MissionResult createAbandonedResult(boolean withPenalty) {
        triggerMissionStatusUpdate(MissionState.ABANDONED, null);
        return null;
    }

    @Override
    public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action = params.get(0).getString(memoryMap);

        switch (action) {
            case "hasShipInFleet":
                return hasShipInFleet();
            case "hasShipDialog":
                hasShipDialog(dialog);
                break;
            case "deliverShip":
                deliverShip(dialog);
                break;
            case "keepShip":
                keepShip(dialog);
                break;
        }

        return true;
    }

    protected boolean hasShipInFleet() {
        List<FleetMemberAPI> playerFleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
        return playerFleetMembers.contains(missionEntity.getShipWithPayment().ship);
    }

    protected void hasShipDialog(InteractionDialogAPI dialog) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        String shipName = missionEntity.getShipWithPayment().getShip().getShipName();
        String dialogText = String.format("\"That's great news! Thank you very much for retrieving the %s and bringing her back safely.\"\n\n" +
                "\"Now, if you would be so kind and hand over the access codes so my men can inspect the ship..\"", shipName);

        textPanel.addPara(dialogText, Misc.getHighlightColor(), shipName);
    }

    protected void deliverShip(InteractionDialogAPI dialog) {
        if (!hasShipInFleet())
            return;

        TextPanelAPI textPanel = dialog.getTextPanel();
        String personNameWithRank = contact.getRank() + " " + contact.getName().getLast();
        String hisOrHer = contact.getHisOrHer();
        String dialogTextPartOne = String.format("\"Excellent. Thank you very much. Here's your payment and I'll make sure your service won't be forgotten.\"\n\n" +
                "You can see a big grin on " + hisOrHer + " face right before the comm link gets cut. An hour later, the well-known and ear-soothing voice of a female KI sounds.", hisOrHer);
        String dialogTextPartTwo = String.format("Another mission done, another paycheck earned. But why did %s want that piece of junk so bad?\n\n" +
                "Well, it's too late for regrets now and credits are credits, right?", personNameWithRank);

        textPanel.addPara(dialogTextPartOne);
        textPanel.setFontSmallInsignia();
        textPanel.addPara("\n\"%s\"\n", Misc.getHighlightColor(), "Credits received");
        textPanel.setFontInsignia();
        textPanel.addPara(dialogTextPartTwo, contact.getFaction().getColor(), personNameWithRank);

        ShipPaymentPair<FleetMemberAPI, Integer> shipWithPayment = missionEntity.getShipWithPayment();
        FleetDataAPI playerFleetFleetData = Global.getSector().getPlayerFleet().getFleetData();

        AddRemoveCommodity.addFleetMemberLossText(shipWithPayment.ship, textPanel);
        playerFleetFleetData.removeFleetMember(shipWithPayment.ship);
        shipWithPayment.setConditionFulfilled(true);
        result.setPayment(FormattingTools.roundWholeNumber(missionEntity.getPayoutForShip(), 2));

        triggerMissionStatusUpdate(MissionState.COMPLETED, dialog);
    }

    protected void keepShip(InteractionDialogAPI dialog) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        String personNameWithRank = contact.getRank() + " " + contact.getName().getLast();
        String heOrShe = contact.getHeOrShe();
        String playerName = Global.getSector().getPlayerPerson().getNameString();
        String dialogTextPartOne = String.format("\"You are doing WHAT?! I'll make sure that you will regret that decision, %s!\"", playerName);
        String dialogTextPartTwo = String.format("Before " + heOrShe + " cuts off the comm link, you get some more harsh words, swearing, insults and threats thrown at you.\n" +
                "%s doesn't seem to be very pleased about this outcome.\n\n" +
                "Nonetheless you order your crew to get your new acquisition ready for takeoff and prepare leave the station as soon as possible.", personNameWithRank);

        textPanel.addPara(dialogTextPartOne, Global.getSector().getPlayerFaction().getBaseUIColor(), playerName);
        textPanel.addPara(dialogTextPartTwo, contact.getFaction().getColor(), personNameWithRank);

        triggerMissionStatusUpdate(MissionState.CANCELLED, dialog);
    }
}