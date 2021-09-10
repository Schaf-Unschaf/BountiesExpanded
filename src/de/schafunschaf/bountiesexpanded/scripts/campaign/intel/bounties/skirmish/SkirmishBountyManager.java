package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.EntityProvider;
import org.apache.log4j.Logger;

import java.util.Random;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

public class SkirmishBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_skirmishBountyManager";
    public static final String BOUNTY_IDENTIFIER_KEY = "$bountiesExpanded_skirmishBountyActive_";
    public static Logger log = Global.getLogger(SkirmishBountyManager.class);

    public SkirmishBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static SkirmishBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (SkirmishBountyManager) test;
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
        return super.getIntervalRateMult();
    }

    @Override
    protected EveryFrameScript createEvent() {
        if (Settings.SKIRMISH_ACTIVE && new Random().nextFloat() <= Settings.SKIRMISH_SPAWN_CHANCE) {
            SkirmishBountyEntity skirmishBountyEntity = null;
            boolean isValidBounty = false;
            int maxSpawningAttempts = 5;

            while (!isValidBounty && maxSpawningAttempts > 0) {
                SkirmishBountyEntity skirmishBountyEntityAttempt = EntityProvider.skirmishBountyEntity();
                if (isNull(skirmishBountyEntityAttempt)) {
                    maxSpawningAttempts--;
                    continue;
                }
                if (Global.getSector().getMemoryWithoutUpdate().contains(BOUNTY_IDENTIFIER_KEY + skirmishBountyEntityAttempt.getStartingPoint().getMarket().getName())) {
                    maxSpawningAttempts--;
                    continue;
                }
                isValidBounty = true;
                skirmishBountyEntity = skirmishBountyEntityAttempt;
            }

            if (!isValidBounty)
                return null;

            CampaignFleetAPI fleet = skirmishBountyEntity.getFleet();
            String fleetTypeName = "Skirmisher Fleet";
            fleet.setName(fleetTypeName);
            fleet.setTransponderOn(true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

            Global.getSector().getMemoryWithoutUpdate().set(BOUNTY_IDENTIFIER_KEY + skirmishBountyEntity.getHideout().getMarket().getName(), null);

            return new SkirmishBountyIntel(skirmishBountyEntity, skirmishBountyEntity.getFleet(), skirmishBountyEntity.getPerson(), skirmishBountyEntity.getStartingPoint());
        }

        return null;
    }
}
