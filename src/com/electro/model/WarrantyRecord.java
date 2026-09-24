package com.electro.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Tracks warranty information for unique serial/IMEI electronics items.
 */
public class WarrantyRecord {
    private String serialNumber;
    private String invoiceId;
    private String productId;
    private String productName;
    private String brand;
    private String customerName;
    private String customerPhone;
    private String purchaseDate; // YYYY-MM-DD
    private int warrantyMonths;
    private String expiryDate;   // YYYY-MM-DD

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WarrantyRecord() {}

    public WarrantyRecord(String serialNumber, String invoiceId, String productId,
                          String productName, String brand, String customerName,
                          String customerPhone, LocalDate purchaseLocalDate, int warrantyMonths) {
        this.serialNumber = serialNumber;
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.purchaseDate = purchaseLocalDate.format(DATE_FORMAT);
        this.warrantyMonths = warrantyMonths;
        this.expiryDate = purchaseLocalDate.plusMonths(warrantyMonths).format(DATE_FORMAT);
    }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isExpired() {
        try {
            LocalDate exp = LocalDate.parse(expiryDate, DATE_FORMAT);
            return LocalDate.now().isAfter(exp);
        } catch (Exception e) {
            return false;
        }
    }

    public long getRemainingDays() {
        try {
            LocalDate exp = LocalDate.parse(expiryDate, DATE_FORMAT);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), exp);
            return Math.max(0, days);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getStatusDescription() {
        if (isExpired()) {
            return "EXPIRED";
        } else {
            return "ACTIVE (" + getRemainingDays() + " days remaining)";
        }
    }
}
