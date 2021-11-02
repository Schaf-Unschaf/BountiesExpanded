package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.market.MarketUtils;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

@Log4j
public class HighValueBountyData {
    public final String bountyId;
    public final int level;
    public final String rank;
    public final String firstName;
    public final String lastName;
    public final String captainPersonality;
    public final String fleetName;
    public final String flagshipName;
    public final String gender;
    public final String supportFleetFactionId;
    public final String portrait;
    public final String greetingText;
    public final boolean suppressIntel;
    public final String postedByFactionId;
    public final Integer creditReward;
    public final float repReward;
    public final String intelText;
    public final String flagshipVariantId;
    public final List<String> fleetVariantIds;
    public final int minimumFleetFP;
    public final float playerFPScalingFactor;
    public final float chanceToAutoRecover;
    public final List<String> prerequisiteBountiesList;
    public final List<String> specialItemRewards;
    public final boolean neverSpawnWhenFactionHostile;
    public final boolean neverSpawnWhenFactionNonHostile;
    public final int neverSpawnBeforeCycle;
    public final int neverSpawnBeforeLevel;
    public final int neverSpawnBeforeFleetPoints;
    public HighValueBountyIntel intel;

    public HighValueBountyData(String bountyId,
                               int level,
                               String rank,
                               String firstName,
                               String lastName,
                               String captainPersonality,
                               String fleetName,
                               String flagshipName,
                               String gender,
                               String supportFleetFactionId,
                               String portrait,
                               String greetingText,
                               boolean suppressIntel,
                               String postedByFactionId,
                               int creditReward,
                               float repReward,
                               String intelText,
                               String flagshipVariantId,
                               List<String> fleetListIds,
                               int minimumFleetFP,
                               float playerFPScalingFactor,
                               float chanceToAutoRecover,
                               List<String> specialItemRewards,
                               List<String> prerequisiteBountiesList,
                               boolean neverSpawnWhenFactionHostile,
                               boolean neverSpawnWhenFactionNonHostile,
                               int neverSpawnBeforeCycle,
                               int neverSpawnBeforeLevel,
                               int neverSpawnBeforeFleetPoints) {
        this.bountyId = bountyId;
        this.level = level > 14 ? (int) Math.ceil(level / 3) : level;
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
        if (isNotNull(captainPersonality) && !captainPersonality.isEmpty())
            this.captainPersonality = captainPersonality;
        else
            this.captainPersonality = Personalities.AGGRESSIVE;
        this.fleetName = fleetName;
        this.flagshipName = flagshipName;
        this.gender = gender;
        this.supportFleetFactionId = supportFleetFactionId.equals("hvb_hostile") ? "hvb_hostile_dummy" : supportFleetFactionId.equals("persean_league") ? Factions.PERSEAN : supportFleetFactionId;
        try {
            Global.getSettings().loadTexture(portrait);
        } catch (IOException e) {
            log.error("Failed to load portrait sprite: " + portrait);
        }
        this.portrait = portrait;
        this.greetingText = greetingText;
        this.suppressIntel = suppressIntel;
        this.postedByFactionId = postedByFactionId.equals("persean_league") ? Factions.PERSEAN : postedByFactionId;
        this.creditReward = creditReward;
        this.repReward = repReward;
        this.intelText = intelText;
        this.flagshipVariantId = flagshipVariantId;
        this.fleetVariantIds = fleetListIds;
        this.minimumFleetFP = minimumFleetFP;
        this.playerFPScalingFactor = playerFPScalingFactor;
        this.chanceToAutoRecover = chanceToAutoRecover;
        this.specialItemRewards = specialItemRewards;
        this.prerequisiteBountiesList = prerequisiteBountiesList;
        this.neverSpawnWhenFactionHostile = neverSpawnWhenFactionHostile;
        this.neverSpawnWhenFactionNonHostile = neverSpawnWhenFactionNonHostile;
        this.neverSpawnBeforeCycle = neverSpawnBeforeCycle;
        this.neverSpawnBeforeLevel = neverSpawnBeforeLevel;
        this.neverSpawnBeforeFleetPoints = neverSpawnBeforeFleetPoints;
    }

    public static HighValueBountyData pickBounty() {
        return pickBounty(null);
    }

    public static HighValueBountyData pickBounty(String bountyId) {
        WeightedRandomPicker<HighValueBountyData> candidates = new WeightedRandomPicker<>();
        HighValueBountyManager manager = HighValueBountyManager.getInstance();

        if (isNotNull(bountyId))
            if (isNull(manager.getBounty(bountyId))) {
                log.error("the bounty manager doesn't know what " + bountyId + " is");
                return null;
            } else
                return manager.getBounty(bountyId);

        for (String id : manager.getBountiesList()) {
            HighValueBountyData bountyData = manager.getBounty(id);
            if (bountyData.conditionsMet()) {
                candidates.add(bountyData);
            }
        }

        if (candidates.isEmpty()) {
            log.info("no eligible unique bounties, returning null");
            return null;
        } else {
            log.info("picking unique bounty");
            HighValueBountyData picked = candidates.pick();
            log.info("picked unique bounty: " + picked.bountyId);
            return picked;
        }
    }

