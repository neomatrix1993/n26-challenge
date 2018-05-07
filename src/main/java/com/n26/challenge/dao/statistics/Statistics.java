package com.n26.challenge.dao.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.n26.challenge.dao.transactions.TransactionRequest;
import lombok.Data;

/**
 * Transaction statistics
 *
 * @author swapnil.gupta
 */
@Data
public class Statistics {
    private double sum;
    private double avg;
    private double max;
    private double min;
    private long count;

    @JsonIgnore
    private long lastUpdatedTimestamp;

    public Statistics() {
        reset();
    }

    public Statistics(TransactionRequest transactionRequest) {
        this.sum = transactionRequest.getAmount();
        this.avg = transactionRequest.getAmount();
        this.max = transactionRequest.getAmount();
        this.min = transactionRequest.getAmount();
        this.count = 1;
        this.lastUpdatedTimestamp = transactionRequest.getTimestamp();
    }

    public void reset() {
        this.sum = 0;
        this.avg = 0;
        this.max = Long.MIN_VALUE;
        this.min = Long.MAX_VALUE;
        this.count = 0;
        this.lastUpdatedTimestamp = 0;
    }

    public void computeWithExistingStats(Statistics existingStats) {
        this.avg = ((existingStats.avg * existingStats.count) + (this.avg * this.count)) / (existingStats.count + this.count);
        this.sum += existingStats.sum;
        this.count += existingStats.count;
        this.max = Math.max(this.max, existingStats.max);
        this.min = Math.min(this.min, existingStats.min);
    }
}
