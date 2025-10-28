package com.workshop.expense.service;

import com.workshop.expense.model.Category;
import com.workshop.expense.model.Expense;
import com.workshop.expense.store.ExpenseStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseService {

  private final ExpenseStore store;
  private BudgetService budgetService;

  public ExpenseService(ExpenseStore store) {
    this.store = store;
  }

  public void setBudgetService(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  // --- core actions ---
  public void register(Expense e) {
    store.save(e);

    // Check budget after registering expense
    if (budgetService != null) {
      checkBudgetWarning(e);
    }
  }

  private void checkBudgetWarning(Expense e) {
    if (budgetService.isOverBudget(e.category(), e.date())) {
      System.out.println("WARNING: You are over budget for " + e.category());
    } else {
      BigDecimal remaining = budgetService.getRemainingBudget(e.category(), e.date());
      if (remaining.doubleValue() < budgetService.getAllBudgets().get(0).getLimit().doubleValue() * 0.9) {
        System.out.println("NOTICE: You are approaching your budget limit for " + e.category());
      }
    }
  }

  public Map<Category, BigDecimal> weeklyTotals(LocalDate anyDateInWeek) {
    return store.findByWeek(anyDateInWeek).stream()
        .collect(Collectors.groupingBy(
            Expense::category,
            Collectors.reducing(BigDecimal.ZERO, Expense::amount, BigDecimal::add)
        ));
  }

  public Map<String, BigDecimal> topMerchantsThisWeek(LocalDate anyDateInWeek, int limit) {
    Map<String, BigDecimal> totals = store.findByWeek(anyDateInWeek).stream()
        .collect(Collectors.groupingBy(
            Expense::merchant,
            Collectors.reducing(BigDecimal.ZERO, Expense::amount, BigDecimal::add)
        ));
    return totals.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .limit(limit)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (a, b) -> a, LinkedHashMap::new));
  }

  // --- cute helpers ("милые методы") ---
  public BigDecimal averagePerDayThisWeek(LocalDate anyDateInWeek) {
    var wf = WeekFields.ISO;
    var monday = anyDateInWeek.with(wf.dayOfWeek(), 1);
    var days = List.of(0, 1, 2, 3, 4, 5, 6);
    BigDecimal total = store.findByWeek(anyDateInWeek).stream()
        .map(Expense::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    return total.divide(BigDecimal.valueOf(days.size()), 2, RoundingMode.HALF_UP);
  }

  public String prettyWeeklySummary(LocalDate anyDateInWeek) {
    var wf = WeekFields.ISO;
    int week = anyDateInWeek.get(wf.weekOfWeekBasedYear());
    int year = anyDateInWeek.get(wf.weekBasedYear());
    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("sv", "SE"));
    nf.setCurrency(java.util.Currency.getInstance("SEK"));

    var totals = weeklyTotals(anyDateInWeek);
    BigDecimal grand = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

    String body = totals.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> " - " + e.getKey() + ": " + nf.format(e.getValue()))
        .collect(Collectors.joining("\n"));

    return "Week " + week + " (" + year + ")\n" + body + "\nTotal: " + nf.format(grand);
  }

  // optional: search in all
  public List<Expense> search(String text) {
    String needle = text.toLowerCase(Locale.ROOT);
    return store.findAll().stream()
        .filter(e -> e.merchant().toLowerCase(Locale.ROOT).contains(needle)
            || (!e.note().isBlank() && e.note().toLowerCase(Locale.ROOT).contains(needle)))
        .toList();
  }




  /**
   * Get expenses that exceed a certain threshold
   * Useful for finding large purchases
   */
  public List<Expense> findLargeExpenses(BigDecimal threshold) {
    return store.findAll().stream()
        .filter(e -> e.amount().compareTo(threshold) > 0)
        .sorted((e1, e2) -> e2.amount().compareTo(e1.amount())) // Descending order
        .toList();
  }
}
