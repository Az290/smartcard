package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Trang chủ sau khi đăng nhập
 */
public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;
    
    // Components
    private UserCard userCard;
    private JPanel contentPanel;
    private JLabel lblWelcome;
    private JPanel packageSummary;
    private JPanel quickActions;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));

        // === LEFT: Side Menu ===
        SideMenu sideMenu = new SideMenu(mainFrame);
        add(sideMenu, BorderLayout.WEST);

        // === CENTER: Main Content ===
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(new Color(30, 30, 45));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeader();
        centerPanel.add(header, BorderLayout.NORTH);

        // Content với scroll
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(30, 30, 45));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // === RIGHT: User Info ===
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Load content
        loadDashboardContent();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 45));
        header.setPreferredSize(new Dimension(0, 60));

        lblWelcome = new JLabel("👋 Xin chào!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(Color.WHITE);

        JLabel lblDate = new JLabel(java.time.LocalDate.now().toString());
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDate.setForeground(Color.GRAY);

        header.add(lblWelcome, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);

        return header;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 35, 50));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));

        // User Card
        userCard = new UserCard();
        panel.add(userCard);

        panel.add(Box.createVerticalStrut(20));

        // Quick buttons
        GymButton btnUploadAvatar = new GymButton("📷 Đổi ảnh đại diện", new Color(100, 100, 130));
        btnUploadAvatar.setMaximumSize(new Dimension(250, 40));
        btnUploadAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUploadAvatar.addActionListener(e -> uploadAvatar());

        GymButton btnEditProfile = GymButton.info("✏️ Sửa thông tin");
        btnEditProfile.setMaximumSize(new Dimension(250, 40));
        btnEditProfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEditProfile.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_PROFILE));

        panel.add(btnUploadAvatar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnEditProfile);

        return panel;
    }

    private void loadDashboardContent() {
        contentPanel.removeAll();

        // Quick Actions
        contentPanel.add(createQuickActionsPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // Active Packages
        contentPanel.add(createActivePackagesPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // Recent Transactions
        contentPanel.add(createRecentTransactionsPanel());

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(30, 30, 45));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(createQuickActionCard("💰", "Nạp tiền", new Color(46, 204, 113), 
            () -> mainFrame.showScreen(MainFrame.SCREEN_TOPUP)));
        
        panel.add(createQuickActionCard("🛒", "Mua gói tập", new Color(52, 152, 219), 
            () -> mainFrame.showScreen(MainFrame.SCREEN_BUY_PACKAGE)));
        
        panel.add(createQuickActionCard("🚪", "Check-in", new Color(155, 89, 182), 
            () -> mainFrame.showScreen(MainFrame.SCREEN_CHECKIN)));
        
        panel.add(createQuickActionCard("📋", "Lịch sử", new Color(241, 196, 15), 
            () -> mainFrame.showScreen(MainFrame.SCREEN_HISTORY)));

        return panel;
    }

    private JPanel createQuickActionCard(String icon, String text, Color color, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(40, 40, 55));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(20, 15, 20, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblText.setForeground(Color.WHITE);
        lblText.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblText);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(50, 50, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(40, 40, 55));
            }
        });

        return card;
    }

    private JPanel createActivePackagesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📦 GÓI TẬP ĐANG SỬ DỤNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(0, 200, 180));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        // Load active packages
        String cardId = mainFrame.getCurrentCardId();
        if (cardId != null) {
            List<DatabaseService.MemberPackageInfo> packages = 
                mainFrame.getDbService().getActiveMemberPackages(cardId);

            if (packages.isEmpty()) {
                JLabel noPackage = new JLabel("Bạn chưa có gói tập nào. Hãy mua gói ngay!");
                noPackage.setForeground(Color.GRAY);
                panel.add(noPackage);
            } else {
                for (DatabaseService.MemberPackageInfo pkg : packages) {
                    panel.add(createPackageRow(pkg));
                    panel.add(Box.createVerticalStrut(8));
                }
            }
        } else {
            JLabel noData = new JLabel("Chưa có dữ liệu");
            noData.setForeground(Color.GRAY);
            panel.add(noData);
        }

        return panel;
    }

    private JPanel createPackageRow(DatabaseService.MemberPackageInfo pkg) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(50, 50, 65));
        row.setBorder(new EmptyBorder(10, 15, 10, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel name = new JLabel("📌 " + pkg.packageName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(Color.WHITE);

        String statusText;
        Color statusColor;
        if (pkg.expireDate != null) {
            long daysLeft = (pkg.expireDate.getTime() - System.currentTimeMillis()) / (1000*60*60*24);
            statusText = "Còn " + daysLeft + " ngày";
            statusColor = daysLeft > 7 ? new Color(46, 204, 113) : new Color(241, 196, 15);
        } else if (pkg.remainingSessions != null) {
            statusText = "Còn " + pkg.remainingSessions + " buổi";
            statusColor = pkg.remainingSessions > 3 ? new Color(46, 204, 113) : new Color(241, 196, 15);
        } else {
            statusText = "Không giới hạn";
            statusColor = new Color(46, 204, 113);
        }

        JLabel status = new JLabel(statusText);
        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(statusColor);

        row.add(name, BorderLayout.WEST);
        row.add(status, BorderLayout.EAST);

        return row;
    }

    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📋 GIAO DỊCH GẦN ĐÂY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(0, 200, 180));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        String cardId = mainFrame.getCurrentCardId();
        if (cardId != null) {
            List<DatabaseService.TransactionInfo> transactions = 
                mainFrame.getDbService().getTransactionHistory(cardId, 5);

            if (transactions.isEmpty()) {
                JLabel noTrans = new JLabel("Chưa có giao dịch nào");
                noTrans.setForeground(Color.GRAY);
                panel.add(noTrans);
            } else {
                for (DatabaseService.TransactionInfo tx : transactions) {
                    panel.add(createTransactionRow(tx));
                    panel.add(Box.createVerticalStrut(5));
                }
            }
        }

        // View all button
        JButton btnViewAll = new JButton("Xem tất cả →");
        btnViewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnViewAll.setForeground(new Color(52, 152, 219));
        btnViewAll.setContentAreaFilled(false);
        btnViewAll.setBorderPainted(false);
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnViewAll.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_HISTORY));

        panel.add(Box.createVerticalStrut(10));
        panel.add(btnViewAll);

        return panel;
    }

    private JPanel createTransactionRow(DatabaseService.TransactionInfo tx) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(50, 50, 65));
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String icon = tx.type.equals("TOPUP") ? "💰" : "🛒";
        String desc = tx.type.equals("TOPUP") ? "Nạp tiền" : 
                     (tx.packageName != null ? "Mua " + tx.packageName : "Mua gói");
        
        JLabel left = new JLabel(icon + " " + desc);
        left.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        left.setForeground(Color.WHITE);

        String amountText = (tx.type.equals("TOPUP") ? "+" : "-") + 
                           String.format("%,d", tx.amount) + " VNĐ";
        Color amountColor = tx.type.equals("TOPUP") ? 
                           new Color(46, 204, 113) : new Color(231, 76, 60);

        JLabel right = new JLabel(amountText);
        right.setFont(new Font("Segoe UI", Font.BOLD, 13));
        right.setForeground(amountColor);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        return row;
    }

   private void uploadAvatar() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        "Image files", "jpg", "jpeg", "png", "gif"
    ));

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        try {
            java.io.File file = chooser.getSelectedFile();
            
            // Read và resize ảnh
            java.awt.image.BufferedImage originalImg = javax.imageio.ImageIO.read(file);
            
            if (originalImg == null) {
                JOptionPane.showMessageDialog(this, 
                    "Không thể đọc file ảnh!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Resize về 150x150
            java.awt.image.BufferedImage resizedImg = new java.awt.image.BufferedImage(
                150, 150, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resizedImg.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                              java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                              java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                              java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(originalImg, 0, 0, 150, 150, null);
            g.dispose();
            
            // Nén JPEG
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            
            if (param.canWriteCompressed()) {
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.85f); // 85% quality
            }
            
            writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
            writer.write(null, new javax.imageio.IIOImage(resizedImg, null, null), param);
            writer.dispose();
            
            byte[] data = baos.toByteArray();
            
            // *** SỬA ĐOẠN NÀY: Kiểm tra với 10KB ***
            if (data.length > 10240) { // Thay vì 1024
                // Thử nén thêm với quality thấp hơn
                baos = new java.io.ByteArrayOutputStream();
                writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
                param = writer.getDefaultWriteParam();
                
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.7f); // 70% quality
                }
                
                writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
                writer.write(null, new javax.imageio.IIOImage(resizedImg, null, null), param);
                writer.dispose();
                
                data = baos.toByteArray();
                
                if (data.length > 10240) {
                    JOptionPane.showMessageDialog(this, 
                        "Ảnh quá lớn! (" + String.format("%.1f KB", data.length / 1024.0) + ")\n" +
                        "Vui lòng chọn ảnh khác.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (mainFrame.getCardService().uploadAvatar(data)) {
                userCard.setAvatar(data);
                JOptionPane.showMessageDialog(this, 
                    "<html><center>" +
                    "<h3>✅ Cập nhật ảnh thành công!</h3>" +
                    "<p>Kích thước: <b>" + String.format("%.1f KB", data.length / 1024.0) + "</b></p>" +
                    "</center></html>",
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Lỗi tải ảnh: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
   

    // ==================== PUBLIC METHODS ====================

  public void setUserInfo(String cardId, String name, String phone) {
    lblWelcome.setText("👋 Xin chào, " + name + "!");
    userCard.setUserInfo(cardId, name, phone);
    
    // *** CHỈ GỌI SAU KHI LOGIN THÀNH CÔNG ***
    if (mainFrame.getCardService().isPinVerified()) {
        userCard.setBalance(mainFrame.getCardService().getBalance());
        loadAvatarFromCard();
    } else {
        System.out.println("[Dashboard] ⚠️ PIN not verified, skipping data load");
    }
}

public void refreshData() {
    if (!mainFrame.getCardService().isPinVerified()) {
        System.out.println("[Dashboard] ⚠️ Cannot refresh - not logged in");
        return;
    }
    
    // Cập nhật số dư
    userCard.setBalance(mainFrame.getCardService().getBalance());
    
    // Load avatar
    loadAvatarFromCard();
    
    // ✅ THÊM: Cập nhật trạng thái check-in
    updateCheckinStatus();
    
    // Reload nội dung dashboard
    loadDashboardContent();
}
private void updateCheckinStatus() {
    String cardId = mainFrame.getCurrentCardId();
    if (cardId == null) {
        userCard.setStatus("⏳ Chưa check-in");
        return;
    }

    java.sql.Connection conn = mainFrame.getDbService().getConnection();
    if (conn == null) {
        userCard.setStatus("⏳ Chưa check-in");
        return;
    }

    try {
        String sql = "SELECT MAX(c.checkin_time) as last_time, " +
                     "SUM(CASE WHEN HOUR(c.checkin_time) >= 5 AND HOUR(c.checkin_time) < 14 THEN 1 ELSE 0 END) as morning, " +
                     "SUM(CASE WHEN HOUR(c.checkin_time) >= 14 THEN 1 ELSE 0 END) as afternoon " +
                     "FROM checkins c " +
                     "JOIN members m ON c.member_id = m.id " +
                     "WHERE m.card_id = ? AND DATE(c.checkin_time) = CURDATE()";
        
        java.sql.PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, cardId);
        java.sql.ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            int morning = rs.getInt("morning");
            int afternoon = rs.getInt("afternoon");
            java.sql.Timestamp lastTime = rs.getTimestamp("last_time");
            
            if (morning + afternoon == 0) {
                userCard.setStatus("⏳ Chưa check-in");
            } else if (morning > 0 && afternoon > 0) {
                userCard.setStatus("✅ Đã check-in 2 buổi");
            } else if (morning > 0) {
                String timeStr = lastTime.toLocalDateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                );
                userCard.setStatus("✅ Đã check-in sáng (" + timeStr + ")");
            } else {
                String timeStr = lastTime.toLocalDateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                );
                userCard.setStatus("✅ Đã check-in chiều (" + timeStr + ")");
            }
        } else {
            userCard.setStatus("⏳ Chưa check-in");
        }
        
        rs.close();
        ps.close();
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
        userCard.setStatus("⏳ Chưa check-in");
    }
}
private void loadAvatarFromCard() {
    // *** KIỂM TRA ĐÃ LOGIN CHƯA ***
    if (!mainFrame.getCardService().isPinVerified()) {
        System.out.println("[Dashboard] ⚠️ Cannot load avatar - not logged in");
        return;
    }
    
    try {
        System.out.println("[Dashboard] 📥 Loading avatar from card...");
        
        // ✅ getAvatar() ĐÃ TỰ ĐỘNG GIẢI MÃ (trong SmartCardService)
        byte[] decryptedAvatar = mainFrame.getCardService().getAvatar();
        
        if (decryptedAvatar != null && decryptedAvatar.length > 0) {
            System.out.println("[Dashboard] ✅ Received DECRYPTED avatar: " + 
                String.format("%.1f KB", decryptedAvatar.length / 1024.0));
            
            // ✅ Hiển thị plaintext image
            userCard.setAvatar(decryptedAvatar);
        } else {
            System.out.println("[Dashboard] ℹ️ No avatar on card");
            userCard.setAvatar(null); // Set default avatar
        }
    } catch (Exception e) {
        System.out.println("[Dashboard] ❌ Error loading avatar: " + e.getMessage());
        e.printStackTrace();
        userCard.setAvatar(null);
    }
}
}