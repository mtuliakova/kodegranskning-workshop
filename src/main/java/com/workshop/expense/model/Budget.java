package com.workshop.expense.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a budget for a category
 */
public class Budget {

  private Category category;
  private BigDecimal limit;
  private String period; // "weekly" or "monthly"

  // TODO: Should probably use an enum for period instead of String
  public Budget(Category category, BigDecimal limit, String period) {
    this.category = category;
    this.limit = limit;
    this.period = period;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public BigDecimal getLimit() {
    return limit;
  }

  public void setLimit(BigDecimal limit) {
    this.limit = limit;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Budget budget = (Budget) o;
    return Objects.equals(category, budget.category) &&
           Objects.equals(period, budget.period);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, period);
  }

  @Override
  public String toString() {
    return "Budget{" +
           "category=" + category +
           ", limit=" + limit +
           ", period='" + period + '\'' +
           '}';
  }
}

