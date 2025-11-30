package gym_app;

import java.io.*;
import java.util.*;

/**
 * SmartCardService - Giả lập JavaCard Applet thegym
 * Hỗ trợ nhiều thẻ, mỗi thẻ 1 file riêng
 * Đăng nhập bằng PIN để tìm thẻ tương ứng
 */
public class SmartCardService {

    // ====================== CONFIG ======================
    private static final int PIN_TRY_LIMIT = 5;
    private static final int PIN_SIZE = 6;
    private static final int AVATAR_MAX_SIZE = 10240;
    private static final int INFO_MAX_SIZE = 256;
    
    // Thư mục lưu các thẻ
    private static final String CARDS_FOLDER = "cards";
    private static final String CARD_FILE_PREFIX = "card_";
    private static final String CARD_FILE_EXT = ".dat";

    // ====================== TRẠNG THÁI THẺ HIỆN TẠI ======================
    private String currentPIN = null;
    private String tempGeneratedPIN = null;
    private int pinTriesRemaining = PIN_TRY_LIMIT;
    private boolean pinVerified = false;
    private boolean mustChangePIN = true;
    private boolean cardRegistered = false;
    
    private String cardId = null;
    private String recoveryPhone = null;
    private long balance = 0;
    
    private String encryptedInfo = null;
    private byte[] avatar = null;
    
    private byte[] masterKey = new byte[16];
    
    // File của thẻ hiện tại đang được sử dụng
    private String currentCardFileName = null;

    // ====================== CONSTRUCTOR ======================
    public SmartCardService() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  SMARTCARD SERVICE - CHẾ ĐỘ GIẢ LẬP (SIMULATION)      ║");
        System.out.println("║  Hỗ trợ nhiều thẻ - Đăng nhập bằng PIN                 ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        // Tạo thư mục cards nếu chưa có
        File cardsDir = new File(CARDS_FOLDER);
        if (!cardsDir.exists()) {
            cardsDir.mkdir();
            System.out.println("[CARD] 📁 Đã tạo thư mục: " + CARDS_FOLDER);
        }
        
        // KHÔNG tự động load thẻ - chờ người dùng đăng nhập
        System.out.println("[CARD] 📋 Sẵn sàng. Vui lòng đăng nhập hoặc đăng ký.");
    }

    // ====================== TÌM THẺ BẰNG PIN ======================
    
    /**
     * Tìm và load thẻ có PIN khớp
     * @return true nếu tìm thấy
     */
    public boolean findAndLoadCardByPIN(String pin) {
        if (pin == null || pin.length() != PIN_SIZE) {
            System.out.println("[CARD] ❌ PIN phải đúng 6 số!");
            return false;
        }
        
        File cardsDir = new File(CARDS_FOLDER);
        if (!cardsDir.exists()) {
            System.out.println("[CARD] ❌ Chưa có thẻ nào!");
            return false;
        }
        
        File[] files = cardsDir.listFiles((dir, name) -> 
            name.startsWith(CARD_FILE_PREFIX) && name.endsWith(CARD_FILE_EXT));
        
        if (files == null || files.length == 0) {
            System.out.println("[CARD] ❌ Chưa có thẻ nào được đăng ký!");
            return false;
        }
        
        // Duyệt qua tất cả các thẻ để tìm PIN khớp
        for (File file : files) {
            CardData data = loadCardDataFromFile(file.getAbsolutePath());
            if (data != null && data.currentPIN != null && data.currentPIN.equals(pin)) {
                // Tìm thấy! Load thẻ này
                applyCardData(data);
                this.currentCardFileName = file.getAbsolutePath();
                
                System.out.println("[CARD] ✅ Tìm thấy thẻ: " + cardId);
                System.out.println("[CARD] 📋 Số dư: " + formatMoney(balance));
                return true;
            }
        }
        
        System.out.println("[CARD] ❌ Không tìm thấy thẻ với PIN này!");
        return false;
    }
    
