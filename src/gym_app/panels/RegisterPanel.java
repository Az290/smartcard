package gym_app.panels;

import gym_app.MainFrame;
import gym_app.SecurityUtils;
import gym_app.components.GymButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình đăng ký thẻ mới
 */
public class RegisterPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtBirthDate;
    private JLabel lblGeneratedPin;
    private JPanel pinPanel;
    private GymButton btnRegister;
    private GymButton btnContinue;

    public RegisterPanel(MainFrame mainFrame) {
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
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(30, 50, 30, 50)
        ));
        container.setPreferredSize(new Dimension(500, 650));

        // Header
        JLabel title = new JLabel("📝 ĐĂNG KÝ THẺ THÀNH VIÊN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(46, 204, 113));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Điền thông tin để nhận thẻ và mã PIN");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields
        txtName = createTextField();
        txtPhone = createTextField();
        txtBirthDate = createTextField();

        // PIN display panel
        pinPanel = createPinPanel();
        pinPanel.setVisible(false);

        // Buttons
        btnRegister = GymButton.success("✓ ĐĂNG KÝ NGAY");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(300, 45));
        btnRegister.addActionListener(e -> doRegister());

        btnContinue = GymButton.primary("→ TIẾP TỤC ĐĂNG NHẬP");
        btnContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContinue.setMaximumSize(new Dimension(300, 45));
        btnContinue.setVisible(false);
        btnContinue.addActionListener(e -> {
            clearForm();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(300, 40));
        btnBack.addActionListener(e -> {
            clearForm();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });

        // Layout
        container.add(title);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(30));
        container.add(createFieldPanel("👤 Họ và tên:", txtName));
        container.add(Box.createVerticalStrut(15));
        container.add(createFieldPanel("📱 Số điện thoại:", txtPhone));
        container.add(Box.createVerticalStrut(15));
        container.add(createFieldPanel("🎂 Ngày sinh:", txtBirthDate));
        container.add(Box.createVerticalStrut(25));
        container.add(pinPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(btnRegister);
        container.add(Box.createVerticalStrut(10));
        container.add(btnContinue);
        container.add(Box.createVerticalStrut(15));
        container.add(btnBack);

        add(container);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setMaximumSize(new Dimension(350, 40));
        tf.setBackground(new Color(60, 60, 75));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(40, 40, 55));
        p.setMaximumSize(new Dimension(350, 70));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        p.add(field);

        return p;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 65));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(350, 100));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPinTitle = new JLabel("🔑 MÃ PIN CỦA BẠN");
        lblPinTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPinTitle.setForeground(new Color(241, 196, 15));
        lblPinTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblGeneratedPin = new JLabel("------");
        lblGeneratedPin.setFont(new Font("Consolas", Font.BOLD, 36));
        lblGeneratedPin.setForeground(Color.WHITE);
        lblGeneratedPin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPinNote = new JLabel("⚠️ Hãy ghi nhớ hoặc chụp lại!");
        lblPinNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPinNote.setForeground(new Color(231, 76, 60));
        lblPinNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblPinTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblGeneratedPin);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblPinNote);

        return panel;
    }

  private void doRegister() {
    String name = txtName.getText().trim();
    String phone = txtPhone.getText().trim();
    String birthDate = txtBirthDate.getText().trim();

    // Validate
    if (name.isEmpty() || name.length() < 2) {
        showError("Vui lòng nhập họ tên hợp lệ!");
        txtName.requestFocus();
        return;
    }
    if (!phone.matches("\\d{10,11}")) {
        showError("Số điện thoại phải có 10-11 chữ số!");
        txtPhone.requestFocus();
        return;
    }

    // *** KIỂM TRA SĐT ĐÃ ĐĂNG KÝ CHƯA ***
    if (mainFrame.getCardService().isPhoneRegistered(phone)) {
        showError("Số điện thoại này đã được đăng ký!\nVui lòng dùng SĐT khác hoặc đăng nhập.");
        txtPhone.requestFocus();
        return;
    }

    // Đăng ký thẻ mới
    String pin = mainFrame.getCardService().registerNewCard();

    if (pin != null) {
        // Lưu thông tin: name|phone|email|birthDate|address
        String info = String.join("|", name, phone, "", birthDate, "");
        mainFrame.getCardService().updateInfo(info);
        mainFrame.getCardService().setRecoveryPhone(phone);

        // Lưu DB
        String cardId = mainFrame.getCardService().getCardId();
        mainFrame.getDbService().registerMember(name, phone, cardId);

        // Hiển thị PIN
        lblGeneratedPin.setText(formatPin(pin));
        pinPanel.setVisible(true);
        btnContinue.setVisible(true);
        btnRegister.setVisible(false);

        JOptionPane.showMessageDialog(this,
            "<html><center>" +
            "<h2>🎉 ĐĂNG KÝ THÀNH CÔNG!</h2>" +
            "<p>Mã thẻ: <b>" + cardId + "</b></p>" +
            "<p>PIN: <b style='font-size:24px; color:red'>" + pin + "</b></p>" +
            "<br><p>⚠️ Hãy đổi PIN ngay lần đăng nhập đầu!</p>" +
            "</center></html>",
            "Thành công",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Rút thẻ ra để người dùng phải đăng nhập lại
        mainFrame.getCardService().logout();

    } else {
        showError("Đăng ký thất bại!");
    }
}

    private String formatPin(String pin) {
        if (pin.length() == 6) {
            return pin.substring(0, 3) + " " + pin.substring(3);
        }
        return pin;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void clearForm() {
        txtName.setText("");
        txtPhone.setText("");
        txtBirthDate.setText("");
        lblGeneratedPin.setText("------");
        pinPanel.setVisible(false);
        btnRegister.setVisible(true);
        btnContinue.setVisible(false);
    }

    public void onShow() {
        clearForm();
    }
}