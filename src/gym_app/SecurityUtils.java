package gym_app;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Random;

public class SecurityUtils {
    private static final String KEY = "PowerGym2025Key!"; // 16 bytes cho AES

    // Mã hóa tên, số điện thoại
    public static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return java.util.Base64.getEncoder()
                    .encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    // Giải mã
    public static String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(java.util.Base64.getDecoder().decode(encrypted)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return encrypted;
        }
    }

    // Hash PIN (SHA-256)
    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return pin;
        }
    }

    // Hash SỐ ĐIỆN THOẠI – ĐÂY LÀ HÀM BẠN ĐANG THIẾU!!!
    public static String hashPhone(String phone) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(phone.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return phone;
        }
    }

    // Sinh mã thẻ ngẫu nhiên
    public static String generateCardId() {
        Random r = new Random();
        return String.format("GYM%04d", r.nextInt(10000));
    }
}