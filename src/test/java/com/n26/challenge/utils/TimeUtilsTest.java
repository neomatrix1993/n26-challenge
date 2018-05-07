package com.n26.challenge.utils;

import com.n26.challenge.configurations.Config;
import com.n26.challenge.dao.statistics.Statistics;
import org.junit.Assert;
import org.junit.Test;

/**
 * Time utils test.
 *
 * @author swapnil.gupta
 */
public class TimeUtilsTest {

    /**
     * Times: 1525724156821L and 1525724156822L will lie in same precision block of 1 sec
     * While 1525724156821L and 1525724158821L would be 2 second apart.
     */
    @Test
    public void statsInSamePrecisionTestFor1000MS() {
        long timestamp = 1525724156821L;
        Statistics existingStats = new Statistics();
        existingStats.setLastUpdatedTimestamp(timestamp);
        Statistics newStats = new Statistics();
        newStats.setLastUpdatedTimestamp(timestamp + 1);
        Assert.assertEquals(true, TimeUtils.statsInSamePrecision(existingStats, newStats));
        existingStats.setLastUpdatedTimestamp(timestamp);
        newStats.setLastUpdatedTimestamp(timestamp + 2 * Config.PRECISION_IN_MS);
        Assert.assertEquals(false, TimeUtils.statsInSamePrecision(existingStats, newStats));
    }

    /**
     * Test if valid transaction times for 60000 ms rolling window.
     */
    @Test
    public void isValidTransactionTimestamp() {
        long now = System.currentTimeMillis();
        long expiredTime = now - Config.ROLLING_WINDOWS_MS - 1000;
        Assert.assertEquals(false, TimeUtils.isValidTransactionTimestamp(expiredTime));
        long almostBorderTime = now - Config.ROLLING_WINDOWS_MS + 1000;
        Assert.assertEquals(true, TimeUtils.isValidTransactionTimestamp(almostBorderTime));
        Assert.assertEquals(true, TimeUtils.isValidTransactionTimestamp(now));
    }
}