    public boolean loadCardById(String cardId) {
    if (cardId == null || cardId.isEmpty()) {
        System.out.println("[CARD] ❌ Card ID không hợp lệ!");
        return false;
    }
    
    String fileName = CARDS_FOLDER + File.separator + CARD_FILE_PREFIX + cardId + CARD_FILE_EXT;
    File cardFile = new File(fileName);
    
    if (!cardFile.exists()) {
        System.out.println("[CARD] ❌ Không tìm thấy thẻ: " + cardId);
        return false;
    }
    
    CardData data = loadCardDataFromFile(fileName);
    if (data != null) {
        applyCardData(data);
        this.currentCardFileName = fileName;
        
        System.out.println("[CARD] ✅ Đã load thẻ: " + cardId);
        System.out.println("[CARD] 📋 Trạng thái: " + (cardRegistered ? "Đã đăng ký" : "Chưa đăng ký"));
        System.out.println("[CARD] 🔐 Số lần thử PIN còn: " + pinTriesRemaining);
        
        return true;
    }
    
    System.out.println("[CARD] ❌ Không thể load thẻ: " + cardId);
    return false;
}
    
    /**
     * Kiểm tra SĐT đã được đăng ký chưa
     */
    public boolean isPhoneRegistered(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        File cardsDir = new File(CARDS_FOLDER);
        if (!cardsDir.exists()) {
            return false;
        }
        
        File[] files = cardsDir.listFiles((dir, name) -> 
            name.startsWith(CARD_FILE_PREFIX) && name.endsWith(CARD_FILE_EXT));
        
        if (files == null) {
            return false;
        }
        
        for (File file : files) {
            CardData data = loadCardDataFromFile(file.getAbsolutePath());
            if (data != null && data.recoveryPhone != null && data.recoveryPhone.equals(phone)) {
                System.out.println("[CARD] ⚠️ SĐT " + phone + " đã được đăng ký!");
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * Tìm và load thẻ bằng SĐT (để khôi phục PIN)
     */
    public boolean findAndLoadCardByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        File cardsDir = new File(CARDS_FOLDER);
        if (!cardsDir.exists()) {
            return false;
        }
        
        File[] files = cardsDir.listFiles((dir, name) -> 
            name.startsWith(CARD_FILE_PREFIX) && name.endsWith(CARD_FILE_EXT));
        
        if (files == null) {
            return false;
        }
        
        for (File file : files) {
            CardData data = loadCardDataFromFile(file.getAbsolutePath());
            if (data != null && data.recoveryPhone != null && data.recoveryPhone.equals(phone)) {
                // Tìm thấy! Load thẻ này
                applyCardData(data);
                this.currentCardFileName = file.getAbsolutePath();
                
                System.out.println("[CARD] ✅ Tìm thấy thẻ với SĐT: " + phone);
                return true;
            }
        }
        
        System.out.println("[CARD] ❌ Không tìm thấy thẻ với SĐT: " + phone);
        return false;
    }

    // ====================== PERSISTENCE ======================
    
    private void saveCardData() {
        if (currentCardFileName == null) {
            System.out.println("[CARD] ❌ Không có thẻ để lưu!");
            return;
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(currentCardFileName))) {
            
            CardData data = new CardData();
            data.currentPIN = this.currentPIN;
            data.pinTriesRemaining = this.pinTriesRemaining;
            data.mustChangePIN = this.mustChangePIN;
            data.cardRegistered = this.cardRegistered;
            data.cardId = this.cardId;
            data.recoveryPhone = this.recoveryPhone;
            data.balance = this.balance;
            data.encryptedInfo = this.encryptedInfo;
            data.avatar = this.avatar;
            data.masterKey = this.masterKey;
            
            oos.writeObject(data);
            System.out.println("[CARD] 💾 Đã lưu thẻ: " + cardId);
            
        } catch (IOException e) {
            System.out.println("[CARD] ❌ Lỗi lưu: " + e.getMessage());
        }
    }
    
    private CardData loadCardDataFromFile(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (CardData) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void applyCardData(CardData data) {
        this.currentPIN = data.currentPIN;
        this.pinTriesRemaining = data.pinTriesRemaining;
        this.mustChangePIN = data.mustChangePIN;
        this.cardRegistered = data.cardRegistered;
        this.cardId = data.cardId;
        this.recoveryPhone = data.recoveryPhone;
        this.balance = data.balance;
        this.encryptedInfo = data.encryptedInfo;
        this.avatar = data.avatar;
        this.masterKey = data.masterKey != null ? data.masterKey : new byte[16];
        
        // Reset session state
        this.pinVerified = false;
        this.tempGeneratedPIN = null;
    }
    
    private void resetAllData() {
        currentPIN = null;
        tempGeneratedPIN = null;
        pinTriesRemaining = PIN_TRY_LIMIT;
        pinVerified = false;
        mustChangePIN = true;
        cardRegistered = false;
        cardId = null;
        recoveryPhone = null;
        balance = 0;
        encryptedInfo = null;
        avatar = null;
        masterKey = new byte[16];
        currentCardFileName = null;
    }
    
    private static class CardData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        String currentPIN;
        int pinTriesRemaining;
        boolean mustChangePIN;
        boolean cardRegistered;
        String cardId;
        String recoveryPhone;
        long balance;
        String encryptedInfo;
        byte[] avatar;
        byte[] masterKey;
    }

    // ====================== CARD ID ======================
    public void setCardId(String cardId) {
        this.cardId = cardId;
        if (currentCardFileName != null) {
            saveCardData();
        }
    }
    
    public String getCardId() {
        return cardId;
    }

    // ====================== 0x20: ĐĂNG KÝ THẺ MỚI ======================
    public String registerNewCard() {
        // Tạo card ID mới
        String newCardId = "GYM" + System.currentTimeMillis() % 1000000;
        
        // Reset tất cả dữ liệu
        resetAllData();
        
        // Sinh PIN ngẫu nhiên 6 số
        Random r = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < PIN_SIZE; i++) {
            pin.append(r.nextInt(10));
        }

        // Sinh Master Key ngẫu nhiên
        r.nextBytes(masterKey);

        currentPIN = pin.toString();
        tempGeneratedPIN = pin.toString();
        cardRegistered = true;
        mustChangePIN = true;
        pinVerified = false;
        pinTriesRemaining = PIN_TRY_LIMIT;
        balance = 0;
        cardId = newCardId;
        
        // Tạo file mới cho thẻ này
        currentCardFileName = CARDS_FOLDER + File.separator + CARD_FILE_PREFIX + newCardId + CARD_FILE_EXT;

        System.out.println("[CARD] ✅ Đăng ký thành công!");
        System.out.println("[CARD] 🆔 Card ID: " + newCardId);
        System.out.println("[CARD] 🔑 PIN mặc định: " + currentPIN);
        System.out.println("[CARD] ⚠️  Bắt buộc đổi PIN lần đầu!");

        // Lưu vào file
        saveCardData();

        return currentPIN;
    }

