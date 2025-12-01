package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.GymButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình đăng nhập - CHỈ 1 THẺ, KHÔNG CẦN CHỌN
 */
public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JPasswordField txtPin;
    private JLabel lblError;
    private JLabel lblTries;
    private JLabel lblCardStatus;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 45));

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

        // Card status
        lblCardStatus = new JLabel("🎫 Đã nhận diện thẻ");
        lblCardStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCardStatus.setForeground(new Color(46, 204, 113));
        lblCardStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
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
        txtPin.addActionListener(e -> doLogin());

        // Error & Tries labels
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTries = new JLabel("PIN mặc định: 123456");
        lblTries.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTries.setForeground(Color.GRAY);
        lblTries.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        GymButton btnLogin = GymButton.success("✓ ĐĂNG NHẬP");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(250, 45));
        btnLogin.addActionListener(e -> doLogin());

        // Forgot PIN button
        JButton btnForgot = new JButton("🔓 Quên PIN / Mở khóa thẻ");
        btnForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnForgot.setForeground(new Color(52, 152, 219));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnForgot.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_UNBLOCK));

        // Layout
        container.add(logo);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(30));
        container.add(lblCardStatus);
        container.add(Box.createVerticalStrut(20));
        container.add(title);
        container.add(Box.createVerticalStrut(20));
        container.add(lblPin);
        container.add(Box.createVerticalStrut(10));
        container.add(txtPin);
        container.add(Box.createVerticalStrut(10));
        container.add(lblError);
        container.add(lblTries);
        container.add(Box.createVerticalStrut(25));
        container.add(btnLogin);
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

        // Kiểm tra thẻ bị khóa
        if (mainFrame.getCardService().isCardBlocked()) {
            showCardBlocked();
            return;
        }

        // Verify PIN
        if (mainFrame.getCardService().verifyPIN(pin)) {
            lblError.setText(" ");

            // Lấy thông tin từ applet
            String cardId = mainFrame.getCardService().getCardId();
            String info = mainFrame.getCardService().getInfo();
            
            String name = "Khách hàng";
            String phone = mainFrame.getCardService().getRecoveryPhone();
            
            if (info != null && !info.isEmpty()) {
                String[] parts = info.split("\\|", -1);
                if (parts.length >= 1 && !parts[0].isEmpty()) name = parts[0];
                if (parts.length >= 2 && !parts[1].isEmpty()) phone = parts[1];
            }

            System.out.println("[LOGIN] ✅ Login success: " + name);

            // *** KIỂM TRA LẦN ĐẦU ĐĂNG NHẬP ***
            if (mainFrame.getCardService().isFirstLogin()) {
                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h2>⚠️ ĐỔI PIN BẮT BUỘC</h2>" +
                    "<p>Đây là lần đầu tiên bạn đăng nhập.</p>" +
                    "<p>Vui lòng đổi mã PIN để bảo mật tài khoản.</p>" +
                    "</center></html>",
                    "Đổi PIN lần đầu",
                    JOptionPane.WARNING_MESSAGE
                );
                
                mainFrame.showChangePinFirstTime(cardId, name, phone);
                txtPin.setText("");
                return;
            }

            // Đăng nhập thành công
            mainFrame.onLoginSuccess(cardId, name, phone);
            txtPin.setText("");

        } else {
            int tries = mainFrame.getCardService().getPinTriesRemaining();
            
            if (tries <= 0 || mainFrame.getCardService().isCardBlocked()) {
                showCardBlocked();
            } else {
                lblTries.setText("⚠️ Còn " + tries + " lần thử");
                lblTries.setForeground(tries <= 2 ? new Color(231, 76, 60) : new Color(241, 196, 15));
                showError("PIN không đúng!");
            }
            
            txtPin.setText("");
        }
    }

    private void showCardBlocked() {
        lblCardStatus.setText("🔒 THẺ ĐÃ BỊ KHÓA!");
        lblCardStatus.setForeground(new Color(231, 76, 60));
        lblTries.setText("Nhập sai PIN quá 5 lần");
        lblTries.setForeground(new Color(231, 76, 60));
        txtPin.setEnabled(false);
        
        JOptionPane.showMessageDialog(this,
            "<html><center>" +
            "<h2>🔒 THẺ ĐÃ BỊ KHÓA!</h2>" +
            "<p>Bạn đã nhập sai PIN quá 5 lần.</p>" +
            "<br>" +
            "<p>Vui lòng sử dụng chức năng <b>'Quên PIN / Mở khóa thẻ'</b></p>" +
            "<p>với số điện thoại đã đăng ký để mở khóa.</p>" +
            "</center></html>",
            "Thẻ bị khóa",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void showError(String msg) {
        lblError.setText(msg);
        txtPin.requestFocus();
    }

    public void onShow() {
        txtPin.setText("");
        lblError.setText(" ");
        
        // Kiểm tra trạng thái thẻ
        if (mainFrame.getCardService().isCardBlocked()) {
            lblCardStatus.setText("🔒 THẺ ĐÃ BỊ KHÓA!");
            lblCardStatus.setForeground(new Color(231, 76, 60));
            lblTries.setText("Dùng 'Quên PIN' để mở khóa");
            lblTries.setForeground(new Color(231, 76, 60));
            txtPin.setEnabled(false);
        } else {
            lblCardStatus.setText("🎫 Đã nhận diện thẻ: " + mainFrame.getCardService().getCardId());
            lblCardStatus.setForeground(new Color(46, 204, 113));
            
            int tries = mainFrame.getCardService().getPinTriesRemaining();
            if (tries < 5) {
                lblTries.setText("Còn " + tries + " lần thử");
                lblTries.setForeground(tries <= 2 ? new Color(241, 196, 15) : Color.GRAY);
            } else {
                lblTries.setText("PIN mặc định: 123456");
                lblTries.setForeground(Color.GRAY);
            }
            txtPin.setEnabled(true);
        }
        
        txtPin.requestFocus();
    }
}