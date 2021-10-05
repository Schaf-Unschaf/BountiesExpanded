package de.schafunschaf.bountiesexpanded.helper.location;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.Settings;

import java.util.Collection;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

public class RemoteWorldPicker {
    public static SectorEntityToken pickRandomHideout() {
        StarSystemAPI system = pickSystem(null);
        return pickPlanet(system);
    }

    public static SectorEntityToken pickRandomHideout(Collection<String> requiredTags) {
        StarSystemAPI system = pickSystem(requiredTags);
        return pickPlanet(system);
    }

    public static void createFakeLocationHint(SectorEntityToken hideoutLocation, PersonAPI person, TooltipMakerAPI info, float padding) {
        if (hideoutLocation != null) {
            SectorEntityToken fake = hideoutLocation.getContainingLocation().createToken(0, 0);
            fake.setOrbit(Global.getFactory().createCircularOrbit(hideoutLocation, 0, 1000, 100));

            String loc = BreadcrumbSpecial.getLocatedString(fake);
            loc = loc.replaceAll("orbiting", "hiding out near");
            loc = loc.replaceAll("located in", "hiding out in");
            String sheIs = "She is";
            if (person.getGender() == FullName.Gender.MALE) {
                sheIs = "He is";
            }
            info.addPara(sheIs + " rumored to be " + loc + ".", padding);
        }
    }

    protected static StarSystemAPI pickSystem(Collection<String> requiredTags) {
        WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!containsAny(system.getTags(), requiredTags))
                continue;

            if (system.hasPulsar()) continue;

            boolean hasHiddenMarket = false;
            for (MarketAPI market : Misc.getMarketsInLocation(system)) {
                if (market.isHidden()) {
                    hasHiddenMarket = true;
                    continue;
                }
                break;
            }
            if (hasHiddenMarket)
                continue;

            float distToPlayer = Misc.getDistanceToPlayerLY(system.getLocation());
            final float noSpawnRange = Global.getSettings().getFloat("personBountyNoSpawnRangeAroundPlayerLY");
            if (distToPlayer < noSpawnRange)
                continue;

            float weight = system.getPlanets().size();
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar())
                    continue;
                if (isNotNull(planet.getMarket())) {
                    float hazardValue = planet.getMarket().getHazardValue();
                    if (hazardValue <= 0f)
                        weight += 5f;
                    else if (hazardValue <= 0.25f)
                        weight += 3f;
                    else if (hazardValue <= 0.5f)
                        weight += 1f;
                }
            }

            float dist = system.getLocation().length();
            float distMult = Math.max(0, 50000f - dist);

            systemPicker.add(system, weight * distMult);
        }

        return systemPicker.pick();
    }

    private static SectorEntityToken pickPlanet(StarSystemAPI system) {
        if (isNull(system))
            return null;

        WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<>();
        for (SectorEntityToken planet : system.getPlanets()) {
            if (planet.isStar())
                continue;
            if (isNotNull(planet.getMarket()) && !planet.getMarket().isPlanetConditionMarketOnly())
                continue;
            if (Settings.IGNORE_PLAYER_MARKETS && planet.getMarket().isPlayerOwned())
                continue;
            if (planet.getMarket().isInHyperspace())
                continue;

            picker.add(planet);
        }
        return picker.pick();
    }
}
