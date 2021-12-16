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

import java.util.Map;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.*;

public class RemoteWorldPicker {
    public static SectorEntityToken pickRandomHideout(boolean useVanillaMethod) {
        StarSystemAPI system = pickSystem(null, useVanillaMethod);
        return pickPlanet(system);
    }

    public static SectorEntityToken pickRandomHideout(Map<String, Integer> requiredTags, boolean useVanillaMethod) {
        StarSystemAPI system = pickSystem(requiredTags, useVanillaMethod);
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

    private static StarSystemAPI pickSystem(Map<String, Integer> requiredTags, boolean useVanillaMethod) {
        WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<>();
        int mult = isNull(requiredTags) ? 1 : 0;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.hasPulsar())
                continue;

            if (isNotNull(requiredTags)) {
                if (!containsAny(system.getTags(), requiredTags.keySet()))
                    continue;

                if (!isNullOrEmpty(requiredTags))
                    for (Map.Entry<String, Integer> entry : requiredTags.entrySet())
                        mult = system.hasTag(entry.getKey()) ? entry.getValue() : 0;
            }

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

            if (useVanillaMethod) {
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

                systemPicker.add(system, weight * distMult * mult);
            } else
                systemPicker.add(system);
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
            if (Settings.ignorePlayerMarkets && planet.getMarket().isPlayerOwned())
                continue;
            if (planet.getMarket().isInHyperspace())
                continue;

            picker.add(planet);
        }
        return picker.pick();
    }
}
