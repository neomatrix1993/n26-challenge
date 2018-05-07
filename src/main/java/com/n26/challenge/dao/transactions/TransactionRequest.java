package com.n26.challenge.dao.transactions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Request pojo to add transaction
 *
 * @author swapnil.gupta
 */
@Data
@Builder
@AllArgsConstructor
public class TransactionRequest {
    private double amount;
    private long timestamp;
}
