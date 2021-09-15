package de.schafunschaf.bountiesexpanded.helper.faction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class FriendlyFactionPicker {
    public static FactionAPI pickFaction(FactionAPI faction, boolean includePlayer) {
        return pickFaction(faction, includePlayer, null);
    }

    public static FactionAPI pickFaction(FactionAPI faction, boolean includePlayer, Set<String> blacklist) {
        List<FactionAPI> factionList = new ArrayList<>();

        for (FactionAPI checkedFaction : Global.getSector().getAllFactions()) {
            if (!includePlayer && checkedFaction.isPlayerFaction())
                continue;
            if (!isNullOrEmpty(blacklist))
                if (blacklist.contains(checkedFaction.getId()))
                    continue;
            factionList.add(checkedFaction);
        }
        return pickFactionFromList(faction, factionList);
    }

    public static FactionAPI pickParticipatingFaction(FactionAPI faction, Set<String> blacklist) {
        List<FactionAPI> factionList = new ArrayList<>();

        for (String factionID : BountyEventData.getParticipatingFactions()) {
            if (!isNullOrEmpty(blacklist))
                if (blacklist.contains(factionID))
                    continue;
            factionList.add(Global.getSector().getFaction(factionID));
        }
        return pickFactionFromList(faction, factionList);
    }

    private static FactionAPI pickFactionFromList(FactionAPI faction, List<FactionAPI> factionList) {
        if (isNull(faction) || isNullOrEmpty(factionList))
            return null;
        WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<>();
        for (FactionAPI checkedFaction : factionList) {
            if (isNull(checkedFaction))
                continue;
            float relationship = checkedFaction.getRelationship(faction.getId());
            if (relationship > RepLevel.FRIENDLY.getMin())
                picker.add(checkedFaction, relationship);
        }
        return picker.pick();
    }
}
