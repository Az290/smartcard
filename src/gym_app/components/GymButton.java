package gym_app.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Button đẹp với hiệu ứng hover
 */
public class GymButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private Color pressColor;

    public GymButton(String text) {
        this(text, new Color(0, 150, 136)); // Teal mặc định
    }

    public GymButton(String text, Color color) {
        super(text);
        this.normalColor = color;
        this.hoverColor = color.brighter();
        this.pressColor = color.darker();

        setupStyle();
        setupEffects();
    }

    private void setupStyle() {
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(normalColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, 45));
    }

    private void setupEffects() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverColor);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền bo góc
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        // Vẽ text
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    // Preset colors
    public static GymButton primary(String text) {
        return new GymButton(text, new Color(0, 150, 136));
    }

    public static GymButton success(String text) {
        return new GymButton(text, new Color(46, 204, 113));
    }

    public static GymButton danger(String text) {
        return new GymButton(text, new Color(231, 76, 60));
    }

    public static GymButton warning(String text) {
        return new GymButton(text, new Color(241, 196, 15));
    }

    public static GymButton info(String text) {
        return new GymButton(text, new Color(52, 152, 219));
    }
}