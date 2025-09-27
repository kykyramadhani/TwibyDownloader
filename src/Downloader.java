import javax.swing.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Downloader extends SwingWorker<Void, Object> {

    private final String urlString;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final long knownFileSize; // File size passed from dialog

    public Downloader(String urlString, JProgressBar progressBar, JLabel statusLabel, long knownFileSize) {
        this.urlString = urlString;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.knownFileSize = knownFileSize;
    }

    @Override
    protected Void doInBackground() throws Exception {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        int lastPublishedProgress = -1;
        long totalBytesRead = 0;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            long totalFileSize = knownFileSize;

            // If file size is still unknown, try to get it from the GET request
            if (totalFileSize == -1) {
                totalFileSize = connection.getContentLengthLong();
            }

            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || fileName.contains("?")) {
                fileName = "downloaded_file";
            }

            inputStream = connection.getInputStream();
            fileOutputStream = new FileOutputStream(fileName);

            final String finalFileName = fileName;
            SwingUtilities.invokeLater(() -> statusLabel.setText("Downloading: " + finalFileName));

            byte[] buffer = new byte[4096];
            int bytesRead;

            if (totalFileSize > 0) {
                // Size known, publish percentage progress
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isCancelled()) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    int currentProgress = (int) ((totalBytesRead * 100) / totalFileSize);
                    if (currentProgress > lastPublishedProgress) {
                        lastPublishedProgress = currentProgress;
                        publish(Integer.valueOf(currentProgress));
                        Thread.sleep(25); // Slow down for visual effect
                    }
                }
                publish(Integer.valueOf(100));
            } else {
                // Size unknown, publish bytes downloaded and use indeterminate progress
                publish("start_unknown");
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isCancelled()) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    publish(Long.valueOf(totalBytesRead));
                    Thread.sleep(25); // Slow down for visual effect
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {}
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    @Override
    protected void process(List<Object> chunks) {
        if (isCancelled()) return;
        Object latest = chunks.get(chunks.size() - 1);
        if (latest instanceof String) {
            if ("start_unknown".equals(latest)) {
                progressBar.setIndeterminate(true);
                statusLabel.setText("Downloading (size unknown)...");
            }
        } else if (latest instanceof Integer) {
            progressBar.setIndeterminate(false); // Ensure determinate mode
            progressBar.setValue((Integer) latest);
        } else if (latest instanceof Long) {
            statusLabel.setText("Downloaded: " + formatBytes((Long) latest));
        }
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get(); // Check for exceptions
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    statusLabel.setText("Status: Selesai");
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                statusLabel.setText("Status: Error! " + e.getMessage());
            });
            e.printStackTrace();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }
}