package de.schafunschaf.bountiesexpanded.helper

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class LocationEntityProvider {

    var locationList: Collection<LocationAPI> = Global.getSector().allLocations
        private set
    var minRange: Float = 0f
        private set
    var maxRange: Float = 0f
        private set
    var allowedSystemTags: Collection<String>? = null
        private set
    var forbiddenSystemTags: Collection<String>? = null
        private set
    var allowedEntityTags: Collection<String>? = null
        private set
    var forbiddenEntityTags: Collection<String>? = null
        private set

    private val galaxyCenter = Vector2f(0f, 0f)

    fun locations(locations: Collection<LocationAPI>) = apply { this.locationList = locations }
    fun withinMinRange(minRange: Float) = apply { this.minRange = minRange }
    fun withinMaxRange(maxRange: Float) = apply { this.maxRange = maxRange }
    fun allowedSystems(tags: Collection<String>) = apply { this.allowedSystemTags = tags }
    fun allowedEntities(tags: Collection<String>) = apply { this.allowedEntityTags = tags }
    fun forbiddenSystems(tags: Collection<String>) = apply { this.forbiddenSystemTags = tags }
    fun forbiddenEntities(tags: Collection<String>) = apply { this.forbiddenEntityTags = tags }

    fun pickSystem(): LocationAPI? {
        val validLocations = HashSet<LocationAPI>()

        locationList.forEach {
            val distanceLY = Misc.getDistanceLY(galaxyCenter, it.location)
            val inMinRange = minRange <= 0 || distanceLY >= minRange
            val inMaxRange = maxRange <= 0 || distanceLY < maxRange

            if (!(inMinRange && inMaxRange)) return@forEach
            if (!forbiddenSystemTags.isNullOrEmpty() && it.tags.any(forbiddenSystemTags!!::contains)) return@forEach
            if (!allowedSystemTags.isNullOrEmpty() && it.tags.none(allowedSystemTags!!::contains)) return@forEach

            validLocations.add(it)
        }

        return if (validLocations.isEmpty()) null else validLocations.random()
    }

    fun pickEntity(): SectorEntityToken? {
        val validEntities = HashSet<SectorEntityToken>()
        val location = pickSystem()

        location?.allEntities?.forEach {
            if (!forbiddenEntityTags.isNullOrEmpty() && it.tags.any(forbiddenEntityTags!!::contains)) return@forEach
            if (!allowedEntityTags.isNullOrEmpty() && it.tags.none(allowedEntityTags!!::contains)) return@forEach

            validEntities.add(it)
        }

        return if (validEntities.isEmpty()) null else validEntities.random()
    }
}
