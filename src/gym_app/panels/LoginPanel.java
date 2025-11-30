package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.GymButton;

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
        container.setPreferredSize(new Dimension(450, 550));

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
        lblCardStatus = new JLabel("📋 Trạng thái thẻ: Chưa xác định");
        lblCardStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCardStatus.setForeground(Color.GRAY);
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

        // Error & Tries labels
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        // Debug button
        JButton btnDebug = new JButton("🔧 Debug Status");
        btnDebug.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnDebug.setForeground(Color.GRAY);
        btnDebug.setContentAreaFilled(false);
        btnDebug.setBorderPainted(false);
        btnDebug.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDebug.addActionListener(e -> mainFrame.getCardService().printStatus());

        txtPin.addActionListener(e -> doLogin());

        // Layout
        container.add(logo);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(10));
        container.add(lblCardStatus);
        container.add(Box.createVerticalStrut(25));
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
        container.add(Box.createVerticalStrut(10));
        container.add(btnDebug);

        add(container);
        
        updateCardStatus();
    }

    private void updateCardStatus() {
        if (mainFrame.getCardService().isCardRegistered()) {
            lblCardStatus.setText("📋 Trạng thái thẻ: ✅ Đã đăng ký");
            lblCardStatus.setForeground(new Color(46, 204, 113));
        } else {
            lblCardStatus.setText("📋 Trạng thái thẻ: ⚠️ Chưa đăng ký");
            lblCardStatus.setForeground(new Color(241, 196, 15));
        }
        
        int tries = mainFrame.getCardService().getPinTriesRemaining();
        lblTries.setText("Còn " + tries + " lần thử");
        
        if (tries <= 2) {
            lblTries.setForeground(new Color(231, 76, 60));
        } else {
            lblTries.setForeground(Color.GRAY);
        }
    }

   private void doLogin() {
    String pin = new String(txtPin.getPassword());

    if (pin.length() != 6 || !pin.matches("\\d{6}")) {
        showError("PIN phải đúng 6 chữ số!");
        return;
    }

    // TÌM VÀ LOAD THẺ THEO PIN
    if (!mainFrame.getCardService().findAndLoadCardByPIN(pin)) {
        showError("Không tìm thấy thẻ với PIN này!\nVui lòng kiểm tra lại hoặc đăng ký mới.");
        return;
    }

    // Thẻ đã được load, verify PIN
    if (mainFrame.getCardService().verifyPIN(pin)) {
        lblError.setText(" ");

        // Lấy thông tin từ SmartCard
        String cardId = mainFrame.getCardService().getCardId();
        String info = mainFrame.getCardService().getInfo();
        
        String name = "Khách hàng";
        String phone = "";
        
        if (info != null && !info.isEmpty()) {
            String[] parts = info.split("\\|", -1);
            if (parts.length >= 1 && !parts[0].isEmpty()) name = parts[0];
            if (parts.length >= 2 && !parts[1].isEmpty()) phone = parts[1];
        }

        // Kiểm tra có phải đổi PIN lần đầu không
        if (mainFrame.getCardService().isMustChangePIN()) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn cần đổi PIN lần đầu tiên.\nĐổi PIN ngay bây giờ?",
                "Đổi PIN bắt buộc",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                mainFrame.setPendingLoginForChangePin(cardId, name, phone);
                mainFrame.showScreen(MainFrame.SCREEN_CHANGE_PIN);
                txtPin.setText("");
                return;
            }
        }

        // Đăng nhập thành công
        mainFrame.onLoginSuccess(cardId, name, phone);
        txtPin.setText("");
        updateCardStatus();

    } else {
        int tries = mainFrame.getCardService().getPinTriesRemaining();
        updateCardStatus();
        
        if (tries <= 0) {
            showError("Thẻ đã bị khóa! Vui lòng dùng 'Quên PIN' để mở khóa.");
            txtPin.setEnabled(false);
        } else {
            showError("Lỗi xác thực! Còn " + tries + " lần thử.");
        }
        
        // Rút thẻ ra nếu verify fail
        mainFrame.getCardService().logout();
    }
}

    private void showError(String msg) {
        lblError.setText(msg);
        txtPin.setText("");
        txtPin.requestFocus();
    }

    public void onShow() {
        txtPin.setText("");
        txtPin.setEnabled(true);
        lblError.setText(" ");
        updateCardStatus();
    }
}