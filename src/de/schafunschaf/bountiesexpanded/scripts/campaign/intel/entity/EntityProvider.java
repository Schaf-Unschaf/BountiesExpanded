package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.credits.CreditCalculator;
import de.schafunschaf.bountiesexpanded.helper.faction.HostileFactionPicker;
import de.schafunschaf.bountiesexpanded.helper.faction.MiscFactionUtils;
import de.schafunschaf.bountiesexpanded.helper.faction.ParticipatingFactionPicker;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetGenerator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetPointCalculator;
import de.schafunschaf.bountiesexpanded.helper.fleet.FleetUtils;
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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.pirate.PirateBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler;
import lombok.extern.log4j.Log4j;

import java.util.*;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.MissionType;
import static de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionHandler.createNewMissionGoal;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

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

        MarketAPI startingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction).getMarket();
        if (isNull(startingPoint)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        MarketAPI endingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction, CoreWorldPicker.getDistantMarkets((float) Settings.assassinationMinTravelDistance, startingPoint.getPrimaryEntity())).getMarket();
        if (isNull(endingPoint)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, fakeMarket, startingPoint.getPrimaryEntity(), fleetCommander);
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

        return new AssassinationBountyEntity(bountyCredits, targetedFaction, bountyFleet, fleetCommander, startingPoint.getPrimaryEntity(), endingPoint.getPrimaryEntity(), missionHandler, difficulty, level, fleetQuality);
    }

    public static HighValueBountyEntity highValueBountyEntity() {
        return highValueBountyEntity(null);
    }

    public static HighValueBountyEntity highValueBountyEntity(String bountyId) {
        HighValueBountyData bountyData;
        if (isNotNull(bountyId))
            bountyData = HighValueBountyData.pickBounty(bountyId);
        else
            bountyData = HighValueBountyData.pickBounty();

        if (isNull(bountyData)) {
            log.warn("BountiesExpanded: Failed to pick HighValueBounty Data");
            return null;
        }

        String description = bountyData.intelText;
        FactionAPI offeringFaction = bountyData.getOfferingFaction();
        FactionAPI targetedFaction = isNull(bountyData.getTargetedFaction()) ? Global.getSector().getFaction(Factions.MERCENARY) : bountyData.getTargetedFaction();

        SectorEntityToken hideout = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS));
        if (isNull(hideout)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, bountyData.level, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, -1, null);
        if (isNull(fleetCommander)) {
            log.warn(String.format(NO_COMMANDER, targetedFaction.getDisplayName()));
            return null;
        }

        fleetCommander.setName(new FullName(bountyData.firstName, bountyData.lastName, FullName.Gender.valueOf(bountyData.gender)));
        fleetCommander.setPersonality(bountyData.captainPersonality);
        fleetCommander.setRankId(bountyData.rank);

        FleetParamsV3 fleetParams = new FleetParamsV3(null, hideout.getLocationInHyperspace(), targetedFaction.getId(), 2f, FleetTypes.PERSON_BOUNTY_FLEET, bountyData.minimumFleetFP, 0f, 0f, 0f, 0f, 0f, 0f);
        CampaignFleetAPI supportFleet = HighValueBountyData.FleetHelper.createSupportFleet(bountyData, fleetParams);
        CampaignFleetAPI bountyFleet = FleetFactoryV3.createEmptyFleet(fleetParams.factionId, FleetTypes.PERSON_BOUNTY_FLEET, null);
        FleetMemberAPI flagship = HighValueBountyData.FleetHelper.generateFlagship(bountyData);
        if (isNull(flagship)) return null;
        bountyFleet.getFleetData().addFleetMember(flagship);

        if (!isNullOrEmpty(bountyData.fleetVariantIds)) {
            List<FleetMemberAPI> additionalShips = HighValueBountyData.FleetHelper.generateAdditionalShips(bountyData);
            for (FleetMemberAPI fleetMember : additionalShips)
                bountyFleet.getFleetData().addFleetMember(fleetMember);
        }

        if (isNotNull(supportFleet)) {
            List<FleetMemberAPI> supportFleetMembers = supportFleet.getFleetData().getMembersListCopy();
            if (isNotNull(supportFleetMembers)) for (FleetMemberAPI fleetMember : supportFleetMembers) {
                fleetMember.setCaptain(null);
                bountyFleet.getFleetData().addFleetMember(fleetMember);
            }
        }

        FleetFactoryV3.addCommanderAndOfficersV2(bountyFleet, fleetParams, new Random());
        bountyFleet.getFleetData().setFlagship(flagship);
        bountyFleet.getFlagship().setCaptain(fleetCommander);
        bountyFleet.setCommander(fleetCommander);
        FleetFactoryV3.addCommanderSkills(bountyFleet.getCommander(), bountyFleet, fleetParams, null);
        bountyFleet.getFleetData().sort();

        return new HighValueBountyEntity(bountyData.creditReward, bountyData.repReward, offeringFaction, targetedFaction, bountyFleet, fleetCommander, hideout, description, bountyData.bountyId, 2f);
    }

    public static HVBRevengeEntity hvbRevengeEntity() {
        FactionAPI targetedFaction = Global.getSector().getFaction(Factions.PIRATES);
        FactionAPI offeringFaction = Global.getSector().getFaction(Factions.INDEPENDENT);
        int creditReward = 20_000_000;

        Map<HullSize, Set<FleetMemberAPI>> shipList = new HashMap<>();
        shipList.put(HullSize.FRIGATE, new HashSet<FleetMemberAPI>());
        shipList.put(HullSize.DESTROYER, new HashSet<FleetMemberAPI>());
        shipList.put(HullSize.CRUISER, new HashSet<FleetMemberAPI>());
        shipList.put(HullSize.CAPITAL_SHIP, new HashSet<FleetMemberAPI>());

        HighValueBountyManager highValueBountyManager = HighValueBountyManager.getInstance();
        Set<String> bountiesList = highValueBountyManager.getBountiesList();
        MarketAPI fakeMarket = MarketUtils.createFakeMarket(Global.getSector().getFaction(targetedFaction.getId()));
        CampaignFleetAPI bountyFleet = FleetFactoryV3.createEmptyFleet(targetedFaction.getId(), FleetTypes.PERSON_BOUNTY_FLEET, fakeMarket);

        for (String bountyID : bountiesList) {
            HighValueBountyData highValueBountyData = highValueBountyManager.getBounty(bountyID);
            FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, highValueBountyData.flagshipVariantId);
            fleetMember.setShipName(highValueBountyData.flagshipName);
            HullSize hullSize = fleetMember.getHullSpec().getHullSize();
            shipList.get(hullSize).add(fleetMember);
        }

        FleetDataAPI fleetData = bountyFleet.getFleetData();

        for (FleetMemberAPI fleetMemberAPI : shipList.get(HullSize.CAPITAL_SHIP)) {
            fleetData.addFleetMember(fleetMemberAPI);
        }

        for (FleetMemberAPI fleetMemberAPI : shipList.get(HullSize.CRUISER)) {
            fleetData.addFleetMember(fleetMemberAPI);
        }

        for (FleetMemberAPI fleetMemberAPI : shipList.get(HullSize.DESTROYER)) {
            fleetData.addFleetMember(fleetMemberAPI);
        }

        for (FleetMemberAPI fleetMemberAPI : shipList.get(HullSize.FRIGATE)) {
            fleetData.addFleetMember(fleetMemberAPI);
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, 14, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 10, null);
        fleetCommander.setName(new FullName("Omega", "Sheep", FullName.Gender.MALE));
        fleetCommander.setPortraitSprite(Global.getSector().getFaction(Factions.OMEGA).getPortraits(FullName.Gender.MALE).pick());
        fleetCommander.setPersonality(Personalities.AGGRESSIVE);
        fleetCommander.setRankId(Ranks.CLONE);

        FleetUtils.getShipWithHighestFP(fleetData.getMembersListCopy()).setFlagship(true);

        bountyFleet.getFlagship().setCaptain(fleetCommander);
        bountyFleet.setCommander(fleetCommander);

        SectorEntityToken hideout = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS));
        if (isNull(hideout)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        FleetFactoryV3.addCommanderSkills(bountyFleet.getCommander(), bountyFleet, null, null);

        return new HVBRevengeEntity(creditReward, 0, offeringFaction, targetedFaction, bountyFleet, fleetCommander, hideout);
    }

    public static WarCriminalEntity warCriminalEntity() {
        MissionHandler missionHandler = createNewMissionGoal();
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 50f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());
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

