package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import de.schafunschaf.bountiesexpanded.Blacklists;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.assassinationbounty.AssassinationBountyEntity;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.skirmish.SkirmishBountyEntity;
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

    public static SkirmishBountyEntity fleetBountyEntity() {
        int level = LevelPicker.pickLevel(2);
        float fractionToKill = (50 - new Random().nextInt(26)) / 100f;
        int bountyCredits = CreditCalculator.vanillaCalculation(level, fractionToKill);
        float fp = FleetPointCalculator.vanillaCalculation(level);
        float qf = QualityCalculator.vanillaCalculation(level);

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction();
        FactionAPI targetedFaction = HostileFactionPicker.pickParticipatingFaction(offeringFaction, Blacklists.getSkirmishBountyBlacklist());
        if (isNull(targetedFaction))
            return null;
        PersonAPI person = OfficerGenerator.generateOfficer(targetedFaction, level);
        SectorEntityToken hideout = CoreWorldPicker.pickSafeHideout(targetedFaction);
        if (isNull(hideout))
            return null;

        CampaignFleetAPI fleet = FleetGenerator.createAndSpawnFleet(fp, qf, null, hideout, person);

        return new SkirmishBountyEntity(bountyCredits, offeringFaction, targetedFaction, fleet, person, hideout, fractionToKill, level);
    }

    public static AssassinationBountyEntity assassinationBountyEntity() {
        int level = LevelPicker.pickLevel(5);
        int bountyCredits = CreditCalculator.vanillaCalculation(level, 1f);
        float fp = FleetPointCalculator.vanillaCalculation(level+1);
        float qf = QualityCalculator.vanillaCalculation(level);

        FactionAPI offeringFaction = ParticipatingFactionPicker.pickFaction();
        FactionAPI targetedFaction = HostileFactionPicker.pickFaction(offeringFaction, false, Blacklists.getSkirmishBountyBlacklist());
        PersonAPI person = OfficerGenerator.generateOfficer(targetedFaction, level);
        MarketAPI hideout = CoreWorldPicker.pickSafeHideout(targetedFaction).getMarket();
        CampaignFleetAPI fleet = FleetGenerator.createCombatFleet(fp, qf, null, hideout.getPrimaryEntity(), person);
        if (isNull(hideout))
            return null;

        return new AssassinationBountyEntity(bountyCredits, offeringFaction, targetedFaction, fleet, person, hideout.getPrimaryEntity());
    }
}