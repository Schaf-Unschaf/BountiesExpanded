package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

enum class DeadOrAlive {
    DEAD,
    ALIVE,
    DEAD_OR_ALIVE;

    override fun toString(): String {
        return name.replace("_", " ")
    }
}