package com.farm.finance.controller;

import com.farm.finance.model.Reminder;
import com.farm.finance.model.User;
import com.farm.finance.repository.ReminderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class ReminderController {

    @Autowired
    private ReminderRepository reminderRepository;

    // ===== SHOW REMINDERS PAGE =====
    @GetMapping("/reminders")
    public String showReminders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("reminders", reminderRepository.findByUserAndCompletedFalse(user));
        model.addAttribute("user", user);
        return "reminders";
    }

    // ===== ADD REMINDER =====
    @PostMapping("/add-reminder")
    public String addReminder(@RequestParam String title,
                              @RequestParam String description,
                              @RequestParam String date,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Reminder reminder = new Reminder(title, description, LocalDate.parse(date), user);
        reminderRepository.save(reminder);

        return "redirect:/reminders";
    }

    // ===== MARK AS COMPLETE =====
    @GetMapping("/complete-reminder/{id}")
    public String completeReminder(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Reminder reminder = reminderRepository.findById(id).orElse(null);
        if (reminder != null && reminder.getUser().getId().equals(user.getId())) {
            reminder.setCompleted(true);
            reminderRepository.save(reminder);
        }
        return "redirect:/reminders";
    }

    // ===== DELETE REMINDER =====
    @GetMapping("/delete-reminder/{id}")
    public String deleteReminder(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Reminder reminder = reminderRepository.findById(id).orElse(null);
        if (reminder != null && reminder.getUser().getId().equals(user.getId())) {
            reminderRepository.delete(reminder);
        }
        return "redirect:/reminders";
    }
}