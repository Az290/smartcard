package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;
import gym_app.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Màn hình xem danh sách gói tập
 */
public class PackageListPanel extends JPanel {

    private MainFrame mainFrame;
    private JPanel packagesContainer;
    private JPanel trainersContainer;

    public PackageListPanel(MainFrame mainFrame) {
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
        JLabel title = new JLabel("📦 DANH SÁCH GÓI TẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 152, 219));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Packages section
        JLabel packagesTitle = new JLabel("🏋️ GÓI TẬP THEO THỜI GIAN");
        packagesTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        packagesTitle.setForeground(Color.WHITE);
        packagesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        packagesContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        packagesContainer.setBackground(new Color(30, 30, 45));
        packagesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Trainers section
        JLabel trainersTitle = new JLabel("👨‍🏫 HUẤN LUYỆN VIÊN CÁ NHÂN (PT)");
        trainersTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        trainersTitle.setForeground(Color.WHITE);
        trainersTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        trainersContainer = new JPanel(new GridLayout(0, 2, 15, 15));
        trainersContainer.setBackground(new Color(30, 30, 45));
        trainersContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Back button
        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        // Layout
        content.add(title);
        content.add(Box.createVerticalStrut(25));
        content.add(packagesTitle);
        content.add(Box.createVerticalStrut(15));
        content.add(packagesContainer);
        content.add(Box.createVerticalStrut(30));
        content.add(trainersTitle);
        content.add(Box.createVerticalStrut(15));
        content.add(trainersContainer);
        content.add(Box.createVerticalStrut(30));
        content.add(btnBack);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadPackages() {
        packagesContainer.removeAll();
        trainersContainer.removeAll();

        // Load packages
        List<DatabaseService.PackageInfo> packages = mainFrame.getDbService().getAllPackages();
        for (DatabaseService.PackageInfo pkg : packages) {
            if (pkg.durationDays != null) {
                // Time-based package
                packagesContainer.add(createPackageCard(pkg));
            }
        }

        // Load trainers
        List<DatabaseService.TrainerInfo> trainers = mainFrame.getDbService().getAllActiveTrainers();
        for (DatabaseService.TrainerInfo trainer : trainers) {
            trainersContainer.add(createTrainerCard(trainer));
        }

        packagesContainer.revalidate();
        packagesContainer.repaint();
        trainersContainer.revalidate();
        trainersContainer.repaint();
    }

    private JPanel createPackageCard(DatabaseService.PackageInfo pkg) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(40, 40, 55));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(200, 180));

        JLabel lblName = new JLabel(pkg.name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel(formatMoney(pkg.price));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPrice.setForeground(new Color(46, 204, 113));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDuration = new JLabel(pkg.durationDays + " ngày");
        lblDuration.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDuration.setForeground(Color.GRAY);
        lblDuration.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><center>" + 
            (pkg.description != null ? pkg.description : "") + "</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.LIGHT_GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblName);
        card.add(Box.createVerticalStrut(10));
        card.add(lblPrice);
        card.add(Box.createVerticalStrut(5));
        card.add(lblDuration);
        card.add(Box.createVerticalStrut(10));
        card.add(lblDesc);

        return card;
    }

    private JPanel createTrainerCard(DatabaseService.TrainerInfo trainer) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(new Color(40, 40, 55));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Avatar placeholder
        JLabel avatar = new JLabel("👨‍🏫");
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        avatar.setPreferredSize(new Dimension(70, 70));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(40, 40, 55));

        JLabel lblName = new JLabel(trainer.name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(Color.WHITE);

        JLabel lblExp = new JLabel("⏱️ " + trainer.experienceYears + " năm kinh nghiệm");
        lblExp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExp.setForeground(Color.GRAY);

        JLabel lblRating = new JLabel("⭐ " + trainer.rating + "/5.0");
        lblRating.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRating.setForeground(new Color(241, 196, 15));

        JLabel lblBio = new JLabel("<html>" + 
            (trainer.bio != null ? trainer.bio : "") + "</html>");
        lblBio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBio.setForeground(Color.LIGHT_GRAY);

        // Price
        int price10 = mainFrame.getDbService().getTrainerPrice(trainer.id, "SESSION_10");
        JLabel lblPrice = new JLabel("10 buổi: " + formatMoney(price10));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrice.setForeground(new Color(46, 204, 113));

        info.add(lblName);
        info.add(Box.createVerticalStrut(3));
        info.add(lblExp);
        info.add(lblRating);
        info.add(Box.createVerticalStrut(5));
        info.add(lblBio);
        info.add(Box.createVerticalStrut(5));
        info.add(lblPrice);

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private String formatMoney(long amount) {
        return String.format("%,d VNĐ", amount);
    }
}