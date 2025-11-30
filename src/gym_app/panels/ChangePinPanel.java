package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình đổi PIN
 */
public class ChangePinPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JPasswordField txtCurrentPin;
    private JPasswordField txtNewPin;
    private JPasswordField txtConfirmPin;
    private JLabel lblStrength;
    private JProgressBar strengthBar;
    
    // Flag: đang trong flow bắt buộc đổi PIN từ Login
    private boolean isFirstTimeChange = false;
    
    // Lưu thông tin user từ Login truyền sang
    private String pendingCardId;
    private String pendingName;
    private String pendingPhone;

    public ChangePinPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));

        // Main Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 45));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JLabel title = new JLabel("🔐 ĐỔI MÃ PIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(155, 89, 182));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Thay đổi mã PIN để bảo vệ tài khoản của bạn");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form
        JPanel formPanel = createFormPanel();

        // Tips
        JPanel tipsPanel = createTipsPanel();

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GymButton btnChange = GymButton.success("✓ ĐỔI PIN");
        btnChange.setPreferredSize(new Dimension(200, 50));
        btnChange.addActionListener(e -> doChangePin());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> {
            clearForm();
            if (isFirstTimeChange) {
                mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
            } else {
                mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
            }
        });

        buttonPanel.add(btnChange);
        buttonPanel.add(btnBack);

        // Layout
        content.add(Box.createVerticalStrut(50));
        content.add(title);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(formPanel);
        content.add(Box.createVerticalStrut(25));
        content.add(tipsPanel);
        content.add(Box.createVerticalStrut(30));
        content.add(buttonPanel);

        // Wrapper để center
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(30, 30, 45));
        wrapper.add(content);

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            new EmptyBorder(30, 35, 30, 35)
        ));
        panel.setMaximumSize(new Dimension(400, 350));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Current PIN
        JLabel lblCurrent = new JLabel("🔑 PIN hiện tại:");
        lblCurrent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrent.setForeground(Color.WHITE);
        lblCurrent.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtCurrentPin = createPinField();

        // New PIN
        JLabel lblNew = new JLabel("🔐 PIN mới (6 số):");
        lblNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNew.setForeground(Color.WHITE);
        lblNew.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtNewPin = createPinField();
        txtNewPin.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });

        // Strength indicator
        JPanel strengthPanel = new JPanel(new BorderLayout(10, 0));
        strengthPanel.setBackground(new Color(40, 40, 55));
        strengthPanel.setMaximumSize(new Dimension(300, 25));
        strengthPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStrength = new JLabel("Độ mạnh: ---");
        lblStrength.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStrength.setForeground(Color.GRAY);

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(120, 10));
        strengthBar.setStringPainted(false);

        strengthPanel.add(lblStrength, BorderLayout.WEST);
        strengthPanel.add(strengthBar, BorderLayout.EAST);

        // Confirm PIN
        JLabel lblConfirm = new JLabel("🔐 Xác nhận PIN mới:");
        lblConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblConfirm.setForeground(Color.WHITE);
        lblConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtConfirmPin = createPinField();

        panel.add(lblCurrent);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtCurrentPin);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblNew);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtNewPin);
        panel.add(Box.createVerticalStrut(8));
        panel.add(strengthPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblConfirm);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtConfirmPin);

        return panel;
    }

    private JPasswordField createPinField() {
        JPasswordField pf = new JPasswordField(6);
        pf.setFont(new Font("Consolas", Font.BOLD, 24));
        pf.setHorizontalAlignment(JTextField.CENTER);
        pf.setBackground(new Color(60, 60, 75));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(12, 15, 12, 15)
        ));
        pf.setMaximumSize(new Dimension(200, 50));
        pf.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pf;
    }

    private JPanel createTipsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(400, 130));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("💡 MẸO TẠO PIN AN TOÀN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(241, 196, 15));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String tips = "<html><center>" +
            "• Không dùng ngày sinh, số điện thoại<br>" +
            "• Tránh các dãy số đơn giản: 123456, 000000<br>" +
            "• Không dùng PIN giống nhau cho nhiều tài khoản" +
            "</center></html>";

        JLabel lblTips = new JLabel(tips);
        lblTips.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTips.setForeground(Color.WHITE);
        lblTips.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTips);

        return panel;
    }

    private void updateStrength() {
        String pin = new String(txtNewPin.getPassword());
        int strength = calculateStrength(pin);

        strengthBar.setValue(strength);

        if (strength < 30) {
            lblStrength.setText("Độ mạnh: Yếu ❌");
            lblStrength.setForeground(new Color(231, 76, 60));
            strengthBar.setForeground(new Color(231, 76, 60));
        } else if (strength < 60) {
            lblStrength.setText("Độ mạnh: Trung bình ⚠️");
            lblStrength.setForeground(new Color(241, 196, 15));
            strengthBar.setForeground(new Color(241, 196, 15));
        } else if (strength < 80) {
            lblStrength.setText("Độ mạnh: Khá tốt 👍");
            lblStrength.setForeground(new Color(52, 152, 219));
            strengthBar.setForeground(new Color(52, 152, 219));
        } else {
            lblStrength.setText("Độ mạnh: Mạnh ✅");
            lblStrength.setForeground(new Color(46, 204, 113));
            strengthBar.setForeground(new Color(46, 204, 113));
        }
    }

    private int calculateStrength(String pin) {
        if (pin.length() < 6) return 10;

        int score = 50;

        if (pin.equals("123456") || pin.equals("654321")) score -= 30;
        if (pin.equals("000000") || pin.equals("111111") || pin.equals("222222")) score -= 40;

        boolean allSame = true;
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i) != pin.charAt(0)) {
                allSame = false;
                break;
            }
        }
        if (allSame) score -= 30;

        java.util.Set<Character> unique = new java.util.HashSet<>();
        for (char c : pin.toCharArray()) unique.add(c);
        score += unique.size() * 8;

        return Math.max(0, Math.min(100, score));
    }

    private void doChangePin() {
        String currentPin = new String(txtCurrentPin.getPassword());
        String newPin = new String(txtNewPin.getPassword());
        String confirmPin = new String(txtConfirmPin.getPassword());

        // Validate
        if (currentPin.length() != 6) {
            showError("PIN hiện tại phải đúng 6 số!");
            txtCurrentPin.requestFocus();
            return;
        }

        if (!newPin.matches("\\d{6}")) {
            showError("PIN mới phải đúng 6 chữ số!");
            txtNewPin.requestFocus();
            return;
        }

        if (!newPin.equals(confirmPin)) {
            showError("PIN xác nhận không khớp!");
            txtConfirmPin.requestFocus();
            return;
        }

        if (currentPin.equals(newPin)) {
            showError("PIN mới phải khác PIN cũ!");
            txtNewPin.requestFocus();
            return;
        }

        // Verify current PIN
        if (!mainFrame.getCardService().verifyPIN(currentPin)) {
            showError("PIN hiện tại không đúng!");
            txtCurrentPin.setText("");
            txtCurrentPin.requestFocus();
            return;
        }

        // Change PIN
        if (mainFrame.getCardService().changePIN(newPin)) {
            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>✅ ĐỔI PIN THÀNH CÔNG!</h2>" +
                "<p>PIN mới của bạn đã được cập nhật.</p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            
            // *** QUAN TRỌNG: Nếu đang trong flow đổi PIN lần đầu ***
            if (isFirstTimeChange) {
                System.out.println("[DEBUG] First time change PIN - completing login...");
                System.out.println("[DEBUG] Pending info: " + pendingCardId + ", " + pendingName + ", " + pendingPhone);
                
                // GỌI onLoginSuccess VỚI THÔNG TIN ĐÃ LƯU
                mainFrame.onLoginSuccess(pendingCardId, pendingName, pendingPhone);
                
                // Reset flag
                isFirstTimeChange = false;
                pendingCardId = null;
                pendingName = null;
                pendingPhone = null;
            } else {
                // Đổi PIN bình thường từ Dashboard
                mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
            }

        } else {
            showError("Đổi PIN thất bại! Vui lòng thử lại.");
        }
    }

    private void clearForm() {
        txtCurrentPin.setText("");
        txtNewPin.setText("");
        txtConfirmPin.setText("");
        strengthBar.setValue(0);
        lblStrength.setText("Độ mạnh: ---");
        lblStrength.setForeground(Color.GRAY);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Được gọi từ LoginPanel khi cần đổi PIN lần đầu
     * Lưu lại thông tin user để sau khi đổi PIN xong sẽ gọi onLoginSuccess
     */
    public void setPendingLogin(String cardId, String name, String phone) {
        this.isFirstTimeChange = true;
        this.pendingCardId = cardId;
        this.pendingName = name;
        this.pendingPhone = phone;
        
        System.out.println("[DEBUG] Set pending login: " + cardId + ", " + name + ", " + phone);
    }
    
    /**
     * Reset về trạng thái bình thường (đổi PIN từ Dashboard)
     */
    public void setNormalMode() {
        this.isFirstTimeChange = false;
        this.pendingCardId = null;
        this.pendingName = null;
        this.pendingPhone = null;
    }
    
    public void onShow() {
        clearForm();
        txtCurrentPin.requestFocus();
    }
}