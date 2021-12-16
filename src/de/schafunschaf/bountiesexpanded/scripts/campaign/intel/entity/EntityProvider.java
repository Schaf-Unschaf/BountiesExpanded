package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.credits.CreditCalculator;
import de.schafunschaf.bountiesexpanded.helper.faction.HostileFactionPicker;
import de.schafunschaf.bountiesexpanded.helper.faction.MiscFactionUtils;
import de.schafunschaf.bountiesexpanded.helper.faction.ParticipatingFactionPicker;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetPointCalculator;
import de.schafunschaf.bountiesexpanded.helper.intel.BountyEventData;
import de.schafunschaf.bountiesexpanded.helper.level.LevelPicker;
import de.schafunschaf.bountiesexpanded.helper.location.CoreWorldPicker;
import de.schafunschaf.bountiesexpanded.helper.location.RemoteWorldPicker;
import de.schafunschaf.bountiesexpanded.helper.location.TagCollection;
import de.schafunschaf.bountiesexpanded.helper.market.MarketUtils;
import de.schafunschaf.bountiesexpanded.helper.person.OfficerGenerator;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.RareFlagshipManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.deserter.DeserterBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate.PirateBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.extern.log4j.Log4j;

import java.util.Random;
import java.util.Set;

import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.MissionType;
import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.createNewMissionGoal;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

/**
 * A convenience class for providing supported bounties
 */
@Log4j
public class EntityProvider {
    public static final String FLEET_IDENTIFIER_KEY = "$bountiesExpanded_fleetIdentifier";
    private static final String NO_TARGETED_FACTION = "BountiesExpanded: failed to pick valid targeted faction";
    private static final String NO_COMMANDER = "BountiesExpanded: failed to generate fleet commander for faction '%s'";
    private static final String NO_HIDEOUT = "BountiesExpanded: failed to pick hideout";
    private static final String NO_DESTINATION = "BountiesExpanded: failed to pick destination";
    private static final String NO_FLEET = "BountiesExpanded: failed to create bounty fleet";

