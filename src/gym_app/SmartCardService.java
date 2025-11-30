package gym_app;

import java.util.Arrays;
import java.util.Random;

/**
 * SmartCardService - Giả lập JavaCard Applet thegym
 * Hỗ trợ đầy đủ 14 INS codes
 * 
 * Chế độ: SIMULATION (không cần thẻ thật)
 * Khi có thẻ thật: Đổi sang dùng javax.smartcardio
 */
public class SmartCardService {

    // ====================== INS CODES (khớp với applet) ======================
    private static final byte CLA                            = (byte) 0x80;
    private static final byte INS_VERIFY_PIN                 = (byte) 0x10;
    private static final byte INS_CHANGE_PIN                 = (byte) 0x11;
    private static final byte INS_UNBLOCK_AND_GEN_NEW_PIN    = (byte) 0x12;
    private static final byte INS_REGISTER_NEW_CARD          = (byte) 0x20;
    private static final byte INS_GET_RANDOM_PIN             = (byte) 0x21;
    private static final byte INS_UPDATE_INFO                = (byte) 0x30;
    private static final byte INS_GET_INFO                   = (byte) 0x31;
    private static final byte INS_EDIT_INFO                  = (byte) 0x32;
    private static final byte INS_UPLOAD_AVATAR              = (byte) 0x40;
    private static final byte INS_GET_AVATAR                 = (byte) 0x41;
    private static final byte INS_TOPUP                      = (byte) 0x50;
    private static final byte INS_GET_BALANCE                = (byte) 0x51;
    private static final byte INS_CHECK_IN                   = (byte) 0x52;
    private static final byte INS_SIGN_TRANSACTION           = (byte) 0x60;

    // ====================== CONFIG ======================
    private static final int PIN_TRY_LIMIT = 5;
    private static final int PIN_SIZE = 6;
    private static final int AVATAR_MAX_SIZE = 1024;
    private static final int INFO_MAX_SIZE = 256;

    // ====================== TRẠNG THÁI THẺ (giả lập EEPROM) ======================
    private String currentPIN = null;           // PIN hiện tại (null = chưa đăng ký)
    private String tempGeneratedPIN = null;     // PIN tạm (để lấy sau register/unblock)
    private int pinTriesRemaining = PIN_TRY_LIMIT;
    private boolean pinVerified = false;
    private boolean mustChangePIN = true;
    private boolean cardRegistered = false;
    
    private String recoveryPhone = null;        // SĐT khôi phục
    private long balance = 0;                   // Số dư
    
    private byte[] encryptedInfo = null;        // Thông tin cá nhân (mã hóa)
    private byte[] avatar = null;               // Ảnh đại diện (mã hóa)
    
    // Giả lập Master Key (trong thực tế nằm trong RAM của thẻ)
    private byte[] masterKey = new byte[16];

