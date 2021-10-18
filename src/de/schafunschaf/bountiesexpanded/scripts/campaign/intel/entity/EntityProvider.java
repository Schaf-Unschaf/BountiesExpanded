package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.helper.credits.CreditCalculator;
import de.schafunschaf.bountiesexpanded.helper.faction.HostileFactionPicker;
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
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyData;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty.HighValueBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

/**
 * A convenience class for providing supported bounties
 */
public class EntityProvider {
    public static final Logger log = Global.getLogger(EntityProvider.class);

    public static SkirmishBountyEntity skirmishBountyEntity() {
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fractionToKill = (50 - new Random().nextInt(26)) / 100f;
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier());
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());
        int bountyLevel = BountyEventData.getSharedData().getLevel();
        fp += level / 100 + 1;
        fp += bountyLevel / 100 + 1;

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction();
        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getSkirmishBountyBlacklist(), true);
        if (isNull(targetedFaction))
            return null;

        PersonAPI person = OfficerManagerEvent.createOfficer(targetedFaction, level);
        person.setPersonality(Personalities.AGGRESSIVE);

        SectorEntityToken hideout = CoreWorldPicker.pickSafeHideout(targetedFaction);
        if (isNull(hideout))
            return null;

        MarketAPI homeMarket = MarketUtils.getBestMarketForQuality(targetedFaction);
        if (isNull(homeMarket))
            homeMarket = MarketUtils.createFakeMarket(targetedFaction);

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, homeMarket.getShipQualityFactor(), homeMarket, hideout, person);

        return new SkirmishBountyEntity(bountyCredits, offeringFaction, targetedFaction, bountyFleet, person, hideout, fractionToKill, difficulty, level);
    }

    public static AssassinationBountyEntity assassinationBountyEntity() {
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getFlatModifier(), 0);
        float fp = FleetPointCalculator.getPlayerBasedFP(difficulty.getModifier());
        int bountyCredits = CreditCalculator.getRewardByFP(fp, difficulty.getModifier());

        FactionAPI targetedFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getSkirmishBountyBlacklist());
        if (isNull(targetedFaction)) return null;

        MarketAPI fakeMarket = MarketUtils.createFakeMarket(targetedFaction);
        float qf = fakeMarket.getShipQualityFactor();

        PersonAPI person = OfficerGenerator.generateOfficer(targetedFaction, level);
        MarketAPI startingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction).getMarket();
        if (isNull(startingPoint))
            return null;

        MarketAPI endingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction, CoreWorldPicker.getDistantMarkets((float) Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE, startingPoint.getPrimaryEntity())).getMarket();
        if (isNull(endingPoint))
            return null;

        CampaignFleetAPI bountyFleet = FleetGenerator.createBountyFleetV2(fp, qf, fakeMarket, startingPoint.getPrimaryEntity(), person);

        return new AssassinationBountyEntity(bountyCredits, targetedFaction, bountyFleet, person, startingPoint.getPrimaryEntity(), endingPoint.getPrimaryEntity(), difficulty, level);
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
            log.warn("BountiesExpanded: Failed to generate HighValueBounty Hideout");
            return null;
        }

        PersonAPI person = OfficerManagerEvent.createOfficer(targetedFaction, bountyData.level, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, -1, null);
        if (isNull(person)) {
            log.warn("BountiesExpanded: Failed to generate HighValueBounty Person");
            return null;
        }

        person.setName(new FullName(bountyData.firstName, bountyData.lastName, FullName.Gender.valueOf(bountyData.gender)));
        person.setPersonality(bountyData.captainPersonality);
        person.setPortraitSprite(bountyData.portrait);
        person.setRankId(bountyData.rank);

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
        bountyFleet.getFlagship().setCaptain(person);
        bountyFleet.setCommander(person);
        FleetFactoryV3.addCommanderSkills(bountyFleet.getCommander(), bountyFleet, fleetParams, null);

        return new HighValueBountyEntity(bountyData.creditReward, bountyData.repReward, offeringFaction, targetedFaction, bountyFleet, person, hideout, description, bountyData.bountyId);
    }
}