    public static SkirmishBountyEntity skirmishBountyEntity() {
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fractionToKill = (50 - new Random().nextInt(26)) / 100f;
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 100f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());
        int bountyLevel = BountyEventData.getSharedData().getLevel();
        fp += level / 100 + 1;
        fp += bountyLevel / 100 + 1;

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction();
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction)) return null;

        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getSkirmishBountyBlacklist(), true);
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(String.format(NO_COMMANDER, targetedFaction.getDisplayName()));
            return null;
        }

        fleetCommander.setPersonality(Personalities.AGGRESSIVE);

        SectorEntityToken hideout = CoreWorldPicker.pickSafeHideout(targetedFaction);
        if (isNull(hideout)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        MarketAPI homeMarket = MarketUtils.getBestMarketForQuality(targetedFaction);
        if (isNull(homeMarket))
            homeMarket = MarketUtils.createFakeMarket(targetedFaction);

        float fleetQuality = Math.max(homeMarket.getShipQualityFactor(), 0.2f);

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, homeMarket, hideout, fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        return new SkirmishBountyEntity(bountyCredits, offeringFaction, targetedFaction, bountyFleet, fleetCommander, hideout, fractionToKill, difficulty, level, fleetQuality);
    }

    public static AssassinationBountyEntity assassinationBountyEntity() {
        MissionHandler missionHandler = createNewMissionGoal(MissionType.ASSASSINATION);
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 50f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());
        int rareFlagshipChance = difficulty.getFlatModifier();

        FactionAPI targetedFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getSkirmishBountyBlacklist());
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        MarketAPI fakeMarket = MarketUtils.createFakeMarket(targetedFaction);

        float fleetQuality = Math.max(fakeMarket.getShipQualityFactor(), 0.2f);

        PersonAPI fleetCommander = OfficerGenerator.generateOfficer(targetedFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(String.format(NO_COMMANDER, targetedFaction.getDisplayName()));
            return null;
        }

        MarketAPI spawnLocation = CoreWorldPicker.pickSafeHideout(targetedFaction).getMarket();
        if (isNull(spawnLocation)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        MarketAPI travelDestination = CoreWorldPicker.pickSafeHideout(targetedFaction, CoreWorldPicker.getDistantMarkets((float) Settings.assassinationMinTravelDistance, spawnLocation.getPrimaryEntity())).getMarket();
        if (isNull(travelDestination)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, fakeMarket, spawnLocation.getPrimaryEntity(), fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                bountyFleet.getMemoryWithoutUpdate().set(RareFlagshipManager.RARE_FLAGSHIP_KEY, flagship);
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.baseRewardPerFP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new AssassinationBountyEntity(bountyCredits, targetedFaction, bountyFleet, fleetCommander, spawnLocation.getPrimaryEntity(), travelDestination.getPrimaryEntity(), missionHandler, difficulty, level, fleetQuality);
    }

    public static WarCriminalEntity warCriminalEntity() {
        MissionHandler missionHandler = createNewMissionGoal();
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 50f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier()) * 4;
        int rareFlagshipChance = difficulty.getFlatModifier();

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getDefaultBlacklist());
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction)) return null;

        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getDefaultBlacklist(), true);
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        if (targetedFaction == offeringFaction)
            return null;

        SectorEntityToken spawnLocation = CoreWorldPicker.pickFactionHideout(targetedFaction);
        if (isNull(spawnLocation)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        // For Retrieval missions
        MarketAPI dropOffLocation = MarketUtils.getRandomFactionMarket(offeringFaction);
        if (isNull(dropOffLocation)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(NO_COMMANDER);
            return null;
        }

        MarketAPI homeMarket = MarketUtils.getBestMarketForQuality(targetedFaction);
        if (isNull(homeMarket))
            homeMarket = MarketUtils.createFakeMarket(targetedFaction);

        float fleetQuality = Math.max(homeMarket.getShipQualityFactor(), 0.2f);

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, homeMarket, spawnLocation, fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                bountyFleet.getMemoryWithoutUpdate().set(RareFlagshipManager.RARE_FLAGSHIP_KEY, flagship);
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.baseRewardPerFP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new WarCriminalEntity(bountyCredits, level, fleetQuality, difficulty, targetedFaction, offeringFaction, bountyFleet, fleetCommander, spawnLocation, dropOffLocation.getPrimaryEntity(), missionHandler);
    }

    public static PirateBountyEntity pirateBountyEntity() {
        MissionHandler missionHandler = createNewMissionGoal(MissionType.ASSASSINATION);
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 30f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier()) * 3;
        int rareFlagshipChance = difficulty.getFlatModifier();
        float fleetQuality = difficulty.getFlatModifier() * 0.2f + 0.2f;

        Set<String> defaultBlacklist = Blacklists.getDefaultBlacklist();
        defaultBlacklist.add(Factions.PIRATES);

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(defaultBlacklist);
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction)) return null;

        FactionAPI targetedFaction = Global.getSector().getFaction(Factions.PIRATES);
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        SectorEntityToken spawnLocation = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS), false);
        if (isNull(spawnLocation)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(NO_COMMANDER);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, null, spawnLocation, fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                bountyFleet.getMemoryWithoutUpdate().set(RareFlagshipManager.RARE_FLAGSHIP_KEY, flagship);
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.baseRewardPerFP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new PirateBountyEntity(bountyCredits, level, fleetQuality, difficulty, offeringFaction, bountyFleet, fleetCommander, spawnLocation, missionHandler);
    }

    public static DeserterBountyEntity deserterBountyEntity() {
        MissionHandler missionHandler = createNewMissionGoal(MissionType.ASSASSINATION);
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 40f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier()) * 4;
        int rareFlagshipChance = difficulty.getFlatModifier();
        float fleetQuality = difficulty.getFlatModifier() * 0.2f + 0.4f;

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getDefaultBlacklist());
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction))
            return null;

        SectorEntityToken spawnLocation = CoreWorldPicker.pickFactionHideout(offeringFaction);
        if (isNull(spawnLocation)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        SectorEntityToken travelDestination = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS), false);
        if (isNull(travelDestination)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(offeringFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(NO_COMMANDER);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, null, spawnLocation, fleetCommander, offeringFaction, true);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                bountyFleet.getMemoryWithoutUpdate().set(RareFlagshipManager.RARE_FLAGSHIP_KEY, flagship);
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.baseRewardPerFP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new DeserterBountyEntity(bountyCredits, level, fleetQuality, difficulty, offeringFaction, bountyFleet, fleetCommander, spawnLocation, travelDestination, missionHandler);
    }
}