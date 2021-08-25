package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountylib.campaign.intel.BountyEventData.BountyResult;


public interface BountyEntity {

    FactionAPI getOfferingFaction();

    CampaignFleetAPI getFleet();

    PersonAPI getPerson();

    SectorEntityToken getHideout();

    String getIcon();

    String getTitle(BountyResult result);

    int getLevel();

    int getBountyCredits();

    void addBulletPoints(BaseBountyIntel plugin, TooltipMakerAPI info, ListInfoMode mode);

    void createSmallDescription(BaseBountyIntel plugin, TooltipMakerAPI info, float width, float height);
}