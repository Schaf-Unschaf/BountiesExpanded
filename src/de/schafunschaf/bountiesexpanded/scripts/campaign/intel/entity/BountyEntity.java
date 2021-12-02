package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;

public interface BountyEntity {
    BaseEventManager getBountyManager();

    FactionAPI getOfferingFaction();

    FactionAPI getTargetedFaction();

    CampaignFleetAPI getFleet();

    PersonAPI getOfferingPerson();

    PersonAPI getTargetedPerson();

    SectorEntityToken getStartingPoint();

    SectorEntityToken getEndingPoint();

    String getIcon();

    String getTitle(BountyResult result);

    MissionHandler getMissionHandler();

    Difficulty getDifficulty();
    
    BaseBountyIntel getBountyIntel();

    int getBaseReward();

    int getLevel();
    
    void addBulletPoints(BaseBountyIntel plugin, TooltipMakerAPI info, ListInfoMode mode);

    void createSmallDescription(BaseBountyIntel plugin, TooltipMakerAPI info, float width, float height);

    void setTargetRepBeforeBattle(float lastValue);
}