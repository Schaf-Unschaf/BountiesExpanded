package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.revenge.HVBRevengeEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.warcriminal.WarCriminalManager;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.parameter.MissionType;
import lombok.extern.log4j.Log4j;

import java.util.*;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

/**
 * A convenience class for providing supported bounties
 */
@Log4j
public class EntityProvider {
    private static final String NO_OFFERING_FACTION = "BountiesExpanded: failed to pick valid offering faction";
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
        if (isNull(offeringFaction)) {
            log.warn(NO_OFFERING_FACTION);
            return null;
        }

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

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, homeMarket.getShipQualityFactor(), homeMarket, hideout, fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        return new SkirmishBountyEntity(bountyCredits, offeringFaction, targetedFaction, bountyFleet, fleetCommander, hideout, fractionToKill, difficulty, level);
    }

    public static AssassinationBountyEntity assassinationBountyEntity() {
        MissionType missionType = MissionType.ASSASSINATION;
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

        float qf = fakeMarket.getShipQualityFactor();

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

        MarketAPI endingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction, CoreWorldPicker.getDistantMarkets((float) Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE, startingPoint.getPrimaryEntity())).getMarket();
        if (isNull(endingPoint)) {
            log.warn(NO_DESTINATION);
            return null;
        }

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, qf, fakeMarket, startingPoint.getPrimaryEntity(), fleetCommander);
        if (isNull(bountyFleet)) {
            log.warn(NO_FLEET);
            return null;
        }

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                bountyFleet.getMemoryWithoutUpdate().set(AssassinationBountyManager.ASSASSINATION_BOUNTY_RARE_SHIP_KEY, true);
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.BASE_REWARD_PER_FP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new AssassinationBountyEntity(bountyCredits, targetedFaction, bountyFleet, fleetCommander, startingPoint.getPrimaryEntity(), endingPoint.getPrimaryEntity(), missionType, difficulty, level);
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
            List<FleetMemberAPI> membersInPriorityOrder = supportFleet.getFleetData().getMembersListCopy();
            if (isNotNull(membersInPriorityOrder)) for (FleetMemberAPI fleetMember : membersInPriorityOrder) {
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

        return new HighValueBountyEntity(bountyData.creditReward, bountyData.repReward, offeringFaction, targetedFaction, bountyFleet, fleetCommander, hideout, description, bountyData.bountyId);
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
        MissionType missionType = MissionType.getRandomMissionType();
        Difficulty difficulty = Difficulty.randomDifficulty();
        float fractionToKill = missionType == MissionType.SKIRMISH ? (50 - new Random().nextInt(26)) / 100f : 0;
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier(), 100f);
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());
        int rareFlagshipChance = difficulty.getFlatModifier();

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getDefaultBlacklist());
        if (isNull(offeringFaction)) {
            log.warn(NO_OFFERING_FACTION);
            return null;
        }

        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getDefaultBlacklist(), true);
        if (isNull(targetedFaction)) {
            log.warn(NO_TARGETED_FACTION);
            return null;
        }

        SectorEntityToken hideout = CoreWorldPicker.pickFactionHideout(targetedFaction);
        if (isNull(hideout)) {
            log.warn(NO_HIDEOUT);
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

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, homeMarket.getShipQualityFactor(), homeMarket, hideout, fleetCommander);
        MemoryAPI fleetMemory = bountyFleet.getMemoryWithoutUpdate();

        if (new Random().nextInt(20) + 1 <= rareFlagshipChance) { // 0/5/10/15 % chance to spawn
            boolean rareFlagshipAdded = RareFlagshipManager.replaceFlagship(bountyFleet);
            if (rareFlagshipAdded) {
                fleetMemory.set(WarCriminalManager.WAR_CRIMINAL_BOUNTY_RARE_SHIP_KEY, true);
                FleetMemberAPI flagship = bountyFleet.getFlagship();
                float flagshipFP = flagship.getFleetPointCost();
                bountyCredits += Settings.BASE_REWARD_PER_FP * flagshipFP * difficulty.getModifier() * Misc.getSizeNum(flagship.getHullSpec().getHullSize());
                log.info(String.format("BountiesExpanded: Fleet got lucky! Added '%s' as rare flagship", flagship.getHullSpec().getHullName()));
            }
        }

        return new WarCriminalEntity(bountyCredits, level, fractionToKill, difficulty, targetedFaction, offeringFaction, bountyFleet, fleetCommander, hideout, missionType);
    }
}