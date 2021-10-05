package de.schafunschaf.bountiesexpanded.util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;

public class ParsingTools {
    public static Collection<String> parseJSONArray(JSONArray jsonArray) throws JSONException {
        Collection<String> parsedStrings = new ArrayList<>();
        int arraySize = jsonArray.length();
        for (int i = 0; i < arraySize; i++)
            parsedStrings.add(jsonArray.get(i).toString());
        return parsedStrings;
    }
}
