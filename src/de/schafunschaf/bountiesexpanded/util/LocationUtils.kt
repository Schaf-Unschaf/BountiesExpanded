package de.schafunschaf.bountiesexpanded.util

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.ids.Terrain
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.util.extensions.getAngle
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.math.PI
import kotlin.math.round

enum class Compass(val heading: Int, val fullName: String) {
    E(0, "East"), NE(1, "Northeast"),
    N(2, "North"), NW(3, "Northwest"),
    W(4, "West"), SW(5, "Southwest"),
    S(6, "South"), SE(7, "Southeast");

    companion object {

        fun getCardinalDirection(location: Vector2f): Compass {
            val octant: Int = (round(8 * location.getAngle() / (2 * PI) + 8) % 8).toInt()
            return values().find { direction -> direction.heading == octant.coerceIn(0..7) }!!
        }
    }
}

fun movePlayerFleet(targetEntity: SectorEntityToken) {
    val playerFleet = Global.getSector().playerFleet
    val oldLocation = playerFleet.containingLocation
    oldLocation.removeEntity(playerFleet)
    val newLocation = targetEntity.containingLocation
    newLocation.addEntity(playerFleet)
    Global.getSector().currentLocation = newLocation
    playerFleet.setLocation(targetEntity.location.x, targetEntity.location.y)
    playerFleet.clearAssignments()
}

fun getLocatedString(target: SectorEntityToken, withSystem: Boolean): String? {
    val loc = getLocationDescription(target, withSystem)
    var orbiting = ""
    var useTerrain = false
    if (target.orbitFocus != null) {
        if (target.orbitFocus is PlanetAPI) {
            val focus = target.orbitFocus as PlanetAPI
            val isPrimary = target.containingLocation is StarSystemAPI &&
                    focus === (target.containingLocation as StarSystemAPI).star
            if (!focus.isStar || !isPrimary) {
                orbiting =
                    "orbiting " + focus.spec.aOrAn + " " + focus.typeNameWithLowerCaseWorld.lowercase(Locale.getDefault()) + " in "
            } else {
                val dist = Misc.getDistance(focus.location, target.location)
                //float dist = Misc.getDistance(new Vector2f(), target.getLocation());
                orbiting = if (dist < 3000) {
                    "located in the heart of "
                } else if (dist > 12000) {
                    "located in the outer reaches of "
                } else {
                    //orbiting = "located in ";
                    "located some distance away from the center of "
                }
                useTerrain = true
            }
        } else if (target.orbitFocus is CustomCampaignEntityAPI) {
            val custom = target.orbitFocus as CustomCampaignEntityAPI
            orbiting = "orbiting " + custom.customEntitySpec.aOrAn + " " + custom.customEntitySpec.nameInText + " in "
        } else if (target.orbitFocus is JumpPointAPI) {
            orbiting = "orbiting a jump-point in "
        } else { // center of a binary/nebula/etc
            //float dist = Misc.getDistance(target.getOrbitFocus().getLocation(), target.getLocation());
            val dist = Misc.getDistance(Vector2f(), target.location)
            orbiting = if (dist < 3000) {
                "located in the heart of "
            } else if (dist > 8000) {
                "located in the outer reaches of "
            } else {
                //orbiting = "located in ";
                "located some distance away from the center of "
            }
            useTerrain = true
        }
    } else if (target.containingLocation != null && target.containingLocation.isNebula) {
        val dist = Misc.getDistance(Vector2f(), target.location)
        orbiting = if (dist < 3000) {
            "located in the heart of "
        } else if (dist > 8000) {
            "located on the outskirts of "
        } else {
            //orbiting = "located in ";
            "located some distance away from the center of "
        }
        useTerrain = true
    }
    if (useTerrain) {
        val terrainString = getTerrainString(target)
        if (terrainString != null) {
            orbiting = "located in $terrainString in "
        }
    }
    if (orbiting == null || orbiting.isEmpty()) orbiting = "located in "
    return orbiting + loc
}

fun getTerrainString(entity: SectorEntityToken): String? {
    if (entity.containingLocation !is StarSystemAPI) return null
    val system = entity.containingLocation as StarSystemAPI
    for (terrain in system.terrainCopy) {
        if (!terrain.plugin.containsEntity(entity)) continue
        val type = terrain.type
        if (Terrain.ASTEROID_BELT == type) return "an asteroid belt"
        if (Terrain.ASTEROID_FIELD == type) return "an asteroid field"
        //if (Terrain.MAGNETIC_FIELD.equals(type)) return "a magnetic field";
        if (terrain.hasTag(Tags.ACCRETION_DISK)) return "an accretion disk"
        if (Terrain.RING == type) return "a ring system"
    }
    return null
}

fun getLocationDescription(entity: SectorEntityToken, withSystem: Boolean): String {
    val loc = entity.containingLocation ?: return "an unknown location"
    if (loc.isHyperspace) {
        return "hyperspace"
    }
    val system = loc as StarSystemAPI

//		if (system == Global.getSector().getCurrentLocation()) {
//			return "the " + system.getNameWithLowercaseType();
//		}

    //if (entity.getConstellation() != null && entity.getConstellation() != Global.getSector().getCurrentLocation().getConstellation()) {
    if (withSystem || entity.constellation == null || entity.constellation.systems.size == 1 ||
        entity.isInCurrentLocation
    ) {
        return "the " + system.nameWithLowercaseType
    }
    val c = entity.constellation
    val cText = "in the " + c.nameWithLowercaseType
    if (c.systems.size == 1) {
        return "the " + system.nameWithLowercaseType
    }
    if (system.isNebula) {
        return "a nebula $cText"
    }
    if (system.tertiary != null) {
        return "a trinary star system $cText"
    }
    if (system.secondary != null) {
        return "a binary star system $cText"
    }
    val star = system.star
    if (star != null) {
        if (star.spec.isBlackHole) {
            return "a black hole system $cText"
        }

        //String sysText = star.getSpec().getAOrAn() + " " + star.getSpec().getName().toLowerCase() + " system ";
        val type = BreadcrumbSpecial.getStarTypeName(star)
        val color = BreadcrumbSpecial.getStarColorName(star)
        var sysText: String? = null
        val counts = BreadcrumbSpecial.getTypeAndColorCounts(c)
        val cColor = counts.getCount(color)
        val cType = counts.getCount(type)
        if (cColor > 1 && cType > cColor) {
            sysText = "a system with " + star.spec.aOrAn + " " + color + " primary star "
        } else if (cType > 0) {
            sysText = "a system with a $type primary star "
        } else if (cColor > 0) {
            sysText = "a system with " + star.spec.aOrAn + " " + color + " primary star "
        }
        if (sysText != null) {
            return sysText + cText
        }
    }

    //if (system.getType() == StarSystemType.SINGLE) {
    return "the " + system.nameWithLowercaseType + " " + cText
    //return "orbit around " + getStarDescription(system.getStar()) + " " + cText;
}