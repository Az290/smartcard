package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Base64;

/**
 * Màn hình nạp tiền - Giới hạn theo applet
 * 1 đơn vị = 10,000 VNĐ, max 255 đơn vị/lần = 2,550,000 VNĐ
 */
public class TopupPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField txtAmount;
    private JLabel lblCurrentBalance;
    private JLabel lblNewBalance;
    private JPanel receiptPanel;

    private static final int BALANCE_UNIT = 10000;
    private static final int MAX_TOPUP_PER_TX = 255 * BALANCE_UNIT;
    private static final int MIN_TOPUP = BALANCE_UNIT;
    
    private static final int[] QUICK_AMOUNTS = {100000, 200000, 500000, 1000000, 2000000, 2500000};

    public TopupPanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("💰 NẠP TIỀN VÀO THẺ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(46, 204, 113));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLimit = new JLabel("<html><span style='color:#f1c40f'>⚠️ Giới hạn: " + 
            formatMoney(MIN_TOPUP) + " - " + formatMoney(MAX_TOPUP_PER_TX) + "/lần</span></html>");
        lblLimit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLimit.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel balancePanel = createBalancePanel();
        JPanel quickPanel = createQuickAmountPanel();
        JPanel customPanel = createCustomAmountPanel();
        receiptPanel = createReceiptPanel();
        receiptPanel.setVisible(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(new Color(30, 30, 45));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GymButton btnTopup = GymButton.success("✓ XÁC NHẬN NẠP TIỀN");
        btnTopup.setPreferredSize(new Dimension(250, 50));
        btnTopup.addActionListener(e -> doTopup());

        GymButton btnBack = new GymButton("← Quay lại", new Color(100, 100, 120));
        btnBack.setPreferredSize(new Dimension(150, 50));
        btnBack.addActionListener(e -> {
            resetForm();
            mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
        });

        buttonPanel.add(btnTopup);
        buttonPanel.add(btnBack);

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(lblLimit);
        content.add(Box.createVerticalStrut(20));
        content.add(balancePanel);
        content.add(Box.createVerticalStrut(25));
        content.add(quickPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(customPanel);
        content.add(Box.createVerticalStrut(25));
        content.add(buttonPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(receiptPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBalancePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(30, 30, 45));
        panel.setMaximumSize(new Dimension(600, 100));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel currentPanel = new JPanel();
        currentPanel.setLayout(new BoxLayout(currentPanel, BoxLayout.Y_AXIS));
        currentPanel.setBackground(new Color(40, 40, 55));
        currentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblCurrentTitle = new JLabel("Số dư hiện tại");
        lblCurrentTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCurrentTitle.setForeground(Color.GRAY);

        lblCurrentBalance = new JLabel("0 VNĐ");
        lblCurrentBalance.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblCurrentBalance.setForeground(new Color(52, 152, 219));

        currentPanel.add(lblCurrentTitle);
        currentPanel.add(Box.createVerticalStrut(5));
        currentPanel.add(lblCurrentBalance);

        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
        newPanel.setBackground(new Color(40, 40, 55));
        newPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblNewTitle = new JLabel("Số dư sau nạp");
        lblNewTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNewTitle.setForeground(Color.GRAY);

        lblNewBalance = new JLabel("0 VNĐ");
        lblNewBalance.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblNewBalance.setForeground(new Color(46, 204, 113));

        newPanel.add(lblNewTitle);
        newPanel.add(Box.createVerticalStrut(5));
        newPanel.add(lblNewBalance);

        panel.add(currentPanel);
        panel.add(newPanel);

        return panel;
    }

    private JPanel createQuickAmountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("⚡ Chọn nhanh:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonsPanel.setBackground(new Color(30, 30, 45));
        buttonsPanel.setMaximumSize(new Dimension(600, 110));

        for (int amount : QUICK_AMOUNTS) {
            JButton btn = new JButton(formatMoney(amount));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBackground(new Color(50, 50, 70));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btn.addActionListener(e -> {
                txtAmount.setText(String.valueOf(amount));
                updateNewBalance();
            });
            
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(new Color(0, 150, 136));
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(new Color(50, 50, 70));
                }
            });
            
            buttonsPanel.add(btn);
        }

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonsPanel);

        return panel;
    }

    private JPanel createCustomAmountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("✏️ Hoặc nhập số tiền:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inputPanel.setBackground(new Color(30, 30, 45));

        txtAmount = new JTextField(15);
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtAmount.setBackground(new Color(50, 50, 70));
        txtAmount.setForeground(Color.WHITE);
        txtAmount.setCaretColor(Color.WHITE);
        txtAmount.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(12, 15, 12, 15)
        ));
        txtAmount.setPreferredSize(new Dimension(250, 50));

        txtAmount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateNewBalance(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateNewBalance(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateNewBalance(); }
        });

        JLabel lblUnit = new JLabel(" VNĐ");
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUnit.setForeground(Color.WHITE);

        inputPanel.add(txtAmount);
        inputPanel.add(lblUnit);

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputPanel);

        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 55, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(500, 300));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    private void updateNewBalance() {
        try {
            long currentBalance = mainFrame.getCardService().getBalance();
            lblCurrentBalance.setText(formatMoney(currentBalance));

            String amountStr = txtAmount.getText().replaceAll("[^0-9]", "");
            if (!amountStr.isEmpty()) {
                long amount = Long.parseLong(amountStr);
                lblNewBalance.setText(formatMoney(currentBalance + amount));
            } else {
                lblNewBalance.setText(formatMoney(currentBalance));
            }
        } catch (Exception e) {
            lblNewBalance.setText("---");
        }
    }

    private void doTopup() {
        try {
            String amountStr = txtAmount.getText().replaceAll("[^0-9]", "");
            if (amountStr.isEmpty()) {
                showError("Vui lòng nhập số tiền!");
                return;
            }

            long amount = Long.parseLong(amountStr);

            if (amount < MIN_TOPUP) {
                showError("Số tiền tối thiểu là " + formatMoney(MIN_TOPUP) + "!");
                return;
            }

            if (amount > MAX_TOPUP_PER_TX) {
                showError("Số tiền tối đa mỗi lần là " + formatMoney(MAX_TOPUP_PER_TX) + "!");
                return;
            }
            
            // Làm tròn
            long roundedAmount = (amount / BALANCE_UNIT) * BALANCE_UNIT;
            if (roundedAmount != amount) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Số tiền sẽ làm tròn thành " + formatMoney(roundedAmount) + "\nĐồng ý?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm != JOptionPane.YES_OPTION) return;
                amount = roundedAmount;
            }

            if (mainFrame.getCardService().topup(amount)) {
                long newBalance = mainFrame.getCardService().getBalance();

                byte[] signature = mainFrame.getCardService().signTransaction((byte) 0x01, amount);
                String sigBase64 = signature != null && signature.length > 0 ? 
                    Base64.getEncoder().encodeToString(signature) : "";

                mainFrame.getDbService().logTransaction(
                    mainFrame.getCurrentCardId(),
                    "TOPUP",
                    amount,
                    sigBase64
                );
                mainFrame.getDbService().updateBalance(mainFrame.getCurrentCardId(), newBalance);

                showReceipt(amount, newBalance, sigBase64);
                updateNewBalance();

                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h2>✅ NẠP TIỀN THÀNH CÔNG!</h2>" +
                    "<p>Số tiền: <b style='color:green'>" + formatMoney(amount) + "</b></p>" +
                    "<p>Số dư mới: <b style='color:blue'>" + formatMoney(newBalance) + "</b></p>" +
                    "</center></html>",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );

                txtAmount.setText("");

            } else {
                showError("Nạp tiền thất bại!");
            }

        } catch (NumberFormatException e) {
            showError("Số tiền không hợp lệ!");
        }
    }

    private void showReceipt(long amount, long newBalance, String signature) {
        receiptPanel.removeAll();
        receiptPanel.setVisible(true);

        JLabel titleLabel = new JLabel("🧾 BIÊN LAI NẠP TIỀN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(46, 204, 113));

        String receiptText = String.format(
            "<html>" +
            "<p>Mã thẻ: <b>%s</b></p>" +
            "<p>Khách hàng: <b>%s</b></p>" +
            "<p>Số tiền: <b style='color:#2ecc71'>%s</b></p>" +
            "<p>Số dư mới: <b>%s</b></p>" +
            "<p>Thời gian: %s</p>" +
            "</html>",
            mainFrame.getCurrentCardId(),
            mainFrame.getCurrentName(),
            formatMoney(amount),
            formatMoney(newBalance),
            java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19)
        );

        JLabel receiptLabel = new JLabel(receiptText);
        receiptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        receiptLabel.setForeground(Color.WHITE);

        receiptPanel.add(titleLabel);
        receiptPanel.add(Box.createVerticalStrut(15));
        receiptPanel.add(receiptLabel);

        receiptPanel.revalidate();
        receiptPanel.repaint();
    }

    private void resetForm() {
        txtAmount.setText("");
        receiptPanel.setVisible(false);
        updateNewBalance();
    }

    private String formatMoney(long amount) {
        return String.format("%,d VNĐ", amount);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void onShow() {
        long balance = mainFrame.getCardService().getBalance();
        lblCurrentBalance.setText(formatMoney(balance));
        lblNewBalance.setText(formatMoney(balance));
        txtAmount.setText("");
        receiptPanel.setVisible(false);
    }
}