package de.schafunschaf.bountiesexpanded.util

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import java.util.*

object ShipUtils {

    fun generateShipNameWithClass(fleetMember: FleetMemberAPI?): String {
        if (fleetMember == null) {
            return "NO SHIP FOR NAME AND CLASS"
        }
        val shipName: String = fleetMember.shipName
        val shipClass: String = fleetMember.hullSpec.hullNameWithDashClass
        val shipDesignation: String = fleetMember.hullSpec.designation.lowercase(Locale.getDefault())
        val shipType = String.format("%s %s", shipClass, shipDesignation)
        return String.format("%s, %s", shipName, shipType)
    }

    fun convertSizeToString(hullSize: ShipAPI.HullSize): String {
        return if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
            "Capital"
        } else {
            FormattingTools.capitalizeFirst(hullSize.toString())
        }
    }
}