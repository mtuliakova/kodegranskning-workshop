package com.workshop.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable expense entry.
 */
public record Expense(
    LocalDate date,
    String merchant,
    BigDecimal amount,   // SEK
    Category category,
    String note
) {

  public Expense {
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(merchant, "merchant");
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(category, "category");
    if (merchant.isBlank()) {
      throw new IllegalArgumentException("merchant is blank");
    }
    if (amount.scale() > 2) {
      amount = amount.setScale(2); // normalize to 2 decimals
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
    if (note == null) {
      note = "";
    }
  }

  /**
   * Convenience helpers
   */
  public boolean is(Category c) {
    return category == c;
  }

  public Expense withNote(String newNote) {
    return new Expense(date, merchant, amount, category, newNote);
  }
}
