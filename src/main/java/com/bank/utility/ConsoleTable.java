package com.bank.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to print structured tabular data dynamically formatted in the console.
 */
public class ConsoleTable {
    private final List<String> headers;
    private final List<List<String>> rows;

    public ConsoleTable() {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public void setHeaders(String... headers) {
        for (String header : headers) {
            this.headers.add(header);
        }
    }

    public void addRow(String... fields) {
        List<String> row = new ArrayList<>();
        for (String field : fields) {
            row.add(field == null ? "" : field);
        }
        rows.add(row);
    }

    public void print() {
        if (headers.isEmpty() && rows.isEmpty()) {
            System.out.println("[Table is empty]");
            return;
        }

        int columnsCount = headers.size();
        if (columnsCount == 0 && !rows.isEmpty()) {
            columnsCount = rows.get(0).size();
        }

        int[] colWidths = new int[columnsCount];

        // Calculate column widths based on headers
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = Math.max(colWidths[i], headers.get(i).length());
        }

        // Calculate column widths based on rows
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(row.size(), columnsCount); i++) {
                colWidths[i] = Math.max(colWidths[i], row.get(i).length());
            }
        }

        // Generate line separator
        StringBuilder separatorBuilder = new StringBuilder();
        separatorBuilder.append("+");
        for (int width : colWidths) {
            separatorBuilder.append("-".repeat(width + 2)).append("+");
        }
        String separator = separatorBuilder.toString();

        System.out.println(separator);

        // Print headers
        if (!headers.isEmpty()) {
            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("|");
            for (int i = 0; i < headers.size(); i++) {
                String val = headers.get(i);
                headerBuilder.append(" ").append(val).append(" ".repeat(colWidths[i] - val.length() + 1)).append("|");
            }
            System.out.println(headerBuilder.toString());
            System.out.println(separator);
        }

        // Print rows
        for (List<String> row : rows) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("|");
            for (int i = 0; i < columnsCount; i++) {
                String val = i < row.size() ? row.get(i) : "";
                rowBuilder.append(" ").append(val).append(" ".repeat(colWidths[i] - val.length() + 1)).append("|");
            }
            System.out.println(rowBuilder.toString());
        }

        System.out.println(separator);
    }
}
