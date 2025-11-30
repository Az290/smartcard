package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình mở khóa thẻ / quên PIN
 */
public class UnblockPinPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JTextField txtPhone;
    private JLabel lblNewPin;
    private JPanel resultPanel;

    public UnblockPinPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 45));

        // Container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(40, 40, 55));
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            new EmptyBorder(40, 50, 40, 50)
        ));
        container.setPreferredSize(new Dimension(500, 550));

        // Icon
        JLabel icon = new JLabel("🔓");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel title = new JLabel("QUÊN MÃ PIN?");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(231, 76, 60));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><center>Nhập số điện thoại đã đăng ký<br>để lấy lại mã PIN mới</center></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Phone input
        JLabel lblPhone = new JLabel("📱 Số điện thoại đã đăng ký:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPhone.setForeground(Color.WHITE);
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPhone = new JTextField(15);
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtPhone.setHorizontalAlignment(JTextField.CENTER);
        txtPhone.setBackground(new Color(60, 60, 75));
        txtPhone.setForeground(Color.WHITE);
        txtPhone.setCaretColor(Color.WHITE);
        txtPhone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(12, 15, 12, 15)
        ));
        txtPhone.setMaximumSize(new Dimension(280, 50));

        // Result panel (hidden initially)
        resultPanel = createResultPanel();
        resultPanel.setVisible(false);

        // Buttons
        GymButton btnUnblock = GymButton.danger("🔑 LẤY LẠI MÃ PIN");
        btnUnblock.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUnblock.setMaximumSize(new Dimension(280, 50));
        btnUnblock.addActionListener(e -> doUnblock());

        GymButton btnBack = new GymButton("← Quay lại đăng nhập", new Color(100, 100, 120));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(280, 45));
        btnBack.addActionListener(e -> {
            resetForm();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });

        // Warning
        JLabel warning = new JLabel("<html><center style='color:#e74c3c'>" +
            "⚠️ Lưu ý: Bạn cần mang thẻ đến quầy<br>" +
            "nếu không nhớ số điện thoại đã đăng ký!" +
            "</center></html>");
        warning.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warning.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout
        container.add(icon);
        container.add(Box.createVerticalStrut(15));
        container.add(title);
        container.add(Box.createVerticalStrut(10));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(30));
        container.add(lblPhone);
        container.add(Box.createVerticalStrut(10));
        container.add(txtPhone);
        container.add(Box.createVerticalStrut(25));
        container.add(resultPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(btnUnblock);
        container.add(Box.createVerticalStrut(15));
        container.add(btnBack);
        container.add(Box.createVerticalStrut(20));
        container.add(warning);

        add(container);
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 70, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(350, 120));

        JLabel successLabel = new JLabel("✅ MỞ KHÓA THÀNH CÔNG!");
        successLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        successLabel.setForeground(new Color(46, 204, 113));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pinLabel = new JLabel("PIN mới của bạn:");
        pinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pinLabel.setForeground(Color.WHITE);
        pinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblNewPin = new JLabel("------");
        lblNewPin.setFont(new Font("Consolas", Font.BOLD, 36));
        lblNewPin.setForeground(Color.WHITE);
        lblNewPin.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(successLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(pinLabel);
        panel.add(lblNewPin);

        return panel;
    }

  private void doUnblock() {
    String phone = txtPhone.getText().trim();

    if (!phone.matches("\\d{10,11}")) {
        showError("Số điện thoại phải có 10-11 chữ số!");
        txtPhone.requestFocus();
        return;
    }

    // *** TÌM VÀ LOAD THẺ THEO SĐT ***
    if (!mainFrame.getCardService().findAndLoadCardByPhone(phone)) {
        showError("Không tìm thấy thẻ với SĐT này!\nVui lòng kiểm tra lại hoặc liên hệ quầy.");
        return;
    }

    // Confirm
    int confirm = JOptionPane.showConfirmDialog(this,
        "<html><center>" +
        "<p>Tìm thấy thẻ: <b>" + mainFrame.getCardService().getCardId() + "</b></p>" +
        "<p>Bạn có chắc muốn lấy lại mã PIN?</p>" +
        "</center></html>",
        "Xác nhận",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        mainFrame.getCardService().logout();
        return;
    }

    // Sinh PIN mới
    String newPin = mainFrame.getCardService().unblockAndGenerateNewPIN(phone);

    if (newPin != null) {
        lblNewPin.setText(formatPin(newPin));
        resultPanel.setVisible(true);

        JOptionPane.showMessageDialog(this,
            "<html><center>" +
            "<h2>✅ MỞ KHÓA THÀNH CÔNG!</h2>" +
            "<p>PIN mới: <b style='font-size:24px; color:green'>" + newPin + "</b></p>" +
            "<p style='color:orange'>⚠️ Vui lòng đổi PIN ngay sau khi đăng nhập!</p>" +
            "</center></html>",
            "Thành công",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Rút thẻ ra
        mainFrame.getCardService().logout();

    } else {
        showError("Không thể tạo PIN mới!");
        mainFrame.getCardService().logout();
    }
}

    private String formatPin(String pin) {
        if (pin.length() == 6) {
            return pin.substring(0, 3) + " " + pin.substring(3);
        }
        return pin;
    }

    private void resetForm() {
        txtPhone.setText("");
        lblNewPin.setText("------");
        resultPanel.setVisible(false);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}