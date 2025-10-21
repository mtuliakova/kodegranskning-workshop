package com.workshop.expense;

import com.workshop.expense.model.Category;
import com.workshop.expense.model.Expense;
import com.workshop.expense.service.ExpenseService;
import com.workshop.expense.store.ExpenseStore;
import com.workshop.expense.store.FileCsvExpenseStore;
import java.math.BigDecimal;
import java.time.LocalDate;

public class App {

  public static void main(String[] args) {
    ExpenseStore store = new FileCsvExpenseStore(); // CSV storage under ~/Documents/expenses/expenses.csv
    ExpenseService svc = new ExpenseService(store);

    // demo: register a few expenses
    svc.register(new Expense(LocalDate.now().with(java.time.DayOfWeek.MONDAY), "Coop",
        new BigDecimal("199.90"), Category.GROCERIES, "weekly groceries"));
    svc.register(new Expense(LocalDate.now().with(java.time.DayOfWeek.TUESDAY), "SL",
        new BigDecimal("39.00"), Category.TRANSPORT, "bus ticket"));
    svc.register(new Expense(LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY), "Espresso House",
        new BigDecimal("56.00"), Category.COFFEE, "flat white"));

    System.out.println("=== Weekly report (ISO week) ===");
    var rep = svc.weeklyTotals(LocalDate.now());
    rep.forEach((cat, sum) -> System.out.println(" - " + cat + ": " + sum));

    System.out.println("\nTop merchants this week (limit=3):");
    svc.topMerchantsThisWeek(LocalDate.now(), 3)
        .forEach((m, sum) -> System.out.println(" - " + m + ": " + sum));

    System.out.println("\nPretty summary:");
    System.out.println(svc.prettyWeeklySummary(LocalDate.now()));

    // optional cute method examples:
    System.out.println("\nAvg per day this week: " + svc.averagePerDayThisWeek(LocalDate.now()));
  }
}