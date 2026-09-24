package com.electro.ui;

import com.electro.model.CartItem;
import com.electro.model.Invoice;
import com.electro.service.DataStore;
import com.electro.service.InvoiceGenerator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to browse past invoices, reprint tax receipts, and view sale details.
 */
public class InvoiceHistoryPanel extends JPanel {
    private final Window parentWindow;
    private final DataStore dataStore;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;
    private JTable detailsTable;
    private DefaultTableModel detailsTableModel;

    private JTextField searchField;
    private List<Invoice> currentInvoices = new ArrayList<>();

    public InvoiceHistoryPanel(Window parentWindow) {
        this.parentWindow = parentWindow;
        this.dataStore = DataStore.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(createTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createInvoicesListPanel(), createDetailsPanel());
        split.setResizeWeight(0.65);
        split.setDividerSize(6);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        refreshTable();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(UITheme.COLOR_PANEL_BG);
        bar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("Sales Invoices & Transaction History");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.COLOR_PRIMARY_DARK);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setBackground(UITheme.COLOR_PANEL_BG);

        searchField = UITheme.createTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search invoice #, customer phone/name...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshTable();
            }
        });

        JButton btnReprint = UITheme.createButton("View / Reprint Bill", UITheme.COLOR_PRIMARY, Color.WHITE);
        btnReprint.addActionListener(e -> viewSelectedInvoice());

        JButton btnOpen = UITheme.createButton("Open PDF/HTML", new Color(13, 148, 136), Color.WHITE);
        btnOpen.addActionListener(e -> openSelectedInBrowser());

        JButton btnReset = UITheme.createButton("Reset Sales Data", UITheme.COLOR_DANGER, Color.WHITE);
        btnReset.setToolTipText("Permanently clear all sales transactions and invoice history");
        btnReset.addActionListener(e -> handleResetSalesData());

        controls.add(new JLabel("Search:"));
        controls.add(searchField);
        controls.add(btnReprint);
        controls.add(btnOpen);
        controls.add(btnReset);

        bar.add(title, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    private JPanel createInvoicesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.COLOR_PANEL_BG);
        panel.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        String[] cols = {"Invoice #", "Date & Time", "Customer Name", "Phone", "Units", "Payment Mode", "Tax Total", "Grand Total"};
        invoiceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        invoiceTable = new JTable(invoiceTableModel);
        UITheme.styleTable(invoiceTable);

        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedInvoiceDetails();
            }
        });

        invoiceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedInvoice();
                }
            }
        });

        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(UITheme.COLOR_PANEL_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel("Invoice Line Items & Registered Serial/IMEI Numbers");
        title.setFont(UITheme.FONT_REGULAR_BOLD);
        title.setForeground(UITheme.COLOR_SECONDARY);

        String[] cols = {"Item Name", "Brand & Model", "Registered Serial / IMEI Numbers", "Qty", "Rate", "Total"};
        detailsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        detailsTable = new JTable(detailsTableModel);
        UITheme.styleTable(detailsTable);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshTable() {
        String q = searchField.getText().trim().toLowerCase();
        List<Invoice> all = dataStore.getAllInvoices();
        currentInvoices = new ArrayList<>();

        for (Invoice inv : all) {
            if (q.isEmpty() ||
                inv.getInvoiceId().toLowerCase().contains(q) ||
                (inv.getCustomer() != null && inv.getCustomer().getName().toLowerCase().contains(q)) ||
                (inv.getCustomer() != null && inv.getCustomer().getPhone().toLowerCase().contains(q)) ||
                inv.getDateTime().toLowerCase().contains(q)) {
                currentInvoices.add(inv);
            }
        }

        invoiceTableModel.setRowCount(0);
        String sym = dataStore.getSettings().getCurrencySymbol();

        for (Invoice inv : currentInvoices) {
            String custName = inv.getCustomer() != null ? inv.getCustomer().getName() : "Walk-in";
            String custPhone = inv.getCustomer() != null ? inv.getCustomer().getPhone() : "-";
            invoiceTableModel.addRow(new Object[]{
                    inv.getInvoiceId(),
                    inv.getDateTime(),
                    custName,
                    custPhone,
                    inv.getTotalUnits(),
                    inv.getPaymentMethod(),
                    UITheme.formatCurrency(inv.getTotalTax(), sym),
                    UITheme.formatCurrency(inv.getGrandTotal(), sym)
            });
        }

        if (!currentInvoices.isEmpty()) {
            invoiceTable.setRowSelectionInterval(0, 0);
        } else {
            detailsTableModel.setRowCount(0);
        }
    }

    private void showSelectedInvoiceDetails() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0 || row >= currentInvoices.size()) {
            detailsTableModel.setRowCount(0);
            return;
        }

        Invoice inv = currentInvoices.get(row);
        detailsTableModel.setRowCount(0);
        String sym = dataStore.getSettings().getCurrencySymbol();

        for (CartItem ci : inv.getItems()) {
            String serials = ci.getSerialNumbers().isEmpty() ? "-" : String.join(", ", ci.getSerialNumbers());
            detailsTableModel.addRow(new Object[]{
                    ci.getProduct().getName(),
                    ci.getProduct().getBrand() + " (" + ci.getProduct().getModelNumber() + ")",
                    serials,
                    ci.getQuantity(),
                    UITheme.formatCurrency(ci.getUnitPrice(), sym),
                    UITheme.formatCurrency(ci.getLineTotal(), sym)
            });
        }
    }

    private void viewSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0 || row >= currentInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first.", "Select Invoice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Invoice inv = currentInvoices.get(row);
        InvoicePreviewDialog dlg = new InvoicePreviewDialog(parentWindow, inv);
        dlg.setVisible(true);
    }

    private void openSelectedInBrowser() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0 || row >= currentInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Please select an invoice first.", "Select Invoice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Invoice inv = currentInvoices.get(row);
        File htmlFile = InvoiceGenerator.saveHtmlInvoiceToFile(inv, dataStore.getSettings());
        if (htmlFile != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(htmlFile);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Saved to: " + htmlFile.getAbsolutePath(), "Invoice Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }
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
                + "This will delete all past transaction records and reset sales revenue to zero.\n\n"
                + "Do you wish to proceed?",
                "Confirm Reset Sales Data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dataStore.resetSalesData();
            refreshTable();
            JOptionPane.showMessageDialog(this, "All sales data and transaction history have been reset!", "Sales Data Reset", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
