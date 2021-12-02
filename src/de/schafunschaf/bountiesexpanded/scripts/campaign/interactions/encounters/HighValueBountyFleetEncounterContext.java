package de.schafunschaf.bountiesexpanded.scripts.campaign.interactions.encounters;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Random;

@Log4j
public class HighValueBountyFleetEncounterContext extends FleetEncounterContext {
    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI
            winningFleet, CampaignFleetAPI otherFleet) {
        log.info("BountiesExpanded: Triggering HighValueBountyFleetEncounterContext");

        List<FleetMemberAPI> recoverableShips = super.getRecoverableShips(battle, winningFleet, otherFleet);
        List<FleetMemberAPI> storyRecoverableShips = getStoryRecoverableShips();
        List<FleetMemberData> enemyCasualties = getDataFor(winningFleet).getEnemyCasualties();

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet))
            return recoverableShips;

        String bountyId = ((HighValueBountyEntity) otherFleet.getMemoryWithoutUpdate().get("$bountiesExpanded_highValueBounty")).getBountyId();
        HighValueBountyData bountyData = HighValueBountyManager.getInstance().getBounty(bountyId);
        String flagshipVariantId = bountyData.flagshipVariantId;
        ShipVariantAPI variant = Global.getSettings().getVariant(flagshipVariantId);

        if (variant.getHullSpec().getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE))
            return recoverableShips;

        boolean uniqueFlagshipDestroyed = false;

        for (FleetMemberData fleetMemberData : enemyCasualties) {
            FleetMemberAPI destroyedShip = fleetMemberData.getMember();
            if (flagshipVariantId.equals(destroyedShip.getVariant().getHullVariantId()))
                uniqueFlagshipDestroyed = true;
        }

        if (!uniqueFlagshipDestroyed)
            return recoverableShips;

        for (FleetMemberAPI recoverableShip : recoverableShips)
            if (recoverableShip.getVariant().getHullVariantId().equals(flagshipVariantId))
                return recoverableShips;

        for (FleetMemberAPI recoverableShip : storyRecoverableShips)
            if (recoverableShip.getVariant().getHullVariantId().equals(flagshipVariantId))
                return recoverableShips;

        if (!(new Random().nextFloat() <= bountyData.chanceToAutoRecover))
            return recoverableShips;

        FleetMemberAPI flagship = otherFleet.getFlagship();
        if (ComparisonTools.isNull(flagship) || !variant.equals(flagship.getVariant())) {
            flagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
            flagship.setShipName(bountyData.flagshipName);
        }

        float dp = flagship.getBaseDeployCost();
        int num = (int) (dp * 100 / 5f);
        if (num < 4) num = 4;

        DModManager.addDMods(flagship, true, num, null);
        if (DModManager.getNumDMods(variant) > 0)
            DModManager.setDHull(variant);

        float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
        float wingProb = Global.getSettings().getFloat("salvageWingProb");

        prepareShipForRecovery(flagship, false, true, true, weaponProb, wingProb, getSalvageRandom());

        storyRecoverableShips.add(flagship);

        return recoverableShips;
    }
}
