package com.expenseanalyzer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Saves/loads expenses to a CSV file so data survives restarts. */
public class ExpenseStorage {
    private final Path file;

    public ExpenseStorage(String filePath) {
        this.file = Path.of(filePath);
    }

    public List<Expense> load() {
        List<Expense> list = new ArrayList<>();
        if (!Files.exists(file)) return list;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                try {
                    list.add(Expense.fromCsv(line));
                } catch (RuntimeException ex) {
                    System.out.println("Skipping bad line: " + line);
                }
            }
        } catch (IOException ex) {
            System.out.println("Could not read " + file + ": " + ex.getMessage());
        }
        return list;
    }

    public void save(List<Expense> expenses) {
        List<String> lines = new ArrayList<>();
        for (Expense e : expenses) lines.add(e.toCsv());
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println("Could not save to " + file + ": " + ex.getMessage());
        }
    }
}
