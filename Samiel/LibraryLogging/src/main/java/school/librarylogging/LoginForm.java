package school.librarylogging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.InputMismatchException;

/**
 * Login Form - The landing window that runs when the program starts.
 * Contains username/password fields and Login/Cancel buttons.
 */
public class LoginForm extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    
    private static final String FORBIDDEN_CHARS_REGEX = "^[^<>/\\\\^();:'\"\\[\\]\\{\\}\\|]+$";
    
    public LoginForm() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Library Logging System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Title label
        JLabel titleLabel = new JLabel("Library Logging System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Username:");
        formPanel.add(usernameLabel, gbc);
        
        // Username Field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);
        
        // Password Field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Status label for error messages
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Allow Enter key to trigger login
        getRootPane().setDefaultButton(loginButton);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        
        try {
            // Validate input
            if (username.isEmpty() || passwordChars.length == 0) {
                statusLabel.setText("Username and password are required");
                return;
            }
            
            String password = new String(passwordChars);
            
            // Check for forbidden characters
            if (!username.matches(FORBIDDEN_CHARS_REGEX) || !password.matches(FORBIDDEN_CHARS_REGEX)) {
                statusLabel.setText("Cannot contain special characters");
                return;
            }
            
            // Attempt login using existing logic from Main.Login
            if (Main.Login.isLoggedIn(username, password)) {
                statusLabel.setText(" ");
                // Open main form and close login
                SwingUtilities.invokeLater(() -> {
                    MainForm mainForm = new MainForm(Main.user);
                    mainForm.setVisible(true);
                    dispose();
                });
            } else {
                statusLabel.setText("Invalid username or password");
                passwordField.setText("");
            }
        } finally {
            // Clear password from memory for security
            Arrays.fill(passwordChars, '\0');
        }
    }
    
    /**
     * Launch the login form
     */
    public static void showLoginForm() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            new LoginForm().setVisible(true);
        });
    }
}
