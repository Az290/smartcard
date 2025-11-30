package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Màn hình mua gói tập
 */
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

    public BuyPackagePanel(MainFrame mainFrame) {
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
        JLabel title = new JLabel("🛒 MUA GÓI TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(46, 204, 113));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Balance
        JPanel balancePanel = createBalancePanel();

        // Package selection
        JPanel packagePanel = createPackagePanel();

        // Trainer selection (optional)
        trainerPanel = createTrainerPanel();
        trainerPanel.setVisible(false);

        // Summary
        JPanel summaryPanel = createSummaryPanel();

        // Buttons
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

        // Layout
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
        cboPackage.setBackground(new Color(50, 50, 70));
        cboPackage.setForeground(Color.WHITE);
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
        cboTrainer.setBackground(new Color(50, 50, 70));
        cboTrainer.setForeground(Color.WHITE);
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

        JLabel title = new JLabel("📋 CHI TIẾT ĐƠN HÀNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(46, 204, 113));

        lblPackagePrice = createPriceLabel("Giá gói tập:", "0 VNĐ");
        lblTrainerPrice = createPriceLabel("Phí HLV:", "0 VNĐ");
        lblTotalPrice = createPriceLabel("TỔNG CỘNG:", "0 VNĐ");
        lblTotalPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRemaining = createPriceLabel("Số dư còn lại:", "0 VNĐ");

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 80, 100));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblPackagePrice);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblTrainerPrice);
        panel.add(Box.createVerticalStrut(10));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTotalPrice);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblRemaining);

        return panel;
    }

    private JLabel createPriceLabel(String title, String value) {
        JLabel lbl = new JLabel("<html><span style='color:white'>" + title + 
            "</span> <b style='color:#2ecc71'>" + value + "</b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lbl;
    }

    private void loadData() {
        // Load packages
        cboPackage.removeAllItems();
        List<DatabaseService.PackageInfo> packages = mainFrame.getDbService().getAllPackages();
        for (DatabaseService.PackageInfo pkg : packages) {
            cboPackage.addItem(new PackageItem(pkg));
        }

        // Load trainers
        cboTrainer.removeAllItems();
        cboTrainer.addItem(new TrainerItem(null)); // No trainer option
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
            // Show trainer selection for PT packages
            trainerPanel.setVisible(selected.pkg.sessions != null);
        }
        updateSummary();
    }

    private void updateBalance() {
        long balance = mainFrame.getCardService().getBalance();
        lblBalance.setText(formatMoney(balance));
    }

    private void updateSummary() {
        PackageItem pkgItem = (PackageItem) cboPackage.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) cboTrainer.getSelectedItem();

        long packagePrice = 0;
        long trainerPrice = 0;

        if (pkgItem != null && pkgItem.pkg != null) {
            if (pkgItem.pkg.sessions != null && trainerItem != null && trainerItem.trainer != null) {
                // PT package - get trainer price
                String pkgType = pkgItem.pkg.sessions == 10 ? "SESSION_10" : "SESSION_20";
                trainerPrice = mainFrame.getDbService().getTrainerPrice(
                    trainerItem.trainer.id, pkgType
                );
            } else {
                packagePrice = pkgItem.pkg.price;
            }
        }

        long total = packagePrice + trainerPrice;
        long balance = mainFrame.getCardService().getBalance();
        long remaining = balance - total;

        lblPackagePrice.setText("<html><span style='color:white'>Giá gói tập:</span> <b style='color:#2ecc71'>" + 
            formatMoney(packagePrice) + "</b></html>");
        lblTrainerPrice.setText("<html><span style='color:white'>Phí HLV:</span> <b style='color:#2ecc71'>" + 
            formatMoney(trainerPrice) + "</b></html>");
        lblTotalPrice.setText("<html><span style='color:white'>TỔNG CỘNG:</span> <b style='color:#f1c40f; font-size:16px'>" + 
            formatMoney(total) + "</b></html>");

        String remainingColor = remaining >= 0 ? "#2ecc71" : "#e74c3c";
        lblRemaining.setText("<html><span style='color:white'>Số dư còn lại:</span> <b style='color:" + 
            remainingColor + "'>" + formatMoney(remaining) + "</b></html>");
    }

    private void doBuyPackage() {
        PackageItem pkgItem = (PackageItem) cboPackage.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) cboTrainer.getSelectedItem();

        if (pkgItem == null || pkgItem.pkg == null) {
            showError("Vui lòng chọn gói tập!");
            return;
        }

        // Check if PT package needs trainer
        if (pkgItem.pkg.sessions != null && (trainerItem == null || trainerItem.trainer == null)) {
            showError("Gói PT cần chọn huấn luyện viên!");
            return;
        }

        // Calculate total
        long total = 0;
        Integer trainerId = null;
        
        if (pkgItem.pkg.sessions != null && trainerItem.trainer != null) {
            String pkgType = pkgItem.pkg.sessions == 10 ? "SESSION_10" : "SESSION_20";
            total = mainFrame.getDbService().getTrainerPrice(trainerItem.trainer.id, pkgType);
            trainerId = trainerItem.trainer.id;
        } else {
            total = pkgItem.pkg.price;
        }

        // Check balance
        long balance = mainFrame.getCardService().getBalance();
        if (balance < total) {
            showError("Số dư không đủ! Vui lòng nạp thêm tiền.");
            return;
        }

        // Confirm
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><center>" +
            "<h3>Xác nhận mua gói?</h3>" +
            "<p>Gói: <b>" + pkgItem.pkg.name + "</b></p>" +
            (trainerId != null ? "<p>HLV: <b>" + trainerItem.trainer.name + "</b></p>" : "") +
            "<p>Tổng tiền: <b style='color:red'>" + formatMoney(total) + "</b></p>" +
            "</center></html>",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // Deduct balance from card
        if (mainFrame.getCardService().deductBalance(total)) {
            // Save to DB
            boolean success = mainFrame.getDbService().purchasePackage(
                mainFrame.getCurrentCardId(),
                pkgItem.pkg.id,
                trainerId
            );

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h2>✅ MUA GÓI THÀNH CÔNG!</h2>" +
                    "<p>Gói: <b>" + pkgItem.pkg.name + "</b></p>" +
                    (trainerId != null ? "<p>HLV: <b>" + trainerItem.trainer.name + "</b></p>" : "") +
                    "<p>Số dư còn lại: <b>" + formatMoney(mainFrame.getCardService().getBalance()) + "</b></p>" +
                    "</center></html>",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );

                updateBalance();
                updateSummary();
            } else {
                showError("Có lỗi xảy ra khi lưu gói tập!");
            }
        } else {
            showError("Không thể trừ tiền từ thẻ!");
        }
    }

    private String formatMoney(long amount) {
        return String.format("%,d VNĐ", amount);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

       public void onShow() {
        // Cập nhật số dư từ SmartCard
        long balance = mainFrame.getCardService().getBalance();
        lblBalance.setText(String.format("%,d VNĐ", balance));
        
        // Reload packages và trainers
        loadData();
    }

    // Helper classes
    private static class PackageItem {
        DatabaseService.PackageInfo pkg;
        
        PackageItem(DatabaseService.PackageInfo pkg) {
            this.pkg = pkg;
        }
        
        @Override
        public String toString() {
            if (pkg == null) return "-- Chọn gói --";
            String duration = pkg.durationDays != null ? 
                pkg.durationDays + " ngày" : pkg.sessions + " buổi";
            return pkg.name + " - " + duration + " - " + String.format("%,d VNĐ", pkg.price);
        }
    }

    private static class TrainerItem {
        DatabaseService.TrainerInfo trainer;
        
        TrainerItem(DatabaseService.TrainerInfo trainer) {
            this.trainer = trainer;
        }
        
        @Override
        public String toString() {
            if (trainer == null) return "-- Không cần HLV --";
            return trainer.name + " - ⭐" + trainer.rating + " - " + 
                trainer.experienceYears + " năm KN";
        }
    }
    
}