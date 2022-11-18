package com.demo.gallery;

import java.util.Calendar;

/**
 *
 * description: 
 */
public final class GalleryUtils {
    private static final int DAY = 24 * 60 * 60 * 1000;

    public static TitleType offsetDays(long time1, long time2) {
        if (Math.abs(time1 - time2) / (DAY) > 8) {
            return TitleType.EARLIER;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time1);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(time2);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);
        int offsetDays = Math.abs(day1 - day2);
        if (offsetDays == 0) {
            return TitleType.TODAY;
        } else if (offsetDays < 7) {
            return TitleType.LAST_7_DAYS;
        }
        return TitleType.EARLIER;
    }

}
