package com.workshop.expense.service;

import com.workshop.expense.model.Budget;
import com.workshop.expense.model.Category;
import com.workshop.expense.model.Expense;
import com.workshop.expense.store.ExpenseStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing budgets and checking spending against limits
 */
public class BudgetService {

  private ExpenseStore store;
  private List<Budget> budgets = new ArrayList<>();

  public BudgetService(ExpenseStore store) {
    this.store = store;
  }

  /**
   * Add a new budget
   */
  public void addBudget(Budget budget) {
    // Bug: No validation if budget already exists
    budgets.add(budget);
  }

  /**
   * Get all budgets
   */
  public List<Budget> getAllBudgets() {
    // Bug: Returning mutable list - caller can modify internal state
    return budgets;
  }

  /**
   * Check if category spending exceeds budget
   * @param category the category to check
   * @param date any date in the period to check
   * @return true if over budget
   */
  public boolean isOverBudget(Category category, LocalDate date) {
    Budget budget = null;
    for (int i = 0; i < budgets.size(); i++) {
      if (budgets.get(i).getCategory() == category) {
        budget = budgets.get(i);
        break;
      }
    }

    if (budget == null) {
      return false; // No budget set
    }

    BigDecimal spent = calculateSpending(category, date, budget.getPeriod());

    // Bug: Using compareTo incorrectly
    return spent.compareTo(budget.getLimit()) == 1;
  }

  /**
   * Calculate total spending for a category in a period
   */
  private BigDecimal calculateSpending(Category category, LocalDate date, String period) {
    List<Expense> expenses;

    if (period.equals("weekly")) {
      expenses = store.findByWeek(date);
    } else {
      // Bug: Monthly calculation not implemented properly
      expenses = store.findAll();
    }

    BigDecimal total = BigDecimal.ZERO;
    for (Expense e : expenses) {
      if (e.category() == category) {
        total = total.add(e.amount());
      }
    }

    return total;
  }

  /**
   * Get budget status for all categories
   * Returns a map with category name and percentage of budget used
   */
  public Map<String, Double> getBudgetStatus(LocalDate date) {
    Map<String, Double> status = new HashMap<>();

    for (Budget budget : budgets) {
      BigDecimal spent = calculateSpending(budget.getCategory(), date, budget.getPeriod());
      // Potential bug: Division by zero not handled
      double percentage = spent.doubleValue() / budget.getLimit().doubleValue() * 100;
      status.put(budget.getCategory().toString(), percentage);
    }

    return status;
  }

  /**
   * Get remaining budget for a category
   */
  public BigDecimal getRemainingBudget(Category category, LocalDate date) {
    for (Budget budget : budgets) {
      if (budget.getCategory().equals(category)) {
        BigDecimal spent = calculateSpending(category, date, budget.getPeriod());
        return budget.getLimit().subtract(spent);
      }
    }
    // Bug: Returns null instead of Optional or throwing exception
    return null;
  }

  // Good practice: Clear documentation
  /**
   * Removes all budgets from the service.
   * This operation cannot be undone.
   */
  public void clearBudgets() {
    budgets.clear();
  }
}

