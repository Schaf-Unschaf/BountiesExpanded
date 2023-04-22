package de.schafunschaf.bountiesexpanded.data.ships

val RARE_FLAGSHIPS = HashMap<String, RareFlagshipData>()

class RareFlagshipData(
    private var flagshipID: String,
    private var flagshipVariantID: String,
    private var factionIDs: Set<String>,
    private var weight: Float
)