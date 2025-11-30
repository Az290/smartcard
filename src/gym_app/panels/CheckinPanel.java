package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Check-in
 */
public class CheckinPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblStatus;
    private JLabel lblTime;
    private JLabel lblPackageInfo;
    private JPanel historyPanel;
    private Timer clockTimer;

    public CheckinPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        startClock();
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
        JLabel title = new JLabel("🚪 CHECK-IN VÀO PHÒNG TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(155, 89, 182));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Clock
        JPanel clockPanel = createClockPanel();

        // Package status
        JPanel packagePanel = createPackageStatusPanel();

        // Check-in button
        JPanel buttonPanel = createButtonPanel();

        // Check-in history today
        historyPanel = createHistoryPanel();

        // Layout
        content.add(title);
        content.add(Box.createVerticalStrut(30));
        content.add(clockPanel);
        content.add(Box.createVerticalStrut(30));
        content.add(packagePanel);
        content.add(Box.createVerticalStrut(30));
        content.add(buttonPanel);
        content.add(Box.createVerticalStrut(30));
        content.add(historyPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createClockPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            new EmptyBorder(30, 50, 30, 50)
        ));
        panel.setMaximumSize(new Dimension(500, 180));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Consolas", Font.BOLD, 60));
        lblTime.setForeground(Color.WHITE);
        lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDate = new JLabel(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")
        ));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setForeground(Color.GRAY);
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStatus = new JLabel("⏳ Sẵn sàng check-in");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatus.setForeground(new Color(241, 196, 15));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTime);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblDate);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblStatus);

        return panel;
    }

    private JPanel createPackageStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(600, 150));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📦 TRẠNG THÁI GÓI TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(0, 200, 180));

        lblPackageInfo = new JLabel("Đang tải...");
        lblPackageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPackageInfo.setForeground(Color.WHITE);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblPackageInfo);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(new Color(30, 30, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GymButton btnCheckin = new GymButton("🚪 CHECK-IN NGAY", new Color(155, 89, 182));
        btnCheckin.setPreferredSize(new Dimension(250, 60));
        btnCheckin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnCheckin.addActionListener(e -> doCheckin());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 60));
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        panel.add(btnCheckin);
        panel.add(btnBack);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(600, 200));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("📋 LỊCH SỬ CHECK-IN THÁNG NÀY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(0, 200, 180));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        // Load monthly count
        int monthlyCount = mainFrame.getDbService().getMonthlyCheckInCount(
            mainFrame.getCurrentCardId()
        );

        JLabel lblCount = new JLabel("Số lần check-in: " + monthlyCount + " buổi");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCount.setForeground(new Color(46, 204, 113));

        panel.add(lblCount);

        return panel;
    }

    private void doCheckin() {
        // Kiểm tra có gói tập không
        String cardId = mainFrame.getCurrentCardId();
        List<DatabaseService.MemberPackageInfo> packages = 
            mainFrame.getDbService().getActiveMemberPackages(cardId);

        if (packages.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>❌ KHÔNG THỂ CHECK-IN</h2>" +
                "<p>Bạn chưa có gói tập hoặc gói đã hết hạn!</p>" +
                "<p>Vui lòng mua gói tập để tiếp tục.</p>" +
                "</center></html>",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Check-in qua SmartCard
        if (mainFrame.getCardService().checkIn()) {
            // Log vào DB
            mainFrame.getDbService().checkIn(cardId);

            // Update UI
            lblStatus.setText("✅ ĐÃ CHECK-IN LÚC " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblStatus.setForeground(new Color(46, 204, 113));

            // Animation
            playSuccessAnimation();

            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h1>✅ CHECK-IN THÀNH CÔNG!</h1>" +
                "<p style='font-size:16px'>Chào mừng <b>" + mainFrame.getCurrentName() + "</b></p>" +
                "<p>Thời gian: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")
                ) + "</p>" +
                "<br><p style='color:green'>💪 Chúc bạn tập luyện vui vẻ!</p>" +
                "</center></html>",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Refresh history
            refreshHistory();

        } else {
            JOptionPane.showMessageDialog(this,
                "Check-in thất bại! Vui lòng xác thực PIN trước.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void playSuccessAnimation() {
        // Flash effect
        Timer flashTimer = new Timer(100, null);
        final int[] count = {0};
        Color originalBg = getBackground();
        
        flashTimer.addActionListener(e -> {
            if (count[0] < 6) {
                if (count[0] % 2 == 0) {
                    setBackground(new Color(46, 204, 113));
                } else {
                    setBackground(originalBg);
                }
                count[0]++;
            } else {
                setBackground(originalBg);
                flashTimer.stop();
            }
        });
        flashTimer.start();
    }

    private void startClock() {
        clockTimer = new Timer(1000, e -> {
            lblTime.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
            ));
        });
        clockTimer.start();
    }

    private void refreshHistory() {
        // Reload monthly count
        int monthlyCount = mainFrame.getDbService().getMonthlyCheckInCount(
            mainFrame.getCurrentCardId()
        );
        
        // Update in historyPanel
        Component[] comps = historyPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Số lần")) {
                ((JLabel) c).setText("Số lần check-in: " + monthlyCount + " buổi");
            }
        }
    }

    public void onShow() {
        // Refresh package status
        String cardId = mainFrame.getCurrentCardId();
        if (cardId != null) {
            List<DatabaseService.MemberPackageInfo> packages = 
                mainFrame.getDbService().getActiveMemberPackages(cardId);

            if (packages.isEmpty()) {
                lblPackageInfo.setText("<html><span style='color:#e74c3c'>⚠️ Không có gói tập! Vui lòng mua gói.</span></html>");
            } else {
                StringBuilder sb = new StringBuilder("<html>");
                for (DatabaseService.MemberPackageInfo pkg : packages) {
                    sb.append("✅ ").append(pkg.packageName);
                    if (pkg.expireDate != null) {
                        long days = (pkg.expireDate.getTime() - System.currentTimeMillis()) / (1000*60*60*24);
                        sb.append(" - Còn ").append(days).append(" ngày");
                    } else if (pkg.remainingSessions != null) {
                        sb.append(" - Còn ").append(pkg.remainingSessions).append(" buổi");
                    }
                    sb.append("<br>");
                }
                sb.append("</html>");
                lblPackageInfo.setText(sb.toString());
            }
        }

        refreshHistory();
    }
}