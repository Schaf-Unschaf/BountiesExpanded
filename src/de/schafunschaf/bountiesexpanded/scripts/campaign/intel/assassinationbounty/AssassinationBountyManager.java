package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.assassinationbounty;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import org.apache.log4j.Logger;

public class AssassinationBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_fleetBountyManager";
    public static Logger log = Global.getLogger(AssassinationBountyManager.class);

    public static AssassinationBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (AssassinationBountyManager) test;
    }

    public AssassinationBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    @Override
    protected int getMinConcurrent() {
        return Settings.SKIRMISH_MIN_BOUNTIES;
    }

    @Override
    protected int getMaxConcurrent() {
        return Settings.SKIRMISH_MAX_BOUNTIES;
    }

    @Override
    protected float getIntervalRateMult() {
//		if (true) {
//			currMax = 200;
//			return 1000f;
//		}
        return super.getIntervalRateMult();
    }

    @Override
    protected EveryFrameScript createEvent() {
//        BEIntel intel = new BEIntel();
//        if (intel.isDone()) intel = null;
//
//
//        return intel;
        return null;
    }
}
