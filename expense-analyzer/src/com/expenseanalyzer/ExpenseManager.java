package com.expenseanalyzer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Core logic. The master list is ALWAYS kept in ascending id order, so binary search by id
 * is valid. Sorting returns a sorted COPY and never reorders the master list.
 */
public class ExpenseManager {

    public enum SortKey { AMOUNT, DATE, CATEGORY }

    private final List<Expense> expenses = new ArrayList<>();
    private int nextId = 1;

    // ---------- CRUD ----------

    public Expense add(LocalDate date, String category, String description, BigDecimal amount) {
        Expense e = new Expense(nextId++, date, category, description, amount);
        expenses.add(e); // ids increase, so list stays sorted by id
        return e;
    }

    public void load(List<Expense> loaded) {
        expenses.clear();
        expenses.addAll(loaded);
        expenses.sort(Comparator.comparingInt(Expense::getId));
        nextId = expenses.isEmpty() ? 1 : expenses.get(expenses.size() - 1).getId() + 1;
    }

    public List<Expense> getAll() {
        return new ArrayList<>(expenses);
    }

    public boolean remove(int id) {
        int idx = binarySearchById(id);
        if (idx < 0) return false;
        expenses.remove(idx);
        return true;
    }

    public boolean isEmpty() { return expenses.isEmpty(); }

    // ---------- SEARCHING ----------

    /** Binary search on id: O(log n). Returns index or -1. */
    private int binarySearchById(int id) {
        int lo = 0, hi = expenses.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midId = expenses.get(mid).getId();
            if (midId == id) return mid;
            if (midId < id) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }

    public Expense findById(int id) {
        int idx = binarySearchById(id);
        return idx < 0 ? null : expenses.get(idx);
    }

    /** Linear search: O(n). Case-insensitive exact category match. */
    public List<Expense> searchByCategory(String category) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(category)) result.add(e);
        }
        return result;
    }

    /** Linear search on description (substring, case-insensitive). */
    public List<Expense> searchByKeyword(String keyword) {
        List<Expense> result = new ArrayList<>();
        String k = keyword.toLowerCase();
        for (Expense e : expenses) {
            if (e.getDescription().toLowerCase().contains(k)) result.add(e);
        }
        return result;
    }

    public List<Expense> searchByDate(LocalDate date) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getDate().equals(date)) result.add(e);
        }
        return result;
    }

    /**
     * Binary search on a copy sorted by amount (lower-bound search, then collect all equal amounts).
     * Shows why binary search needs sorted data.
     */
    public List<Expense> searchByExactAmount(BigDecimal amount) {
        List<Expense> sorted = mergeSort(expenses, Comparator.comparing(Expense::getAmount));
        int lo = 0, hi = sorted.size();
        while (lo < hi) { // first index with amount >= target
            int mid = (lo + hi) >>> 1;
            if (sorted.get(mid).getAmount().compareTo(amount) < 0) lo = mid + 1; else hi = mid;
        }
        List<Expense> result = new ArrayList<>();
        while (lo < sorted.size() && sorted.get(lo).getAmount().compareTo(amount) == 0) {
            result.add(sorted.get(lo++));
        }
        return result;
    }

    // ---------- SORTING ----------

    /** Sorted copy using the built-in sort (TimSort) with a Comparator. */
    public List<Expense> sorted(SortKey key, boolean ascending) {
        Comparator<Expense> c = comparatorFor(key);
        if (!ascending) c = c.reversed();
        List<Expense> copy = new ArrayList<>(expenses);
        copy.sort(c);
        return copy;
    }

    /** Sorted copy using our own merge sort (O(n log n), stable). */
    public List<Expense> sortedWithMergeSort(SortKey key, boolean ascending) {
        Comparator<Expense> c = comparatorFor(key);
        if (!ascending) c = c.reversed();
        return mergeSort(expenses, c);
    }

    private Comparator<Expense> comparatorFor(SortKey key) {
        switch (key) {
            case AMOUNT:   return Comparator.comparing(Expense::getAmount);
            case DATE:     return Comparator.comparing(Expense::getDate);
            default:       return Comparator.comparing(Expense::getCategory, String.CASE_INSENSITIVE_ORDER);
        }
    }

    private static List<Expense> mergeSort(List<Expense> list, Comparator<Expense> c) {
        if (list.size() <= 1) return new ArrayList<>(list);
        int mid = list.size() / 2;
        List<Expense> left = mergeSort(list.subList(0, mid), c);
        List<Expense> right = mergeSort(list.subList(mid, list.size()), c);
        List<Expense> merged = new ArrayList<>(list.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            // <= keeps equal elements in original order (stable)
            if (c.compare(left.get(i), right.get(j)) <= 0) merged.add(left.get(i++));
            else merged.add(right.get(j++));
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    // ---------- ANALYSIS ----------

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Expense e : expenses) sum = sum.add(e.getAmount());
        return sum;
    }

    public BigDecimal average() {
        if (expenses.isEmpty()) return BigDecimal.ZERO;
        return total().divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
    }

    public Expense highest() {
        Expense best = null;
        for (Expense e : expenses) if (best == null || e.getAmount().compareTo(best.getAmount()) > 0) best = e;
        return best;
    }

    public Expense lowest() {
        Expense best = null;
        for (Expense e : expenses) if (best == null || e.getAmount().compareTo(best.getAmount()) < 0) best = e;
        return best;
    }

    public Map<String, BigDecimal> totalsByCategory() {
        Map<String, BigDecimal> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Expense e : expenses) map.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        return map;
    }

    public Map<LocalDate, BigDecimal> totalsByDate() {
        Map<LocalDate, BigDecimal> map = new TreeMap<>();
        for (Expense e : expenses) map.merge(e.getDate(), e.getAmount(), BigDecimal::add);
        return map;
    }
}
