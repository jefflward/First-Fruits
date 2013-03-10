package us.wardware.firstfruits.util;

import java.util.List;

public class StringUtils
{
    public static String getBestMatch(String input, List<?> dataList, boolean isCaseSensitive)
    {
        String bestMatch = null;
        Integer bestMatchPosition = null;
        for (Object item : dataList) {
            final String itemString = item.toString();
            if (itemString != null) {
                if (!isCaseSensitive && itemString.toLowerCase().contains(input.toLowerCase()))
                {
                    if (bestMatchPosition == null || itemString.toLowerCase().indexOf(input.toLowerCase()) < bestMatchPosition)
                    {
                        bestMatchPosition = itemString.toLowerCase().indexOf(input.toLowerCase()); 
                        bestMatch = itemString;
                    }
                }
                if (isCaseSensitive && itemString.contains(input))
                {
                    if (bestMatchPosition == null || itemString.indexOf(input) < bestMatchPosition)
                    {
                        bestMatchPosition = itemString.indexOf(input); 
                        bestMatch = itemString;
                    }
                }
            }
        }

        return bestMatch;
    }
}
