import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private int cornerRadius = 15; // Tingkat kelengkungan sudut

    public RoundedButton(String text) {
        super(text);
        super.setContentAreaFilled(false); // Kita akan menggambar background sendiri

        // Atur warna dasar
        setBackground(Color.decode("#2D96F6")); // Default background
        hoverBackgroundColor = getBackground().brighter(); // Warna saat mouse di atas
        pressedBackgroundColor = getBackground().darker(); // Warna saat ditekan

        // Listener untuk mengubah warna saat mouse berinteraksi
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Jangan ubah warna jika tombol dinonaktifkan
                if (isEnabled()) {
                    setBackground(hoverBackgroundColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(getBackground().darker()); // Kembali ke warna awal
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(pressedBackgroundColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    // Jika mouse masih di dalam tombol, kembali ke warna hover
                    if (getBounds().contains(e.getPoint())) {
                        setBackground(hoverBackgroundColor);
                    } else { // Jika mouse sudah keluar, kembali ke warna awal
                        setBackground(getBackground().darker());
                    }
                }
            }
        });
    }

    // Override paintComponent untuk menggambar tombol kustom
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Aktifkan anti-aliasing agar kurva terlihat mulus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Pilih warna background berdasarkan state tombol
        if (getModel().isPressed()) {
            g2.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverBackgroundColor);
        } else {
            g2.setColor(getBackground());
        }

        // Gambar bentuk persegi panjang dengan sudut melengkung
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        g2.dispose();

        // Biarkan superclass (JButton) menggambar teks di atasnya
        super.paintComponent(g);
    }

    // Override ini agar border tidak digambar oleh default painter
    @Override
    public void paintBorder(Graphics g) {
        // Tidak melakukan apa-apa (kita tidak ingin border default)
    }

    // Setter untuk corner radius jika Anda ingin mengubahnya
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }
}