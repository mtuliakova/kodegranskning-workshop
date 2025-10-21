package com.workshop.expense.store;

import com.workshop.expense.model.Expense;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseStore {

  void save(Expense e);

  void saveAll(List<Expense> e);

  List<Expense> findAll();

  List<Expense> findByWeek(LocalDate anyDateInWeek);
}
