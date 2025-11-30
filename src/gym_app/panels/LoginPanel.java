package gym_app.panels;

import gym_app.MainFrame;
import gym_app.components.GymButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Màn hình đăng nhập bằng PIN
 */
public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JComboBox<String> cboCards;
    private JPasswordField txtPin;
    private JLabel lblError;
    private JLabel lblTries;
    private JLabel lblCardStatus;
    private String selectedCardId = null;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 45));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(40, 40, 55));
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 136), 2),
            new EmptyBorder(40, 50, 40, 50)
        ));
        container.setPreferredSize(new Dimension(450, 600));

        // Logo
        JLabel logo = new JLabel("💪 POWER GYM");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(new Color(0, 200, 180));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hệ thống thẻ thành viên thông minh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Card selection
        JLabel lblSelectCard = new JLabel("🎫 Chọn thẻ của bạn:");
        lblSelectCard.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectCard.setForeground(new Color(0, 200, 180));
        lblSelectCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        cboCards = new JComboBox<>();
        cboCards.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboCards.setBackground(new Color(60, 60, 75));
        cboCards.setForeground(Color.WHITE);
        cboCards.setMaximumSize(new Dimension(280, 35));
        cboCards.addActionListener(e -> onCardSelected());

        // Refresh button
        GymButton btnRefresh = new GymButton("🔄 Làm mới", new Color(52, 152, 219));
        btnRefresh.setMaximumSize(new Dimension(120, 30));
        btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefresh.addActionListener(e -> loadAvailableCards());

        // Card status
        lblCardStatus = new JLabel("📋 Chưa chọn thẻ");
        lblCardStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCardStatus.setForeground(Color.GRAY);
        lblCardStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel title = new JLabel("🔐 ĐĂNG NHẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // PIN input
        JLabel lblPin = new JLabel("Nhập mã PIN (6 số):");
        lblPin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPin.setForeground(Color.WHITE);
        lblPin.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPin = new JPasswordField(6);
        txtPin.setFont(new Font("Segoe UI", Font.BOLD, 32));
        txtPin.setHorizontalAlignment(JTextField.CENTER);
        txtPin.setMaximumSize(new Dimension(200, 50));
        txtPin.setBackground(new Color(60, 60, 75));
        txtPin.setForeground(Color.WHITE);
        txtPin.setCaretColor(Color.WHITE);
        txtPin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 120)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        txtPin.setEnabled(false);

        // Error & Tries labels
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(231, 76, 60));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTries = new JLabel("Chọn thẻ để tiếp tục");
        lblTries.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTries.setForeground(Color.GRAY);
        lblTries.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        GymButton btnLogin = GymButton.success("✓ ĐĂNG NHẬP");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(250, 45));
        btnLogin.addActionListener(e -> doLogin());

        GymButton btnRegister = GymButton.info("📝 ĐĂNG KÝ THẺ MỚI");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(250, 45));
        btnRegister.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_REGISTER));

        JButton btnForgot = new JButton("Quên PIN?");
        btnForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnForgot.setForeground(new Color(52, 152, 219));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnForgot.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_UNBLOCK));

        txtPin.addActionListener(e -> doLogin());

        // Layout
        container.add(logo);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(20));
        container.add(lblSelectCard);
        container.add(Box.createVerticalStrut(8));
        container.add(cboCards);
        container.add(Box.createVerticalStrut(5));
        container.add(btnRefresh);
        container.add(Box.createVerticalStrut(10));
        container.add(lblCardStatus);
        container.add(Box.createVerticalStrut(20));
        container.add(title);
        container.add(Box.createVerticalStrut(20));
        container.add(lblPin);
        container.add(Box.createVerticalStrut(10));
        container.add(txtPin);
        container.add(Box.createVerticalStrut(10));
        container.add(lblError);
        container.add(lblTries);
        container.add(Box.createVerticalStrut(25));
        container.add(btnLogin);
        container.add(Box.createVerticalStrut(15));
        container.add(btnRegister);
        container.add(Box.createVerticalStrut(20));
        container.add(btnForgot);

        add(container);
        
        loadAvailableCards();
    }

    private void loadAvailableCards() {
        cboCards.removeAllItems();
        cboCards.addItem("-- Chọn thẻ --");
        
        selectedCardId = null;
        txtPin.setEnabled(false);
        txtPin.setText("");
        lblError.setText(" ");
        lblCardStatus.setText("📋 Chưa chọn thẻ");
        lblCardStatus.setForeground(Color.GRAY);
        lblTries.setText("Chọn thẻ để tiếp tục");

        // Load danh sách thẻ từ thư mục cards
        File cardsDir = new File("cards");
        if (cardsDir.exists() && cardsDir.isDirectory()) {
            File[] files = cardsDir.listFiles((dir, name) -> 
                name.startsWith("card_") && name.endsWith(".dat"));

            if (files != null && files.length > 0) {
                for (File file : files) {
                    String fileName = file.getName();
                    // Extract card ID: card_GYM1234.dat -> GYM1234
                    String cardId = fileName.substring(5, fileName.length() - 4);
                    cboCards.addItem(cardId);
                }
            } else {
                lblCardStatus.setText("⚠️ Chưa có thẻ nào được đăng ký");
                lblCardStatus.setForeground(new Color(241, 196, 15));
            }
        }
    }

    private void onCardSelected() {
        String selected = (String) cboCards.getSelectedItem();
        
        if (selected == null || selected.startsWith("--")) {
            selectedCardId = null;
            txtPin.setEnabled(false);
            lblCardStatus.setText("📋 Chưa chọn thẻ");
            lblCardStatus.setForeground(Color.GRAY);
            lblTries.setText("Chọn thẻ để tiếp tục");
            return;
        }

        selectedCardId = selected;
        
        // Reset và load thẻ đã chọn
        mainFrame.getCardService().reset();
        
        // *** QUAN TRỌNG: Load thẻ cụ thể theo ID ***
        if (!mainFrame.getCardService().loadCardById(selectedCardId)) {
            lblCardStatus.setText("❌ Không thể load thẻ!");
            lblCardStatus.setForeground(new Color(231, 76, 60));
            txtPin.setEnabled(false);
            return;
        }

        // Kiểm tra trạng thái thẻ sau khi load
        int tries = mainFrame.getCardService().getPinTriesRemaining();
        
        if (tries <= 0) {
            lblCardStatus.setText("🔒 THẺ " + selectedCardId + " ĐÃ BỊ KHÓA!");
            lblCardStatus.setForeground(new Color(231, 76, 60));
            lblTries.setText("Dùng 'Quên PIN?' để mở khóa");
            lblTries.setForeground(new Color(231, 76, 60));
            txtPin.setEnabled(false);
        } else {
            lblCardStatus.setText("✅ Đã chọn thẻ: " + selectedCardId);
            lblCardStatus.setForeground(new Color(46, 204, 113));
            lblTries.setText("Còn " + tries + " lần thử PIN");
            lblTries.setForeground(tries <= 2 ? new Color(241, 196, 15) : Color.WHITE);
            txtPin.setEnabled(true);
            txtPin.requestFocus();
        }
    }

    private void doLogin() {
        if (selectedCardId == null) {
            showError("Vui lòng chọn thẻ trước!");
            return;
        }

        String pin = new String(txtPin.getPassword());

        if (pin.length() != 6 || !pin.matches("\\d{6}")) {
            showError("PIN phải đúng 6 chữ số!");
            return;
        }

        // *** Thẻ đã được load sẵn, CHỈ CẦN VERIFY PIN ***
        if (mainFrame.getCardService().verifyPIN(pin)) {
            lblError.setText(" ");

            // Lấy thông tin từ SmartCard
            String cardId = mainFrame.getCardService().getCardId();
            String info = mainFrame.getCardService().getInfo();
            
            String name = "Khách hàng";
            String phone = "";
            
            if (info != null && !info.isEmpty()) {
                String[] parts = info.split("\\|", -1);
                if (parts.length >= 1 && !parts[0].isEmpty()) name = parts[0];
                if (parts.length >= 2 && !parts[1].isEmpty()) phone = parts[1];
            }

            // Kiểm tra có phải đổi PIN lần đầu không
            if (mainFrame.getCardService().isMustChangePIN()) {
                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn cần đổi PIN lần đầu tiên.\nĐổi PIN ngay bây giờ?",
                    "Đổi PIN bắt buộc",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    mainFrame.setPendingLoginForChangePin(cardId, name, phone);
                    mainFrame.showScreen(MainFrame.SCREEN_CHANGE_PIN);
                    txtPin.setText("");
                    return;
                }
            }

            // Đăng nhập thành công
            mainFrame.onLoginSuccess(cardId, name, phone);
            txtPin.setText("");

        } else {
            // Verify thất bại
            int tries = mainFrame.getCardService().getPinTriesRemaining();
            
            if (tries <= 0) {
                lblCardStatus.setText("🔒 THẺ " + selectedCardId + " ĐÃ BỊ KHÓA!");
                lblCardStatus.setForeground(new Color(231, 76, 60));
                lblTries.setText("Thẻ bị khóa do nhập sai 5 lần!");
                lblTries.setForeground(new Color(231, 76, 60));
                txtPin.setEnabled(false);
                
                JOptionPane.showMessageDialog(this,
                    "<html><center>" +
                    "<h3>🔒 THẺ ĐÃ BỊ KHÓA!</h3>" +
                    "<p>Thẻ <b>" + selectedCardId + "</b> đã bị khóa</p>" +
                    "<p>do nhập sai PIN quá 5 lần.</p>" +
                    "<br>" +
                    "<p>Vui lòng sử dụng chức năng <b>'Quên PIN?'</b></p>" +
                    "<p>với số điện thoại đã đăng ký để mở khóa.</p>" +
                    "</center></html>",
                    "Thẻ bị khóa",
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                lblTries.setText("⚠�� Còn " + tries + " lần thử");
                lblTries.setForeground(tries <= 2 ? new Color(231, 76, 60) : new Color(241, 196, 15));
                
                if (tries <= 2) {
                    showError("PIN KHÔNG ĐÚNG!\nCẢNH BÁO: Chỉ còn " + tries + " lần thử!\nThẻ sẽ bị khóa nếu sai thêm " + tries + " lần nữa.");
                } else {
                    showError("PIN không đúng! Còn " + tries + " lần thử.");
                }
            }
            
            txtPin.setText("");
            // Không reset thẻ để giữ trạng thái tries
        }
    }

    private void showError(String msg) {
        lblError.setText(msg.contains("\n") ? msg.split("\n")[0] : msg);
        if (msg.contains("\n")) {
            JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
        if (txtPin.isEnabled()) {
            txtPin.requestFocus();
        }
    }

    public void onShow() {
        loadAvailableCards();
        txtPin.setText("");
        lblError.setText(" ");
    }
}