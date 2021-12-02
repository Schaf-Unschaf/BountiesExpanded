package de.schafunschaf.bountiesexpanded.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormattingTools {
    public static String singularOrPlural(int number, String wordAsSingular) {
        return (number == 1) ? wordAsSingular : wordAsSingular + "s";
    }

    public static String aOrAn(String name) {
        List<Character> vowels = new ArrayList<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        return vowels.contains(name.toLowerCase(Locale.ROOT).charAt(0)) ? "an" : "a";
    }

    public static int roundWholeNumber(int number, int numPlaces) {
        double pow = Math.pow(10, numPlaces);
        return (int) (Math.round(number / pow) * pow);
    }

    public static float roundWholeNumber(float number, int numPlaces) {
        double pow = Math.pow(10, numPlaces);
        return (float) (Math.round(number / pow) * pow);
    }

    public static double roundWholeNumber(double number, int numPlaces) {
        double pow = Math.pow(10, numPlaces);
        return (Math.round(number / pow) * pow);
    }
}
