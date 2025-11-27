package school.librarylogging;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * QR Scanner Form - Camera interface for QR scanning workflow.
 * Shows device camera, displays scan status, and handles book borrowing flow.
 * Status states: "No QR", "QR Detected", "QR Scan Failed"
 */
public class QRScannerForm extends JFrame implements Runnable {
    
    private MainForm parentForm;
    private User currentUser;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private JLabel statusLabel;
    private JLabel qrInfoLabel;
    private JButton borrowButton;
    private JButton cancelButton;
    private JPanel qrInfoPanel;
    
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean scanning = new AtomicBoolean(true);
    private String detectedQRContent = null;
    private final Object scanningLock = new Object();
    
    // Status constants
    private static final String STATUS_NO_QR = "No QR";
    private static final String STATUS_QR_DETECTED = "QR Detected";
    private static final String STATUS_QR_SCAN_FAILED = "QR Scan Failed";
    private static final String STATUS_SCANNING = "Scanning...";
    
    public QRScannerForm(MainForm parent, User user) {
        this.parentForm = parent;
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Library Logging System - QR Scanner");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(640, 580));
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Scan QR Code to Borrow Book", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Webcam panel placeholder
        JPanel cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.setPreferredSize(new Dimension(640, 480));
        cameraContainer.setBackground(Color.BLACK);
        
        // Try to initialize webcam
        try {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcamPanel = new WebcamPanel(webcam);
                webcamPanel.setMirrored(true);
                webcamPanel.setFPSDisplayed(false);
                cameraContainer.add(webcamPanel, BorderLayout.CENTER);
                
                // Start scanning thread
                executor.execute(this);
            } else {
                JLabel noCameraLabel = new JLabel("No camera detected", SwingConstants.CENTER);
                noCameraLabel.setForeground(Color.WHITE);
                noCameraLabel.setFont(new Font("Arial", Font.BOLD, 16));
                cameraContainer.add(noCameraLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Camera Error: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            cameraContainer.add(errorLabel, BorderLayout.CENTER);
        }
        
        mainPanel.add(cameraContainer, BorderLayout.CENTER);
        
        // Bottom panel with status and buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        
        // Status label
        statusLabel = new JLabel(STATUS_SCANNING, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateStatusColor(STATUS_SCANNING);
        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createVerticalStrut(10));
        
        // QR Info panel (initially hidden)
        qrInfoPanel = new JPanel();
        qrInfoPanel.setLayout(new BoxLayout(qrInfoPanel, BoxLayout.Y_AXIS));
        qrInfoPanel.setBorder(BorderFactory.createTitledBorder("Detected QR Information"));
        qrInfoPanel.setVisible(false);
        
        qrInfoLabel = new JLabel(" ");
        qrInfoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        qrInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrInfoPanel.add(qrInfoLabel);
        
        bottomPanel.add(qrInfoPanel);
        bottomPanel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        borrowButton = new JButton("Borrow");
        borrowButton.setPreferredSize(new Dimension(120, 35));
        borrowButton.setEnabled(false);
        borrowButton.addActionListener(e -> confirmBorrow());
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.addActionListener(e -> cancelScanning());
        
        buttonPanel.add(borrowButton);
        buttonPanel.add(cancelButton);
        
        bottomPanel.add(buttonPanel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelScanning();
            }
        });
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    @Override
    public void run() {
        // Continuous QR scanning loop
        while (scanning.get()) {
            try {
                Thread.sleep(100); // Scan every 100ms
                
                if (webcam == null || !webcam.isOpen()) {
                    continue;
                }
                
                BufferedImage image = webcam.getImage();
                if (image == null) {
                    updateStatus(STATUS_NO_QR);
                    continue;
                }
                
                // Try to decode QR code
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                
                try {
                    MultiFormatReader reader = new MultiFormatReader();
                    Result result = reader.decode(bitmap);
                    
                    if (result != null && result.getText() != null && !result.getText().isEmpty()) {
                        detectedQRContent = result.getText();
                        scanning.set(false);
                        handleQRDetected(detectedQRContent);
                    }
                } catch (NotFoundException e) {
                    // No QR code found in this frame
                    updateStatus(STATUS_NO_QR);
                } catch (Exception e) {
                    updateStatus(STATUS_QR_SCAN_FAILED);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            updateStatusColor(status);
        });
    }
    
    private void updateStatusColor(String status) {
        switch (status) {
            case STATUS_QR_DETECTED:
                statusLabel.setForeground(new Color(0, 150, 0)); // Green
                break;
            case STATUS_QR_SCAN_FAILED:
                statusLabel.setForeground(Color.RED);
                break;
            case STATUS_SCANNING:
                statusLabel.setForeground(Color.BLUE);
                break;
            default:
                statusLabel.setForeground(Color.ORANGE);
        }
    }
    
    private void handleQRDetected(String qrContent) {
        SwingUtilities.invokeLater(() -> {
            updateStatus(STATUS_QR_DETECTED);
            
            // Display QR information
            qrInfoPanel.setVisible(true);
            
            // Parse QR content and check if it contains book information
            String displayInfo = parseQRContent(qrContent);
            qrInfoLabel.setText("<html>" + displayInfo.replace("\n", "<br>") + "</html>");
            
            // Enable borrow button if book info is detected
            if (containsBookInfo(qrContent)) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Borrow");
            } else {
                borrowButton.setEnabled(false);
                borrowButton.setText("No Book Info");
            }
            
            pack();
        });
    }
    
