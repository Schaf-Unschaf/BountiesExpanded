package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class WarCriminalManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_warCriminalManagerBountyManager";
    public static final String WAR_CRIMINAL_BOUNTY_FLEET_KEY = "$bountiesExpanded_warCriminalBountyFleet";
    public static final String WAR_CRIMINAL_BOUNTY_RARE_SHIP_KEY = "$bountiesExpanded_warCriminalBountyFleet_rareFlagship";

    public WarCriminalManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static WarCriminalManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (WarCriminalManager) instance;
    }

    @Override
    protected int getMinConcurrent() {
        return 0;
    }

    @Override
    protected int getMaxConcurrent() {
        return 0;
    }

    @Override
    protected EveryFrameScript createEvent() {
        return null;
    }

    public WarCriminalIntel createWarCriminalBountyEvent() {
        return null;
    }
}