//        SectorEntityToken startingPoint = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS));
        SectorEntityToken startingPoint = CoreWorldPicker.pickFactionHideout(targetedFaction);
        if (isNull(startingPoint)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        MarketAPI endingPoint = MarketUtils.getRandomFactionMarket(offeringFaction);
        if (isNull(endingPoint)) {
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

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, homeMarket, startingPoint, fleetCommander);
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

        if (missionHandler.getMissionType() != MissionType.RETRIEVAL)
            bountyCredits *= 4;

        return new WarCriminalEntity(bountyCredits, level, fleetQuality, difficulty, targetedFaction, offeringFaction, bountyFleet, fleetCommander, startingPoint, endingPoint.getPrimaryEntity(), missionHandler);
    }

    public static PirateBountyEntity pirateBountyEntity() {
        MissionHandler missionHandler = createNewMissionGoal(MissionType.ASSASSINATION);
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 100f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier()) * 4;
        int rareFlagshipChance = difficulty.getFlatModifier();
        float fleetQuality = difficulty.getFlatModifier() * 0.2f + 0.2f;

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getDefaultBlacklist());
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction)) return null;

        FactionAPI targetedFaction = Global.getSector().getFaction(Factions.PIRATES);
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        SectorEntityToken startingPoint = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS));
        if (isNull(startingPoint)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(targetedFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(NO_COMMANDER);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, null, startingPoint, fleetCommander);
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

        return new PirateBountyEntity(bountyCredits, level, fleetQuality, difficulty, offeringFaction, bountyFleet, fleetCommander, startingPoint, missionHandler);
    }

    public static DeserterBountyEntity deserterBountyEntity() {
        MissionHandler missionHandler = createNewMissionGoal(MissionType.ASSASSINATION);
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 100f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier()) * 5;
        int rareFlagshipChance = difficulty.getFlatModifier();
        float fleetQuality = difficulty.getFlatModifier() * 0.2f + 0.4f;

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getDefaultBlacklist());
        if (!MiscFactionUtils.canFactionOfferBounties(offeringFaction))
            return null;

        SectorEntityToken startingPoint = CoreWorldPicker.pickFactionHideout(offeringFaction);
        if (isNull(startingPoint)) {
            log.warn(NO_HIDEOUT);
            return null;
        }

        SectorEntityToken endingPoint = RemoteWorldPicker.pickRandomHideout(TagCollection.getDefaultTagMap(TagCollection.VANILLA_BOUNTY_SYSTEM_TAGS));
        if (isNull(endingPoint)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        PersonAPI fleetCommander = OfficerManagerEvent.createOfficer(offeringFaction, level);
        if (isNull(fleetCommander)) {
            log.warn(NO_COMMANDER);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, fleetQuality, null, startingPoint, fleetCommander, offeringFaction, true);
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

        return new DeserterBountyEntity(bountyCredits, level, fleetQuality, difficulty, offeringFaction, bountyFleet, fleetCommander, startingPoint, endingPoint, missionHandler);
    }
}