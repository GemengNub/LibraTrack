package school.librarylogging;

import com.google.zxing.WriterException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Dialog that lets administrators pick a book, generate its QR code, preview it, and save the PNG.
 */
public class BookQrDialog extends JDialog {

    private final QrCode qrCode;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final JLabel previewLabel;
    private final JButton generateButton;
    private final JButton saveButton;

    private BufferedImage currentQrImage;
    private String currentFileNameBase;

    public BookQrDialog(Frame owner, QrCode qrCode) {
        super(owner, "Generate Book QR", true);
        this.qrCode = qrCode;

        setMinimumSize(new Dimension(700, 500));
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);

        String[] columns = {"book_id", "book_name", "borrowed"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(new SelectionWatcher());

        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Select a Book"));

        previewLabel = new JLabel("No QR generated", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(260, 260));
        previewLabel.setBorder(BorderFactory.createTitledBorder("QR Preview"));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(previewLabel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        generateButton = new JButton("Generate QR");
        saveButton = new JButton("Save QR");
        JButton closeButton = new JButton("Close");

        saveButton.setEnabled(false);

        generateButton.addActionListener(e -> generateQr());
        saveButton.addActionListener(e -> saveQr());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        add(statusLabel, BorderLayout.NORTH);

        loadTableData();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        Object[][] data = DatabaseHandler.findBookAsTableData(null);
        if (data.length == 0) {
            statusLabel.setText("No books found in database.");
            return;
        }
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void generateQr() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            statusLabel.setText("Select a book first.");
            return;
        }

        Object bookIdObj = tableModel.getValueAt(selectedRow, 0);
        Object bookNameObj = tableModel.getValueAt(selectedRow, 1);
        Object borrowedObj = tableModel.getValueAt(selectedRow, 2);

        String bookId = String.valueOf(bookIdObj);
        String bookName = String.valueOf(bookNameObj);
        String borrowed = String.valueOf(borrowedObj);

        String payload = String.format("book_id=%s;book_name=%s;borrowed=%s", bookId, bookName, borrowed);
        try {
            currentQrImage = qrCode.createQrImage(payload, 300);
            currentFileNameBase = sanitizeFileName(bookId + "_" + bookName);
            previewLabel.setIcon(new ImageIcon(currentQrImage));
            previewLabel.setText(null);
            saveButton.setEnabled(true);
            statusLabel.setText("QR generated for book " + bookId + ".");
        } catch (WriterException ex) {
            statusLabel.setText("Failed to generate QR: " + ex.getMessage());
        }
    }

    private void saveQr() {
        if (currentQrImage == null || currentFileNameBase == null) {
            statusLabel.setText("Generate a QR first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save QR Code");
        chooser.setSelectedFile(new File(currentFileNameBase + ".png"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        String fileName = selectedFile.getName().toLowerCase().endsWith(".png")
                ? selectedFile.getName()
                : selectedFile.getName() + ".png";
        Path destination = selectedFile.toPath().resolveSibling(fileName);
        try {
            qrCode.saveQrImage(currentQrImage, destination);
            statusLabel.setText("Saved QR to " + destination);
        } catch (IOException ex) {
            statusLabel.setText("Failed to save: " + ex.getMessage());
        }
    }

    private static String sanitizeFileName(String candidate) {
        return candidate.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private class SelectionWatcher implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                statusLabel.setText(" ");
            }
        }
    }
}

