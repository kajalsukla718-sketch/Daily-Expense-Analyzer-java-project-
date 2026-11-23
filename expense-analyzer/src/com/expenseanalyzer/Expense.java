package com.expenseanalyzer;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A single expense record. Immutable. */
public class Expense {
    private final int id;
    private final LocalDate date;
    private final String category;
    private final String description;
    private final BigDecimal amount; // BigDecimal: double causes rounding errors with money

    public Expense(int id, LocalDate date, String category, String description, BigDecimal amount) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }

    /** CSV line. Commas are stripped from text fields on input, so a plain split is safe. */
    public String toCsv() {
        return id + "," + date + "," + category + "," + description + "," + amount;
    }

    public static Expense fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length != 5) {
            throw new IllegalArgumentException("Malformed line: " + line);
        }
        return new Expense(Integer.parseInt(p[0].trim()), LocalDate.parse(p[1].trim()),
                p[2].trim(), p[3].trim(), new BigDecimal(p[4].trim()));
    }

    @Override
    public String toString() {
        return String.format("%-4d %-10s %-14s %-22s %10s", id, date, category, description, amount.toPlainString());
    }
}
