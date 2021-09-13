package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.Settings;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.assassination.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.skirmish.SkirmishBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountylib.campaign.helper.credits.CreditCalculator;
import de.schafunschaf.bountylib.campaign.helper.faction.HostileFactionPicker;
import de.schafunschaf.bountylib.campaign.helper.faction.ParticipatingFactionPicker;
import de.schafunschaf.bountylib.campaign.helper.fleet.FleetGenerator;
import de.schafunschaf.bountylib.campaign.helper.fleet.FleetPointCalculator;
import de.schafunschaf.bountylib.campaign.helper.fleet.QualityCalculator;
import de.schafunschaf.bountylib.campaign.helper.level.LevelPicker;
import de.schafunschaf.bountylib.campaign.helper.location.CoreWorldPicker;
import de.schafunschaf.bountylib.campaign.helper.person.OfficerGenerator;

import java.util.Random;

import static de.schafunschaf.bountylib.campaign.helper.util.ComparisonTools.isNull;

/**
 * A convenience class for providing supported bounties
 */
public class EntityProvider {

    public static SkirmishBountyEntity skirmishBountyEntity() {
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getLevelAdjustment(), 0);
        float fractionToKill = (50 - new Random().nextInt(26)) / 100f;
        int bountyCredits = Math.round((int) ((CreditCalculator.vanillaCalculation(level, fractionToKill) * difficulty.getModifier()) / 1000)) * 1000;
        float fp = FleetPointCalculator.vanillaCalculation(level) * difficulty.getModifier();
        float qf = QualityCalculator.vanillaCalculation(level);

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction();
        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getSkirmishBountyBlacklist());
        if (isNull(targetedFaction))
            return null;
        PersonAPI person = OfficerGenerator.generateOfficer(targetedFaction, level);
        SectorEntityToken hideout = CoreWorldPicker.pickSafeHideout(targetedFaction);
        if (isNull(hideout))
            return null;

        CampaignFleetAPI fleet = FleetGenerator.createAndSpawnFleetTesting(fp, qf, null, hideout, person);

        return new SkirmishBountyEntity(bountyCredits, offeringFaction, targetedFaction, fleet, person, hideout, fractionToKill, difficulty, level);
    }

    public static AssassinationBountyEntity assassinationBountyEntity() {
        Difficulty difficulty = Difficulty.randomDifficulty();
        int level = Math.max(LevelPicker.pickLevel(0) + difficulty.getLevelAdjustment(), 0);
        int bountyCredits = Math.round(CreditCalculator.vanillaCalculation(level, difficulty.getModifier()) / 1000) * 1000;
        float fp = FleetPointCalculator.vanillaCalculation(level);
        float qf = QualityCalculator.vanillaCalculation(level);

        FactionAPI targetedFaction = ParticipatingFactionPicker.pickFaction(Blacklists.getSkirmishBountyBlacklist());
        if (isNull(targetedFaction))
            return null;
        PersonAPI person = OfficerGenerator.generateOfficer(targetedFaction, level);
        MarketAPI startingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction).getMarket();
        if (isNull(startingPoint))
            return null;
        MarketAPI endingPoint = CoreWorldPicker.pickSafeHideout(targetedFaction, CoreWorldPicker.getDistantMarkets((float) Settings.ASSASSINATION_MIN_TRAVEL_DISTANCE, startingPoint.getPrimaryEntity())).getMarket();
        if (isNull(endingPoint))
            return null;

        CampaignFleetAPI fleet = FleetGenerator.createAndSpawnFleet(fp, qf, null, startingPoint.getPrimaryEntity(), person);

        return new AssassinationBountyEntity(bountyCredits, targetedFaction, fleet, person, startingPoint.getPrimaryEntity(), endingPoint.getPrimaryEntity(), difficulty, level);
    }


}