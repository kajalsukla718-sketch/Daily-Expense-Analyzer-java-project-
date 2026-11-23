package com.expenseanalyzer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final ExpenseManager manager = new ExpenseManager();
    private static final ExpenseStorage storage = new ExpenseStorage("data/expenses.csv");

    public static void main(String[] args) {
        manager.load(storage.load());
        while (true) {
            printMenu();
            switch (readInt("Choice: ")) {
                case 1: addExpense(); break;
                case 2: show(manager.getAll()); break;
                case 3: deleteExpense(); break;
                case 4: sortMenu(); break;
                case 5: searchMenu(); break;
                case 6: analysis(); break;
                case 0: System.out.println("Bye."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== DAILY EXPENSE ANALYZER =====");
        System.out.println("1. Add expense");
        System.out.println("2. View all expenses");
        System.out.println("3. Delete expense (by ID)");
        System.out.println("4. Sort expenses");
        System.out.println("5. Search expenses");
        System.out.println("6. Analysis / summary");
        System.out.println("0. Exit");
    }

    private static void addExpense() {
        LocalDate date = readDate("Date (yyyy-MM-dd, blank = today): ", true);
        String category = readText("Category (e.g. Food, Travel): ");
        String description = readText("Description: ");
        BigDecimal amount = readAmount("Amount: ");
        Expense e = manager.add(date, category, description, amount);
        storage.save(manager.getAll());
        System.out.println("Added with ID " + e.getId());
    }

    private static void deleteExpense() {
        int id = readInt("ID to delete: ");
        System.out.println(manager.remove(id) ? "Deleted." : "No expense with that ID.");
        storage.save(manager.getAll());
    }

    private static void sortMenu() {
        System.out.println("Sort by: 1) Amount  2) Date  3) Category");
        int k = readInt("Choice: ");
        ExpenseManager.SortKey key;
        if (k == 1) key = ExpenseManager.SortKey.AMOUNT;
        else if (k == 2) key = ExpenseManager.SortKey.DATE;
        else if (k == 3) key = ExpenseManager.SortKey.CATEGORY;
        else { System.out.println("Invalid choice."); return; }

        boolean asc = readInt("1) Ascending  2) Descending: ") != 2;
        boolean custom = readInt("1) Built-in sort  2) Own merge sort: ") == 2;
        show(custom ? manager.sortedWithMergeSort(key, asc) : manager.sorted(key, asc));
    }

    private static void searchMenu() {
        System.out.println("Search by: 1) ID  2) Category  3) Keyword  4) Date  5) Exact amount");
        switch (readInt("Choice: ")) {
            case 1:
                Expense e = manager.findById(readInt("ID: "));
                show(e == null ? List.of() : List.of(e));
                break;
            case 2: show(manager.searchByCategory(readText("Category: "))); break;
            case 3: show(manager.searchByKeyword(readText("Keyword: "))); break;
            case 4: show(manager.searchByDate(readDate("Date (yyyy-MM-dd): ", false))); break;
            case 5: show(manager.searchByExactAmount(readAmount("Amount: "))); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void analysis() {
        if (manager.isEmpty()) { System.out.println("No data yet."); return; }
        System.out.println("Total spent   : " + manager.total());
        System.out.println("Average/entry : " + manager.average());
        System.out.println("Highest       : " + manager.highest());
        System.out.println("Lowest        : " + manager.lowest());
        System.out.println("\n-- By category --");
        for (Map.Entry<String, BigDecimal> en : manager.totalsByCategory().entrySet())
            System.out.printf("%-14s %10s%n", en.getKey(), en.getValue());
        System.out.println("\n-- By day --");
        for (Map.Entry<LocalDate, BigDecimal> en : manager.totalsByDate().entrySet())
            System.out.printf("%-14s %10s%n", en.getKey(), en.getValue());
    }

    private static void show(List<Expense> list) {
        if (list.isEmpty()) { System.out.println("No expenses found."); return; }
        System.out.printf("%-4s %-10s %-14s %-22s %10s%n", "ID", "Date", "Category", "Description", "Amount");
        System.out.println("-".repeat(65));
        for (Expense e : list) System.out.println(e);
    }

    // ---------- input helpers (reject bad input instead of crashing) ----------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("Enter a whole number."); }
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().replace(",", " ").trim();
            if (!s.isEmpty()) return s;
            System.out.println("Cannot be empty.");
        }
    }

    private static BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal a = new BigDecimal(in.nextLine().trim());
                if (a.signum() > 0) return a.setScale(2, java.math.RoundingMode.HALF_UP);
                System.out.println("Amount must be greater than 0.");
            } catch (NumberFormatException ex) { System.out.println("Enter a valid number."); }
        }
    }

    private static LocalDate readDate(String prompt, boolean blankMeansToday) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (s.isEmpty() && blankMeansToday) return LocalDate.now();
            try { return LocalDate.parse(s); }
            catch (DateTimeParseException ex) { System.out.println("Use format yyyy-MM-dd."); }
        }
    }
}
