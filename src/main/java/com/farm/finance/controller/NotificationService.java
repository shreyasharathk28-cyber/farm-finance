package com.farm.finance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(message);
            mailSender.send(msg);
            System.out.println("✅ Email sent to: " + to);
        } catch (Exception e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    // ===== BUDGET ALERT =====
    public void sendBudgetAlert(String to, String labourName, double amount) {
        String subject = "🚨 Budget Alert - Farm Finance";
        String message = "⚠️ You have paid " + labourName + " ₹" + amount + " this month!\n" +
                "Please check your expenses.";
        sendEmail(to, subject, message);
    }

    // ===== PROFIT MILESTONE =====
    public void sendProfitMilestone(String to, double profit) {
        String subject = "🎉 Profit Milestone Reached!";
        String message = "Congratulations! Your farm has made ₹" + profit + " profit!\n" +
                "Keep up the great work! 🌾";
        sendEmail(to, subject, message);
    }

    // ===== WEEKLY SUMMARY =====
    public void sendWeeklySummary(String to, double income, double expense, double profit) {
        String subject = "📊 Weekly Farm Summary";
        String message = "📈 This Week's Summary:\n" +
                "💰 Income: ₹" + income + "\n" +
                "💸 Expense: ₹" + expense + "\n" +
                "📈 Profit: ₹" + profit + "\n\n" +
                "🌾 Keep tracking your farm!";
        sendEmail(to, subject, message);
    }

    // ============================================================
    // 💰 MONTHLY BUDGET ALERT
    // ============================================================
    public void sendMonthlyBudgetAlert(String to, double monthlyExpense, double budget, int month, int year) {
        String subject = "🚨 Monthly Budget Alert - Farm Finance";

        String monthName = java.time.Month.of(month).name().charAt(0) +
                java.time.Month.of(month).name().substring(1).toLowerCase();

        String message = "⚠️ MONTHLY BUDGET ALERT ⚠️\n\n" +
                "Month: " + monthName + " " + year + "\n" +
                "Budget: ₹" + budget + "\n" +
                "Actual Expense: ₹" + monthlyExpense + "\n" +
                "Exceeded by: ₹" + (monthlyExpense - budget) + "\n\n" +
                "💡 Tip: Review your expenses to stay within budget!\n\n" +
                "🌾 Farm Finance Management System";

        sendEmail(to, subject, message);
    }
    }





