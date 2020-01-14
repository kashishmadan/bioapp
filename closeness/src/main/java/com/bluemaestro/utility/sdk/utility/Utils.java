package com.bluemaestro.utility.sdk.utility;

import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils
{
    private static final int COLON_INDEX = 22;
    public static final String ISO_8601_PATTERN_1 = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static String dateToIsoString(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_PATTERN_1);
        return dateFormat.format(date);
    }

    public static String formatIso8601DateTime(Date date, TimeZone timeZone)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_PATTERN_1, Locale.US);
        return formatDateTime(date, timeZone, dateFormat);
    }

    @Nullable
    private static String formatDateTime(Date date, TimeZone timeZone, SimpleDateFormat dateFormat)
    {
        if(timeZone != null)
        {
            dateFormat.setTimeZone(timeZone);
        }
        String formatted = dateFormat.format(date);
        if(formatted != null && formatted.length() > COLON_INDEX)
        {
            formatted = formatted.substring(0, 22) + ":" + formatted.substring(22);
        }
        return formatted;
    }
}
