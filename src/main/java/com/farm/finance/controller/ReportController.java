package com.farm.finance.controller;

import com.farm.finance.model.Income;
import com.farm.finance.model.Expense;
import com.farm.finance.repository.IncomeRepository;
import com.farm.finance.repository.ExpenseRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.awt.Color;

@Controller
public class ReportController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/download-report")
    public ResponseEntity<byte[]> downloadReport(HttpSession session) throws Exception {
        // Check if user is logged in
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(403).build();
        }

        List<Income> incomes = incomeRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        // Calculate totals
        double totalIncome = 0;
        double totalExpense = 0;
        for (Income inc : incomes) { totalIncome += inc.getTotalIncome(); }
        for (Expense exp : expenses) { totalExpense += exp.getAmount(); }
        double profit = totalIncome - totalExpense;

        // Create PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
        Paragraph title = new Paragraph("🌾 Farm Finance Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        Font normalFont = new Font(Font.HELVETICA, 12);
        document.add(new Paragraph("Generated: " + LocalDate.now(), normalFont));
        document.add(new Paragraph(" "));

        // Summary
        Font headerFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        document.add(new Paragraph("📊 Summary Report", headerFont));
        document.add(new Paragraph("Total Income: ₹" + totalIncome, normalFont));
        document.add(new Paragraph("Total Expense: ₹" + totalExpense, normalFont));
        document.add(new Paragraph("Profit/Loss: ₹" + profit, normalFont));
        document.add(new Paragraph(" "));

        // Income Table
        document.add(new Paragraph("💰 Income Records", headerFont));

        PdfPTable incomeTable = new PdfPTable(6);
        incomeTable.addCell(createHeaderCell("ID"));
        incomeTable.addCell(createHeaderCell("Crop"));
        incomeTable.addCell(createHeaderCell("Quantity"));
        incomeTable.addCell(createHeaderCell("Price/Kg"));
        incomeTable.addCell(createHeaderCell("Total"));
        incomeTable.addCell(createHeaderCell("Date"));

        for (Income inc : incomes) {
            incomeTable.addCell(createCell(String.valueOf(inc.getId())));
            incomeTable.addCell(createCell(inc.getCropName()));
            incomeTable.addCell(createCell(String.valueOf(inc.getQuantity())));
            incomeTable.addCell(createCell(String.valueOf(inc.getPricePerKg())));
            incomeTable.addCell(createCell(String.valueOf(inc.getTotalIncome())));
            incomeTable.addCell(createCell(inc.getDate().toString()));
        }
        document.add(incomeTable);
        document.add(new Paragraph(" "));

        // Expense Table
        document.add(new Paragraph("💸 Expense Records", headerFont));

        PdfPTable expenseTable = new PdfPTable(5);
        expenseTable.addCell(createHeaderCell("ID"));
        expenseTable.addCell(createHeaderCell("Labour"));
        expenseTable.addCell(createHeaderCell("Work"));
        expenseTable.addCell(createHeaderCell("Amount"));
        expenseTable.addCell(createHeaderCell("Date"));

        for (Expense exp : expenses) {
            expenseTable.addCell(createCell(String.valueOf(exp.getId())));
            expenseTable.addCell(createCell(exp.getLabourName()));
            expenseTable.addCell(createCell(exp.getWorkDone()));
            expenseTable.addCell(createCell(String.valueOf(exp.getAmount())));
            expenseTable.addCell(createCell(exp.getDate().toString()));
        }
        document.add(expenseTable);

        document.close();

        // Return PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "farm-finance-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
    }

    private PdfPCell createCell(String text) {
        return new PdfPCell(new Phrase(text));
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        // Simple gray background - no BaseColor needed!
        cell.setBackgroundColor(new Color(200, 200, 200));
        return cell;
    }
}