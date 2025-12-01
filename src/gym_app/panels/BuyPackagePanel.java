package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BuyPackagePanel extends JPanel {

    private MainFrame mainFrame;
    private JComboBox<PackageItem> cboPackage;
    private JComboBox<TrainerItem> cboTrainer;
    private JLabel lblBalance;
    private JLabel lblPackagePrice;
    private JLabel lblTrainerPrice;
    private JLabel lblTotalPrice;
    private JLabel lblRemaining;
    private JPanel trainerPanel;

    private static final int BALANCE_UNIT = 10000;

    public BuyPackagePanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("🛒 MUA GÓI TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(46, 204, 113));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel balancePanel = createBalancePanel();
        JPanel packagePanel = createPackagePanel();
        trainerPanel = createTrainerPanel();
        trainerPanel.setVisible(false);
        JPanel summaryPanel = createSummaryPanel();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GymButton btnBuy = GymButton.success("✓ XÁC NHẬN MUA GÓI");
        btnBuy.setPreferredSize(new Dimension(250, 50));
        btnBuy.addActionListener(e -> doBuyPackage());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        buttonPanel.add(btnBuy);
        buttonPanel.add(btnBack);

        content.add(title);
        content.add(Box.createVerticalStrut(25));
        content.add(balancePanel);
        content.add(Box.createVerticalStrut(25));
        content.add(packagePanel);
        content.add(Box.createVerticalStrut(20));
        content.add(trainerPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(summaryPanel);
        content.add(Box.createVerticalStrut(25));
        content.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    private JPanel createBalancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(400, 80));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("💰 Số dư hiện tại:");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        lblBalance = new JLabel("0 VNĐ");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBalance.setForeground(new Color(52, 152, 219));

        panel.add(lblTitle);
        panel.add(lblBalance);

        return panel;
    }

    private JPanel createPackagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📦 Chọn gói tập:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        cboPackage = new JComboBox<>();
        cboPackage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboPackage.setMaximumSize(new Dimension(400, 40));
        cboPackage.addActionListener(e -> onPackageSelected());

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cboPackage);

        return panel;
    }

    private JPanel createTrainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("👨‍🏫 Chọn huấn luyện viên:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        cboTrainer = new JComboBox<>();
        cboTrainer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboTrainer.setMaximumSize(new Dimension(400, 40));
        cboTrainer.addActionListener(e -> updateSummary());

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cboTrainer);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(500, 200));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📋 CHI TIẾT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(46, 204, 113));

        lblPackagePrice = new JLabel("Giá gói: 0 VNĐ");
        lblPackagePrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPackagePrice.setForeground(Color.WHITE);

        lblTrainerPrice = new JLabel("Phí HLV: 0 VNĐ");
        lblTrainerPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTrainerPrice.setForeground(Color.WHITE);

        lblTotalPrice = new JLabel("TỔNG: 0 VNĐ");
        lblTotalPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalPrice.setForeground(new Color(241, 196, 15));

        lblRemaining = new JLabel("Còn lại: 0 VNĐ");
        lblRemaining.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRemaining.setForeground(Color.WHITE);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblPackagePrice);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblTrainerPrice);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTotalPrice);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblRemaining);

        return panel;
    }

    private void loadData() {
        cboPackage.removeAllItems();
        List<DatabaseService.PackageInfo> packages = mainFrame.getDbService().getAllPackages();
        for (DatabaseService.PackageInfo pkg : packages) {
            cboPackage.addItem(new PackageItem(pkg));
        }

        cboTrainer.removeAllItems();
        cboTrainer.addItem(new TrainerItem(null));
        List<DatabaseService.TrainerInfo> trainers = mainFrame.getDbService().getAllActiveTrainers();
        for (DatabaseService.TrainerInfo trainer : trainers) {
            cboTrainer.addItem(new TrainerItem(trainer));
        }

        updateBalance();
        updateSummary();
    }

    private void onPackageSelected() {
        PackageItem selected = (PackageItem) cboPackage.getSelectedItem();
        if (selected != null && selected.pkg != null) {
            trainerPanel.setVisible(selected.pkg.sessions != null);
        }
        updateSummary();
    }

    private void updateBalance() {
        long balance = mainFrame.getCardService().getBalance();
        lblBalance.setText(String.format("%,d VNĐ", balance));
    }

    private void updateSummary() {
        PackageItem pkgItem = (PackageItem) cboPackage.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) cboTrainer.getSelectedItem();

        long packagePrice = 0;
        long trainerPrice = 0;

        if (pkgItem != null && pkgItem.pkg != null) {
            if (pkgItem.pkg.sessions != null && trainerItem != null && trainerItem.trainer != null) {
                String pkgType = pkgItem.pkg.sessions == 10 ? "SESSION_10" : "SESSION_20";
                trainerPrice = mainFrame.getDbService().getTrainerPrice(trainerItem.trainer.id, pkgType);
            } else {
                packagePrice = pkgItem.pkg.price;
            }
        }

        long total = packagePrice + trainerPrice;
        long balance = mainFrame.getCardService().getBalance();
        long remaining = balance - total;

        lblPackagePrice.setText("Giá gói: " + String.format("%,d VNĐ", packagePrice));
        lblTrainerPrice.setText("Phí HLV: " + String.format("%,d VNĐ", trainerPrice));
        lblTotalPrice.setText("TỔNG: " + String.format("%,d VNĐ", total));
        
        if (remaining >= 0) {
            lblRemaining.setText("Còn lại: " + String.format("%,d VNĐ", remaining));
            lblRemaining.setForeground(new Color(46, 204, 113));
        } else {
            lblRemaining.setText("Còn lại: " + String.format("%,d VNĐ", remaining) + " (THIẾU!)");
            lblRemaining.setForeground(new Color(231, 76, 60));
        }
    }

    private void doBuyPackage() {
        PackageItem pkgItem = (PackageItem) cboPackage.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) cboTrainer.getSelectedItem();

        if (pkgItem == null || pkgItem.pkg == null) {
            showError("Vui lòng chọn gói tập!");
            return;
        }

        if (pkgItem.pkg.sessions != null && (trainerItem == null || trainerItem.trainer == null)) {
            showError("Gói PT cần chọn huấn luyện viên!");
            return;
        }

        System.out.println("\n[BuyPackage] ====== BẮT ĐẦU MUA GÓI =======");

        // ========== 1. KIỂM TRA & TẠO MEMBER NẾU CHƯA CÓ ==========
        String cardId = mainFrame.getCurrentCardId();
        DatabaseService.MemberInfo member = mainFrame.getDbService().getMemberByCardId(cardId);
        
        if (member == null) {
            System.out.println("[BuyPackage] ⚠️ Member not found in database, auto-registering...");
            
            String name = mainFrame.getCurrentName();
            String phone = mainFrame.getCurrentPhone();
            
            if (name == null || name.isEmpty()) {
                name = "Khách hàng";
            }
            
            if (phone == null || phone.isEmpty()) {
                showError("<html><center>" +
                    "<h3>❌ Không tìm thấy số điện thoại!</h3>" +
                    "<p>Vui lòng vào <b>Sửa thông tin</b><br>để cập nhật số điện thoại trước khi mua gói.</p>" +
                    "</center></html>");
                System.out.println("[BuyPackage] ❌ No phone number found");
                return;
            }
            
            boolean registered = mainFrame.getDbService().registerMember(name, phone, cardId);
            if (!registered) {
                showError("Không thể tạo tài khoản member!\nVui lòng liên hệ quản trị viên.");
                System.out.println("[BuyPackage] ❌ Failed to register member");
                return;
            }
            
            // Load lại member
            member = mainFrame.getDbService().getMemberByCardId(cardId);
            if (member == null) {
                showError("Lỗi hệ thống! Không thể load thông tin member.\nVui lòng thử lại.");
                System.out.println("[BuyPackage] ❌ Failed to load member after registration");
                return;
            }
            
            System.out.println("[BuyPackage] ✅ Auto-registered member: " + name + " (" + cardId + ")");
        } else {
            System.out.println("[BuyPackage] ✅ Member found: " + member.name + " (ID: " + member.id + ")");
        }

        // ========== 2. TÍNH TỔNG TIỀN ==========
        long total = 0;
        Integer trainerId = null;
        
        if (pkgItem.pkg.sessions != null && trainerItem != null && trainerItem.trainer != null) {
            String pkgType = pkgItem.pkg.sessions == 10 ? "SESSION_10" : "SESSION_20";
            total = mainFrame.getDbService().getTrainerPrice(trainerItem.trainer.id, pkgType);
            trainerId = trainerItem.trainer.id;
            System.out.println("[BuyPackage] Package type: PT - Trainer: " + trainerItem.trainer.name);
        } else {
            total = pkgItem.pkg.price;
            System.out.println("[BuyPackage] Package type: Time-based");
        }

        System.out.println("[BuyPackage] Total price: " + String.format("%,d VNĐ", total));

        // ========== 3. KIỂM TRA SỐ DƯ ==========
        long balance = mainFrame.getCardService().getBalance();
        if (balance < total) {
            showError("Số dư không đủ!\nCần: " + String.format("%,d", total) + " VNĐ\nCó: " + String.format("%,d", balance) + " VNĐ");
            System.out.println("[BuyPackage] ❌ Insufficient balance");
            return;
        }

        // ========== 4. LÀM TRÒN ==========
        long roundedTotal = (total / BALANCE_UNIT) * BALANCE_UNIT;
        if (roundedTotal < total) {
            roundedTotal += BALANCE_UNIT;
        }
        System.out.println("[BuyPackage] Rounded total: " + String.format("%,d VNĐ", roundedTotal));

        // ========== 5. XÁC NHẬN ==========
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><center>" +
            "<h3>Xác nhận mua gói?</h3>" +
            "<p>Gói: <b>" + pkgItem.pkg.name + "</b></p>" +
            (trainerId != null ? "<p>HLV: <b>" + trainerItem.trainer.name + "</b></p>" : "") +
            "<p>Tổng: <b style='color:green'>" + String.format("%,d", roundedTotal) + " VNĐ</b></p>" +
            "<p>Còn lại: <b>" + String.format("%,d", balance - roundedTotal) + " VNĐ</b></p>" +
            "</center></html>",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            System.out.println("[BuyPackage] ❌ User cancelled");
            return;
        }

        // ========== 6. TRỪ TIỀN TỪ THẺ ==========
        System.out.println("[BuyPackage] 💳 Deducting balance from card...");
        if (!mainFrame.getCardService().deductBalance(roundedTotal)) {
            showError("Không thể trừ tiền từ thẻ!\nVui lòng thử lại.");
            System.out.println("[BuyPackage] ❌ Failed to deduct balance from card");
            return;
        }
        System.out.println("[BuyPackage] ✅ Balance deducted from card");

        // ========== 7. LƯU VÀO DATABASE ==========
        try {
            System.out.println("[BuyPackage] 💾 Saving to database...");
            
            // Tính ngày hết hạn
            java.sql.Timestamp expireDate = null;
            if (pkgItem.pkg.durationDays != null) {
                expireDate = new java.sql.Timestamp(
                    System.currentTimeMillis() + (long)pkgItem.pkg.durationDays * 24 * 60 * 60 * 1000
                );
                System.out.println("[BuyPackage] Expire date: " + expireDate);
            }

            // Insert member_packages
            java.sql.Connection conn = mainFrame.getDbService().getConnection();
            String sql = "INSERT INTO member_packages (member_id, package_id, trainer_id, expire_date, remaining_sessions, is_active) " +
                         "VALUES (?, ?, ?, ?, ?, 1)";
            
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, member.id);
                ps.setInt(2, pkgItem.pkg.id);
                
                if (trainerId != null) {
                    ps.setInt(3, trainerId);
                } else {
                    ps.setNull(3, java.sql.Types.INTEGER);
                }
                
                if (expireDate != null) {
                    ps.setTimestamp(4, expireDate);
                } else {
                    ps.setNull(4, java.sql.Types.TIMESTAMP);
                }
                
                if (pkgItem.pkg.sessions != null) {
                    ps.setInt(5, pkgItem.pkg.sessions);
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                
                int rows = ps.executeUpdate();
                System.out.println("[BuyPackage] ✅ Inserted " + rows + " row(s) into member_packages");
            }

            // Log transaction
            mainFrame.getDbService().logPackagePurchase(cardId, pkgItem.pkg.id, trainerId, roundedTotal, "");
            System.out.println("[BuyPackage] ✅ Transaction logged");

            // Sync balance to database
            long newBalance = mainFrame.getCardService().getBalance();
            mainFrame.getDbService().updateBalance(cardId, newBalance);
            System.out.println("[BuyPackage] ✅ Balance synced to database: " + String.format("%,d VNĐ", newBalance));

            System.out.println("[BuyPackage] ====== HOÀN TẤT =======\n");

            // ========== 8. THÔNG BÁO THÀNH CÔNG ==========
            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>✅ MUA GÓI THÀNH CÔNG!</h2>" +
                "<p>Gói: <b>" + pkgItem.pkg.name + "</b></p>" +
                (trainerId != null ? "<p>HLV: <b>" + trainerItem.trainer.name + "</b></p>" : "") +
                (pkgItem.pkg.durationDays != null ? 
                    "<p>Thời hạn: <b>" + pkgItem.pkg.durationDays + " ngày</b></p>" : 
                    "<p>Số buổi: <b>" + pkgItem.pkg.sessions + "</b></p>") +
                "<br><p>Số dư còn lại: <b style='color:blue'>" + String.format("%,d", newBalance) + " VNĐ</b></p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

            updateBalance();
            updateSummary();

        } catch (Exception e) {
            System.out.println("[BuyPackage] ❌ Database error: " + e.getMessage());
            e.printStackTrace();
            
            // Hoàn tiền
            System.out.println("[BuyPackage] 🔄 Refunding...");
            mainFrame.getCardService().topup(roundedTotal);
            
            showError("Lỗi lưu gói tập vào database!\n" + e.getMessage() + "\n\nĐã hoàn tiền về thẻ.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void onShow() {
        updateBalance();
        loadData();
    }

    // ==================== INNER CLASSES ====================

    private static class PackageItem {
        DatabaseService.PackageInfo pkg;
        
        PackageItem(DatabaseService.PackageInfo pkg) { 
            this.pkg = pkg; 
        }
        
        @Override
        public String toString() {
            if (pkg == null) return "-- Chọn gói tập --";
            
            String duration = "";
            if (pkg.durationDays != null) {
                duration = pkg.durationDays + " ngày";
            } else if (pkg.sessions != null) {
                duration = pkg.sessions + " buổi";
            }
            
            String price = pkg.price > 0 ? String.format("%,d VNĐ", pkg.price) : "Tùy HLV";
            
            return pkg.name + " - " + duration + " - " + price;
        }
    }

    private static class TrainerItem {
        DatabaseService.TrainerInfo trainer;
        
        TrainerItem(DatabaseService.TrainerInfo t) { 
            this.trainer = t; 
        }
        
        @Override
        public String toString() {
            if (trainer == null) return "-- Không cần HLV --";
            return trainer.name + " - ⭐" + trainer.rating + " (" + trainer.experienceYears + " năm KN)";
        }
    }
}