    private String parseQRContent(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("QR Content:\n");
        
        // Try to parse as structured book data
        // Expected format: book_id|book_name|other_info
        String[] parts = content.split("\\|");
        if (parts.length >= 2) {
            sb.append("Book ID: ").append(parts[0]).append("\n");
            sb.append("Book Name: ").append(parts[1]).append("\n");
            if (parts.length > 2) {
                sb.append("Additional Info: ").append(parts[2]);
            }
        } else {
            sb.append(content);
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if the QR content contains valid book information.
     * 
     * Two accepted formats are supported:
     * 1. Structured format with pipe separator: "book_id|book_name" or "book_id|book_name|additional_info"
     *    - Both book_id and book_name must be non-empty
     * 2. Numeric-only book ID: A string containing only digits (e.g., "42")
     *
     * @param content The QR code content to validate
     * @return true if the content contains valid book information, false otherwise
     */
    private boolean containsBookInfo(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        // Check for structured format (book_id|book_name)
        if (content.contains("|")) {
            String[] parts = content.split("\\|");
            return parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
        }
        
        // Check if it's just a book ID (numeric)
        return content.matches("\\d+");
    }
    
    private void confirmBorrow() {
        if (detectedQRContent == null || detectedQRContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No QR code detected", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Parse book info from QR - handle empty strings and delimiter-only content
        String[] parts = detectedQRContent.split("\\|", -1);
        String bookId = (parts.length > 0 && parts[0] != null && !parts[0].trim().isEmpty()) 
                ? parts[0].trim() : "Unknown";
        String bookName = (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty()) 
                ? parts[1].trim() : "Unknown";
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm borrowing:\n\nBook ID: " + bookId + "\nBook Name: " + bookName + 
                "\nBorrower: " + currentUser.name,
                "Confirm Borrow",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Log the borrowing (in real implementation, update database)
            JOptionPane.showMessageDialog(this,
                    "Book '" + bookName + "' has been borrowed successfully!\n" +
                    "Borrower: " + currentUser.name,
                    "Borrow Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            
            closeAndReturn();
        } else {
            // Resume scanning
            resetScanning();
        }
    }
    
    private void resetScanning() {
        synchronized (scanningLock) {
            detectedQRContent = null;
            qrInfoPanel.setVisible(false);
            borrowButton.setEnabled(false);
            scanning.set(true);
            updateStatus(STATUS_SCANNING);
            // Submit a new scanning task using the single-threaded executor
            // which ensures proper task queuing and prevents concurrent scanning threads
            executor.execute(this);
        }
    }
    
    private void cancelScanning() {
        closeAndReturn();
    }
    
    private void closeAndReturn() {
        scanning.set(false);
        
        // Close webcam
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        // Shutdown executor service and wait for termination
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        dispose();
        
        // Return to main form
        if (parentForm != null) {
            parentForm.returnFromScanner();
        }
    }
}
