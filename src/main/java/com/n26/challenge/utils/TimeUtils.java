package com.n26.challenge.utils;

import com.n26.challenge.configurations.Config;
import com.n26.challenge.dao.statistics.Statistics;

/**
 * Time utilities.
 *
 * @author swapnil.gupta
 */
public class TimeUtils {
    private TimeUtils() {
    }

    public static boolean statsInSamePrecision(Statistics existingStats, Statistics newStats) {
        return newStats.getLastUpdatedTimestamp() / Config.PRECISION_IN_MS ==
                existingStats.getLastUpdatedTimestamp() / Config.PRECISION_IN_MS;
    }

    public static boolean isValidTransactionTimestamp(long timestamp) {
        return timestamp >= getValidTimeRange();
    }

    private static long getValidTimeRange() {
        return System.currentTimeMillis() - Config.ROLLING_WINDOWS_MS;
    }
}
