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
 * ✅ Fix: Check-in 2 buổi/ngày (sáng 5h-14h, chiều 14h-23h)
 */
public class CheckinPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblStatus;
    private JLabel lblTime;
    private JLabel lblPackageInfo;
    private JLabel lblLastCheckin; // ✅ THÊM
    private JPanel historyPanel;
    private Timer clockTimer;
    private GymButton btnCheckin; // ✅ THÊM

    public CheckinPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
        startClock();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));

        add(new SideMenu(mainFrame), BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(30, 30, 45));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("🚪 CHECK-IN VÀO PHÒNG TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(155, 89, 182));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel clockPanel = createClockPanel();
        JPanel packagePanel = createPackageStatusPanel();
        JPanel buttonPanel = createButtonPanel();
        historyPanel = createHistoryPanel();

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
        panel.setMaximumSize(new Dimension(500, 220)); // ✅ TĂNG HEIGHT
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

        // ✅ THÊM
        lblLastCheckin = new JLabel(" ");
        lblLastCheckin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLastCheckin.setForeground(Color.GRAY);
        lblLastCheckin.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTime);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblDate);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(5)); // ✅ THÊM
        panel.add(lblLastCheckin); // ✅ THÊM

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

        // ✅ SỬA: Bỏ "GymButton" ở đầu
        btnCheckin = new GymButton("🚪 CHECK-IN NGAY", new Color(155, 89, 182));
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

        return panel;
    }

    /**
     * ✅ THÊM: Kiểm tra đã check-in buổi hiện tại chưa
     */
    private boolean hasCheckedInThisSession() {
        String cardId = mainFrame.getCurrentCardId();
        if (cardId == null) return false;

        java.sql.Connection conn = mainFrame.getDbService().getConnection();
        if (conn == null) return false;

        try {
            int currentHour = LocalDateTime.now().getHour();
            String sessionCondition;
            
            if (currentHour >= 5 && currentHour < 14) {
                sessionCondition = "HOUR(c.checkin_time) >= 5 AND HOUR(c.checkin_time) < 14";
            } else {
                sessionCondition = "HOUR(c.checkin_time) >= 14 AND HOUR(c.checkin_time) < 24";
            }
            
            String sql = "SELECT COUNT(*) as cnt FROM checkins c " +
                         "JOIN members m ON c.member_id = m.id " +
                         "WHERE m.card_id = ? AND DATE(c.checkin_time) = CURDATE() AND " + sessionCondition;
            
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cardId);
            java.sql.ResultSet rs = ps.executeQuery();
            
            boolean result = false;
            if (rs.next()) {
                result = rs.getInt("cnt") > 0;
            }
            
            rs.close();
            ps.close();
            return result;
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ THÊM: Lấy thời gian check-in buổi hiện tại
     */
    private java.sql.Timestamp getLastCheckinThisSession() {
        String cardId = mainFrame.getCurrentCardId();
        if (cardId == null) return null;

        java.sql.Connection conn = mainFrame.getDbService().getConnection();
        if (conn == null) return null;

        try {
            int currentHour = LocalDateTime.now().getHour();
            String sessionCondition;
            
            if (currentHour >= 5 && currentHour < 14) {
                sessionCondition = "HOUR(c.checkin_time) >= 5 AND HOUR(c.checkin_time) < 14";
            } else {
                sessionCondition = "HOUR(c.checkin_time) >= 14 AND HOUR(c.checkin_time) < 24";
            }
            
            String sql = "SELECT MAX(c.checkin_time) as last_time FROM checkins c " +
                         "JOIN members m ON c.member_id = m.id " +
                         "WHERE m.card_id = ? AND DATE(c.checkin_time) = CURDATE() AND " + sessionCondition;
            
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cardId);
            java.sql.ResultSet rs = ps.executeQuery();
            
            java.sql.Timestamp result = null;
            if (rs.next()) {
                result = rs.getTimestamp("last_time");
            }
            
            rs.close();
            ps.close();
            return result;
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ THÊM: Lấy tên buổi
     */
    private String getCurrentSessionName() {
        int hour = LocalDateTime.now().getHour();
        return (hour >= 5 && hour < 14) ? "sáng" : "chiều";
    }

    private void doCheckin() {
        System.out.println("\n[Checkin] ====== BẮT ĐẦU CHECK-IN =======");

        // ✅ Kiểm tra đã check-in buổi này chưa
        if (hasCheckedInThisSession()) {
            java.sql.Timestamp lastTime = getLastCheckinThisSession();
            String timeStr = lastTime != null ? 
                lastTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "---";
            
            JOptionPane.showMessageDialog(this,
                "<html><center>" +
                "<h2>⚠️ ĐÃ CHECK-IN BUỔI " + getCurrentSessionName().toUpperCase() + "!</h2>" +
                "<p>Bạn đã check-in buổi " + getCurrentSessionName() + " lúc <b>" + timeStr + "</b></p>" +
                "<p>Mỗi buổi chỉ được check-in 1 lần.</p>" +
                "<p style='color:#888'>Buổi sáng: 5h-14h | Buổi chiều: 14h-23h</p>" +
                "</center></html>",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );
            System.out.println("[Checkin] ❌ Already checked in this session at " + timeStr);
            return;
        }

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
            System.out.println("[Checkin] ❌ No active packages");
            return;
        }

        if (mainFrame.getCardService().checkIn()) {
            if (mainFrame.getDbService().checkIn(cardId)) {
                LocalDateTime now = LocalDateTime.now();
                String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                lblStatus.setText("✅ ĐÃ CHECK-IN BUỔI " + getCurrentSessionName().toUpperCase());
                lblStatus.setForeground(new Color(46, 204, 113));
                lblLastCheckin.setText("Lúc " + timeStr);

                btnCheckin.setEnabled(false);
                btnCheckin.setText("✅ Đã check-in buổi " + getCurrentSessionName());

                playSuccessAnimation();

                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h1>✅ CHECK-IN BUỔI " + getCurrentSessionName().toUpperCase() + " THÀNH CÔNG!</h1>" +
                    "<p style='font-size:16px'>Chào mừng <b>" + mainFrame.getCurrentName() + "</b></p>" +
                    "<p>Thời gian: <b>" + timeStr + "</b></p>" +
                    "<br><p style='color:green; font-size:18px'>💪 Chúc bạn tập luyện vui vẻ!</p>" +
                    "</center></html>",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );

                refreshHistory();
                System.out.println("[Checkin] ✅ Check-in successful (session: " + getCurrentSessionName() + ")");
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Check-in thất bại! Vui lòng xác thực PIN trước.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void playSuccessAnimation() {
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
        int monthlyCount = mainFrame.getDbService().getMonthlyCheckInCount(
            mainFrame.getCurrentCardId()
        );
        
        Component[] comps = historyPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JLabel && ((JLabel) c).getText().startsWith("Số lần")) {
                historyPanel.remove(c);
                break;
            }
        }
        
        JLabel lblCount = new JLabel("Số lần check-in: " + monthlyCount + " buổi");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCount.setForeground(new Color(46, 204, 113));
        historyPanel.add(lblCount);
        
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    public void onShow() {
        System.out.println("[Checkin] onShow() - Refreshing status...");

        String cardId = mainFrame.getCurrentCardId();
        if (cardId != null) {
            boolean checkedInThisSession = hasCheckedInThisSession();
            java.sql.Timestamp lastTime = getLastCheckinThisSession();

            if (checkedInThisSession && lastTime != null) {
                String timeStr = lastTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                lblStatus.setText("✅ ĐÃ CHECK-IN BUỔI " + getCurrentSessionName().toUpperCase());
                lblStatus.setForeground(new Color(46, 204, 113));
                lblLastCheckin.setText("Lúc " + timeStr);
                
                btnCheckin.setEnabled(false);
                btnCheckin.setText("✅ Đã check-in buổi " + getCurrentSessionName());
                
                System.out.println("[Checkin] Already checked in this session at " + timeStr);
            } else {
                lblStatus.setText("⏳ Sẵn sàng check-in buổi " + getCurrentSessionName());
                lblStatus.setForeground(new Color(241, 196, 15));
                lblLastCheckin.setText(" ");
                
                btnCheckin.setEnabled(true);
                btnCheckin.setText("🚪 CHECK-IN BUỔI " + getCurrentSessionName().toUpperCase());
                
                System.out.println("[Checkin] Not checked in this session yet");
            }

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