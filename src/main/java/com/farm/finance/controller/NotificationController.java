package com.farm.finance.controller;

import com.farm.finance.model.User;
import com.farm.finance.service.NotificationService;
import com.farm.finance.repository.IncomeRepository;
import com.farm.finance.repository.ExpenseRepository;
import com.farm.finance.model.Income;
import com.farm.finance.model.Expense;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // ===== SEND TEST NOTIFICATION =====
    @GetMapping("/send-notification")
    public String showNotificationPage() {
        return "send-notification";
    }

    @PostMapping("/send-notification")
    public String sendNotification(@RequestParam String email,
                                   @RequestParam String subject,
                                   @RequestParam String message,
                                   RedirectAttributes redirectAttributes) {
        notificationService.sendEmail(email, subject, message);
        redirectAttributes.addFlashAttribute("success", "✅ Notification sent successfully!");
        return "redirect:/send-notification";
    }

    // ===== SEND BUDGET ALERT =====
    @GetMapping("/send-budget-alert")
    public String sendBudgetAlert(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Expense> expenses = expenseRepository.findAll();
        double totalExpense = 0;
        for (Expense exp : expenses) {
            if (exp.getDate().getMonth() == LocalDate.now().getMonth()) {
                totalExpense += exp.getAmount();
            }
        }

        if (totalExpense > 10000) {
            notificationService.sendBudgetAlert(user.getEmail(), "All Labour", totalExpense);
        }

        return "redirect:/";
    }

    // ===== SEND WEEKLY SUMMARY =====
    @GetMapping("/send-weekly-summary")
    public String sendWeeklySummary(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        WeekFields weekFields = WeekFields.ISO;
        int currentWeek = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        int currentYear = LocalDate.now().getYear();

        double income = 0;
        double expense = 0;

        for (Income inc : incomeRepository.findAll()) {
            int incWeek = inc.getDate().get(weekFields.weekOfWeekBasedYear());
            int incYear = inc.getDate().getYear();
            if (incWeek == currentWeek && incYear == currentYear) {
                income += inc.getTotalIncome();
            }
        }

        for (Expense exp : expenseRepository.findAll()) {
            int expWeek = exp.getDate().get(weekFields.weekOfWeekBasedYear());
            int expYear = exp.getDate().getYear();
            if (expWeek == currentWeek && expYear == currentYear) {
                expense += exp.getAmount();
            }
        }

        double profit = income - expense;
        notificationService.sendWeeklySummary(user.getEmail(), income, expense, profit);

        return "redirect:/";
    }

    // ===== CHECK BUDGET =====
    @GetMapping("/check-budget")
    public String checkBudget(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Expense> expenses = expenseRepository.findAll();
        double monthlyBudget = 10000;
        double monthlyExpense = 0;
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        for (Expense exp : expenses) {
            if (exp.getDate().getMonthValue() == currentMonth &&
                    exp.getDate().getYear() == currentYear) {
                monthlyExpense += exp.getAmount();
            }
        }

        boolean budgetExceeded = monthlyExpense > monthlyBudget;
        double exceededAmount = budgetExceeded ? (monthlyExpense - monthlyBudget) : 0;

        model.addAttribute("monthlyBudget", monthlyBudget);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("budgetExceeded", budgetExceeded);
        model.addAttribute("exceededAmount", exceededAmount);
        model.addAttribute("user", user);

        return "budget-check";
    }
}