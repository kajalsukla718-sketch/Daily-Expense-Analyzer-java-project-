# Daily Expense Analyzer (Java)

A console application to record daily expenses and analyze them. Built to demonstrate
**ArrayList**, **sorting** and **searching** algorithms in core Java (no external libraries).

## Features
- Add, view and delete expenses (data saved to `data/expenses.csv`)
- **Sorting** by amount, date or category (ascending/descending)
  - built-in `List.sort` with `Comparator`
  - hand-written **merge sort** (O(n log n), stable)
- **Searching**
  - **Binary search** by ID (O(log n)) and by exact amount (on a sorted copy)
  - **Linear search** by category, keyword, date (O(n))
- Analysis: total, average, highest, lowest, totals per category and per day

## Design decisions
- `BigDecimal` for money. `double` produces rounding errors (`0.1 + 0.2 != 0.3`).
- The master `ArrayList` always stays in ID order, so binary search by ID is valid. Sorting
  returns a sorted **copy**; it never reorders the master list.
- Input is validated; bad input re-prompts instead of crashing.

## Project structure
```
src/com/expenseanalyzer/
  Expense.java         model (immutable)
  ExpenseManager.java  ArrayList logic, sorting, searching, analysis
  ExpenseStorage.java  CSV load/save
  Main.java            console menu
```

## Run (Java 11+)
```bash
javac -d out $(find src -name "*.java")
java -cp out com.expenseanalyzer.Main
```

## Sample session
```
1. Add expense  ->  2026-11-23, Food, Lunch, 120.50
6. Analysis     ->  Total spent: 120.50 ...
```

## Known limitations
- Single user, flat CSV file (no database), no concurrency handling.
- Linear searches are fine for thousands of records, not millions.

## Possible improvements
- Monthly/weekly reports, budget limits per category
- Unit tests (JUnit) for `ExpenseManager`
- Export to Excel / GUI (JavaFX) / Spring Boot REST API
