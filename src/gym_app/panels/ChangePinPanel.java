package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình đổi PIN
 *  Fix: Hiện nút "Quay lại đăng nhập" sau khi đổi PIN thành công
 */
public class ChangePinPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JPasswordField txtCurrentPin;
    private JPasswordField txtNewPin;
    private JPasswordField txtConfirmPin;
    private JLabel lblStrength;
    private JProgressBar strengthBar;
    private JLabel lblTitle;
    private JLabel lblSubtitle;
    private JPanel buttonPanel; //  THÊM
    
    // Mode: first time hoặc normal
    private boolean isFirstTimeMode = false;
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

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 45));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        lblTitle = new JLabel(" ĐỔI MÃ PIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(155, 89, 182));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitle = new JLabel("Thay đổi mã PIN để bảo vệ tài khoản");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = createFormPanel();
        JPanel tipsPanel = createTipsPanel();

        //  SỬA: Bỏ "JPanel" ở đầu
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GymButton btnChange = GymButton.success(" ĐỔI PIN");
        btnChange.setPreferredSize(new Dimension(200, 50));
        btnChange.addActionListener(e -> doChangePin());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> {
            clearForm();
            if (isFirstTimeMode) {
                mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
            } else {
                mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
            }
        });

        buttonPanel.add(btnChange);
        buttonPanel.add(btnBack);

        content.add(Box.createVerticalStrut(50));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(5));
        content.add(lblSubtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(formPanel);
        content.add(Box.createVerticalStrut(25));
        content.add(tipsPanel);
        content.add(Box.createVerticalStrut(30));
        content.add(buttonPanel);

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

        JLabel lblCurrent = new JLabel(" PIN hiện tại:");
        lblCurrent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrent.setForeground(Color.WHITE);
        lblCurrent.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtCurrentPin = createPinField();

        JLabel lblNew = new JLabel(" PIN mới (6 số):");
        lblNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNew.setForeground(Color.WHITE);
        lblNew.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtNewPin = createPinField();
        txtNewPin.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });

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

        JLabel lblConfirm = new JLabel(" Xác nhận PIN mới:");
        lblConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblConfirm.setForeground(Color.WHITE);
        lblConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtConfirmPin = createPinField();

        panel.add(lblCurrent);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtCurrentPin);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblNew);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtNewPin);
        panel.add(Box.createVerticalStrut(5));
        panel.add(strengthPanel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblConfirm);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtConfirmPin);

        return panel;
    }

   private JPasswordField createPinField() {
        JPasswordField pf = new JPasswordField(6);
        pf.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Cỡ chữ 18 đồng bộ
        pf.setHorizontalAlignment(JTextField.CENTER);
        pf.setBackground(new Color(60, 60, 75));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(2, 5, 2, 5)                  // Padding tối giản
        ));
        pf.setMaximumSize(new Dimension(120, 32));          // Kích thước siêu gọn 120x32
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

        JLabel titleLabel = new JLabel(" MẸO TẠO PIN AN TOÀN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(241, 196, 15));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String tips = "<html><center>" +
            "• Không dùng ngày sinh, số điện thoại<br>" +
            "• Tránh: 123456, 000000, 111111<br>" +
            "• Không dùng PIN giống tài khoản khác" +
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
            lblStrength.setText("Yếu ");
            lblStrength.setForeground(new Color(231, 76, 60));
            strengthBar.setForeground(new Color(231, 76, 60));
        } else if (strength < 60) {
            lblStrength.setText("Trung bình ️");
            lblStrength.setForeground(new Color(241, 196, 15));
            strengthBar.setForeground(new Color(241, 196, 15));
        } else if (strength < 80) {
            lblStrength.setText("Khá tốt ");
            lblStrength.setForeground(new Color(52, 152, 219));
            strengthBar.setForeground(new Color(52, 152, 219));
        } else {
            lblStrength.setText("Mạnh ");
            lblStrength.setForeground(new Color(46, 204, 113));
            strengthBar.setForeground(new Color(46, 204, 113));
        }
    }

    private int calculateStrength(String pin) {
        if (pin.length() < 6) return 10;

        int score = 50;

        if (pin.equals("123456") || pin.equals("654321")) score -= 30;
        if (pin.equals("000000") || pin.equals("111111") || pin.equals("222222")) score -= 40;
        if (pin.equals("888888") || pin.equals("666666")) score -= 30;

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

        if (!currentPin.matches("\\d{6}")) {
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

        if (newPin.equals("123456") || newPin.equals("000000")) {
            showError("PIN này quá yếu! Vui lòng chọn PIN khác.");
            txtNewPin.requestFocus();
            return;
        }

        if (mainFrame.getCardService().changePIN(currentPin, newPin)) {
            mainFrame.getCardService().setFirstLoginComplete();
            
            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2> ĐỔI PIN THÀNH CÔNG!</h2>" +
                "<p>PIN mới đã được cập nhật.</p>" +
                "<p><b>Hãy ghi nhớ PIN mới!</b></p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            
            if (isFirstTimeMode) {
                mainFrame.onLoginSuccess(pendingCardId, pendingName, pendingPhone);
                setNormalMode();
            } else {
                //  THÊM: Hiện nút quay lại đăng nhập
                showBackToLoginButton();
            }

        } else {
            showError("Đổi PIN thất bại!\nPIN hiện tại không đúng.");
            txtCurrentPin.setText("");
            txtCurrentPin.requestFocus();
        }
    }

    /**
     *  THÊM: Hiện nút "Quay lại đăng nhập"
     */
    private void showBackToLoginButton() {
        buttonPanel.removeAll();
        
        GymButton btnLogin = GymButton.success(" Quay lại đăng nhập");
        btnLogin.setPreferredSize(new Dimension(250, 50));
        btnLogin.addActionListener(e -> {
            clearForm();
            mainFrame.logout();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });
        
        buttonPanel.add(btnLogin);
        buttonPanel.revalidate();
        buttonPanel.repaint();
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
    
    public void setFirstTimeMode(String cardId, String name, String phone) {
        this.isFirstTimeMode = true;
        this.pendingCardId = cardId;
        this.pendingName = name;
        this.pendingPhone = phone;
        
        lblTitle.setText(" ĐỔI PIN LẦN ĐẦU");
        lblSubtitle.setText("<html><center>Bắt buộc đổi PIN để bảo mật tài khoản.<br>PIN mặc định: <b>123456</b></center></html>");
    }
    
    public void setNormalMode() {
        this.isFirstTimeMode = false;
        this.pendingCardId = null;
        this.pendingName = null;
        this.pendingPhone = null;
        
        lblTitle.setText(" ĐỔI MÃ PIN");
        lblSubtitle.setText("Thay đổi mã PIN để bảo vệ tài khoản");
    }
    
    public void onShow() {
        clearForm();
        txtCurrentPin.requestFocus();
        
        //  THÊM: Reset button panel
        buttonPanel.removeAll();
        
        GymButton btnChange = GymButton.success(" ĐỔI PIN");
        btnChange.setPreferredSize(new Dimension(200, 50));
        btnChange.addActionListener(e -> doChangePin());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> {
            clearForm();
            if (isFirstTimeMode) {
                mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
            } else {
                mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
            }
        });

        buttonPanel.add(btnChange);
        buttonPanel.add(btnBack);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
}