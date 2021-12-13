package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.BEMissionResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions.BEBaseMissionIntel;



public interface MissionEntity {
    BEBaseMissionIntel getMissionIntel();

    FactionAPI getOfferingFaction();

    FactionAPI getTargetedFaction();

    PersonAPI getMissionContact();

    MarketAPI getMissionMarket();

    String getIcon();

    String getTitle(BEMissionResult result);

    int getBaseReward();

    void addBulletPoints(BEBaseMissionIntel plugin, TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode);

    void createSmallDescription(BEBaseMissionIntel plugin, TooltipMakerAPI info, float width, float height);
}
