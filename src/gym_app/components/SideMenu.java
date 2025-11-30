package gym_app.components;

import gym_app.MainFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Menu bên trái của Dashboard
 */
public class SideMenu extends JPanel {

    private MainFrame mainFrame;
    private JButton selectedButton = null;

    // Menu items
    private static final String[][] MENU_ITEMS = {
        {"🏠", "Trang chủ", MainFrame.SCREEN_DASHBOARD},
        {"💰", "Nạp tiền", MainFrame.SCREEN_TOPUP},
        {"📦", "Gói tập", MainFrame.SCREEN_PACKAGES},
        {"🛒", "Mua gói", MainFrame.SCREEN_BUY_PACKAGE},
        {"🚪", "Check-in", MainFrame.SCREEN_CHECKIN},
        {"📋", "Lịch sử", MainFrame.SCREEN_HISTORY},
        {"👤", "Sửa thông tin", MainFrame.SCREEN_PROFILE},
        {"🔐", "Đổi PIN", MainFrame.SCREEN_CHANGE_PIN},
    };

    public SideMenu(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(25, 25, 35));
        setPreferredSize(new Dimension(220, 0));
        setBorder(new EmptyBorder(20, 10, 20, 10));

        // Logo
        JLabel logo = new JLabel("💪 POWER GYM");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(new Color(0, 200, 180));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(logo);

        add(Box.createVerticalStrut(30));

        // Menu items
        for (String[] item : MENU_ITEMS) {
            add(createMenuItem(item[0], item[1], item[2]));
            add(Box.createVerticalStrut(5));
        }

        add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = createMenuItem("🚪", "Đăng xuất", null);
        logoutBtn.setBackground(new Color(180, 50, 50));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                mainFrame, 
                "Bạn có chắc muốn đăng xuất?", 
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.logout();
            }
        });
        add(logoutBtn);
    }

    private JButton createMenuItem(String icon, String text, String screen) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 40, 55));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != selectedButton) {
                    btn.setBackground(new Color(60, 60, 80));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != selectedButton) {
                    btn.setBackground(new Color(40, 40, 55));
                }
            }
        });

        // Click action
        if (screen != null) {
            btn.addActionListener(e -> {
                // Deselect previous
                if (selectedButton != null) {
                    selectedButton.setBackground(new Color(40, 40, 55));
                }
                // Select current
                selectedButton = btn;
                btn.setBackground(new Color(0, 150, 136));
                
                mainFrame.showScreen(screen);
            });
        }

        return btn;
    }

    public void selectItem(int index) {
        // Có thể implement nếu cần
    }
}