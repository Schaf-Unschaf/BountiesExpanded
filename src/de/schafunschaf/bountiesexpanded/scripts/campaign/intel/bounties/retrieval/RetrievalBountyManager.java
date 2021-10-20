package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.retrieval;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import de.schafunschaf.bountiesexpanded.ExternalDataSupplier;
import de.schafunschaf.bountiesexpanded.plugins.BountiesExpandedPlugin;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RetrievalBountyManager extends BaseEventManager {
    public static final String KEY = "$bountiesExpanded_retrievalBountyManager";
    public static final String RETRIEVAL_BOUNTY_FLEET_KEY = "$bountiesExpanded_retrievalBountyFleet";
    private Map<String, RareFlagshipData> rareFlagshipData = new HashMap<>();
    public static final Logger log = Global.getLogger(RetrievalBountyManager.class);

    public RetrievalBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        loadRareFlagshipData();
    }

    public static RetrievalBountyManager getInstance() {
        Object instance = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (RetrievalBountyManager) instance;
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

    public void loadRareFlagshipData() {
        rareFlagshipData.putAll(ExternalDataSupplier.loadRareFlagshipData(BountiesExpandedPlugin.RARE_FLAGSHIPS_FILE));
    }

    public void createRetrievalBountyEvent() {

    }
}
