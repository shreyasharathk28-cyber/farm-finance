package com.farm.finance.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="income")


public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crop_name" , nullable = false)
    private String cropName;

    private Double quantity;

    @Column(name = "price_per_kg")
    private Double pricePerKg;

    @Column(name = "total_income")
    private Double totalIncome;

    @Column(name = "income_date")
    private LocalDate date;

    public Income() {

    }

    public Income(String cropName, double quantity, double pricePerKg, Double totalIncome, LocalDate date) {
        this.cropName = cropName;
        this.quantity = quantity;
        this.pricePerKg = pricePerKg;
        this.totalIncome = totalIncome;
        this.date = date;
    }

    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public double getPricePerKg() {
        return pricePerKg;

    }

    public void setPricePerKg(Double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public Double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
