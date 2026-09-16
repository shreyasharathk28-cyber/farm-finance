package com.farm.finance.controller;

import com.farm.finance.model.Income;
import com.farm.finance.model.Expense;
import com.farm.finance.model.User;
import com.farm.finance.repository.IncomeRepository;
import com.farm.finance.repository.ExpenseRepository;
import com.farm.finance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.farm.finance.service.WhatsAppService;
import com.farm.finance.service.FileUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
public class HomeController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private FileUploadService fileUploadService;


    // ===== HOME PAGE =====
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

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
        model.addAttribute("user", user);

        return "index";
    }

    // ===== ADD INCOME FORM =====
    @GetMapping("/add-income")
    public String showAddIncomeForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("income", new Income());
        return "add-income";
    }

    // ===== SAVE INCOME =====
    @PostMapping("/save-income")
    public String saveIncome(@RequestParam String cropName,
                             @RequestParam Double quantity,
                             @RequestParam Double pricePerKg,
                             @RequestParam String date,
                             @RequestParam(value = "photo", required = false) MultipartFile photo,
                             HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            LocalDate incomeDate = LocalDate.parse(date);
            Double totalIncome = quantity * pricePerKg;

            Income income = new Income(cropName, quantity, pricePerKg, totalIncome, incomeDate);

            // ===== UPLOAD PHOTO (NEW!) =====
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = fileUploadService.uploadPhoto(photo);
                income.setPhotoUrl(photoUrl);
                System.out.println("✅ Photo uploaded: " + photoUrl);
            }

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
    public String showAddExpenseForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    // ===== SAVE EXPENSE =====
    @PostMapping("/save-expense")
    public String saveExpense(@RequestParam String labourName,
                              @RequestParam String workDone,
                              @RequestParam Double amount,
                              @RequestParam String date,
                              @RequestParam(value = "photo", required = false) MultipartFile photo,
                              HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            LocalDate expenseDate = LocalDate.parse(date);
            Expense expense = new Expense(labourName, workDone, amount, expenseDate);

            // ===== UPLOAD PHOTO (NEW!) =====
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = fileUploadService.uploadPhoto(photo);
                expense.setPhotoUrl(photoUrl);
                System.out.println("✅ Photo uploaded: " + photoUrl);
            }

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
    public String deleteIncome(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

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
    public String deleteExpense(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

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
                                Model model,
                                HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

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
        model.addAttribute("user", user);

        return "index";
    }

    // ===== FILTER BY WEEK =====
    @GetMapping("/filter/week")
    public String filterByWeek(@RequestParam String week,
                               @RequestParam String year,
                               Model model,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

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
        model.addAttribute("user", user);

        return "index";
    }

    // ============================================================
    // ========== DASHBOARD PAGE ==========
    // ============================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        System.out.println("===DASHBOARD CONTROLLER CALLED===");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Income> incomes = incomeRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        // ===== 1. Monthly Data for Bar Chart =====
        Map<String, Double[]> monthlyMap = new LinkedHashMap<>();

        for (Income inc : incomes) {
            String month = inc.getDate().toString().substring(0, 7);
            if (!monthlyMap.containsKey(month)) {
                monthlyMap.put(month, new Double[]{0.0, 0.0});
            }
            monthlyMap.get(month)[0] += inc.getTotalIncome();
        }

        for (Expense exp : expenses) {
            String month = exp.getDate().toString().substring(0, 7);
            if (!monthlyMap.containsKey(month)) {
                monthlyMap.put(month, new Double[]{0.0, 0.0});
            }
            monthlyMap.get(month)[1] += exp.getAmount();
        }

        List<Map<String, Object>> monthlyData = new ArrayList<>();
        for (Map.Entry<String, Double[]> entry : monthlyMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("month", entry.getKey());
            data.put("income", entry.getValue()[0]);
            data.put("expense", entry.getValue()[1]);
            monthlyData.add(data);
        }

        // ===== 2. Expense Breakdown for Pie Chart =====
        Map<String, Double> expenseMap = new HashMap<>();
        for (Expense exp : expenses) {
            String key = exp.getWorkDone();
            if (!expenseMap.containsKey(key)) {
                expenseMap.put(key, 0.0);
            }
            expenseMap.put(key, expenseMap.get(key) + exp.getAmount());
        }

        List<Map<String, Object>> expenseBreakdown = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expenseMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", entry.getKey());
            data.put("value", entry.getValue());
            expenseBreakdown.add(data);
        }

        // ===== 3. Profit Trend =====
        List<Map<String, Object>> profitTrend = new ArrayList<>();
        for (Map.Entry<String, Double[]> entry : monthlyMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("month", entry.getKey());
            data.put("profit", entry.getValue()[0] - entry.getValue()[1]);
            profitTrend.add(data);
        }

        // ===== 4. Top Crops =====
        Map<String, Double> cropMap = new HashMap<>();
        for (Income inc : incomes) {
            String crop = inc.getCropName();
            if (!cropMap.containsKey(crop)) {
                cropMap.put(crop, 0.0);
            }
            cropMap.put(crop, cropMap.get(crop) + inc.getTotalIncome());
        }

        List<Map<String, Object>> cropData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : cropMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", entry.getKey());
            data.put("value", entry.getValue());
            cropData.add(data);
        }

        // ===== 5. Yearly Comparison Data =====
        Map<String, Double[]> yearlyMap = new LinkedHashMap<>();
        for (Income inc : incomes) {
            String year = inc.getDate().toString().substring(0, 4);
            if (!yearlyMap.containsKey(year)) {
                yearlyMap.put(year, new Double[]{0.0, 0.0});
            }
            yearlyMap.get(year)[0] += inc.getTotalIncome();
        }
        for (Expense exp : expenses) {
            String year = exp.getDate().toString().substring(0, 4);
            if (!yearlyMap.containsKey(year)) {
                yearlyMap.put(year, new Double[]{0.0, 0.0});
            }
            yearlyMap.get(year)[1] += exp.getAmount();
        }
        List<Map<String, Object>> yearlyData = new ArrayList<>();
        for (Map.Entry<String, Double[]> entry : yearlyMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("year", entry.getKey());
            data.put("income", entry.getValue()[0]);
            data.put("expense", entry.getValue()[1]);
            yearlyData.add(data);
        }

        // ===== 6. Income by Crop Pie Chart =====
        Map<String, Double> incomeCropMap = new HashMap<>();
        for (Income inc : incomes) {
            String crop = inc.getCropName();
            if (!incomeCropMap.containsKey(crop)) {
                incomeCropMap.put(crop, 0.0);
            }
            incomeCropMap.put(crop, incomeCropMap.get(crop) + inc.getTotalIncome());
        }
        List<Map<String, Object>> incomeCropData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : incomeCropMap.entrySet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", entry.getKey());
            data.put("value", entry.getValue());
            incomeCropData.add(data);
        }

        // ===== Totals =====
        double totalIncome = 0;
        double totalExpense = 0;
        for (Income inc : incomes) {
            totalIncome += inc.getTotalIncome();
        }
        for (Expense exp : expenses) {
            totalExpense += exp.getAmount();
        }
        double profit = totalIncome - totalExpense;

        // ============================================================
        // 🌾 CROP PROFIT COMPARISON
        // ============================================================
        Map<String, Double> cropIncomeMap = new HashMap<>();
        Map<String, Double> cropExpenseMap = new HashMap<>();
        Map<String, Double> cropProfitMap = new HashMap<>();

        for (Income inc : incomes) {
            String crop = inc.getCropName();
            cropIncomeMap.put(crop, cropIncomeMap.getOrDefault(crop, 0.0) + inc.getTotalIncome());
        }

        for (Expense exp : expenses) {
            String work = exp.getWorkDone().toLowerCase();
            for (String crop : cropIncomeMap.keySet()) {
                if (work.contains(crop.toLowerCase())) {
                    cropExpenseMap.put(crop, cropExpenseMap.getOrDefault(crop, 0.0) + exp.getAmount());
                }
            }
        }

        for (String crop : cropIncomeMap.keySet()) {
            double income = cropIncomeMap.getOrDefault(crop, 0.0);
            double expense = cropExpenseMap.getOrDefault(crop, 0.0);
            cropProfitMap.put(crop, income - expense);
        }

        List<Map.Entry<String, Double>> sortedCrops = new ArrayList<>(cropProfitMap.entrySet());
        sortedCrops.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Map<String, Object>> cropProfitData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedCrops) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", entry.getKey());
            data.put("profit", entry.getValue());
            data.put("income", cropIncomeMap.getOrDefault(entry.getKey(), 0.0));
            data.put("expense", cropExpenseMap.getOrDefault(entry.getKey(), 0.0));
            cropProfitData.add(data);
        }

        // ============================================================
        // 📊 EXPENSE VS INCOME RATIO
        // ============================================================
        double expenseRatio = 0;
        String ratioStatus = "";
        String ratioColor = "";

        if (totalIncome > 0) {
            expenseRatio = (totalExpense / totalIncome) * 100;
            expenseRatio = Math.round(expenseRatio * 10.0) / 10.0;

            if (expenseRatio < 30) {
                ratioStatus = "✅ Excellent! Very healthy!";
                ratioColor = "#2e7d32";
            } else if (expenseRatio < 50) {
                ratioStatus = "👍 Good! You're managing well!";
                ratioColor = "#4CAF50";
            } else if (expenseRatio < 70) {
                ratioStatus = "⚠️ Warning! Expenses are getting high!";
                ratioColor = "#ff9800";
            } else {
                ratioStatus = "🚨 Danger! Too much expense!";
                ratioColor = "#ff2e63";
            }
        } else {
            ratioStatus = "📊 Add some income records to see ratio!";
            ratioColor = "#888888";
        }

        // ============================================================
        // 🏆 TOP LABOUR ANALYSIS
        // ============================================================
        Map<String, Double> labourPaymentMap = new HashMap<>();
        Map<String, Integer> labourWorkCountMap = new HashMap<>();

        for (Expense exp : expenses) {
            String name = exp.getLabourName();
            labourPaymentMap.put(name, labourPaymentMap.getOrDefault(name, 0.0) + exp.getAmount());
            labourWorkCountMap.put(name, labourWorkCountMap.getOrDefault(name, 0) + 1);
        }

        List<Map.Entry<String, Double>> sortedLabour = new ArrayList<>(labourPaymentMap.entrySet());
        sortedLabour.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int maxLabours = Math.min(5, sortedLabour.size());
        List<Map<String, Object>> topLabourData = new ArrayList<>();

        for (int i = 0; i < maxLabours; i++) {
            Map.Entry<String, Double> entry = sortedLabour.get(i);
            String name = entry.getKey();
            double totalAmount = entry.getValue();
            int workCount = labourWorkCountMap.getOrDefault(name, 0);
            double average = workCount > 0 ? totalAmount / workCount : 0;

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("rank", i + 1);
            data.put("name", name);
            data.put("amount", totalAmount);
            data.put("workCount", workCount);
            data.put("average", Math.round(average * 100.0) / 100.0);

            String medal = "";
            if (i == 0) medal = "🥇";
            else if (i == 1) medal = "🥈";
            else if (i == 2) medal = "🥉";
            else medal = (i + 1) + "️⃣";
            data.put("medal", medal);

            topLabourData.add(data);
        }

        // ============================================================
        // 📅 YEARLY TRENDS
        // ============================================================
        int currentYear = LocalDate.now().getYear();
        int lastYear = currentYear - 1;

        double currentYearIncome = 0;
        double lastYearIncome = 0;
        double currentYearExpense = 0;
        double lastYearExpense = 0;

        for (Income inc : incomes) {
            int year = inc.getDate().getYear();
            if (year == currentYear) {
                currentYearIncome += inc.getTotalIncome();
            } else if (year == lastYear) {
                lastYearIncome += inc.getTotalIncome();
            }
        }

        for (Expense exp : expenses) {
            int year = exp.getDate().getYear();
            if (year == currentYear) {
                currentYearExpense += exp.getAmount();
            } else if (year == lastYear) {
                lastYearExpense += exp.getAmount();
            }
        }

        double currentYearProfit = currentYearIncome - currentYearExpense;
        double lastYearProfit = lastYearIncome - lastYearExpense;

        double incomeGrowth = 0;
        double expenseGrowth = 0;
        double profitGrowth = 0;

        if (lastYearIncome > 0) {
            incomeGrowth = ((currentYearIncome - lastYearIncome) / lastYearIncome) * 100;
        }
        if (lastYearExpense > 0) {
            expenseGrowth = ((currentYearExpense - lastYearExpense) / lastYearExpense) * 100;
        }
        if (lastYearProfit > 0) {
            profitGrowth = ((currentYearProfit - lastYearProfit) / lastYearProfit) * 100;
        }

        incomeGrowth = Math.round(incomeGrowth * 10.0) / 10.0;
        expenseGrowth = Math.round(expenseGrowth * 10.0) / 10.0;
        profitGrowth = Math.round(profitGrowth * 10.0) / 10.0;

        String growthStatus = "";
        String growthColor = "";
        String growthEmoji = "";

        if (incomeGrowth > 10) {
            growthStatus = "Excellent! Farm is GROWING! 🎉";
            growthColor = "#2e7d32";
            growthEmoji = "📈";
        } else if (incomeGrowth >= 0) {
            growthStatus = "Good! Stable growth! ✅";
            growthColor = "#4CAF50";
            growthEmoji = "📊";
        } else if (incomeGrowth > -10) {
            growthStatus = "⚠️ Slight decline! Needs attention!";
            growthColor = "#ff9800";
            growthEmoji = "📉";
        } else {
            growthStatus = "🚨 Significant decline! Check your farm!";
            growthColor = "#ff2e63";
            growthEmoji = "📉";
        }

        // ============================================================
        // 💰 MONTHLY BUDGET CHECK
        // ============================================================
        double monthlyBudget = 10000;
        double monthlyExpense = 0;
        int currentMonth = LocalDate.now().getMonthValue();

        for (Expense exp : expenses) {
            if (exp.getDate().getMonthValue() == currentMonth &&
                    exp.getDate().getYear() == currentYear) {
                monthlyExpense += exp.getAmount();
            }
        }

        boolean budgetExceeded = monthlyExpense > monthlyBudget;
        double exceededAmount = budgetExceeded ? (monthlyExpense - monthlyBudget) : 0;

        if (budgetExceeded) {
            notificationService.sendMonthlyBudgetAlert(
                    user.getEmail(),
                    monthlyExpense,
                    monthlyBudget,
                    currentMonth,
                    currentYear
            );
            System.out.println("✅ Budget alert sent to: " + user.getEmail());
            // Send WhatsApp (if user has phone number)
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                whatsAppService.sendBudgetAlertWhatsApp(user.getPhoneNumber(), monthlyExpense, monthlyBudget);
            }
        }

        // ===== WEATHER =====
        String weatherInfo = weatherService.getWeather();
        model.addAttribute("weatherInfo", weatherInfo);

        // ============================================================
        // ===== Add to Model =====
        // ============================================================
        model.addAttribute("incomes", incomes);
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("profit", profit);
        model.addAttribute("user", user);

        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("expenseBreakdown", expenseBreakdown);
        model.addAttribute("profitTrend", profitTrend);
        model.addAttribute("cropData", cropData);
        model.addAttribute("yearlyData", yearlyData);
        model.addAttribute("incomeCropData", incomeCropData);

        model.addAttribute("cropProfitData", cropProfitData);

        model.addAttribute("expenseRatio", expenseRatio);
        model.addAttribute("ratioStatus", ratioStatus);
        model.addAttribute("ratioColor", ratioColor);

        model.addAttribute("topLabourData", topLabourData);

        model.addAttribute("currentYear", currentYear);
        model.addAttribute("lastYear", lastYear);
        model.addAttribute("currentYearIncome", currentYearIncome);
        model.addAttribute("lastYearIncome", lastYearIncome);
        model.addAttribute("currentYearExpense", currentYearExpense);
        model.addAttribute("lastYearExpense", lastYearExpense);
        model.addAttribute("currentYearProfit", currentYearProfit);
        model.addAttribute("lastYearProfit", lastYearProfit);
        model.addAttribute("incomeGrowth", incomeGrowth);
        model.addAttribute("expenseGrowth", expenseGrowth);
        model.addAttribute("profitGrowth", profitGrowth);
        model.addAttribute("growthStatus", growthStatus);
        model.addAttribute("growthColor", growthColor);
        model.addAttribute("growthEmoji", growthEmoji);

        model.addAttribute("monthlyBudget", monthlyBudget);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("budgetExceeded", budgetExceeded);
        model.addAttribute("exceededAmount", exceededAmount);

        return "dashboard";
    }
}