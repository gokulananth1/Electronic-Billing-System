package com.electro.ui;

import com.electro.model.User;
import com.electro.service.AuthService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative dialog to manage staff accounts, roles, and passwords.
 */
public class UserManagerDialog extends JDialog {
    private final AuthService authService;
    private JTable userTable;
    private DefaultTableModel userTableModel;

    public UserManagerDialog(Window parent) {
        super(parent, "Staff & User Account Management", ModalityType.APPLICATION_MODAL);
        this.authService = AuthService.getInstance();

        initUI();
    }

    private void initUI() {
        setSize(650, 480);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.COLOR_BG);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(UITheme.COLOR_PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("\uD83D\uDC65 Staff Accounts & Access Control");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Create cashiers, assign roles, and manage credentials", SwingConstants.LEFT);
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(new Color(203, 213, 225));

        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Username", "Full Name", "Role", "Created Date"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(userTableModel);
        UITheme.styleTable(userTable);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(UITheme.COLOR_PANEL_BG);
        tableWrapper.setBorder(new CompoundBorder(
                new EmptyBorder(10, 16, 6, 16),
                new LineBorder(UITheme.COLOR_BORDER, 1, true)
        ));
        tableWrapper.add(new JScrollPane(userTable), BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UITheme.COLOR_PANEL_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER));

        JButton btnAdd = UITheme.createButton("+ Add New User", UITheme.COLOR_SUCCESS, Color.WHITE);
        btnAdd.addActionListener(e -> showAddUserDialog());

        JButton btnChangePass = UITheme.createButton("Change Password", UITheme.COLOR_PRIMARY, Color.WHITE);
        btnChangePass.addActionListener(e -> handleChangePassword());

        JButton btnDelete = UITheme.createButton("Delete User", UITheme.COLOR_DANGER, Color.WHITE);
        btnDelete.addActionListener(e -> handleDeleteUser());

        JButton btnClose = UITheme.createButton("Close", UITheme.COLOR_BORDER, UITheme.COLOR_TEXT_PRIMARY);
        btnClose.addActionListener(e -> dispose());

        footer.add(btnAdd);
        footer.add(btnChangePass);
        footer.add(btnDelete);
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        userTableModel.setRowCount(0);
        List<User> list = authService.getAllUsers();
        for (User u : list) {
            userTableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole().name(),
                    u.getCreatedAt()
            });
        }
    }

    private void showAddUserDialog() {
        JDialog dlg = new JDialog(this, "Add New Staff User", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 16, 16, 16));

        JTextField tfUser = UITheme.createTextField(15);
        JTextField tfName = UITheme.createTextField(15);
        JPasswordField tfPass = new JPasswordField(15);
        JComboBox<User.Role> comboRole = new JComboBox<>(User.Role.values());

        form.add(new JLabel("Username:")); form.add(tfUser);
        form.add(new JLabel("Full Name:")); form.add(tfName);
        form.add(new JLabel("Password:")); form.add(tfPass);
        form.add(new JLabel("Role:")); form.add(comboRole);

        dlg.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = UITheme.createButton("Cancel", UITheme.COLOR_BORDER, UITheme.COLOR_TEXT_PRIMARY);
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createButton("Create User", UITheme.COLOR_PRIMARY, Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                String u = tfUser.getText().trim();
                String n = tfName.getText().trim();
                String p = new String(tfPass.getPassword());
                User.Role r = (User.Role) comboRole.getSelectedItem();

                if (u.isEmpty() || p.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Username and password are required.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                authService.createUser(u, p, n.isEmpty() ? u : n, r);
                dlg.dispose();
                refreshTable();
                JOptionPane.showMessageDialog(this, "User '" + u + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(btnCancel);
        actions.add(btnSave);
        dlg.add(actions, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void handleChangePassword() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) userTableModel.getValueAt(row, 0);
        User user = authService.getUserByUsername(username);
        if (user != null) {
            ChangePasswordDialog dlg = new ChangePasswordDialog(this, user);
            dlg.setVisible(true);
        }
    }

    private void handleDeleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) userTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user account '" + username + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                authService.deleteUser(username);
                refreshTable();
                JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
