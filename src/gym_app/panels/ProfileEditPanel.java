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
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import java.sql.*;

/**
 * Màn hình sửa thông tin cá nhân
 * ✅ Avatar: PC mã hóa → Card lưu encrypted → PC giải mã khi hiển thị
 * ✅ Info: Card mã hóa → Card giải mã khi get
 * ✅ Database: Mã hóa AES theo SecurityUtils
 */
public class ProfileEditPanel extends JPanel {

    private MainFrame mainFrame;
    
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtBirthDate;
    private JTextField txtAddress;
    private JLabel lblAvatar;
    private JLabel lblAvatarInfo;
    private byte[] newAvatarData;      // Avatar MỚI được chọn (plaintext)
    private byte[] currentAvatarData;  // Avatar HIỆN TẠI từ thẻ (plaintext - đã decrypt)
    
    private static final int AVATAR_MAX_SIZE = 10240;  // 10KB
    private static final int MAX_NAME_LENGTH = 40;

    public ProfileEditPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));

        add(new SideMenu(mainFrame), BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 45));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("👤 THÔNG TIN CÁ NHÂN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 152, 219));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatarPanel = createAvatarPanel();
        JPanel formPanel = createFormPanel();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GymButton btnSave = GymButton.success("💾 LƯU THAY ĐỔI");
        btnSave.setPreferredSize(new Dimension(200, 50));
        btnSave.addActionListener(e -> saveProfile());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnBack);

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
        panel.setMaximumSize(new Dimension(300, 300));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("📷 ẢNH ĐẠI DIỆN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 200, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(100, 100));
        lblAvatar.setMaximumSize(new Dimension(100, 100));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120), 2));
        setDefaultAvatar();

        GymButton btnUpload = new GymButton("📤 Chọn ảnh mới", new Color(100, 100, 130));
        btnUpload.setMaximumSize(new Dimension(200, 35));
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUpload.addActionListener(e -> uploadAvatar());

        lblAvatarInfo = new JLabel("Ảnh tự động nén về ≤10KB");
        lblAvatarInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAvatarInfo.setForeground(Color.GRAY);
        lblAvatarInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblAvatar);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnUpload);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblAvatarInfo);

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

        txtName = createTextField();
        txtPhone = createTextField();
        txtEmail = createTextField();
        txtBirthDate = createTextField();
        txtAddress = createTextField();

        panel.add(formTitle);
        panel.add(Box.createVerticalStrut(20));
        panel.add(createFieldRow("👤 Họ và tên *", txtName));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("📱 Số điện thoại *", txtPhone));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("📧 Email", txtEmail));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("🎂 Ngày sinh (dd/MM/yyyy)", txtBirthDate));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("🏠 Địa chỉ", txtAddress));

        JLabel lblImportant = new JLabel("<html><span style='color:#e74c3c'>⚠️ SĐT dùng để mở khóa thẻ nếu quên PIN!</span></html>");
        lblImportant.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImportant.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblImportant);

        return panel;
    }

    private JTextField createTextField() {
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
        lbl.setPreferredSize(new Dimension(180, 30));

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    private void setDefaultAvatar() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, new Color(60, 60, 80), 
                                              100, 100, new Color(40, 40, 60));
        g.setPaint(gp);
        g.fillRect(0, 0, 100, 100);

        g.setColor(new Color(100, 100, 130));
        g.fillOval(35, 15, 30, 30);
        g.fillRoundRect(25, 50, 50, 40, 15, 15);

        g.dispose();
        lblAvatar.setIcon(new ImageIcon(img));
    }

    private void uploadAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage originalImg = ImageIO.read(file);
                
                if (originalImg == null) {
                    showError("Không thể đọc file ảnh!");
                    return;
                }
                
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                lblAvatarInfo.setText("⏳ Đang nén ảnh...");
                lblAvatarInfo.setForeground(new Color(241, 196, 15));
                
                // ✅ Tự động nén để vừa 10KB
                byte[] imageData = compressToFit(originalImg);
                
                setCursor(Cursor.getDefaultCursor());
                
                if (imageData == null || imageData.length == 0) {
                    imageData = createPlaceholderAvatar();
                    lblAvatarInfo.setText("⚠️ Dùng ảnh mặc định (" + imageData.length + " bytes)");
                } else {
                    lblAvatarInfo.setText("✅ " + String.format("%.1f KB", imageData.length / 1024.0));
                    lblAvatarInfo.setForeground(new Color(46, 204, 113));
                }

                // ✅ Lưu PLAINTEXT vào newAvatarData
                newAvatarData = imageData;
                
                // Preview
                BufferedImage preview = resizeImage(originalImg, 100, 100);
                lblAvatar.setIcon(new ImageIcon(preview));
                
                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h3>✅ Đã chọn ảnh mới!</h3>" +
                    "<p>Dung lượng: <b>" + String.format("%.1f KB", imageData.length / 1024.0) + "</b></p>" +
                    "<p style='color:#f1c40f'>⚠️ Ảnh sẽ được mã hóa khi lưu vào thẻ</p>" +
                    "</center></html>",
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                showError("Lỗi: " + ex.getMessage());
            }
        }
    }

    private byte[] compressToFit(BufferedImage original) {
        int[] sizes = {128, 96, 64, 48, 40, 32, 28, 24, 20, 16, 12, 10, 8};
        float[] qualities = {0.85f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.15f, 0.1f};
        
        byte[] result = null;
        
        for (int size : sizes) {
            for (float quality : qualities) {
                byte[] data = compressImage(original, size, quality);
                if (data != null && data.length <= AVATAR_MAX_SIZE) {
                    if (result == null || data.length > result.length) {
                        result = data;
                    }
                    if (data.length >= 8192) { // ≥8KB là tốt
                        return data;
                    }
                }
            }
            if (result != null && result.length >= 4096) { // ≥4KB là chấp nhận được
                return result;
            }
        }
        
        return result;
    }

    private byte[] createPlaceholderAvatar() {
        try {
            BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(100, 100, 130));
            g.fillRect(0, 0, 8, 8);
            g.setColor(new Color(150, 150, 170));
            g.fillOval(2, 1, 4, 3);
            g.fillRect(1, 4, 6, 4);
            g.dispose();
            return compressImage(img, 8, 0.1f);
        } catch (Exception e) {
            return new byte[0];
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

    private byte[] compressImage(BufferedImage original, int size, float quality) {
        try {
            BufferedImage resized = resizeImage(original, size, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(resized, null, null), param);
            writer.dispose();
            
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ Load dữ liệu hiện tại
     */
    private void loadCurrentData() {
        newAvatarData = null;
        currentAvatarData = null;
        
        String cardId = mainFrame.getCurrentCardId();
        
        // ========== 1. LOAD INFO TỪ DATABASE (ENCRYPTED) ==========
        Connection conn = mainFrame.getDbService().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT name_enc, phone_enc, birth_date_enc FROM members WHERE card_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, cardId);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String nameEnc = rs.getString("name_enc");
                    String phoneEnc = rs.getString("phone_enc");
                    String birthEnc = rs.getString("birth_date_enc");
                    
                    // ✅ GIẢI MÃ trước khi hiển thị
                    txtName.setText(nameEnc != null ? SecurityUtils.decrypt(nameEnc) : "");
                    txtPhone.setText(phoneEnc != null ? SecurityUtils.decrypt(phoneEnc) : "");
                    txtBirthDate.setText(birthEnc != null ? SecurityUtils.decrypt(birthEnc) : "");
                    
                    System.out.println("[Profile] ✅ Loaded DECRYPTED info from database");
                } else {
                    // Fallback từ MainFrame
                    txtName.setText(mainFrame.getCurrentName() != null ? mainFrame.getCurrentName() : "");
                    txtPhone.setText(mainFrame.getCurrentPhone() != null ? mainFrame.getCurrentPhone() : "");
                    System.out.println("[Profile] ℹ️ No data in database, using MainFrame cache");
                }
                
                rs.close();
                ps.close();
            } catch (SQLException e) {
                System.out.println("[Profile] ❌ Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ========== 2. LOAD AVATAR TỪ THẺ (ENCRYPTED → DECRYPTED) ==========
        if (mainFrame.getCardService().isPinVerified()) {
            try {
                System.out.println("[Profile] 📥 Loading avatar from card...");
                
                // ✅ getAvatar() TỰ ĐỘNG DOWNLOAD VÀ GIẢI MÃ
                currentAvatarData = mainFrame.getCardService().getAvatar();
                
                if (currentAvatarData != null && currentAvatarData.length > 0) {
                    System.out.println("[Profile] ✅ Received DECRYPTED avatar: " + 
                        String.format("%.1f KB", currentAvatarData.length / 1024.0));
                    
                    // ✅ Hiển thị plaintext image
                    try {
                        ImageIcon icon = new ImageIcon(currentAvatarData);
                        Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        lblAvatar.setIcon(new ImageIcon(scaled));
                        lblAvatarInfo.setText("✅ " + String.format("%.1f KB", currentAvatarData.length / 1024.0));
                        lblAvatarInfo.setForeground(new Color(46, 204, 113));
                    } catch (Exception e) {
                        System.out.println("[Profile] ❌ Cannot display avatar: " + e.getMessage());
                        e.printStackTrace();
                        setDefaultAvatar();
                        lblAvatarInfo.setText("❌ Ảnh lỗi");
                        lblAvatarInfo.setForeground(new Color(231, 76, 60));
                    }
                } else {
                    System.out.println("[Profile] ℹ️ No avatar on card");
                    setDefaultAvatar();
                    lblAvatarInfo.setText("Chưa có ảnh");
                    lblAvatarInfo.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                System.out.println("[Profile] ❌ Error loading avatar: " + e.getMessage());
                e.printStackTrace();
                setDefaultAvatar();
                lblAvatarInfo.setText("Lỗi tải ảnh");
                lblAvatarInfo.setForeground(new Color(231, 76, 60));
            }
        } else {
            System.out.println("[Profile] ⚠️ PIN not verified, cannot load avatar");
            setDefaultAvatar();
            lblAvatarInfo.setText("Chưa đăng nhập");
            lblAvatarInfo.setForeground(Color.GRAY);
        }
    }

    /**
     * ✅ LƯU THÔNG TIN - FULL ENCRYPTION
     */
    private void saveProfile() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String birthDate = txtBirthDate.getText().trim();
        String address = txtAddress.getText().trim();

        // ========== VALIDATE ==========
        if (name.isEmpty() || name.length() < 2) {
            showError("Vui lòng nhập họ tên!");
            txtName.requestFocus();
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            showError("Số điện thoại phải có 10-11 số!");
            txtPhone.requestFocus();
            return;
        }

        System.out.println("\n[Profile] ====== BẮT ĐẦU LƯU =======");

        // ========== 1. UPLOAD AVATAR VÀO THẺ (NẾU CÓ) ==========
        if (newAvatarData != null && newAvatarData.length > 0) {
            System.out.println("[Profile] 📤 Uploading new avatar to card (" + 
                String.format("%.1f KB", newAvatarData.length / 1024.0) + ")...");
            
            // ✅ uploadAvatar() TỰ ĐỘNG MÃ HÓA VÀ UPLOAD
            if (mainFrame.getCardService().uploadAvatar(newAvatarData)) {
                System.out.println("[Profile] ✅ Avatar uploaded and ENCRYPTED on card");
                currentAvatarData = newAvatarData; // Update current
            } else {
                System.out.println("[Profile] ❌ Avatar upload FAILED");
                showError("Không thể lưu ảnh vào thẻ!\nVui lòng thử lại.");
                return;
            }
        }

        // ========== 2. LƯU INFO VÀO THẺ ==========
        String infoForCard = name;
        byte[] nameBytes = infoForCard.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (nameBytes.length > MAX_NAME_LENGTH) {
            infoForCard = truncateUTF8(name, MAX_NAME_LENGTH);
            System.out.println("[Profile] Name truncated to: " + infoForCard);
        }
        
        System.out.println("[Profile] 💾 Saving info to card...");
        // ✅ updateInfo() → Card tự mã hóa
        boolean cardSaved = mainFrame.getCardService().updateInfo(phone, infoForCard);
        
        if (!cardSaved) {
            showError("Lưu thông tin vào thẻ thất bại!");
            return;
        }
        System.out.println("[Profile] ✅ Info saved and ENCRYPTED on card");

        // ========== 3. LƯU VÀO DATABASE (MÃ HÓA) ==========
        String cardId = mainFrame.getCurrentCardId();
        Connection conn = mainFrame.getDbService().getConnection();
        
        if (conn != null) {
            try {
                System.out.println("[Profile] 💾 Saving to database (encrypted)...");
                
                // ✅ MÃ HÓA dữ liệu trước khi lưu
                String nameEnc = SecurityUtils.encrypt(name);
                String phoneEnc = SecurityUtils.encrypt(phone);
                String phoneHash = SecurityUtils.hashPhone(phone);
                String birthEnc = !birthDate.isEmpty() ? SecurityUtils.encrypt(birthDate) : null;
                
                // Check member tồn tại chưa
                String checkSql = "SELECT id FROM members WHERE card_id = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setString(1, cardId);
                ResultSet rs = checkPs.executeQuery();
                
                boolean exists = rs.next();
                rs.close();
                checkPs.close();
                
                if (exists) {
                    // UPDATE
                    String sql = "UPDATE members SET name_enc = ?, phone_enc = ?, phone_hash = ?, " +
                                "birth_date_enc = ? WHERE card_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, nameEnc);
                    ps.setString(2, phoneEnc);
                    ps.setString(3, phoneHash);
                    ps.setString(4, birthEnc);
                    ps.setString(5, cardId);
                    ps.executeUpdate();
                    ps.close();
                    System.out.println("[Profile] ✅ Database UPDATED (encrypted)");
                } else {
                    // INSERT
                    String sql = "INSERT INTO members (card_id, name_enc, phone_enc, phone_hash, " +
                                "birth_date_enc, balance, status) VALUES (?, ?, ?, ?, ?, 0, 'active')";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, cardId);
                    ps.setString(2, nameEnc);
                    ps.setString(3, phoneEnc);
                    ps.setString(4, phoneHash);
                    ps.setString(5, birthEnc);
                    ps.executeUpdate();
                    ps.close();
                    System.out.println("[Profile] ✅ Database INSERTED (encrypted)");
                }
                
            } catch (SQLException e) {
                System.out.println("[Profile] ❌ Database error: " + e.getMessage());
                e.printStackTrace();
                showError("Lỗi lưu database: " + e.getMessage());
                return;
            }
        }

        // ========== 4. CẬP NHẬT UI ==========
        mainFrame.setCurrentName(name);
        mainFrame.setCurrentPhone(phone);
        mainFrame.getCardService().setRecoveryPhone(phone);
        mainFrame.getCardService().saveCardState();

        System.out.println("[Profile] ====== HOÀN TẤT =======\n");

        JOptionPane.showMessageDialog(this,
            "<html><center>" +
            "<h2>✅ LƯU THÀNH CÔNG!</h2>" +
            "<p>✅ Thẻ: Info encrypted (AES)</p>" +
            (newAvatarData != null ? "<p>✅ Thẻ: Avatar encrypted (AES PC-side)</p>" : "") +
            "<p>✅ Database: Encrypted (AES)</p>" +
            "<br><p style='color:#f1c40f'>SĐT <b>" + phone + "</b> dùng để mở khóa thẻ</p>" +
            "</center></html>",
            "Thành công",
            JOptionPane.INFORMATION_MESSAGE
        );

        mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
    }

    /**
     * ✅ Cắt chuỗi UTF-8 đúng cách
     */
    private String truncateUTF8(String str, int maxBytes) {
        if (str == null) return "";
        
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return str;
        
        int len = maxBytes;
        while (len > 0 && (bytes[len] & 0xC0) == 0x80) {
            len--;
        }
        
        return new String(bytes, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * ✅ Được gọi khi panel hiển thị
     */
    public void onShow() {
        loadCurrentData();
    }
}