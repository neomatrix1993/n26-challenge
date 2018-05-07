package com.n26.challenge.controllers;

import com.n26.challenge.dao.transactions.TransactionRequest;
import com.n26.challenge.exceptions.InvalidTransactionException;
import com.n26.challenge.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Transactions controller.
 *
 * @author swapnil.gupta
 */
@RestController
@RequestMapping("/transactions")
public class TransactionsController {
    private TransactionService transactionService;

    @Autowired
    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Void> addTransactions(@RequestBody TransactionRequest addTransactionRequest) {
        try {
            transactionService.addTransaction(addTransactionRequest);
            return ResponseEntity.created(URI.create("")).build();
        } catch (InvalidTransactionException e) {
            return ResponseEntity.noContent().build();
        }
    }
}
