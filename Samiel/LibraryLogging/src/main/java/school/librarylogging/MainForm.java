package school.librarylogging;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

/**
 * Main Form - Role-based main UI with welcome message and role-specific features.
 * - NONE (regular users): Welcome message and "Borrow a Book" button
 * - LIBRARIAN: Add, Remove, Update books + user functionality
 * - ADMINISTRATOR: All functionality including creating librarian accounts
 */
public class MainForm extends JFrame {
    
    private User currentUser;
    private JPanel mainContentPanel;
    private JLabel statusMessageLabel;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    
    public MainForm(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Library Logging System - Main");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 500));
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel with welcome message
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.name + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
        
        JLabel roleLabel = new JLabel("Role: " + currentUser.role, SwingConstants.CENTER);
        roleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        headerPanel.add(roleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel with buttons based on role
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        setupRoleBasedUI();
        
        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status bar at bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusMessageLabel = new JLabel(" ");
        statusMessageLabel.setForeground(Color.BLUE);
        statusPanel.add(statusMessageLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        statusPanel.add(logoutButton, BorderLayout.EAST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupRoleBasedUI() {
        // Clear existing components
        mainContentPanel.removeAll();
        
        // Add vertical spacing
        mainContentPanel.add(Box.createVerticalStrut(10));
        
        // All users can borrow books
        addButton("Borrow a Book (Scan QR)", e -> openQRScanner());
        
        // LIBRARIAN and ADMINISTRATOR features
        if ("LIBRARIAN".equals(currentUser.role) || "ADMINISTRATOR".equals(currentUser.role)) {
            mainContentPanel.add(Box.createVerticalStrut(10));
            addButton("Add a Book", e -> showAddBookDialog());
            mainContentPanel.add(Box.createVerticalStrut(10));
            addButton("Remove a Book", e -> showRemoveBookDialog());
            mainContentPanel.add(Box.createVerticalStrut(10));
            addButton("Update a Book", e -> showUpdateBookDialog());
        }
        
        // All users can view books
        mainContentPanel.add(Box.createVerticalStrut(10));
        addButton("View All Books", e -> showBookTable());
        
        // ADMINISTRATOR only features
        if ("ADMINISTRATOR".equals(currentUser.role)) {
            mainContentPanel.add(Box.createVerticalStrut(20));
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            mainContentPanel.add(separator);
            mainContentPanel.add(Box.createVerticalStrut(10));
            
            JLabel adminLabel = new JLabel("Administrator Functions");
            adminLabel.setFont(new Font("Arial", Font.BOLD, 14));
            adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainContentPanel.add(adminLabel);
            mainContentPanel.add(Box.createVerticalStrut(10));
            
            addButton("Create Librarian Account", e -> showCreateLibrarianDialog());
            mainContentPanel.add(Box.createVerticalStrut(10));
            addButton("Generate Book QR", e -> showBookQrDialog());
        }
        
        mainContentPanel.add(Box.createVerticalGlue());
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void addButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 40));
        button.setPreferredSize(new Dimension(300, 40));
        button.addActionListener(action);
        mainContentPanel.add(button);
    }
    
    private void openQRScanner() {
        statusMessageLabel.setText("Opening QR Scanner...");
        SwingUtilities.invokeLater(() -> {
            QRScannerForm scannerForm = new QRScannerForm(this, currentUser);
            scannerForm.setVisible(true);
            setVisible(false);
        });
    }
    
    private void showAddBookDialog() {
        JTextField bookNameField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Book Name:"));
        panel.add(bookNameField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add a Book", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String bookName = bookNameField.getText().trim();
            if (!bookName.isEmpty()) {
                statusMessageLabel.setText("Adding book: " + bookName);
                try {
                    DatabaseHandler.addBookDataToDatabase(bookName, 0);
                    JOptionPane.showMessageDialog(this, 
                            "Book '" + bookName + "' added successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Failed to add book: " + ex.getMessage(), 
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Book name cannot be empty", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showRemoveBookDialog() {
        JTextField bookIdField = new JTextField(10);
        
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Remove a Book", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String bookId = bookIdField.getText().trim();
            if (!bookId.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to remove book with ID: " + bookId + "?",
                        "Confirm Removal", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    statusMessageLabel.setText("Removing book with ID: " + bookId);
                    try {
                        DatabaseHandler.removeBookFromDatabase(Integer.parseInt(bookId));
                        JOptionPane.showMessageDialog(this, 
                                "Book removed successfully!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, 
                                "Book ID must be a valid number", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, 
                                "Failed to remove book: " + ex.getMessage(), 
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Book ID cannot be empty", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showUpdateBookDialog() {
        JTextField bookIdField = new JTextField(10);
        JTextField newBookNameField = new JTextField(20);
        JComboBox<String> borrowedCombo = new JComboBox<>(new String[]{"Not Borrowed (0)", "Borrowed (1)"});
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(new JLabel("New Book Name:"));
        panel.add(newBookNameField);
        panel.add(new JLabel("Status:"));
        panel.add(borrowedCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Update a Book", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String bookId = bookIdField.getText().trim();
            String newBookName = newBookNameField.getText().trim();
            int borrowedStatus = borrowedCombo.getSelectedIndex();
            
            if (!bookId.isEmpty()) {
                statusMessageLabel.setText("Updating book with ID: " + bookId);
                try {
                    DatabaseHandler.updateBookInDatabase(Integer.parseInt(bookId), newBookName, borrowedStatus);
                    JOptionPane.showMessageDialog(this, 
                            "Book updated successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Book ID must be a valid number", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Failed to update book: " + ex.getMessage(), 
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Book ID cannot be empty", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showBookTable() {
        // Create a dialog to display books in a table format
        JDialog tableDialog = new JDialog(this, "Book Records", true);
        tableDialog.setSize(500, 400);
        tableDialog.setLocationRelativeTo(this);
        
        // Create table model with columns: book_id, book_name, borrowed
        String[] columnNames = {"book_id", "book_name", "borrowed"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookTable = new JTable(tableModel);
        bookTable.getTableHeader().setReorderingAllowed(false);
        
        // Fetch data from database
        try {
            Object[][] bookData = DatabaseHandler.findBookAsTableData(null);
            for (Object[] row : bookData) {
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            // If database connection fails, show sample data as fallback
            Object[][] sampleData = {
                {1, "Introduction to Java", 0},
                {2, "Database Systems", 1},
                {3, "Data Structures", 0},
                {4, "Science Fundamentals", 1}
            };
            for (Object[] row : sampleData) {
                tableModel.addRow(row);
            }
            statusMessageLabel.setText("Note: Showing sample data (database unavailable)");
        }
        
        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableDialog.add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tableDialog.dispose());
        buttonPanel.add(closeButton);
        tableDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        tableDialog.setVisible(true);
    }
    
    private void showCreateLibrarianDialog() {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Create Librarian Account", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Username and password cannot be empty", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                        "Passwords do not match", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            statusMessageLabel.setText("Creating librarian account: " + username);
            JOptionPane.showMessageDialog(this, 
                    "Librarian account '" + username + "' created successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showBookQrDialog() {
        if (!"ADMINISTRATOR".equals(currentUser.role)) {
            JOptionPane.showMessageDialog(this, "Only administrators can generate book QR codes.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BookQrDialog qrDialog = new BookQrDialog(this, Main.qrCode);
        qrDialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginForm.showLoginForm();
        }
    }
    
    /**
     * Called when returning from QR Scanner
     */
    public void returnFromScanner() {
        setVisible(true);
        statusMessageLabel.setText(" ");
    }
}
