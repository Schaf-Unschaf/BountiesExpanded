package de.schafunschaf.bountiesexpanded.campaign.intel.bounties

import java.awt.Color

enum class Difficulty(val color: Color, val multiplier: Float) {
    ONE_STAR(Color(50, 200, 0, 255), 1f),
    TWO_STAR(Color(150, 200, 0, 255), 1.3f),
    THREE_STAR(Color(200, 200, 0, 255), 1.8f),
    FOUR_STAR(Color(255, 150, 0, 255), 2.3f),
    FIVE_STAR(Color(255, 50, 0, 255), 3f)
}