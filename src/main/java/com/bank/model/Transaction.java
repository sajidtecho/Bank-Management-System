package com.bank.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single transaction performed on a bank account.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private LocalDateTime dateTime;
    private TransactionType type;
    private double amount;
    private double balanceAfter;

    public Transaction(String transactionId, TransactionType type, double amount, double balanceAfter) {
        this.transactionId = transactionId;
        this.dateTime = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public Transaction(String transactionId, LocalDateTime dateTime, TransactionType type, double amount, double balanceAfter) {
        this.transactionId = transactionId;
        this.dateTime = dateTime;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
}
