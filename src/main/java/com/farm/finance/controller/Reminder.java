package com.farm.finance.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Reminder() {}

    public Reminder(String title, String description, LocalDate reminderDate, User user) {
        this.title = title;
        this.description = description;
        this.reminderDate = reminderDate;
        this.completed = false;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDate reminderDate) { this.reminderDate = reminderDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}