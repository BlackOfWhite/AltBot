package org.logic.utils;

import java.util.Date;

public class TimeUtils {

    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Timestamp from N hours in the past.
     *
     * @param hours
     * @return
     */
    public static long getTimestampPast(int hours) {
        return new Date(getTimestamp() - 60 * 1000 * 48).getTime();
    }
}