    // ====================== 0x21: LẤY PIN ĐÃ SINH ======================
    public String getGeneratedPIN() {
        if (tempGeneratedPIN == null) {
            System.out.println("[CARD] ❌ Không có PIN tạm để lấy!");
            return null;
        }

        String pin = tempGeneratedPIN;
        tempGeneratedPIN = null;
        System.out.println("[CARD] 🔑 PIN đã lấy: " + pin);
        return pin;
    }

    // ====================== 0x10: XÁC THỰC PIN ======================
    public boolean verifyPIN(String pin6) {
        if (!cardRegistered) {
            System.out.println("[CARD] ❌ Thẻ chưa đăng ký!");
            return false;
        }

        if (pin6 == null || pin6.length() != PIN_SIZE) {
            System.out.println("[CARD] ❌ PIN phải đúng 6 số!");
            return false;
        }

        if (pinTriesRemaining <= 0) {
            System.out.println("[CARD] 🔒 THẺ ĐÃ BỊ KHÓA! Cần unblock.");
            return false;
        }

        if (currentPIN.equals(pin6)) {
            pinVerified = true;
            pinTriesRemaining = PIN_TRY_LIMIT;
            System.out.println("[CARD] ✅ Xác thực PIN thành công!");
            
            if (mustChangePIN) {
                System.out.println("[CARD] ⚠️  Cần đổi PIN lần đầu! (SW=9C10)");
            }
            
            saveCardData();
            return true;
        } else {
            pinTriesRemaining--;
            pinVerified = false;
            System.out.println("[CARD] ❌ PIN sai! Còn " + pinTriesRemaining + " lần thử.");
            
            saveCardData();
            return false;
        }
    }

