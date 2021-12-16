package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.MissionEntity;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Random;
import java.util.Set;

import static com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

@Getter
public abstract class BEBaseMissionIntel extends BaseMissionIntel {
    protected final MissionEntity missionEntity;
    protected final MarketAPI market;
    protected final PersonAPI contact;
    protected final SectorEntityToken spawnLocation;
    public final BEMissionResult result;
    protected final Float duration;
    protected float elapsedDays = 0f;

    public BEBaseMissionIntel(MissionEntity missionEntity) {
        this.missionEntity = missionEntity;
        this.market = missionEntity.getMissionMarket();
        this.contact = missionEntity.getMissionContact();
        this.spawnLocation = market.getPrimaryEntity();
        this.duration = (float) Settings.retrievalEventDuration;
        this.result = new BEMissionResult();
        Global.getSector().getIntelManager().queueIntel(this);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        info.addPara(missionEntity.getTitle(result), getTitleColor(mode), 0f);
        missionEntity.addBulletPoints(this, info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        missionEntity.createSmallDescription(this, info, width, height);
        if (Settings.isDebugActive()
                || result.getMissionState() == MissionState.FAILED
                || result.getMissionState() == MissionState.ABANDONED
                || result.getMissionState() == MissionState.CANCELLED
                || result.getMissionState() == MissionState.COMPLETED)
            addDeleteButton(info, width);
        else
            addAbandonButton(info, width);
    }

    @Override
    public void advanceImpl(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        elapsedDays += days;

        if (elapsedDays >= duration && !isDone()) {
            boolean canEnd = isNull(spawnLocation);
            if (canEnd) {
                result.setMissionState(MissionState.FAILED);
                endAfterDelay();
            }
        }
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return missionEntity.getOfferingFaction();
    }

    @Override
    public String getIcon() {
        return missionEntity.getIcon();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MISSIONS);
        tags.add(missionEntity.getOfferingFaction().getId());

        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return spawnLocation;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return missionEntity.getTitle(result);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        missionEntity.addBulletPoints(this, info, mode);
    }

    @Override
    public Color getBulletColorForMode(ListInfoMode mode) {
        return super.getBulletColorForMode(mode);
    }

    @Override
    public void addDays(TooltipMakerAPI info, String after, float days, Color c, float pad) {
        super.addDays(info, after, days, c, pad);
    }

    @Override
    public void bullet(TooltipMakerAPI info) {
        super.bullet(info);
    }

    @Override
    public void unindent(TooltipMakerAPI info) {
        super.unindent(info);
    }

    protected void triggerMissionStatusUpdate(@NotNull MissionState newMissionState, @Nullable InteractionDialogAPI dialog) {
        TextPanelAPI textPanel = null;

        if (ComparisonTools.isNotNull(dialog))
            textPanel = dialog.getTextPanel();

        ReputationAdjustmentResult reputationAdjustmentResult = newMissionState
                == MissionState.COMPLETED
                ? generateMissionSuccessRepAdjustment(textPanel)
                : generateMissionFailureRepAdjustment(textPanel);

        result.setMissionState(newMissionState);
        result.setOfferingFactionRepChange(reputationAdjustmentResult);

        sendUpdate(newMissionState, textPanel);

        endMission();
    }

    private ReputationAdjustmentResult generateMissionSuccessRepAdjustment(@Nullable TextPanelAPI textPanel) {
        CoreReputationPlugin.CustomRepImpact customRepImpact = new CoreReputationPlugin.CustomRepImpact();
        float maxRepGain = 0.15f;
        float neutralRepGain = 0.05f;
        float repToPlayer = contact.getFaction().getRelToPlayer().getRel();

        if (repToPlayer < 0f)
            customRepImpact.delta = ((maxRepGain - neutralRepGain) * -repToPlayer + neutralRepGain);
        else if (repToPlayer > 0f)
            customRepImpact.delta = (neutralRepGain - repToPlayer * neutralRepGain);
        else
            customRepImpact.delta = neutralRepGain;

        return Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, customRepImpact,
                        null, null, true, false),
                contact.getFaction().getId());
    }

    private ReputationAdjustmentResult generateMissionFailureRepAdjustment(@Nullable TextPanelAPI textPanel) {
        CoreReputationPlugin.CustomRepImpact customRepImpact = new CoreReputationPlugin.CustomRepImpact();
        float delta = (float) (new Random(contact.getId().hashCode()).nextInt(20) + 11) / 100;
        customRepImpact.delta = -delta;

        return Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, customRepImpact,
                        null, null, true, false),
                contact.getFaction().getId());
    }
}
