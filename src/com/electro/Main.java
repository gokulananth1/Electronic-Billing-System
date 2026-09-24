package com.electro;

import com.electro.service.AuthService;
import com.electro.service.DataStore;
import com.electro.ui.LoginDialog;
import com.electro.ui.MainFrame;

import javax.swing.*;

/**
 * Main application launcher for the Electronics Shop Billing System with authentication.
 */
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to System native for crisp UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Initialize Services
        DataStore.getInstance();
        AuthService.getInstance();

        // Launch Login Dialog first
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDlg = new LoginDialog(null);
            loginDlg.setVisible(true);

            if (loginDlg.isAuthenticated()) {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
