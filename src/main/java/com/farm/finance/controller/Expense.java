package com.farm.finance.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "labour_name")
    private String labourName;

    @Column(name = "work_done")
    private String workDone;

    private Double amount;

    @Column(name = "expense_date")
    private LocalDate date;

    @Column(name="photo_url")
    private String photoUrl;

    public Expense() {}

    public Expense(String labourName, String workDone, Double amount, LocalDate date) {
        this.labourName = labourName;
        this.workDone = workDone;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabourName() { return labourName; }
    public void setLabourName(String labourName) { this.labourName = labourName; }

    public String getWorkDone() { return workDone; }
    public void setWorkDone(String workDone) { this.workDone = workDone; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }


    // Getter
    public String getPhotoUrl() { return photoUrl; }

    // Setter
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}