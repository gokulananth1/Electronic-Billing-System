package com.electro.test;

import com.electro.model.*;
import com.electro.service.*;

import java.io.File;
import java.util.List;

/**
 * Automated headless test suite verifying all core billing, inventory, warranty, and invoice features.
 */
public class SystemTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" Starting Automated System Tests for Billing App ");
        System.out.println("==================================================");

        try {
            // 1. Test DataStore & Sample Catalog Seeding
            DataStore store = DataStore.getInstance();
            List<Product> initialProducts = store.getAllProducts();
            assert initialProducts.size() >= 10 : "Catalog should be seeded with at least 10 products";
            System.out.println("\u2705 [PASS] DataStore initialized with " + initialProducts.size() + " electronic products.");

            // 2. Test Inventory Service
            InventoryService invService = new InventoryService();
            List<Product> phones = invService.searchProducts("iPhone", "Smartphones");
            assert !phones.isEmpty() : "Should find iPhone under Smartphones";
            Product iphone = phones.get(0);
            int initialStock = iphone.getStockQuantity();
            System.out.println("\u2705 [PASS] Product found: " + iphone.getName() + " (Initial stock: " + initialStock + ")");

            // 3. Test Billing POS Cart Lifecycle
            BillingService billingService = new BillingService();
            billingService.clearCart();
            billingService.addToCart(iphone, 1);

            // iPhone requires serial
            String errBeforeSerial = billingService.validateCartForCheckout();
            assert errBeforeSerial != null && errBeforeSerial.contains("Serial/IMEI number missing") : "Validation must reject checkout without serial";
            System.out.println("\u2705 [PASS] Serial validation correctly caught missing serial: " + errBeforeSerial);

            // Assign serial number
            String testSerial = "SN-TEST-IPHONE-001";
            billingService.setItemSerials(iphone.getId(), List.of(testSerial));
            String errAfterSerial = billingService.validateCartForCheckout();
            assert errAfterSerial == null : "Validation should pass once serial is provided";
            System.out.println("\u2705 [PASS] Serial validation passed after serial assignment.");

            // 4. Test Financial Calculations
            double subtotal = billingService.calculateSubtotal();
            double tax = billingService.calculateTotalTax();
            double grandTotal = billingService.calculateGrandTotal();
            assert Math.abs((subtotal + tax) - grandTotal) < 0.05 : "Grand total must equal subtotal + tax";
            System.out.println("\u2705 [PASS] Calculations verified: Subtotal=" + subtotal + ", Tax=" + tax + ", Grand Total=" + grandTotal);

            // 5. Test Checkout Execution
            Customer customer = new Customer("C-101", "Alex Turing", "9876543210", "alex@example.com", "Bangalore, India", "");
            Invoice invoice = billingService.checkout(customer, "UPI", "UPI-REF-998877", "Test sale");
            assert invoice != null : "Invoice must be generated";
            assert invoice.getInvoiceId().startsWith("INV-") : "Invoice ID format check";
            System.out.println("\u2705 [PASS] Checkout successful. Generated Invoice: " + invoice.getInvoiceId());

            // 6. Verify Stock Decrement
            Product updatedIphone = store.getProductById(iphone.getId());
            assert updatedIphone.getStockQuantity() == initialStock - 1 : "Stock must decrement by 1";
            System.out.println("\u2705 [PASS] Stock decremented properly. New Stock: " + updatedIphone.getStockQuantity());

            // 7. Verify Warranty Registration
            WarrantyService warrantyService = new WarrantyService();
            WarrantyRecord wr = warrantyService.findBySerial(testSerial);
            assert wr != null : "Warranty record must exist for serial";
            assert !wr.isExpired() : "New warranty must be active";
            assert wr.getCustomerName().equals("Alex Turing") : "Customer name must match";
            System.out.println("\u2705 [PASS] Warranty record verified: " + wr.getStatusDescription());

            // 8. Test Invoice HTML & Thermal Receipt Generation
            ShopSettings settings = store.getSettings();
            String html = InvoiceGenerator.generateHtmlInvoice(invoice, settings);
            assert html.contains(invoice.getInvoiceId()) : "HTML invoice must contain invoice ID";
            assert html.contains(testSerial) : "HTML invoice must contain serial number";

            String thermal = InvoiceGenerator.generateThermalReceipt(invoice, settings);
            assert thermal.contains("Alex Turing") : "Thermal receipt must contain customer";

            File htmlFile = InvoiceGenerator.saveHtmlInvoiceToFile(invoice, settings);
            assert htmlFile != null && htmlFile.exists() : "HTML file must be saved on disk";
            System.out.println("\u2705 [PASS] HTML Invoice generated and verified: " + htmlFile.getAbsolutePath());

            // 9. Test Reset Sales Data
            store.resetSalesData();
            assert store.getAllInvoices().isEmpty() : "Invoices must be empty after reset";
            assert store.getAllWarranties().isEmpty() : "Warranties must be empty after reset";
            System.out.println("\u2705 [PASS] Reset Sales Data successfully wiped all invoices and warranty records.");

            // 10. Test Authentication & Role Management
            AuthService auth = AuthService.getInstance();
            boolean adminLogin = auth.login("admin", "admin123");
            assert adminLogin : "Admin login with correct password must succeed";
            assert auth.getCurrentUser().isAdmin() : "Admin role check must return true";
            assert !auth.getCurrentUser().isCashier() : "Admin is not cashier";
            System.out.println("\u2705 [PASS] Admin authentication and role verification successful.");

            boolean cashierLogin = auth.login("cashier", "cashier123");
            assert cashierLogin : "Cashier login with correct password must succeed";
            assert auth.getCurrentUser().isCashier() : "Cashier role check must return true";
            assert !auth.getCurrentUser().isAdmin() : "Cashier is not admin";
            System.out.println("\u2705 [PASS] Cashier authentication and permission boundaries verified.");

            // 11. Test Security: Reject Invalid Credentials & User Management
            boolean badPass = auth.login("admin", "wrongpass");
            assert !badPass : "Invalid password must be rejected";

            boolean badUser = auth.login("unknownuser", "password");
            assert !badUser : "Unknown user must be rejected";

            if (auth.getUserByUsername("tester_cashier") != null) {
                auth.deleteUser("tester_cashier");
            }
            auth.createUser("tester_cashier", "securepass", "Tester Cashier", User.Role.CASHIER);
            assert auth.login("tester_cashier", "securepass") : "Newly created user must be able to log in";
            auth.login("admin", "admin123");
            assert auth.deleteUser("tester_cashier") : "Admin must be able to delete staff user";
            System.out.println("\u2705 [PASS] Security verification: Invalid logins blocked & User CRUD tested.");

            // 12. Test Retail vs Wholesale Pricing Classification
            BillingService pricingTest = new BillingService();
            pricingTest.clearCart();
            pricingTest.setCustomerClassification(BillingService.CustomerClassification.RETAIL);
            pricingTest.addToCart(iphone, 1);
            double retailPrice = pricingTest.getCart().get(0).getUnitPrice();
            assert Math.abs(retailPrice - iphone.getSellingPrice()) < 0.01 : "Retail unit price must match selling price";

            // Switch to Wholesale mode
            pricingTest.setCustomerClassification(BillingService.CustomerClassification.WHOLESALE);
            double wholesalePrice = pricingTest.getCart().get(0).getUnitPrice();
            assert Math.abs(wholesalePrice - iphone.getWholesalePrice()) < 0.01 : "Wholesale unit price must match wholesale price";
            assert wholesalePrice < retailPrice : "Wholesale price must be lower than retail price";
            System.out.println("\u2705 [PASS] Classification Pricing verified: Retail=\u20B9" + retailPrice + " -> Wholesale=\u20B9" + wholesalePrice);

            System.out.println("==================================================");
            System.out.println(" ALL 12 SYSTEM TESTS PASSED SUCCESSFULLY! \u2705\u2705\u2705");
            System.out.println("==================================================");

        } catch (Throwable t) {
            System.err.println("\u274C Test failed with error: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
