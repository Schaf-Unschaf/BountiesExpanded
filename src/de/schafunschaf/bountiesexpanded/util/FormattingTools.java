package de.schafunschaf.bountiesexpanded.util;

import java.util.ArrayList;
import java.util.List;

public class FormattingTools {
    public static String singularOrPlural(int number) {
        return (number == 1) ? "" : "s";
    }

    public static String aOrAn(String name) {
        List<Character> vowels = new ArrayList<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        return vowels.contains(name.charAt(0)) ? "an" : "a";
    }
}
