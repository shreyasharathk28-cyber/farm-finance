package com.farm.finance.controller;

import com.farm.finance.model.Income;
import com.farm.finance.model.Expense;
import com.farm.finance.repository.IncomeRepository;
import com.farm.finance.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // ===== HOME PAGE =====
    @GetMapping("/")
    public String home(Model model) {
        List<Income> incomes = incomeRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        double totalIncome = 0;
        double totalExpense = 0;

        for (Income inc : incomes) {
            totalIncome += inc.getTotalIncome();
        }
        for (Expense exp : expenses) {
            totalExpense += exp.getAmount();
        }

        double profit = totalIncome - totalExpense;

        model.addAttribute("incomes", incomes);
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("profit", profit);
        model.addAttribute("filterMessage", "📊 All Records");

        return "index";
    }

    // ===== ADD INCOME FORM =====
    @GetMapping("/add-income")
    public String showAddIncomeForm(Model model) {
        model.addAttribute("income", new Income());
        return "add-income";
    }

    // ===== SAVE INCOME =====
    @PostMapping("/save-income")
    public String saveIncome(@RequestParam String cropName,
                             @RequestParam Double quantity,
                             @RequestParam Double pricePerKg,
                             @RequestParam String date) {

        // ===== DEBUG PRINT =====
        System.out.println("=================================");
        System.out.println("📝 INCOME FORM DATA RECEIVED:");
        System.out.println("cropName: '" + cropName + "'");
        System.out.println("quantity: " + quantity);
        System.out.println("pricePerKg: " + pricePerKg);
        System.out.println("date: " + date);
        System.out.println("=================================");

        try {
            LocalDate incomeDate = LocalDate.parse(date);
            Double totalIncome = quantity * pricePerKg;

            Income income = new Income(cropName, quantity, pricePerKg, totalIncome, incomeDate);
            incomeRepository.save(income);

            System.out.println("✅ Income saved successfully!");
            return "redirect:/";

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/add-income";
        }
    }

    // ===== ADD EXPENSE FORM =====
    @GetMapping("/add-expense")
    public String showAddExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    // ===== SAVE EXPENSE =====
    @PostMapping("/save-expense")
    public String saveExpense(@RequestParam String labourName,
                              @RequestParam String workDone,
                              @RequestParam Double amount,
                              @RequestParam String date) {
        try {
            LocalDate expenseDate = LocalDate.parse(date);
            Expense expense = new Expense(labourName, workDone, amount, expenseDate);
            expenseRepository.save(expense);

            System.out.println("✅ Expense saved: " + labourName + " - ₹" + amount);
            return "redirect:/";
        } catch (Exception e) {
            System.out.println("❌ ERROR saving expense: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/add-expense";
        }
    }

    // ===== DELETE INCOME =====
    @GetMapping("/delete-income/{id}")
    public String deleteIncome(@PathVariable Long id) {
        try {
            incomeRepository.deleteById(id);
            System.out.println("🗑️ Income deleted: " + id);
        } catch (Exception e) {
            System.out.println("❌ Error deleting income: " + e.getMessage());
        }
        return "redirect:/";
    }

    // ===== DELETE EXPENSE =====
    @GetMapping("/delete-expense/{id}")
    public String deleteExpense(@PathVariable Long id) {
        try {
            expenseRepository.deleteById(id);
            System.out.println("🗑️ Expense deleted: " + id);
        } catch (Exception e) {
            System.out.println("❌ Error deleting expense: " + e.getMessage());
        }
        return "redirect:/";
    }

    // ===== FILTER BY MONTH =====
    @GetMapping("/filter/month")
    public String filterByMonth(@RequestParam String month,
                                @RequestParam String year,
                                Model model) {
        List<Income> allIncomes = incomeRepository.findAll();
        List<Expense> allExpenses = expenseRepository.findAll();

        List<Income> filteredIncomes = new ArrayList<>();
        List<Expense> filteredExpenses = new ArrayList<>();

        String monthPattern = "-" + String.format("%02d", Integer.parseInt(month)) + "-";

        for (Income inc : allIncomes) {
            String dateStr = inc.getDate().toString();
            if (dateStr.contains(monthPattern) && dateStr.contains(year)) {
                filteredIncomes.add(inc);
            }
        }

        for (Expense exp : allExpenses) {
            String dateStr = exp.getDate().toString();
            if (dateStr.contains(monthPattern) && dateStr.contains(year)) {
                filteredExpenses.add(exp);
            }
        }

        double totalIncome = 0;
        double totalExpense = 0;

        for (Income inc : filteredIncomes) {
            totalIncome += inc.getTotalIncome();
        }
        for (Expense exp : filteredExpenses) {
            totalExpense += exp.getAmount();
        }

        double profit = totalIncome - totalExpense;

        model.addAttribute("incomes", filteredIncomes);
        model.addAttribute("expenses", filteredExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("profit", profit);
        model.addAttribute("filterMessage", "📅 Month " + month + "/" + year);

        return "index";
    }

    // ===== FILTER BY WEEK =====
    @GetMapping("/filter/week")
    public String filterByWeek(@RequestParam String week,
                               @RequestParam String year,
                               Model model) {
        List<Income> allIncomes = incomeRepository.findAll();
        List<Expense> allExpenses = expenseRepository.findAll();

        List<Income> filteredIncomes = new ArrayList<>();
        List<Expense> filteredExpenses = new ArrayList<>();

        int weekNum = Integer.parseInt(week);
        int yearNum = Integer.parseInt(year);

        WeekFields weekFields = WeekFields.ISO;

        for (Income inc : allIncomes) {
            int incWeek = inc.getDate().get(weekFields.weekOfWeekBasedYear());
            int incYear = inc.getDate().getYear();
            if (incWeek == weekNum && incYear == yearNum) {
                filteredIncomes.add(inc);
            }
        }

        for (Expense exp : allExpenses) {
            int expWeek = exp.getDate().get(weekFields.weekOfWeekBasedYear());
            int expYear = exp.getDate().getYear();
            if (expWeek == weekNum && expYear == yearNum) {
                filteredExpenses.add(exp);
            }
        }

        double totalIncome = 0;
        double totalExpense = 0;

        for (Income inc : filteredIncomes) {
            totalIncome += inc.getTotalIncome();
        }
        for (Expense exp : filteredExpenses) {
            totalExpense += exp.getAmount();
        }

        double profit = totalIncome - totalExpense;

        model.addAttribute("incomes", filteredIncomes);
        model.addAttribute("expenses", filteredExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("profit", profit);
        model.addAttribute("filterMessage", "📊 Week " + week + ", " + year);

        return "index";
    }
}