    public boolean isMustChangePIN() {
        return mustChangePIN;
    }

    public int getPinTriesRemaining() {
        return pinTriesRemaining;
    }

    // ====================== 0x11: ĐỔI PIN ======================
    public boolean changePIN(String newPin6) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN cũ!");
            return false;
        }

        if (newPin6 == null || newPin6.length() != PIN_SIZE || !newPin6.matches("\\d{6}")) {
            System.out.println("[CARD] ❌ PIN mới phải đúng 6 chữ số!");
            return false;
        }

        currentPIN = newPin6;
        mustChangePIN = false;
        System.out.println("[CARD] ✅ Đổi PIN thành công: " + currentPIN);
        
        saveCardData();
        return true;
    }

    // ====================== 0x12: UNBLOCK & SINH PIN MỚI ======================
    public String unblockAndGenerateNewPIN(String phone) {
        if (recoveryPhone == null || recoveryPhone.isEmpty()) {
            System.out.println("[CARD] ❌ Chưa đăng ký SĐT khôi phục!");
            return null;
        }

        if (!recoveryPhone.equals(phone)) {
            System.out.println("[CARD] ❌ SĐT khôi phục không đúng!");
            System.out.println("[CARD] Expected: " + recoveryPhone + ", Got: " + phone);
            return null;
        }

        // Sinh PIN mới
        Random r = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < PIN_SIZE; i++) {
            pin.append(r.nextInt(10));
        }

        currentPIN = pin.toString();
        tempGeneratedPIN = pin.toString();
        pinTriesRemaining = PIN_TRY_LIMIT;
        mustChangePIN = true;
        pinVerified = false;

        System.out.println("[CARD] ✅ Unblock thành công!");
        System.out.println("[CARD] 🔑 PIN mới: " + currentPIN);
        
        saveCardData();
        return currentPIN;
    }

    public void setRecoveryPhone(String phone) {
        this.recoveryPhone = phone;
        System.out.println("[CARD] 📱 Đã lưu SĐT khôi phục: " + phone);
        if (currentCardFileName != null) {
            saveCardData();
        }
    }
    
    public String getRecoveryPhone() {
        return recoveryPhone;
    }

    // ====================== 0x30: CẬP NHẬT THÔNG TIN ======================
    public boolean updateInfo(String info) {
        if (!cardRegistered) {
            System.out.println("[CARD] ❌ Thẻ chưa đăng ký!");
            return false;
        }

        if (info == null || info.length() > INFO_MAX_SIZE) {
            System.out.println("[CARD] ❌ Thông tin không hợp lệ hoặc quá dài!");
            return false;
        }

        encryptedInfo = info;
        System.out.println("[CARD] ✅ Đã lưu thông tin (" + info.length() + " bytes)");
        
        if (currentCardFileName != null) {
            saveCardData();
        }
        return true;
    }

    // ====================== 0x31: LẤY THÔNG TIN ======================
    public String getInfo() {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        if (encryptedInfo == null) {
            System.out.println("[CARD] ❌ Chưa có thông tin!");
            return null;
        }

        return encryptedInfo;
    }

    // ====================== 0x32: SỬA THÔNG TIN ======================
    public boolean editInfo(String info) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (info == null || info.length() > INFO_MAX_SIZE) {
            System.out.println("[CARD] ❌ Thông tin không hợp lệ!");
            return false;
        }

        encryptedInfo = info;
        System.out.println("[CARD] ✅ Đã sửa thông tin");
        
        saveCardData();
        return true;
    }

    // ====================== 0x40: UPLOAD AVATAR ======================
    public boolean uploadAvatar(byte[] avatarData) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (avatarData == null || avatarData.length > AVATAR_MAX_SIZE) {
            System.out.println("[CARD] ❌ Ảnh không hợp lệ hoặc quá lớn (max 1KB)!");
            return false;
        }

        avatar = avatarData.clone();
        System.out.println("[CARD] 🖼️ Đã lưu avatar (" + avatarData.length + " bytes)");
        
        saveCardData();
        return true;
    }

    // ====================== 0x41: LẤY AVATAR ======================
    public byte[] getAvatar() {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        if (avatar == null) {
            return null;
        }

        return avatar.clone();
    }

    // ====================== 0x50: NẠP TIỀN ======================
    public boolean topup(int amount) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (amount <= 0) {
            System.out.println("[CARD] ❌ Số tiền không hợp lệ!");
            return false;
        }

        if (balance + amount < balance) {
            System.out.println("[CARD] ❌ Số dư vượt quá giới hạn!");
            return false;
        }

        balance += amount;
        System.out.println("[CARD] 💰 Nạp " + formatMoney(amount) + " → Số dư: " + formatMoney(balance));
        
        saveCardData();
        return true;
    }

    // ====================== 0x51: LẤY SỐ DƯ ======================
    public long getBalance() {
        return balance;
    }

    // ====================== 0x52: CHECK-IN ======================
    public boolean checkIn() {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        System.out.println("[CARD] 🚪 CHECK-IN thành công! " + 
            java.time.LocalDateTime.now().toString().replace("T", " "));
        return true;
    }

    // ====================== 0x60: KÝ GIAO DỊCH ======================
    public byte[] signTransaction(byte type, int amount) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        String sigData = String.format("SIG|%02X|%d|%d|%d", 
            type, amount, balance, System.currentTimeMillis());
        
        System.out.println("[CARD] ✍️ Đã ký giao dịch: type=" + type + ", amount=" + amount);
        return sigData.getBytes();
    }

    // ====================== TRỪ TIỀN ======================
    public boolean deductBalance(long amount) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (balance < amount) {
            System.out.println("[CARD] ❌ Số dư không đủ! Cần " + formatMoney(amount) + 
                ", hiện có " + formatMoney(balance));
            return false;
        }

        balance -= amount;
        System.out.println("[CARD] 💸 Trừ " + formatMoney(amount) + " → Còn: " + formatMoney(balance));
        
        saveCardData();
        return true;
    }

    // ====================== LOGOUT (Rút thẻ) ======================
    public void logout() {
        System.out.println("[CARD] 📤 Rút thẻ: " + (cardId != null ? cardId : "N/A"));
        resetAllData();
    }

    // ====================== FULL RESET ======================
    public void fullReset() {
        if (currentCardFileName != null) {
            File file = new File(currentCardFileName);
            if (file.exists()) {
                file.delete();
                System.out.println("[CARD] 🗑️ Đã xóa file thẻ: " + currentCardFileName);
            }
        }
        
        resetAllData();
        System.out.println("[CARD] 🔄 Đã reset hoàn toàn thẻ!");
    }
    
    public void reset() {
        logout();
    }

    // ====================== UTILITY ======================

    public boolean isCardRegistered() {
        return cardRegistered;
    }

    public boolean isPinVerified() {
        return pinVerified;
    }

    private String formatMoney(long amount) {
        return String.format("%,d VNĐ", amount);
    }

    // ====================== DEBUG ======================
    
    public void printStatus() {
        System.out.println("\n╔═══════════════ TRẠNG THÁI THẺ ═══════════════╗");
        System.out.println("║ Card File:       " + (currentCardFileName != null ? currentCardFileName : "Chưa chọn"));
        System.out.println("║ Card ID:         " + (cardId != null ? cardId : "Chưa có"));
        System.out.println("║ Đã đăng ký:      " + (cardRegistered ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ PIN hiện tại:    " + (currentPIN != null ? currentPIN : "Chưa có"));
        System.out.println("║ PIN verified:    " + (pinVerified ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ Phải đổi PIN:    " + (mustChangePIN ? "⚠️ Có" : "✅ Không"));
        System.out.println("║ Số lần thử PIN:  " + pinTriesRemaining + "/" + PIN_TRY_LIMIT);
        System.out.println("║ Số dư:           " + formatMoney(balance));
        System.out.println("║ Có thông tin:    " + (encryptedInfo != null ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ Có avatar:       " + (avatar != null ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ SĐT khôi phục:   " + (recoveryPhone != null ? recoveryPhone : "Chưa đăng ký"));
        System.out.println("╚══════════════════════════════════════════════╝\n");
    }
}