package de.schafunschaf.bountiesexpanded.helper.faction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;

import java.util.Set;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNullOrEmpty;

public class ParticipatingFactionPicker {
    public static FactionAPI pickFaction(Set<String> blacklist) {
        WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<>();

        for (String factionID : BountyEventData.getParticipatingFactions()) {
            if (!isNullOrEmpty(blacklist))
                if (blacklist.contains(factionID))
                    continue;
            FactionAPI faction = Global.getSector().getFaction(factionID);
            if (isNull(faction))
                continue;
            picker.add(Global.getSector().getFaction(factionID));
        }

        return picker.pick();
    }

    public static FactionAPI pickFaction() {
        return pickFaction(null);
    }
}
