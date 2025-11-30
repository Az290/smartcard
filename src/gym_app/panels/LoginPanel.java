package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.GymButton;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình đăng nhập bằng PIN
 */
public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JPasswordField txtPin;
    private JLabel lblError;
    private JLabel lblTries;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 45));

        // Container chính
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(40, 40, 55));
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 136), 2),
            new EmptyBorder(40, 50, 40, 50)
        ));
        container.setPreferredSize(new Dimension(450, 500));

        // Logo
        JLabel logo = new JLabel("💪 POWER GYM");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(new Color(0, 200, 180));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hệ thống thẻ thành viên thông minh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tiêu đề
        JLabel title = new JLabel("🔐 ĐĂNG NHẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // PIN input
        JLabel lblPin = new JLabel("Nhập mã PIN (6 số):");
        lblPin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPin.setForeground(Color.WHITE);
        lblPin.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPin = new JPasswordField(6);
        txtPin.setFont(new Font("Segoe UI", Font.BOLD, 32));
        txtPin.setHorizontalAlignment(JTextField.CENTER);
        txtPin.setMaximumSize(new Dimension(200, 50));
        txtPin.setBackground(new Color(60, 60, 75));
        txtPin.setForeground(Color.WHITE);
        txtPin.setCaretColor(Color.WHITE);
        txtPin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tries remaining
        lblTries = new JLabel("Còn 5 lần thử");
        lblTries.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTries.setForeground(Color.GRAY);
        lblTries.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        GymButton btnLogin = GymButton.success("✓ ĐĂNG NHẬP");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(250, 45));
        btnLogin.addActionListener(e -> doLogin());

        GymButton btnRegister = GymButton.info("📝 ĐĂNG KÝ THẺ MỚI");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(250, 45));
        btnRegister.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_REGISTER));

        JButton btnForgot = new JButton("Quên PIN?");
        btnForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnForgot.setForeground(new Color(52, 152, 219));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnForgot.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_UNBLOCK));

        // Enter key
        txtPin.addActionListener(e -> doLogin());

        // Add components
        container.add(logo);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(30));
        container.add(title);
        container.add(Box.createVerticalStrut(25));
        container.add(lblPin);
        container.add(Box.createVerticalStrut(10));
        container.add(txtPin);
        container.add(Box.createVerticalStrut(10));
        container.add(lblError);
        container.add(lblTries);
        container.add(Box.createVerticalStrut(25));
        container.add(btnLogin);
        container.add(Box.createVerticalStrut(15));
        container.add(btnRegister);
        container.add(Box.createVerticalStrut(20));
        container.add(btnForgot);

        add(container);
    }

    private void doLogin() {
        String pin = new String(txtPin.getPassword());

        if (pin.length() != 6 || !pin.matches("\\d{6}")) {
            showError("PIN phải đúng 6 chữ số!");
            return;
        }

        // Verify PIN với SmartCard
        if (mainFrame.getCardService().verifyPIN(pin)) {
            lblError.setText(" ");

            // Kiểm tra phải đổi PIN không
            if (mainFrame.getCardService().isMustChangePIN()) {
                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn cần đổi PIN lần đầu tiên.\nĐổi PIN ngay bây giờ?",
                    "Đổi PIN bắt buộc",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    mainFrame.showScreen(MainFrame.SCREEN_CHANGE_PIN);
                    return;
                }
            }

            // Lấy thông tin từ DB (giả lập)
            String cardId = mainFrame.getCardService().isCardRegistered() ? 
                           "GYM" + System.currentTimeMillis() % 100000 : "GUEST";
            
            // Thử lấy từ DB
            DatabaseService.MemberInfo member = null;
            // member = mainFrame.getDbService().getMemberByCardId(cardId);

            String name = member != null ? member.name : "Khách hàng";
            String phone = member != null ? member.phone : "0901234567";

            mainFrame.onLoginSuccess(cardId, name, phone);
            txtPin.setText("");

        } else {
            int tries = mainFrame.getCardService().getPinTriesRemaining();
            lblTries.setText("Còn " + tries + " lần thử");
            
            if (tries <= 0) {
                showError("Thẻ đã bị khóa! Vui lòng liên hệ quầy.");
                txtPin.setEnabled(false);
            } else if (tries <= 2) {
                showError("PIN sai! Cẩn thận - còn " + tries + " lần!");
                lblTries.setForeground(new Color(231, 76, 60));
            } else {
                showError("PIN không đúng!");
            }
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        txtPin.setText("");
        txtPin.requestFocus();

        // Shake animation
        Timer timer = new Timer(50, null);
        final int[] count = {0};
        final int[] offset = {-10, 10, -8, 8, -5, 5, -2, 2, 0};
        Point originalLocation = txtPin.getLocation();
        
        timer.addActionListener(e -> {
            if (count[0] < offset.length) {
                txtPin.setLocation(originalLocation.x + offset[count[0]], originalLocation.y);
                count[0]++;
            } else {
                txtPin.setLocation(originalLocation);
                timer.stop();
            }
        });
        timer.start();
    }
}