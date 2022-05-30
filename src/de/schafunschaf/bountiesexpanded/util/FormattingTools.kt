package de.schafunschaf.bountiesexpanded.util

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.util.Misc
import java.util.*

object FormattingTools {

    fun singularOrPlural(number: Int, wordAsSingular: String): String {
        return if (number == 1) wordAsSingular else "${wordAsSingular}s"
    }

    fun aOrAn(name: String): String {
        val vowels: MutableList<Char> = ArrayList()
        vowels.add('a')
        vowels.add('e')
        vowels.add('i')
        vowels.add('o')
        vowels.add('u')
        return if (vowels.contains(name.lowercase()[0])) "an" else "a"
    }

    fun capitalizeFirst(string: String): String {
        return string.substring(0, 1).uppercase(Locale.getDefault()) + string.substring(1)
            .lowercase(Locale.getDefault())
    }

    fun formatCredits(creditValue: Float): String {
        val toLarge = creditValue > 1000000000
        val postFix = if (toLarge) "k " else ""
        return Misc.getFormat().format((creditValue.toInt() / if (toLarge) 1000 else 1).toLong()) + postFix + Strings.C
    }

    fun parseInteger(string: String, defaultValue: Int): Int {
        var parsedValue = defaultValue
        try {
            parsedValue = string.toInt()
        } catch (exception: NumberFormatException) {
            Global.getLogger(this::class.java).error(exception)
        }
        return parsedValue
    }
}