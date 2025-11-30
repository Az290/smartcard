package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.SecurityUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Màn hình sửa thông tin cá nhân
 */
public class ProfileEditPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtBirthDate;
    private JTextField txtAddress;
    private JLabel lblAvatar;
    private byte[] avatarData;

    public ProfileEditPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));

        // Side Menu
        add(new SideMenu(mainFrame), BorderLayout.WEST);

        // Main Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 45));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JLabel title = new JLabel("👤 CHỈNH SỬA THÔNG TIN CÁ NHÂN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 152, 219));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar section
        JPanel avatarPanel = createAvatarPanel();

        // Form section
        JPanel formPanel = createFormPanel();

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GymButton btnSave = GymButton.success("💾 LƯU THAY ĐỔI");
        btnSave.setPreferredSize(new Dimension(200, 50));
        btnSave.addActionListener(e -> saveProfile());

        GymButton btnReset = GymButton.warning("🔄 KHÔI PHỤC");
        btnReset.setPreferredSize(new Dimension(150, 50));
        btnReset.addActionListener(e -> loadCurrentData());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnBack);

        // Layout
        content.add(title);
        content.add(Box.createVerticalStrut(25));
        content.add(avatarPanel);
        content.add(Box.createVerticalStrut(25));
        content.add(formPanel);
        content.add(Box.createVerticalStrut(30));
        content.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(300, 280));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("📷 ẢNH ĐẠI DIỆN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 200, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Avatar display
        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(150, 150));
        lblAvatar.setMaximumSize(new Dimension(150, 150));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120), 2));
        setDefaultAvatar();

        // Upload button
        GymButton btnUpload = new GymButton("📤 Tải ảnh lên", new Color(100, 100, 130));
        btnUpload.setMaximumSize(new Dimension(200, 35));
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUpload.addActionListener(e -> uploadAvatar());

        JLabel lblNote = new JLabel("<html><center>Ảnh tối đa 1KB<br>Định dạng: JPG, PNG</center></html>");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(Color.GRAY);
        lblNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblAvatar);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnUpload);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblNote);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(25, 30, 25, 30)
        ));
        panel.setMaximumSize(new Dimension(600, 400));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formTitle = new JLabel("📝 THÔNG TIN CÁ NHÂN");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(0, 200, 180));

        // Form fields
        txtName = createTextField("Họ và tên");
        txtPhone = createTextField("Số điện thoại");
        txtEmail = createTextField("Email");
        txtBirthDate = createTextField("Ngày sinh (DD/MM/YYYY)");
        txtAddress = createTextField("Địa chỉ");

        panel.add(formTitle);
        panel.add(Box.createVerticalStrut(20));
        panel.add(createFieldRow("👤 Họ và tên *", txtName));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("📱 Số điện thoại *", txtPhone));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("📧 Email", txtEmail));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("🎂 Ngày sinh", txtBirthDate));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("🏠 Địa chỉ", txtAddress));

        return panel;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(60, 60, 75));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setMaximumSize(new Dimension(350, 40));
        return tf;
    }

    private JPanel createFieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(new Color(40, 40, 55));
        row.setMaximumSize(new Dimension(550, 45));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setPreferredSize(new Dimension(150, 30));

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    private void setDefaultAvatar() {
        BufferedImage img = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, new Color(60, 60, 80), 
                                              150, 150, new Color(40, 40, 60));
        g.setPaint(gp);
        g.fillRect(0, 0, 150, 150);

        g.setColor(new Color(100, 100, 130));
        g.fillOval(50, 25, 50, 50);
        g.fillRoundRect(35, 85, 80, 55, 20, 20);

        g.dispose();
        lblAvatar.setIcon(new ImageIcon(img));
    }

    private void uploadAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                
                // Read and resize image
                BufferedImage originalImg = ImageIO.read(file);
                BufferedImage resizedImg = resizeImage(originalImg, 150, 150);
                
                // Convert to bytes
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(resizedImg, "jpg", baos);
                avatarData = baos.toByteArray();
                
                // Check size
                if (avatarData.length > 1024) {
                    // Compress more
                    avatarData = compressImage(resizedImg, 0.5f);
                    
                    if (avatarData.length > 1024) {
                        JOptionPane.showMessageDialog(this,
                            "Ảnh quá lớn sau khi nén! Vui lòng chọn ảnh nhỏ hơn.",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // Display
                lblAvatar.setIcon(new ImageIcon(resizedImg));
                
                JOptionPane.showMessageDialog(this,
                    "✅ Đã chọn ảnh! Nhấn 'Lưu thay đổi' để cập nhật.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Lỗi tải ảnh: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private byte[] compressImage(BufferedImage img, float quality) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
        writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
        writer.dispose();
        
        return baos.toByteArray();
    }

    private void loadCurrentData() {
        // Load từ MainFrame
        txtName.setText(mainFrame.getCurrentName() != null ? mainFrame.getCurrentName() : "");
        txtPhone.setText(mainFrame.getCurrentPhone() != null ? mainFrame.getCurrentPhone() : "");
        
        // Load từ SmartCard
        String info = mainFrame.getCardService().getInfo();
        if (info != null) {
            // Parse info nếu là JSON hoặc format cụ thể
            // Ví dụ: "name|phone|email|birthdate|address"
            String[] parts = info.split("\\|");
            if (parts.length >= 1) txtName.setText(parts[0]);
            if (parts.length >= 2) txtPhone.setText(parts[1]);
            if (parts.length >= 3) txtEmail.setText(parts[2]);
            if (parts.length >= 4) txtBirthDate.setText(parts[3]);
            if (parts.length >= 5) txtAddress.setText(parts[4]);
        }

        // Load avatar
        byte[] avatar = mainFrame.getCardService().getAvatar();
        if (avatar != null && avatar.length > 0) {
            avatarData = avatar;
            lblAvatar.setIcon(new ImageIcon(avatar));
        } else {
            setDefaultAvatar();
        }
    }

    private void saveProfile() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String birthDate = txtBirthDate.getText().trim();
        String address = txtAddress.getText().trim();

        // Validate
        if (name.isEmpty()) {
            showError("Vui lòng nhập họ tên!");
            txtName.requestFocus();
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            showError("Số điện thoại phải có 10-11 chữ số!");
            txtPhone.requestFocus();
            return;
        }

        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Email không hợp lệ!");
            txtEmail.requestFocus();
            return;
        }

        // Save to SmartCard
        String info = String.join("|", name, phone, email, birthDate, address);
        
        if (mainFrame.getCardService().editInfo(info)) {
            // Save avatar if changed
            if (avatarData != null) {
                mainFrame.getCardService().uploadAvatar(avatarData);
            }

            // Update MainFrame
            mainFrame.setCurrentName(name);
            mainFrame.setCurrentPhone(phone);

            // Update DB
            // mainFrame.getDbService().updateMemberInfo(mainFrame.getCurrentCardId(), name, phone, email);

            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>✅ LƯU THÀNH CÔNG!</h2>" +
                "<p>Thông tin cá nhân đã được cập nhật.</p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

            mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);

        } else {
            showError("Lưu thông tin thất bại! Vui lòng thử lại.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void onShow() {
        loadCurrentData();
    }
}