package com.electro.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Line item in a shopping cart / bill.
 */
public class CartItem {
    private Product product;
    private int quantity;
    private double unitPrice;
    private double discountPercent; // e.g. 5.0 for 5%
    private List<String> serialNumbers; // Each unit of serial-tracked item has a unique serial/IMEI

    public CartItem() {
        this.serialNumbers = new ArrayList<>();
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getSellingPrice();
        this.discountPercent = 0.0;
        this.serialNumbers = new ArrayList<>();
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public List<String> getSerialNumbers() { return serialNumbers; }
    public void setSerialNumbers(List<String> serialNumbers) { this.serialNumbers = serialNumbers; }

    public void addSerialNumber(String serial) {
        if (serial != null && !serial.trim().isEmpty()) {
            this.serialNumbers.add(serial.trim());
        }
    }

    public double getRawSubtotal() {
        return unitPrice * quantity;
    }

    public double getDiscountAmount() {
        return (getRawSubtotal() * discountPercent) / 100.0;
    }

    public double getTaxableAmount() {
        return getRawSubtotal() - getDiscountAmount();
    }

    public double getTaxRate() {
        return product != null ? product.getTaxRate() : 0.0;
    }

    public double getTaxAmount() {
        return (getTaxableAmount() * getTaxRate()) / 100.0;
    }

    public double getLineTotal() {
        return getTaxableAmount() + getTaxAmount();
    }

    public boolean hasRequiredSerials() {
        if (product == null || !product.isRequiresSerial()) {
            return true;
        }
        return serialNumbers != null && serialNumbers.size() == quantity;
    }
}
