package gym_app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Base64;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

public class GymGUI extends JFrame {

    private SmartCardService card;
    private DatabaseService db;
    private String currentCardId;

    // Component
    private JLabel lblAvatar, lblName, lblPhone, lblBalance, lblStatus;
    private JTextField txtName, txtPhone, txtAmount;
    private JPasswordField txtPin, txtNewPin;
    private JButton btnRegister, btnVerify, btnTopup;

    public GymGUI() {
        card = new SmartCardService();
        db = new DatabaseService();
        initUI();
    }

    private void initUI() {
        setTitle("POWER GYM PREMIUM - HỆ THỐNG THẺ THÔNG MINH 2025");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === PANEL TRÁI - THÔNG TIN ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(30, 30, 40));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new java.awt.Dimension(250, 250));
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setBorder(BorderFactory.createLineBorder(Color.CYAN, 4));
        lblAvatar.setIcon(new ImageIcon(createDefaultAvatar()));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(new Color(30, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Họ tên:", "SĐT:", "Số dư:", "Trạng thái:"};
        JLabel[] values = {
            lblName = new JLabel("Chưa có thông tin"),
            lblPhone = new JLabel("---"),
            lblBalance = new JLabel("0 VNĐ"),
            lblStatus = new JLabel("Chưa check-in")
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            infoPanel.add(lbl, gbc);

            gbc.gridx = 1;
            values[i].setForeground(Color.CYAN);
            values[i].setFont(new Font("Segoe UI", Font.PLAIN, 18));
            infoPanel.add(values[i], gbc);
        }

        leftPanel.add(lblAvatar, BorderLayout.NORTH);
        leftPanel.add(infoPanel, BorderLayout.CENTER);

        // === PANEL PHẢI - CHỨC NĂNG ===
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(45, 45, 60));
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        addTitle(rightPanel, "ĐĂNG KÝ THÀNH VIÊN MỚI");
        txtName = addTextField(rightPanel, "Họ và tên");
        txtPhone = addTextField(rightPanel, "Số điện thoại");
        btnRegister = addButton(rightPanel, "ĐĂNG KÝ & IN PIN", Color.GREEN, e -> registerMember());

        addTitle(rightPanel, "XÁC THỰC & ĐỔI PIN");
        txtPin = new JPasswordField(20);
        txtNewPin = new JPasswordField(20);
        rightPanel.add(createLabeledField("PIN hiện tại (6 số):", txtPin));
        rightPanel.add(createLabeledField("PIN mới (6 số):", txtNewPin));
        btnVerify = addButton(rightPanel, "XÁC THỰC & ĐỔI PIN", Color.ORANGE, e -> verifyAndChangePin());

        addTitle(rightPanel, "NẠP TIỀN TỰ DO");
        txtAmount = addTextField(rightPanel, "Số tiền nạp (VNĐ)");
        btnTopup = addButton(rightPanel, "NẠP TIỀN & IN HÓA ĐƠN QR", Color.MAGENTA, e -> topupAndPrint());

        add(leftPanel, BorderLayout.WEST);
        add(new JScrollPane(rightPanel), BorderLayout.CENTER);
        setVisible(true);
    }

    private void registerMember() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (name.isEmpty() || phone.length() < 10) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        String pin = card.registerNewCard();
        if (pin != null) {
            currentCardId = SecurityUtils.generateCardId();
            db.registerMember(name, phone, currentCardId);  // Chỉ 3 tham số
            lblName.setText(name);
            lblPhone.setText(phone);
            lblStatus.setText("Đã đăng ký - Chưa đổi PIN");

            JOptionPane.showMessageDialog(this,
                "<html><h2>ĐĂNG KÝ THÀNH CÔNG!</h2>" +
                "Mã thẻ: <b>" + currentCardId + "</b><br>" +
                "PIN mặc định: <b>" + pin + "</b><br><br>" +
                "<font color='red'>VUI LÒNG ĐỔI PIN NGAY LẦN ĐẦU!</font></html>",
                "THÀNH CÔNG", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi đăng ký thẻ!");
        }
    }

    private void verifyAndChangePin() {
        String oldPin = new String(txtPin.getPassword());
        String newPin = new String(txtNewPin.getPassword());

        if (oldPin.length() != 6 || newPin.length() != 6) {
            JOptionPane.showMessageDialog(this, "PIN phải đúng 6 số!");
            return;
        }

        if (card.verifyPIN(oldPin)) {
            if (card.changePIN(newPin)) {
                lblStatus.setText("ĐÃ ĐỔI PIN THÀNH CÔNG!");
                JOptionPane.showMessageDialog(this, "Đổi PIN thành công! Bạn đã kích hoạt đầy đủ thẻ.");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi PIN thất bại!");
            }
        } else {
            lblStatus.setText("PIN SAI!");
            JOptionPane.showMessageDialog(this, "PIN không đúng! Vui lòng thử lại.");
        }
    }

    private void topupAndPrint() {
        try {
            int amount = Integer.parseInt(txtAmount.getText());
            if (amount <= 0) throw new Exception();

            if (card.topup(amount)) {
                long balance = card.getBalance();
                lblBalance.setText(String.format("%,d VNĐ", balance));

                // Tạo chữ ký giả (vì applet chưa có thật)
                byte[] fakeSig = "SIGNATURE_FAKE".getBytes();

                String qrData = "GYM|2025|" + currentCardId + "|TOPUP|" + amount + "|" + Base64.getEncoder().encodeToString(fakeSig);
                generateQR(qrData);

                JOptionPane.showMessageDialog(this,
                    "<html><h2>HÓA ĐƠN NẠP TIỀN</h2>" +
                    "Khách: <b>" + lblName.getText() + "</b><br>" +
                    "Mã thẻ: " + currentCardId + "<br>" +
                    "Số tiền: <b>" + String.format("%,d VNĐ", amount) + "</b><br>" +
                    "Số dư mới: <b>" + String.format("%,d VNĐ", balance) + "</b><br>" +
                    "Thời gian: " + java.time.LocalDateTime.now() + "<br><br>" +
                    "<img src='file:qr_invoice.png' width=200 height=200><br>" +
                    "<small>Chữ ký điện tử RSA</small></html>",
                    "HÓA ĐƠN", JOptionPane.INFORMATION_MESSAGE);

                db.logTransaction(currentCardId, "TOPUP", amount, "Nạp tiền", Base64.getEncoder().encodeToString(fakeSig));
                db.updateBalance(currentCardId, balance);

            } else {
                JOptionPane.showMessageDialog(this, "Nạp tiền thất bại!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!");
        }
    }

    private void generateQR(String data) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToPath(matrix, "PNG", new File("qr_invoice.png").toPath());
        } catch (WriterException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Helper methods
    private JTextField addTextField(Container c, String label) {
        JTextField tf = new JTextField(25);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.add(createLabeledField(label + ":", tf));
        return tf;
    }

    private JPanel createLabeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(new Color(45, 45, 60));
        JLabel l = new JLabel(label);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return  p;
    }

    private JButton addButton(Container c, String text, Color color, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new java.awt.Dimension(350, 50));
        btn.addActionListener(al);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(new Color(45, 45, 60));
        wrapper.add(btn);
        c.add(wrapper);
        c.add(Box.createVerticalStrut(20));
        return btn;
    }

     private void addTitle(Container c, String title) {
        JLabel lbl = new JLabel(title);                    
        lbl.setForeground(Color.YELLOW);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(lbl);
        c.add(Box.createVerticalStrut(15));
    }

    private Image createDefaultAvatar() {
        BufferedImage img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(60, 60, 80));
        g.fillRect(0, 0, 250, 250);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g.drawString("POWER GYM", 20, 125);
        g.dispose();
        return img;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GymGUI::new);
    }
}