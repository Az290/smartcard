package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình mở khóa thẻ
 */
public class UnblockPinPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JTextField txtPhone;
    private JLabel lblResult;
    private JPanel resultPanel;

    public UnblockPinPanel(MainFrame mainFrame) {
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
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            new EmptyBorder(40, 50, 40, 50)
        ));
        container.setPreferredSize(new Dimension(500, 500));

        // Icon
        JLabel icon = new JLabel("🔓");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel title = new JLabel("MỞ KHÓA THẺ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(231, 76, 60));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><center>Nhập số điện thoại đã đăng ký<br>để mở khóa và lấy lại PIN mặc định</center></html>");
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

        // Result panel
        resultPanel = createResultPanel();
        resultPanel.setVisible(false);

        // Buttons
        GymButton btnUnblock = GymButton.danger("🔑 MỞ KHÓA THẺ");
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
            "⚠️ Lưu ý: Bạn cần nhớ số điện thoại<br>" +
            "đã đăng ký khi thiết lập thông tin!" +
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

        lblResult = new JLabel("123 456");
        lblResult.setFont(new Font("Consolas", Font.BOLD, 36));
        lblResult.setForeground(Color.WHITE);
        lblResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(successLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(pinLabel);
        panel.add(lblResult);

        return panel;
    }

    private void doUnblock() {
        String phone = txtPhone.getText().trim();

        if (!phone.matches("\\d{10,11}")) {
            showError("Số điện thoại phải có 10-11 chữ số!");
            txtPhone.requestFocus();
            return;
        }

        // Kiểm tra có phải thẻ bị khóa không
        if (!mainFrame.getCardService().isCardBlocked()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Thẻ chưa bị khóa.\nBạn có muốn reset PIN về mặc định?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Mở khóa
        if (mainFrame.getCardService().unblockCard(phone)) {
            lblResult.setText("123 456");
            resultPanel.setVisible(true);

            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>✅ MỞ KHÓA THÀNH CÔNG!</h2>" +
                "<p>PIN mới: <b style='font-size:24px; color:green'>123456</b></p>" +
                "<p style='color:orange'>⚠️ Bạn sẽ phải đổi PIN khi đăng nhập!</p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

        } else {
            showError("Mở khóa thất bại!\n\nSố điện thoại không khớp với thông tin đã đăng ký.");
        }
    }

    private void resetForm() {
        txtPhone.setText("");
        lblResult.setText("------");
        resultPanel.setVisible(false);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}