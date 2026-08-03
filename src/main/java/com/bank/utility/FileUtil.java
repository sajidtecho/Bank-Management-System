package com.bank.utility;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for common file I/O operations using BufferedReader and BufferedWriter.
 */
public class FileUtil {

    /**
     * Reads all lines from a text file. If the file does not exist, returns an empty list.
     */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
        }
        return lines;
    }

    /**
     * Writes all lines to a text file (overwriting existing content).
     * Automatically creates parent directories if they don't exist.
     */
    public static void writeLines(String filePath, List<String> lines) {
        ensureFileExists(filePath);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Appends a single line to a text file.
     */
    public static void appendLine(String filePath, String line) {
        ensureFileExists(filePath);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            writer.println(line);
        } catch (IOException e) {
            System.err.println("Error appending to file " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Ensures that the file and its parent directories exist.
     */
    public static void ensureFileExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error ensuring file existence for " + filePath + ": " + e.getMessage());
        }
    }
}