    public boolean conditionsMet() {
        HighValueBountyManager manager = HighValueBountyManager.getInstance();
        if (isNotNull(getOfferingFaction()) && isNotNull(getTargetedFaction())) {
            if (!Global.getSettings().doesVariantExist(flagshipVariantId)) {
                log.warn(bountyId + " has invalid flagship variant ID");
                return false;
            } else {
                if (Settings.isDebugActive())
                    return true;

                int playerLevel = Global.getSector().getPlayerStats().getLevel();
                int adjustedLevelRequirement = Math.round(15f / 40 * neverSpawnBeforeLevel);
                boolean isActive = manager.isBountyActive(bountyId);
                boolean isDone = manager.isBountyCompleted(bountyId);
                boolean hasRequiredLevel = playerLevel >= adjustedLevelRequirement;
                boolean hasReachedRequiredCycle = Global.getSector().getClock().getCycle() >= neverSpawnBeforeCycle;
                boolean requiredBountiesCompleted = true;
                boolean failedPlayerHostilityCheck = (neverSpawnWhenFactionHostile && Global.getSector().getFaction(postedByFactionId).isHostileTo("player")) ||
                        (neverSpawnWhenFactionNonHostile && !Global.getSector().getFaction(supportFleetFactionId).isHostileTo("player"));

                if (!isNullOrEmpty(prerequisiteBountiesList))
                    for (String completedBounty : prerequisiteBountiesList)
                        requiredBountiesCompleted = manager.isBountyCompleted(completedBounty);

                if (isActive || isDone || !requiredBountiesCompleted || !hasRequiredLevel || !hasReachedRequiredCycle || failedPlayerHostilityCheck)
                    return false;

                return (float) Global.getSector().getPlayerFleet().getFleetPoints() * 1.33F >= (float) this.neverSpawnBeforeFleetPoints;
            }
        } else {
            log.warn(this.bountyId + " has invalid factionId or bountyFactionId");
            return false;
        }
    }

    public FactionAPI getTargetedFaction() {
        return Global.getSector().getFaction(supportFleetFactionId);
    }

    public FactionAPI getOfferingFaction() {
        return Global.getSector().getFaction(postedByFactionId);
    }

    public static class FleetHelper {
        public static CampaignFleetAPI createSupportFleet(HighValueBountyData bountyData, FleetParamsV3 fleetParams) {
            fleetParams.source = MarketUtils.createFakeMarket(bountyData.getTargetedFaction());
            fleetParams.ignoreMarketFleetSizeMult = true;
            fleetParams.maxNumShips = 50;
            fleetParams.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;

            CampaignFleetAPI tempFleet = FleetFactoryV3.createFleet(fleetParams);
            if (isNull(tempFleet) || tempFleet.isEmpty()) {
                log.warn("BountiesExpanded: Failed to create HighValueBounty Support-Fleet");
                return null;
            }

            int targetFP = tempFleet.getFleetPoints();
            int extraFP = targetFP < bountyData.minimumFleetFP ? bountyData.minimumFleetFP - targetFP : 0;
            int playerFP = (int) (Global.getSector().getPlayerFleet().getFleetPoints() * bountyData.playerFPScalingFactor);
            targetFP = tempFleet.getFleetPoints();
            extraFP += playerFP > targetFP ? playerFP - targetFP : 0;

            FleetFactoryV3.addPriorityOnlyThenAll(tempFleet, new Random(), extraFP, fleetParams, null, ShipRoles.COMBAT_CAPITAL, ShipRoles.COMBAT_LARGE, ShipRoles.COMBAT_MEDIUM);
            return tempFleet;
        }

        public static FleetMemberAPI generateFlagship(HighValueBountyData bountyData) {
            FleetMemberAPI flagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, bountyData.flagshipVariantId);
            if (isNull(flagship)) {
                log.warn("BountiesExpanded: Failed to generate HighValueBounty Flagship");
                return null;
            }
            flagship.setShipName(bountyData.flagshipName);
            return flagship;
        }

        public static List<FleetMemberAPI> generateAdditionalShips(HighValueBountyData bountyData) {
            List<FleetMemberAPI> fleetMemberList = new ArrayList<>();
            for (String shipVariantId : bountyData.fleetVariantIds) {
                FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipVariantId);
                fleetMemberList.add(fleetMember);
            }
            return fleetMemberList;
        }
    }
}

