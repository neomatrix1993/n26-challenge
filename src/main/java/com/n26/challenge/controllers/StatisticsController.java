package com.n26.challenge.controllers;

import com.n26.challenge.dao.statistics.Statistics;
import com.n26.challenge.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Statistics controller.
 *
 * @author swapnil.gupta
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private TransactionService transactionService;

    @Autowired
    public StatisticsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Statistics> getStatistics() {
        return ResponseEntity.ok(transactionService.getStatistics());
    }
}
