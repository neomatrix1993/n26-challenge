package com.n26.challenge.services;

import com.n26.challenge.dao.statistics.Statistics;
import com.n26.challenge.dao.transactions.TransactionRequest;
import com.n26.challenge.exceptions.InvalidTransactionException;
import com.n26.challenge.configurations.Config;
import com.n26.challenge.utils.TimeUtils;
import org.springframework.stereotype.Service;

/**
 * Transaction Service
 *
 * @author swapnil.gupta
 */

@Service
public class TransactionService {

    private final Statistics[] statistics;
    private static final int WINDOW_IN_SECONDS = (int) (Config.ROLLING_WINDOWS_MS / Config.PRECISION_IN_MS);
    private final Statistics computedStats;
    private final Object lock;

    public TransactionService() {
        this.lock = new Object();
        this.statistics = new Statistics[WINDOW_IN_SECONDS];
        this.computedStats = new Statistics();
        init();
    }

    /**
     * Function to add transaction in our memory.
     * 1. Validates transaction
     * 2. Gets a lock on the array[index]
     * 3. Computes stats and stores
     * <p>
     * If invalid throws exception to return 204.
     *
     * @param transactionRequest transactionRequest
     * @throws InvalidTransactionException InvalidTransactionException
     */
    public void addTransaction(TransactionRequest transactionRequest) throws InvalidTransactionException {
        validateTransactionTimestamp(transactionRequest);
        int index = getIndexShardedByTimestamp(transactionRequest.getTimestamp());
        synchronized (statistics[index]) {
            Statistics newStats = new Statistics(transactionRequest);
            statistics[index] = computeStats(newStats, index);
        }
    }

    /**
     * Function to retrieve statistics when polled from api.
     * The array size is constant no matter the number of polls or additions.
     * Hence the space/time complexity is constant.
     * 1. Starts iterating the array and keeps adding using validation in each step.
     * <p>
     * Using double checked locking to avoid wasteful locking in case of concurrent requests.
     *
     * @return Statistics
     */
    public Statistics getStatistics() {
        final long now = System.currentTimeMillis();
        if (computedStats.getLastUpdatedTimestamp() < now) {
            synchronized (lock) {
                if (computedStats.getLastUpdatedTimestamp() < now) {
                    computedStats.reset();
                    for (int i = 0; i < statistics.length; i++) {
                        computeStats(computedStats, i);
                    }

                    computedStats.setLastUpdatedTimestamp(now);
                }
            }
        }
        return computedStats;
    }

    private Statistics computeStats(Statistics newStats, int index) {
        Statistics existingStats = statistics[index];
        if (areStatsMergeable(existingStats, newStats)) {
            newStats.computeWithExistingStats(existingStats);
        }

        return newStats;
    }

    private boolean areStatsMergeable(Statistics existingStats, Statistics newStats) {
        return existingStats.getCount() > 0 &&
                TimeUtils.isValidTransactionTimestamp(existingStats.getLastUpdatedTimestamp()) &&
                (newStats.getCount() == 0 || newStats.getLastUpdatedTimestamp() == 0 ||
                        TimeUtils.statsInSamePrecision(existingStats, newStats));
    }

    /**
     * A precision block is named here as a quantum.
     *
     * @param index index
     */
    private void resetQuantum(int index) {
        statistics[index] = new Statistics();
    }

    private int getIndexShardedByTimestamp(long timestamp) {
        return (int) ((timestamp / Config.PRECISION_IN_MS) % WINDOW_IN_SECONDS);
    }

    private void validateTransactionTimestamp(TransactionRequest transactionRequest) throws InvalidTransactionException {
        if (!TimeUtils.isValidTransactionTimestamp(transactionRequest.getTimestamp())) {
            throw new InvalidTransactionException();
        }
    }

    private void init() {
        for (int i = 0; i < statistics.length; i++) {
            resetQuantum(i);
        }
    }
}
