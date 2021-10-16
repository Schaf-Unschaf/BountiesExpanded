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

public class HostileFactionPicker {
    public static FactionAPI pickFaction(FactionAPI faction, boolean includePlayer, boolean considerPlayerRep) {
        return pickFaction(faction, includePlayer, null, considerPlayerRep);
    }

    public static FactionAPI pickFaction(FactionAPI faction, boolean includePlayer, Set<String> blacklist, boolean considerPlayerRep) {
        List<FactionAPI> factionList = new ArrayList<>();

        for (FactionAPI checkedFaction : Global.getSector().getAllFactions()) {
            if (!includePlayer && checkedFaction.isPlayerFaction())
                continue;
            if (!isNullOrEmpty(blacklist))
                if (blacklist.contains(checkedFaction.getId()))
                    continue;
            factionList.add(checkedFaction);
        }
        return pickFactionFromList(faction, factionList, considerPlayerRep);
    }

    public static FactionAPI pickParticipatingFaction(FactionAPI faction, Set<String> blacklist, boolean considerPlayerRep) {
        List<FactionAPI> factionList = new ArrayList<>();

        for (String factionID : BountyEventData.getParticipatingFactions()) {
            if (!isNullOrEmpty(blacklist))
                if (blacklist.contains(factionID))
                    continue;
            factionList.add(Global.getSector().getFaction(factionID));
        }
        return pickFactionFromList(faction, factionList, considerPlayerRep);
    }

    private static FactionAPI pickFactionFromList(FactionAPI faction, List<FactionAPI> factionList, boolean considerPlayerRep) {
        if (isNull(faction) || isNullOrEmpty(factionList))
            return null;
        WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<>();
        for (FactionAPI checkedFaction : factionList) {
            if (isNull(checkedFaction))
                continue;

            float weight = 1f;
            float relationship = checkedFaction.getRelationship(faction.getId());

            float minRep = -RepLevel.HOSTILE.getMin();
            if (relationship >= minRep)
                continue;

            if (considerPlayerRep)
                weight -= checkedFaction.getRelToPlayer().getRel();
            else
                weight -= relationship;

            if (weight > 0f)
                picker.add(checkedFaction, weight);
        }
        return picker.pick();
    }
}
