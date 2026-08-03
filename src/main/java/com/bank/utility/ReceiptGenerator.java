package com.bank.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to generate and write formatted transaction receipts to text files.
 */
public class ReceiptGenerator {

    private static final String RECEIPTS_DIR = "receipts";

    /**
     * Generates a transaction receipt text file inside the receipts/ directory.
     */
    public static void generateReceipt(
            String txnId,
            String customerName,
            String accountNumber,
            String txnType,
            double amount,
            double prevBalance,
            double currentBalance,
            String status
    ) {
        String fileName = RECEIPTS_DIR + "/" + txnId + ".txt";
        FileUtil.ensureFileExists(fileName);

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            writer.println("==========================================");
            writer.println("         BANK MANAGEMENT SYSTEM           ");
            writer.println("==========================================");
            writer.printf("Transaction ID   : %s\n", txnId);
            writer.printf("Customer Name    : %s\n", customerName);
            writer.printf("Account Number   : %s\n", accountNumber);
            writer.printf("Transaction Type : %s\n", txnType);
            writer.printf("Amount           : INR %,.2f\n", amount);
            writer.printf("Previous Balance : INR %,.2f\n", prevBalance);
            writer.printf("Current Balance  : INR %,.2f\n", currentBalance);
            writer.printf("Date             : %s\n", date);
            writer.printf("Time             : %s\n", time.format(timeFormatter));
            writer.printf("Status           : %s\n", status);
            writer.println("==========================================");
            writer.println("   Thank you for banking with us!        ");
            writer.println("==========================================");
        } catch (IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
        }
    }
}
