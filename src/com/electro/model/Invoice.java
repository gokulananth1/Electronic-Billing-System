package com.electro.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Completed invoice / sales transaction.
 */
public class Invoice {
    private String invoiceId;
    private String dateTime;
    private Customer customer;
    private List<CartItem> items;
    private double subtotal;
    private double totalDiscount;
    private double taxableAmount;
    private double cgstAmount;
    private double sgstAmount;
    private double grandTotal;
    private String paymentMethod; // Cash, Card, UPI, EMI
    private String paymentReference;
    private String notes;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Invoice() {
        this.items = new ArrayList<>();
        this.dateTime = LocalDateTime.now().format(FORMATTER);
    }

    public Invoice(String invoiceId, Customer customer, List<CartItem> items,
                   double subtotal, double totalDiscount, double taxableAmount,
                   double cgstAmount, double sgstAmount, double grandTotal,
                   String paymentMethod, String paymentReference, String notes) {
        this.invoiceId = invoiceId;
        this.dateTime = LocalDateTime.now().format(FORMATTER);
        this.customer = customer;
        this.items = items != null ? items : new ArrayList<>();
        this.subtotal = subtotal;
        this.totalDiscount = totalDiscount;
        this.taxableAmount = taxableAmount;
        this.cgstAmount = cgstAmount;
        this.sgstAmount = sgstAmount;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.notes = notes;
    }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(double totalDiscount) { this.totalDiscount = totalDiscount; }

    public double getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(double taxableAmount) { this.taxableAmount = taxableAmount; }

    public double getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(double cgstAmount) { this.cgstAmount = cgstAmount; }

    public double getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(double sgstAmount) { this.sgstAmount = sgstAmount; }

    public double getTotalTax() { return cgstAmount + sgstAmount; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTotalUnits() {
        int count = 0;
        if (items != null) {
            for (CartItem item : items) {
                count += item.getQuantity();
            }
        }
        return count;
    }
}
