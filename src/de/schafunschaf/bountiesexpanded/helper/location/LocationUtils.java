package de.schafunschaf.bountiesexpanded.helper.location;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import de.schafunschaf.bountiesexpanded.util.ComparisonTools;

import java.util.HashSet;
import java.util.Set;

public class LocationUtils {
    public static SectorEntityToken getNearestLocation(CampaignFleetAPI fleet) {
        LocationAPI containingLocation = fleet.getContainingLocation();
        if (containingLocation.isHyperspace())
            return Misc.getNearestStarSystem(fleet).getHyperspaceAnchor();

        Set<SectorEntityToken> validEntities = new HashSet<>();
        validEntities.addAll(containingLocation.getPlanets());
        validEntities.addAll(containingLocation.getJumpPoints());
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.STABLE_LOCATION));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.COMM_RELAY));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.NAV_BUOY));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.SENSOR_ARRAY));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.GATE));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.STATION));
        validEntities.addAll(containingLocation.getEntitiesWithTag(Tags.STAR));

        if (validEntities.isEmpty()) return Misc.getNearestStarSystem(fleet).getHyperspaceAnchor();

        float nearestDistance = 0f;
        SectorEntityToken nearestEntity = null;
        for (SectorEntityToken entity : validEntities) {
            float distance = Misc.getDistance(fleet, entity);
            if (ComparisonTools.isNull(nearestEntity)) {
                nearestEntity = entity;
                nearestDistance = distance;
                continue;
            }

            if (distance < nearestDistance) {
                nearestEntity = entity;
                nearestDistance = distance;
            }
        }

        return nearestEntity;
    }
}
