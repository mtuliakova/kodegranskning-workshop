package com.workshop.expense.store;

import com.workshop.expense.model.Category;
import com.workshop.expense.model.Expense;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Super-simple CSV storage: date;merchant;amount;category;note
 */
public class FileCsvExpenseStore implements ExpenseStore {

  private final Path dir;
  private final Path file;

  public FileCsvExpenseStore() {
    String home = System.getProperty("user.home");
    this.dir = Paths.get(home, "Documents", "expenses");
    this.file = dir.resolve("expenses.csv");
    try {
      if (Files.notExists(dir)) {
        Files.createDirectories(dir);
      }
      if (Files.notExists(file)) {
        Files.createFile(file);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to init storage", e);
    }
  }

  @Override
  public void save(Expense e) {
    String line = toCsv(e) + System.lineSeparator();
    try {
      Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public void saveAll(List<Expense> list) {
    String payload =
        list.stream().map(this::toCsv).collect(Collectors.joining(System.lineSeparator()))
            + System.lineSeparator();
    try {
      Files.writeString(file, payload, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public List<Expense> findAll() {
    try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      return br.lines().filter(s -> !s.isBlank()).map(this::fromCsv).toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public List<Expense> findByWeek(LocalDate anyDateInWeek) {
    var all = findAll();
    var wf = WeekFields.ISO;
    int w = anyDateInWeek.get(wf.weekOfWeekBasedYear());
    int y = anyDateInWeek.get(wf.weekBasedYear());
    return all.stream().filter(e -> {
      int ew = e.date().get(wf.weekOfWeekBasedYear());
      int ey = e.date().get(wf.weekBasedYear());
      return ew == w && ey == y;
    }).toList();
  }

  // naive CSV (no escaping quotes/semicolons inside fields; keep notes simple)
  private String toCsv(Expense e) {
    return String.join(";",
        e.date().toString(),
        e.merchant().replace(";", ","),  // avoid delimiter break
        e.amount().toPlainString(),
        e.category().name(),
        e.note().replace(";", ","));
  }

  private Expense fromCsv(String line) {
    String[] p = line.split(";", -1);
    if (p.length < 5) {
      throw new IllegalArgumentException("Bad CSV: " + line);
    }
    return new Expense(LocalDate.parse(p[0]), p[1], new BigDecimal(p[2]),
        Category.valueOf(p[3]), p[4]);
  }
}
