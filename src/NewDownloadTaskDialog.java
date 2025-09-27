import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewDownloadTaskDialog extends JDialog {

    private JTextField urlTextField;
    private JLabel fileSizeLabel;
    private RoundedButton downloadButton;
    private RoundedButton cancelButton;
    private String enteredUrl = null;
    private static final Color APPLE_GREEN = Color.decode("#60CD38");
    private static final Color LIGHT_GRAY = Color.decode("#F2F2F2");
    private static final Color QUILL_GREY = Color.decode("#A8A8A8");
    private static final Color CHARCOAL = Color.decode("#333333");

    public NewDownloadTaskDialog(JFrame parentFrame) {
        super(parentFrame, "New Download Task", true);
        setSize(500, 250); // Increased height to accommodate file size label
        setResizable(false);
        setLocationRelativeTo(parentFrame);

        initComponents();
        addListeners();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // URL Label and Text Field
        JLabel enterUrlLabel = new JLabel("Enter URL:");
        enterUrlLabel.setFont(new Font("Arial", Font.BOLD, 14));
        enterUrlLabel.setForeground(CHARCOAL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(enterUrlLabel, gbc);

        urlTextField = new JTextField(35);
        urlTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        urlTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(QUILL_GREY, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(urlTextField, gbc);

        // File Size Label
        JLabel fileSizeTitleLabel = new JLabel("File Size:");
        fileSizeTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fileSizeTitleLabel.setForeground(CHARCOAL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(fileSizeTitleLabel, gbc);

        fileSizeLabel = new JLabel("Unknown");
        fileSizeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        fileSizeLabel.setForeground(CHARCOAL);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(fileSizeLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(LIGHT_GRAY);

        cancelButton = new RoundedButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(CHARCOAL);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(new EmptyBorder(7, 15, 7, 15));

        downloadButton = new RoundedButton("Download");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 13));
        downloadButton.setBackground(APPLE_GREEN);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(new EmptyBorder(7, 15, 7, 15));
        downloadButton.setEnabled(false); // Disabled until valid URL

        buttonPanel.add(cancelButton);
        buttonPanel.add(downloadButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void addListeners() {
        // Listen for changes in the URL text field
        urlTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFileSize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFileSize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFileSize();
            }
        });

        downloadButton.addActionListener(e -> {
            enteredUrl = urlTextField.getText().trim();
            if (enteredUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URL tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                enteredUrl = null;
            } else {
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            enteredUrl = null;
            dispose();
        });
    }

    private void updateFileSize() {
        String urlText = urlTextField.getText().trim();
        downloadButton.setEnabled(false);
        fileSizeLabel.setText("Checking...");

        if (urlText.isEmpty()) {
            fileSizeLabel.setText("Unknown");
            return;
        }

        // Run the HEAD request in a separate thread to avoid freezing the UI
        new Thread(() -> {
            try {
                URL url = new URL(urlText);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();
                long fileSize = connection.getContentLengthLong();
                connection.disconnect();

                SwingUtilities.invokeLater(() -> {
                    if (fileSize >= 0) {
                        fileSizeLabel.setText(formatBytes(fileSize));
                        downloadButton.setEnabled(true);
                    } else {
                        fileSizeLabel.setText("Unknown");
                        downloadButton.setEnabled(true); // Allow download even if size unknown
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    fileSizeLabel.setText("Unknown");
                    downloadButton.setEnabled(true); // Allow download on error
                });
            }
        }).start();
    }
    public long getFileSize() {
        String sizeText = fileSizeLabel.getText();
        if (sizeText.equals("Unknown") || sizeText.equals("Checking...")) {
            return -1;
        }
        // Parse the file size back to bytes (inverse of formatBytes)
        try {
            String[] parts = sizeText.split(" ");
            double value = Double.parseDouble(parts[0]);
            String unit = parts[1];
            int exp = "BKMGTPE".indexOf(unit.charAt(0));
            return (long) (value * Math.pow(1024, exp));
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    public String getEnteredUrl() {
        return enteredUrl;
    }
}