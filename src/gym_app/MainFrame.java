package gym_app;

import gym_app.panels.*;
import javax.swing.*;
import java.awt.*;

/**
 * MainFrame - Khung chính của ứng dụng
 * Sử dụng CardLayout để chuyển đổi giữa các màn hình
 */
public class MainFrame extends JFrame {

    // Services
    private SmartCardService cardService;
    private DatabaseService dbService;

    // CardLayout để chuyển màn hình
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Các màn hình
    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_REGISTER = "REGISTER";
    public static final String SCREEN_DASHBOARD = "DASHBOARD";
    public static final String SCREEN_TOPUP = "TOPUP";
    public static final String SCREEN_PACKAGES = "PACKAGES";
    public static final String SCREEN_BUY_PACKAGE = "BUY_PACKAGE";
    public static final String SCREEN_PROFILE = "PROFILE";
    public static final String SCREEN_CHANGE_PIN = "CHANGE_PIN";
    public static final String SCREEN_UNBLOCK = "UNBLOCK";
    public static final String SCREEN_HISTORY = "HISTORY";
    public static final String SCREEN_CHECKIN = "CHECKIN";

    // Thông tin user hiện tại
    private String currentCardId;
    private String currentName;
    private String currentPhone;

    // Panels (để có thể refresh)
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private PackageListPanel packageListPanel;

    public MainFrame() {
        // Khởi tạo services
        cardService = new SmartCardService();
        dbService = new DatabaseService();

        initUI();
        initPanels();
    }

    private void initUI() {
        setTitle("⚡ POWER GYM - HỆ THỐNG QUẢN LÝ THẺ THÀNH VIÊN");
        setSize(1300, 800);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Icon (nếu có)
        // setIconImage(new ImageIcon("icon.png").getImage());

        // CardLayout container
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(30, 30, 45));

        add(mainContainer);
    }

    private void initPanels() {
        // Màn hình đăng nhập/đăng ký
        mainContainer.add(new LoginPanel(this), SCREEN_LOGIN);
        mainContainer.add(new RegisterPanel(this), SCREEN_REGISTER);

        // Màn hình chính (sẽ tạo sau khi đăng nhập)
        dashboardPanel = new DashboardPanel(this);
        mainContainer.add(dashboardPanel, SCREEN_DASHBOARD);

        // Các màn hình chức năng
        mainContainer.add(new TopupPanel(this), SCREEN_TOPUP);
        
        packageListPanel = new PackageListPanel(this);
        mainContainer.add(packageListPanel, SCREEN_PACKAGES);
        
        mainContainer.add(new BuyPackagePanel(this), SCREEN_BUY_PACKAGE);
        mainContainer.add(new ProfileEditPanel(this), SCREEN_PROFILE);
        mainContainer.add(new ChangePinPanel(this), SCREEN_CHANGE_PIN);
        mainContainer.add(new UnblockPinPanel(this), SCREEN_UNBLOCK);
        
        historyPanel = new HistoryPanel(this);
        mainContainer.add(historyPanel, SCREEN_HISTORY);
        
        mainContainer.add(new CheckinPanel(this), SCREEN_CHECKIN);

        // Bắt đầu từ màn hình Login
        showScreen(SCREEN_LOGIN);
    }

    /**
     * Chuyển đến màn hình khác
     */
    public void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);

        // Refresh data khi chuyển màn hình
        if (screenName.equals(SCREEN_DASHBOARD)) {
            dashboardPanel.refreshData();
        } else if (screenName.equals(SCREEN_HISTORY)) {
            historyPanel.loadHistory();
        } else if (screenName.equals(SCREEN_PACKAGES)) {
            packageListPanel.loadPackages();
        }
    }

    /**
     * Đăng nhập thành công - lưu thông tin user
     */
    public void onLoginSuccess(String cardId, String name, String phone) {
        this.currentCardId = cardId;
        this.currentName = name;
        this.currentPhone = phone;
        
        dashboardPanel.setUserInfo(cardId, name, phone);
        showScreen(SCREEN_DASHBOARD);
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        currentCardId = null;
        currentName = null;
        currentPhone = null;
        cardService.reset();
        showScreen(SCREEN_LOGIN);
    }

    // ==================== GETTERS ====================

    public SmartCardService getCardService() {
        return cardService;
    }

    public DatabaseService getDbService() {
        return dbService;
    }

    public String getCurrentCardId() {
        return currentCardId;
    }

    public String getCurrentName() {
        return currentName;
    }

    public String getCurrentPhone() {
        return currentPhone;
    }

    public void setCurrentName(String name) {
        this.currentName = name;
        dashboardPanel.setUserInfo(currentCardId, name, currentPhone);
    }

    public void setCurrentPhone(String phone) {
        this.currentPhone = phone;
        dashboardPanel.setUserInfo(currentCardId, currentName, phone);
    }
}