    // ====================== CONSTRUCTOR ======================
    public SmartCardService() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  SMARTCARD SERVICE - CHẾ ĐỘ GIẢ LẬP (SIMULATION)      ║");
        System.out.println("║  Hỗ trợ đầy đủ 14 INS codes như applet thật           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ====================== 0x20: ĐĂNG KÝ THẺ MỚI ======================
    /**
     * INS 0x20 - Đăng ký thẻ mới, sinh PIN ngẫu nhiên 6 số
     * @return PIN 6 số hoặc null nếu thẻ đã đăng ký
     */
    public String registerNewCard() {
        if (cardRegistered && pinVerified) {
            System.out.println("[CARD] ❌ Thẻ đã được đăng ký và kích hoạt!");
            return null;
        }

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

        System.out.println("[CARD] ✅ Đăng ký thành công!");
        System.out.println("[CARD] 🔑 PIN mặc định: " + currentPIN);
        System.out.println("[CARD] ⚠️  Bắt buộc đổi PIN lần đầu!");

        return currentPIN;
    }

    // ====================== 0x21: LẤY PIN ĐÃ SINH ======================
    /**
     * INS 0x21 - Lấy PIN đã sinh (chỉ lấy được 1 lần sau register/unblock)
     * @return PIN 6 số hoặc null
     */
    public String getGeneratedPIN() {
        if (tempGeneratedPIN == null) {
            System.out.println("[CARD] ❌ Không có PIN tạm để lấy!");
            return null;
        }

        String pin = tempGeneratedPIN;
        tempGeneratedPIN = null; // Xóa sau khi lấy (bảo mật)
        System.out.println("[CARD] 🔑 PIN đã lấy: " + pin);
        return pin;
    }

    // ====================== 0x10: XÁC THỰC PIN ======================
    /**
     * INS 0x10 - Xác thực PIN
     * @param pin6 PIN 6 số
     * @return true nếu đúng, false nếu sai
     */
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
            return true;
        } else {
            pinTriesRemaining--;
            pinVerified = false;
            System.out.println("[CARD] ❌ PIN sai! Còn " + pinTriesRemaining + " lần thử.");
            return false;
        }
    }

    /**
     * Kiểm tra trạng thái phải đổi PIN
     */
    public boolean isMustChangePIN() {
        return mustChangePIN;
    }

    /**
     * Lấy số lần thử PIN còn lại
     */
    public int getPinTriesRemaining() {
        return pinTriesRemaining;
    }

    // ====================== 0x11: ĐỔI PIN ======================
    /**
     * INS 0x11 - Đổi PIN mới
     * @param newPin6 PIN mới 6 số
     * @return true nếu thành công
     */
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
        return true;
    }

    // ====================== 0x12: UNBLOCK & SINH PIN MỚI ======================
    /**
     * INS 0x12 - Mở khóa thẻ bằng SĐT khôi phục, sinh PIN mới
     * @param phone SĐT khôi phục
     * @return PIN mới 6 số hoặc null
     */
    public String unblockAndGenerateNewPIN(String phone) {
        if (recoveryPhone == null || recoveryPhone.isEmpty()) {
            System.out.println("[CARD] ❌ Chưa đăng ký SĐT khôi phục!");
            return null;
        }

        if (!recoveryPhone.equals(phone)) {
            System.out.println("[CARD] ❌ SĐT khôi phục không đúng!");
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
        return currentPIN;
    }

    /**
     * Đăng ký SĐT khôi phục (gọi sau khi update info)
     */
    public void setRecoveryPhone(String phone) {
        this.recoveryPhone = phone;
        System.out.println("[CARD] 📱 Đã lưu SĐT khôi phục: " + phone);
    }

    // ====================== 0x30: CẬP NHẬT THÔNG TIN ======================
    /**
     * INS 0x30 - Cập nhật thông tin cá nhân (lưu mã hóa)
     * @param info Thông tin dạng JSON hoặc text
     * @return true nếu thành công
     */
    public boolean updateInfo(String info) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (info == null || info.length() > INFO_MAX_SIZE) {
            System.out.println("[CARD] ❌ Thông tin không hợp lệ hoặc quá dài!");
            return false;
        }

        // Giả lập mã hóa AES (trong applet thật dùng aesCipher)
        encryptedInfo = fakeEncrypt(info.getBytes());
        System.out.println("[CARD] ✅ Đã lưu thông tin (" + info.length() + " bytes)");
        return true;
    }

    // ====================== 0x31: LẤY THÔNG TIN ======================
    /**
     * INS 0x31 - Lấy thông tin cá nhân (giải mã)
     * @return Thông tin hoặc null
     */
    public String getInfo() {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        if (encryptedInfo == null) {
            System.out.println("[CARD] ❌ Chưa có thông tin!");
            return null;
        }

        byte[] decrypted = fakeDecrypt(encryptedInfo);
        String info = new String(decrypted).trim();
        System.out.println("[CARD] 📄 Thông tin: " + info);
        return info;
    }

    // ====================== 0x32: SỬA THÔNG TIN ======================
    /**
     * INS 0x32 - Sửa thông tin (không thay đổi SĐT khôi phục)
     * @param info Thông tin mới
     * @return true nếu thành công
     */
    public boolean editInfo(String info) {
        // Giống updateInfo nhưng không đổi recoveryPhone
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (info == null || info.length() > INFO_MAX_SIZE) {
            System.out.println("[CARD] ❌ Thông tin không hợp lệ!");
            return false;
        }

        encryptedInfo = fakeEncrypt(info.getBytes());
        System.out.println("[CARD] ✅ Đã sửa thông tin");
        return true;
    }

    // ====================== 0x40: UPLOAD AVATAR ======================
    /**
     * INS 0x40 - Upload ảnh đại diện (tối đa 1024 bytes)
     * @param avatarData Dữ liệu ảnh (đã resize/compress)
     * @return true nếu thành công
     */
    public boolean uploadAvatar(byte[] avatarData) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (avatarData == null || avatarData.length > AVATAR_MAX_SIZE) {
            System.out.println("[CARD] ❌ Ảnh không hợp lệ hoặc quá lớn (max 1KB)!");
            return false;
        }

        // Mã hóa và lưu
        avatar = fakeEncrypt(avatarData);
        System.out.println("[CARD] 🖼️ Đã lưu avatar (" + avatarData.length + " bytes)");
        return true;
    }

    // ====================== 0x41: LẤY AVATAR ======================
    /**
     * INS 0x41 - Lấy ảnh đại diện
     * @return Dữ liệu ảnh hoặc null
     */
    public byte[] getAvatar() {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        if (avatar == null) {
            System.out.println("[CARD] ❌ Chưa có avatar!");
            return null;
        }

        byte[] decrypted = fakeDecrypt(avatar);
        System.out.println("[CARD] 🖼️ Lấy avatar (" + decrypted.length + " bytes)");
        return decrypted;
    }

    // ====================== 0x50: NẠP TIỀN ======================
    /**
     * INS 0x50 - Nạp tiền vào thẻ
     * @param amount Số tiền (VNĐ)
     * @return true nếu thành công
     */
    public boolean topup(int amount) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return false;
        }

        if (amount <= 0) {
            System.out.println("[CARD] ❌ Số tiền không hợp lệ!");
            return false;
        }

        // Kiểm tra overflow (max ~2 tỷ với long)
        if (balance + amount < balance) {
            System.out.println("[CARD] ❌ Số dư vượt quá giới hạn!");
            return false;
        }

        balance += amount;
        System.out.println("[CARD] 💰 Nạp " + formatMoney(amount) + " → Số dư: " + formatMoney(balance));
        return true;
    }

    // ====================== 0x51: LẤY SỐ DƯ ======================
    /**
     * INS 0x51 - Lấy số dư hiện tại
     * @return Số dư (VNĐ)
     */
    public long getBalance() {
        return balance;
    }

    // ====================== 0x52: CHECK-IN ======================
    /**
     * INS 0x52 - Check-in vào phòng gym
     * @return true nếu thành công
     */
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
    /**
     * INS 0x60 - Ký giao dịch RSA
     * @param type Loại giao dịch (0x01=topup, 0x02=mua gói...)
     * @param amount Số tiền
     * @return Chữ ký (giả lập)
     */
    public byte[] signTransaction(byte type, int amount) {
        if (!pinVerified) {
            System.out.println("[CARD] ❌ Chưa xác thực PIN!");
            return null;
        }

        // Giả lập chữ ký RSA
        // Trong applet thật: rsaSigner.sign(data, ...)
        String sigData = String.format("SIG|%02X|%d|%d|%d", 
            type, amount, balance, System.currentTimeMillis());
        
        System.out.println("[CARD] ✍️ Đã ký giao dịch: type=" + type + ", amount=" + amount);
        return sigData.getBytes();
    }

    // ====================== TRỪ TIỀN (cho mua gói) ======================
    /**
     * Trừ tiền khi mua gói tập
     * @param amount Số tiền cần trừ
     * @return true nếu đủ tiền và trừ thành công
     */
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
        return true;
    }

    // ====================== UTILITY ======================
    
    /**
     * Reset thẻ về trạng thái ban đầu (để test)
     */
    public void reset() {
        currentPIN = null;
        tempGeneratedPIN = null;
        pinTriesRemaining = PIN_TRY_LIMIT;
        pinVerified = false;
        mustChangePIN = true;
        cardRegistered = false;
        recoveryPhone = null;
        balance = 0;
        encryptedInfo = null;
        avatar = null;
        System.out.println("[CARD] 🔄 Đã reset thẻ!");
    }

    /**
     * Kiểm tra thẻ đã đăng ký chưa
     */
    public boolean isCardRegistered() {
        return cardRegistered;
    }

    /**
     * Kiểm tra đã xác thực PIN chưa
     */
    public boolean isPinVerified() {
        return pinVerified;
    }

    /**
     * Format tiền VNĐ
     */
    private String formatMoney(long amount) {
        return String.format("%,d VNĐ", amount);
    }

    // ====================== GIẢ LẬP MÃ HÓA ======================
    // Trong applet thật: dùng AES với masterKey
    
    private byte[] fakeEncrypt(byte[] data) {
        // XOR đơn giản với masterKey (CHỈ ĐỂ TEST!)
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte)(data[i] ^ masterKey[i % masterKey.length]);
        }
        return result;
    }

    private byte[] fakeDecrypt(byte[] data) {
        // XOR ngược lại
        return fakeEncrypt(data); // XOR 2 lần = về ban đầu
    }

    // ====================== DEBUG ======================
    
    /**
     * In trạng thái thẻ (debug)
     */
    public void printStatus() {
        System.out.println("\n╔═══════════════ TRẠNG THÁI THẺ ═══════════════╗");
        System.out.println("║ Đã đăng ký:      " + (cardRegistered ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ PIN verified:    " + (pinVerified ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ Phải đổi PIN:    " + (mustChangePIN ? "⚠️ Có" : "✅ Không"));
        System.out.println("║ Số lần thử PIN:  " + pinTriesRemaining + "/" + PIN_TRY_LIMIT);
        System.out.println("║ Số dư:           " + formatMoney(balance));
        System.out.println("║ Có thông tin:    " + (encryptedInfo != null ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ Có avatar:       " + (avatar != null ? "✅ Có" : "❌ Chưa"));
        System.out.println("║ SĐT khôi phục:   " + (recoveryPhone != null ? recoveryPhone : "Chưa đăng ký"));
        System.out.println("╚══════════════════════════════════════════════╝\n");
    }

    // ====================== MAIN TEST ======================
    public static void main(String[] args) {
        SmartCardService card = new SmartCardService();
        
        System.out.println("\n========== TEST SMARTCARD SERVICE ==========\n");
        
        // Test 1: Đăng ký
        System.out.println("--- Test 1: Đăng ký thẻ mới ---");
        String pin = card.registerNewCard();
        card.printStatus();
        
        // Test 2: Verify PIN
        System.out.println("--- Test 2: Verify PIN ---");
        card.verifyPIN(pin);
        card.printStatus();
        
        // Test 3: Đổi PIN
        System.out.println("--- Test 3: Đổi PIN ---");
        card.changePIN("654321");
        card.printStatus();
        
        // Test 4: Nạp tiền
        System.out.println("--- Test 4: Nạp tiền ---");
        card.topup(500000);
        card.topup(300000);
        System.out.println("Số dư: " + card.getBalance());
        
        // Test 5: Update info
        System.out.println("--- Test 5: Update thông tin ---");
        card.updateInfo("{\"name\":\"Nguyễn Văn A\",\"phone\":\"0901234567\"}");
        card.setRecoveryPhone("0901234567");
        System.out.println("Info: " + card.getInfo());
        
        // Test 6: Sign transaction
        System.out.println("--- Test 6: Ký giao dịch ---");
        byte[] sig = card.signTransaction((byte)0x01, 500000);
        System.out.println("Signature: " + new String(sig));
        
        // Test 7: Check-in
        System.out.println("--- Test 7: Check-in ---");
        card.checkIn();
        
        card.printStatus();
        
        System.out.println("\n========== TEST HOÀN TẤT ==========");
    }
}