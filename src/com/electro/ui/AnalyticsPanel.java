package com.electro.ui;

import com.electro.model.CartItem;
import com.electro.model.Invoice;
import com.electro.model.Product;
import com.electro.service.DataStore;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Analytics and KPI dashboard for store managers.
 */
public class AnalyticsPanel extends JPanel {
    private final DataStore dataStore;

    private JLabel lblTotalRevenue;
    private JLabel lblTotalInvoices;
    private JLabel lblTotalUnitsSold;
    private JLabel lblLowStockCount;

    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    private JTable topProductsTable;
    private DefaultTableModel topProductsTableModel;

    public AnalyticsPanel() {
        this.dataStore = DataStore.getInstance();

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UITheme.COLOR_BG);
        topContainer.add(createTopBar(), BorderLayout.NORTH);
        topContainer.add(createKpiHeader(), BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);
        centerPanel.add(createCategoryBreakdownPanel());
        centerPanel.add(createTopSellingProductsPanel());

        add(centerPanel, BorderLayout.CENTER);
        refreshAnalytics();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(UITheme.COLOR_PANEL_BG);
        bar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("Sales Performance & Business Analytics");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.COLOR_PRIMARY_DARK);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setBackground(UITheme.COLOR_PANEL_BG);

        JButton btnResetSales = UITheme.createButton("Reset Sales Data", UITheme.COLOR_DANGER, Color.WHITE);
        btnResetSales.setToolTipText("Clear all sales history, revenue records, and invoices");
        btnResetSales.addActionListener(e -> handleResetSalesData());

        controls.add(btnResetSales);

        bar.add(title, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    private void handleResetSalesData() {
        com.electro.model.User user = com.electro.service.AuthService.getInstance().getCurrentUser();
        if (user != null && !user.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access Denied: Only Administrators can reset sales data.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently clear all sales data and invoice history?\n"
                + "This will delete all past transactions and reset sales revenue to zero.\n\n"
                + "Do you wish to proceed?",
                "Confirm Reset Sales Data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dataStore.resetSalesData();
            refreshAnalytics();
            JOptionPane.showMessageDialog(this, "All sales data and transaction history have been reset!", "Sales Data Reset", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createKpiHeader() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 12));
        panel.setBackground(UITheme.COLOR_BG);

        lblTotalRevenue = new JLabel("0.00");
        lblTotalInvoices = new JLabel("0");
        lblTotalUnitsSold = new JLabel("0");
        lblLowStockCount = new JLabel("0");

        panel.add(createKpiCard("TOTAL REVENUE", lblTotalRevenue, UITheme.COLOR_PRIMARY, "All-time completed sales"));
        panel.add(createKpiCard("INVOICES BILLED", lblTotalInvoices, new Color(13, 148, 136), "Total customer bills issued"));
        panel.add(createKpiCard("UNITS SOLD", lblTotalUnitsSold, new Color(147, 51, 234), "Individual electronics units"));
        panel.add(createKpiCard("LOW STOCK ITEMS", lblLowStockCount, UITheme.COLOR_DANGER, "Products with <= 3 in stock"));

        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor, String hint) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(UITheme.COLOR_PANEL_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new CompoundBorder(
                        new LineBorder(UITheme.COLOR_BORDER, 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_BIG_NUMBER);
        valueLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(UITheme.FONT_SMALL);
        hintLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(hintLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCategoryBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(UITheme.COLOR_PANEL_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Sales by Category");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.COLOR_PRIMARY_DARK);

        String[] cols = {"Category", "Units Sold", "Total Revenue"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        categoryTable = new JTable(categoryTableModel);
        UITheme.styleTable(categoryTable);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopSellingProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(UITheme.COLOR_PANEL_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Top Selling Electronics");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.COLOR_PRIMARY_DARK);

        String[] cols = {"Product Name", "Brand", "Units Sold", "Total Sales"};
        topProductsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        topProductsTable = new JTable(topProductsTableModel);
        UITheme.styleTable(topProductsTable);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(topProductsTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshAnalytics() {
        List<Invoice> invoices = dataStore.getAllInvoices();
        List<Product> products = dataStore.getAllProducts();
        String sym = dataStore.getSettings().getCurrencySymbol();

        double totalRevenue = 0;
        int totalUnits = 0;
        Map<String, Integer> catUnits = new HashMap<>();
        Map<String, Double> catRevenue = new HashMap<>();

        Map<String, Integer> prodUnits = new HashMap<>();
        Map<String, Double> prodRevenue = new HashMap<>();
        Map<String, String> prodBrand = new HashMap<>();

        for (Invoice inv : invoices) {
            totalRevenue += inv.getGrandTotal();
            for (CartItem item : inv.getItems()) {
                int q = item.getQuantity();
                double line = item.getLineTotal();
                totalUnits += q;

                String cat = item.getProduct().getCategory();
                catUnits.put(cat, catUnits.getOrDefault(cat, 0) + q);
                catRevenue.put(cat, catRevenue.getOrDefault(cat, 0.0) + line);

                String pName = item.getProduct().getName();
                prodUnits.put(pName, prodUnits.getOrDefault(pName, 0) + q);
                prodRevenue.put(pName, prodRevenue.getOrDefault(pName, 0.0) + line);
                prodBrand.put(pName, item.getProduct().getBrand());
            }
        }

        long lowStock = products.stream().filter(Product::isLowStock).count();

        lblTotalRevenue.setText(UITheme.formatCurrency(totalRevenue, sym));
        lblTotalInvoices.setText(String.valueOf(invoices.size()));
        lblTotalUnitsSold.setText(String.valueOf(totalUnits));
        lblLowStockCount.setText(String.valueOf(lowStock));

        // Category Table
        categoryTableModel.setRowCount(0);
        for (String cat : catUnits.keySet()) {
            categoryTableModel.addRow(new Object[]{
                    cat,
                    catUnits.get(cat),
                    UITheme.formatCurrency(catRevenue.get(cat), sym)
            });
        }

        // Top Products Table
        topProductsTableModel.setRowCount(0);
        List<Map.Entry<String, Integer>> sortedProds = new ArrayList<>(prodUnits.entrySet());
        sortedProds.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedProds) {
            String pName = entry.getKey();
            topProductsTableModel.addRow(new Object[]{
                    pName,
                    prodBrand.getOrDefault(pName, ""),
                    entry.getValue(),
                    UITheme.formatCurrency(prodRevenue.getOrDefault(pName, 0.0), sym)
            });
        }
    }
}
