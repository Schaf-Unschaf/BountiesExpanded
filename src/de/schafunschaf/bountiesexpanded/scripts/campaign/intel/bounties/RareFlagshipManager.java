package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.ExternalDataSupplier;
import de.schafunschaf.bountiesexpanded.plugins.BountiesExpandedPlugin;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Log4j
public class RareFlagshipManager {
    private static final Map<String, RareFlagshipData> rareFlagshipData = new HashMap<>();

    public static void loadRareFlagshipData() {
        rareFlagshipData.putAll(ExternalDataSupplier.loadRareFlagshipData(BountiesExpandedPlugin.RARE_FLAGSHIPS_FILE));
        log.info(String.format("BountiesExpanded: loaded %s rare flagships", rareFlagshipData.size()));
        for (Map.Entry<String, RareFlagshipData> rareFlagshipDataEntry : rareFlagshipData.entrySet()) {
            String flagshipID = rareFlagshipDataEntry.getValue().getFlagshipID();
            String flagshipVariantID = rareFlagshipDataEntry.getValue().getFlagshipVariantID();
            float weight = rareFlagshipDataEntry.getValue().getWeight();
            Set<String> factionID = rareFlagshipDataEntry.getValue().getFactionIDs();

            log.info(String.format("ID: '%s' || VariantID: '%s' || Weight: '%s' || FactionID: '%s'", flagshipID, flagshipVariantID, weight, factionID));
        }
    }

    public static Map<String, RareFlagshipData> getRareFlagshipData() {
        return new HashMap<>(rareFlagshipData);
    }

    public static RareFlagshipData getRareFlagship(String flagshipID) {
        return rareFlagshipData.get(flagshipID);
    }

    public static Set<String> getAllRareFlagships() {
        Set<String> allFlagshipIDs = new HashSet<>();
        for (Map.Entry<String, RareFlagshipData> entry : rareFlagshipData.entrySet())
            allFlagshipIDs.add(entry.getValue().getFlagshipVariantID());
        return allFlagshipIDs;
    }

    public static RareFlagshipData pickRareFlagship() {
        return pickRareFlagship(null, null, null);
    }

    public static RareFlagshipData pickRareFlagship(ShipAPI.HullSize maxShipSize) {
        return pickRareFlagship(null, null, maxShipSize);
    }

    public static RareFlagshipData pickRareFlagship(String factionID) {
        return pickRareFlagship(null, factionID, null);
    }

    public static RareFlagshipData pickRareFlagship(String factionID, ShipAPI.HullSize maxShipSize) {
        return pickRareFlagship(null, factionID, maxShipSize);
    }

    public static RareFlagshipData pickRareFlagship(String flagshipID, String factionID, ShipAPI.HullSize maxShipSize) {
        if (isNotNull(flagshipID))
            return getRareFlagship(flagshipID);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();

        Map<String, RareFlagshipData> rareFlagshipData = getRareFlagshipData();

        for (Map.Entry<String, RareFlagshipData> dataEntry : rareFlagshipData.entrySet()) {
            if (isNotNull(factionID))
                if (!dataEntry.getValue().getFactionIDs().contains(factionID))
                    continue;

            if (isNotNull(maxShipSize))
                if (!checkShipSize(maxShipSize, dataEntry))
                    continue;

            picker.add(dataEntry.getKey(), dataEntry.getValue().getWeight());
        }

        return getRareFlagship(picker.pick());
    }

    public static boolean replaceFlagship(CampaignFleetAPI fleet) {
        String factionID = fleet.getFaction().getId();
        PersonAPI fleetCommander = fleet.getCommander();
        ShipAPI.HullSize hullSizeFlagship = isNull(fleet.getFlagship()) ? ShipAPI.HullSize.CAPITAL_SHIP : fleet.getFlagship().getHullSpec().getHullSize();

        RareFlagshipData rareFlagshipData = RareFlagshipManager.pickRareFlagship(factionID, hullSizeFlagship);
        if (isNotNull(rareFlagshipData)) {
            ShipVariantAPI variant = Global.getSettings().getVariant(rareFlagshipData.getFlagshipVariantID());
            if (isNotNull(variant)) {
                FleetMemberAPI rareFlagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
                if (isNotNull(rareFlagship)) {
                    fleet.getFleetData().addFleetMember(rareFlagship);
                    fleet.getFleetData().setFlagship(rareFlagship);
                    fleet.getFlagship().setCaptain(fleetCommander);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkShipSize(ShipAPI.HullSize maxShipSize, Map.Entry<String, RareFlagshipData> dataEntry) {
        String flagshipVariantID = dataEntry.getValue().getFlagshipVariantID();
        float maxShipSizeNum = Misc.getSizeNum(maxShipSize);
        ShipVariantAPI flagshipVariant = Global.getSettings().getVariant(flagshipVariantID);
        if (isNull(flagshipVariant)) return false;
        float sizeNumFlagship = Misc.getSizeNum(flagshipVariant.getHullSpec().getHullSize());

        return sizeNumFlagship <= maxShipSizeNum;
    }
}
