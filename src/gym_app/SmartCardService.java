package gym_app;

import javax.smartcardio.*;
import java.util.Random;

public class SmartCardService {

    private String currentPIN = "123456";  // PIN mặc định
    private long balance = 0;

    public SmartCardService() {
        System.out.println("THẺ GIẢ LẬP ĐÃ KHỞI TẠO THÀNH CÔNG (DÙNG ĐỂ TEST NHANHH)");
        System.out.println("PIN mặc định: 123456");
    }

    private ResponseAPDU fakeResponse(byte[] data, int sw) {
        return new ResponseAPDU(appendSW(data != null ? data : new byte[0], sw));
    }

    private byte[] appendSW(byte[] data, int sw) {
        byte[] resp = new byte[data.length + 2];
        System.arraycopy(data, 0, resp, 0, data.length);
        resp[resp.length - 2] = (byte)(sw >> 8);
        resp[resp.length - 1] = (byte)sw;
        return resp;
    }

    // 1. ĐĂNG KÝ → TẠO PIN NGẪU NHIÊN 6 SỐ
    public String registerNewCard() {
        Random r = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < 6; i++) pin.append(r.nextInt(10));
        currentPIN = pin.toString();
        System.out.println("ĐÃ TẠO PIN MỚI: " + currentPIN);
        byte[] pinBytes = currentPIN.getBytes();
        return currentPIN;
    }

    // 2. XÁC THỰC PIN
    public boolean verifyPIN(String pin6) {
        boolean ok = currentPIN.equals(pin6);
        System.out.println("Verify PIN: " + pin6 + " → " + (ok ? "THÀNH CÔNG" : "SAI"));
        return ok;
    }

    // 3. ĐỔI PIN
    public boolean changePIN(String newPin6) {
        if (newPin6.length() != 6) return false;
        currentPIN = newPin6;
        System.out.println("ĐÃ ĐỔI PIN THÀNH: " + currentPIN);
        return true;
    }

    // 4. NẠP TIỀN
    public boolean topup(int amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("NẠP THÀNH CÔNG " + amount + " → SỐ DƯ: " + balance);
            return true;
        }
        return false;
    }

    // 5. LẤY SỐ DƯ
    public long getBalance() {
        return balance;
    }

    // 6. CHECK-IN
    public boolean checkIn() {
        System.out.println("CHECK-IN THÀNH CÔNG!");
        return true;
    }

    // 7. KÝ GIAO DỊCH (GIẢ)
    public byte[] signTransaction(byte type, int amount) {
        return ("SIGN_" + amount + "_" + System.currentTimeMillis()).getBytes();
    }
}