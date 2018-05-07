package com.n26.challenge.services;

import com.n26.challenge.dao.statistics.Statistics;
import com.n26.challenge.dao.transactions.TransactionRequest;
import com.n26.challenge.exceptions.InvalidTransactionException;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


/**
 * Transaction service test.
 *
 * @author swapnil.gupta
 */
public class TransactionServiceTest {

    /**
     * Compare empty stats without entering data by comparing total count.
     */
    @Test
    public void getEmptyStatsTest() {
        TransactionService transactionService = new TransactionService();
        Statistics actualStatistics = transactionService.getStatistics();
        Statistics expectedStatistics = new Statistics();
        Assert.assertEquals(expectedStatistics.getCount(), actualStatistics.getCount());
    }


    /**
     * Check invalid/expired transaction exception
     *
     * @throws InvalidTransactionException InvalidTransactionException
     */
    @Test(expected = InvalidTransactionException.class)
    public void invalidTransactionTest() throws InvalidTransactionException {
        TransactionService transactionService = new TransactionService();
        long expiredTimestamp = 1L;
        TransactionRequest transactionRequest = new TransactionRequest(10.3, expiredTimestamp);
        transactionService.addTransaction(transactionRequest);
    }

    /**
     * Add single transaction and compare it to expected result.
     *
     * @throws InvalidTransactionException InvalidTransactionException
     */
    @Test
    public void addNewTransactionTest() throws InvalidTransactionException {
        TransactionService transactionService = new TransactionService();
        long now = System.currentTimeMillis();
        TransactionRequest transactionRequest = new TransactionRequest(10.3, now);
        transactionService.addTransaction(transactionRequest);
        Statistics actualStatistics = transactionService.getStatistics();
        assertEquals(transactionRequest.getAmount(), actualStatistics.getSum());
        assertEquals(transactionRequest.getAmount(), actualStatistics.getAvg());
        assertEquals(transactionRequest.getAmount(), actualStatistics.getMin());
        assertEquals(transactionRequest.getAmount(), actualStatistics.getMax());
        assertEquals(1L, actualStatistics.getCount());
    }

    /**
     * Add multiple transactions and compare it with expected results.
     *
     * @throws InvalidTransactionException InvalidTransactionException
     */
    @Test
    public void addMultipleTransactionsTest() throws InvalidTransactionException {
        TransactionService transactionService = new TransactionService();
        long now = System.currentTimeMillis();
        TransactionRequest request1 = new TransactionRequest(1.0, now - 1000);
        TransactionRequest request2 = new TransactionRequest(2.0, now - 1500);
        TransactionRequest request3 = new TransactionRequest(3.0, now - 2000);
        TransactionRequest request4 = new TransactionRequest(4.0, now - 3000);
        // one outdated request
        TransactionRequest request5 = new TransactionRequest(4.0, now - 65000);
        double sum = request1.getAmount() + request2.getAmount() + request3.getAmount() + request4.getAmount();
        double avg = sum / 4.0;
        double max = 4.0;
        double min = 1.0;
        long count = 4L;
        transactionService.addTransaction(request1);
        transactionService.addTransaction(request2);
        transactionService.addTransaction(request3);
        transactionService.addTransaction(request4);
        try {
            transactionService.addTransaction(request5);
        } catch (InvalidTransactionException ignored) {
        }

        Statistics statistics = transactionService.getStatistics();
        assertEquals(sum, statistics.getSum());
        assertEquals(avg, statistics.getAvg());
        assertEquals(max, statistics.getMax());
        assertEquals(min, statistics.getMin());
        assertEquals(count, statistics.getCount());
    }

    /**
     * Run a sample test of 1000 data points.
     * When added double loses precision when decimals numbers with huge trail are there.
     * So we use precision in assertion to make sure it lies within the range.
     *
     * @throws InvalidTransactionException InvalidTransactionException
     */
    @Test
    public void addSampleSetOfTransactionsTest() throws InvalidTransactionException {
        TransactionService transactionService = new TransactionService();
        long now = System.currentTimeMillis();
        double delta = 0.001;
        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        long count = 1000;

        for (int i = 0; i < count; i++) {
            double amount = Math.random() * 1000;
            sum += amount;
            max = Math.max(max, amount);
            min = Math.min(min, amount);
            long variable30Seconds = (long) (Math.random() * 100) % 30;
            TransactionRequest transactionRequest = new TransactionRequest(amount, now - variable30Seconds * 1000);
            transactionService.addTransaction(transactionRequest);
        }

        double avg = sum / count;
        Statistics statistics = transactionService.getStatistics();
        assertEquals(sum, statistics.getSum(), delta);
        assertEquals(avg, statistics.getAvg(), delta);
        assertEquals(max, statistics.getMax(), delta);
        assertEquals(min, statistics.getMin(), delta);
        assertEquals(count, statistics.getCount());
    }

    /**
     * To test that when statistics are fetched out of rolling window they expire.
     * Here 3 request are about to expire in 10, 20 and 30 ms.
     * Fetching after 100 ms, we would get empty stats.
     * <p>
     * Could also make fake time service just for test case purpose to rewind/forward/freeze time.
     * But right now for the sake of simplicity using TimeUtils and Thread.sleep with short duration.
     *
     * @throws InvalidTransactionException InvalidTransactionException
     * @throws InterruptedException        InterruptedException
     */
    @Test
    public void getEmptyStatisticsAfterTimeHasPassedTest() throws InvalidTransactionException, InterruptedException {
        TransactionService transactionService = new TransactionService();
        long now = System.currentTimeMillis();
        // requests about to expire in 10, 20, 30 ms
        TransactionRequest request1 = new TransactionRequest(10, now - 59990);
        TransactionRequest request2 = new TransactionRequest(20, now - 59980);
        TransactionRequest request3 = new TransactionRequest(5, now - 59970);
        transactionService.addTransaction(request1);
        transactionService.addTransaction(request2);
        transactionService.addTransaction(request3);
        // the expired events shouldn't be fetched.
        Thread.sleep(100);
        Statistics statisticsAfter5MS = transactionService.getStatistics();
        assertEquals(0, statisticsAfter5MS.getCount());
    }
}