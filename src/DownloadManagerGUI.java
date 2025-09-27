import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DownloadManagerGUI {

    private JFrame frame;
    private JPanel mainPanel;
    private RoundedButton newTaskButton;
    private JPanel downloadListPanel;

    private static final Color BLEU_DE_FRANCE = Color.decode("#2D96F6");
    private static final Color LIGHT_GRAY = Color.decode("#F2F2F2");
    private static final Color QUILL_GREY = Color.decode("#A8A8A8");

    private Map<String, Downloader> activeDownloads = new HashMap<>();

    public DownloadManagerGUI() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Pro Download Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_GRAY);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, QUILL_GREY),
                new EmptyBorder(5, 10, 5, 10)));

        newTaskButton = new RoundedButton("New Task");
        newTaskButton.setFont(new Font("Arial", Font.BOLD, 14));
        newTaskButton.setBackground(BLEU_DE_FRANCE);
        newTaskButton.setForeground(Color.WHITE);
        newTaskButton.setFocusPainted(false);
        newTaskButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        headerPanel.add(newTaskButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        downloadListPanel = new JPanel();
        downloadListPanel.setLayout(new BoxLayout(downloadListPanel, BoxLayout.Y_AXIS));
        downloadListPanel.setBackground(LIGHT_GRAY);
        JScrollPane scrollPane = new JScrollPane(downloadListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        newTaskButton.addActionListener(e -> {
            NewDownloadTaskDialog dialog = new NewDownloadTaskDialog(frame);
            dialog.setVisible(true);

            String url = dialog.getEnteredUrl();
            long fileSize = dialog.getFileSize();
            if (url != null && !url.isEmpty()) {
                startDownload(url, fileSize);
            }
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void startDownload(String urlString, long fileSize) {
        DownloadItemPanel itemPanel = new DownloadItemPanel(urlString);
        downloadListPanel.add(itemPanel);
        downloadListPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spasi antar item
        downloadListPanel.revalidate();
        downloadListPanel.repaint();

        Downloader downloader = new Downloader(urlString, itemPanel.getProgressBar(), itemPanel.getStatusLabel(), fileSize);
        activeDownloads.put(urlString, downloader);

        downloader.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                activeDownloads.remove(urlString);
            }
        });
        downloader.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DownloadManagerGUI::new);
    }
}

class DownloadItemPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private static final Color APPLE_GREEN = Color.decode("#60CD38");
    private static final Color LIGHT_GRAY = Color.decode("#F2F2F2");
    private static final Color QUILL_GREY = Color.decode("#A8A8A8");
    private static final Color CHARCOAL = Color.decode("#333333");

    public DownloadItemPanel(String urlString) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DDDDDD"), 1),
                new EmptyBorder(10, 15, 10, 15)));
        setBackground(Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
        if (fileName.isEmpty() || fileName.contains("?")) fileName = "downloaded_file";
        JLabel fileNameLabel = new JLabel(fileName);
        fileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fileNameLabel.setForeground(CHARCOAL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        add(fileNameLabel, gbc);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200, 22));
        progressBar.setBackground(LIGHT_GRAY);
        progressBar.setForeground(APPLE_GREEN);
        progressBar.setBorder(BorderFactory.createLineBorder(QUILL_GREY, 1));
        gbc.gridy = 1;
        add(progressBar, gbc);

        statusLabel = new JLabel("Connecting...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(QUILL_GREY.darker());
        gbc.gridy = 2;
        add(statusLabel, gbc);
    }

    public JProgressBar getProgressBar() { return progressBar; }
    public JLabel getStatusLabel() { return statusLabel; }
}